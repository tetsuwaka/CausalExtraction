package extractCausal;

public class Causal {

	public String basis = "";
	public String result = "";
	public String subj = "";
	public String pattern = "";
	public String clue = "";
	public String filePath = "";
	public int line;
	public String sentence ="";

	public Causal() {
		super();
	}

	public Causal(String basis, String result, String subj, String pattern) {
		super();
		this.basis = basis;
		this.result = result;
		this.subj = subj;
		this.pattern = pattern;
	}

}
