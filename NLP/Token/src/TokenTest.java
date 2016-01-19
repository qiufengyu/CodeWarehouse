import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class TokenTest {
	
	String s;
	Map<String, SpeechAndMeaning> wordMap = new TreeMap<String, SpeechAndMeaning>();
	Map<String, String> irregular = new TreeMap<String, String>();
	class SpeechAndMeaning {
		
		String speech;
		String meaning;
		
		public SpeechAndMeaning(String speech, String meaning) {
			this.speech = speech;
			this.meaning = meaning;
		}

		public SpeechAndMeaning() {
			speech = new String();
			meaning = new String();
		}
		
		public String getSpeech() {
			return speech;
		}
		public void setSpeech(String speech) {
			this.speech = speech;
		}
		public String getMeaning() {
			return meaning;
		}
		public void setMeaning(String meaning) {
			this.meaning = meaning;
		}
		public String toString() {
			return (speech+" "+meaning);
		}
			
	}
	
	public TokenTest() throws IOException {
		s = new String();
		loadDictionary();
	}
	
	public TokenTest(String ss) throws IOException {
		s = new String(ss);
		loadDictionary();
	}
	
	public void loadDictionary() throws IOException {
		// Load dictionary file
		setIrregular();
		File file = new File("dic_ec.txt");
		String line = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		while(true) {
			line = br.readLine();
			if(line == null)
				break;
			line = line.trim();
			StringTokenizer st = new StringTokenizer(line);
			// System.out.println(st.countTokens());
			String word = st.nextToken().toLowerCase();
			String partOfSpeech = new String();
			if(st.hasMoreTokens()) {
				partOfSpeech =  st.nextToken();
			}
			String meaning = new String();
			while(st.hasMoreTokens()) {
				meaning = meaning.concat(st.nextToken());
				meaning = meaning.concat(" ");
			}
			meaning = meaning.trim();
			// System.out.println(meaning);
			SpeechAndMeaning sam = new SpeechAndMeaning(partOfSpeech, meaning);
			wordMap.put(word, sam);
		}
		// System.out.println("End?");
		br.close();
	}
	

	public String find(String s) {
		/*
		System.out.println(s);
		System.out.println(wordMap.get(s));
		*/
		return (s+"\n"+wordMap.get(s));
	}

	public String stemmer(String s) {
		String temp = new String(s.toLowerCase());
		
		// directly find		
		if(wordMap.containsKey(temp)) {			
			return find(temp);
		}
		
		// Irregular verb
		if(irregular.containsKey(temp)) {
			// System.out.println("Irregular");
			String tempy = getIrregular(s);
			return find(tempy);
		}				
		// stemmer
		int length = temp.length();
		String tempx = new String(temp);
		if(length > 5) { // *??ing ->*?
			if(tempx.endsWith("ing") && tempx.charAt(length-4) == tempx.charAt(length-5)) {
				tempx = tempx.substring(0, length-4);
				// System.out.println("*??ing ->*?");
				return find(tempx);
			}
		}
		
		if(length > 4) { 
			// *ying -> *ie
			if(tempx.endsWith("ying")) {
				String temp0 = tempx.substring(0,length-4);
				temp0 = temp0.concat("ie");
				if(wordMap.containsKey(temp0)) {
					// System.out.println("*ying -> *ie");
					return find(temp0);
				}
			}
			else if(tempx.endsWith("ed") && tempx.charAt(length-3) == tempx.charAt(length-4)) {
				// *??ed -> *?
				String temp0 = tempx.substring(0, length-3);
				if(wordMap.containsKey(temp0)) {
					// System.out.println("*??ed -> *?");
					return find(temp0);
				}
			}
		}
		
		if(length > 3) {
			if(tempx.endsWith("ied")) {
				// *ied -> *y 
				String temp0 = tempx.substring(0, length-3);
				temp0 = temp0.concat("y");
				if(wordMap.containsKey(temp0)) {
					// System.out.println("*ied -> *y");
					return find(temp0);
				}
			}
			else if(tempx.endsWith("ies")) {
				// *ies -> *y 
				String temp0 = tempx.substring(0, length-3);
				temp0 = temp0.concat("y");
				if(wordMap.containsKey(temp0)) {
					// System.out.println("*ies -> *y");
					return find(temp0);
				}
			}
			else if(tempx.endsWith("ing")) {
				String temp0 = tempx.substring(0, length-3);
				String temp1 = temp0.concat("e");
				if(wordMap.containsKey(temp1)) {
					// *ing -> e*
					// System.out.println("*ing -> e*");
					return find(temp1);
				}				
				else {
					// *ing -> *
					if(wordMap.containsKey(temp0)) {
						// System.out.println("*ing -> *");
						return find(temp0);
					}					
				}
			}
		}
		
		if(length > 2) {
			if(tempx.endsWith("es")) {
				String temp0 = tempx.substring(0, length-2);
				String temp1 = temp0.concat("e");
				if(wordMap.containsKey(temp1)) {
					// *s -> *
					// System.out.println("*s -> *");
					return find(temp1);
				}				
				if(wordMap.containsKey(temp0)) {
					// *es -> *
					// System.out.println("*es -> *");
					return find(temp0);
				}				
			}
			else if(tempx.endsWith("ed")) {
				String temp0 = tempx.substring(0, length-2);
				String temp1 = temp0.concat("e");
				if(wordMap.containsKey(temp1)) {
					// *ed -> e*
					// System.out.println("*ed -> e*");
					return find(temp1);
				}				
				else {
					// *ing -> *
					if(wordMap.containsKey(temp0)) {
						// System.out.println("*ed -> *");
						return find(temp0);
					}					
				}
			}
		}
		
		if(length > 1) {
			if(tempx.endsWith("s")) {
				String temp0 = tempx.substring(0, length-1);
				if(wordMap.containsKey(temp0)) {
					// *s -> *
					// System.out.println("*s -> *");
					return find(temp0);
				}				
			}
		}		
		return noRegister(temp);
	}
	
	private String noRegister(String x) {
		// TO-DO
		return ("No Register word "+x+"!");
	}

	private String getIrregular(String s) {
		if(irregular.containsKey(s))
			return irregular.get(s);
		else 
			return s;
	}
	
	private void setIrregular() {
		// you can extend it!
		irregular.put("arose", "arise");
		irregular.put("arose", "arise");
		irregular.put("awoke", "awake");
		irregular.put("awoken", "awake");
		irregular.put("was", "be");
		irregular.put("were", "be");
		irregular.put("beaten", "beat");
		irregular.put("became", "become");
		irregular.put("began", "begin");
		irregular.put("bent", "bend");
		irregular.put("bitten", "bite");
		irregular.put("bled", "bleed");
		irregular.put("blew", "blow");
		irregular.put("blown", "blow");
		irregular.put("broke", "break");
		irregular.put("broken", "break");
		irregular.put("went", "go");
		irregular.put("gone", "go");
		irregular.put("sat", "sit");
	}
	
}
