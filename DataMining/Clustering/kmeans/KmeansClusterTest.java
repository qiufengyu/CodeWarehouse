import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

public class KmeansClusterTest {
	
	Vector<Vector<Double>> srcData;
	int[] label;
	int[] mylabel;
	Vector<Double>[] center;
	Vector<Double>[] center2;
	int[] num;
	double[][] sumX;
	
	private final static int K = 2;
	private final static int D = 24;
	private final static int M = 1000;
	
	/* dataset 2
	private final static int K = 10;
	private final static int D = 784;
	private final static int M = 10000;
	 */
	
	@SuppressWarnings("unchecked")
	public KmeansClusterTest() throws IOException {
		srcData = new Vector<Vector<Double>>();
		label = new int[M];
		mylabel = new int[M];
		center = new Vector[K];
		center2 = new Vector[K];
		num = new int[K];
		sumX = new double[K][D];
		for(int i = 0; i<K; i++) {
			num[i] = 0;
			for(int j = 0; j<D; j++) 
				sumX[i][j] = 0.0;
		}
		fileLoad();
	}
	
	private void fileLoad() throws IOException {	
		// for dataset2 
		// File src = new File("mnist.txt");
		File src = new File("german.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src)));
		int count = 0;
		while(true) {
			String line = br.readLine();
			if (line == null)
				break;
			Vector<Double> innerVec = new Vector<Double>();
			String[] v = line.split(",");
			for(int i = 0; i<v.length-1; i++) {
				innerVec.addElement(Double.valueOf(v[i]));
			}
			label[count++] = Integer.valueOf(v[v.length-1]);
			srcData.addElement(innerVec);
		}
		br.close();
	}
	
	// Euclidean Distance
	private double dist(Vector<Double> v1, Vector<Double> v2) {
		if(v1.size() != v2.size()) {
			System.out.println("!!");
			return 0.0;
		}
		int len = v1.size();
		double sum = 0.0;
		for(int i = 0; i<len; i++) {
			sum += Math.pow(v1.get(i)-v2.get(i), 2);
		}
		return Math.sqrt(sum);
	}
	
	private void createRandomStart() {
		for(int i = 0; i<K; i++) {
			Vector<Double> inner = new Vector<Double>();
			for(int j = 0; j<D; j++) {
				inner.addElement(Math.random());
			}
			center[i] = inner;
		}
	}
	
	
	public void test() throws IOException {
		createRandomStart();
		boolean flag = false;
		int it = 0;
		while(!flag) {
			++it;
			// System.out.println(++it);
			// center[j]: old center
			// center2[j]: new center
			// which one closer?
			for(int i = 0; i<M; i++) {
				double min = dist(center[0],srcData.get(i));
				mylabel[i] = 0;
				for(int j = 1; j<K; j++) {
					double current = dist(center[j], srcData.get(i));
					if(current < min) {
						min = current;
						mylabel[i] = j;
					}
				}
			}
			// gen new center
			// clear first
			for(int i = 0; i<K; i++) {
				num[i] = 0;
				for(int j = 0; j<D; j++) 
					sumX[i][j] = 0.0;
			}
			// cal sumX
			for(int k = 0; k<K; k++) {
				for(int t = 0; t<M; t++) {
					if(mylabel[t] == k) {
						num[k]++;
						for(int x = 0; x<D; x++) {
							sumX[k][x] += srcData.get(t).get(x);
						}
					}
				}
			}
			// set new centers
			for(int k = 0; k<K; k++) {
				for(int x = 0; x<D; x++) {
					sumX[k][x] = (double)sumX[k][x]/num[k];
				}
				Vector<Double> v = new Vector<Double>();
				for(int y = 0; y<D; y++) {
					v.addElement(sumX[k][y]);
				}
				center2[k] = v;
			}
			
			// if stop?
			flag = true;
			for(int k = 0; k<K; k++) {				
				flag = flag && dist(center[k], center2[k])<1e-8;
				center[k] = center2[k];
			}
			// max iterator: 2000 times
			if(it>2000)
				break;
		}
		writeResults();
	}
	
	public void writeResults() throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("result.txt"),"UTF-8"));
		for(int aa = 0; aa<mylabel.length; aa++) {
			bw.write((mylabel[aa]+1)+"\n");
			bw.flush();
		}		
		bw.close();
		/*
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ground.txt"),"UTF-8"));
		for(int aa = 0; aa<label.length; aa++) {
			bw2.write(label[aa]+"\n");
			bw2.flush();
		}		
		bw2.close();
		*/		
	}
}
