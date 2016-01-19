import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

public class Main {
	public static void main(String[] args) throws IOException {		

		File path = new File(".\\results");
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
		        return name.endsWith(".csv");
		      }
		};
		File[] files = path.listFiles(filter);
		int fileNum = files.length;
		
		int[] weights = new int[fileNum];
		for(int i = 0; i<files.length; i++) {
			String temp = files[i].getName();
			String sub = temp.substring(0,5);
			weights[i] = Integer.valueOf(sub);
		}
		
		BufferedReader[] ensembleReaders = new BufferedReader[fileNum];
		for(int i = 0; i<fileNum; i++) {
			ensembleReaders[i] = new BufferedReader(new InputStreamReader(new FileInputStream(files[i])));
			ensembleReaders[i].readLine();
		}
		
		BufferedReader idReader = new BufferedReader(new InputStreamReader(new FileInputStream(".\\results\\testid.txt")));
		
		TreeMap<Integer, int[]> map = new TreeMap<Integer, int[]>();
		while(true) {
			String idIndex = idReader.readLine();
			if(idIndex == null)
				break;
			int[] responseList = new int[8];
			for(int k = 0; k<8; k++) {
				responseList[k] = 0;
			}
			int id = Integer.valueOf(idIndex);
			for(int i = 0; i<fileNum; i++) {
				String x = ensembleReaders[i].readLine();
				String[] parts = x.split(",");
				int weightIndex = Integer.valueOf(parts[1])-1;
				responseList[weightIndex] += weights[i];
			}
			map.put(id, responseList);
		}
		
		BufferedWriter ensembleWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ensemble.csv")));
		
		ensembleWriter.write("\"Id\",\"Response\"\n");
		
		for(Entry<Integer, int[]> entry:map.entrySet()) {
			int id = entry.getKey();
			int[] z = entry.getValue();
			int response = getMax(z);
			ensembleWriter.write(id+","+response+"\n");
			ensembleWriter.flush();
		}
		ensembleWriter.close();
		idReader.close();
		for(int i = 0; i<fileNum; i++) {
			ensembleReaders[i].close();
		}
		
	}
	
	static int getMax(int[] x) {
		int maxValue = -1;
		int maxIndex = 0;
		for(int i = 0; i<x.length; i++) {
			if(x[i]>maxValue) {
				maxValue = x[i];
				maxIndex = i;
			}
		}
		return maxIndex+1;
	}
}
