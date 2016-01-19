import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class Model {
	
	int maxLen;
	double threshold;
	int[][] count;
	int[] start;
	Map<String, Integer> tableMap;
	Set<String> mySet;
	String type;
	
	public Model(String type) {
		maxLen = 5;
		threshold = -10.0;
		tableMap= new TreeMap<String, Integer>();
		mySet = new HashSet<String>();
		this.type = type;		
	}
	
	public void train() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(type+".txt")));
		TreeSet<String> table = new TreeSet<String>();
		
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		while(true) {
			ArrayList<String> temp = new ArrayList<String>();
			String line = br.readLine();
			if(line == null)
				break;
			String[] parts = line.split(" ");
			if(parts.length>=2) {
				for(int i = 1; i<parts.length; i++) {
					temp.add(parts[i]); // serial strings
					table.add(parts[i]);
				}
			}
			maxLen = Math.max(maxLen, parts.length-1);
			list.add(temp);
		}
		br.close();
		
		Vector<String> v = new Vector<String>();
		v.addAll(table);
		for(int i = 0; i<table.size(); i++) {
			tableMap.put(v.get(i), i);
		}
		
		int sz = table.size();
		threshold = ((double) 1)/((double) sz);
		
		count = new int[sz][sz];
		start = new int[sz];
		for(int i = 0; i<sz; i++) {
			for(int j = 0; j<sz; j++) {
				count[i][j] = 0;
			}
			start[i] = 0;
		}
		
		for(ArrayList<String> l : list) {
			int tsize = l.size();
			for(int i = 0; i<tsize-1; i++) {
				int firstIndex = tableMap.get(l.get(i));
				int secondIndex = tableMap.get(l.get(i+1));
				count[firstIndex][secondIndex]++;
			}
			int index = tableMap.get(l.get(0));
			start[index]++;
		}
		
		/*
		for(int i = 0; i<sz; i++) {
			for(int j = 0; j<sz; j++) {
				System.out.print(count[i][j]+" ");
			}
			System.out.println();
		}
		
		for(int i = 0; i<sz; i++) {
			System.out.print(start[i]+" ");
		}
		*/
	}

	
	public double getNextP(int a, int b) {
		int t = 0;
		for(int i = 0; i<count[a].length; i++) {
			t += count[a][b];
		}
		t += count[a].length;
		return ((double) (1+count[a][b]))/((double)t);
	}
	
	public double getStartP(int a) {
		int t = 0;
		for(int i = 0; i<start.length; i++) {
			t += start[i];
		}
		t += start.length;
		return ((double)(start[a]+1))/((double)t);
	}
	
	public void test(int s, int e) throws IOException {
		if(s>=e)
			return;
		String filePath = ".\\file\\";
		for(int i = s+1; i<=e; i++) {
			ArrayList<String> wordList = new ArrayList<String>();
			ArrayList<String> speechList = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath+String.valueOf(i)+".txt")));
			while(true) {
				String line = br.readLine();
				if(line == null)
					break;
				String[] parts = line.split(" ");
				if(parts.length>=2) {
					wordList.add(parts[0]);
					speechList.add(parts[1]);
				}
			}
			br.close();
			
			// to get the p!
			int sz = speechList.size();
			HashSet<String> set = new HashSet<String>();
			set.addAll(tableMap.keySet());
			for(int j = 0; j<sz; j++) {		
				String sp = speechList.get(j);
				if(set.contains(sp)) {
					double p = Math.log(getStartP(tableMap.get(sp)));
					int k = 0;
					for(k = 0; k<maxLen-1; k++) {
						if(j+k+1>=sz)
							break;
						String former = speechList.get(j+k);
						String latter = speechList.get(j+k+1);
						if(tableMap.get(former) == null)
							break;
						if(tableMap.get(latter) == null)
							break;						
						int formerIndex = tableMap.get(former);
						int latterIndex = tableMap.get(latter);						
						p += Math.log(getNextP(formerIndex, latterIndex));
						String result = new String("");
						double param = -10.0;
						if(type.equals("product_name"))
							param = -13.0;
						if(p > param) {
							if(k > 0) {
								for(int x = j; x<j+k; x++) {
									result += (wordList.get(x));
								}
							}
							// System.out.println(p);
						}
						if (result.length()>0) {
							// System.out.println(result);
							mySet.add(result);
							
						}
					}
					
				}
			}
		}
	}

	public void getAccuracy(int p) throws IOException {
		HashSet<String> set = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".\\target\\"+type+".txt")));
		while(true) {
			String line = br.readLine();
			if(line == null)
				break;
			if(line.length() >=1) {
				set.add(line);
			}
		}
		
		int correct1 = 0;
		int correct2 = 0;
		Vector<String> v = new Vector<String>();
		v.addAll(set);
		Vector<String> myVector = new Vector<String>();
		myVector.addAll(mySet);
		// System.out.println(mySet.size());
		for(int i = 0; i<myVector.size(); i++) {
			String my = myVector.get(i);
			for(int j = 0; j<v.size(); j++) {
				String x = v.get(j);
				int index = x.indexOf(my);
				if(index>=0) {
					correct1++;
					j = v.size(); // break;
				}
			}
		}
		for(int j = 0; j<v.size(); j++) {
			String ground = v.get(j);
			for(int i = 0; i<myVector.size(); i++) {
				String x = myVector.get(i);
				int index = ground.indexOf(x);
				if(index>=0) {
					correct2++;
					break;
				}
			}
		}
		System.out.println("test "+ (p+1));
		double val1 = (double)correct1/myVector.size();
		double val2 = (double)correct2/v.size();
		System.out.println((double)correct1/myVector.size()); // in my prediction
		System.out.println((double)correct2/v.size()); // compared to ground
		double f1 = 2*val1*val2/(val1+val2);
		System.out.println("F1 = "+f1);
		br.close();
	}
}
