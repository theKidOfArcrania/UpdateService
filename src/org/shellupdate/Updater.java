package org.shellupdate;

import java.awt.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.shellupdate.ui.ProgressViewer;
import org.shellupdate.ui.UpdateDialog;

public class Updater {
	public static final Properties params = new Properties();

	public static void addUpdate(ProgressViewer progress, String updateName, File newShell, Version latest, char[] keyStorePass, char[] updaterPass) {
		try {
			URL keyStoreUrl = ClassLoader.getSystemResource(".keystore");
			progress.setProgressText("Preparing to update.");

			if (!verifyUpdateID((Component) progress, keyStorePass, updaterPass)) {
				progress.finish();
				return;
			}

			progress.setProgress(10);

			File oldShell = new File(params.getProperty("shell.path"));
			if (!newShell.exists()) {
				throw new FileNotFoundException(newShell.toString());
			}
			File updateFile = new File(params.getProperty("update.path"), updateName + ".upd");
			int lenRead;
			byte[] buffer = new byte[8196];
			JarFile oldVersion = new JarFile(oldShell);
			JarFile newVersion = new JarFile(newShell);

			progress.setProgress(11);

			long updateRead = 0;
			long updateSize = 0;
			Vector<JarEntry> entriesVec = new Vector<>();
			Enumeration<JarEntry> entries = newVersion.entries();
			// Get size stuff and add directories.
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				JarEntry oldEntry = oldVersion.getJarEntry(entry.getName());

				if (oldEntry == null) {
					// Retrieve entries with back-slashes instead of forward slashes?
					oldEntry = oldVersion.getJarEntry(entry.getName().replace('/', '\\'));
				}

				if (entry.getName().equalsIgnoreCase("VERSION")) {
					continue;
				}

				if (oldEntry == null || entry.getLastModifiedTime().compareTo(oldEntry.getLastModifiedTime()) > 0) {
					long size = entry.getSize();

					if (entry.isDirectory()) {
						continue;
					}

					entriesVec.addElement(entry);

					if (size == -1) {
						updateSize += 1000;
					} else {
						updateSize += size;
					}
				}
			}
			ValueChange<Double> updateProgress = progress.progressProperty(.11, .9);
			updateFile.getParentFile().mkdirs();
			updateFile.createNewFile();

			boolean changed = false;

			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(updateFile))) {
				for (JarEntry entry : entriesVec) {
					changed = true;
					jos.putNextEntry(entry);
					progress.setProgressText("Adding " + entry.getName() + " to update.");
					try (InputStream in = newVersion.getInputStream(entry)) {
						while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
							jos.write(buffer, 0, lenRead);
							updateRead += lenRead;
							updateProgress.setValue((double) updateRead / updateSize);
						}
						jos.closeEntry();
					}
				}
				jos.putNextEntry(new ZipEntry("VERSION"));
				latest.writeVersion(new DataOutputStream(jos));
			}
			oldVersion.close();
			newVersion.close();

			if (changed) {
				// Files.move(newShell.toPath(), oldShell.toPath(), StandardCopyOption.REPLACE_EXISTING);
				progress.setProgressText("Signing update file...");

				sun.security.tools.jarsigner.Main.main(new String[] { "-keystore", keyStoreUrl.toExternalForm(), "-storepass", new String(keyStorePass),
						"-keypass", new String(updaterPass), updateFile.toString(), "updater", });
				System.exit(0);
			} else {
				updateFile.delete();
				JOptionPane.showMessageDialog((Component) progress, "New version has no changes made.", "Updater", JOptionPane.WARNING_MESSAGE);
				progress.finish();
			}

		} catch (KeyStoreException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog((Component) progress, "Incorrect Password", "Updater", JOptionPane.WARNING_MESSAGE);
			progress.finish();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog((Component) progress, "Unable to update jar.", "Updater", JOptionPane.ERROR_MESSAGE);
			progress.finish();
		}
	}

	public static Version getLastestVersion() {
		String updatePath = Updater.params.getProperty("update.path");
		if (updatePath != null) {
			File updateFile = new File(updatePath);
			File[] availUpdates = updateFile.listFiles(file -> file.getName().endsWith(".upd"));

			if (availUpdates == null) {
				return new Version(1, 0, 1, false);
			} else {
				Version max = new Version(1, 0, availUpdates.length + 1, false);
				for (File availUpdate : availUpdates) {
					try {
						JarFile update = new JarFile(availUpdate, false);
						ZipEntry versionFile = update.getEntry("VERSION");
						DataInputStream dis = new DataInputStream(update.getInputStream(versionFile));

						Version version = new Version();
						version.readVersion(dis);
						version.increment();
						max = Version.latestVersion(max, version);
					} catch (Exception e) {
						e.printStackTrace();
						// Silently ignore any errors. We don't care about them at this point.
					}
				}

				return max;
			}
		} else {
			return new Version();
		}
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));
		System.setErr(new PrintStream(new FileOutputStream(File.createTempFile("err", ".log"))));
		UpdateDialog dlg = new UpdateDialog();
		dlg.setVisible(true);
	}

	public static boolean verifyUpdateID(Component parent, char[] keyStorePass, char[] updaterPass) {
		// Make sure that we can access the updater password.
		URL keyStoreUrl = ClassLoader.getSystemResource(".keystore");
		if (keyStoreUrl == null) {
			JOptionPane.showMessageDialog(parent, "Cannot find keystore.", "Updater", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			KeyStore keyStore = KeyStore.getInstance("jks");
			keyStore.load(keyStoreUrl.openStream(), keyStorePass);
			keyStore.getKey("updater", updaterPass);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Incorrect Password", "Updater", JOptionPane.WARNING_MESSAGE);

			return false;
		}
		return true;
	}
}