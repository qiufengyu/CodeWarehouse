import java.io.*;
import java.util.*;

public class AprioriTest {
	
	//Configurations
	private final int NUM = 11;
	private final int LINES= 1000;
	private final double MINSUP = 0.144;
	
	Vector<Vector<String>> largeItemset = new Vector<Vector<String>>();
	Vector<CandidateElement> candi = new Vector<CandidateElement>();
	
	// for result
	Vector<String> itemsetVector = new Vector<String>();
	Vector<Double> numVector = new Vector<Double>();
	Map<Vector<String>, Double> resultMap = new TreeMap<Vector<String>, Double>();
	
	String fullItemset;
	
	class ItemsetNode {
		String itemset;
		int count;
		
		public ItemsetNode() {
			itemset = new String();
			count = 0;
		}
		
		public ItemsetNode(String s, int i) {
			itemset = new String(s);
			count = i;
		}

	}
	
	class HashTreeNode {
		int nodeAttr; // 1-hashtable, 2-itemset list
		int nodeDepth; //level of the node means the length of the itemset
		Hashtable<String, HashTreeNode> hashTable;
		Vector<ItemsetNode> itemsetList;
		
		public HashTreeNode() {
			nodeAttr = 1;
			hashTable = new Hashtable<String, HashTreeNode>();
			itemsetList = new Vector<ItemsetNode>();
			nodeDepth = 0;
		}
		
		public HashTreeNode(int attr) {
			nodeAttr = attr;
			hashTable = new Hashtable<String, HashTreeNode>();
			itemsetList = new Vector<ItemsetNode>();
			nodeDepth = 0;
		}
		
	}
	
	class CandidateElement {
		HashTreeNode hashTreeNode;
		Vector candidateList;
	}
	
	public AprioriTest() throws IOException {
		CandidateElement candiElem;
		int k = 0;
		
		fullItemset = new String();
		fullItemset = fullItemset.concat("1");
		for(int i = 2; i<=NUM; i++) {
			fullItemset = fullItemset.concat(" ");
			fullItemset = fullItemset.concat(Integer.toString(i));
		}
		
		// start
		while(true) {
			k++;
			candiElem = new CandidateElement();
			candiElem.candidateList = genCandidate(k);
			
			if(candiElem.candidateList.isEmpty())
				break;
			
			candiElem.hashTreeNode = null;
			candi.addElement(candiElem);
			
			((CandidateElement)candi.get(k-1)).hashTreeNode = genCandidateHashTree(k);
			traverse(k);
			genFrequentItemset(k);		
		}
		resultWrite();
		System.out.println("Apriori Algorithm End");
	}
	
