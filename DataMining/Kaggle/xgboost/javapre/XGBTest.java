import org.dmlc.xgboost4j.DMatrix;
import org.dmlc.xgboost4j.util.XGBoostError;
import org.dmlc.xgboost4j.Booster;
import org.dmlc.xgboost4j.util.Trainer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public class XGBTest {

	public static void main(String[] args) throws XGBoostError, IOException {
		
		System.out.println("Generating formatted training and testing files...");
		XGBDriver xgbDriver = new XGBDriver();
		xgbDriver.genTrainData();
		xgbDriver.genTestData();
		
		/*
		System.out.println("Loading training and testing files...");
		DMatrix trainData = new DMatrix("trainsvm.txt");
		DMatrix testData = new DMatrix("testsvm.txt");
		
		System.out.println("Training...");
		Iterable<Entry<String, Object>> params = new Params() {
			{
			    put("eta", 0.075);
			    put("max_depth", 10);
			    put("gamma", 0.7);
			    put("silent", 1);
			    put("objective", "reg:linear");
			    put("subsample", 0.99);
			    put("nthread", 6);
			}
		};
			
		List<Entry<String, DMatrix>> watchList =  new ArrayList<>();
	    watchList.add(new SimpleEntry<>("train", trainData));
	    watchList.add(new SimpleEntry<>("test", testData));
	     
	    int num_of_rounds = 10000;
	     
	    Booster booster = Trainer.train(params, trainData, num_of_rounds, watchList, null, null);
	    
	    System.out.println("Predicting...");
	    BufferedWriter predictWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("xgb"+num_of_rounds+".csv")));
	    predictWriter.write("\"Id\",\"Response\"\n");
	    float[][] predicts = booster.predict(testData);
	    ArrayList<Integer> testId = xgbDriver.getTestIds();
	    int idIndex = 0;
	    for(float[] x:predicts) {
	    	for(int i = 0; i<x.length; i++) {
	    		predictWriter.write(testId.get(idIndex)+","+(int)(1+x[i])+"\n");
	    		predictWriter.flush();
	    	}
	    	idIndex++;
	    }
	    predictWriter.write(params.toString()+"num_of_rounds:"+num_of_rounds+"\n");
	    predictWriter.close();
	    System.out.println("Finished");
	    */
	}

}
