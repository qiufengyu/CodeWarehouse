import java.util.*;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.*;

public class SengenTest {
	String[] words;
	int length;
	Map<String, Integer>wordMap;
	// The size of the matrix
	int N;
	int[][] countMatrix;
	int[] countSum;
	final double M;
	Map<Integer, Map<Integer, Double>>pwMap;
	
	//for Permutation
	Vector<Vector<Integer>> permutationList;
	Vector<Double> pwList;
	
	public SengenTest() throws IOException {
		words = new String[20];
		wordMap = new TreeMap<String, Integer>();
		loadDataDic();
		// countMatrix = new int[N][N];
		countSum = new int[N];
		M = Math.log(100*(double)1/(double)N);
		pwMap = new TreeMap<Integer, Map<Integer, Double>>();
		for(int i = 0; i<N; i++) {
			Map<Integer, Double> inmap = new TreeMap<Integer, Double>();
			/*
			for(int j = 0; j<N; j++) {
				countMatrix[i][j] = 0;
				// pwMatrix[i][j] = 0.0;
			}
			*/
			countSum[i]=0;
			// Map<Integer, Double> inmap = new TreeMap<Integer, Double>();
			pwMap.put(i, inmap);
		}		
		// loadDataCorpus();
		loadCountSum();
		loadPWMatrix();
		
		permutationList = new Vector<Vector<Integer>>();
		pwList = new Vector<Double>();
	}
	
	public void setWords(String s) {
		words = s.split(" ");
		length = words.length;
	}
	
	public void loadDataDic() throws IOException {
		File target = new File("word.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split("\t");
			String key = item[0];
			Integer value = Integer.valueOf(item[1]);
			wordMap.put(key, value);
		}
		br.close();
		N = wordMap.size();
	}
	
	
	public void loadCountSum() throws IOException {
		File target = new File("countsum.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
		int i = 0;
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			int value = Integer.valueOf(line);
			countSum[i] = value;
			i++;
		}
		br.close();
	}
	
	public void loadPWMatrix() throws IOException {
		File target = new File("pwmatrix.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] item = line.split(" ");
			Integer i1 = Integer.valueOf(item[0]);
			Integer i2 = Integer.valueOf(item[1]);
			Map<Integer, Double> inm = pwMap.get(i1);
			inm.put(i2, Double.valueOf(item[2]));			
		}
		br.close();
	}
	
