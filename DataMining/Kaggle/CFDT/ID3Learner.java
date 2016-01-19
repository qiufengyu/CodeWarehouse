import java.io.IOException;
import java.util.*;

public class ID3Learner {
	public ID3Learner() {
		
	}
	
	public TreeNode startLearning(String fileName) throws IOException {
		MatrixData matrix = new MatrixData();
		matrix.loadMatrix(fileName,1);
		
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
		return root;
	}

	private void learnTree(HashMap<String, int[]> setTrainingVector, int[] finalClass, TreeNode root,
			MatrixData matrix) {
		// if all is 0 or all is 1, leaf node
		if(checkFinalClass(finalClass, 1)) {
			root.setClassFlag(1);
			return;
		}
		else if(checkFinalClass(finalClass, 2)) {
			root.setClassFlag(2);
			return;
		}
		else if(checkFinalClass(finalClass, 3)) {
			root.setClassFlag(3);
			return;
		}
		else if(checkFinalClass(finalClass, 4)) {
			root.setClassFlag(4);
			return;
		}
		else if(checkFinalClass(finalClass, 5)) {
			root.setClassFlag(5);
			return;
		}
		else if(checkFinalClass(finalClass, 6)) {
			root.setClassFlag(6);
			return;
		}
		else if(checkFinalClass(finalClass, 7)) {
			root.setClassFlag(7);
			return;
		}
		else if(checkFinalClass(finalClass, 8)) {
			root.setClassFlag(8);
			return;
		}
		
		// if only one attribute, select the finalclass as max occurance
		if(setTrainingVector.keySet().size() == 1) {
			int[] vals = new int[8];
			for(int i = 0; i<8; i++)
				vals[i] = 0;
			for(int j = 0; j<finalClass.length; j++) {
				int index = finalClass[j]-1;
				vals[index]++;
			}
			int max = -1;
			int maxIndex = 7;
			for(int x = 0; x<8; x++) {
				if(vals[x]>=max) {
					max = vals[x];
					maxIndex = x;
				}
			}
			root.setClassFlag(maxIndex+1);	
		}
		else {
			// use gain to decide
			HashMap<String, Double> attributesGains = new HashMap<String, Double>();
			HashMap<String, ArrayList<Integer>> mapAttributesValuesInListUnique = new HashMap<String, ArrayList<Integer>>();
			double entropyS =  getEntropy(finalClass);
			
			for(@SuppressWarnings("rawtypes") Map.Entry entry: setTrainingVector.entrySet()) {
				ArrayList<HashMap<Integer, Integer>> attrs = new ArrayList<HashMap<Integer, Integer>>();
				for(int i = 0; i<8; i++) {
					HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
					attrs.add(temp);
				}
				ArrayList<Integer> attrUnique = new ArrayList<Integer>();
				
				int[] trainingClass = (int[]) entry.getValue();
				for(int i = 0; i<trainingClass.length; i++) {
					addOnlyUnique(attrUnique, trainingClass[i]);
					int x = finalClass[i]-1;
					if(attrs.get(x).containsKey(trainingClass[i])) {
						attrs.get(x).put(trainingClass[i], attrs.get(x).get(trainingClass[i])+1);
					}
					else
						attrs.get(x).put(trainingClass[i], 1);
				}
				mapAttributesValuesInListUnique.put((String)entry.getKey(), attrUnique);
			
				// calculate gain
				double gain = entropyS;
				for(int tempAttr : attrUnique) {
					double entropyTemp = 0.0;
					int[] val= new int[8];
					for(int i = 0; i<8; i++)
						val[i] = 0;
					for(int j = 0; j<8; j++) {
						if(attrs.get(j).get(tempAttr) != null) {
							val[j] = attrs.get(j).get(tempAttr);
						}
					}
					int sum = 0;
					for(int x = 0; x<8; x++) {
						sum+= val[x];
					}
					double[] doublevals = new double[8];
					for(int t = 0; t<8; t++) {
						doublevals[t] = (double)(val[t]) / sum;
						entropyTemp += (-doublevals[t]*(log2(doublevals[t])));
					}
					gain = gain - entropyTemp*(((double) sum) / trainingClass.length);
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
		int listSize = vector.length;
		int[] temp = new int[8];
		for(int i = 0; i<8; i++) {
			temp[i]=0;
		}
		for(int j = 0; j<listSize; j++) {
			int indexVal = vector[j]-1;
			temp[indexVal]++;
		}
		double[] tempE = new double[8];
		double en = 0.0;
		for(int k = 0; k<8; k++) {
			tempE[k] = (double) temp[k]/(double) listSize;
			en += (-(tempE[k]*log2(tempE[k])));
		}
		return en;
		
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
	
}
