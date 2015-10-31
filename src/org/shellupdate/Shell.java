package org.shellupdate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;

public class Shell {

	private static Properties params = new Properties();

	private static X509Certificate updateCert = null;

	/**
	 * Performs a check on the new update based on the updater.crt data and then merges the updates
	 *
	 * @param updateSource the new jar source update for the program
	 * @param programPath the path of current program to update
	 * @param progress the progress monitor variable.
	 * @return true if it is verified, false if it is not verified.
	 * @throws IOException if an I/O exception occurs
	 * @throws CertificateException if the updates certification cannot be loaded
	 * @throws SecurityException if a verification error occurs.
	 */
	public static final synchronized boolean doUpdate(URL updateSource, Path programPath, ValueChange<Double> progress)
			throws CertificateException, IOException, SecurityException {
		if (updateSource == null) {
			return false;
		}

		// Open a connnection to the provider JAR file
		JarVerifier jv = new JarVerifier(updateSource);

		Path tempUpgradePath = Files.createTempDirectory("update");
		if (updateCert == null) {
			updateCert = getUpdatesCert();
		}

		// Make sure that the provider JAR file is signed with the "updater" signing certificate.
		jv.verifyAndLoad(updateCert, tempUpgradePath, progress);

		Files.move(tempUpgradePath, programPath, StandardCopyOption.REPLACE_EXISTING);

		return true;
	}

	public static final String[] getUpdates(JarFile program) throws IOException {
		JarEntry updateFiles = program.getJarEntry("VERSION");
		if (updateFiles == null) {
			return new String[0];
		}

		DataInputStream dis = new DataInputStream(program.getInputStream(updateFiles));
		int updatesLen = dis.readInt();
		String[] updates = new String[updatesLen];

		for (int i = 0; i < updatesLen; i++) {
			updates[i] = dis.readUTF();
		}

		return updates;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(System.getProperty("user.dir"));
		params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

		String shellName = params.getProperty("shell.name");
		File shellFile = new File(params.getProperty("shell.path"));
		JarFile shellJarFile = new JarFile(shellFile);
		Path updShellPath = Files.createTempDirectory("program");

		ProgressDialog progDialog = new ProgressDialog("Opening " + shellName);
		progDialog.setVisible(true);
		progDialog.setProgressText("Fetching updates...");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// Copy the contents of jar file into a temp directory.
		copyTempFiles(shellJarFile, updShellPath, progDialog.progressProperty(0, .2));

		boolean updated = false;
		File updatePath = new File(params.getProperty("update.path"));
		if (updatePath.exists()) {

			// Get available updates
			File[] updates = updatePath.listFiles(file -> file.getName().endsWith(".upd"));
			String[] updatesName = Arrays.stream(updates).map(Shell::getUpdateName).toArray(String[]::new);
			String[] version = getUpdates(shellJarFile);
			Arrays.sort(updates, Comparator.comparing(File::lastModified));
			Arrays.sort(version);

			double applyProgress = .2;
			double updateIncr = .8 / updates.length;

			// loop through updates and apply them as it comes.
			for (File update : updates) {
				String updateName = getUpdateName(update);
				if (Arrays.binarySearch(version, updateName) >= 0) {
					applyProgress += updateIncr;
					continue;
				}

				updated = true;
				progDialog.setProgressText("Applying update " + updateName);

				try {
					doUpdate(update.toURI().toURL(), updShellPath, progDialog.progressProperty(applyProgress, applyProgress + updateIncr));
				} catch (CertificateException | SecurityException e2) {
					String errMsg = "Update " + updateName + " has failed to install due to some security issues.";
					JOptionPane.showMessageDialog(progDialog, errMsg, shellName + " Updater", JOptionPane.ERROR_MESSAGE);
					System.err.println(errMsg);
					e2.printStackTrace();
				}

				applyProgress += updateIncr;
			}
			writeNewVersion(Files.newOutputStream(updShellPath.resolve("VERSION")), updatesName);
		}
		shellJarFile.close();

		if (updated) {
			progDialog.setProgressText("Copying update applies...");
			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(shellFile))) {
				byte[] buffer = new byte[8192];

				Files.walk(updShellPath).forEach(path -> {
					int lenRead;
					try {
						ZipEntry entry = new ZipEntry(path.relativize(updShellPath).toString());
						jos.putNextEntry(entry);
						try (InputStream in = Files.newInputStream(path)) {
							while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
								jos.write(buffer, 0, lenRead);
							}
							jos.closeEntry();
						}
					} catch (IOException e) {
						System.err.println("Unable to copy " + path.relativize(updShellPath) + "dir");
					}
				});
			}
		}

		// Delete entire update folder.
		Files.walkFileTree(updShellPath, new FileVisitor<Path>() {
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

		Files.deleteIfExists(updShellPath);

		progDialog.dispose();
	}

	public static final void writeNewVersion(OutputStream out, String[] updates) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		dos.writeInt(updates.length);
		for (String update : updates) {
			dos.writeUTF(update);
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
				System.out.println(je.getName());
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

	private Shell() {

	}
}
