import java.io.*;
import java.util.*;

public class MatrixData {
	private int columns;
	private int num;
	private ArrayList<String> headers;	
	private ArrayList<int[]> rows;
	private ArrayList<Integer> testID;
	
	private ArrayList<Integer> label;
	
	Set<String> s;
	
	public MatrixData() {
		rows = new ArrayList<int[]>();
		headers = new ArrayList<String>();
		s = new HashSet<String>();
		label = new ArrayList<Integer>();
		testID = new ArrayList<Integer>();
	}
	
	// deal with Training file
	public void loadMatrix(String fileName, int type) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int numOfColumns = 0;
		// fill data
		while(true) {
			String line = br.readLine();
			if(line == null) 
				break;
			String[] lineSplit = line.split(",");
			numOfColumns = lineSplit.length;
			int[] temp = new int[numOfColumns];
			for(int i = 0; i<numOfColumns; i++) {
				int intVal = Integer.valueOf(lineSplit[i]);
				temp[i] = intVal;
			}
			int labelVal = Integer.valueOf(lineSplit[numOfColumns-1]);
			label.add(labelVal);
			rows.add(temp);
		}	
		br.close();
		for(int i = 1; i<numOfColumns; i++) {
			String tempAttr = "attr"+String.valueOf(i);
			headers.add(tempAttr);
		}
		if(type == 1) {
			headers.add("Class");
		}
		else {
			headers.add("attr"+String.valueOf(numOfColumns));
		}
		columns = headers.size();
		num = rows.size();		
	}
	
	public MatrixData splitMatrix(String attrName, int value) {
		MatrixData matrixRet = new MatrixData();
		ArrayList<int[]> rowsMatrixRet = new ArrayList<int[]>();
		ArrayList<String> headersMatrixRet = new ArrayList<String>();
		int attrIndex = 0;
		
		for(String tempHeadValue: headers) {
			if(!tempHeadValue.equals(attrName)) {
				headersMatrixRet.add(tempHeadValue);
			}			
		}
		matrixRet.columns = headersMatrixRet.size();
		matrixRet.setHeaders(headersMatrixRet);
		
		// get the attribute name index
		for(attrIndex = 0; attrIndex<headers.size(); attrIndex++) {
			if(attrName.equals(headers.get(attrIndex)))
				break;
		}
		for(int[] temp : rows) {
			if(temp[attrIndex] == value) {
				int[] tempRow = new int[headersMatrixRet.size()];
				int indexTempRow = 0;
				for(int i = 0; i<columns; i++) {
					if(attrIndex != i) {
						tempRow[indexTempRow++] = temp[i];
					}
				}
				rowsMatrixRet.add(tempRow);
			}
		}
		matrixRet.setRows(rowsMatrixRet);
		matrixRet.num = rowsMatrixRet.size();
		return matrixRet;
	}
	
	public void getTest(TreeNode root) throws IOException {
		
		System.out.println("Classifying and predicting...");

		ArrayList<Integer> resultList = new ArrayList<Integer>();
		CFDriver cfd = new CFDriver();
		for (int[] temp : rows) {
			HashMap<String, Integer> testValues = new HashMap<String, Integer>();
			for (int i = 0; i < columns; i++) {				
				testValues.put((String) headers.get(i), temp[i]);
			}
			int flag = root.classifyTest(testValues, root);
			resultList.add(flag);
		}
		ArrayList<Integer> result2 = cfd.run();
		testID = cfd.getIDList();
		int countByCF = 0;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cfdt.csv")));
		bw.write("\"Id\",\"Response\"\n");
		for(int i = 0; i<result2.size(); i++) {
			int val = resultList.get(i)>0?resultList.get(i):result2.get(i);
			if(resultList.get(i)<0) {
				countByCF++;
			}
			// System.out.print(resultList.get(i)+"\t");
			bw.write(testID.get(i)+","+String.valueOf(val)+"\n");
			bw.flush();
		}
		bw.close();
		double ratio = (double)countByCF/(double)resultList.size()*100;
		// System.out.println(countByCF);
		System.out.println("Decided by CF: "+String.format("%.4f", ratio)+"%");
		
	}
	
	public void fillArray(int[] data, int index) {
		int arrayIndex = 0;
		for(int[]temp : rows) {
			data[arrayIndex++] = temp[index];
		}
	}
	
	public double getEntropy(ArrayList<Integer> list) {
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
	
	private double log2(double d) {
		if(d<=0)
			return 0.0;
		else 
			return (Math.log(d)/Math.log(2));
	}
	

	public ArrayList<String> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}

	public ArrayList<int[]> getRows() {
		return rows;
	}

	public void setRows(ArrayList<int[]> rows) {
		this.rows = rows;
	}
	
	public int getColumns() {
		return columns;
	}

	public int getNum() {
		return num;
	}
	
	public void printMatrix() {
		// print header
		for (String temp : headers) {
			System.out.print("\t" + temp);
		}
		for (int[] temp : rows) {
			System.out.println("");
			for (int i = 0; i < columns; i++) {
				System.out.print("\t" + temp[i]);
			}
		}
		System.out.println("");
	}
	
}
