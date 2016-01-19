import java.io.*;
import java.util.*;

public class MatrixData {
	private int columns;
	private int num;
	private ArrayList<String> headers;	
	private ArrayList<int[]> rows;
	
	
	private ArrayList<double[]> rawData;
	private ArrayList<Integer> headerType;

	private ArrayList<Integer> label;
	private Set<Double> splitPoint;
	private ArrayList<Double> splitValue;
	private ArrayList<Double> columnVal;
	
	private Set<String> s;
	
	public MatrixData() {
		rows = new ArrayList<int[]>();
		rawData = new ArrayList<double[]>();
		headers = new ArrayList<String>();
		headerType = new ArrayList<Integer>();
		s = new HashSet<String>();
		label = new ArrayList<Integer>();
		
		splitPoint = new TreeSet<Double>();
		columnVal = new ArrayList<Double>();
		splitValue = new ArrayList<Double>();
	}
	
	// deal with 
	public void loadMatrix(String fileName, int start, int end, int flag) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int numOfColumns = 0;
		String h = br.readLine();
		String[] hSplit = h.split(",");
		int hLen = hSplit.length;
		for(int i = 1; i<=hLen; i++) {
			headerType.add(Integer.valueOf(hSplit[i-1]));
			String tempAttr = "attr"+String.valueOf(i);
			headers.add(tempAttr);
		}
		headers.add("Class");
		headerType.add(1);
		
		numOfColumns = headers.size();
		columns = numOfColumns;
		int lineCount = 0;
		// fill raw data
		while(true) {
			String line = br.readLine();
			if(line == null) 
				break;
			String[] lineSplit = line.split(",");
			double[] temp = new double[numOfColumns];
			int[] temp2 = new int[numOfColumns];
			String unique = "";
			int len = lineSplit.length;
			for(int i = 0; i<len; i++) {
				double doubleVal = Double.valueOf(lineSplit[i]);
				if(doubleVal < 0.0) 
					temp[i] = 0.0;
				else 
					temp[i] = doubleVal;
				if(i<len-1)
					unique = unique.concat(lineSplit[i]);
			}
			double labelVal = Double.valueOf(lineSplit[len-1]);
			
			// if(!s.contains(unique)) {
				if(lineCount<start || lineCount>=end) {
				rawData.add(temp);
				rows.add(temp2);
				s.add(unique);
				if(labelVal < 0.5)
					label.add(0);
				else
					label.add(1);
				}
			// }	
			lineCount++;
		}		
		br.close();
		/*
		int rowsDeleted = percentage*s.size()/100;
		if(rowsDeleted == s.size()) {
			// do nothing
		}
		else {
			for(int i = s.size() - 1; i>rowsDeleted; i--) {
				rawData.remove(i);
				rows.remove(i);
			}
		}
		*/
		num = rawData.size();
		
		// get all values
		for(int k = 0; k<columns&&headerType.get(k)==0; k++) {
			splitPoint.clear();
			columnVal.clear();
			for(int i = 0; i<num; i++) {
				double d = getRawMatrix(i,k);
				columnVal.add(d);
				splitPoint.add(d);
			}
			// the k-th column all split values
			Vector<Double> v = new Vector<Double>();
			v.addAll(splitPoint);
			double[] sp = new double[v.size()-1];
			for(int j = 0 ; j<v.size()-1; j++) {
				sp[j] = (v.get(j)+v.get(j+1))/2;
			}
			double[] infoGain = new double[v.size()-1];
			double entropy = getEntropy(label);
			for(int t = 0; t<v.size()-1; t++) {
				int zeros = 0;
				int zerosPos = 0;
				int zerosNeg = 0;
				int ones = 0;
				int onesPos = 0;
				int onesNeg = 0;
				for(int i = 0; i<columnVal.size(); i++) {
					if(columnVal.get(i) <= sp[t]) {
						zeros++;
						if(label.get(i) == 0) {
							zerosPos++;
						}
						else {
							zerosNeg++;
						}
					}
					else {
						ones++;
						if(label.get(i) == 0) {
							onesPos++;
						}
						else {
							onesNeg++;
						}
					}
				}
				double val1 = ((double)onesPos)/((double)ones);
				double val2 = ((double)onesNeg)/((double)ones);
				double val3 = ((double)zerosPos)/((double)zeros);
				double val4 = ((double)zerosNeg)/((double)zeros);
				double entropy1 = -val1*log2(val1)-val2*log2(val2);
				double entropy2 = -val3*log2(val3)-val4*log2(val4);
				double val5 = ((double)ones)/((double)(ones+zeros));
				double val6 = ((double)zeros)/((double)(ones+zeros));
				double gain = entropy-val5*entropy1-val6*entropy2;				
				infoGain[t] = gain;
			}
			
			double pivotVal = -0.0;
			double maxGain = -1.0;
			for(int t = 0; t<v.size()-1; t++) {
				if(infoGain[t]>maxGain) {
					maxGain = infoGain[t];
					pivotVal = sp[t];
				}
			}
			
			splitValue.add(pivotVal);
		}
		
		if(flag == 1 && splitValue.size()>0) {
			System.out.println("Split Values : if less then = 0, else = 1");
			System.out.println(splitValue);				
		}
		
