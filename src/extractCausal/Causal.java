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
	
	public String toJson() {
		String json = "{";
		json += "\"clue\": " + quote(this.clue) + ", ";
		json += "\"basis\": " + quote(this.basis) + ", ";
		json += "\"result\": " + quote(this.result) + ", ";
		json += "\"subj\": " + quote(this.subj) + ", ";
		json += "\"pattern\": " + quote(this.pattern) + ", ";
		json += "\"filePath\": " + quote(this.filePath) + ", ";
		json += "\"line\": " + this.line;
		json += "}";
		return json;
	}

	/**
	 * 文字列を JSON の文字列リテラル（ダブルクォート付き）に変換する。
	 * " と \\ 、および制御文字をエスケープする。
	 */
	static String quote(String s) {
		if (s == null) {
			return "\"\"";
		}
		StringBuilder sb = new StringBuilder(s.length() + 2);
		sb.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '"':  sb.append("\\\""); break;
			case '\\': sb.append("\\\\"); break;
			case '\b': sb.append("\\b"); break;
			case '\f': sb.append("\\f"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '\t': sb.append("\\t"); break;
			default:
				if (c < 0x20) {
					sb.append(String.format("\\u%04x", (int) c));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
