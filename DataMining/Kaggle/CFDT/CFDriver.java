import java.io.*;
import java.util.*;
public class CFDriver {
	
	Map<Integer, double[]> data;
	List<Integer> keys;
	Map<Integer, Integer> response;
	Map<Integer, Integer> predict;
	Random random;
	int trainSize;
	
	public CFDriver() {
		data = new TreeMap<Integer, double[]>();	
		response = new TreeMap<Integer, Integer>();
		predict = new TreeMap<Integer, Integer>();
		random = new Random();
		
	}
	
	public void readData() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("train.csv")));
		String line = br.readLine();
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			int id = Integer.valueOf(parts[0]);
			double[] datas = new double[parts.length-2];
			for(int i = 1; i<parts.length-1; i++) {
				if( i == 2 ) {
					char x = parts[2].charAt(1);
					char y = parts[2].charAt(2);
					int xval = 10*(x-'A');
					int yval = y-'0';
					double val = xval+yval;
					datas[1] = val;
				}
				else {
					if(parts[i].length()<1) {
						datas[i-1] = 0.0;
					}
					else {						
						datas[i-1] = Double.valueOf(parts[i]);
					}
				}
			}
			data.put(id, datas);
			response.put(id, Integer.valueOf(parts[parts.length-1]));
		}		
		keys = new ArrayList<Integer>(data.keySet());
		trainSize = data.size();
		br.close();
	}
	
	public ArrayList<Integer> run() throws IOException {
		ArrayList<Integer> result2 = new ArrayList<Integer>();
		predict.clear();
		File f = new File("CFresult.csv");
		if(f.exists()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			br.readLine();
			while(true) {
				String line = br.readLine();
				if(line == null) 
					break;
				String[] parts = line.split(",");
				int ids = Integer.valueOf(parts[0]);
				int label = Integer.valueOf(parts[1]);
				predict.put(ids, label);
				result2.add(label);
			}
			br.close();
		}
		else {
			readData();
			predictData();	
			showResults();
			for(Map.Entry<Integer, Integer> entry:predict.entrySet()) {
				result2.add(entry.getValue());
			}
		}
		return result2;
	}
	

	public double getCosine(double[] a, double[] b) {
		if(a.length!=b.length) 
			return -1.0;
		else {
			int len = a.length;
			double prodSum = 0.0;
			double aSquare = 0.0;
			double bSquare = 0.0;
			for(int i = 0; i<len; i++) {
				prodSum += a[i]*b[i];
				aSquare += a[i]*a[i];
				bSquare += b[i]*b[i];
			}
			return (prodSum)/(Math.sqrt(aSquare*bSquare));
		}
	}
	
	public void predictData() throws IOException {
		System.out.println("Collaborative Filtering Predicting...");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test.csv")));
		String line = br.readLine();
		int lineCount = 0;
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			int id = Integer.valueOf(parts[0]);
			double[] datas = new double[parts.length-1];
			for(int i = 1; i<parts.length-1; i++) {
				if( i == 2 ) {
					char x = parts[2].charAt(1);
					char y = parts[2].charAt(2);
					int xval = 10*(x-'A');
					int yval = y-'0';
					double val = xval+yval;
					datas[1] = val;
				}
				else {
					if(parts[i].length()<1) {
						datas[i-1] = 0.0;
					}
					else {						
						datas[i-1] = Double.valueOf(parts[i]);
					}
				}
			}
			int maxID = -1;
			double maxCosine = -1.0;
			/*
			for(int i = 0; i<60000; i++) {
				int index = random.nextInt(trainSize);
				int indexID = keys.get(index);
				double[] temp = data.get(indexID);
				double tempCosine = getCosine(temp, datas);
				if(maxCosine < tempCosine) {
					maxCosine = tempCosine;
					maxID = indexID;
				}	
				// System.out.println(lineCount++);
			}
			*/
			for(Map.Entry<Integer, double[]> entry : data.entrySet()) {
				double[] temp = (double[])entry.getValue();
				double tempCosine = getCosine(temp, datas);
				if(maxCosine < tempCosine) {
					maxCosine = tempCosine;
					maxID = entry.getKey();
				}	
			}			
			predict.put(id, response.get(maxID));
			lineCount++;
			if(lineCount%500 == 0) {
			 System.out.println(lineCount);
			}
		}
		br.close();
		System.out.println("Collaborative Filtering Finished...");
	}
	
	public void showResults() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("CFresult.csv")));
		bw.write("\"ID\",\"Response\"\n");
		for(Map.Entry<Integer, Integer> entry:predict.entrySet()) {
			bw.write(entry.getKey()+","+entry.getValue()+"\n");
			bw.flush();
		}
		bw.close();
	}
	
	public ArrayList<Integer> getIDList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.addAll(predict.keySet());
		return list;
	}
	

}
