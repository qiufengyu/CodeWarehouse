import java.io.BufferedWriter;
import java.io.FileWriter;

import weka.classifiers.trees.J48;  
import weka.core.Instances;  
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class WekaDT {

	public static void main(String[] args) throws Exception {
		
		// WekaDriver.getWekaResult();
		
		// Generating formatted train and test files
		System.out.println("Generating formatted train and test files...");
		WekaDriver wekaDriver = new WekaDriver();
		wekaDriver.genArffFile();
		
		// Training data
		System.out.println("Loading training data...");
		
		DataSource sourceTrain = new DataSource("trainweka.arff");
		Instances train = sourceTrain.getDataSet();
		// Filtering
		String[] optionsRemove = new String[2];
		optionsRemove[0] = "-R";                                    // "range"
		optionsRemove[1] = "1";                                     // first attribute
		Remove remove = new Remove();                         // new instance of filter
		remove.setOptions(optionsRemove);                           // set options
		remove.setInputFormat(train);                          // inform filter about dataset **AFTER** setting options
		Instances instancesTrain = Filter.useFilter(train, remove);   // apply filter
		if (instancesTrain.classIndex() == -1)
			instancesTrain.setClassIndex(instancesTrain.numAttributes()-1);
		
		// Testing data
		System.out.println("Loading testing data...");
		
		DataSource sourceTest = new DataSource("testweka.arff");
		Instances test = sourceTest.getDataSet();
		remove.setInputFormat(test);
		Instances instancesTest = Filter.useFilter(test, remove);		
		if (instancesTest.classIndex() == -1)
			instancesTest.setClassIndex(instancesTest.numAttributes()-1);
		
		// result data
		Instances labeled = new Instances(test);
		if(labeled.classIndex() == -1)
			labeled.setClassIndex(labeled.numAttributes()-1);		
		
		String[] options = new String[1];
		options[0] = "-C 0.25 -M 4 -A";
		
		J48 tree = new J48();         // new instance of tree
		
		String treeType = tree.getClass().getName();
		String[] parts = treeType.split("\\.");
		String type = parts[parts.length-1];
		
		// building a classifier
		System.out.println("Building classifier with "+treeType+"...");
		
		tree.setOptions(options);     // set the options
		tree.buildClassifier(instancesTrain);   // build classifier		
		
		
		// predict for unlabeled	
		System.out.println("Predicting on test...");
		
		for(int i = 0; i<instancesTest.numInstances(); i++) {
			double clsLabel = tree.classifyInstance(instancesTest.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}
		
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(type+"labeled.csv"));
		writer.write("\"Id\",\"Response\"\n");
		for(int j = 0; j<labeled.numInstances(); j++) {
			writer.write(String.valueOf((int)labeled.instance(j).value(0))+",");
			writer.write(String.valueOf((int)labeled.instance(j).classValue()+1)+"\n");
			writer.flush();
		}
		writer.newLine();
		writer.flush();
		writer.close();
		
		System.out.println("Finished");
		
	}  


}
