import java.io.*;
import java.util.*;

import org.tartarus.snowball.ext.*;

public class TagsAndContent {

	private Map<String, Vector<String>> tagsContentsMap;
	// private String tag;
	// private Set<String> contentSet;
	private Set<String> filterSet;
	//private Set<String> allWordsSet;
	private Set<String> tagSet;
	private Map<String, Integer> tagMap;
	private Set<String> wordSet;
	private Map<String, Integer> wordMap;
	private Map<Integer, String> getTagMap;
	private int tagsNum;
	private int wordsNum;
	private int [][]countMatrix;
	private int []countik;
	private int []countkj;
	private double [][]pw;
	private final double threshold = 440.5;
	private final int maxLables = 6;
	private final int minLables = 3;
	private int testNum;

	public TagsAndContent() throws Exception {
		/*
		Date date = new Date();
		System.out.println(date);
		System.out.println("-------------------------------------");
		*/
		// train();
		
		Date date1 = new Date();
		System.out.println(date1);
		System.out.println("-------------------------------------");
		test_init();
	}
	
	public int[] test_one(String testtext) throws IOException {	
		//String testtext = "A  Defect  Tracking System. This aims at providing a user friendly interface for users to track the  defects  encountered while developing a software. Created using PHP, JGraph, JavaScript and MySQL RDBMS on Linux Platform with Apache.";
		String []temp = testtext.split("[\\W]+");
		Vector<String> vec = new Vector<String>();
		for (int j = 0; j < temp.length; j++) {
			String s = temp[j].toLowerCase();
			if (s.length() >= 2 && (!filterSet.contains(s)) && s.length() <= 20 && s.charAt(0)>='a' && s.charAt(0)<='z') {
				englishStemmer stemmer = new englishStemmer();
				stemmer.setCurrent(s);
					if (stemmer.stem()){
						vec.add(stemmer.getCurrent());
				}
			}
		}
		double []ps = new double[354];
		for(int z = 0; z<354; z++) {
			ps[z] = 0.0;
		}
		for(int y = 0; y<354; y++) {
			for(int yy = 0; yy<vec.size(); yy++) {
				int index = getWordIndex(vec.get(yy));
				if(index != -1) {
					ps[y] += pw[y][index];
				}
			}
		}
		//System.out.println("Cal end...\n");
		int[] indexArray = new int[354];
		int i = 0;
		int indexOfArray = 0;
		for(i = 0; i<354; i++) {
			indexArray[i] = -1;
			if(ps[i]>threshold) {
				indexArray[indexOfArray] = i;
				indexOfArray++;
			}
		}
		if(indexOfArray >= maxLables) {
			//indexOfArray = 0;
			//Select highest 6 labels
			int maxIndex = 0;
			double maxValue = -1.0;
			for(int times = 0; times<maxLables; times++) {
				int k = 0;
				maxIndex = 0;
				maxValue = -1.0;
				for(k = 0; k<354; k++) {
					if(ps[k]>maxValue) {
						maxValue = ps[k];
						maxIndex = k;
					}
				}
				indexArray[times] = maxIndex;
				//indexOfArray++;
				ps[maxIndex] = -1.0;
			}
			quickSort(indexArray, 0, maxLables);
			for(i = maxLables; i<354; i++) {
				indexArray[i] = -1;
			}
		}
		if(indexOfArray < minLables) {
			int maxIndex = 0;
			double maxValue = -1.0;
			for(int times = 0; times<minLables; times++) {
				int k = 0;
				maxIndex = 0;
				maxValue = -1.0;
				for(k = 0; k<354; k++) {
					if(ps[k]>maxValue) {
						maxValue = ps[k];
						maxIndex = k;
					}
				}
				indexArray[times] = maxIndex;
				//indexOfArray++;
				ps[maxIndex] = -1.0;
			}
			quickSort(indexArray, 0, minLables);
			for(i = minLables; i<354; i++) {
				indexArray[i] = -1;
			}
		}
		testNum++;
		if(testNum %100 == 0) {
			System.out.println("Finished Test Case#"+testNum);
		} 
		return indexArray;
	} 
		
	
	private void test_init() throws IOException {
		System.out.println("Test Initializing...");
		//init!!!!
		initFilterSet();
		//Loading files wordlist into allWordsSet
		BufferedReader brwordlist = new BufferedReader(new FileReader("wordlist.txt"));
		
		wordMap = new HashMap<String, Integer>();
		while(true) {
			String line = brwordlist.readLine();
			if (line == null)
				break;
			String []temp = line.split(" ");
			int t = Integer.parseInt(temp[1]);
			wordMap.put(temp[0],t);		
		}
		BufferedReader brtaglist = new BufferedReader(new FileReader("taglist.txt"));
		tagMap = new HashMap<String, Integer>();
		getTagMap = new HashMap<Integer, String>();
		while(true) {
			String line1 = brtaglist.readLine();
			if (line1 == null)
				break;
			String []temp1 = line1.split(" ");
			int t1 = Integer.parseInt(temp1[1]);
			tagMap.put(temp1[0], t1);
			getTagMap.put(t1, temp1[0]);
		}
		pw = new double[354][21500];
		for(int i =0; i<354; i++) {
			for(int j = 0; j<21500; j++) {
				pw[i][j] = 0.0;
			}
		}
		tagsNum = tagMap.size();
		wordsNum = wordMap.size();
		testNum = 0;
		brtaglist.close();
		brwordlist.close();
		BufferedReader brpw = new BufferedReader(new FileReader("pwmatrix.txt"));		
		while(true) {
			String line2 = brpw.readLine();
			if (line2 == null)
				break;
			String[] value = line2.split(" ");
			int l = Integer.parseInt(value[0]);
			int c = Integer.parseInt(value[1]);
			double v = Double.parseDouble(value[2]);
			pw[l][c]=v;			
		}
		brpw.close();
		System.out.println("Testing loading over...");
	}
	
	
	private void train() throws IOException {
		//Initializing
		System.out.println("Initializing Training...");
		train_init();
		System.out.println("Starting calculate count matrix...");
		//calculate countMatrix
		calCountMatrix();
		System.out.println("Starting calculate countik and countkj...");
		//calculate for countik and countkj
		matrixCalSum();
		System.out.println("Starting calculate pw...");
		//calculate pw
		calPW();	
		System.out.println("Training finished...");
	}
	
