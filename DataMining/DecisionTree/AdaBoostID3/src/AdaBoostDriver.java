import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AdaBoostDriver {
	
	MatrixData matrix;
	Random random;
	int dataSize;
	String fileName;
	
	public AdaBoostDriver() {
		
	}
	
	public AdaBoostDriver(ID3Learner learner, String fileName) {
		matrix = new MatrixData();
		matrix = learner.getMatrix(); 
		dataSize = matrix.getNum();
		
		random = new Random();
		this.fileName = fileName;
	}
	
	public void Driver(ArrayList<Double>weight, int start, int end, int pp) throws IOException {
		File path = new File("ada");
		if(path.exists()) {
			path.delete();
		}
		path.mkdir();
		int features = matrix.getColumns();
		int rows = matrix.getNum();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\ada\\tempTrain"+String.valueOf(pp+1)+".txt")));		
		String headers = new String("");
		for(int t = 0; t<features-1; t++) {
			headers = headers + String.valueOf(1)+",";
		}
		headers = headers.substring(0, headers.length()-1)+"\n";
		bw.write(headers);
		int retVal = 0;
		for(int i = 0; i<rows; i++) {
			double p = weight.get(i);
			int samples = (int)Math.round(p*rows);
			for(int j = 0; j<samples+1; j++) {
				String temp = new String("");			
				for(int t = 0; t<features-1; t++) {
					temp = temp+String.valueOf(matrix.getRows().get(i)[t])+",";
				}
				int l = matrix.getRows().get(i)[features-1];
				temp = temp+String.valueOf(l)+"\n";
				bw.write(temp);
				retVal++;
			}
			bw.flush();			
		}
		
		for(int x = 0; retVal<dataSize; retVal++) {
			x = random.nextInt(dataSize);
			String temp = new String("");	
			for(int t = 0; t<features-1; t++) {
				temp = temp+String.valueOf(matrix.getRows().get(x)[t])+",";
			}
			int l = matrix.getRows().get(x)[features-1];
			temp = temp+String.valueOf(l)+"\n";
			bw.write(temp);
			bw.flush();
		}
		
		bw.close();	
	}
	
	// http://jasonding1354.github.io/2015/07/24/Machine%20Learning/%E3%80%90%E6%9C%BA%E5%99%A8%E5%AD%A6%E4%B9%A0%E5%9F%BA%E7%A1%80%E3%80%91%E6%A2%AF%E5%BA%A6%E6%8F%90%E5%8D%87%E5%86%B3%E7%AD%96%E6%A0%91/
	public double updateWeight(ArrayList<Double> weight, TreeNode node, ID3Learner learner) {
		int correct = 0;
		MatrixData m = learner.getMatrix();
		int sz = m.getNum();
		boolean[] flags = new boolean[sz];
		for(int i = 0; i<sz; i++) {
			flags[i] = false;
		}
		int x = -2;
		int j = 0;
		for(int[] temp : m.getRows()) {
			// printTemp(temp);
			HashMap<String, Integer> testValues = new HashMap<String, Integer>();
			int col = m.getColumns();
			for (int i = 0; i < col-1; i++) {
				testValues.put((String) m.getHeaders().get(i), temp[i]);
			}
			int finalValue = temp[col-1];
			x = node.classifyTest(testValues, node);
			if(x == finalValue) {
				correct++;
				flags[j] = true;
			}		
			j++;
		}	
		double val = ((double)correct)/((double)sz-(double)correct);
		// double errorRate = ((double)sz-(double)correct)/((double)sz);
		if(sz-correct == 0)
			return Double.MAX_VALUE;
		double val2 = Math.sqrt(val);
		// System.out.println(m.getNum()+"\t"+correct+"\t"+val2);
		double alpha = Math.log(val2);
		double sigma = 0.0;
		for(int i = 0; i<weight.size(); i++) {
			double oldValue = weight.get(i);
			if(flags[i]) {				
				weight.set(i, oldValue/val2);
			}
			else {
				weight.set(i, oldValue*val2);
			}
			sigma = sigma+weight.get(i);
		}	
		
		// normalize weight
		for(int i = 0; i<weight.size(); i++) {
			double oldVal = weight.get(i);
			weight.set(i, oldVal/sigma);			
		}	
		
		return alpha;		
	}
	
	
	public double getAccuracy(MatrixData matrixTest, ArrayList<TreeNode> tn, ArrayList<Double> alpha,  int p) {
		int correct = 0;
		int num = matrixTest.getNum();
		double[] sum = new double[num];
		double[] sigmaW = new double[num];
		for(int i = 0; i<num; i++) {
			sum[i] = 0.0;
			sigmaW[i] = 0.0;
		}
		
		for(int j = 0; j<tn.size(); j++) {
			TreeNode node = tn.get(j);
			double w = alpha.get(j);
			int x = -1;
			int index = 0;
			for(int[] temp: matrixTest.getRows()) {
				HashMap<String, Integer> testValues = new HashMap<String, Integer>();
				int col = matrixTest.getColumns();
				for (int i = 0; i < col-1; i++) {
					testValues.put((String) matrixTest.getHeaders().get(i), temp[i]);
				}
				x = node.classifyTest(testValues, node);
				if(x!=-1) {
					sum[index]=sum[index]+(double)x*w;
					sigmaW[index] += alpha.get(j);
				}
				index++;
			}			
		}
		
		int[] groundLabels = new int[num];
		matrixTest.fillArray(groundLabels, matrixTest.getColumns()-1);
		
		double[] testLabels = new double[num];
		for(int i = 0; i<num; i++) {
			testLabels[i] = sum[i]/sigmaW[i];
			// System.out.println(testLabels[i]);
			if((testLabels[i]>=0.5 && groundLabels[i]==1) ||(testLabels[i]<0.5 && groundLabels[i]==0)) {
				correct++;
			}
			
		}
		
		String info = new String("");
		info = info + "Testing Set "+ String.valueOf(p+1)+" Accuracy = ";
		double accuracy = ((double)(correct))/((double)num)*100;
		System.out.println(info+accuracy+"%\n");
		return accuracy;
	}
	
}
