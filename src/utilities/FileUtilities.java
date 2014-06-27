package utilities;

import java.io.*;
import java.util.ArrayList;

public class FileUtilities {

	/**
	 * 指定したファイルを一行ずつ読み込み、Stringの配列に入れて返す
	 * @param filePath ファイルの絶対パス
	 * @return ファイルの内容を１行ずつStringの配列に入れたもの
	 */
	static public String[] readLines(String filePath) {
		ArrayList<String> strings = new ArrayList<String>();
		try {
			File file = new File(filePath);
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
		return (String[])strings.toArray(new String[strings.size()]);
	}

	/**
	 * 手がかり表現のリストをファイルから読み取って返す関数
	 * @return 手がかり表現のリスト
	 */
	static public ArrayList<String[]> readClueList() {
		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<String> endClues = new ArrayList<String>();
		try {
			File file = new File("src/extractCausal/clue_list.txt");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while (str != null) {
				String[] temp = str.split("]");
				strings.add(temp[1]);
				if (str.startsWith("[E]")) {
					endClues.add(temp[1]);
				}
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

		// ArrayListからString[]に変換してから、返す
		String[] clues = (String[])strings.toArray(new String[strings.size()]);
		String[] eclues = (String[])endClues.toArray(new String[endClues.size()]);
		ArrayList<String[]> result = new ArrayList<String[]>(2);
		result.add(clues);
		result.add(eclues);
		return result;
	}
}