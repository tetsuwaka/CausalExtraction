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
		StringBuilder json = new StringBuilder("{");
		json.append("\"clue\": ").append(escapeJsonString(this.clue)).append(", ");
		json.append("\"basis\": ").append(escapeJsonString(this.basis)).append(", ");
		json.append("\"result\": ").append(escapeJsonString(this.result)).append(", ");
		json.append("\"subj\": ").append(escapeJsonString(this.subj)).append(", ");
		json.append("\"pattern\": ").append(escapeJsonString(this.pattern)).append(", ");
		json.append("\"filePath\": ").append(escapeJsonString(this.filePath)).append(", ");
		json.append("\"line\": ").append(this.line);
		json.append("}");
		return json.toString();
	}
	
	/**
	 * Escape special characters in JSON strings to prevent malformed JSON
	 * @param str input string
	 * @return escaped JSON string
	 */
	private String escapeJsonString(String str) {
		if (str == null) {
			return "null";
		}
		StringBuilder escaped = new StringBuilder("\"");
		for (char c : str.toCharArray()) {
			switch (c) {
				case '"':
					escaped.append("\\\"");
					break;
				case '\\':
					escaped.append("\\\\");
					break;
				case '\b':
					escaped.append("\\b");
					break;
				case '\f':
					escaped.append("\\f");
					break;
				case '\n':
					escaped.append("\\n");
					break;
				case '\r':
					escaped.append("\\r");
					break;
				case '\t':
					escaped.append("\\t");
					break;
				default:
					if (c < 0x20) {
						escaped.append(String.format("\\u%04x", (int) c));
					} else {
						escaped.append(c);
					}
					break;
			}
		}
		escaped.append("\"");
		return escaped.toString();
	}

}
