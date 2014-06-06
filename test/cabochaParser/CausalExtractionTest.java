package cabochaParser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class CausalExtractionTest {

	CausalExtraction ce = new CausalExtraction();
	
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
	public void testGetCoreIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveParticle() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResultVP() {
		fail("Not yet implemented");
	}

}