	private void resultWrite() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("result.txt"),"UTF-8"));
		for(int aa = 0; aa<numVector.size(); aa++) {
			bw.write(itemsetVector.get(aa)+" "+String.format("%.3f",numVector.get(aa))+"\n");
			bw.flush();
		}		
		bw.close();
	}
	
	// generate from k-set to (k+1) candidate set
	public Vector genCandidate(int k) {
		Vector<String> tempCandidateList = new Vector<String>();
		Vector<String> temp_v = new Vector<String>();
		int length;
		String candidate1 = new String();
		String candidate2 = new String();
		String newCandidate = new String();
		// 1 itemset
		if( 1 == k ) {
			for(int i = 1; i<=NUM; i++) {
				tempCandidateList.addElement(Integer.toString(i));
			}
		}
		// >=2
		else {
			temp_v = (Vector)largeItemset.get(k-2); //-2, not 1
			length = temp_v.size();
			for(int j = 0; j<length; j++) {
				// get 1 itemset element
				candidate1 = temp_v.get(j);
				// newCandidate = new String();
				for(int jj=j+1; jj<length; jj++) {
					candidate2 = temp_v.get(jj);
					newCandidate = new String();
					// attention: if k == 2, no rule, just join
					if( 2 == k) {
						newCandidate = candidate1.concat(" ");
						newCandidate = newCandidate.concat(candidate2);
						tempCandidateList.addElement(newCandidate.trim());
					}
					else { //only first k-2 element same ->downward closure property!
						String s1, s2;
						boolean same = true;
						for(int pos = 1; pos<k-1; pos++) {
							s1 = getItemAt(candidate1, pos);
							s2 = getItemAt(candidate2, pos);
							if(s1.compareToIgnoreCase(s2) != 0) {
								same = false;
								break;
							}
							else {
								newCandidate = newCandidate.concat(" ");
								newCandidate = newCandidate.concat(s1);
							}
						}
						// a legal C(k+1) element we've got
						if(same) {
							s1 = getItemAt(candidate1, k-1);
							s2 = getItemAt(candidate2, k-1);
							newCandidate = newCandidate.concat(" ");
							newCandidate = newCandidate.concat(s1);
							newCandidate = newCandidate.concat(" ");
							newCandidate = newCandidate.concat(s2);
							tempCandidateList.addElement(newCandidate.trim());
						}
					}
				}
			}
		}
				
		return tempCandidateList;
	}
	
	// generate hash tree for candidates
	public HashTreeNode genCandidateHashTree(int k) {
		
		HashTreeNode htn = new HashTreeNode();
		
		if( 1 == k ) {
			htn.nodeAttr = 2;
		}
		else {
			htn.nodeAttr = 1;
		}
		
		int length = ((CandidateElement)candi.get(k-1)).candidateList.size();
		for(int i = 1; i<=length; i++) {
			String candidate1 = new String();
			candidate1 = (String) ((CandidateElement)candi.get(k-1)).candidateList.get(i-1);
			genHash(1, htn, candidate1);
		}
		return htn;
	}
	
	// Recursively create node in hash tree for candidates
	private void genHash(int i, HashTreeNode htn, String s) {
		// i is the recursive depth
		int n = itemsetSize(s);
		if( i == n) {
			htn.nodeAttr = 2;
			htn.nodeDepth = n;
			ItemsetNode isn = new ItemsetNode(s, 0); 
			if(htn.itemsetList == null) {
				htn.itemsetList = new Vector<ItemsetNode>();
			}
			htn.itemsetList.addElement(isn);
		}
		else {
			if(htn.hashTable==null) {
				htn.hashTable = new Hashtable<String, HashTreeNode>(1);
			}
			if(htn.hashTable.containsKey(getItemAt(s, i))) {
				htn = (HashTreeNode)htn.hashTable.get(getItemAt(s, i));
				genHash(i+1, htn, s);
			}
			else {
				HashTreeNode nhtn = new HashTreeNode();
				htn.hashTable.put(getItemAt(s, i), nhtn);
				if( i == n-1 ) {
					nhtn.nodeAttr = 2;
					genHash(i+1, nhtn, s);
				}
				else {
					nhtn.nodeAttr = 1;
					genHash(i+1, nhtn, s);
				}
			}
		}		
	}
	
	
	// Find frequent intemset
	public void genFrequentItemset(int k) {
		Vector candList = new Vector();
		Vector<String> largeis = new Vector<String>(); //is short for itemset!
		HashTreeNode htn = new HashTreeNode();
		
		candList = ((CandidateElement)candi.get(k-1)).candidateList;
		htn = ((CandidateElement)candi.get(k-1)).hashTreeNode;
		getFrequentHash(0, htn, fullItemset, largeis);
		
		largeItemset.addElement(largeis);
	}
	
	// Recursively traverse the candidate hash tree
	// if count > MINSUP * num of lines
	public void getFrequentHash(int i, HashTreeNode htn, String s, Vector<String> lis) {
		Vector tempv = new Vector();
		if(htn.nodeAttr == 2) {
			tempv = htn.itemsetList;
			for(int j = 1; j<=tempv.size(); j++) {
				if (((ItemsetNode)tempv.get(j-1)).count >= MINSUP * LINES) {
					String frequentString = ((ItemsetNode)tempv.get(j-1)).itemset;
					lis.addElement(frequentString);
					itemsetVector.addElement(frequentString);
					numVector.addElement(((ItemsetNode)tempv.get(j-1)).count/1000.0);
				}
			}
		}
		else { // HashTable node
			if(htn.hashTable == null) 
				return;
			for(int t = i+1; t<=NUM; t++) {
				if(htn.hashTable.containsKey(getItemAt(s, t))) {
					getFrequentHash(t, (HashTreeNode)htn.hashTable.get((getItemAt(s, t))), s, lis);
				}
			}
		}
	}
	
	// read file and traverse the hash tree
	// count + 1 when find the itemset in all transactions
	public void traverse(int n) throws IOException {
		File file = new File("assignment2-data.txt");
		String lines = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		lines = br.readLine(); //first line ignored
		int i = 0, j = 0;
		String transaction;
		HashTreeNode htn = new HashTreeNode();
		StringTokenizer st;
		String s0;
		
		htn=((CandidateElement)candi.get(n-1)).hashTreeNode;
		
		while(true) {
			transaction = new String();
			String line = br.readLine();
			if (line == null)
				break;
			st = new StringTokenizer(line.trim());
			j = 0;
			while(st.hasMoreTokens() && j<NUM) {
				j++;
				s0 = st.nextToken();
				i = Integer.valueOf(s0).intValue();
				if( i != 0) {
					transaction = transaction.concat(" ");
					transaction = transaction.concat(Integer.toString(j));
				}
			}
			transaction = transaction.trim();
			transactionHash(0, htn, transaction);
		}		
	}
	
	// recursively traverse hash tree
	private void transactionHash(int i, HashTreeNode htn, String transaction) {
		Vector itemsetList = new Vector();
		int length;
		String temp;
		ItemsetNode tempNode = new ItemsetNode();		
		StringTokenizer st;
		
		if(htn.nodeAttr == 2) {
			itemsetList = (Vector)htn.itemsetList;
			length = itemsetList.size();
			for(int j = 0; j<length; j++) {
				st = new StringTokenizer(transaction);
				tempNode = (ItemsetNode)itemsetList.get(j);
				temp = getItemAt(tempNode.itemset, htn.nodeDepth);
				while(st.hasMoreTokens()) {
					if(st.nextToken().compareToIgnoreCase(temp) == 0) {
						((ItemsetNode)itemsetList.get(j)).count++;
					}
				}
			}
			return;
		}
		else { //HashTable node
			for(int t = i+1; t<=itemsetSize(transaction); t++) {
				if(htn.hashTable.containsKey((getItemAt(transaction, t)))){
					transactionHash(i, (HashTreeNode)htn.hashTable.get(getItemAt(transaction, t)), transaction);
				}
			}
		}
	}
	
	// get the index-th Item of an itemset
	private String getItemAt(String s, int index) {
		String temp = new String();
		StringTokenizer st = new StringTokenizer(s);
		
		if(index > st.countTokens()) {
			System.exit(-1);
		}
		
		for(int j = 1; j<=index; j++) {
			temp = st.nextToken();
		}
		
		return temp;
	}
	
	// get the size of a certain itemset
	public int itemsetSize(String s) {
	    StringTokenizer st=new StringTokenizer(s);
	    return st.countTokens();
	}
	
}
