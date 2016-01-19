import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class IKAnalyse {
	
	public IKAnalyse() {
		
	}
	
	public Vector<String> splitString(String text) throws IOException {
		Vector<String> vector = new Vector<String>();
		Reader reader = new StringReader(text);
		IKSegmenter segmenter = new IKSegmenter(reader, true);
		Lexeme lexeme;
		while((lexeme = segmenter.next())!=null) {
			vector.add(lexeme.getLexemeText());
		}
		return vector;
	}
	
	public void vectorResultDisplay(Vector<String> v) {
		int size = v.size();
		for(int i = 0; i<size; i++) {
			System.out.println(v.get(i));
		}
		
	}
}
