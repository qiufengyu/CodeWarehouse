import java.io.*;
import java.util.*;

public class FileLoad {
	
	private IKAnalyse analyse;
	
	private Vector<String> allWordVector;
	private Vector<String>[] singleVector;
	private int[] wordCountByLine;
	private int linesCount;
	
	private Set<String> allWordSet;
	private Map<String, Integer>[] singleMap;
	
	public FileLoad() {

	}
	
	public FileLoad(String fileName) throws IOException {
		init();
		singleFileReader(fileName);
		dataTransformat();
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		analyse = new IKAnalyse();
		allWordVector = new Vector<String>();
		singleVector = new Vector[200];
		allWordSet = new TreeSet<String>();
		singleMap = new HashMap[200];
		wordCountByLine = new int[200];
		for(int k = 0; k<200; k++) {
			singleVector[k] = new Vector<String>();
			singleMap[k]= new HashMap<String, Integer>();
			wordCountByLine[k] = -1;
		}
		linesCount = 0;		
	}	
	
	public Set<String> fileReader() throws IOException {
		// 读取10个文件，获取完整字典信息
		IKAnalyse analyse = new IKAnalyse();
		Set<String> wordSet = new TreeSet<String>();
		File path = new File(".");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				if(tempFileName.endsWith("txt")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFileName),"UTF-8"));
					while(true) {
						String line = br.readLine();
						if (line == null)
							break;
						wordSet.addAll(analyse.splitString(line));
					}
					// System.out.println(linesCount);
					br.close();
				}
			}
		}	
		return wordSet;
	}
	
	
	private void singleFileReader(String fileName) throws IOException {
		// 处理单个文件
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			singleVector[linesCount] = analyse.splitString(line);
			wordCountByLine[linesCount] = singleVector[linesCount].size();
			allWordVector.addAll(singleVector[linesCount]);
			linesCount++;
		}
		// System.out.println(linesCount);
		br.close();
	}
	
	/*public void displayWord() {
		int size = singleVector[0].size();
		for(int i = 0; i<size; i++) {
			System.out.print(singleVector[0].get(i)+" ");
		}
		System.out.println();
	}*/
	
	private void putInAllWordSet() {
		allWordSet.addAll(allWordVector);
	}
	
	private void putInSingleWordMap() {
		for(int i = 0; i<linesCount; i++) {
			int sizeValue = singleVector[i].size();
			for(int sizei = 0; sizei < sizeValue; sizei++) {
				String temp = singleVector[i].get(sizei);
				if(singleMap[i].get(temp)==null) {
					// first time
					singleMap[i].put(temp, 1);
				}
				else {
					// count should increase by 1
					int oldValue = singleMap[i].get(temp);
					singleMap[i].put(temp, oldValue+1);
				}
			}
			
		}
	}
	
	private void dataTransformat() {
		// 数据结构转化至Map
		putInAllWordSet();
		// System.out.println(allWordSet.size());
		putInSingleWordMap();
		for(int i = 0; i<linesCount; i++) {
			// System.out.println("----"+singleMap[i].size());
			// System.out.println(wordCountLines[i]);
		}
	}
	
	public int getLines() {
		return linesCount;
	}
	
	public int getWordCountByLine(int index) {
		return wordCountByLine[index];
	}	
	
	public int getAllWordCount() {
		return allWordSet.size();
	}
	
	public int getN_ij(String word, int lines) {
		if(singleMap[lines].get(word)==null)
			return 0;
		else
			return singleMap[lines].get(word);
	}
	
	public Set<String> getWordSet() {
		return allWordSet;
	}
	
	public int getContainWordCount(String string) {
		int count = 0;
		for(int i = 0; i<linesCount; i++) {
			if(singleMap[i].get(string)==null)
				continue;
			else
				count++;
		}
		return count;
	}
	
}
