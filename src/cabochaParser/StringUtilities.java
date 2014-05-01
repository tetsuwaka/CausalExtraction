package cabochaParser;

import java.util.List;

public class StringUtilities {
	public static String join(String delimiter, List<String> list) {
		String line = "";
		int i = 0;
		for (String s : list) {
			if (i == 0) {
				line += s;
				i++;
			} else {
				line += delimiter + s;
			}
		}
		return line;
	}
}
