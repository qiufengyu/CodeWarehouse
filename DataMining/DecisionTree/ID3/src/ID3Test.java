import java.io.IOException;
import java.util.ArrayList;

public class ID3Test {

	public static void main(String[] args) throws IOException {
		long time1 = System.currentTimeMillis();
		String fileName;
		if(args.length == 1 && args[0].equals("german")) {
			fileName = "german-assignment5.txt";	
		}
		else {
			fileName = "breast-cancer-assignment5.txt";
		}
		
		System.out.println(ID3Test.class+" on DataSet <"+fileName+">");
		
		final int CROSS = 10;
		MatrixData md = new MatrixData();
		md.loadMatrix(fileName, 0, 0, 2);
		int scale = (int)Math.round(md.getNum()/10);
		
		int start = 0;
		int end = 0;
		double[] acc = new double[CROSS];
		
		ArrayList<Double> splitValue;
		for(int p = 0; p<CROSS; p++) {
			start = p*scale;
			end = (p+1)*scale;
			ID3Learner learner = new ID3Learner(fileName, start, end);
			TreeNode rootNode = learner.startLearning();
			splitValue = learner.getSplitValue();
			if (rootNode != null) {				
				/*
				// Calculating the accuracy on Training Set
				MatrixData matrixTests = new MatrixData();
				matrixTests.loadMatrix(fileName, start, end, 2);
				// matrixTests.printMatrix();
				matrixTests.getTestAccuracy(rootNode, "Training Set "+String.valueOf(p+1));
				*/
				
				// Calculating the accuracy on Test Set
				MatrixData matrixTest = new MatrixData();
				matrixTest.loadTestMatrix(fileName, start, end, 2, splitValue);
				// matrixTests.printMatrix();
				String info = new String("");
				info = info + "Testing Set "+ String.valueOf(p+1);
				acc[p] = matrixTest.getTestAccuracy(rootNode, info);	
				System.out.println();
	
			}
			else {
				System.out.println("Sorry! Something Went wrong, The algorithm was not able to create a decision Tree.");
			}
		}
		
		System.out.println(ID3Test.class+" on DataSet <"+fileName+">");
		md.getMeanandStDev(acc);
		
		long time2 = System.currentTimeMillis();
		long t = time2-time1;
		System.out.println("Run Time = "+t+" ms");
	}

}
