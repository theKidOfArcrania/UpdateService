package org.shellupdate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import javax.swing.JOptionPane;

import net.jimmc.jshortcut.JShellLink;

public class Main {

	public static JShellLink createDesktopShortcut(String filePath, String name, String iconLocation, int iconIndex, String arguments) {
		JShellLink link = new JShellLink();
		link.setFolder(JShellLink.getDirectory("desktop"));
		link.setName(name);
		link.setArguments(arguments);
		link.setPath(filePath);
		link.setIconLocation(iconLocation);
		link.setIconIndex(iconIndex);
		link.save();
		return link;
	}

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				// Set up the updater.
				Properties params = new Properties();
				params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

				String shellName = params.getProperty("shell.name");
				Path shellAppPath = Paths.get(System.getenv("HOMEDRIVE"), System.getenv("HOMEPATH"), shellName);
				Path shellJarPath = shellAppPath.resolve("Shell.jar");
				Path shellIconPath = shellAppPath.resolve("AppIcon.ico");

				Path installJarPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

				if (Files.exists(shellAppPath)) {
					if (JOptionPane.showConfirmDialog(null, shellName
							+ " already is installed, would you like to reinstall program?", "Installer", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
						deleteFolder(shellAppPath);
					}
				}

				Files.createDirectory(shellAppPath);
				Files.copy(installJarPath, shellJarPath);

				@SuppressWarnings("resource")
				InputStream shellIcon = ClassLoader.getSystemResourceAsStream(params.getProperty("shell.icon.path", ""));
				if (shellIcon != null) {
					Files.copy(shellIcon, shellIconPath);
					createDesktopShortcut(shellJarPath.toString(), shellName, shellIconPath.toString(), 0, "-shell");
					shellIcon.close();
				} else {
					createDesktopShortcut(shellJarPath.toString(), shellName, "java.exe", 0, "-shell");
				}

				ProcessBuilder shell = new ProcessBuilder();
				Process program = shell.command("java", "-jar", shellJarPath.toString()).directory(shellAppPath.toFile()).inheritIO().start();
			} else {
				if (args[0].equalsIgnoreCase("-shell") || args[0].equals("-s")) {
					Shell.run(new String[0]);
					System.exit(0);
				} else if (args[0].equalsIgnoreCase("-update") || args[0].equals("-u")) {
					Updater.run(new String[0]);
				}
			}
		} catch (Exception e) {
			// TO DO: better error reporting.
			e.printStackTrace();
			System.exit(1);
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
}
