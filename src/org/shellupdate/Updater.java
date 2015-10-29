package org.shellupdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Updater {
	private static Properties params;

	public static void main(String[] args) throws IOException {
		params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

		Scanner scrn = new Scanner(System.in);
		if (args.length != 1) {
			System.out.println("Usage: java -jar Update.jar [New-jar version]");
			System.exit(1);
		}

		File oldShell = new File(params.getProperty("shell.path"));
		File newShell = new File(args[0]);

		if (!newShell.exists()) {
			System.out.println("File does not exist");
			System.exit(1);
		}

		File updateFile = new File("");
		String updateName = "";
		while (updateName.matches("[A-Za-z1-0]+") && updateFile.exists()) {
			System.out.print("Enter update name: ");
			updateName = scrn.nextLine();
			if (updateName.isEmpty()) {
				return;
			}
			updateFile = new File(params.getProperty("update.path"), updateName + ".upd");
		}

		System.out.println();

		try {
			int lenRead;
			byte[] buffer = new byte[8196];

			JarFile oldVersion = new JarFile(oldShell);
			JarFile newVersion = new JarFile(newShell);

			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(updateFile))) {
				Enumeration<JarEntry> entries = newVersion.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					JarEntry oldEntry = oldVersion.getJarEntry(entry.getName());
					if (oldEntry == null || entry.getLastModifiedTime().compareTo(oldEntry.getLastModifiedTime()) > 0) {
						// TO DO: sign updates.
						jos.putNextEntry(entry);
						try (InputStream in = newVersion.getInputStream(entry)) {
							while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
								jos.write(buffer, 0, lenRead);
							}
							jos.closeEntry();
						}
						System.out.println("Adding " + entry.getName() + " to update " + ".");
					}
				}
			}

			oldVersion.close();
			newVersion.close();

			if (!oldShell.equals(newShell)) {
				Files.move(newShell.toPath(), oldShell.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			System.out.println("Signing update file...");
			URL keyStoreUrl = ClassLoader.getSystemResource(".keystore");
			KeyStore keyStore = KeyStore.getInstance("jks");

			System.out.print("Enter password: ");
			// TO DO: query passwords before doing jar signing with ui.
			keyStoreUrl.openStream();
			sun.security.tools.jarsigner.Main.main(new String[] { "-keystore", keyStoreUrl.toExternalForm(), updateFile.toString(), "updater" });
		} catch (IOException e) {
			System.out.println("Not a valid jar file.");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
}
