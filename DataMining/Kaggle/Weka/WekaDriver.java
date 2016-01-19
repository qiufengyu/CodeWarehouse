import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class WekaDriver {
	
	ArrayList<String> headerName;
	ArrayList<Integer> headerType;
	ArrayList<String[]> rawData;
	ArrayList<String[]> trainRawData;
	ArrayList<String[]> testRawData;	
	
	public WekaDriver() {
		headerName = new ArrayList<String>();
		headerType = new ArrayList<Integer>();
		rawData = new ArrayList<String[]>();
		trainRawData = new ArrayList<String[]>();
		testRawData = new ArrayList<String[]>();
		String type = "3,1,1,1,0,1,1,1,0,0,0,0,0,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,0,0,0,0,2,1,1,1,1,1,1,1,1,2,1,1,1,1,2,1,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1";
		String[] typeSplit = type.split(",");
		for(String temp: typeSplit) {
			headerType.add(Integer.valueOf(temp));
		}
		// System.out.println(headerType.size());
		
	}
	
	public void genArffFile() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("train.csv")));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("test.csv")));
		String line = br.readLine();
		br2.readLine();
		String[] lineSplit = line.split(",");
		for(String temp: lineSplit) {
			temp = temp.substring(1,temp.length()-1);
			headerName.add(temp);
		}
		// System.out.println(lineSplit.length);
		
		while(true) {
			String l = br.readLine();
			if(l == null)
				break;
			String[] lsplit = l.split(",");
			String[] newString = new String[lsplit.length];
			for(int x = 0; x<lsplit.length; x++) {
				if(lsplit[x].length()<1)
					newString[x]="?";
				else {
					if(x == 2) {
						newString[x] = lsplit[x].substring(1, lsplit[x].length()-1);
					}
					else {
						newString[x]=lsplit[x];
					}
				}
					
			}
			rawData.add(newString);
			trainRawData.add(newString);
		}
		
		while(true) {
			String l = br2.readLine();
			if(l == null)
				break;
			String[] lsplit = l.split(",");
			String[] newString = new String[lsplit.length];
			for(int x = 0; x<lsplit.length; x++) {
				if(lsplit[x].length()<1)
					newString[x]="?";
				else {
					if(x == 2) {
						newString[x] = lsplit[x].substring(1, lsplit[x].length()-1);
					}
					else {
						newString[x]=lsplit[x];
					}
				}
					
			}
			rawData.add(newString);
			testRawData.add(newString);
		}	
		
		
		int size = rawData.size();
		
		BufferedWriter bwtrain = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("trainweka.arff")));
		BufferedWriter bwtest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testweka.arff")));
		bwtrain.write("@relation train\n\n");
		bwtest.write("@relation test\n\n");
		// @attributes
		for(int i = 0; i<lineSplit.length; i++) {
			if(i == 2) {
				TreeSet<String> s = new TreeSet<String>();
				for(int j = 0; j<size; j++) {
					s.add(getRawString(j,i));
				}
				bwtrain.write("@attribute "+headerName.get(i)+" {");
				bwtest.write("@attribute "+headerName.get(i)+" {");
				Vector<String> v = new Vector<String>();
				v.addAll(s);
				int smallSize = s.size();
				for(int k = 0; k<smallSize-1; k++) {
					bwtrain.write(v.get(k)+",");
					bwtest.write(v.get(k)+",");
				}
				bwtrain.write(v.get(smallSize-1)+"}\n");
				bwtest.write(v.get(smallSize-1)+"}\n");
			}
			else {
				int type = headerType.get(i);
				if(type == 0) {
					bwtrain.write("@attribute "+headerName.get(i)+" numeric\n");
					bwtest.write("@attribute "+headerName.get(i)+" numeric\n");
				}
				
				else if(type == 3) {
					bwtrain.write("@attribute "+headerName.get(i)+" integer\n");
					bwtest.write("@attribute "+headerName.get(i)+" integer\n");
				}
				
				else {
					// type == 1
					TreeSet<Integer> s = new TreeSet<Integer>();
					for(int j = 0; j<size; j++) {
						int tempx = (int)getRawXY(j,i);
						if(tempx<0) {
							
						}
						else {
							s.add(tempx);
						}
					}
					Vector<Integer> v = new Vector<Integer>();
					v.addAll(s);
					int y = v.get(0);
					int z = v.lastElement();
					for(int t = y; t<=z; t++) {
						if(t == y) {
							bwtrain.write("@attribute "+headerName.get(i));
							bwtrain.write(" {");
							bwtest.write("@attribute "+headerName.get(i));
							bwtest.write(" {");
						}
						bwtrain.write(String.valueOf(t));
						bwtest.write(String.valueOf(t));
						if(t<z) {
							bwtrain.write(",");
							bwtest.write(",");
						}
						else {
							bwtrain.write("}\n");
							bwtest.write("}\n");
						}
					}
				}				
			}
			bwtrain.flush();
			bwtest.flush();
		}
		
		bwtrain.write("\n@data\n");
		bwtest.write("\n@data\n");
		for(String[] tempList: trainRawData) {
			for(int i = 0; i<tempList.length-1; i++) {
				bwtrain.write(tempList[i]+",");
			}
			bwtrain.write(tempList[tempList.length-1]+"\n");
			bwtrain.flush();
		}	
		
		for(String[] tempList2: testRawData) {
			for(int i = 0; i<tempList2.length; i++) {
				bwtest.write(tempList2[i]+",");
			}
			bwtest.write("8\n");
			bwtest.flush();
		}
		
		bwtrain.close();
		bwtest.close();
		br.close();
		br2.close();
	}
	
	static public void getWekaResult() throws IOException {
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream("test.csv")));
		BufferedReader br2= new BufferedReader(new InputStreamReader(new FileInputStream("wekain.txt")));
		br1.readLine();
		br2.readLine();
		TreeMap<Integer, Integer> predict = new TreeMap<Integer, Integer>();
		while(true) {
			String line = br1.readLine();
			String line2 = br2.readLine();
			if(line == null)
				break;
			String[] lineSplit = line.split(",");
			int id = Integer.valueOf(lineSplit[0]);	
			String[] line2Split = line2.split(":");
			char x = line2Split[2].charAt(0);
			int y = x-'0';
			predict.put(id, y);			
		}
		
		br1.close();
		br2.close();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("resultg.csv")));
		bw.write("\"Id\",\"Response\"\n");
		for(Map.Entry<Integer, Integer> entry: predict.entrySet()) {			
			bw.write(entry.getKey()+","+entry.getValue()+"\n");;
			bw.flush();
		}
		bw.close();
	}
	
	private int getRawXY(int x, int y) {
		String[] temp = rawData.get(x);
		if(y>=temp.length) {
			return -1;
		}
		if(temp[y].equals("?") || temp[y].length()<1) {
			return -1;
		}
		return Integer.valueOf(temp[y]);
	}
	
	private String getRawString(int x, int y) {
		String temp = rawData.get(x)[y];
		return temp;
	}
}
