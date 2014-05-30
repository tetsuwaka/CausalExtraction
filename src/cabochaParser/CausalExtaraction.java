package cabochaParser;

import java.util.ArrayList;
import java.util.regex.*;

import cabochaParser.CabochaParser.*;

public class CausalExtaraction {
	
	// 指示詞のリスト
	private String[] demonList;
	
	// 文字列の末尾の「こと」などを見つける正規表現
	private Pattern pKoto = Pattern.compile("こと$|など$|等$|の$");
	
	// 文字列に不要な文字が含まれているかを調べる正規表現
	private Pattern pGomi = Pattern.compile("。|、|の");
	
	
	public CausalExtaraction() {
		super();
		this.demonList = FileUtilities.readDemonList();
	}

	/**
	 * 入力された文字列の末尾の「こと」などを削除し、返す関数
	 * @param str 文字列
	 * @return 「こと」などを削除した文字列
	 */
	String removeKoto(String str) {
		Matcher m = this.pKoto.matcher(str);
		if (m.find()) {
			str = this.removeKoto(m.replaceAll(""));
		}
		return str;
	}

	/**
	 * 核文節のIDのリストを返す
	 * @param caboList カボリスト
	 * @param clue 手がかり表現
	 * @return 核文節IDのリスト
	 */
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

	/**
	 * 文節の末尾の助詞などを削除する
	 * @param pos POSクラスのインスタンス
	 * @return 文字列
	 */
	String removeParticle(POS pos) {
		String word = "";
		boolean flag = false;
		for (Morph morph : pos.morph) {
			if (flag) {
				word = morph.face + word;
			} else {
				if (
					(morph.pos.indexOf("助詞") == -1) &&
					(!this.pGomi.matcher(morph.face).find()) 
				) {
					flag = true;
					word = morph.face;
				}
			}
		}
		return word;
	}
	
	/**
	 * 結果を取得する（動詞にかかっている場合）
	 * @param caboList カボリスト
	 * @param clue 手がかり表現
	 * @param coreId 核文節Id
	 * @return 結果表現
	 */
	String getResultVP(ArrayList<POS> caboList, String clue, int coreId) {
		boolean flag = false;
		int chunkId = -1;
		String word = "";
		String resultExpression = "";
		
		for (POS pos : caboList) {
			String tempWord = "";
			for (String s : pos.str) {
				word = word + s;
				tempWord = tempWord + s;
			}

			if (flag && pos.id < chunkId) {
				resultExpression = resultExpression + tempWord;
			} else if (flag && pos.id < chunkId) {
				resultExpression = resultExpression + removeParticle(pos);
			}

			if (pos.id == coreId) {
				String[] temp = word.split(clue);
				resultExpression = temp[temp.length - 1];
				chunkId = pos.chunk;
				flag = true;
			}
		}
		
		return resultExpression;
	}
	
}
