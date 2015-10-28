package org.shellupdate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Updater {
	private static Properties params;

	public static void main(String[] args) throws IOException {
		params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

		Scanner in = new Scanner(System.in);
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
			updateName = in.nextLine();
			if (updateName.isEmpty()) {
				return;
			}
			updateFile = new File(params.getProperty("update.path"), updateName + ".upd");
		}

		System.out.println();

		try {
			JarFile oldVersion = new JarFile(oldShell);
			JarFile newVersion = new JarFile(newShell);

			Enumeration<JarEntry> entries = newVersion.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				JarEntry oldEntry = oldVersion.getJarEntry(entry.getName());
				if (oldEntry == null || entry.getLastModifiedTime().compareTo(oldEntry.getLastModifiedTime()) > 0) {
					// TO DO: Copy new entry to an update jar file.
					System.out.println("Adding " + entry.getName() + " to update " + ".");
				}
			}

			oldVersion.close();
			newVersion.close();

			if (!oldShell.equals(newShell)) {
				Files.move(newShell.toPath(), oldShell.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			System.out.println("Not a valid jar file.");
			e.printStackTrace();
			System.exit(1);
		}

	}
}
