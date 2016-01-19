import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class SentimentTrainTest {
	Vector<String> fileNameList;
	Map<String, Integer> sentimentMap;
	Map<String, Integer> wordMap;
	int wordCount;
	int sentimentCount;
	Set<String> wordSet;
	Vector<Integer> labels;
	// row 0: +1
	// row 1: -1
	int[][] classCount; 
	int[][] z; 
	
	int[][] DitZik;
	int DisZik0; //  = 172998301;
	int DisZik1; // = 173089059;
	double[][] Pwtck;
	double[][] BernoulliPwtck;
	
	// final static double class0Const = -5.153281773640306;
	// final static double class1Const = -5.153806253945397;
	
	Map<Integer, Map<Integer, Integer>> countMap;

	public SentimentTrainTest() throws IOException {
		wordSet = new TreeSet<String>();
		wordMap = new TreeMap<String, Integer>();		
		wordCount = loadWordlist();
		sentimentMap = new TreeMap<String, Integer>();
		sentimentCount = loadSentimentMap();
		classCount = new int[2][wordCount];
		z = new int[2][wordCount];
		DitZik = new int[2][wordCount];
		Pwtck = new double[2][wordCount];
		BernoulliPwtck = new double[2][wordCount];
		for(int i = 0; i<2; i++) {
			for(int j = 0; j<wordCount; j++) {
				classCount[i][j]=0;
				z[i][j] = 0;
				DitZik[i][j] = 0;
				Pwtck[i][j] = 0.0;
				BernoulliPwtck[i][j]=0.0;
			}
		}
		DisZik0 = 0;
		DisZik1 = 0;
		fileNameList = new Vector<String>();
		fileNameList.addAll(sentimentMap.keySet());
		countMap = new TreeMap<Integer, Map<Integer, Integer>>();
		for(int i = 0; i<sentimentCount; i++) {
			Map<Integer, Integer> innerMap = new TreeMap<Integer, Integer>();
			countMap.put(i, innerMap);
		}
	}
	
	
	public void run() throws IOException {
		// start time
		Date d = new Date();
	    long s1;
	    s1 = d.getTime();
	    
		generateMatrix();
		generateClassMatrix();
		generatePwtck(s1);
		
	}
	

	// train only
	// Load count Matrix: word j in document i: map(i,j), like a 2-array
	/*
	private void loadMatrix() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\countmatrix.txt")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			// System.out.println(line);
			int index1 = Integer.valueOf(item[0]);
			int index2 = Integer.valueOf(item[1]);
			int value = Integer.valueOf(item[2]);
			Map<Integer,Integer> temp = countMap.get(index1);
			temp.put(index2, value);
			countMap.put(index1, temp);
		}
		br.close();		
	}
	*/	
	
	private void generateClassMatrix() throws IOException {
		Map<Integer, Integer> m = new TreeMap<Integer, Integer>();
		Vector<Integer> v = new Vector<Integer>(); 
		for(int i = 0; i<sentimentCount; i++) {
			m = countMap.get(i);
			v.removeAllElements();
			v.addAll(m.keySet());
			int sz = v.size();
			int value = 0;
			String fileName = fileNameList.get(i);
			int label = sentimentMap.get(fileName);
			if(label == 1) {
				for(int j = 0; j<sz; j++) {
					int index = v.get(j);
					value = m.get(index);
					classCount[0][index] = value; 
				}
			}
			else {
				for(int j = 0; j<sz; j++) {
					int index = v.get(j);
					value = m.get(index);
					classCount[1][index] = value;
				}
			}
		}
		
		// write classCount
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("bernoulli.txt")));		
		for(int i = 0; i<2; i++) {
			for(int j = 0; j<wordCount; j++) {
				bw.write(i+" "+j+" "+ classCount[i][j]+"\n");						
			}
			bw.flush();
		}
		bw.close();		
	}
	
	
	// for generate the Matrix
	private void generateMatrix() throws IOException {
		IKAnalyse analyse = new IKAnalyse();
		File path = new File(".\\data\\train2");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				tempFileName = (".\\data\\train2\\").concat(tempFileName);
				if(tempFileName.endsWith("txt")) {
					Map<Integer, Integer> inner = countMap.get(i);
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFileName)));
					while(true) {
						String line = br.readLine();
						if (line == null)
							break;
						Vector<String> v = analyse.splitString(line);
						int st = v.size();
						for(int t = 0; t<st; t++) {
							String s = v.get(t);
							if(s != null) {
								if(wordMap.get(s)!= null) {
									int columnIndex = wordMap.get(s);
									if(inner.get(columnIndex) != null) {
										int newvalue = inner.get(columnIndex)+1;
										inner.put(columnIndex, newvalue);
									}
									else {
										inner.put(columnIndex, 1);
									}
								}								
							}							
						}						
					}
					// System.out.println(linesCount);
					br.close();
					countMap.put(i, inner);
				}
			}
		}
		
		/*
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("countmatrix.txt")));		
		for(int i = 0; i<sentimentCount; i++) {
			for(int j = 0; j<wordCount; j++) {
				if(countMap.get(i).get(j) != null) {
					if(countMap.get(i).get(j) != 0)
						bw.write(i+" "+j+" "+ countMap.get(i).get(j)+"\n");						
				}
			}
			bw.flush();
		}
		bw.close();
		*/
		
	}
	
	
	// for load data only
	// generate wordlist from training data	
	private int loadWordlist() throws IOException {
		//IKAnalyse tokenizer
		IKAnalyse analyse = new IKAnalyse();
		File path = new File(".\\data\\train2");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				tempFileName = (".\\data\\train2\\").concat(tempFileName);
				if(tempFileName.endsWith("txt")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFileName)));
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
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("wordlist.txt")));
		Iterator<String> it = wordSet.iterator();
		int num = 0;
		while(it.hasNext()){
			String x = it.next();
			if(!isWord(x)) {
				bw.write(x+"\t"+num+"\n");
				wordMap.put(x, num);
				num++;
				bw.flush();				
			}
		}		
		bw.close();
		return wordMap.size();
	}
	
	private boolean isWord(String x) {
		char c = x.charAt(0);
		if((c >= '0' && c<='9') || (c>='a' && c<='z') || (c>='A' && c<='Z'))
			return true;
		return false;
	}
	
	private int loadSentimentMap() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\train2.rlabelclass")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			// System.out.println(line);
			sentimentMap.put(item[0], Integer.valueOf(item[1]));
		}
		br.close();
		return sentimentMap.size();
	}

	private int getCountMatrix(int i, int j) {
		if(countMap.get(i).get(j) == null)
			return 1;
		else 
			return countMap.get(i).get(j)+1;
	}
	
	private void generatePwtck(long l) throws IOException {
		// generate zik
		int st = fileNameList.size();
		for(int i = 0; i<st; i++) {
			int label = sentimentMap.get(fileNameList.get(i));
			if(label == 1) {
				z[0][i] = 1;
			}
			else if(label == -1) {
				z[1][i] = 1;
			}
		}
		
		// generate DitZik
		for(int i = 0; i<wordCount; i++) {
			int sum0 = 0;
			int sum1 = 0;
			for(int j = 0; j<sentimentCount; j++) {
				sum0 = sum0 + (z[0][j]*getCountMatrix(j, i));
				sum1 = sum1 + (z[1][j]*getCountMatrix(j, i));
			}
			DitZik[0][i] = sum0;
			DitZik[1][i] = sum1;
		}
		
		//generate DisZik 
		for(int i = 0; i<wordCount; i++) {
			DisZik0 += DitZik[0][i];
			DisZik1 += DitZik[1][i];
		}
		
		//generate PWtCk
		for(int i = 0; i<wordCount; i++) {
			Pwtck[0][i] = Math.log(1000000*((double)DitZik[0][i])/((double)DisZik0));
			Pwtck[1][i] = Math.log(1000000*((double)DitZik[1][i])/((double)DisZik1));
		}
		
		
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("PWtCk.txt")));	
		for(int i = 0; i<2; i++) {
			for(int j = 0; j<wordCount; j++) {
				bw.write(i+" "+j+" "+ Pwtck[i][j]+"\n");						
			}
			bw.flush();
		}
		bw.close();	
		
		//train end
		Date d = new Date();
	    long s2 = d.getTime();
	    String time = String.valueOf((s2-l)/(double)1000);
	    
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("constants.txt")));	
		
		bw2.write("DisZik0 = "+DisZik0+"\n");
		bw2.write("DisZik1 = "+DisZik1+"\n");
		bw2.write("class0Const = "+Math.log(1000000*1.0/(double)(DisZik0))+"\n");
		bw2.write("class1Const = "+Math.log(1000000*1.0/(double)(DisZik1))+"\n");		
		bw2.write("Training time is "+time+" seconds.\n");
		
		bw2.flush();
		bw2.close();	

	}		
}