	private void train_init() throws IOException {
		countMatrix = new int[354][21500];
		pw = new double[354][21500];
		for(int i =0; i<354; i++) {
			for(int j = 0; j<21500; j++) {
				countMatrix[i][j] = 0;
				pw[i][j] = 0.0;
			}
		}
		countik = new int[21500];
		countkj = new int[354];
		for(int a = 0; a<21500; a++) {
			countik[a]=0;
		}
		for(int b = 0; b<354; b++) {
			countkj[b]=0;
		}
		tagRead();
		tagsNum = tagSet.size();
		initFilterSet();
		initAllSet();		
		wordsNum = wordSet.size();
		System.out.println("Writing wordlist...");
		wordListOrder();
		System.out.println("Writing taglist...");
		tagListOrder();
	}

	private void tagRead() throws FileNotFoundException {
		tagSet = new TreeSet<String>();
		tagMap = new TreeMap<String, Integer>();
		getTagMap = new HashMap<Integer, String>();
		File sourceFile = new File(".\\trainingset\\allTags.txt");
		if (!sourceFile.exists()) {
			System.out.println("All Tags file does not exist");
			System.exit(0);
		}
		Scanner input = new Scanner(sourceFile);
		int index = 0;
		while (input.hasNext()) {
			String line = input.nextLine();
			String line1 = line.replace(' ', '_');
			tagSet.add(line1);
		}
		int st = tagSet.size();
		Iterator<String>it = tagSet.iterator();
		for(index = 0; index<st; index++) {
			String s = it.next();
			tagMap.put(s, index);
			getTagMap.put(index, s);
		}
		input.close();
	}

