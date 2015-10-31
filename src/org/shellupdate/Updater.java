package org.shellupdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.swing.JOptionPane;

public class Updater {
	private static Properties params;

	public static void addUpdate(ProgressDialog progress, String updateName, File newShell, char[] keyStorePass, char[] updaterPass) {
		try {
			progress.setProgressText("Preparing to update.");
			progress.setVisible(true);

			params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

			// Make sure that we can access the updater password.
			URL keyStoreUrl = ClassLoader.getSystemResource(".keystore");
			if (keyStoreUrl == null) {
				JOptionPane.showMessageDialog(progress, "Cannot find keystore.", "Updater", JOptionPane.ERROR_MESSAGE);
				return;
			}

			progress.setProgress(5);

			KeyStore keyStore = KeyStore.getInstance("jks", "sun");
			keyStore.load(keyStoreUrl.openStream(), keyStorePass);
			keyStore.getKey("updater", updaterPass);

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
				JarEntry je = entries.nextElement();

				long size = je.getSize();
				entriesVec.addElement(je);

				if (je.isDirectory()) {
					continue;
				}

				if (size == -1) {
					updateSize += 1000;
				} else {
					updateSize += size;
				}
			}
			ValueChange<Double> updateProgress = progress.progressProperty(.11, .9);
			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(updateFile))) {
				for (JarEntry entry : entriesVec) {
					JarEntry oldEntry = oldVersion.getJarEntry(entry.getName());
					if (oldEntry == null || entry.getLastModifiedTime().compareTo(oldEntry.getLastModifiedTime()) > 0) {
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

					} else if (!entry.isDirectory()) {
						if (entry.getSize() == -1) {
							updateSize += 1000;
						} else {
							updateSize += entry.getSize();
						}
						updateProgress.setValue((double) updateRead / updateSize);
					}
				}
			}
			oldVersion.close();
			newVersion.close();
			if (!oldShell.equals(newShell)) {
				Files.move(newShell.toPath(), oldShell.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			progress.setProgressText("Signing update file...");

			sun.security.tools.jarsigner.Main.main(new String[] { "-keystore", keyStoreUrl.toExternalForm(), "-storepasswd", new String(updaterPass),
					"-keypasswd", new String(keyStorePass), updateFile.toString(), "updater", });
			System.exit(0);
		} catch (KeyStoreException e) {
			JOptionPane.showMessageDialog(progress, "Incorrect Password", "Updater", JOptionPane.WARNING_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(progress, "Unable to update jar: " + e.getMessage(), "Updater", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void main(String[] args) throws IOException {

	}
}
