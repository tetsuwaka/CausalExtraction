package extractCausal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import utilities.FileUtilities;

import java.util.ArrayList;
import java.util.HashMap;

public class runExtractCausalTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    String[] demonList = FileUtilities.readLines("sample/demonstrative_list.txt");
    ArrayList<String[]> clueList = FileUtilities.readClueList("sample/clue_list.txt");
    ArrayList<String[]> additionalData = FileUtilities.readAdditionalData("sample/additional_data.txt");
    HashMap<String, Integer> svmHash = FileUtilities.readSvmResults("test/sampleData/svm_result4test.txt");

    CausalExtraction ce = new CausalExtraction(clueList, demonList);

    @Test
    public void testPrintJson() {
        CausalExtraction.svmFlag = true;
        CausalExtraction.setSvmHash(this.svmHash);
        ArrayList<Causal> causalList = this.ce.getInga("sample/test1.txt");
        String ls = System.getProperty("line.separator");

        // 結果を出力
        for (Causal causal : causalList) {
            System.out.println(causal.toJson());
        }

        String result = "{\"clue\": \"から、\", \"basis\": \"製菓・製パン向けの販売が総じて低調に推移した\", \"result\": \"各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。\", \"subj\": \"製菓原材料類は、\", \"pattern\": \"B\", \"filePath\": \"sample/test1.txt\", \"line\": 1}" +
                        ls +
                        "{\"clue\": \"ため。\", \"basis\": \"前期末の有価証券評価差額金が十七億円強の含み損となった\", \"result\": \"配当原資が不足するのは、\", \"subj\": \"\", \"pattern\": \"C\", \"filePath\": \"sample/test1.txt\", \"line\": 4}" +
                        ls;
        assertThat(outContent.toString(), is(result));
    }
}
