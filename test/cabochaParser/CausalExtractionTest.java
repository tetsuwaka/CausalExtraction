package cabochaParser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import cabochaParser.CabochaParser.*;

public class CausalExtractionTest {

	CausalExtraction ce = new CausalExtraction();
	CabochaParser parser = new CabochaParser();
	
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
		assertThat("損した", is(this.ce.removeParticle(caboList.get(5))));
	}

	@Test
	public void testGetResultVP() throws Exception {
		String sentence = "円高による不況の影響で、買い物客が激減した。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("買い物客が激減した。", is(this.ce.getResultVP(caboList, "で、", 2)));
		
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetResultNP() throws Exception {
		String sentence = "円高による不況の影響で、買い物客が激減。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況の影響", is(this.ce.getResultNP(caboList, 0)));
		
		sentence = "円高による不況で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況", is(this.ce.getResultNP(caboList, 0)));
		
//		fail("Not yet implemented");
		// 追加した方がいいが、思いつかない
	}

	@Test
	public void testGetSubj() {
		fail("Not yet implemented");
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
}
