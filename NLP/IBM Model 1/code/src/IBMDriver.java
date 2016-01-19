import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class IBMDriver {

	Map<String, Map<String, Double>> probT;//t(ch|en) with probability
 	Map<String, Map<String, Double>> countMap; // count(ch|en)
	Map<String, Double> totalMap; //total(en) 
	
	ArrayList<ArrayList<String>> english; //english sentences
	ArrayList<ArrayList<String>> chinese; //chinese sentences
	
	public IBMDriver() {
		probT = new HashMap<String, Map<String, Double>>();
		countMap = new HashMap<String, Map<String, Double>>();
		totalMap = new HashMap<String, Double>();
		english = new ArrayList<ArrayList<String>>();
		chinese = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * 
	 * @param enFile English sentences file
	 * @param chFile Chinese sentences file
	 * @param MAX_ITERATOR iteration times, not need to be too large, 100 is ok!
	 * @throws IOException java.io.IOException
	 */
	public void model(String enFile, String chFile, int MAX_ITERATOR) throws IOException {		
		//Initialize t(ch|en), with 1/num of Chinese words		
		int numOfCh = getNumOfChinese(chFile);
		// System.out.println(numOfCh);
		// 934 chinese words, including punctuations!
		
		// fill in probability t(ch|en) with initialized 1/num of Chinese words
		initProbT(enFile, chFile, numOfCh);		
		
		// do until coverage with EM
		EMModel(MAX_ITERATOR);
		
		writeMatrix("matrix"+MAX_ITERATOR+".txt");
		
		System.out.println("IBM Model 1 Trained");	
		
	}
	
	public Map<String, Map<String, Double>> getProbT() {
		return probT;
	}
	
	
	
	/**
	 * update probability t(ch|en) with iteration
	 * @param max_iterator
	 */
	private void EMModel(int max_iterator) {
		for(int it = 0; it<max_iterator; it++) {
			// initial count and total = 0
			countMap.clear();
			totalMap.clear();
			for(int k = 0; k<english.size(); k++) {
				ArrayList<String> enlist = english.get(k);
				ArrayList<String> chlist = chinese.get(k);
				for(int i = 0; i<enlist.size(); i++) {
					if(countMap.containsKey(enlist.get(i))) {
						Map<String, Double> innerMap = countMap.get(enlist.get(i));
						for(int j = 0; j<chlist.size(); j++) {
							innerMap.put(chlist.get(j), 0.0);
						}
						countMap.put(enlist.get(i), innerMap);
					}
					else {
						Map<String, Double> innerMap = new HashMap<String, Double>();
						for(int j = 0; j<chlist.size(); j++) {
							innerMap.put(chlist.get(j), 0.0);
						}
						countMap.put(enlist.get(i), innerMap);
					}
					totalMap.put(enlist.get(i), 0.0);
				}
				
			}
			
			// update total_s
			for(int k = 0; k<english.size(); k++) {				
				ArrayList<String> enlist = english.get(k);
				ArrayList<String> chlist = chinese.get(k);
				// total_s map for chinese
				HashMap<String, Double> totalSumMap = new HashMap<String, Double>();
				// all chinese words in chinese sentence
				for(int i = 0; i<chlist.size(); i++) {
					totalSumMap.put(chlist.get(i), 0.0);
					for(int j = 0; j<enlist.size(); j++) {
						double oldVal = totalSumMap.get(chlist.get(i));
						double incrementT = probT.get(enlist.get(j)).get(chlist.get(i));
						totalSumMap.put(chlist.get(i), oldVal+incrementT);
					}
				}
				
				// updating countMap and totalMap
				for(int i = 0; i<chlist.size(); i++) {
					for(int j = 0; j<enlist.size(); j++) {
						double tempT = probT.get(enlist.get(j)).get(chlist.get(i));
						double tempIncrement = tempT / totalSumMap.get(chlist.get(i));
						double oldCount = countMap.get(enlist.get(j)).get(chlist.get(i));
						double oldTotal = totalMap.get(enlist.get(j));
						Map<String, Double> inner = countMap.get(enlist.get(j));
						inner.put(chlist.get(i), tempIncrement+oldCount);
						countMap.put(enlist.get(j), inner);
						totalMap.put(enlist.get(j), tempIncrement+oldTotal);
					}
				}				
			}
			
			// updating probT table for all en and ch
			for(Entry<String, Map<String, Double>> entry: countMap.entrySet()) {
				String enToken = entry.getKey();
				Map<String, Double> m = entry.getValue();
				Map<String, Double> innerT = probT.get(enToken);
				for(Entry<String, Double> entry2: m.entrySet()) {
					String chToken = entry2.getKey();
					innerT.put(chToken, entry2.getValue()/totalMap.get(enToken));
				}
				probT.put(enToken, innerT);
			}
			
			
		} // end of iteration
		
	}
	

	/**
	 * it also fill the arraylist for english sentences and chinese sentences
	 * @param enFile english sentences file
	 * @param chFile chinese sentences file
	 * @param numOfCh number of chinese words
	 * @throws IOException
	 */
	private void initProbT(String enFile, String chFile, int numOfCh) throws IOException {
		BufferedReader enReader = new BufferedReader(new InputStreamReader(new FileInputStream(enFile),"UTF-8"));
		BufferedReader chReader = new BufferedReader(new InputStreamReader(new FileInputStream(chFile),"UTF-8"));
		while(true) {
			String enline = enReader.readLine();
			String chline = chReader.readLine();
			if(enline == null || chline == null) 
				break;
			ArrayList<String> enlist = new ArrayList<String>();
			ArrayList<String> chlist = new ArrayList<String>();
			for(String entoken : enline.split(" ")) {
				if(!Decoder.filter(entoken))
					enlist.add(entoken);
			}
			enlist.add("NULL");
			english.add(enlist);
			for(String chtoken : chline.split(" ")) {
				if(!Decoder.filter(chtoken))
				chlist.add(chtoken);
			}
			chinese.add(chlist);
			for(int i = 0; i<enlist.size(); i++) {
				if(probT.containsKey(enlist.get(i))) {
					Map<String, Double> innerMap = probT.get(enlist.get(i));
					for(int j = 0; j<chlist.size(); j++) {
						innerMap.put(chlist.get(j), (double)1.0/numOfCh);
					}
					probT.put(enlist.get(i), innerMap);
				}
				else {
					Map<String, Double> innerMap = new HashMap<String, Double>();
					for(int j = 0; j<chlist.size(); j++) {
						innerMap.put(chlist.get(j), (double)1.0/numOfCh);
					}
					probT.put(enlist.get(i), innerMap);
				}				
			}			
		}
		enReader.close();
		chReader.close();
		
	}
	

		
	private int getNumOfChinese(String chFile) throws IOException {
		BufferedReader chReader = new BufferedReader(new InputStreamReader(new FileInputStream(chFile),"UTF-8"));
		HashSet<String> chineseSet = new HashSet<String>();
		while(true) {
			String chline = chReader.readLine();
			if(chline == null)
				break;
			String[] parts = chline.split(" ");
			for(int i = 0; i<parts.length; i++) {
				if(!Decoder.filter(parts[i]))
					chineseSet.add(parts[i]);
			}
		}		
		
		chReader.close();
		return chineseSet.size();
	}

	
	private void writeMatrix(String name) throws IOException {		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
//		double max = -1.0;
//		double min = 1.0;
		for(Entry<String, Map<String, Double>> entry : probT.entrySet()) {
			String en = entry.getKey();
			Map<String, Double> inn = probT.get(en);
			for(Entry<String, Double> inentry : inn.entrySet()) {
//				if(inentry.getValue()<min) {
//					min = inentry.getValue();
//				}
//				if(inentry.getValue()>max) {
//					max = inentry.getValue();
//				}
				writer.write("P("+inentry.getKey()+"|"+en+")="+Math.log(inentry.getValue())+"\n");
			}
					
			writer.flush();
		}
		
		writer.close();
		
//		System.out.println(max+"\t"+min);
		
	}
	
	
	
	
	
}
