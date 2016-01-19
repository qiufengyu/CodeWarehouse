import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AdaBoostTest {
	
	static int MAXIT = 100;

	public static void main(String[] args) throws IOException {
		long time1 = System.currentTimeMillis();
		// TODO Auto-generated method stub
		// cmd line
		String fileName;
		if(args.length == 1 && args[0].equals("german")) {
			fileName = "german-assignment5.txt";
		}
		else {
			fileName = "breast-cancer-assignment5.txt";
		}
		System.out.println(AdaBoostTest.class+" on DataSet <"+fileName+">.");

		final int CROSS = 10;
		MatrixData md = new MatrixData();
		md.loadMatrix(fileName, 0, 0, 2);
		int scale = (int)Math.ceil(md.getNum()/10);	
		int start = 0;
		int end = 0;

		ArrayList<Double> splitValue;		
		// mark the accuracy vote
		ArrayList<Double>alpha = new ArrayList<Double>();
		ArrayList<TreeNode> testNode = new ArrayList<TreeNode>();
		
		double[] acc = new double[CROSS];
		// 10 fold cross
		for(int p = 0; p<CROSS; p++) {
			start = p*scale;
			if(p == CROSS-1) {
				end = md.getNum();
			}
			else {
				end = (p+1)*scale;
			}
			
			ID3Learner learner = new ID3Learner(fileName, start, end);
			learner.startLearning();
			// MatrixData tempp = learner.getMatrix();
			// tempp.printMatrix();
			splitValue = learner.getSplitValue();
			int NCount = learner.getMatrix().getNum();
			
			MatrixData testMatrix = new MatrixData();
			testMatrix.loadTestMatrix(fileName, start, end, 2, splitValue);
			// Ada
			AdaBoostDriver ada = new AdaBoostDriver(learner, fileName);
			// System.out.println("--------------------");
			String newTestFile = ".\\ada\\tempTrain"+String.valueOf(p+1)+".txt";
			// int samples = NCount - scale;
			double initWeight = ((double)1)/((double)(NCount));
			ArrayList<Double> weight = new ArrayList<Double>();
			
			for(int i = 0; i<NCount; i++) {			
				weight.add(initWeight);
			}
			
			for(int i = 0; i<MAXIT; i++) {					
				ada.Driver(weight, start, end, p);	
				ID3Learner adaLearner = new ID3Learner(newTestFile, 0, 0);
				TreeNode adaNode = adaLearner.startLearning();
				double current_correct = ada.updateWeight(weight, adaNode, adaLearner);
				alpha.add(current_correct);
				testNode.add(adaNode);
				if(i>0) {
					if(Math.abs(alpha.get(i)-alpha.get(i-1))<1e-6) 
						i = MAXIT; // break;
				}
			}
			
			// testMatrix.printMatrix();
			acc[p]=ada.getAccuracy(testMatrix, testNode, alpha, p);
			// System.out.println(testMatrix.getNum());
			
			/*
			int maxIndex = 0;
			double tempMax = -1.0;
			for(int i = 0; i<MAXIT; i++) {
				if(acc[i] > tempMax) {
					tempMax = acc[i];
					maxIndex = i;
				}
			}
			TreeNode maxNode = testNode[maxIndex];
			// Calculating the accuracy on Test Set
			MatrixData matrixTest = new MatrixData();
			matrixTest.loadTestMatrix(fileName, start, end, 2, splitValue);
			// matrixTests.printMatrix();
			String info = new String("");
			info = info + "Testing Set "+ String.valueOf(p+1);
			matrixTest.getTestAccuracy(maxNode, info);
			System.out.println();
			*/
		}
		
		System.out.println(AdaBoostTest.class+" on DataSet <"+fileName+">.");
		
		// get mean and standard deviation
		md.getMeanandStDev(acc);
		
		
		File path = new File("ada");
		for(File f: path.listFiles()) {
			f.delete();
		}
		path.delete();
		
		
		long time2 = System.currentTimeMillis();
		long t = time2-time1;
		
		System.out.println("Run Time = "+t+" ms, with Max Iterator "+MAXIT);
		
	}

}
