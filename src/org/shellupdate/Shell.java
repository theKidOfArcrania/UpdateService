package org.shellupdate;

import java.awt.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.shellupdate.ui.ProgressDialog;
import org.shellupdate.ui.ProgressViewer;
import org.shellupdate.ui.SplashLoadDialog;

public class Shell {

	private static Properties params = new Properties();

	private static X509Certificate updateCert = null;

	/**
	 * Performs a check on the new update based on the updater.crt data and then merges the updates
	 *
	 * @param updateSource the new jar source update for the program
	 * @param programPath the path of current program to update
	 * @param progress the progress monitor variable.
	 * @return the version of this update
	 * @throws IOException if an I/O exception occurs
	 * @throws CertificateException if the updates certification cannot be loaded
	 * @throws SecurityException if a verification error occurs.
	 */
	public static final synchronized Version doUpdate(URL updateSource, Path programPath, ValueChange<Double> progress)
			throws CertificateException, IOException, SecurityException {

		// Open a connnection to the provider JAR file
		JarVerifier jv = new JarVerifier(updateSource);

		Path tempUpgradePath = Files.createTempDirectory("update");
		if (updateCert == null) {
			updateCert = getUpdatesCert();
		}

		// Make sure that the provider JAR file is signed with the "updater" signing certificate.
		Version current = jv.verifyAndLoad(updateCert, tempUpgradePath, progress);
		moveFolder(tempUpgradePath, programPath, StandardCopyOption.REPLACE_EXISTING);
		return current;
	}

	public static final String[] getUpdates(JarFile program, Version current) throws IOException {
		JarEntry updateFiles = program.getJarEntry("VERSION");
		if (updateFiles == null) {
			return new String[0];
		}

		try (DataInputStream dis = new DataInputStream(program.getInputStream(updateFiles))) {
			current.readVersion(dis);
			int updatesLen = dis.readInt();
			String[] updates = new String[updatesLen];

			for (int i = 0; i < updatesLen; i++) {
				updates[i] = dis.readUTF();
			}

			return updates;
		}
	}

