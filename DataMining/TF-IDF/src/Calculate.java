import java.io.*;
import java.util.*;

public class Calculate {
	
	double[][] tf;
	double[] idf;
	double[][] result;
	int allWordCount;
	int lines;
	Vector<String> v;
	
	FileLoad fileLoad;
	
	Set<String> set;
	
	public Calculate() {
		
	}
	
	public Calculate(Set<String>wordSet) throws IOException {
		set = new TreeSet<String>();
		v = new Vector<String>();
		fileLoad = new FileLoad("Basketball.txt");
		set = wordSet;
		v.addAll(set);
		tf = new double[fileLoad.getAllWordCount()][fileLoad.getLines()];
		idf = new double[fileLoad.getAllWordCount()];
		result = new double[fileLoad.getAllWordCount()][fileLoad.getLines()];
		allWordCount = fileLoad.getAllWordCount();
		lines = fileLoad.getLines();
	}
	
	public Calculate(String fileName, Set<String>wordSet) throws IOException {
		set = new TreeSet<String>();
		v = new Vector<String>();
		fileLoad = new FileLoad(fileName);
		set = wordSet;
		v.addAll(set);
		allWordCount = wordSet.size();
		lines = fileLoad.getLines();
		tf = new double[allWordCount][lines];
		idf = new double[allWordCount];
		result = new double[allWordCount][lines];
		
	}
	
	private void calculateTF() {
		// 计算TF
		for(int i = 0; i<allWordCount; i++) {
			String s = v.get(i);
			for(int j = 0; j<lines; j++) {
				tf[i][j]=(double)fileLoad.getN_ij(s, j)/(double)fileLoad.getWordCountByLine(j);
			}
		}
	}
	
	private void calculateIDF() {
		// 计算IDF
		for(int i = 0; i<allWordCount; i++) {
			String s = v.get(i);
			// System.out.println(lines);
			idf[i] = Math.log10((double)lines)-Math.log10((double)fileLoad.getContainWordCount(s));
		}
	}	
	
	public void calculateTF_IDF() {
		// // 计算TF-IDF
		calculateTF();
		calculateIDF();
		for(int i = 0; i<allWordCount; i++) {
			for(int j = 0; j<lines; j++) {
				result[i][j]=tf[i][j]*idf[i];
			}
		}
	}
	
	public void writeFiles(String fileName) throws IOException {
		// 结果输出
		File file = new File(".\\result\\"+fileName);
		if(!file.getParentFile().exists()) {
			// if file doesn't exist, create it!
			file.getParentFile().mkdirs();
		}
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\result\\"+fileName),"UTF-8"));
		for(int j = 0; j<lines; j++) {
			// bw.write("Line "+ (j+1) +": ");
			for(int i = 0; i<allWordCount; i++) {
				/*
				if(result[i][j]>0) {
					bw.write(String.format("%.4f", result[i][j])+" ");
				}
				else {
					bw.write("0 ");
				}
				*/
				if(result[i][j]>0) {
					// index+1 = location
					bw.write((i+1)+":"+String.format("%.4f", result[i][j])+" ");
				}
			}
			bw.write("\n");
			bw.flush();
		}
		bw.close();
	}

}
