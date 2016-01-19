import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
	
public class DataPre {
	
	static final Pattern patternEntity = Pattern.compile("\\{\\{(.*?):(.*?)\\}\\}");
	static final Pattern patternDim = Pattern.compile("\\{\\{[a-z_]*:");
	
	public static void extract(String fileName, int start, int end) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		ArrayList<String> entityList = new ArrayList<String>();
		String timeE = "time";
		String locationE = "location";
		String person_nameE = "person_name";
		String org_nameE = "org_name";
		String company_nameE = "company_name";
		String product_nameE = "product_name";
		entityList.add(timeE);
		entityList.add(locationE);
		entityList.add(person_nameE);
		entityList.add(org_nameE);
		entityList.add(company_nameE);
		entityList.add(product_nameE);
		// Set<String> filterSet = new HashSet<String>();
		BufferedWriter[] bw = new BufferedWriter[6];
		// BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data.txt")));
		for(int i = 0; i<entityList.size(); i++) {
			String temp = entityList.get(i);
			bw[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp+".txt")));
		}
		int lineCount = 1;
		while(true) {
			String line = br.readLine();
			String line2 = new String("");
			if(line == null)
				break;
			BufferedReader brInner = new BufferedReader(new InputStreamReader(new FileInputStream(".\\file\\"+String.valueOf(lineCount)+".txt")));
			ArrayList<String> word = new ArrayList<String>();
			ArrayList<String> tag = new ArrayList<String>();
			ArrayList<Integer> offset = new ArrayList<Integer>();
			while(true) {
				String w = brInner.readLine();
				if(w == null)
					break;
				String[] parts = w.split(" ");
				if(parts.length >= 3) {
					word.add(parts[0]);
					tag.add(parts[1]);
					offset.add(Integer.valueOf(parts[2]));
				}				
			}
			
			for(int i = 0; i<word.size(); i++) {
				line2 = line2+word.get(i);
			}
			if(lineCount>=start && lineCount<end) {
				// do nothing
			}			
			else {				
				Matcher matcherEntity = patternEntity.matcher(line);
				while(matcherEntity.find()) {
					 String temp1 = matcherEntity.group(1);
					 String temp2 = matcherEntity.group(2);
					 temp2 = temp2.trim();
					 String endSymbol = temp2.substring(temp2.length()-1);
					
					 int startIndex = 0;
					 int sz = Math.min(word.size(), tag.size());
					 sz = Math.min(sz, offset.size());
					 int ss = 0;
					 // System.out.println(temp2);
					 startIndex = line2.indexOf(temp2);
					 for(int k = 0; k<sz-1; k++) {
						 if(offset.get(k)<=startIndex && offset.get(k+1)>startIndex) {
							 ss = k;
							 break;
						 }
					 }
					 
					 int endIndex = ss;
					 for(int j = ss; j<sz; j++) {
						 if(word.get(j).endsWith(endSymbol)) {
							 endIndex = j;
							 break;
						 }
					 }
					 endIndex = Math.min(endIndex, ss+temp2.length()-1);
					 // System.out.println(ss+" "+endIndex);
					 String serial = new String("");
					 while(ss<=endIndex) {
						 serial = serial + tag.get(ss)+" ";
						 ss++;
					 }
					 int bwIndex = entityList.indexOf(temp1);
					 bw[bwIndex].write(temp2+" "+serial+"\n");
					 bw[bwIndex].flush();
				 }
			}
			for(int i = 0; i<entityList.size(); i++) {
				bw[i].flush();
			}
			brInner.close();
			lineCount++;
		}
		for(int i = 0; i<entityList.size(); i++) {
			bw[i].close();
		}
		br.close();	
		
	}

	
	public void extract2() throws IOException {
		for(int i = 1; i<=2000; i++) {
			int len = 0;
			BufferedReader brInner = new BufferedReader(new InputStreamReader(new FileInputStream(".\\files\\"+String.valueOf(i)+".txt")));
			BufferedWriter bwInner = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\file\\"+String.valueOf(i)+".txt")));
			while(true) {
				String line = brInner.readLine();
				if(line == null) 
					break;
				String[] parts = line.split(" ");
				if(parts.length >= 2) {
					bwInner.write(parts[0]+" "+parts[1]+" "+len+"\n");
					len += parts[0].length();
				}				
				bwInner.flush();
			}
			bwInner.close();
			brInner.close();
		}
		
	}



	public static void genCRF(String fileName) throws IOException {
		File crf = new File("crfs");
		crf.mkdir();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		int lineCount = 1;
		while(true) {
			String line = br.readLine();
			ArrayList<Group> groupList = new ArrayList<Group>();
			if(line == null)
				break;
			String line2 = new String("");
			// line2.replaceAll("\\u201d", "");
			BufferedReader brSub = new BufferedReader(new InputStreamReader(new FileInputStream(".\\files\\"+String.valueOf(lineCount)+".txt")));
			while(true) {
				String subLine = brSub.readLine();
				if(subLine == null) 
					break;
				String[] parts= subLine.split(" ");
				if(parts.length >=2) {
					String temp = parts[0];
					temp = temp.trim();
					line2+=temp;
					String temp2 = parts[1];
					for(int j = 0; j<temp.length(); j++) {
						Group g;
						if(j == 0) {
							g = new Group(temp.charAt(j), temp2, 'B', "Other");
						}
						else {
							g = new Group(temp.charAt(j), temp2, 'I', "Other");
						}
						groupList.add(g);
					}					
				}
			}
			brSub.close();
			
			// change the entity type
			Matcher m = patternEntity.matcher(line);
			while(m.find()) {				
				String en1 = m.group(1).trim();
				String en2 = m.group(2).trim();
				int len = en2.length();
				// System.out.println(en2);
				int startIndex = line2.indexOf(en2);
				// System.out.println(startIndex);
				if(startIndex >= 0) { 
					// System.out.println(startIndex);
					for(int y = startIndex; y<len+startIndex; y++) {
						groupList.get(y).setType(en1);
						// System.out.println(groupList.get(y).getType());
					}
				}
			}
			
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\crfs\\"+String.valueOf(lineCount)+".txt")));
			for(int x = 0; x<groupList.size(); x++) {
				Group g = groupList.get(x);
				bw.write(g.getWord()+" "+g.getType()+" "+g.getBori()+"\n");
				bw.flush();
			}
			bw.close();
			
			lineCount++;
			
		}
		br.close();		
	}


	public static void loadTarget(String fileName, int start, int end) throws IOException {
		File path = new File("target");
		path.mkdir();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		ArrayList<String> entityList = new ArrayList<String>();
		String timeE = "time";
		String locationE = "location";
		String person_nameE = "person_name";
		String org_nameE = "org_name";
		String company_nameE = "company_name";
		String product_nameE = "product_name";
		entityList.add(timeE);
		entityList.add(locationE);
		entityList.add(person_nameE);
		entityList.add(org_nameE);
		entityList.add(company_nameE);
		entityList.add(product_nameE);
		// Set<String> filterSet = new HashSet<String>();
		BufferedWriter[] bw = new BufferedWriter[6];
		// BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data.txt")));
		for(int i = 0; i<entityList.size(); i++) {
			String temp = entityList.get(i);
			bw[i] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(".\\target\\"+temp+".txt")));
		}
		int lineCount = 1;
		while(true) {
			String line = br.readLine();
			if(line == null)
				break;
			
			if(lineCount>=start && lineCount<end) {
				Matcher matcherEntity = patternEntity.matcher(line);
				while(matcherEntity.find()) {
					 String temp1 = matcherEntity.group(1);
					 String temp2 = matcherEntity.group(2);	
					 int bwIndex = entityList.indexOf(temp1);
					 bw[bwIndex].write(temp2+"\n");
					 bw[bwIndex].flush();
				}
			}			
			else {
				
			}
			for(int i = 0; i<entityList.size(); i++) {
				bw[i].flush();
			}
			lineCount++;
		}
		for(int i = 0; i<entityList.size(); i++) {
			bw[i].close();
		}
		br.close();			
	}
	
	
	
}
	
