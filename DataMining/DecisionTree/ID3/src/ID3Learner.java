import java.io.IOException;
import java.util.*;

public class ID3Learner {
	String fileName;
	int start;
	int end;
	ArrayList<Double> splitValue;
	/**
	 * @param fileName
	 * @param percentage
	 */
	public ID3Learner(String fileName, int start, int end) {
		this.fileName = fileName;
		this.start = start;
		this.end = end;
	}
	
	public TreeNode startLearning() throws IOException {
		MatrixData matrix = new MatrixData();
		matrix.loadMatrix(fileName, start, end, 1);
		
		HashMap<String, int[]> setTrainingVector = new HashMap<String, int[]>();
		for(int i = 0; i<matrix.getColumns()-1; i++) {
			int[] trainingVector =  new int[matrix.getNum()];
			matrix.fillArray(trainingVector, i);
			setTrainingVector.put(matrix.getHeaders().get(i), trainingVector);
		}
		
		int[] finalClass = new int[matrix.getNum()];
		matrix.fillArray(finalClass, matrix.getColumns()-1);
		
		//generate a tree node
		TreeNode root = new TreeNode();
		root.setAttrDisValue(-1);
		learnTree(setTrainingVector, finalClass, root, matrix);
		
		splitValue = matrix.getSplitValue();
		return root;
	}

	private void learnTree(HashMap<String, int[]> setTrainingVector, int[] finalClass, TreeNode root,
			MatrixData matrix) {
		// if all is 0 or all is 1, leaf node
		if(checkFinalClass(finalClass, 0)) {
			root.setClassFlag(0);
			return;
		}
		else if(checkFinalClass(finalClass, 1)) {
			root.setClassFlag(1);
			return;
		}
		
		// if only one attribute, select the finalclass as max occurance
		if(setTrainingVector.keySet().size() == 1) {
			int cPos = getCountPositives(finalClass);
			int cNeg = finalClass.length-cPos;
			if(cPos>=cNeg) {
				root.setClassFlag(0);
				return;
			}
			else {
				root.setClassFlag(1);
				return;
			}
		}
		else {
			// use gain to decide
			HashMap<String, Double> attributesGains = new HashMap<String, Double>();
			HashMap<String, ArrayList<Integer>> mapAttributesValuesInListUnique = new HashMap<String, ArrayList<Integer>>();
			double entropyS =  getEntropy(finalClass);
			
			for(@SuppressWarnings("rawtypes") Map.Entry entry: setTrainingVector.entrySet()) {
				HashMap<Integer, Integer> attrPositive = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> attrNegative = new HashMap<Integer, Integer>();
				ArrayList<Integer> attrUnique = new ArrayList<Integer>();
				
				int[] trainingClass = (int[]) entry.getValue();
				for(int i = 0; i<trainingClass.length; i++) {
					addOnlyUnique(attrUnique, trainingClass[i]);
					if(finalClass[i] == 0) { //positive
						if(attrPositive.containsKey(trainingClass[i])) {
							attrPositive.put(trainingClass[i], attrPositive.get(trainingClass[i])+1);
						}
						else {
							attrPositive.put(trainingClass[i], 1);
						}
					}
					else {
						if(attrNegative.containsKey(trainingClass[i])) {
							attrNegative.put(trainingClass[i], attrNegative.get(trainingClass[i])+1);
						}
						else {
							attrNegative.put(trainingClass[i], 1);
						}
					}
				}
				mapAttributesValuesInListUnique.put((String)entry.getKey(), attrUnique);
			
				// calculate gain
				double gain = entropyS;
				for(int tempAttr : attrUnique) {
					double entropyTemp = 0.0;
					int positives = 0;
					int negatives = 0;
					if (attrPositive.get(tempAttr) != null)
						positives = attrPositive.get(tempAttr);
					if (attrNegative.get(tempAttr) != null)
						negatives = attrNegative.get(tempAttr);
					double val1 = (double)(positives) / (positives+negatives);
					double val2 = (double)(negatives) / (positives+negatives);
					entropyTemp = -(val1* log2(val1))-(val2*log2(val2));
					gain = gain - entropyTemp*(((double) positives + negatives) / trainingClass.length);
					attributesGains.put((String) entry.getKey(), gain);
				}
			}
			
			//select max gain
			String attributeMaxGain = "";
			double maxGainValue = 0.0;
			int indexToChoose = 0;
			for(@SuppressWarnings("rawtypes") Map.Entry entry : setTrainingVector.entrySet()) {
				double tempGain = attributesGains.get((String)entry.getKey());
				if(indexToChoose == 0) {
					maxGainValue = tempGain;
					attributeMaxGain = (String) entry.getKey();
					indexToChoose++;
				}
				if(tempGain > maxGainValue) {
					maxGainValue = tempGain;
					attributeMaxGain = (String) entry.getKey();
				}
			}
			
			// 
			root.setAttributeName(attributeMaxGain);
			root.setClassFlag(-1);
			root.setGain(maxGainValue);
			// recursively
			ArrayList<Integer> attrUniqueValuesForAttrMaxGain = mapAttributesValuesInListUnique.get(attributeMaxGain);
			for(int tempAttrUniqueValue : attrUniqueValuesForAttrMaxGain) {
				TreeNode nodeChild = new TreeNode();
				nodeChild.setAttrDisValue(tempAttrUniqueValue);
				root.getBranches().add(nodeChild);
				MatrixData matrixChild = matrix.splitMatrix(attributeMaxGain, tempAttrUniqueValue);
				HashMap<String, int[]> setTrainingVectorChild = new HashMap<String, int[]>();
				for (int i = 0; i < matrixChild.getColumns() - 1; i++) {
					int[] trainingVectorChild = new int[matrixChild.getNum()];
					matrixChild.fillArray(trainingVectorChild, i);
					setTrainingVectorChild.put(matrixChild.getHeaders().get(i), trainingVectorChild);
				}
				int[] finalClassChild = new int[matrixChild.getNum()];
				matrixChild.fillArray(finalClassChild, matrixChild.getColumns() - 1);

				learnTree(setTrainingVectorChild, finalClassChild, nodeChild, matrixChild);
			}
			return;
		}
	}
	
	public boolean checkFinalClass(int[] finalClass, int valueToChecked) {
		for (int i = 0; i < finalClass.length; i++) {
			if (finalClass[i] != valueToChecked)
				return false;
		}
		return true;
	}
	
	public int getCountPositives(int[] finalClass) {
		int countPos = 0;
		for (int i = 0; i < finalClass.length; i++) {
			if (finalClass[i] == 0)
				countPos++;
		}
		return countPos;
	}
	
	public double getEntropy(int[] vector) {
		double entropy = 0.0;
		int positives = 0;
		int negatives = 0;
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] == 0) { // its a positive
				positives++;
			}
			else { // finalClass is negative
				negatives++;
			}
		}
		double val1 = (double) (positives) / (positives + negatives);
		double val2 = (double) (negatives) / (positives + negatives);
		entropy = -(val1 * log2(val1)) - (val2 * log2(val2));
		return entropy;
	}
	
	private double log2(double d) {
		if(d<=0)
			return 0.0;
		else 
			return (Math.log(d)/Math.log(2));
	}
	
	public void addOnlyUnique(ArrayList<Integer> data, int val) {
		if (!data.contains(val))
			data.add(val);
	}
	
	public ArrayList<Double> getSplitValue() {
		return splitValue;
	}
	
}