		// move to all discrete values matrix data
		for(int i = 0; i<num; i++) {
			for(int j = 0; j<columns; j++) {
				if(headerType.get(j) == 1) {
					int val = (int)getRawMatrix(i,j);
					setMatrix(val, i, j);
					if(val<0)
						setMatrix(0, i, j);
				}
				else { // from numerical to discrete
					double mid = splitValue.get(j);
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
		// printMatrix();
		
	}
	
	public void loadTestMatrix(String fileName, int start, int end, int flag, ArrayList<Double> splitValue) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int numOfColumns = 0;
		String h = br.readLine();
		String[] hSplit = h.split(",");
		int hLen = hSplit.length;
		for(int i = 1; i<=hLen; i++) {
			headerType.add(Integer.valueOf(hSplit[i-1]));
			String tempAttr = "attr"+String.valueOf(i);
			headers.add(tempAttr);
		}
		headers.add("Class");
		headerType.add(1);
		
		numOfColumns = headers.size();
		columns = numOfColumns;
		int lineCount = 0;
		// fill raw data
		while(true) {
			String line = br.readLine();
			if(line == null) 
				break;
			String[] lineSplit = line.split(",");
			double[] temp = new double[numOfColumns];
			int[] temp2 = new int[numOfColumns];
			String unique = "";
			int len = lineSplit.length;
			for(int i = 0; i<len; i++) {
				double doubleVal = Double.valueOf(lineSplit[i]);
				if(doubleVal < 0.0) 
					temp[i] = 0.0;
				else 
					temp[i] = doubleVal;
				if(i<len-1)
					unique = unique.concat(lineSplit[i]);
			}
			double labelVal = Double.valueOf(lineSplit[len-1]);
			
			// if(!s.contains(unique)) {
				if(lineCount>=start && lineCount<end) {
				rawData.add(temp);
				rows.add(temp2);
				s.add(unique);
				if(labelVal < 0.5)
					label.add(0);
				else
					label.add(1);
				}
			// }	
			lineCount++;
		}		
		br.close();
		/*
		int rowsDeleted = percentage*s.size()/100;
		if(rowsDeleted == s.size()) {
			// do nothing
		}
		else {
			for(int i = s.size() - 1; i>rowsDeleted; i--) {
				rawData.remove(i);
				rows.remove(i);
			}
		}
		*/
		num = rawData.size();
		
		if(flag == 1 && splitValue.size()>0) {
			System.out.println("Split Values : if less then = 0, else = 1");
			System.out.println(splitValue);				
		}
		
		// move to all discrete values matrix data
		for(int i = 0; i<num; i++) {
			for(int j = 0; j<columns; j++) {
				if(headerType.get(j) == 1) {
					int val = (int)getRawMatrix(i,j);
					setMatrix(val, i, j);
					if(val<0)
						setMatrix(0, i, j);
				}
				else { // from numerical to discrete
					double mid = splitValue.get(j);
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

	private void setMatrix(int val, int i, int j) {
		rows.get(i)[j] = val;
	}
	
	private double getRawMatrix(int i, int j) {
		double[] temp = rawData.get(i);
		return temp[j];
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
	
	public double getTestAccuracy(TreeNode root, String typeOfData) {
		double accuracy = 0.0;
		int countPositives = 0;
		for (int[] temp : rows) {
			HashMap<String, Integer> testValues = new HashMap<String, Integer>();
			int finalValue = -1;
			for (int i = 0; i < columns; i++) {
				if (i == (columns - 1)) {
					finalValue = temp[i];
				} else {
					testValues.put((String) headers.get(i), temp[i]);
				}
			}
			if (finalValue == root.classifyTest(testValues, root)) {
				countPositives++;
			}
		}
		// System.out.println("Test Positives"+countPositives);
		accuracy = ((double) countPositives / (rows.size())) * 100;
		System.out.println(typeOfData+" Accuracy = "+ accuracy + "%");
		return accuracy;
	}
	
	public void fillArray(int[] data, int index) {
		int arrayIndex = 0;
		for(int[]temp : rows) {
			data[arrayIndex++] = temp[index];
		}
	}
	
	public double getEntropy(ArrayList<Integer> list) {
		double entropy = 0.0;
		int positives = 0;
		int negatives = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == 0) { // its a positive
				positives++;
			}
			else { // finalClass is negative
				negatives++;
			}
		}
		double val1 = (double) (positives) / (positives + negatives);
		double val2 = (double) (negatives) / (positives + negatives);
		entropy = -(val1 * log2(val1)) - (val2 * log2(val2));
		return entropy;
	}
	
	public double log2(double d) {
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

	public int getColumns() {
		return columns;
	}

	public int getNum() {
		// TODO Auto-generated method stub
		return num;
	}
	
	public ArrayList<Integer> getHeaderType() {
		return headerType;
	}
	
	public ArrayList<Double> getSplitValue() {
		return splitValue;
	}

	public void getMeanandStDev(double[] acc) {
		
		double sum = 0.0;
		for(int i = 0; i<acc.length; i++) {
			sum+=acc[i];
		}
		double mean = sum/acc.length;
		
		double sumsq = 0.0;
		for(int i = 0; i<acc.length; i++) {
			sumsq += (acc[i]-mean)*(acc[i]-mean);
		}
		double stdev = Math.sqrt(sumsq/acc.length);
		
		System.out.println("Mean Accuracy = "+String.format("%.4f", mean)+"%, and the Standard Deviation = "+String.format("%.4f", stdev)+"%.");
		/*
		mean = mean/100.0;
		stdev = stdev/100.0;
		System.out.print(String.format("%.6f, ", mean)+String.format("%.6f -", stdev));
		*/
	}

}
