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

}
