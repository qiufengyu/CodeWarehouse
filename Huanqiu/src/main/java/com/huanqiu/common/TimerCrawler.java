package com.huanqiu.common;
import java.io.IOException;
import java.sql.SQLException;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huanqiu.world.CrawlerWorld;
import com.huanqiu.china.CrawlerChina;

public class TimerCrawler extends TimerTask {

	@Override
	public void run() {
		Logger logger = LogManager.getLogger(TimerCrawler.class.getName());
		logger.info("Start Crawling by Timer");
		CrawlerChina crlChina = new CrawlerChina();
		CrawlerWorld crlWorld = new CrawlerWorld();
		try {
			crlChina.runCrawler();
			crlWorld.runCrawler();
		} catch (IOException e) {			
			logger.error("java IO fail");
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("database fail");
			e.printStackTrace();
		}
		finally {
			logger.info("End Testing");
		}
		
		
	}
	
	

}
