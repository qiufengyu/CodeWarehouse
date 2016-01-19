import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class IBMInverse {
	Map<String, Map<String, Double>> probTInverse; // t(en|ch) with probablity

	Map<String, Map<String, Double>> countMap; // count(ch|en)
	Map<String, Double> totalMap; //total(en) 
	
	ArrayList<ArrayList<String>> english; //english sentences
	ArrayList<ArrayList<String>> chinese; //chinese sentences
	
	public IBMInverse() {
		probTInverse = new HashMap<String, Map<String, Double>>();
		countMap = new HashMap<String, Map<String, Double>>();
		totalMap = new HashMap<String, Double>();
		english = new ArrayList<ArrayList<String>>();
		chinese = new ArrayList<ArrayList<String>>();
	}
	
	public void modelInverse(String enFile, String chFile, int MAX_ITERATOR) throws IOException {
		
		int numOfCh = getNumOfEnglish(enFile);
		// System.out.println(numOfCh);
		// 934 chinese words, including punctuations!
		
		// fill in probability t(ch|en) with initialized 1/num of Chinese words
		initProbTInverse(chFile, enFile, numOfCh);		
		
		// do until coverage with EM
		EMModelInverse(MAX_ITERATOR);
		
		writeMatrixInverse("matrix"+MAX_ITERATOR+"Inverse.txt");
		
		System.out.println("IBM Model 1(Inverse) Trained");	
		
	}
	
	public Map<String, Map<String, Double>> getProbTInverse() {
		return probTInverse;
	}
	
	private void EMModelInverse(int max_iterator) {
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
						double incrementT = probTInverse.get(enlist.get(j)).get(chlist.get(i));
						totalSumMap.put(chlist.get(i), oldVal+incrementT);
					}
				}
				
				// updating countMap and totalMap
				for(int i = 0; i<chlist.size(); i++) {
					for(int j = 0; j<enlist.size(); j++) {
						double tempT = probTInverse.get(enlist.get(j)).get(chlist.get(i));
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
			
			// updating probTInverse table for all en and ch
			for(Entry<String, Map<String, Double>> entry: countMap.entrySet()) {
				String enToken = entry.getKey();
				Map<String, Double> m = entry.getValue();
				Map<String, Double> innerT = probTInverse.get(enToken);
				for(Entry<String, Double> entry2: m.entrySet()) {
					String chToken = entry2.getKey();
					innerT.put(chToken, entry2.getValue()/totalMap.get(enToken));
				}
				probTInverse.put(enToken, innerT);
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
	private void initProbTInverse(String enFile, String chFile, int numOfCh) throws IOException {
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
				if(probTInverse.containsKey(enlist.get(i))) {
					Map<String, Double> innerMap = probTInverse.get(enlist.get(i));
					for(int j = 0; j<chlist.size(); j++) {
						innerMap.put(chlist.get(j), (double)1.0/numOfCh);
					}
					probTInverse.put(enlist.get(i), innerMap);
				}
				else {
					Map<String, Double> innerMap = new HashMap<String, Double>();
					for(int j = 0; j<chlist.size(); j++) {
						innerMap.put(chlist.get(j), (double)1.0/numOfCh);
					}
					probTInverse.put(enlist.get(i), innerMap);
				}				
			}			
		}
		enReader.close();
		chReader.close();
		
	}
		
	private int getNumOfEnglish(String chFile) throws IOException {
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
	
	private void writeMatrixInverse(String name) throws IOException {		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name)));
		for(Entry<String, Map<String, Double>> entry : probTInverse.entrySet()) {
			String en = entry.getKey();
			Map<String, Double> inn = probTInverse.get(en);
			for(Entry<String, Double> inentry : inn.entrySet()) {
				writer.write("P("+inentry.getKey()+"|"+en+")="+Math.log(inentry.getValue())+"\n");
			}
					
			writer.flush();
		}
		
		writer.close();
	}

}