	private void initAllSet() throws IOException {
		tagsContentsMap = new TreeMap<String, Vector<String>>();
		tagsContentsMap.clear();
		wordMap = new TreeMap<String, Integer>();
		wordSet = new TreeSet<String>();
		BufferedReader input = new BufferedReader(new FileReader(".\\trainingset\\train.data"));
		
		while (true) {
			String line = input.readLine();
			if (line == null)
				break;
			// System.out.println(line);
			String[] item = line.split("#\\$#");
			String content = item[1];
			String[] words = content.split("[\\W]+");
			Vector<String> tempvector = new Vector<String>();
			tempvector.clear();
			for (int j = 0; j < words.length; j++) {
				String s = words[j].toLowerCase();
				if (s.length() >= 2 && (!filterSet.contains(s)) && s.length() <= 20 && s.charAt(0)>='a' && s.charAt(0)<='z') {
					englishStemmer stemmer = new englishStemmer();
					stemmer.setCurrent(s);
					if (stemmer.stem()){
						String sadd = stemmer.getCurrent();
						tempvector.add(sadd);
						wordSet.add(sadd);
					}
				}
			}
			
			String[] tempTags = item[2].split(","); 
			for(int i= 0; i<tempTags.length; i++) {
				String tagtemp = tempTags[i].replace(' ', '_');
				boolean flag = tagsContentsMap.containsKey(tagtemp); 
				if(flag) {
					Vector<String> tempvector0 = new Vector<String>(tempvector);
					tagsContentsMap.get(tagtemp).addAll(tempvector0); 
				} 
				else { //do not contain this tag 
					Vector<String> tempvector1 = new Vector<String>(tempvector);
					tagsContentsMap.put(tagtemp,tempvector1); 
				}
			}
		}//end of while
		//Re-Order wordList
		int sz = wordSet.size();
		Iterator<String> it = wordSet.iterator();
		for(int x = 0; x<sz; x++) {
			wordMap.put(it.next(), x);
		}
		input.close();
	}
	
	
	private int getWordIndex(String x) {
		Integer itemp = wordMap.get(x);
		if (itemp != null) {
			return (int)itemp;
		}
		else
			return -1;
	}
	
	public String getTag(int x) {
		return getTagMap.get(x);
	}
	
	public int getTagsSize() {
		return tagMap.size();
	}
	public int getAllWordsSize() {
		return wordMap.size();
	}
	
	private void wordListOrder() throws IOException {
		Set<String> wordSet = wordMap.keySet();
		Iterator<String> itWord = wordSet.iterator();
		BufferedWriter bw = new BufferedWriter(new FileWriter("wordlist.txt")); 
		for(int b =0; b<wordsNum; b++) {
			bw.write(itWord.next()+" "+b+"\n");
			bw.flush();
		}
		bw.close();
	
	}
	
	private void tagListOrder() throws IOException {
		Set<String> tagSet = tagMap.keySet();
		Iterator<String> itTag = tagSet.iterator();
		BufferedWriter bw = new BufferedWriter(new FileWriter("taglist.txt")); 
		for(int b =0; b<tagsNum; b++) {
			bw.write(itTag.next()+" "+b+"\n");
			bw.flush();
		}
		bw.close();
	}
	
	private void calCountMatrix() throws IOException {
		//Iterator<String> itT = tagSet.iterator();
		int i = 0;
		while(i<tagsNum) {
			String s = getTagMap.get(i);
			Vector<String> vec = tagsContentsMap.get(s);
			if(vec != null) {
				int vecsize = vec.size();
				int j = 0;
				for(j = 0; j<vecsize; j++) {
					int t = getWordIndex(vec.get(j));
					if(t >= 0) {
						countMatrix[i][t]++;
					}
				}
			}
			i++;
		}
		/*
		BufferedWriter bw = new BufferedWriter(new FileWriter("countmatrix.txt")); 
		for(int a = 0; a<tagsNum; a++) {
			for(int b =0; b<wordsNum; b++) {
				if(countMatrix[a][b] != 0) {
					bw.write(a+" "+b+" " +countMatrix[a][b]+"\n");
				}
				bw.flush();
			}
		}
		bw.close();
		*/
	
	}
	
