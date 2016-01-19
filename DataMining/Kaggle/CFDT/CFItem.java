import java.io.IOException;

public class CFItem {
	
	public static void main(String[] args) throws IOException {
		
		// generating files for training and testing
		System.out.println("Generating training and testing files...");
		SrcDeal sd = new SrcDeal();
		sd.init();
		/* My decision Tree */
		// build tree
		String trainFile = "cfdttrain.csv";
		String testFile = "cfdttest.csv";
		System.out.println("Loading training file...");
		MatrixData md = new MatrixData();
		md.loadMatrix(trainFile, 1);
		System.out.println("Start Learning...");
		ID3Learner learner = new ID3Learner();
		TreeNode rootNode = learner.startLearning(trainFile);
		System.out.println("Loading testing file...");
		MatrixData mdTest = new MatrixData();
		mdTest.loadMatrix(testFile, 0);
		mdTest.getTest(rootNode);
		sd.collect();
		System.out.println("End");
		// Decided by CF: 33.5644%		
	}
}
