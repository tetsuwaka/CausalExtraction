package cabochaParser;

import java.util.ArrayList;
import java.util.regex.*;

import cabochaParser.CabochaParser.POS;

public class CausalExtaraction {
	
	// 指示詞のリスト
	String[] demonList;
	
	// 文字列の末尾の「こと」などを見つける正規表現
	Pattern pKoto = Pattern.compile("こと$|など$|等$|の$");
	
	
	public CausalExtaraction() {
		super();
		
	}

	// 入力された文字列の末尾の「こと」などを削除し、返す関数
	// 入力：文字列
	// 返り値：「こと」などを削除した文字列
	String removeKoto(String str) {
		Matcher m = this.pKoto.matcher(str);
		if (m.find()) {
			str = this.removeKoto(m.replaceAll(""));
		}
		return str;
	}

	// 核文節のIDのリストを返す
	// 入力：カボリスト、手がかり表現
	// 返り値：IDのリスト
	ArrayList<Integer> getCoreIds(ArrayList<POS> caboList, String clue) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		String word = "";
		for (POS pos : caboList) {
			for (String s : pos.str) {
				word = word + s;
			}
			if (word.indexOf(clue) != -1) {
				ids.add(pos.id);
				word = "";
			}
		}
		return ids;
	}
	
}
