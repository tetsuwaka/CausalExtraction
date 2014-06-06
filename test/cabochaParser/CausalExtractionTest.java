package cabochaParser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import cabochaParser.CabochaParser.POS;

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
	public void testGetResultVP() {
		fail("Not yet implemented");
	}

}
