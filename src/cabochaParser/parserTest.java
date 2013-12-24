package cabochaParser;

import java.util.ArrayList;
import cabochaParser.CabochaParser.POS;

public class parserTest {

	public static void main(String[] args) {
		String test = "* 0 1D 0/1 0.000000\n今日\t名詞,副詞可能,*,*,*,*,今日,キョウ,キョー\nは\t助詞,係助詞,*,*,*,*,は,ハ,ワ\n* 1 -1D 0/0 0.000000\n晴れ\t名詞,一般,*,*,*,*,晴れ,ハレ,ハレ\nEOS";
		CabochaParser parser = new CabochaParser(test);
		ArrayList<POS> results = parser.parse();
		System.out.println("終了");
	}

}
