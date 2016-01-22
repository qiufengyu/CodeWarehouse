package com.huanqiu;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.huanqiu.china.CrawlerChina;
import com.huanqiu.world.CrawlerWorld;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	Logger logger = LogManager.getLogger(AppTest.class.getName());
		logger.info("Start Crawling");
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
