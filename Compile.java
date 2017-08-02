import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
		PrintThread printer;
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
				process = (new ProcessBuilder("javac", "-d", rootPath+bin+slash, "-cp", rootPath+src+slash, absolutePath)).redirectErrorStream(true).start();
				printer = new PrintThread(process.getInputStream());
				printer.start();
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
					process = (new ProcessBuilder("java", "-cp", rootPath+bin+slash, relPath)).redirectErrorStream(true).start();
					printer = new PrintThread(process.getInputStream());
					printer.start();
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

/**
 * Given an InputStream and an optional error message, the PrintThread class
 * prints the stream to the console when the thread is started.
 */
class PrintThread extends Thread {
	InputStream in;
	String errMsg;
	public PrintThread(InputStream in) {
		super();
		this.in = in;
		this.errMsg = "Error: Could not read input stream";
	}
	public PrintThread(InputStream in, String errMsg) {
		super();
		this.in = in;
		this.errMsg = errMsg;
	}
	public void run() {
		try {
			InputStreamReader streamReader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(streamReader);
			String line = bufferedReader.readLine();
			while (line != null) {
				System.out.println(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			streamReader.close();
			in.close();
		} catch (IOException e) {
			System.err.println(errMsg);
		}
	}
}
