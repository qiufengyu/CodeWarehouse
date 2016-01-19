import java.io.*;
import java.util.*;

public class SrcDeal {
	
	Map<Integer, double[]> data;
	
	List<Integer> keys;
	Map<Integer, Integer> response;
	Map<Integer, Integer> predict;
	ArrayList<Integer> predictID;
	int trainSize;
	
	ArrayList<int[]> intData;
	ArrayList<Integer> headerType;
	ArrayList<String> headerName;
	Set<Double> splitPoint;
	ArrayList<Double> splitValue;
	ArrayList<Double> columnVal;
	ArrayList<double[]> rawData;
	ArrayList<Integer> label;
	
	ArrayList<double[]> rawTestData;
	ArrayList<int[]> intTestData;
	
	
	
	public SrcDeal() {
		data = new TreeMap<Integer, double[]>();
		response = new TreeMap<Integer, Integer>();
		predict = new TreeMap<Integer, Integer>();
		predictID = new ArrayList<Integer>();
		intData = new ArrayList<int[]>();
		rawData = new ArrayList<double[]>();
		headerType = new ArrayList<Integer>();
		headerName = new ArrayList<String>();
		splitPoint = new TreeSet<Double>();
		splitValue = new ArrayList<Double>();
		columnVal = new ArrayList<Double>();
		label = new ArrayList<Integer>();
		rawTestData = new ArrayList<double[]>();
		intTestData = new ArrayList<int[]>();
		
	}
	
