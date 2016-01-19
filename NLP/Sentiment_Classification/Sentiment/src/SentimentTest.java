import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class SentimentTest {
	Vector<String> fileNameList;
	Map<String, Integer> groundMap;
	Map<String, Integer> myMap;
	Map<String, Integer> wordMap;
	int wordCount;
	int sentimentCount;
	// Set<String> wordSet;
	Vector<Integer> labels;
	// row 0: +1
	// row 1: -1
	// int[][] classCount; 
	// int[][] z; 
	
	// int[][] DitZik;
	final static int DisZik0 = 172998301;
	final static int DisZik1 = 173089059;
	double[][] Pwtck;
	double[][] BernoulliPwtck;
	
	final static double class0Const = -5.153281773640306;
	final static double class1Const = -5.153806253945397;
	
	// Map<Integer, Map<Integer, Integer>> countMap;

	public SentimentTest() throws IOException {
		// wordSet = new TreeSet<String>();
		wordMap = new TreeMap<String, Integer>();		
		wordCount = loadWordlist();
		groundMap = new TreeMap<String, Integer>();
		myMap = new TreeMap<String, Integer>();
		labels = new Vector<Integer>();
		// sentimentCount = loadSentimentMap();
		// classCount = new int[2][wordCount];
		// z = new int[2][wordCount];
		// DitZik = new int[2][wordCount];
		Pwtck = new double[2][wordCount];
		BernoulliPwtck = new double[2][wordCount];
		for(int i = 0; i<2; i++) {
			for(int j = 0; j<wordCount; j++) {
				// classCount[i][j]=0;
				// z[i][j] = 0;
				// DitZik[i][j] = 0;
				Pwtck[i][j] = 0.0;
				BernoulliPwtck[i][j]=0.0;
			}
		}
		// DisZik0 = 0;
		// DisZik1 = 0;
		fileNameList = new Vector<String>();
//		fileNameList.addAll(sentimentMap.keySet());
//		countMap = new TreeMap<Integer, Map<Integer, Integer>>();
//		for(int i = 0; i<sentimentCount; i++) {
//			Map<Integer, Integer> innerMap = new TreeMap<Integer, Integer>();
//			countMap.put(i, innerMap);
//		}
	}
	
	private void loadPwtck() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\PWtCk.txt")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			int indexx = Integer.valueOf(item[0]);
			int indexy = Integer.valueOf(item[1]);
			double v = Double.valueOf(item[2]);
			// System.out.println(line);
			Pwtck[indexx][indexy] = v;
		}
		br.close();
	}
	
	private int loadWordlist() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\wordlist.txt")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split("\t");
			// System.out.println(line);
			wordMap.put(item[0], Integer.valueOf(item[1]));
		}
		br.close();
		return wordMap.size();
	}
	
	public void test() throws IOException {
		loadPwtck();
		loadGroundMap();
		// start time
		Date d = new Date();
	    long s1,s2;
	    s1 = d.getTime();
	    
		handle();
		
		// end time
	    d = new Date();
	    s2 = d.getTime();
	    String time = String.valueOf((s2-s1)/(double)1000);
		String timeString = "Execution time is "+time+ " seconds.";
		// write label result
		writeResult();
		// write execution result
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Result.txt")));		
		bw.write(timeString+"\n");	
		String e = evaluate();
		bw.write(e);
		bw.flush();
		bw.close();		
		
		// Another test!
		bernoulliModel();
	}
	

	private void handle() throws IOException {
		IKAnalyse analyse = new IKAnalyse();
		File path = new File(".\\data\\test2");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				fileNameList.addElement(tempFileName);
				tempFileName = (".\\data\\test2\\").concat(tempFileName);
				if(tempFileName.endsWith("txt")) {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFileName)));
					Vector<String> v = new Vector<String>();
					while(true) {
						String line = br.readLine();
						if (line == null)
							break;
						v.addAll(analyse.splitString(line));
					}
					br.close();
					double class0 = 0.0; // for +1
					double class1 = 0.0; // for -1
					int st = v.size();
					for(int t = 0; t<st; t++) {
						String s = v.get(t);
						// System.out.println(s);
						if(s != null) {
							if(wordMap.get(s)!= null) {
								int columnIndex = wordMap.get(s);
								class0 += Pwtck[0][columnIndex];
								class1 += Pwtck[1][columnIndex];
							}
							else {
								class0 += class0Const;
								class1 += class1Const;
							}
						}
					}							
					// compare class0 or class1 value
					if(class0 > class1) {
						labels.addElement(1);
					}
					else if(class0 < class1) {
						labels.addElement(-1);
					}
					else {
						// randomly guess
						int rand = (int) Math.round(Math.random()*100);
						if(rand%2 == 1) labels.addElement(1);
						else labels.addElement(-1);
					}
				}

			}
		}
	}
	
	private void loadGroundMap() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\test2.rlabelclass")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			// System.out.println(line);
			groundMap.put(item[0], Integer.valueOf(item[1]));
		}
		br.close();
	}
	
	private void writeResult() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("mytest2.rlabelclass")));
		int sz = fileNameList.size();
		for(int i = 0; i<sz; i++) {		
			int l = labels.get(i);
			if(l > 0) {
				bw.write(fileNameList.get(i)+" +"+l+"\n");
			}
			else {
				bw.write(fileNameList.get(i)+" "+ l+"\n");
			}
			myMap.put(fileNameList.get(i), l);
			bw.flush();
		}
		bw.close();		
	}
	
	private String evaluate() {
		int sz = fileNameList.size();
		int St = 0;
		int G = 0;
		int StandG = 0;
		// St\G\StandG
		for(int i = 0; i<sz; i++) {
			if(labels.get(i) == 1)
				St++;
			if(groundMap.get(fileNameList.get(i)) == 1) {
				G++;
			}
			if(labels.get(i) == 1 && groundMap.get(fileNameList.get(i)) == 1) {
				StandG++;
			}
		}
		
		int count = 0;
		for(int i = 0; i<sz; i++) {
			if(labels.get(i) == groundMap.get(fileNameList.get(i)))
				count++;
		}		
		
		double precision = 100.0*((double)StandG)/((double)St);
		double recall = 100.0*((double)StandG)/((double)G);
		double f_1 = 2.0*precision*recall/(recall+precision);
		double accuracy = 100.0*((double)count)/((double)sz);
		
		String resultString = new String();
		resultString = resultString+"precision = "+String.format("%.4f", precision)+"%\n";
		resultString = resultString+"recall = "+String.format("%.4f", recall)+"%\n";
		resultString = resultString+"F1 = "+String.format("%.4f", f_1)+"%\n";
		resultString = resultString+"Accuracy = "+String.format("%.4f", accuracy)+"%\n";
		return resultString;		
	}
	
	
	private void bernoulliModel() throws IOException {
		// load ground labels
		// loadGroundMap();
		// load classcount(0->+1, 1->-1)
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\data\\bernoulli.txt")));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			int indexx = Integer.valueOf(item[0]);
			int indexy = Integer.valueOf(item[1]);
			int v = Integer.valueOf(item[2]);
			// System.out.println(line);
			double value = (((double)(v+1))/6000.0);
			BernoulliPwtck[indexx][indexy] = value;
		}
		br.close();
		
		// clear
		Vector<String> wordV = new Vector<String>();
		wordV.addAll(wordMap.keySet());
		fileNameList.removeAllElements();
		labels.removeAllElements();
		
		// start time
		Date d = new Date();
	    long s1,s2;
	    s1 = d.getTime();
	    	
		// Test
		double class0 = 0.0;
		double class1 = 0.0;		
		
		IKAnalyse analyse = new IKAnalyse();
		File path = new File(".\\data\\test2");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				fileNameList.addElement(tempFileName);
				tempFileName = (".\\data\\test2\\").concat(tempFileName);
				if(tempFileName.endsWith("txt")) {
					BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(tempFileName)));
					Vector<String> v = new Vector<String>();
					while(true) {
						String line = br2.readLine();
						if (line == null)
							break;
						v.addAll(analyse.splitString(line));
					}
					br2.close();
					for(int t = 0; t<wordCount; t++) {
						String s = wordV.get(t);
						// System.out.println(s);
						if(v.contains(s)) {
							class0 += Math.log(BernoulliPwtck[0][t]);
							class1 += Math.log(BernoulliPwtck[1][t]);
						}
						else {
							class0 += Math.log(1-BernoulliPwtck[0][t]);
							class1 += Math.log(1-BernoulliPwtck[1][t]);
						}
					}							
					// compare class0 or class1 value
					if(class0 > class1) {
						labels.addElement(1);
					}
					else if(class0 < class1) {
						labels.addElement(-1);
					}
					else {
						// randomly guess
						int rand = (int) Math.round(Math.random()*100);
						if(rand%2 == 1) labels.addElement(1);
						else labels.addElement(-1);
					}
				}				
			}
		}
		
		// end time
	    d = new Date();
	    s2 = d.getTime();
	    String time = String.valueOf((s2-s1)/(double)1000);
		String timeString = "Bernoulli Model execution time is "+time+ " seconds.";
		
		// write label result
		BufferedWriter bw0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("bernoullitest2.rlabelclass")));
		int sz = fileNameList.size();
		for(int i = 0; i<sz; i++) {		
			int l = labels.get(i);
			if(l > 0) {
				bw0.write(fileNameList.get(i)+" +"+l+"\n");
			}
			else {
				bw0.write(fileNameList.get(i)+" "+ l+"\n");
			}
			myMap.put(fileNameList.get(i), l);
			bw0.flush();
		}
		bw0.close();		
		
		// write result file		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("bernoulliResult.txt")));		
		bw.write(timeString+"\n");	
		String e = evaluate();
		bw.write(e);
		bw.flush();
		bw.close();		
	}
	
	
	/*
	// train only
	// Load count Matrix: word j in document i: map(i,j), like a 2-array
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
	
	/* 
	private void genClassMatrix() throws IOException {
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
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("classcount.txt")));		
		for(int i = 0; i<2; i++) {
			for(int j = 0; j<wordCount; j++) {
				bw.write(i+" "+j+" "+ classCount[i][j]+"\n");						
			}
			bw.flush();
		}
		bw.close();		
	}
	*/
	
	/* for generate the Matrix
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
				}
			}
		}
		
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
	}
	*/
	
	/* for load data only
	// generate wordlist from training data	
	private void wordlist() throws IOException {
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
				num++;
				bw.flush();
			}
		}		
		bw.close();
	}
	
	private boolean isWord(String x) {
		char c = x.charAt(0);
		if((c >= '0' && c<='9') || (c>='a' && c<='z') || (c>='A' && c<='Z'))
			return true;
		return false;
	}
	*/
	
	/*
	private int getCountMatrix(int i, int j) {
		if(countMap.get(i).get(j) == null)
			return 1;
		else 
			return countMap.get(i).get(j)+1;
	}
	
	private void generatePwtck() throws IOException {
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
		System.out.println(DisZik0);
		System.out.println(DisZik1);
		System.out.println(Math.log(1000000*1.0/(double)(DisZik0)));
		System.out.println(Math.log(1000000*1.0/(double)(DisZik1)));
	}	
	*/
}