	// for train only
	public void loadDataCorpus() throws IOException {
		File target = new File("corpus.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
		Vector<String> vector = new Vector<String>();
		while(true) {
			vector.removeAllElements();
			String line = br.readLine();
			if (line == null)
				break;
			Reader reader = new StringReader(line);
			IKSegmenter segmenter = new IKSegmenter(reader, true);
			Lexeme lexeme;
			while((lexeme = segmenter.next())!=null) {
				vector.add(lexeme.getLexemeText());
			}
			Vector<String> vtemp = new Vector<String>(vector);
			// Eliminate the unuseful word, like english and numbers
			int sz = vector.size();
			for(int t = 0; t<sz; t++) {
				String temp = vector.get(t);
				if(!wordMap.containsKey(temp)) {
					vtemp.remove(temp);
				}
			}
			// countMatrix
			int sz2 = vtemp.size();
			for(int u = 1; u<sz2; u++) {
				String former = vtemp.get(u-1);
				String latter = vtemp.get(u);
				int indexFormer = wordMap.get(former);
				int indexLatter = wordMap.get(latter);
				countMatrix[indexFormer][indexLatter]++;			
			}			
		}		
		br.close();
	}
	// for train only
	public void writeCountMatrix() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("countmatrix.txt")));
		for(int i = 0; i<N; i++) {
			for(int j = 0; j<N; j++) {
				if(countMatrix[i][j]>0) {
					bw.write(i+" "+j+" "+countMatrix[i][j]+"\n");
				}
			}
			bw.flush();
		}
		bw.close();
	}
	// for train only
	public void calPWMatrix() {
		// add-1 smoothing
		for(int i = 0; i<N; i++) {
			for(int j = 0; j<N; j++) {
				// countMatrix[i][j]++;
			}
		}
		// count sum
		for(int i = 0; i<N; i++) {
			int tempsum = 0;
			for(int j = 0; j<N; j++) {
				tempsum += countMatrix[i][j];
			}
			countSum[i] = tempsum;
		}
		
		//calculate probability
		for(int i = 0; i<N; i++) {
			Map<Integer, Double> inm = pwMap.get(i);
			for(int j = 0; j<N; j++) {
				if(countMatrix[i][j]>0)
					inm.put(j, (double)(countMatrix[i][j]+1)/(double)(countSum[i]+N));
			}
			pwMap.put(i, inm);
		}		
	}
	// for train only
	public void writeCountSum() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("countsum.txt")));
		for(int j = 0; j<N; j++) {
			bw.write(countSum[j]+"\n");
		}
		bw.flush();
		bw.close();
		
	}
	// for train only
	public void writePWMatrix() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("pwmatrix.txt")));
		double d;
		for(int i = 0; i<N; i++) {
			for(int j = 0; j<N; j++) {
				if(pwMap.get(i).get(j) != null) {
					d = 100*pwMap.get(i).get(j).doubleValue();
					bw.write(i+" "+j+" "+d+"\n");
				}
				bw.flush();
			}
		}
		bw.close();
	}
	// get n-gram values by index
	public double getLogValue(int i, int j) {
		if(pwMap.get(i).get(j) != null) {
			double d = pwMap.get(i).get(j).doubleValue();
			return Math.log(d);
		}
		else {
			double d1 = (double)1/(double)(countSum[i]+N);
			return Math.log(d1);
		}
	}
	
	// http://blog.csdn.net/randyjiawenjie/article/details/6313729
	public void genSerial(int[] str) {
		arrange(str, 0, str.length);
	}
	
	private void arrange (int[] str, int st, int len) {
        if (st == len - 1) {
        	Vector<Integer> v = new Vector<Integer>();
            for (int i = 0; i<len; i++) {
            	v.add(str[i]);
                // System.out.print(str[i]+ "  ");
            }
            permutationList.add(v);
        }  
        else {  
            for (int i = st; i < len; i ++) {  
                swap(str, st, i);  
                arrange(str, st + 1, len);  
                swap(str, st, i);  
            }  
        }           
    }  
	
	private void swap(int[] str, int i, int j) {  
        int temp = str[i];  
        str[i] = str[j];  
        str[j] = temp;  
    }  

	public String showResults() {
		// int maxIndex = 0;
		// Record max index
		String ret= new String("  ");
		Vector<Integer> tempv = new Vector<Integer>();
		int size = pwList.size();
		double maxValue = -9999.9;
		for(int i = 0; i<size; i++) {
			if(maxValue < pwList.get(i)) {
				// maxIndex = i;
				maxValue = pwList.get(i);
			}
		}
		for(int i = 0; i<size; i++) {
			if(pwList.get(i) == maxValue)
				tempv.addElement(i);
		}
		int tsize = tempv.size();
		for(int t = 0; t<tsize; t++) {
			Vector<Integer> rvec = permutationList.get(tempv.get(t));
			int rsize = rvec.size();
			for(int j = 0; j<rsize; j++) {
				ret = ret.concat(words[rvec.get(j)]);
			}
			ret = ret.concat("\n  ");
		}
		return ret;
	}
	
	
	public void run() {
		// System.out.println(words);
		int [] a = new int[words.length];
		for(int i = 0; i<words.length; i++) {
			a[i]=i;
		}
		permutationList.removeAllElements();
		pwList.removeAllElements();
		genSerial(a);
		// permutationList and pwList
		int size = permutationList.size();
		for(int j = 0; j<size; j++) {
			double x = 0.0;
			Vector<Integer> v = permutationList.get(j);
			// System.out.println(v);
			int vsize = v.size();
			for(int t = 1; t<vsize; t++) {
				if(wordMap.get(words[v.get(t-1)]) == null) {
					x = x + M;
				}
				else if(wordMap.get(words[v.get(t)]) == null) {
					x = x + M;
				}
				else {
					int formerIndex = wordMap.get(words[v.get(t-1)]);
					int latterIndex = wordMap.get(words[v.get(t)]);
					double increment = getLogValue(formerIndex, latterIndex);
					x = x + increment;				
				}
			}
			pwList.add(x);
		}
		// System.out.println("End");
	}
	
	
}
