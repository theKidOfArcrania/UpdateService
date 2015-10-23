package updateservice;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	private static final Path UPDATES_PATH = Paths.get(System.getProperty("user.home"), "AppData/Roaming/JarUpdaterService");

	private class DynClassLoader extends ClassLoader {

	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Usage: java -jar UpdateService.jar <jar-file> <program-name>");
			return;
		}

		Path jarFile = Paths.get(args[0]);
		String programName = args[1];

		//Check if jar file exists
		if (!Files.exists(jarFile)) {
			System.out.println("Jar file: " + jarFile + " does not exist.");
			return;
		}

		//Check jar file extension
		String jarName = jarFile.getFileName().toString();
		if (!jarName.endsWith(".jar")) {
			System.out.println("Invalid jar file: " + jarFile);
			return;
		}

		//Create updates directory
		Path updates = UPDATES_PATH.resolve(programName);
		Files.createDirectories(updates);

		//Check any updates
		try (DirectoryStream<Path> updateFiles = Files.newDirectoryStream(updates, "*.class")) {
			ClassLoader
			updateFiles.forEach(path -> {
				ClassLoader.getSystemClassLoader()
			});
		}
	}

}
