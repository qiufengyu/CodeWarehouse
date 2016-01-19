import java.util.*;

public class TreeNode {
	private int attrDisValue;    // discrete values: 1-10, default: -1
	private double attrNumValue; // numerical values: double, default: -1.0
	private String attributeName;
	private ArrayList<TreeNode> branches; // children node

	private int classFlag; // -1 means not leaf, 0&1 accounts
	private double gain; // Info Gain for this attribute
	
	/**
	 * @param attrDisValue
	 * @param attrNumValue
	 * @param attributeName
	 * @param classFlag
	 * @param gain
	 */
	public TreeNode(int attrDisValue, double attrNumValue, String attributeName, int classFlag, double gain) {
		this.attrDisValue = attrDisValue;
		this.attrNumValue = attrNumValue;
		this.attributeName = attributeName;
		this.classFlag = classFlag;
		this.gain = gain;
	}
	
	/**
	 * branches
	 */
	public TreeNode() {
		branches = new ArrayList<TreeNode>();
	}
	
	public void printTree(TreeNode root, int depth) {
		if(root.classFlag == -1) {
			for(TreeNode temp: root.branches) {
				int i = 0;
				while(i < depth) {
					i++;
					System.out.print("| ");
				}
				System.out.print(root.attributeName);
				System.out.print(" = ");
				if(temp.classFlag == -1) {
					System.out.println(temp.attrDisValue+" ->");
				}
				else {
					System.out.print(temp.attrDisValue+" : ");
				}
				printTree(temp, depth+1);
			}			
		}
		else {
			System.out.println(root.classFlag);
		}
	}
	
	public int classifyTest(HashMap<String, Integer> testValues, TreeNode root) {
		int retVal = -1;
		TreeNode traverse = root;
		if(traverse.attributeName == null || traverse.attributeName.length() == 0) {
			return retVal;
		}
		while(traverse != null) {
			if(traverse.classFlag != -1) {
				retVal = traverse.classFlag;
				traverse = null;
				break;
			}
			else {
				String key = traverse.attributeName;
				if(testValues.containsKey(key)) {
					int value = testValues.get(key);
					if(traverse.branches.size() == 0) {
						break;
					}
					boolean valFound = false;
					for(TreeNode tempChild: traverse.branches) {
						if(tempChild.attrDisValue == value) {
							valFound = true;
							if(tempChild.classFlag != -1) {
								retVal = tempChild.classFlag;
								traverse = null;
								break;
							}
							else {
								traverse = tempChild;
								continue;
							}
						}
					}
					if(!valFound) {
						break;
					}
				}
				else {
					System.out.println("Not found Attr Name "+key);
				}
			}
		}
		
		return retVal;
	}
	

	public int getAttrDisValue() {
		return attrDisValue;
	}

	public void setAttrDisValue(int attrDisValue) {
		this.attrDisValue = attrDisValue;
	}

	public double getAttrNumValue() {
		return attrNumValue;
	}

	public void setAttrNumValue(double attrNumValue) {
		this.attrNumValue = attrNumValue;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public ArrayList<TreeNode> getBranches() {
		return branches;
	}

	public void setBranches(ArrayList<TreeNode> branches) {
		this.branches = branches;
	}

	public int getClassFlag() {
		return classFlag;
	}

	public void setClassFlag(int classFlag) {
		this.classFlag = classFlag;
	}

	public double getGain() {
		return gain;
	}

	public void setGain(double gain) {
		this.gain = gain;
	}
	
	
	
	
}