	@SuppressWarnings("resource")
	public static void run(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.setErr(new PrintStream(new FileOutputStream(File.createTempFile("err", ".log"))));
		params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

		String shellName = params.getProperty("shell.name");
		File shellFile = new File(params.getProperty("shell.path"));
		ProgressViewer progView;

		Path updShellPath = Files.createTempDirectory("program");
		boolean updated = false;
		boolean hasManifest = false;
		Version current = new Version();

		if (!shellFile.exists()) {
			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(shellFile))) {
				// Create an empty directory.
			}
		}

		try (JarFile shellJarFile = new JarFile(shellFile)) {
			hasManifest = shellJarFile.getManifest() == null;
			try {
				SplashLoadDialog progDlg = new SplashLoadDialog(null, ImageHelper.loadImage("org/shellupdate/About.png"));
				progDlg.setVisible(true);
				progDlg.setLocationRelativeTo(null);
				progView = progDlg;
			} catch (IOException e1) {
				e1.printStackTrace();
				ProgressDialog progDlg = new ProgressDialog(null, "Opening " + params.getProperty("shell.name", "program"));
				progDlg.setVisible(true);
				progView = progDlg;
			}

			progView.setProgress(1);
			progView.setProgressText("Fetching updates...");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			// check for updates
			File updatePath = new File(params.getProperty("update.path"));

			if (updatePath.exists()) {

				// Get available updates
				File[] availUpdates = updatePath.listFiles(file -> file.getName().endsWith(".upd"));

				LinkedList<String> updatesBefore = new LinkedList<>();
				ArrayList<File> updatesToDo = new ArrayList<>();
				ArrayList<String> updatesCompleted = new ArrayList<>();

				// Get updates that we already have.
				updatesBefore.addAll(Arrays.asList(getUpdates(shellJarFile, current)));

				// Catelog updates
				for (File update : availUpdates) {
					String updateName = Shell.getUpdateName(update);

					if (!updatesBefore.isEmpty() && updatesBefore.remove(updateName)) {
						// This version has this update already.
						updatesCompleted.add(updateName);
					} else {
						// We need to add to the to-do-list of updates.
						updatesToDo.add(update);
					}
				}

				if (!updatesToDo.isEmpty()) {
					updatesToDo.sort(Comparator.comparing(File::lastModified));

					progView.setProgress(5);
					progView.setProgressText("Preparing to update...");
					// Explode the contents of jar file into a temp directory.
					copyTempFiles(shellJarFile, updShellPath, progView.progressProperty(.05, .2));

					double applyProgress = .2;
					double updateIncr = .8 / updatesToDo.size();

					// loop through updates and apply them as it comes.
					for (File update : updatesToDo) {
						String updateName = getUpdateName(update);

						progView.setProgressText("Applying update " + updateName + ".");
						updatesCompleted.add(Shell.getUpdateName(update));

						try {
							current = doUpdate(update.toURI().toURL(), updShellPath, progView.progressProperty(applyProgress, applyProgress + updateIncr));
							updated = true;
						} catch (CertificateException | SecurityException e2) {
							e2.printStackTrace();
							String errMsg = "Update " + updateName + " has failed to install due to some security issues.";
							JOptionPane.showMessageDialog((Component) progView, errMsg, shellName + " Updater", JOptionPane.ERROR_MESSAGE);
							System.err.println(errMsg);
						}

						applyProgress += updateIncr;

					}
					writeNewVersion(Files.newOutputStream(updShellPath.resolve("VERSION")), current, updatesCompleted);
				}
			} else {
				progView.setProgress(30);
			}
		}

		// Copy the new updates back into the program jar file.
		if (updated) {
			progView.setProgressText("Copying update applies...");
			boolean error[] = { false };

			try (JarOutputStream jos = initJar(new FileOutputStream(shellFile), !hasManifest)) {
				byte[] buffer = new byte[8192];
				jos.setLevel(9);

				Files.walk(updShellPath).forEach(entryPath -> {
					if (Files.isDirectory(entryPath)) {
						return;
					}

					int lenRead;
					try {
						ZipEntry entry = new ZipEntry(updShellPath.relativize(entryPath).toString().replace('\\', '/'));
						entry.setLastModifiedTime(Files.getLastModifiedTime(entryPath));
						jos.putNextEntry(entry);

						try (InputStream in = Files.newInputStream(entryPath)) {
							while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
								jos.write(buffer, 0, lenRead);
							}
						} finally {
							jos.closeEntry();
						}

						Files.delete(entryPath);
					} catch (IOException e) {
						error[0] = true;
						e.printStackTrace();
					}
				});
			} finally {
				if (error[0]) {
					JOptionPane.showMessageDialog((Component) progView, "Some files could not be copied...", "Updater", JOptionPane.ERROR_MESSAGE);
				}
			}

		}

		// Delete entire update folder.
		deleteFolder(updShellPath);
		progView.finish();

		ProcessBuilder shell = new ProcessBuilder();
		Process program = shell.command("java", "-jar", shellFile.getAbsolutePath()).inheritIO().start();
	}

	public static final void writeNewVersion(OutputStream out, Version current, List<String> updates) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(out)) {
			current.writeVersion(dos);
			dos.writeInt(updates.size());
			for (String update : updates) {
				dos.writeUTF(update);
			}
		}
	}

	private static void copyTempFiles(JarFile shellJarFile, Path updShellPath, ValueChange<Double> progress) throws IOException {

		long totalShellSize = 0;
		long copyShellSize = 0;

		byte[] buffer = new byte[8192];
		Enumeration<JarEntry> entries = shellJarFile.entries();
		Vector<JarEntry> entriesVec = new Vector<>();

		// Get size stuff and add directories.
		while (entries.hasMoreElements()) {
			JarEntry je = entries.nextElement();

			// Add directory
			if (je.isDirectory()) {
				Files.createDirectories(updShellPath.resolve(je.getName()));
				continue;
			}

			long size = je.getSize();
			entriesVec.addElement(je);

			if (size == -1) {
				totalShellSize += 1000;
			} else {
				totalShellSize += size;
			}
		}

		Enumeration<JarEntry> e = entriesVec.elements();

		// Write the output
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();

			Files.createDirectories(updShellPath.resolve(je.getName()).getParent());
			OutputStream out = Files.newOutputStream(updShellPath.resolve(je.getName()));
			InputStream in = shellJarFile.getInputStream(je);

			// Read in each jar entry and copy to temp folder.
			int lenRead;
			while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, lenRead);
				copyShellSize += lenRead;
				progress.setValue((double) copyShellSize / totalShellSize);
			}
			out.close();
			in.close();
		}
	}

	private static void deleteFolder(Path folder) throws IOException {
		Files.walkFileTree(folder, new FileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static String getUpdateName(File updateFile) {
		String fileName = updateFile.getName();
		return fileName.substring(0, fileName.length() - 4);
	}

	private static X509Certificate getUpdatesCert() throws IOException, CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		try (InputStream in = ClassLoader.getSystemResourceAsStream(params.getProperty("update.cert", "updater.crt"))) {
			X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
			return cert;
		}
	}

	private static JarOutputStream initJar(OutputStream os, boolean addManifest) throws IOException {
		if (addManifest) {
			Manifest mf = new Manifest();
			Attributes main = mf.getMainAttributes();
			main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
			main.put(Attributes.Name.CLASS_PATH, ".");
			main.put(Attributes.Name.MAIN_CLASS, params.getProperty("shell.main"));
			return new JarOutputStream(os, mf);
		} else {
			return new JarOutputStream(os);
		}
	}

	private static void moveFolder(Path src, Path dest, CopyOption... options) throws IOException {
		Files.walkFileTree(src, new FileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(dest.resolve(src.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path srcFile, BasicFileAttributes attrs) throws IOException {
				Files.move(srcFile, dest.resolve(src.relativize(srcFile)), options);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private Shell() {

	}
}
