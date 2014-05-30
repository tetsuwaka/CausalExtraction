package cabochaParser;

import java.io.*;
import java.util.ArrayList;

public class FileUtilities {

	/**
	 * 指示詞リストをファイルから読み取って返す関数
	 * @return 指示詞のリスト
	 */
	static public String[] readDemonList() {
		ArrayList<String> strings = new ArrayList<String>();
		try {
			File file = new File("src/cabochaParser/demonstrative_list.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while (str != null) {
				strings.add(str);
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		
		// ArrayListからString[]に変換してから、返す
		String[] results = (String[])strings.toArray(new String[strings.size()]);
		return results;
	}
}
