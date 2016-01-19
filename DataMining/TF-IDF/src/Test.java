import java.io.*;
import java.util.*;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FileLoad fileLoad = new FileLoad();
		Set<String> wordSet = fileLoad.fileReader();
		File path = new File(".");
		File[] files = path.listFiles();
		for(int i = 0; i<files.length; i++) {
			if(files[i].isFile()) {
				String tempFileName = files[i].getName();
				if(tempFileName.endsWith("txt")) {
					Calculate cal = new Calculate(tempFileName, wordSet);
					cal.calculateTF_IDF();
					cal.writeFiles(tempFileName);
				}
			}
		}	
		System.out.println("Test End");
	}

}
