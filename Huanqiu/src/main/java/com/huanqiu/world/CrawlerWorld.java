package com.huanqiu.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huanqiu.common.DBTools;
import com.huanqiu.common.News;

public class CrawlerWorld {
	private static final Logger logger = LogManager.getLogger(CrawlerWorld.class);
	static String urlHead="http://mil.huanqiu.com/world";
	URL indexPage;
	InputStreamReader inputStream;
	BufferedReader br;
	String temp;
	String buf;
	List<String> picList;
	
	//DB connection
	DBTools dbtool;
	public CrawlerWorld() {
		logger.debug("Initializing");
		try {
			dbtool = new DBTools();
			indexPage = new URL(urlHead.trim());
			inputStream = new InputStreamReader(indexPage.openStream(),"UTF-8");
			br = new BufferedReader(inputStream);
			buf = "";
			while((temp = br.readLine()) != null) {
				if(temp.trim().equals("<ul class=\"listPicBox\">")) {
					break;
				}
			}
			while((temp = br.readLine()) != null) {
				buf += temp.trim();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void runCrawler() throws UnsupportedEncodingException, IOException, SQLException {
		String[] results = buf.split("<li class=\"item\">");
		for(int i = 1; i<results.length; i++) { //results.length
			logger.debug("Deal with item["+i+"]");
			String t = results[i];
			Pattern patternURL = Pattern.compile("href=\"(.+?)\"");
			Matcher matcherURL = patternURL.matcher(t);
			if(matcherURL.find()) {
				News nInsert = dealItem(matcherURL.group(1));
				dbtool.insertValuesWorld(nInsert);
			}
		}
	}
	
	private News dealItem(String s) throws UnsupportedEncodingException, IOException {
		News n = new News();
		URL itemPage = new URL(s.trim());
		InputStreamReader istream = new InputStreamReader(itemPage.openStream(), "UTF-8");
		BufferedReader breader = new BufferedReader(istream);
		String header = "";
		String info = "";
		String buffer = "";
		String ttemp;
		while((ttemp = breader.readLine())!=null) {
			if(ttemp.trim().equals("</head>"))
				break;
			header += ttemp;
		}
		// Title
		String title = getTitle(header);
		n.setTitle(title);
		logger.debug("Title: "+title);
		// System.out.println("1. Title: "+title);
		// Agents	
		String agent = getAgent(header);
		//System.out.println("2. Agent: "+agent);
		n.setAgent(agent);
		
		while((ttemp = breader.readLine())!= null) {
			if(ttemp.trim().equals("<!-- 信息区 begin -->")) {
				break;
			}
		}
		
		while((ttemp = breader.readLine())!= null) {
			if(ttemp.trim().equals("<!-- 信息区 end -->"))
				break;
			info += ttemp;
		}		
		// Author	
		String author = getAuthor(info);
		// System.out.println("3. Author: "+author);
		n.setAuthor(author);
		
		// Date and time
		String dt = getDateTime(info);
		// System.out.println("4. Date and Time: " + dt);
		n.setUpdateTime(dt);
				
		while((ttemp = breader.readLine())!= null) {
			if(ttemp.trim().equals("<!--相关新闻 begain-->")) {
				break;
			}
			buffer += ttemp;
		}	
		
		//URL 
		// System.out.println("5. URL: "+s.trim());
		n.setUrl(s.trim());
		// buffer contains the content
		dealItemContent(buffer.trim(), s.trim(), n);
		return n;
	}
	
	private void dealItemContent(String s, String url, News n) throws UnsupportedEncodingException, IOException {
		// System.out.print("6. Content: ");
		picList = new ArrayList<String>();
		//Image
		String[] imgStart = s.split("<div id=\"atlas\" style=\"display:none;\">");
		if(imgStart.length>=2) {
			String[] imgDeal = imgStart[1].split("<img ");
			Pattern patternImg = Pattern.compile("src=\"(.+?)\" /><span>(.+?)</span>");
			for(int i = 1; i<imgDeal.length; i++) {
				Matcher matcherImg = patternImg.matcher(imgDeal[i].trim());
				if(matcherImg.find()) {
					picList.add(matcherImg.group(1));
				}
			}
		}
		
		//Next page
		Pattern patternNext = Pattern.compile("上一页(.+)?下一页");
		//Content
		String[] contentStart=s.split("<div class=\"text\" id=\"text\">");
		String c0 = contentStart[1].trim();		
		String[] contentPara = c0.split("<p>");
		String result="";
		for(int i = 0; i<contentPara.length; i++) {
			String x = contentPara[i];
			if(x.startsWith("<p ")) { // some pictures
				Pattern patternImgUrl = Pattern.compile("src=\"(.+?)\\.jpg\"");
				Matcher matcherImgUrl = patternImgUrl.matcher(x);
				if(matcherImgUrl.find()) {
					String picUrlItem = matcherImgUrl.group(1)+".jpg";
					picList.add(picUrlItem);
				}
			}
			else { //Real content
				// script
				if(i==0)
					continue;
				String c2 = x.replaceAll("<script[^>]*?>[\\s\\S]*?<\\/script>", "");
				// other html tags
				String c3 = c2.replaceAll("<[^>]+>", "");
				String c4 = c3.replaceAll("　　", "");
				String tttt = replaceHtml(c4);
				String c5 = tttt.trim();
				Matcher matcherNext = patternNext.matcher(tttt);
				if(!matcherNext.find()) {
					result = result + c5 +"\n";
				}
				else {
					String pages = matcherNext.group(1);
					String[] nums = pages.split(" ");
					String total = nums[nums.length-1];
					int len = 0;
					if(total.length()<=2) {
						len = Integer.parseInt(total);
					}
					else {
						String subTotal = total.substring(total.length()-2);
						len = Integer.parseInt(subTotal);
					}
					// System.out.println("---------"+len+"------");
					for(int i1 = 2; i1<=len; i1++) {
						logger.debug("Next page"+i1+"!");
						result += dealPage(String.valueOf(i1), url);
						
					}
				}				
			}
		}
		// System.out.print(result);
		n.setContent(result);
		
		// Pictures		
		// System.out.print("7. Pictures: ");
		String pictemp="";
		for(String u: picList){
			// System.out.println(u);
			pictemp = pictemp+u+"\n";
		}
		n.setPicture(pictemp);
		
		// System.out.println("\n====== end of this news ======");
	}
	
	private String replaceHtml(String s) {
		String s0 = s.replaceAll("&mdash;", "—");
		String s1 = s0.replaceAll("&mdash;", "—");
		String s2 = s1.replaceAll("&hellip;", "…");
		String s3 = s2.replaceAll("&nbsp;", "");
		String s4 = s3.replaceAll("&ldquo;", "“");
		String s5 = s4.replaceAll("&rdquo;", "”");
		String s6 = s5.replaceAll("&lsquo;", "‘");
		String s7 = s6.replaceAll("&rsquo;", "’");
		String s8 = s7.replaceAll("&deg;", "°");
		String s9 = s8.replaceAll("&prime;","'");
		String s10 = s9.replaceAll("&middot;", "·");
		String s11 = s10.replaceAll("&Prime;", "\"");
		return s11;
	}
	
	private String getTitle(String h) {
		String title;
		Pattern patternTitle = Pattern.compile("<title>(.+)?_军事_环球网</title>");
		Matcher matcherTitle = patternTitle.matcher(h);
		if(matcherTitle.find()) {
			title = matcherTitle.group(1);
			title = title.replaceAll("&quot;", "\"");
			title = title.replaceAll("<[^>]+>", "");
			return title;
		}
		else
			return "";
	}
	
	private String getAgent(String h) {
		String agent;
		Pattern patternAgent = Pattern.compile("<meta name=\"source\" content=\"(.+)?\">");
		Matcher matcherAgent = patternAgent.matcher(h);
		if(matcherAgent.find()) {
			agent = matcherAgent.group(1);
			int qIndex = agent.indexOf('"');
			String subAgent = agent.substring(0, qIndex);
			return subAgent;
		}
		else
			return "";
	}

	private String getAuthor(String c) {
		String author;
		Pattern patternAuthor = Pattern.compile("name=\"authorPop\">(.+?)</span>");
		Matcher matcherAuthor = patternAuthor.matcher(c);
		if(matcherAuthor.find()) {
			// System.out.println("0000"+matcherAuthor.group(1));
			author = matcherAuthor.group(1).replaceAll("<[^>]+>", "");
			author = author.trim();
			return author;
		}
		else
			return "";
	}

	private String getDateTime(String c) {
		Pattern patternDaytime = Pattern.compile("id=\"pubtime_baidu\">(.+?)</strong>");
		Matcher matcherDaytime = patternDaytime.matcher(c);
		if(matcherDaytime.find()) {
			String daytime = matcherDaytime.group(1);
			return daytime;
		}	
		else
			return null;
	}
	
	private String dealPage(String pnum, String url) throws UnsupportedEncodingException, IOException {
		String tresult="";
		String newUrl = url.replaceAll("\\.html", "_"+pnum+".html");
		// System.out.println(newUrl);
		URL nextPage = new URL(newUrl.trim());
		InputStreamReader istream = new InputStreamReader(nextPage.openStream(), "UTF-8");
		BufferedReader breader = new BufferedReader(istream);
		String buffer = "";
		String ttemp;
		while((ttemp = breader.readLine())!=null) {
			if(ttemp.trim().equals("<div class=\"text\" id=\"text\">"))
				break;
		}
		while((ttemp = breader.readLine())!=null) {
			if(ttemp.trim().startsWith("<div class=\"page\">")) {
				break;
			}
			buffer += ttemp;
		}
		// Extract Info
		String[] contentPara = buffer.split("<p>");
		if(contentPara.length == 1) {
			String x = contentPara[0].trim();			
			Pattern patternImgUrl = Pattern.compile("src=\"(.+?)\\.jpg\"");
			Matcher matcherImgUrl = patternImgUrl.matcher(x);
			if(matcherImgUrl.find()) {
				String picUrlItem = matcherImgUrl.group(1)+".jpg";
				picList.add(picUrlItem);
			}
		}
		else {
			for(int i = 1; i<contentPara.length; i++) {
				String x = contentPara[i];
				if(x.startsWith("<p ")) { // some pictures
					Pattern patternImgUrl = Pattern.compile("src=\"(.+?)\\.jpg\"");
					Matcher matcherImgUrl = patternImgUrl.matcher(x);
					if(matcherImgUrl.find()) {
						String picUrlItem = matcherImgUrl.group(1)+".jpg";
						picList.add(picUrlItem);
					}
				}
				else {
					String c2 = x.replaceAll("<script[^>]*?>[\\s\\S]*?<\\/script>", "");
					// other html tags
					String c3 = c2.replaceAll("<[^>]+>", "");
					String c4 = c3.replaceAll("　　", "");
					String c5 = c4.trim();
					tresult = tresult + replaceHtml(c5)+"\n";
				}
			}	
		}
		return tresult;
		
	}
}
