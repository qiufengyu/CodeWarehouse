import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.mashape.unirest.http.exceptions.UnirestException;

public class NERTest {

	public static void main(String[] args) throws IOException, UnirestException {
		// TODO Auto-generated method stub
		long t1 = System.currentTimeMillis();
		// splitTest();
		String fileName = "BosonNLP_NER_6C.txt";
		
		String[] types = {"time", "location", "person_name", "org_name", "company_name", "product_name"};
		for(int i = 0; i<types.length; i++) {
			String type = types[i];
			System.out.println("Testing on entity <"+type+">...");
			for(int p = 0; p<5; p++) {
				DataPre.extract(fileName, p*400, (p+1)*400);
				DataPre.loadTarget(fileName, p*400, (p+1)*400);
				Model model = new Model(type);
				model.train();
				model.test(p*400, (p+1)*400);
				model.getAccuracy(p);
			}	
		}
		
		long t2 = System.currentTimeMillis();
		double exeTime = (double)(t2-t1)/1000.0;
		System.out.println("Testing Time = "+String.format("%.4f", exeTime)+" seconds");
		
		for(int i = 0; i<types.length; i++) {
			File f = new File(types[i]+".txt");
			f.delete();
		}
		File path = new File("target");
		String[] fileList = path.list();
		for(String f : fileList) {
			File tempf = new File(path+".\\"+f);
			tempf.delete();
		}

	}
	
	public static void splitTest() throws IOException, UnirestException {
		long t1 = System.currentTimeMillis();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("data.txt")));
		SentenceSplit ss = new SentenceSplit();
		for(int i = 0; i<2000; i++) {			
			String temp = br.readLine();
			String[] line = {temp};
			ss.splitWords(line);
			ArrayList<String> word = ss.getSplitWords();
			ArrayList<String> tag = ss.getSplitTags();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\file\\"+String.valueOf(i+1)+".txt")));
			int sz1 = word.size();
			int sz2 = tag.size();
			int sz = Math.min(sz1, sz2);
			for(int j = 0; j<sz; j++) {
				bw.write(word.get(j)+" "+tag.get(j)+"\n");
			}
			bw.flush();
			bw.close();
		}
		br.close();
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1+" ms");
	}

}
