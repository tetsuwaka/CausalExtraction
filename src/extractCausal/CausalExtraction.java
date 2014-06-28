package extractCausal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.*;
import utilities.FileUtilities;
import utilities.Reversed;
import utilities.StringUtilities;
import cabochaParser.*;
import cabochaParser.CabochaParser.Morph;
import cabochaParser.CabochaParser.POS;

public class CausalExtraction {

	// CabochaParser
	private CabochaParser parser = new CabochaParser();
	
	// 手がかり表現のリスト
	static private String[] clueList;

	// Pattern Eの手がかり表現のリスト
	static private String[] eclueList;

	// 指示詞のリスト
	static private String[] demonList;
	
	// 手がかり表現かぶり判定HashMap
	public HashMap<String, ArrayList<String>> clueHash;

	// 文字列の末尾の「こと」などを見つける正規表現
	private Pattern pKoto = Pattern.compile("こと$|など$|等$|の$");

	// 文字列に不要な文字が含まれているかを調べる正規表現
	private Pattern pGomi = Pattern.compile("、|の");


	public CausalExtraction() {
		super();
		this.clueHash = this.makeIncludingCluse();
	}
	
	public CausalExtraction(ArrayList<String[]> clueList, String[] demoList) {
		super();
		CausalExtraction.setClueList(clueList);
		CausalExtraction.setDemonList(demoList);
		this.clueHash = this.makeIncludingCluse();
	}
	
	/**
	 * 手がかり表現のかぶり判定HashMapを作成
	 * @return 手がかり表現のかぶり判定HashMap
	 */
	public HashMap<String, ArrayList<String>> makeIncludingCluse() {
		HashMap<String, ArrayList<String>> clueHash = new HashMap<String, ArrayList<String>>(CausalExtraction.clueList.length);
		for (String clue1 : CausalExtraction.clueList) {
			clueHash.put(clue1, new ArrayList<String>());
			for (String clue2 : CausalExtraction.clueList) {
				if (!clue1.equals(clue2) && StringUtilities.in(clue1, clue2)) {
					clueHash.get(clue1).add(clue2);
				}
			}
		}
		return clueHash;
	}
	
	/**
	 * 入力された文字列の末尾の「こと」などを削除し、返す関数
	 * @param str 文字列
	 * @return 「こと」などを削除した文字列
	 */
	public String removeKoto(String str) {
		Matcher m = this.pKoto.matcher(str);
		return m.find() ? this.removeKoto(m.replaceAll("")) : str;
	}

