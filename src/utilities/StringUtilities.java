package utilities;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilities {

	/**
	 * join関数
	 * @param delimiter 区切り文字
	 * @param list Stringの配列
	 * @return 配列の要素を区切り文字でつなげた文字列
	 */
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

	/**
	 * 文字列str2に文字列str1が含まれるか否かを判定
	 * @param str1
	 * @param str2
	 * @return 文字列str2に文字列str1が含まれていればtrue, いなければfalse
	 */
	public static boolean in(String str1, String str2) {
		if (str2.indexOf(str1) != -1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 文字列strに含まれる文字列removeStrを削除する
	 * @param str
	 * @param removeStr
	 * @return removeStrを削除した文字列str
	 */
	public static String remove(String str, String removeStr) {
		Pattern pattern = Pattern.compile(removeStr);
		Matcher m = pattern.matcher(str);
		return m.find() ? m.replaceAll("") : str;
	}
}
