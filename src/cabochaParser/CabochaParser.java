package cabochaParser;

import java.util.ArrayList;

public class CabochaParser {
	public String cabochaResult;
	private ArrayList<POS> cabochaList = new ArrayList<POS>(); 
	
	public CabochaParser(String mecabResult) {
		this.cabochaResult = mecabResult;
	}
	
	ArrayList<POS> parse() {
		String lines[] = this.cabochaResult.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.startsWith("EOS")) {
				break;
			} else if (line.startsWith("* ")) {
				POS pos = new POS();
				String items[] = line.split("\\s");
				if (items[2].indexOf('D') != -1) {
					pos.id = Integer.parseInt(items[1]);
					pos.chunk = Integer.parseInt(items[2].substring(0, items[2].length() - 1));
				} else {
					pos.id = Integer.parseInt(items[1]);
					pos.chunk = Integer.parseInt(items[2]);
				}
				this.cabochaList.add(pos);
			} else if (line.equals("")) {
			} else {
				String tempList[] = line.split("\t");
				String items[] = tempList[1].split(",");
				Morph morph = new Morph();
				POS pos = this.cabochaList.get(this.cabochaList.size() - 1);
				pos.str.add(tempList[0]);
				morph.face = tempList[0];
				morph.base = items[items.length - 3];
				morph.pos = items[0];
				morph.posd = items[1];
				pos.morph.add(morph);
				this.cabochaList.set(this.cabochaList.size() - 1, pos);
			}
		}
		return this.cabochaList;
	}
	
	class POS {
		public int id;
		public int chunk;
		public ArrayList<String> str = new ArrayList<String>();
		public ArrayList<Morph> morph = new ArrayList<Morph>();
	}
	
	class Morph {
		public String face;
		public String base;
		public String pos;
		public String posd;
	}
	
}
