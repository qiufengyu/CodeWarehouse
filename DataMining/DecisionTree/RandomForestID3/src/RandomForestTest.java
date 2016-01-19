import java.io.File;
import java.io.IOException;

public class RandomForestTest {

	public static void main(String[] args) throws IOException {
		long time1 = System.currentTimeMillis();
		// cmd line
		String fileName;
		if(args.length == 1 && args[0].equals("german")) {
			fileName = "german-assignment5.txt";	
		}
		else {
			fileName = "breast-cancer-assignment5.txt";
		}		
		System.out.println(RandomForestTest.class+" on DataSet <"+fileName+">");
		
		final int CROSS = 10;
		MatrixData md = new MatrixData();
		md.loadMatrix(fileName, 0, 0, 2);
		int scale = (int)Math.round(md.getNum()/10);
		int col = md.getColumns()-1;
		int start = 0;
		int end = 0;
		RandomForestDriver rf = new RandomForestDriver(fileName);
		
		double[] acc = new double[CROSS];
		// 10 fold cross validation
		for(int p = 0; p<CROSS; p++) {	
			start = p*scale;
			end = (p+1)*scale;			
			rf.Driver(start, end, col);
			int trees = rf.getTreeCount();
			File path = new File("rf");
			ID3Learner[] learners = new ID3Learner[trees];
			TreeNode[] treeNodes = new TreeNode[trees];
			if(path.isDirectory()) {
				for(int i = 0; i<trees; i++) {
					String subName = ".\\rf\\forest-train"+String.valueOf(i+1)+".txt";
					ID3Learner learner = new ID3Learner(subName, 0, 0);
					TreeNode rootNode = new TreeNode();
					rootNode = learner.startLearning();
					// System.out.println(learner.getMatrix().getNum());
					learners[i] = learner;
					treeNodes[i] = rootNode;
				}
			}	
			else {
				System.out.println("File path error!");
			}
			acc[p] = rf.getAccuracy(treeNodes, learners, 0, scale, p);
			
			for(File f: path.listFiles()) {
				f.delete();
			}
			path.delete();
			
		}		
		System.out.println(RandomForestTest.class+" on DataSet <"+fileName+">");
		
		// get mean and standard deviation		
		md.getMeanandStDev(acc);
			
		long time2 = System.currentTimeMillis();
		long t = time2-time1;
		System.out.println(t+"ms");
		System.out.println("Run Time = "+t+" ms");
	}

}