	public void matrixCalSum() throws IOException {
		/*
		BufferedReader br = new BufferedReader(new FileReader("countmatrix.txt"));		
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] value = line.split(" ");
			int l = Integer.parseInt(value[0]);
			int c = Integer.parseInt(value[1]);
			int v = Integer.parseInt(value[2]);
			countMatrix[l][c]=v;			
		}
		br.close();
		*/
		//BufferedWriter bw1 = new BufferedWriter(new FileWriter("countkj.txt"));
		
		for(int i = 0; i<tagsNum; i++) {
			int suml = 0;
			for(int j = 0; j<wordsNum; j++) {
				suml += countMatrix[i][j];
			}
			countkj[i] = suml;
			//bw1.write(suml+"\n");
			//bw1.flush();
		}
		//bw1.close();
		//BufferedWriter bw2 = new BufferedWriter(new FileWriter("countik.txt"));
		
		for(int i = 0; i<wordsNum; i++) {
			int sumc = 0;
			for(int j = 0; j<tagsNum; j++) {
				sumc += countMatrix[j][i];
			}
			countik[i] = sumc; 
			//bw2.write(sumc+"\n");
			//bw2.flush();
		}		
		//bw2.close();		
	}
	
	private void calPW() throws IOException {
		/*
		BufferedReader br = new BufferedReader(new FileReader("countmatrix.txt"));		
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			String[] value = line.split(" ");
			int l = Integer.parseInt(value[0]);
			int c = Integer.parseInt(value[1]);
			int v = Integer.parseInt(value[2]);
			countMatrix[l][c]=v;			
		}
		br.close();
		BufferedReader br1 = new BufferedReader(new FileReader("countik.txt"));
		int i = 0;
		while(true) {
			String line1 = br1.readLine();
			if(line1 == null) 
				break;
			countik[i]=Integer.parseInt(line1);
			i++;
		}
		br1.close();
		BufferedReader br2 = new BufferedReader(new FileReader("countkj.txt"));
		int j = 0;
		while(true) {
			String line2 = br2.readLine();
			if(line2 == null) 
				break;
			countkj[j]=Integer.parseInt(line2);
			j++;
		}
		br2.close();
		*/
		int i = 0;
		int j = 0;
		//calculate PW matrix
		for(i = 0; i<tagsNum; i++) {
			for(j=0; j<wordsNum; j++) {
				if(countMatrix[i][j] != 0) {
					pw[i][j] = (double)countMatrix[i][j]*(double)countMatrix[i][j]/((double)countik[j]*(double)countkj[i]);
				}
			}
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("pwmatrix.txt")); 
		for(int a = 0; a<tagsNum; a++) {
			for(int b =0; b<wordsNum; b++) {
				if(pw[a][b]!=0.0) {
					bw.write(a+" "+b+" " +pw[a][b]*100000+"\n");
				}
				bw.flush();
			}
		}
		bw.close();		
		
	}	
	
	public void showTagVector(String s) {
		System.out.println(tagsContentsMap.get(s).size());
		System.out.println(tagsContentsMap.get(s));
		
	}
	
	//words not take into considerateion
	private void initFilterSet() throws IOException {
		filterSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader("elimlist.txt"));		
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;			
			filterSet.add(line);		
		}
		br.close();
	}
	
	@SuppressWarnings("unused")
	private int getTagIndex(String x) {
		return tagMap.get(x);
	}
	
	public int getcaseNum() {
		return testNum;
	}
	
	//subfunctions:quicksort and partition
	private void quickSort(int[] a, int start, int end) {
		int pivot = partition(a, start, end);
		if (start < end) {
			quickSort(a, start, pivot);
			quickSort(a, pivot + 1, end);
		}
	}
	
	private int partition(int[] a, int start, int end) {
		int i = start;
		int ele = a[start];
		for (int t = start+1; t <end; t++) {
			if (a[t] < ele) {
				i++;
				swap(a, t, i);
			}
		}
		swap(a, start, i);
		return i;
	}
	
	void swap(int[] a, int start, int end) {
		int temp = a[start];
		a[start] = a[end];
		a[end]= temp;
	}
	
}

