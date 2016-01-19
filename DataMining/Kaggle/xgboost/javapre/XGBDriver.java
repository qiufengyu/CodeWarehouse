import java.io.*;
import java.util.ArrayList;

public class XGBDriver {
	ArrayList<double[]> data;
	ArrayList<Integer> headerType;
	ArrayList<Integer> testIds;

	public XGBDriver() {
		data = new ArrayList<double[]>();	
		headerType = new ArrayList<Integer>();
		String headers = "1,1,1,1,0,1,1,1,0,0,0,0,0,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1";
		String[] p = headers.split(",");
		for(String x: p) {
			int y = Integer.valueOf(x);
			headerType.add(y);
		}
		testIds = new ArrayList<Integer>();
	}
	
	public void genTrainData() throws IOException {
		data.clear();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("train.csv")));
		String line = br.readLine();
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			double[] datas = new double[parts.length];
			for(int i = 0; i<parts.length; i++) {
				if( i == 2 ) {
					char x = parts[2].charAt(1);
					char y = parts[2].charAt(2);
					int xval = 10*(x-'A');
					int yval = y-'0';
					double val = xval+yval;
					datas[2] = val;
				}
				else {
					if(parts[i].length()<1) {
						datas[i] = -999.0;
					}
					else {						
						datas[i] = Double.valueOf(parts[i]);
					}
				}
			}
			data.add(datas);
		}		
		br.close();
		
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("trainsvm.txt")));
		for(double[] temp : data) {
			int len = temp.length;
			bw.write((int)(temp[len-1]-1)+" ");
			for(int i = 1; i<len-2; i++) {
				if(temp[i]>=-0.1) {
					if(headerType.get(i) == 0)
						bw.write((int)(i-1)+":"+temp[i]+" ");
					else
						bw.write((int)(i-1)+":"+(int)temp[i]+" ");
				}
			}
			// write len-1
			if(temp[len-2]>=-0.1) {
				if(headerType.get(len-2) == 0)
					bw.write((int)(len-3)+":"+temp[len-2]);
				else
					bw.write((int)(len-3)+":"+(int)temp[len-2]);
			}
			bw.write("\n");
			bw.flush();
			
		}
		bw.close();
	}
	
	public void genTestData() throws IOException {
		data.clear();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("test.csv")));
		String line = br.readLine();
		while(true) {
			line = br.readLine();
			if(line == null) {
				break;
			}
			String[] parts = line.split(",");
			double[] datas = new double[parts.length+1];
			for(int i = 0; i<parts.length; i++) {
				if( i == 2 ) {
					char x = parts[2].charAt(1);
					char y = parts[2].charAt(2);
					int xval = 10*(x-'A');
					int yval = y-'0';
					double val = xval+yval;
					datas[2] = val;
				}
				else {
					if(parts[i].length()<1) {
						datas[i] = -999.0;
					}
					else {						
						datas[i] = Double.valueOf(parts[i]);
					}
				}
			}
			datas[parts.length] = 8.0;
			data.add(datas);
		}		
		br.close();
		
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testsvm.txt")));
		for(double[] temp : data) {
			int len = temp.length;
			bw.write((int)(temp[len-1]-1)+" ");
			for(int i = 1; i<len-2; i++) {
				if(temp[i]>=-0.1) {
					if(headerType.get(i) == 0)
						bw.write((int)(i-1)+":"+temp[i]+" ");
					else
						bw.write((int)(i-1)+":"+(int)temp[i]+" ");
				}
			}
			// write len-1
			if(temp[len-2]>=-0.1) {
				if(headerType.get(len-2) == 0)
					bw.write((int)(len-3)+":"+temp[len-2]);
				else
					bw.write((int)(len-3)+":"+(int)temp[len-2]);
			}
			bw.write("\n");
			bw.flush();
			
		}
		bw.close();
		
		for(double[] temp2:data) {
			testIds.add((int)temp2[0]);
		}
		
		
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testid.txt")));
		for(double[] temp2:data) {
			bw2.write((int)temp2[0]+"\n");
			bw2.flush();
		}
		bw2.close();
		
		
		
	}

	public ArrayList<Integer> getTestIds() {
		return testIds;
	}
	
	
	
	
}
