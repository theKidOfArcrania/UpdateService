package org.shellupdate;

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

import javax.swing.JOptionPane;

public class Updater {
	private static final Properties params = new Properties();

	public static void addUpdate(ProgressDialog progress, String updateName, File newShell, char[] keyStorePass, char[] updaterPass) {
		try {
			progress.setProgressText("Preparing to update.");
			params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

			// Make sure that we can access the updater password.
			URL keyStoreUrl = ClassLoader.getSystemResource(".keystore");
			if (keyStoreUrl == null) {
				JOptionPane.showMessageDialog(progress, "Cannot find keystore.", "Updater", JOptionPane.ERROR_MESSAGE);
				progress.setVisible(false);
				return;
			}

			progress.setProgress(5);

			try {
				KeyStore keyStore = KeyStore.getInstance("jks");
				keyStore.load(keyStoreUrl.openStream(), keyStorePass);
				keyStore.getKey("updater", updaterPass);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(progress, "Incorrect Password", "Updater", JOptionPane.WARNING_MESSAGE);
				progress.setVisible(false);
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
				JarEntry oldEntry = oldVersion.getJarEntry(new String(entry.getName()));

				if (oldEntry == null || entry.getLastModifiedTime().compareTo(oldEntry.getLastModifiedTime()) > 0) {
					long size = entry.getSize();
					entriesVec.addElement(entry);

					if (entry.isDirectory()) {
						continue;
					}

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
				JOptionPane.showMessageDialog(progress, "New version has no changes made.", "Updater", JOptionPane.WARNING_MESSAGE);
				progress.dispose();
			}

		} catch (KeyStoreException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(progress, "Incorrect Password", "Updater", JOptionPane.WARNING_MESSAGE);
			progress.dispose();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(progress, "Unable to update jar.", "Updater", JOptionPane.ERROR_MESSAGE);
			progress.dispose();
		}
	}

	public static void main(String[] args) throws IOException {
		File userDir = new File(System.getProperty("user.dir"));
		System.setErr(new PrintStream(new FileOutputStream(File.createTempFile("err", ".log", userDir))));
		UpdateProgressDialog dlg = new UpdateProgressDialog();
		dlg.setVisible(true);
	}
}
