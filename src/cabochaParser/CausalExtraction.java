package cabochaParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;

import cabochaParser.CabochaParser.POS;
import cabochaParser.CabochaParser.*;

public class CausalExtraction {
	
	// 指示詞のリスト
	private String[] demonList;
	
	// 文字列の末尾の「こと」などを見つける正規表現
	private Pattern pKoto = Pattern.compile("こと$|など$|等$|の$");
	
	// 文字列に不要な文字が含まれているかを調べる正規表現
	private Pattern pGomi = Pattern.compile("。|、|の");
	
	
	public CausalExtraction() {
		super();
		this.demonList = FileUtilities.readDemonList();
	}

	/**
	 * 入力された文字列の末尾の「こと」などを削除し、返す関数
	 * @param str 文字列
	 * @return 「こと」などを削除した文字列
	 */
	public String removeKoto(String str) {
		Matcher m = this.pKoto.matcher(str);
		if (m.find()) {
			str = this.removeKoto(m.replaceAll(""));
		}
		return str;
	}

	/**
	 * 指示詞が先頭に含まれているか否かを判定
	 * @param sentence 文
	 * @return 含まれているばTrue, いなければFalse
	 */
	public boolean includeDemon(String sentence) {
		for (String demon : this.demonList) {
			if (sentence.startsWith(demon)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 核文節のIDのリストを返す
	 * @param caboList カボリスト
	 * @param clue 手がかり表現
	 * @return 核文節IDのリスト
	 */
	public Integer[] getCoreIds(ArrayList<POS> caboList, String clue) {
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
		return (Integer[])ids.toArray(new Integer[ids.size()]);
	}

	/**
	 * 文節の末尾の助詞などを削除する
	 * @param pos POSクラスのインスタンス
	 * @return 文字列
	 */
	public String removeParticle(POS pos) {
		String word = "";
		boolean flag = false;
		for (Morph morph : Reversed.reversed(pos.morph)) {
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
	public String getResultVP(ArrayList<POS> caboList, String clue, int coreId) {
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
	
	/**
	 * @param caboList カボリスト
	 * @param coreId 核文節のID
	 * @return 結果表現
	 */
	public String getResultNP(ArrayList<POS> caboList, int coreId) {
		String result = "";
		int clueChunkId = caboList.get(coreId).chunk;
		HashMap<Integer, ArrayList<Integer>> passiveHash = new HashMap<Integer, ArrayList<Integer>>();
		
		// 係り元IDの連想配列を得る
		for (POS pos : caboList) {
			if (passiveHash.containsKey(pos.chunk)) {
				passiveHash.get(pos.chunk).add(pos.id);
			} else {
				passiveHash.put(pos.chunk, new ArrayList<Integer>(Arrays.asList(pos.id)));
			}
		}
		
		// 結果表現を得る
		for (POS pos : caboList) {
			if (pos.id < clueChunkId) {
				continue;
			}
			if ((pos.morph.get(pos.morph.size() - 1).posd.indexOf("格助詞") != -1) ||
				(pos.morph.get(pos.morph.size() - 2).posd.indexOf("格助詞") != -1) // 読点がある場合
			) {
				result = result + this.removeParticle(pos);
				break;
			}
			
			if (passiveHash.containsKey(pos.id)) {
				int passive = 999;
				for (int p : passiveHash.get(pos.chunk)) {
					passive = p < passive ? p : passive;
				}
				if (passive < coreId) {
					result = result + this.removeParticle(pos);
					break;
				}
			}
			
			String tempWord = "";
			for (String str : pos.str) {
				tempWord = tempWord + str;
			}
			
			if (tempWord.indexOf("など、") != -1) {
				result = result + this.removeParticle(pos);
				break;
			}
			
			result = result + tempWord;
		}
		return this.removeKoto(result);
	}
	
	public String getSubj(ArrayList<POS> caboList, int clueId, int clueChunkId) {
		return "";
	}
	
	public String getKotoResult(ArrayList<POS> caboList, int resultId) {
		return "";
	}
	
	public String getBasis(ArrayList<POS> caboList, String clue, int coreId) {
		String basis = "";
		String word = "";
		boolean flag = false;
		
		for (POS pos : Reversed.reversed(caboList)) {
			// 末尾から文字を再構成していく
			String tempWord = "";
			for (Morph morph : pos.morph) {
				tempWord = tempWord + morph.face;
			}
			word = tempWord + word;
			
//			// Pattern C用のフラグと係り元
//			if (endFlag && pos.chunk == coreId) {
//				npFlag = false;
//				for (Morph morph : Reversed.reversed(pos.morph)) {
//					if (morph.posd.indexOf("接続助詞") != -1) {
//						kotoFlag = false;
//						break;
//					} else if (morph.posd.indexOf("接続助詞") != -1) {
//						kotoFlag = false;
//						cNum = pos.id;
//						break;
//					} else if (morph.posd.indexOf("読点") != -1) {
//						// なにもしない
//					} else {
//						break;
//					}
//				}
//			}
			
			// 操作終了条件：核文節より文末に近い文節に係っている場合
			if (pos.chunk > coreId) {
				flag = false;
			}
			
			// 原因表現を構成する文字列を足していく
			if (flag) {
				if (basis.equals("")) {
					basis = this.removeParticle(pos);
				} else {
					basis  = tempWord + basis;
				}
			}
			
			// 原因表現の末尾判定と獲得
			if ((word.indexOf(clue) != -1) && 
				((pos.id == coreId) || (pos.id + 1 == coreId)) &&
				(!flag)
			) {
				flag = true;
				basis = this.removeKoto(word.split(clue)[0]);
//				if (clue.indexOf("。") != -1) {
//					endFlag = true;
//				}
			} else if (word.indexOf(clue) != -1) {
				word = word.split(clue)[0];
			}
		}
		
//		// Pattern Cの例外処理
//		if ((cNum != -1) && (caboList.get(cNum).morph.get(0).posd.indexOf("代名詞") != -1)) {
//			cNum = -1;
//		}
		
		return basis;
	}
}
