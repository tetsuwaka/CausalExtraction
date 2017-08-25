package cabochaParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExecCabocha {

	public static ArrayList<String> exec(String sentence)  throws IOException, InterruptedException {
		String OS_NAME = System.getProperty("os.name").toLowerCase();
		return OS_NAME.startsWith("windows") ? exec4windows(sentence) : execNormal(sentence);
	}

	public static ArrayList<String> execNormal(String sentence) throws IOException, InterruptedException{
		sentence = "\"" + sentence + "\"";

		List<String> list = new ArrayList<String>();
		list.add("/bin/bash");
		list.add("-c");
		list.add("echo " + sentence + " | cabocha -f1");

		ProcessBuilder pb = new ProcessBuilder(list);
		Process p = pb.start();
		p.waitFor();

		ArrayList<String> results = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
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
		OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
		osw.write(sentence);
		osw.close();

		//出力結果を読み込む
		InputStream is = process.getInputStream();
		ArrayList<String> results = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				results.add(line);
			}
		}

		return results;
	}
}
