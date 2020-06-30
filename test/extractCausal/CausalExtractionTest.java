package extractCausal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import cabochaParser.CabochaParser;
import cabochaParser.CabochaParser.POS;
import cabochaParser.ExecCabocha;
import utilities.FileUtilities;
import utilities.StringUtilities;

public class CausalExtractionTest {
	String[] demonList = FileUtilities.readLines("sample/demonstrative_list.txt");
	ArrayList<String[]> clueList = FileUtilities.readClueList("sample/clue_list.txt");
	ArrayList<String[]> additionalData = FileUtilities.readAdditionalData("sample/additional_data.txt");
	HashMap<String, Integer> svmHash = FileUtilities.readSvmResults("test/sampleData/svm_result4test.txt");

	CausalExtraction ce = new CausalExtraction(clueList, demonList);
	CabochaParser parser = new CabochaParser();


	@Test
	public void testRemovePattern() {
		CausalExtraction.addClueList(this.additionalData.get(0));
		CausalExtraction.setPrefixPatternList(this.additionalData.get(1));
		assertThat("ほげほげのため", is(this.ce.removePattern("これは、ほげほげのため")));
		assertThat("ほげほげのため、これは、", is(this.ce.removePattern("ほげほげのため、これは、")));
		assertThat("ほげほげのため", is(this.ce.removePattern("増加要因はほげほげのため")));
	}

	@Test
	public void testHavePattern() {
		CausalExtraction.setPrefixPatternList(this.additionalData.get(1));
		assertThat(true, is(this.ce.havePattern("これは、ほげほげのため")));
		assertThat(false, is(this.ce.havePattern("ほげほげのため、これは、")));
		assertThat(true, is(this.ce.havePattern("増加要因はほげほげのため")));
	}