	// 1,1,1,1,0,1,1,1,0,0,0,0,0,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,,
	public void loadSrc() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("train.csv")));
		String names = br.readLine();
		String[] nSplit = names.split(",");
		int nLen = nSplit.length;
		for(int i = 0; i<nLen; i++) {
			int temp = nSplit[i].length();
			String realName = nSplit[i].substring(1, temp-1);
			headerName.add(realName);
		}
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			int id = Integer.valueOf(parts[0]);
			double[] datas = new double[parts.length-1];
			
			datas[0] = Double.valueOf(parts[1]);
			char x = parts[2].charAt(1);
			char y = parts[2].charAt(2);
			int xval = (x-'A');
			int yval = y-'0';
			// double val = xval+yval;
			datas[1] = xval;
			datas[2] = yval;
			for(int i = 3; i<parts.length-1; i++) {				
				if(parts[i].length()<1) {
					datas[i] = -1.0;
				}
				else {						
					datas[i] = Double.valueOf(parts[i]);
				}			
			}
			data.put(id, datas);
			response.put(id, Integer.valueOf(parts[parts.length-1]));
		}		
		keys = new ArrayList<Integer>(data.keySet());
		trainSize = data.size();
		br.close();
	}
	
	public void firstWrite() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("train2.csv")));
		bw.write("1,1,1,1,0,1,1,1,0,0,0,0,0,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1\n");
		bw.flush();
		for(Map.Entry<Integer, double[]> entry: data.entrySet()) {
			int key = entry.getKey();
			// bw.write(key+",");
			double[] x = entry.getValue();
			for(double y:x) {
				bw.write(y+",");
			}
			int res = response.get(key);
			bw.write(res+"\n");
			bw.flush();
			//break;
		}
		bw.close();
	}

	// load file and from double attribute values to int attributes
	public void loadTrain2() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("train2.csv"));
		String h = br.readLine();
		String[] hSplit = h.split(",");
		int hLen = hSplit.length;
		for(int i = 1; i<=hLen; i++) {
			headerType.add(Integer.valueOf(hSplit[i-1]));
		}
		headerType.add(1);
		int numOfColumns = headerType.size();
		while(true) {
			String line = br.readLine();
			if(line == null)
				break;
			String[] lineSplit = line.split(",");
			double[] temp = new double[numOfColumns];
			int[] temp2 = new int[numOfColumns];
			int len = lineSplit.length;
			for(int i = 0; i<len; i++) {
				double doubleVal = Double.valueOf(lineSplit[i]);
				temp[i] = doubleVal;
			}
			rawData.add(temp);
			intData.add(temp2);
			int labelVal = Integer.valueOf(lineSplit[len-1]);
			label.add(labelVal);
		}
		br.close();
		
		int numOfRows = rawData.size();
		
		for(int k = 0; k<numOfColumns; k++) {
			if(headerType.get(k) == 1) {
				splitValue.add(-2.0);				
			}
			else { // continues value
				splitPoint.clear(); // set
				columnVal.clear();
				for(int i = 0; i<numOfRows; i++) {
					double d = getRawMatrix(i,k);
					columnVal.add(d);
					splitPoint.add(d);
				}
				// the k-th column all possible split values
				Vector<Double> v = new Vector<Double>();
				v.addAll(splitPoint);
				double[] sp = new double[v.size()-1];
				for(int j = 0 ; j<v.size()-1; j++) {
					sp[j] = (v.get(j)+v.get(j+1))/2;
				}
				double[] infoGain = new double[v.size()-1];
				double entropy = getEntropy(label);
				for(int t = 0; t<v.size()-1; t++) {
					int[] zeroscate = new int[8];
					int[] onescate = new int[8];
					int zeros = 0;
					int ones = 0;
					double[] valZero = new double[8];
					double[] valOne = new double[8];
					for(int i = 0; i<8; i++) {
						 zeroscate[i] = 0;
						 onescate[i] = 0;
						 valZero[i] = 0.0;
						 valOne[i] = 0.0;
					}
					for(int l = 0; l<columnVal.size(); l++) {
						if(columnVal.get(l)<=sp[t]) {
							zeros++;
							int indexZ = label.get(l)-1;
							zeroscate[indexZ]++;
						}
						else {
							ones++;
							int indexO = label.get(l)-1;
							onescate[indexO]++;
						}
					}
					double entropyZero=0.0, entropyOne=0.0;
					for(int i = 0; i<8; i++) {
						valZero[i] = (double)zeroscate[i]/(double)zeros;
						valOne[i] = (double)onescate[i]/(double)ones;
						entropyZero += (-valZero[i]*log2(valZero[i]));
						entropyOne += (-valOne[i]*log2(valOne[i]));
					}
					
					double val0 = ((double) zeros)/((double)(zeros+ones)); //for zero
					double val1 = ((double) ones)/((double)(zeros+ones)); //for one
					double gain = entropy - val0*entropyZero-val1*entropyOne;
					infoGain[t] = gain;
					
				}
				
				double pivot = -0.0;
				double maxGain = -10.0;
				for(int m = 0; m<v.size()-1; m++) {
					if(infoGain[m]>maxGain) {
						maxGain = infoGain[m];
						pivot = sp[m];
					}
				}				
				splitValue.add(pivot);
			}
		}
		
		System.out.println(splitValue);
		
		// transfer to integer values
		for(int i = 0; i<numOfRows; i++) {
			for(int j = 0; j<numOfColumns; j++) {
				if(headerType.get(j) == 1) {
					int val = (int)getRawMatrix(i,j);
						setMatrix(val, i, j);
					if(val<0)
						setMatrix(-1, i, j);
				}
				else { // from numerical to discrete
					double mid = splitValue.get(j);
					if(mid == -1.0) {
						System.out.println("??!!!!!");
					}
					double val = getRawMatrix(i,j);
					if(val <= mid ) {
						setMatrix(0, i, j);
					}
					else {
						setMatrix(1, i, j);
					}
				}
			}
		}		
	}
	
	public void genTrainFile() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cfdttrain.csv")));
		for(int[] temp:intData) {
			String line = "";
			for(int t:temp) {
				line = line.concat(t+",");
			}
			String newLine = line.substring(0, line.length()-1);
			bw.write(newLine+"\n");
			bw.flush();
		}
		bw.close();
		File f = new File("train2.csv");
		f.delete();
	}

	public void init() throws IOException {		
		loadSrc();
		firstWrite();
		loadTrain2();
		genTrainFile();
		genTestFile();
	}	
	
	private double getEntropy(ArrayList<Integer> list) {
		int listSize = list.size();
		int[] temp = new int[8];
		for(int i = 0; i<8; i++) {
			temp[i]=0;
		}
		for(int j = 0; j<listSize; j++) {
			int indexVal = list.get(j)-1;
			temp[indexVal]++;
		}
		double[] tempE = new double[8];
		double en = 0.0;
		for(int k = 0; k<8; k++) {
			tempE[k] = (double) temp[k]/(double) listSize;
			en += (-(tempE[k]*log2(tempE[k])));
		}
		return en;
	}
	
	private void setMatrix(int val, int i, int j) {
		intData.get(i)[j] = val;
	}
	
	private double getRawMatrix(int i, int j) {
		double[] temp = rawData.get(i);
		return temp[j];
	}

	private double log2(double d) {
		if(d<=0)
			return 0.0;
		else 
			return (Math.log(d)/Math.log(2));
	}
	
	public ArrayList<Double> getSplit() {
		return splitValue;
	}

	public void genTestFile() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test.csv")));
		br.readLine();
		while(true) {
			String line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			int[] datas = new int[parts.length-1];
			
			datas[0] = Integer.valueOf(parts[1]);
			char x = parts[2].charAt(1);
			char y = parts[2].charAt(2);
			int xval = (x-'A');
			int yval = y-'0';
			// double val = xval+yval;
			datas[1] = xval;
			datas[2] = yval;
			for(int i = 3; i<parts.length-1; i++) {				
				if(parts[i].length()<1) {
					datas[i] = -1;
				}
				else {		
					if(splitValue.get(i)<-1.5) {
						datas[i] = Integer.valueOf(parts[i]);
					}
					else {
						double temp = Double.valueOf(parts[i]);
						double pivot = splitValue.get(i);
						if(temp>pivot)
							datas[i] = 1;
						else
							datas[i] = 0;
					}
				}			
			}
			intTestData.add(datas);	
		}		

		br.close();
		
		// write test3.csv
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cfdttest.csv")));
		for(int[] temp:intTestData) {
			String line = "";
			for(int t:temp) {
				line = line.concat(t+",");
			}
			String newLine = line.substring(0, line.length()-1);
			bw.write(newLine+"\n");
			bw.flush();
		}
		bw.close();
	}

	public void collect() {
		File f1 = new File("cfdttrain.csv");
		if(f1.exists()) {
			f1.delete();
		}
		File f2 = new File("cfdttest.csv");
		if(f2.exists()) {
			f2.delete();
		}
		
	}
	
}
