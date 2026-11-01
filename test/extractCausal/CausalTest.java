package extractCausal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

public class CausalTest {

	@Test
	public void testToJson() {
		Causal seikai = new Causal("円高", "不況になった。", "TTT", "A");
		seikai.clue = "ため、";
		seikai.filePath = "test";
		seikai.line = 99;
		assertThat(seikai.toJson(), is("{\"clue\": \"ため、\", \"basis\": \"円高\", \"result\": \"不況になった。\", \"subj\": \"TTT\", \"pattern\": \"A\", \"filePath\": \"test\", \"line\": 99}"));
	}

	@Test
	public void testToJsonEscapesSpecialCharacters() {
		Causal c = new Causal("「円高\"ドル安\"」", "C:\\path\\to", "タブ\tあり", "A");
		c.clue = "改行\nあり";
		c.filePath = "test";
		c.line = 1;
		assertThat(c.toJson(), is("{\"clue\": \"改行\\nあり\", \"basis\": \"「円高\\\"ドル安\\\"」\", \"result\": \"C:\\\\path\\\\to\", \"subj\": \"タブ\\tあり\", \"pattern\": \"A\", \"filePath\": \"test\", \"line\": 1}"));
	}

	@Test
	public void testToJsonWithNullField() {
		Causal c = new Causal("a", "b", null, "A");
		assertThat(c.toJson(), is("{\"clue\": \"\", \"basis\": \"a\", \"result\": \"b\", \"subj\": \"\", \"pattern\": \"A\", \"filePath\": \"\", \"line\": 0}"));
	}

}
