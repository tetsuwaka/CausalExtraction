package cabochaParser;

import java.io.IOException;
import java.util.ArrayList;

import cabochaParser.CabochaParser.POS;

public class parserTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		String test = StringUtilities.join("\n", ExecCabocha.exec("今日は晴れ晴れ愉快"));
		//String test = "* 0 1D 0/1 0.000000\n今日\t名詞,副詞可能,*,*,*,*,今日,キョウ,キョー\nは\t助詞,係助詞,*,*,*,*,は,ハ,ワ\n* 1 -1D 0/0 0.000000\n晴れ\t名詞,一般,*,*,*,*,晴れ,ハレ,ハレ\nEOS";
		CabochaParser parser = new CabochaParser();
		ArrayList<POS> results = parser.parse(test);
		for (POS pos : results) {
			System.out.println(pos.str);
		}
		System.out.println("終了");
		
		int[] a = {1, 2, 3};
		System.out.println(a[a.length-1]);
		
	}

}
