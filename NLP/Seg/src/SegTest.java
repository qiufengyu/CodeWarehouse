import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

public class SegTest {

	String sentence;
	Set<String> wordSet;
	
	public SegTest() throws IOException {
		sentence = new String();
		wordSet = new TreeSet<String>();
		loadDictionary();
	}
	
	public SegTest(String s) throws IOException {
		sentence = new String(s);
		wordSet = new TreeSet<String>();
		loadDictionary();
	}
	
	private void loadDictionary() throws IOException {
		// Load dictionary file
		File file = new File("dic_ce.txt");
		String line = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		while(true) {
			line = br.readLine();
			if(line == null)
				break;
			line = line.trim();
			StringTokenizer st = new StringTokenizer(line, ",");
			// System.out.println(st.countTokens());
			String token = new String();
			while(st.hasMoreTokens()) {
				token = st.nextToken();
				if(!isEnglish(token)) {
					// System.out.println(token);
					wordSet.add(token);
				}
			}
		}
		// System.out.println(i);
		// System.out.println(wordSet.size());
		br.close();
	}
	
	// src: 中文分词算法 之 基于词典的正向最大匹配算法, by 杨尚川
	
	public Vector<String> seg(String s) {
		Vector<String> resultVector = new Vector<String>();
		
		String testString = new String(s);
	
		while(testString.length() > 0) {
			String testSeg = new String(testString);
			while(!wordSet.contains(testSeg)) {
				// length is 1 -> single word
				if(testSeg.length() == 1) {
					break;
				}
				// length - 1 to search
				testSeg = testSeg.substring(0, testSeg.length()-1);
			}
			resultVector.addElement(testSeg);
			// delete the matched part then repeat the match process
			testString = testString.substring(testSeg.length());			
		}		
		System.out.println(resultVector);
		return resultVector;
	}
	
	private boolean isEnglish(String x) {
		return x.matches("^[a-zA-Z\\'\\s\\.-]*");
	}
}
