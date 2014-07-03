package utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

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
				if (str.equals("")) {
					continue;
				}
				strings.add(str);
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		// ArrayListからString[]に変換してから、返す
		return (String[])strings.toArray(new String[strings.size()]);
	}

	/**
	 * 手がかり表現のリストをファイルから読み取って返す関数
	 * @return 手がかり表現のリスト
	 */
	static public ArrayList<String[]> readClueList(String filePath) {
		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<String> endClues = new ArrayList<String>();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while (str != null) {
				if (str.equals("")) {
					continue;
				}
				String[] temp = str.split("]");
				strings.add(temp[1]);
				if (str.startsWith("[E]")) {
					endClues.add(temp[1]);
				}
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		// ArrayListからString[]に変換してから、返す
		String[] clues = (String[])strings.toArray(new String[strings.size()]);
		String[] eclues = (String[])endClues.toArray(new String[endClues.size()]);
		ArrayList<String[]> result = new ArrayList<String[]>(2);
		result.add(clues);
		result.add(eclues);
		return result;
	}
	
	/**
	 * 手がかり表現とPrefixPatternのリストをファイルから読み取って返す関数
	 * @return 手がかり表現とPrefixPatternのリスト
	 */
	static public ArrayList<String[]> readAdditionalData(String filePath) {
		ArrayList<String> clues = new ArrayList<String>();
		ArrayList<String> patterns = new ArrayList<String>();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while (str != null) {
				if (str.equals("")) {
					continue;
				}
				String[] temp = str.split("]");
				if (str.startsWith("[clue]")) {
					clues.add(temp[1]);
				} else if (str.startsWith("[pattern]")) {
					patterns.add(temp[1]);
				}
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		// ArrayListからString[]に変換してから、返す
		String[] clueList = (String[])clues.toArray(new String[clues.size()]);
		String[] patternList = (String[])patterns.toArray(new String[patterns.size()]);
		ArrayList<String[]> result = new ArrayList<String[]>(2);
		result.add(clueList);
		result.add(patternList);
		return result;
	}


	/**
	 * SVMの結果を読み込む
	 * @param filePath SVM結果ファイルのファイルパス
	 * @return SVM結果のハッシュ
	 */
	static public HashMap<String, Integer> readSvmResults(String filePath) {
		HashMap<String, Integer> svmHash = new HashMap<String, Integer>();
		try {
			File file = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while (str != null) {
				if (str.equals("")) {
					continue;
				}
				String[] temp = str.split("\t");
				svmHash.put(temp[1], Integer.parseInt(temp[0]));
				str = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}

		return svmHash;
	}
}
