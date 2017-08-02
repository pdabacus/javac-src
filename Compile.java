import java.io.IOException;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Given paths to java source files as arguments, the Compile class
 * locates the parent src folder and compiles the source files into
 * the neighboring bin folder and then executes them.
 */
public class Compile {

	private static final String buildMsg = "======= built =======";

	private static final String src = "src";
	private static final String bin = "bin";
	private static final String slash = File.separator;
	private static final String javaFileExt = ".java";

	private static final boolean useLastSrc = true;
	private static final boolean printBuildMsg = true;
	private static final boolean execute = true;
	private static final boolean stopOnError = true;

	private static void error(String msg, int err) {
		System.err.println(msg);
		if (stopOnError) {
			System.exit(err);
		}
	}

	public static void compile(String path) throws IOException, InterruptedException {
		Pattern testPath = Pattern.compile("\\" + slash + src + "\\" + slash + ".*?\\" + javaFileExt + "$");
		Pattern testJava = Pattern.compile("\\" + javaFileExt + "$");
		Matcher matcher;
		boolean matchFound;
		int matchStart;
		File testFile;
		String rootPath;
		String relPath;
		String absolutePath;
		Process process;
		int err;

		testFile = new File(path);
		absolutePath = testFile.getAbsolutePath();
		matcher = testPath.matcher(absolutePath);
		if (testFile.exists()) {
			//find src folder
			matchStart = 0;
			if (useLastSrc) {
				matchFound = true;
				while(matchFound) {
					if (matchFound = matcher.find(matchStart)) {
						matchStart++;
					}
				}
				matchStart--;
			}

			if (matchStart >= 0 && matcher.find(matchStart)) {
				relPath = matcher.group(0).substring(src.length() + 2);
				rootPath = absolutePath.substring(0, matcher.start()) + slash;

				//create bin folder
				testFile = new File(rootPath+bin+slash);
				if (!testFile.exists()) {
					System.err.println("Warning: Creating '" + rootPath+bin+slash + "' to hold compiled class");
					testFile.mkdir();
				}

				//compile
				process = (new ProcessBuilder("javac", "-d", rootPath+bin+slash, "-cp", rootPath+src+slash, absolutePath)).inheritIO().start();
				err = process.waitFor();
				if (err != 0) {
					error("Error: Could not compile '" + path + "'", err);
				}

				if (printBuildMsg) {
					System.err.println(buildMsg);
				}

				//execute
				if (execute) {
					relPath = testJava.matcher(relPath).replaceAll("");
					process = (new ProcessBuilder("java", "-cp", rootPath+bin+slash, relPath)).inheritIO().start();
					err = process.waitFor();
					if (err != 0) {
						error("Error: Failed to run '" + path + "'", err);
					}
				}
			} else {
				error("'" + src + "' folder not found in '" + path + "'", 3);
			}
		} else {
			error("Error: '" + javaFileExt + "' source file not found in '" + path + "'", 2);
		}

	}

	public static void main(String[] args) {
		if (args.length < 1) {
			error("Error: No path(s) given", 1);
		}

		for (String path : args) {
			try {
				compile(path);
			} catch (IOException e) {
				error("Error: Process failed", 4);
			} catch (InterruptedException e) {
				error("Error: Process stalled", 5);
			}
		}
	}

}