	@Test
	public void testRemoveKoto() {
		String str;

		str = this.ce.removeKoto("ほげほげの");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげなどの");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげ");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげことのなど等");
		assertThat("ほげほげ", is(str));
	}

	@Test
	public void testIncludeDemon() {
		assertThat(false, is(this.ce.includeDemon("あたなのために")));
		assertThat(false, is(this.ce.includeDemon("私それで")));
		assertThat(true, is(this.ce.includeDemon("そのため")));
		assertThat(true, is(this.ce.includeDemon("それで")));
	}

	@Test
	public void testGetCoreIds() throws IOException, InterruptedException {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になった。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat(new Integer[]{1}, is(this.ce.getCoreIds(caboList, "ため、")));

		assertThat(new Integer[]{}, is(this.ce.getCoreIds(caboList, "によって、")));

		str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になったため、損した。"));
		caboList = parser.parse(str);
		assertThat(new Integer[]{1, 4}, is(this.ce.getCoreIds(caboList, "ため、")));

		str = StringUtilities.join("\n", ExecCabocha.exec("公共工事と住宅建設が高水準を維持、個人消費も堅調なうえ、設備投資が前年度を上回る見通しとなっているためだ。"));
		caboList = parser.parse(str);
		assertThat(new Integer[]{12}, is(this.ce.getCoreIds(caboList, "ためだ。")));
	}

	@Test
	public void testRemoveParticle() throws Exception {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になったため、損した。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat("円高", is(this.ce.removeParticle(caboList.get(0))));
		assertThat("ため", is(this.ce.removeParticle(caboList.get(1))));
		assertThat("不況", is(this.ce.removeParticle(caboList.get(2))));
		assertThat("なった", is(this.ce.removeParticle(caboList.get(3))));
		assertThat("ため", is(this.ce.removeParticle(caboList.get(4))));
		assertThat("損した。", is(this.ce.removeParticle(caboList.get(5))));
	}

	@Test
	public void testGetResultVP() throws Exception {
		String clue = "で、";
		String sentence = "円高による不況の影響で、買い物客が激減した。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("買い物客が激減した。", is(this.ce.getResultVP(caboList, clue, 2)));

		clue = "ため、";
		sentence = "十分なデータの蓄積がなく、合理的な見積もりが困難であるため、権利行使期間の中間点において行使されるものと想定して見積もっております。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("権利行使期間の中間点において行使される", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "により";
		sentence = "食品業界で、景気後退に伴う消費マインドの冷え込みや、生活防衛による購買単価の落ち込みなどにより企業業績の後退を余儀なくされ、企業間競争はますます熾烈さを増してまいりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("企業業績の後退を余儀なくされ", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "により";
		sentence = "さて当連結会計年度におけるわが国経済は、アジア・新興国を中心とした外需による生産活動の増加により企業収益も改善へと転じ、緩やかな回復基調にありましたが、円高の進行やデフレの長期化により本格的な回復には至らず、加えて東日本大震災の影響もあり、先行き不透明な状況となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("本格的な回復には至らず", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[1])));
	}

	@Test
	public void testGetResultNP() throws Exception {
		String sentence = "円高による不況の影響で、買い物客が激減。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況の影響", is(this.ce.getResultNP(caboList, 0)));

		sentence = "円高による不況で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況", is(this.ce.getResultNP(caboList, 0)));
	}

	@Test
	public void testGetSubj() throws Exception {
		String clue = "により";
		String sentence = "食品業界で、景気後退に伴う消費マインドの冷え込みや、生活防衛による購買単価の落ち込みなどにより企業業績の後退を余儀なくされ、企業間競争はますます熾烈さを増してまいりました。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("食品業界で、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("製菓原材料類は、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "で、";
		sentence = "国内の生茸の販売は、消費全体が収縮する中で茸の需要も低迷し、価格は平年を下回る厳しい相場で推移したことで、販売量、販売価格ともに前年を割り込む結果となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("国内の生茸の販売は、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));
	}

	@Test
	public void testGetKotoResult() throws Exception {
		String sentence = "ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、景気回復を確実なものにするには、雇用悪化に歯止めをかける必要があると判断したためだ。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、", is(this.ce.getKotoResult(caboList, 6)));

		sentence = "日銀が景気の先行きに慎重なのは、設備投資調整や公共事業の拡大などプラス要因がある半面、雇用調整や円高などマイナス要因も目立ち、「両者がせめぎ合っているのが現状」と見ているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("日銀が景気の先行きに慎重なのは、", is(this.ce.getKotoResult(caboList, 3)));

		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("配当原資が不足するのは、", is(this.ce.getKotoResult(caboList, 1)));
	}

	@Test
	public void testGetBasis() throws Exception {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になった。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat("円高", is(this.ce.getBasis(caboList, "ため、", 1)));
		assertThat("", is(this.ce.getBasis(caboList, "ため、", 3)));

		str = StringUtilities.join("\n", ExecCabocha.exec("十分なデータの蓄積がなく、合理的な見積もりが困難であるため、権利行使期間の中間点において行使されるものと想定して見積もっております。"));
		caboList = parser.parse(str);
		Integer[] coreIds = this.ce.getCoreIds(caboList, "ため、");
		assertThat("十分なデータの蓄積がなく、合理的な見積もりが困難である", is(this.ce.getBasis(caboList, "ため、", coreIds[0])));
		assertThat("", is(this.ce.getBasis(caboList, "ため、", 1)));
	}

	@Test
	public void testGetPatternCFlag() throws Exception {
		String sentence = "ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、景気回復を確実なものにするには、雇用悪化に歯止めをかける必要があると判断したためだ。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(6, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ためだ。")[0])));

		sentence = "日銀が景気の先行きに慎重なのは、設備投資調整や公共事業の拡大などプラス要因がある半面、雇用調整や円高などマイナス要因も目立ち、「両者がせめぎ合っているのが現状」と見ているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(3, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ためだ。")[0])));

		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(1, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ため。")[0])));
	}

	@Test
	public void testGetIncludingClues() throws Exception {
		String sentence = "円高を背景に、景気が悪化した。";
		HashMap<String, Integer> result = this.ce.getIncludingClues(sentence, this.ce.clueHash);
		assertThat(result.get("を背景に、"), is(0));
		assertThat(result.get("を背景に"), is(1)); // かぶりがあったら1になる

		sentence = "円高を背景に景気が悪化した。";
		result = this.ce.getIncludingClues(sentence, this.ce.clueHash);
		assertThat(result.get("を背景に、"), is(0));
		assertThat(result.get("を背景に"), is(0));
	}

	@Test
	public void testGetCausalExpression() throws Exception {
		String clue = "ため、";
		String sentence = "円高のため、不況になった。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		Causal causal = this.ce.getCausalExpression(caboList, clue, 1, sentence, "");
		Causal seikai = new Causal("円高", "不況になった。", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "による";
		sentence = "円高による不況の影響で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, 0, sentence, "");
		seikai = new Causal("円高", "不況の影響", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "で、";
		sentence = "円高による不況の影響で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, 2, sentence, "");
		seikai = new Causal("円高による不況の影響", "買い物客が激減。", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "による";
		sentence = "この結果による不況の影響で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, 0, sentence, "");
		seikai = new Causal("", "", "", "");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("製菓・製パン向けの販売が総じて低調に推移した", "各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", "製菓原材料類は、", "B");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "ため。";
		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("前期末の有価証券評価差額金が十七億円強の含み損となった", "配当原資が不足するのは、", "", "C");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "ためだ。";
		sentence = "公共工事と住宅建設が高水準を維持、個人消費も堅調なうえ、設備投資が前年度を上回る見通しとなっているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("公共工事と住宅建設が高水準を維持、個人消費も堅調なうえ、設備投資が前年度を上回る見通しとなっている", "TTT", "", "D");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "そのため、";
		sentence = "そのため、平成２３年３月期第１四半期の経営成績（累計）及び対前年同四半期増減率については記載しておりません。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("TTT", "平成２３年３月期第１四半期の経営成績（累計）及び対前年同四半期増減率については記載しておりません。", "", "E");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "で、";
		sentence = "下半期では、上半期に導入を予定していながら諸事情により計画が遅れた案件の成約が見込めますので、売上高に関しましては上半期の不足を補い、期初の通期予想を達成するものと思われますが、利益に関しましては、利益率が比較的低い低価格ツールの占める割合が増えていることが影響し若干減少する見通しです。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("下半期では、上半期に導入を予定していながら諸事情により計画が遅れた案件の成約が見込めます", "売上高に関しましては上半期の不足を補い、期初の通期予想を達成する", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		CausalExtraction.patternFlag = true;
		clue = "によります。";
		sentence = "これは主に短期借入金が６３億５１百万円増加し、未払法人税等が１億６８百万円、流動負債の「その他」に含まれる設備支払手形が１０億２０百万円減少したこと等によります。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("主に短期借入金が６３億５１百万円増加し、未払法人税等が１億６８百万円、流動負債の「その他」に含まれる設備支払手形が１０億２０百万円減少した", "TTT", "", "D");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		CausalExtraction.patternFlag = true;
		clue = "によるものであります。";
		sentence = "その主な要因は、当期純利益の影響により利益剰余金が４１０，５２０千円増加したものの、剰余金の配当により利益剰余金が２１２，１５５千円減少したことによるものであります。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("剰余金の配当により利益剰余金が２１２，１５５千円減少した", "TTT", "", "D");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		CausalExtraction.patternFlag = false;
		clue = "により";
		sentence = "さて当連結会計年度におけるわが国経済は、アジア・新興国を中心とした外需による生産活動の増加により企業収益も改善へと転じ、緩やかな回復基調にありましたが、円高の進行やデフレの長期化により本格的な回復には至らず、加えて東日本大震災の影響もあり、先行き不透明な状況となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[1], sentence, "");
		seikai = new Causal("円高の進行やデフレの長期化", "本格的な回復には至らず", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		CausalExtraction.patternFlag = false;
		clue = "により、";
		sentence = "当家電販売業界におきましては、消費低迷に加え、冷夏などの天候不順の影響により、エアコンが低調に推移し、また、新ＯＳの発売を控えたパソコン等の情報関連商品も低調でありましたが、エコポイント制度の開始により、薄型テレビ・冷蔵庫が順調に推移しました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[1], sentence, "");
		seikai = new Causal("エコポイント制度の開始", "薄型テレビ・冷蔵庫が順調に推移しました。", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		CausalExtraction.patternFlag = false;
		clue = "により";
		sentence = "【設備機器】売上高２，３８８百万円（前年同期比１１３．１％）設備機器につきましては、住宅用設備機器はオール電化ブームにより温水器が好調でしたが、冷夏の影響により家庭用エアコンは低調な推移となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[1], sentence, "");
		seikai = new Causal("冷夏の影響", "家庭用エアコンは低調な推移となりました。", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		// Prefixを適用しない場合: Pattern Cに適用される
		CausalExtraction.patternFlag = false;
		clue = "によるものであります。";
		sentence = "その主な要因は、当期純利益の影響により利益剰余金が４１０，５２０千円増加したものの、剰余金の配当により利益剰余金が２１２，１５５千円減少したことによるものであります。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("剰余金の配当により利益剰余金が２１２，１５５千円減少した", "その主な要因は、", "", "C");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		// 基点文節の末尾が助動詞の場合
		CausalExtraction.patternFlag = false;
		clue = "により、";
		sentence = "ガーデンセンターでは、猛暑の影響により、屋外植物の販売が苦戦したものの、屋内の観葉植物やインテリア、インターネット販売が好調に推移した結果、売上高は前年同期比増収となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("猛暑の影響", "屋外植物の販売が苦戦した", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));
	}

	@Test
	public void testGetInga() throws Exception {
		//ArrayList<Causal> causalList = this.ce.getInga("sample/test00.txt");
		//assertThat(causalList.size(), is(0));

		CausalExtraction.svmFlag = false;
		ArrayList<Causal> causalList = this.ce.getInga("sample/test1.txt");
		assertThat(3, is(causalList.size()));
		Causal seikai = new Causal("製菓・製パン向けの販売が総じて低調に推移した", "各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", "製菓原材料類は、", "B");
		assertThat(seikai.basis, is(causalList.get(0).basis));
		assertThat(seikai.result, is(causalList.get(0).result));
		assertThat(seikai.subj, is(causalList.get(0).subj));
		assertThat(seikai.pattern, is(causalList.get(0).pattern));
		assertThat("から、", is(causalList.get(0).clue));
		assertThat(1, is(causalList.get(0).line));
		assertThat("sample/test1.txt", is(causalList.get(0).filePath));
		assertThat("製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", is(causalList.get(0).sentence));

		CausalExtraction.svmFlag = true;
		CausalExtraction.setSvmHash(this.svmHash);
		causalList = this.ce.getInga("sample/test1.txt");
		assertThat(2, is(causalList.size()));
		causalList = this.ce.getInga("sample/test2.txt");
		assertThat(2, is(causalList.size()));

		CausalExtraction.svmFlag = false;
		causalList = this.ce.getInga("sample/tanshin2010.txt");
		assertThat(70, is(causalList.size()));

		causalList = this.ce.getInga("sample/sample_reuter_error.txt");
		assertThat(1, is(causalList.size()));
	}

}
