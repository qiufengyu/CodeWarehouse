import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Decoder {
	
	/**
	 * alignment with agreement
	 * @param probT
	 * @param probTInverse
	 * @param enFile
	 * @param chFile
	 * @param align
	 * @throws IOException
	 */
	private static Set<String> filterSet;
	
	public static void initSet() {
		filterSet = new HashSet<String>();
		/*
		filterSet.add(",");
		filterSet.add(".");
		filterSet.add("(");
		filterSet.add(")");
		filterSet.add("?");
		filterSet.add(":");
		filterSet.add("\"");
		filterSet.add("¡£");
		filterSet.add("£¬");
		filterSet.add("£¨");
		filterSet.add("£©");
		filterSet.add("£¿");
		filterSet.add("£º");
		filterSet.add("¡°");
		filterSet.add("¡±");
		*/
	}	
	
	public static void decode(Map<String, Map<String, Double>> probT,
			Map<String, Map<String, Double>> probTInverse,
			String enFile,
			String chFile,
			String align
			) throws IOException  {
		BufferedReader enReader = new BufferedReader(new InputStreamReader(new FileInputStream(enFile),"UTF-8"));
		BufferedReader chReader = new BufferedReader(new InputStreamReader(new FileInputStream(chFile),"UTF-8"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(align)));
		while(true) {
			String enline = enReader.readLine();
			String chline = chReader.readLine();
			if(enline == null || chline == null) 
				break;
			ArrayList<String> enlist = new ArrayList<String>();
			ArrayList<String> chlist = new ArrayList<String>();
			for(String entoken : enline.split(" ")) {
				enlist.add(entoken);
			}
			for(String chtoken : chline.split(" ")) {
				chlist.add(chtoken);
			}
			// chlist.add("NULL");
			
			for(int i = 0; i<enlist.size(); i++) {
				if(!filter(enlist.get(i))) {
					double max_t = -Double.MAX_VALUE;
					// find max_t
					for(int j = 0; j<chlist.size(); j++) {
						if(!filter(chlist.get(j))) {
							double temp = Math.log(probT.get(enlist.get(i)).get(chlist.get(j)));
							double tempInverse = Math.log(probTInverse.get(chlist.get(j)).get(enlist.get(i)));	
							double sum = temp+tempInverse;
							if(sum>max_t) {
								max_t = sum;
							}
						}
					}
					// get all max_t
					if(max_t > -3.9) {
						for(int j = 0; j<chlist.size(); j++) {
							if(!filter(chlist.get(j))) {
								double temp = Math.log(probT.get(enlist.get(i)).get(chlist.get(j)));
								double tempInverse = Math.log(probTInverse.get(chlist.get(j)).get(enlist.get(i)));
								if(temp+tempInverse == max_t) {
									writer.write(j+"-"+i+" ");
								}
							}
						}
					}
					
				}
			}
			writer.write("\n");
			writer.flush();
			
		}
		enReader.close();
		chReader.close();
		writer.close();
	}
	
	
	public static void evaluate(String ground, String myFile) throws IOException {
		BufferedReader groundReader = new BufferedReader(new InputStreamReader(new FileInputStream(ground)));
		BufferedReader myReader = new BufferedReader(new InputStreamReader(new FileInputStream(myFile)));
		int totalGround = 0;
		int totalMy = 0;
		int totalCorrect = 0;
		while(true) {
			String gline = groundReader.readLine();
			String mline = myReader.readLine();
			if(gline == null || mline == null)
				break;
			HashSet<String> groundSet = new HashSet<String>();
			HashSet<String> mySet = new HashSet<String>();
			for(String temp1:gline.split(" ")) {
				if(temp1.length()>2) {
					groundSet.add(temp1);
				}
			}
			for(String temp2:mline.split(" ")) {
				if(temp2.length()>2) {
					mySet.add(temp2);
				}
			}
			totalGround += groundSet.size();
			totalMy += mySet.size();
			for(String x: groundSet) {
				if(mySet.contains(x)) {
					totalCorrect++;
				}
			}				
		}
		
		System.out.println("correct/ground = "+((double) totalCorrect/totalGround)*100+"%");
		System.out.println("correct/my = "+((double) totalCorrect/totalMy)*100+"%");
		
		groundReader.close();
		myReader.close();
	}
	
	public static boolean filter(String t) {
		return filterSet.contains(t);
		
	}

}
