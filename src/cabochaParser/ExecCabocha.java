package cabochaParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExecCabocha {

	public static ArrayList<String> exec(String sentence) throws IOException, InterruptedException {
		// Input validation to prevent command injection
		if (sentence == null || sentence.trim().isEmpty()) {
			throw new IllegalArgumentException("Sentence cannot be null or empty");
		}
		
		// Basic sanitization - remove potentially dangerous characters
		sentence = sentence.replaceAll("[;&|`$]", "");
		
		String OS_NAME = System.getProperty("os.name").toLowerCase();
		return OS_NAME.startsWith("windows") ? exec4windows(sentence) : execNormal(sentence);
	}

	public static ArrayList<String> execNormal(String sentence) throws IOException, InterruptedException {
		// Escape the sentence properly to prevent command injection
		sentence = sentence.replace("\"", "\\\"").replace("'", "\\'");
		sentence = "\"" + sentence + "\"";

		List<String> list = new ArrayList<String>();
		list.add("/bin/bash");
		list.add("-c");
		list.add("echo " + sentence + " | cabocha -f1");

		ProcessBuilder pb = new ProcessBuilder(list);
		Process p = pb.start();
		
		// Set a reasonable timeout and handle it properly
		boolean finished = p.waitFor(5, TimeUnit.SECONDS);
		if (!finished) {
			p.destroyForcibly();
			throw new InterruptedException("CaboCha process timed out after 5 seconds");
		}

		ArrayList<String> results = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				results.add(line);
			}
		}

		return results;
	}

	public static ArrayList<String> exec4windows(String sentence) throws IOException, InterruptedException {
		String cabochaPath = "C:\\Program Files (x86)\\CaboCha\\bin\\cabocha.exe";

		ProcessBuilder pb = new ProcessBuilder(cabochaPath, "-f1");
		Process process = pb.start();

		//実行途中で文字列を入力(コマンドプロンプトで文字を入力する操作)
		try (OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream(), "UTF-8")) {
			osw.write(sentence);
			osw.flush();
		}

		// Set a reasonable timeout
		boolean finished = process.waitFor(5, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			throw new InterruptedException("CaboCha process timed out after 5 seconds");
		}

		//出力結果を読み込む
		ArrayList<String> results = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				results.add(line);
			}
		}

		return results;
	}
}
