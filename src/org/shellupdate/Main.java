package org.shellupdate;

import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import net.jimmc.jshortcut.JShellLink;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			// Set up the updater.
			Properties params = new Properties();
			params.load(ClassLoader.getSystemResourceAsStream("params.PROPERTIES"));

			Path shellAppPath = Paths.get(System.getenv("APPDATA"), params.getProperty("shell.name"));
			Path shellJarPath = shellAppPath.resolve("Shell.jar");
			Path installJarPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			if (Files.exists(shellAppPath)) {
				Shell.run(new String[0]);
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
			ClassLoader.get
			Files.copy(, target, options)

			splashScreen.dispose();
		} else {
			if (args[0].equals("-shell") || args[0].equals("-s")) {
				Shell.run(new String[0]);
			} else if (args[0].equals("-update") || args[0].equals("-u")) {
				Updater.run(new String[0]);
			}
		}
	}

	public static JShellLink createDesktopShortcut(Path filePath, Path icon) {
		try {
			JShellLink link = new JShellLink();
			link.setFolder(JShellLink.getDirectory("desktop"));
			link.setName("Stuff");
			link.setPath(filePath.toString());
			link.setIconLocation("java.exe");
			link.save();
			return link;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
