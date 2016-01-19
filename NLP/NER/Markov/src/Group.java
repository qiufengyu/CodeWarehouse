
public class Group {
	char word;
	String type;
	char bori;
	String label;
	
	
	/**
	 * @param word
	 * @param type
	 * @param borI
	 */
	public Group(char word, String type, char bori, String label) {
		this.word = word;
		this.type = type;
		this.bori = bori;
		this.label = label;
	}
	public char getWord() {
		return word;
	}
	public void setWord(char word) {
		this.word = word;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public char getBori() {
		return bori;
	}
	public void setBorI(char bori) {
		this.bori = bori;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
