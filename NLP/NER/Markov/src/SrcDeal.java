import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SrcDeal {
	
	public static final String SENTIMENT_URL = "http://api.bosonnlp.com/tag/analysis?space_mode=0&oov_level=3&t2s=0";
	public static final String MY_TOKEN = "Cu9yNicK.4330.pQ03SpUwWCuN";
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String file  = "result";
		ArrayList<String> entityList = new ArrayList<String>();
		String timeE = "time";
		String locationE = "location";
		String person_nameE = "person_name";
		String org_nameE = "org_name";
		String company_nameE = "company_name";
		String product_nameE = "product_name";
		entityList.add(timeE);
		entityList.add(locationE);
		entityList.add(person_nameE);
		entityList.add(org_nameE);
		entityList.add(company_nameE);
		entityList.add(product_nameE);
		for(int i = 1; i<=5; i++) {
			String tempName = file+String.valueOf(i)+".txt";
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempName)));
			int[] count = new int[6];
			int[] correct = new int[6];
			while(true) {
				String line = br.readLine();
				if(line == null)
					break;
				if(line.length()>10) {
					String[] parts = line.split("\t");
					if(parts.length == 4) {
						if(!parts[2].equals("Other")) {
							int index = entityList.indexOf(parts[2]);
							count[index]++;
							if(parts[2].equals(parts[3])) {
								correct[index]++;
							}
						}
					}
				}
				
			}
			for(int j = 0; j<6; j++) {
				// System.out.print(entityList.get(j)+"\t");
				System.out.println((double) correct[j]/count[j]);
			}
			
			br.close();
		}
		
	
	}
	
	public static void httpRequest() throws UnirestException, IOException {
		Pattern p = Pattern.compile("\"word\":\\[(.*?)\\]");
		Pattern tag = Pattern.compile("\"tag\":\\[(.*?)\\]");
		Pattern pp = Pattern.compile("\"(.*?)\"");
		String body = new JSONArray(new String[]{"各项配置和新增功能值得用户期待，预计上市时间已经不远，各类车型也将会陆续推出。",
				"国内居民消费价格指数（CPI）上涨到6%以上，已经持续三个月了"}).toString();
        HttpResponse<JsonNode> jsonResponse = Unirest.post(SENTIMENT_URL)
            .header("Accept", "application/json")
            .header("X-Token", MY_TOKEN)
            .body(body)
            .asJson();

        System.out.println(jsonResponse.getBody());
        String x = jsonResponse.getBody().toString();
        Matcher m = p.matcher(x);
		ArrayList<String> splitWords = new ArrayList<String>();
        while (m.find()) {
            splitWords.add(m.group(1));            
        }
        for(String splitW : splitWords) {
			ArrayList<String> list = new ArrayList<String>();
			Matcher mm = pp.matcher(splitW);
			while(mm.find()) {
				String temp = mm.group(1);
				list.add(temp);
			}
			System.out.println(list);
        }
		        
        Unirest.shutdown();
	}
				
}
