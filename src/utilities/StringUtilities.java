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
		if (list == null || list.isEmpty()) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : list) {
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * 文字列str2に文字列str1が含まれるか否かを判定
	 * @param str1
	 * @param str2
	 * @return 文字列str2に文字列str1が含まれていればtrue, いなければfalse
	 */
	public static boolean in(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		return str2.contains(str1);
	}
	
	/**
	 * 文字列strに含まれる文字列removeStrを削除する
	 * @param str
	 * @param removeStr
	 * @return removeStrを削除した文字列str
	 */
	public static String remove(String str, String removeStr) {
		if (str == null || removeStr == null) {
			return str;
		}
		try {
			Pattern pattern = Pattern.compile(removeStr);
			Matcher m = pattern.matcher(str);
			return m.find() ? m.replaceAll("") : str;
		} catch (Exception e) {
			// If regex compilation fails, fall back to simple string replacement
			return str.replace(removeStr, "");
		}
	}
}
