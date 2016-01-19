package world;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Logger logger = LogManager.getLogger(WorldTest.class.getName());
		logger.info("Start Testing");
		CrawlerWorld crl = new CrawlerWorld();
		try {
			crl.runCrawler();
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
