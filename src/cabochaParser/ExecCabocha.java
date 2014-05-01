package cabochaParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecCabocha {
	public static ArrayList<String> exec(String sentence) throws IOException, InterruptedException{ 
		sentence = "\"" + sentence + "\"";
		
		List<String> list = new ArrayList<String>();
		list.add("/bin/bash");
		list.add("-c");
		list.add("echo " + sentence + " | /usr/local/bin/cabocha -f1");

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
}
