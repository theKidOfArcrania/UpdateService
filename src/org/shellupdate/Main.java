package org.shellupdate;

import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import net.jimmc.jshortcut.JShellLink;

public class Main {

	public static JShellLink createDesktopShortcut(String filePath, String iconLocation, int iconIndex) {
		JShellLink link = new JShellLink();
		link.setFolder(JShellLink.getDirectory("desktop"));
		link.setName("Stuff");
		link.setPath(filePath);
		link.setIconLocation(iconLocation);
		link.setIconIndex(iconIndex);
		link.save();
		return link;
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			// Set up the updater.
			Properties params = new Properties();
			params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

			Path shellAppPath = Paths.get(System.getenv("HOMEDRIVE"), System.getenv("HOMEPATH"), params.getProperty("shell.name"));
			Path shellJarPath = shellAppPath.resolve("Shell.jar");
			Path shellIconPath = shellAppPath.resolve("AppIcon.ico");

			Path installJarPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			if (Files.exists(shellAppPath)) {
				return;
			}

			BufferedImage splash = ImageHelper.loadImage("org/shellupdate/About.png");
			Window splashScreen = new Window(null) {
				private static final long serialVersionUID = 8733767680868899639L;

				@Override
				public void paint(Graphics g) {
					g.drawImage(splash, 0, 0, this);
				}
			};
			splashScreen.setSize(splash.getWidth(), splash.getHeight());
			splashScreen.setVisible(true);
			splashScreen.setLocationRelativeTo(null);

			Files.createDirectory(shellAppPath);
			Files.copy(installJarPath, shellJarPath);

			@SuppressWarnings("resource")
			InputStream shellIcon = ClassLoader.getSystemResourceAsStream(params.getProperty("shell.icon.path"));
			if (shellIcon != null) {
				Files.copy(shellIcon, shellIconPath);
				createDesktopShortcut(shellJarPath.toString(), shellIconPath.toString(), 0);
				shellIcon.close();
			} else {
				createDesktopShortcut(shellJarPath.toString(), "java.exe", 0);
			}
			splashScreen.dispose();
		} else {
			if (args[0].equals("-shell") || args[0].equals("-s")) {
				Shell.run(new String[0]);
			} else if (args[0].equals("-update") || args[0].equals("-u")) {
				Updater.run(new String[0]);
			}
		}
	}
}
