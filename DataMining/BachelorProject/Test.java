import java.io.*;

public class Test {

	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.out.println("Format: java Test [Test File Name] (e.g.test.data)");
			System.out.println("Pls restart the program!");
			System.exit(1);
		}
		TagsAndContent tac = new TagsAndContent();
		// System.out.println(tac.getTagsSize());
		// System.out.println(tac.getAllWordsSize());
		int[] resultIndex = new int[354];
		for (int i = 0; i < 354; i++) {
			resultIndex[i] = -1;
		}

		BufferedReader brtest = null;
		//BufferedWriter bwtest = null;
		BufferedWriter bwresult = null;
		// Read the test file
		try {
			brtest = new BufferedReader(new FileReader(args[0].trim()));
			//bwtest = new BufferedWriter(new FileWriter("test.txt"));
			bwresult = new BufferedWriter(new FileWriter("result.txt"));
			while (true) {
				String line = brtest.readLine();
				if (line == null)
					break;
				String[] temp = line.split("#\\$#");
				if (temp.length != 3) {
					break;
				}
				//bwtest.write(temp[0]+"#$#"+temp[2]+"\n");
				//bwtest.flush();
				resultIndex = tac.test_one(temp[1]);
				String tags="";
				for (int j = 0; j < 354; j++) {
					if(resultIndex[j] != -1) {
						String label = tac.getTag(resultIndex[j]).replace('_',' ');
						tags = tags+label+",";
					}
					else {
						break;
					}
				}
				bwresult.write(temp[0]+"#$#"+tags+"\n");
				bwresult.flush();
				}
		}
		catch(FileNotFoundException ex) {
			System.out.println("File "+args[0]+" does not exist\nPls restart the test program!");
			System.exit(1);
		}
		finally {
			brtest.close();
		}
		// brtest.close();
		bwresult.close();
		System.out.println("End Test!");
		System.out.println("========================================================================");
		System.out.println("In all test case: "+tac.getcaseNum()+"\nPls refer to file result.txt for detail.");
		System.out.println("========================================================================");
		// Evaluation evtest = new Evaluation();
		System.out.println("Thank you!");
	}
}