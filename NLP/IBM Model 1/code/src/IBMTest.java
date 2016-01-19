/**
 * IBM Language Translation Model 1, a simple implementation
 */
import java.io.IOException;

public class IBMTest {
	
	private static int MAX_IT = 100;

	public static void main(String[] args) throws IOException {
		final long t1 = System.currentTimeMillis();
		// Initialize the filter Set
		Decoder.initSet();
		
		IBMDriver driver = new IBMDriver();
		driver.model("test.en.txt", "test.ch.txt", MAX_IT);
		
		IBMInverse inverse = new IBMInverse();
		inverse.modelInverse("test.en.txt", "test.ch.txt", MAX_IT);
		
		Decoder.decode(driver.getProbT(), inverse.getProbTInverse(), "test.en.txt", "test.ch.txt", "myalignment.txt");
		
		Decoder.evaluate("test.align.txt", "myalignment.txt");
		
		final long t2 = System.currentTimeMillis();
		
		System.out.println("Time = "+(t2-t1)+" ms with "+MAX_IT+" iterations.");
	}

}
