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
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
			String str;
			while ((str = br.readLine()) != null) {
				if (!str.trim().isEmpty()) {
					strings.add(str);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + filePath + " - " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
		}

		// ArrayListからString[]に変換してから、返す
		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * 手がかり表現のリストをファイルから読み取って返す関数
	 * @return 手がかり表現のリスト
	 */
	static public ArrayList<String[]> readClueList(String filePath) {
		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<String> endClues = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
			String str;
			while ((str = br.readLine()) != null) {
				if (!str.trim().isEmpty()) {
					String[] temp = str.split("]");
					if (temp.length >= 2) {
						strings.add(temp[1]);
						if (str.startsWith("[E]")) {
							endClues.add(temp[1]);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Clue list file not found: " + filePath + " - " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading clue list file: " + filePath + " - " + e.getMessage());
		}

		// ArrayListからString[]に変換してから、返す
		String[] clues = strings.toArray(new String[strings.size()]);
		String[] eclues = endClues.toArray(new String[endClues.size()]);
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
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
			String str;
			while ((str = br.readLine()) != null) {
				if (!str.trim().isEmpty()) {
					String[] temp = str.split("]");
					if (temp.length >= 2) {
						if (str.startsWith("[clue]")) {
							clues.add(temp[1]);
						} else if (str.startsWith("[pattern]")) {
							patterns.add(temp[1]);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Additional data file not found: " + filePath + " - " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading additional data file: " + filePath + " - " + e.getMessage());
		}

		// ArrayListからString[]に変換してから、返す
		String[] clueList = clues.toArray(new String[clues.size()]);
		String[] patternList = patterns.toArray(new String[patterns.size()]);
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
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
			String str;
			while ((str = br.readLine()) != null) {
				if (!str.trim().isEmpty()) {
					String[] temp = str.split("\t");
					if (temp.length >= 2) {
						try {
							svmHash.put(temp[1], Integer.parseInt(temp[0]));
						} catch (NumberFormatException e) {
							System.err.println("Invalid SVM result format in line: " + str);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("SVM results file not found: " + filePath + " - " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error reading SVM results file: " + filePath + " - " + e.getMessage());
		}

		return svmHash;
	}
}
