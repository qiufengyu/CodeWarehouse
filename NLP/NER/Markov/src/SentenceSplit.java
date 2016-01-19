import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SentenceSplit {
	final String SENTIMENT_URL = "http://api.bosonnlp.com/tag/analysis?space_mode=0&oov_level=3&t2s=0";
	final String MY_TOKEN = "YOUR_TOKEN";
	
	ArrayList<String> splitWords;
	ArrayList<String> splitTags;
	
	public SentenceSplit() {
		splitWords = new ArrayList<String>();
		splitTags = new ArrayList<String>();
	}
	
	public void splitWords(String[] args) throws UnirestException, IOException {
		splitWords.clear();
		splitTags.clear();
		Pattern word = Pattern.compile("\"word\":\\[(.*?)\\]");
		Pattern tag = Pattern.compile("\"tag\":\\[(.*?)\\]");
		Pattern pp = Pattern.compile("\"(.*?)\"");
		String body = new JSONArray(args).toString();
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SENTIMENT_URL)
            .header("Accept", "application/json")
            .header("X-Token", MY_TOKEN)
            .body(body)
            .asJson();

      //   System.out.println(jsonResponse.getBody());
        String x = jsonResponse.getBody().toString();
        Matcher mWord = word.matcher(x);
        Matcher mTag = tag.matcher(x);
        String wordList = new String();
        String tagList = new String();
        while (mWord.find()) {
            wordList = mWord.group(1);            
        }
        while(mTag.find()) {
        	tagList = mTag.group(1);
        }
        
        Matcher mWordList = pp.matcher(wordList);
        Matcher mTagList = pp.matcher(tagList);
        while(mWordList.find()) {
        	splitWords.add(mWordList.group(1));
        }
        while(mTagList.find()) {
        	splitTags.add(mTagList.group(1));
        }
		        
        // Unirest.shutdown();
	
	}

	public ArrayList<String> getSplitWords() {
		return splitWords;
	}

	public void setSplitWords(ArrayList<String> splitWords) {
		this.splitWords = splitWords;
	}

	public ArrayList<String> getSplitTags() {
		return splitTags;
	}

	public void setSplitTags(ArrayList<String> splitTags) {
		this.splitTags = splitTags;
	}
	
	
				
}