	/**
	 * 指示詞が先頭に含まれているか否かを判定
	 * @param sentence 文
	 * @return 含まれているばTrue, いなければFalse
	 */
	public boolean includeDemon(String sentence) {
		for (String demon : CausalExtraction.demonList) {
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
			word = word + StringUtilities.join("", pos.str);
			if (StringUtilities.in(clue, word)) {
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
					(!morph.pos.equals("助詞")) &&
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
			String tempWord = StringUtilities.join("", pos.str);
			word = word + tempWord;

			if (flag && pos.id < chunkId) {
				resultExpression = resultExpression + tempWord;
			} else if (flag && pos.id == chunkId) {
				resultExpression = resultExpression + removeParticle(pos);
				break;
			}

			if (pos.id == coreId) {
				String[] temp = word.split(clue);
				resultExpression = temp.length > 1 ? temp[temp.length - 1]  : "";
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
			if (pos.morph.get(pos.morph.size() - 1).posd.equals("格助詞") ||
				pos.morph.get(pos.morph.size() - 2).posd.equals("格助詞") // 読点がある場合
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

			String tempWord = StringUtilities.join("", pos.str);

			if (StringUtilities.in("など、", tempWord)) {
				result = result + this.removeParticle(pos);
				break;
			}

			result = result + tempWord;
		}
		return this.removeKoto(result);
	}

	/**
	 * 結果表現の主部を獲得
	 * @param caboList カボリスト
	 * @param coreId 核文節のID
	 * @return 結果表現の主部
	 */
	public String getSubj(ArrayList<POS> caboList, int coreId) {
		int clueChunkId = caboList.get(coreId).chunk;
		boolean flag = false;
		String subj = "";
		String tempWord = "";
		int subjId = -1;

		for (POS pos : Reversed.reversed(caboList)) {
			tempWord = StringUtilities.join("", pos.str);

			if ((pos.id < coreId - 1) && (pos.chunk == clueChunkId)){
				if (pos.morph.get(0).pos.equals("接続詞")) {
					break;
				}
				for (Morph morph : Reversed.reversed(pos.morph)) {
					if (morph.posd.equals("格助詞") || morph.posd.equals("係助詞")) {
						flag = true;
						subj = tempWord;
						subjId = pos.id;
					} else {
						if (!morph.pos.equals("記号")) {
							break;
						}
					}
				}
			}

			if ((flag) && (pos.chunk == subjId) && (pos.id == subjId - 1)) {
				subj = tempWord + subj;
				subjId -= 1;
			}
		}
		return subj;
	}

	/**
	 * 「こと」の結果表現を取得する
	 * @param caboList カボリスト
	 * @param cNum 結果の末尾文節のID
	 * @return 結果表現
	 */
	public String getKotoResult(ArrayList<POS> caboList, int cNum) {
		String result = "";
		String temp = "";
		boolean flag = false;
		
		for (POS pos : Reversed.reversed(caboList)) {
			temp = StringUtilities.join("", pos.str);
			
			if (temp.endsWith("。") && flag) {
				break;
			}
			if (flag) {
				if (pos.chunk <= cNum) {
					result = temp + result;
				} else {
					break;
				}
			}
			if (pos.id == cNum) {
				result = temp;
				flag = true;
			}
		}
		return result;
	}
	
	/**
	 * 原因表現を抽出する
	 * @param caboList カボリスト
	 * @param clue 手がかり表現
	 * @param coreId 核文節のID
	 * @return 原因表現
	 */
	public String getBasis(ArrayList<POS> caboList, String clue, int coreId) {
		String basis = "";
		String word = "";
		boolean flag = false;

		for (POS pos : Reversed.reversed(caboList)) {
			// 末尾から文字を再構成していく
			String tempWord = StringUtilities.join("", pos.str);
			word = tempWord + word;
			
			// 操作終了条件：核文節に係っていて、条件を満たした場合
			if (pos.chunk == coreId && clue.endsWith("。") && pos.id != coreId - 1) {
				if (pos.morph.get(pos.morph.size() - 1).posd.equals("接続助詞") || 
					pos.morph.get(pos.morph.size() - 2).posd.equals("接続助詞") ||
					pos.morph.get(pos.morph.size() - 1).posd.equals("係助詞") || 
					pos.morph.get(pos.morph.size() - 2).posd.equals("係助詞")
				) {
					break;
				}
			}

			// 操作終了条件：核文節より文末に近い文節に係っている場合
			if (pos.chunk > coreId) {
				flag = false; // ここでbreakすると、直近の原因表現を獲得できなくなる
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
			if ((StringUtilities.in(clue, word)) && 
				((pos.id == coreId) || (pos.id + 1 == coreId)) &&
				(!flag)
			) {
				flag = true;
				String [] temp = word.split(clue);
				basis = temp.length == 0 ? "" : this.removeKoto(temp[0]);
			} else if (StringUtilities.in(clue, word)) {
				word = word.split(clue)[0];
			}
		}

		return basis;
	}

	/**
	 * Pattern Cのフラグ(係り元)を得る
	 * @param caboList カボリスト
	 * @param coreId 核文節のID
	 * @return Pattern Cであるなら、係り元IDを返し、そうでなければ、-1を返す
	 */
	public int getPatternCFlag(ArrayList<POS> caboList, int coreId) {
		int cNum = -1;
		for (POS pos : caboList) {
			if (pos.chunk != coreId) {
				continue;
			}
			for (Morph morph : Reversed.reversed(pos.morph)) {
				if (morph.posd.equals("係助詞")) {
					cNum = pos.id;
					break;
				} else if (morph.posd.equals("読点")) {
					continue;
				} else {
					break;
				}
			}
		}

		// 代名詞が文節の頭に存在するなら、フラグ取り消し
		if (cNum != -1) {
			cNum = caboList.get(cNum).morph.get(0).posd.equals("代名詞") ? -1 : cNum;
		}
		return cNum;
	}

	/**
	 * 手がかり表現かぶり判定処理
	 * @param sentence 手がかり表現を含む文
	 * @param clueHash 手がかり表現かぶりHashMap
	 * @return 手がかり表現のかぶり判定を格納したHashMap
	 */
	public HashMap<String, Integer> getIncludingClues(String sentence, HashMap<String, ArrayList<String>>clueHash) {
		HashMap<String, Integer> hash = new HashMap<String, Integer>();
		for (String clue : clueHash.keySet()) {
			hash.put(clue, 0);
			for (String _clue : clueHash.get(clue)) {
				if (StringUtilities.in(_clue, sentence)) {
					hash.put(clue, 1);
				}
			}
		}
		return hash;
	}

	/**
	 * 適切なPatternを選択し、原因表現・結果表現を抽出
	 * @param caboList カボリスト
	 * @param clue 手がかり表現
	 * @param coreId 核文節のID
	 * @param sentence 手がかり表現を含む文
	 * @param beforeSentence 一つ前の文
	 * @return Causalインスタンス(原因・結果表現、結果表現の主部、Pattern)
	 */
	public Causal getCausalExpression(ArrayList<POS> caboList, String clue, int coreId, String sentence, String beforeSentence) {
		Causal causal = new Causal();
		int chunkId = caboList.get(coreId).chunk;

		if (Arrays.asList(CausalExtraction.eclueList).contains(clue)) {
			causal.basis = beforeSentence;
		} else {
			causal.basis = this.getBasis(caboList, clue, coreId);
		}

		// 原因表現がとれなかったり、原因表現に指示詞が含まれている場合
		if (causal.basis.equals("") || this.includeDemon(causal.basis)) {
			return new Causal();
		}
		
		// Pattern Eの場合
		if (Arrays.asList(CausalExtraction.eclueList).contains(clue)) {
			causal.result = sentence.replaceAll(clue, "");
			causal.pattern = "E";

		// Pattern AとBの場合
		} else if (!clue.endsWith("。") && !sentence.endsWith(clue + "。")) {
			Morph lastMorph = caboList.get(chunkId).morph.get(caboList.get(chunkId).morph.size() - 1);
			Morph firstMorph = caboList.get(chunkId).morph.get(0);
			if (lastMorph.pos.equals("動詞") || lastMorph.face.endsWith("。") || lastMorph.face.endsWith("、")) {
				causal.result = this.getResultVP(caboList, clue, coreId);
				causal.subj = this.getSubj(caboList, coreId);
			} else if (firstMorph.posd.equals("非自立")) {
				causal.result = this.getResultVP(caboList, clue, coreId);
				causal.result = StringUtilities.remove(causal.result, StringUtilities.join("", caboList.get(chunkId).str) + "$");
			} else if (firstMorph.pos.equals("名詞")) {
				causal.result = this.getResultNP(caboList, coreId);
			} else if (firstMorph.pos.equals("形容詞")) {
				causal.result = this.getResultVP(caboList, clue, coreId);
			}

			// Patternの判定
			causal.pattern = causal.subj.equals("") ? "A" : "B";

		// Pattern CとDの場合
		} else {
			int cNum = this.getPatternCFlag(caboList, coreId);
			if (cNum == -1) {
				causal.result = beforeSentence;
				causal.pattern = "D";
			} else {
				causal.result = this.getKotoResult(caboList, cNum);
				causal.pattern = "C";
			}
		}
		return causal;
	}

	/**
	 * 指定したファイル内に含まれる因果関係を抽出する関数
	 * @param fileName ファイルのパス
	 * @return 因果関係を保存したCausalのリスト
	 */
	public ArrayList<Causal> getInga(String fileName) {
		ArrayList<Causal> causalList = new ArrayList<Causal>();
		
		// 一つ前の文
		String beforeSentence = "";
		
		// 文のリストを得る
		String[] lines = FileUtilities.readLines(fileName);
		
		// 因果関係の抽出
		int count = 1;
		for (String sentence : lines) {
			HashMap<String, Integer> clueHash = this.getIncludingClues(sentence, this.clueHash);
			ArrayList<POS> caboList = null;
			
			// 手がかり表現ごとに原因・結果表現を抽出する
			for (String clue : CausalExtraction.clueList) {
				// 手がかり表現の状態によっては処理をスキップ
				if (!StringUtilities.in(clue, sentence) ||
					(clue.equals("による。") && StringUtilities.in("ところによる。", sentence)) ||
					StringUtilities.in(clue + "いる", sentence) ||
					StringUtilities.in(clue + "いた", sentence) ||
					clueHash.get(clue) == 1
				) {
					continue;
				}
				
				// 係り受け解析を行う
				if (caboList == null) {
					try {
						caboList = this.parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
						continue;
					}
				}
				
				// 核文節を得る
				Integer[] coreIds = this.getCoreIds(caboList, clue);
				
				// 同一手がかり表現が同じ文に複数ある場合の対処
				for (int coreId : coreIds) {
					Causal causal = this.getCausalExpression(caboList, clue, coreId, sentence, beforeSentence);
					// 因果関係が抽出できなかった場合
					if (causal.basis.equals("") || causal.result.equals("")) {
						continue;
					}
					causal.clue = clue;
					causal.sentence = sentence;
					causal.filePath = fileName;
					causal.line = count;
					causalList.add(causal);
				}
			}
			count++;
			beforeSentence = sentence;
		}
		
		return causalList;
	}

	public static void setClueList(ArrayList<String[]> clueList) {
		CausalExtraction.clueList = clueList.get(0);
		CausalExtraction.eclueList = clueList.get(1);
	}

	public static void setDemonList(String[] demonList) {
		CausalExtraction.demonList = demonList;
	}
	
}
