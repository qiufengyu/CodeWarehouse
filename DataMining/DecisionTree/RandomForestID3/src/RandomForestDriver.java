import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomForestDriver {

	int features;
	int treeCount;
	int[] groundLabels;
	int num;
	ArrayList<Double> sv;
	Set<Integer> featureSet;
	String fileName;
	Random random;
	
	public RandomForestDriver(String fileName) {
		sv = new ArrayList<Double>();
		featureSet = new HashSet<Integer>();
		this.fileName = fileName;
		random = new Random();
	}
	
	public void Driver(int start, int end, int col) throws IOException {
		File dir = new File("rf");
		dir.mkdirs();

		MatrixData tempMatrix = new MatrixData();
		tempMatrix.loadMatrix(fileName, start, end, 2);
		sv = tempMatrix.getSplitValue();
		MatrixData tempMatrix2 = new MatrixData();
		tempMatrix2.loadTestMatrix(fileName, start, end, 2, sv);
		// System.out.println(firstMatrix.getColumns());
		//features =(int)Math.ceil((Math.log(col)/Math.log(2)+1));
		features = (int)(Math.ceil(Math.sqrt(col)));
		treeCount = features*2+1;
				
		int featureMax = col; // already -1!
		// System.out.println(col);
		int rowMax = tempMatrix.getNum();
		for(int i = 0; i<treeCount; i++) {
			int[] fIndex = new int[features];
			featureSet.clear();
			for(int t = 0; t<features; t++) {
				int temp = (int)(random.nextInt(featureMax));
				if(featureSet.contains(temp)) {
					t--;
				}
				else {				
					fIndex[t] = temp;
					featureSet.add(temp);
				}
				// System.out.println(fIndex[t]);
			}
			String subTrainFileName = ".\\rf\\forest-train"+String.valueOf(i+1)+".txt";
			String subTestFileName = ".\\rf\\forest-test"+String.valueOf(i+1)+".txt";
			BufferedWriter bwTrain = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(subTrainFileName)));
			BufferedWriter bwTest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(subTestFileName)));
			String headers = new String("");
			for(int t = 0; t<features; t++) {
				headers = headers + String.valueOf(1)+",";
			}
			headers = headers.substring(0, headers.length()-1)+"\n";
			bwTrain.write(headers);
			bwTest.write(headers);
			for(int j = 0; j<rowMax; j++) {
				int rowIndex = (int)random.nextInt(rowMax);
				String temp = new String("");				
				for(int t = 0; t<features; t++) {
					temp = temp+String.valueOf(tempMatrix.getRows().get(rowIndex)[fIndex[t]])+",";
				}
				int l = tempMatrix.getRows().get(rowIndex)[featureMax];
				temp = temp+String.valueOf(l)+"\n";
				bwTrain.write(temp);
				bwTrain.flush();
			}
			for(int j = 0; j<tempMatrix2.getNum(); j++) {
				String temp2 = new String("");
				for(int t = 0; t<features; t++) {
					temp2 = temp2+String.valueOf(tempMatrix2.getRows().get(j)[fIndex[t]])+",";
				}
				int l = tempMatrix2.getRows().get(j)[featureMax-1];
				temp2 = temp2+String.valueOf(l)+"\n";
				bwTest.write(temp2);
			}
			bwTrain.close();
			bwTest.close();			
			
			int sz = tempMatrix2.getNum();
			groundLabels = new int[sz];
			num = sz;
			tempMatrix2.fillArray(groundLabels, tempMatrix.getColumns()-1);
		}
	}
	
	public int getFeatures() {
		return features;
	}

	public int getTreeCount() {
		return treeCount;
	}
	
	public double getAccuracy(TreeNode[] tn, ID3Learner[] ln, int start, int end, int p) throws IOException {
		int correct = 0;
		int[] sum = new int[num];
		int[] count = new int[num];
		for(int i = 0; i<num; i++) {
			sum[i]=0;
			count[i]=0;
		}
		for(int tnum = 0; tnum<treeCount; tnum++) {
			String subTestFileName = ".\\rf\\forest-test"+String.valueOf(tnum+1)+".txt";
			MatrixData m = new MatrixData();
			m.loadTestMatrix(subTestFileName, start, end, 2, sv);
			int x = 0;
			int j = 0;
			for(int[] temp : m.getRows()) {
				// printTemp(temp);
				HashMap<String, Integer> testValues = new HashMap<String, Integer>();
				int col = m.getColumns();
				for (int i = 0; i < col-1; i++) {
					testValues.put((String) m.getHeaders().get(i), temp[i]);
				}
				x = tn[tnum].classifyTest(testValues, tn[tnum]);
				if(x!=-1) {
					count[j]++;
					sum[j]=sum[j]+x;
				}
				j++;
			}			
		}
		for(int sz = 0; sz<num; sz++) {
			double w = ((double) sum[sz])/((double)count[sz]);
			// System.out.println(w);
			if((w>=0.5 && groundLabels[sz]==1) || (w<0.5 && groundLabels[sz] == 0) ) {
				correct++;
			}			
		}
		String info = new String("");
		info = info + "Testing Set "+ String.valueOf(p+1)+" Accuracy = ";
		double accuracy = ((double)(correct))/((double)num)*100;
		System.out.println(info+accuracy+"%\n");
		return accuracy;
	}
	
	
	public void printTemp(int[] a) {
		for(int i = 0; i<a.length; i++) {
			System.out.print(a[i]+" ");
		}
		System.out.println();
	}
}
