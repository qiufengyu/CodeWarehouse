package com.huanqiu.common;

import java.util.Timer;

public class Crawlers {

	public static void main(String[] args) {
		System.out.println("ddd");
		Timer timer = new Timer();
		// do every 6 hours
		timer.schedule(new TimerCrawler(), 1000, (long)1000*60*60*6);  
				
	}

}

