package common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.PreparedStatement;

public class DBTools {
	Logger logger = LogManager.getLogger(DBTools.class.getName());
	private Connection connection;
	PreparedStatement pStmt=null;
	PreparedStatement pStmt2=null;
	PreparedStatement pStmt3=null;
	PreparedStatement pwStmt=null;
	PreparedStatement pwStmt2=null;
	PreparedStatement pwStmt3=null;
	public DBTools() {
		try {
			linkDatabase();
			logger.info("database connect ok");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Database initial fail");
			logger.error("database connect fail");
			System.exit(0);
		}
	}
	
	private void linkDatabase() throws ClassNotFoundException, SQLException {
		logger.info("Connecting database huanqiu");
		Class.forName("com.mysql.jdbc.Driver");
		logger.info("jdbc driver loaded!");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/huanqiu","root","root");
		logger.info("Database connected!");
		pStmt = (PreparedStatement) connection.prepareStatement("INSERT INTO `huanqiu_china` VALUES (null,?,?,?,?,?,?,?,?)");
		pStmt2 = (PreparedStatement) connection.prepareStatement("SELECT * FROM `huanqiu_china` WHERE `url`=?");
		pStmt3 = (PreparedStatement) connection.prepareStatement("SELECT * FROM `huanqiu_china` WHERE `id`=?");
		pwStmt = (PreparedStatement) connection.prepareStatement("INSERT INTO `huanqiu_world` VALUES (null,?,?,?,?,?,?,?,?)");
		pwStmt2 = (PreparedStatement) connection.prepareStatement("SELECT * FROM `huanqiu_world` WHERE `url`=?");
		pwStmt3 = (PreparedStatement) connection.prepareStatement("SELECT * FROM `huanqiu_world` WHERE `id`=?");
	}
	
	public void insertValuesChina(News n) throws SQLException {
		String uniqueUrl = n.getUrl();
		pStmt2.setString(1, uniqueUrl);
		ResultSet rs = pStmt2.executeQuery();
		if(rs.next()) {
			logger.error("Ignore repeated news");
			/**
			 * Better not exit, if sometimes fail, then i can repair!
			 */
			// logger.info("End Testing");
			// System.exit(0);
			return;
		}
		java.util.Date date=new java.util.Date();
		java.sql.Timestamp tt=new java.sql.Timestamp(date.getTime());
		pStmt.setString(1, n.getTitle());
		pStmt.setString(2, n.getAgent());
		pStmt.setString(3, n.getAuthor());
		pStmt.setString(4, uniqueUrl);
		//Check if existed!
		pStmt.setString(5, n.getContent());
		pStmt.setString(6, n.getPicture());
		pStmt.setString(7, n.getUpdateTime());
		pStmt.setTimestamp(8, tt);	
		int i = pStmt.executeUpdate();
		if(i<=0) {
			logger.error("Insert fail!");
			System.exit(0);
		}
		else {
			logger.info("Insert ok!");
		}
	}	
	
	/**
	 * 将数据库中的Blob类型转换为java的String
	 * @param t
	 * @throws SQLException
	 * @throws IOException
	 */
	public void selectValuesChina(int t) throws SQLException, IOException {
		pStmt3.setInt(1, t);
		ResultSet rs = pStmt3.executeQuery();
		if(rs.next()) {
			logger.info("select OK");
			java.sql.Blob contentBlob = rs.getBlob("content");
			BufferedInputStream contentData = new BufferedInputStream (contentBlob.getBinaryStream());
			byte [] buf = new byte [contentData.available()];
			contentData.read(buf, 0, buf.length);
			String content = new String(buf, "UTF-8");
			System.out.println(content);
		}
		else {
			logger.error("select error");
			System.exit(0);
		}
	}
	
	public void insertValuesWorld(News n) throws SQLException {
		String uniqueUrl = n.getUrl();
		pwStmt2.setString(1, uniqueUrl);
		ResultSet rs = pwStmt2.executeQuery();
		if(rs.next()) {
			logger.error("Ignore repeated news");
			logger.info("End Testing");
			System.exit(0);
			return;
		}
		java.util.Date date=new java.util.Date();
		java.sql.Timestamp tt=new java.sql.Timestamp(date.getTime());
		pwStmt.setString(1, n.getTitle());
		pwStmt.setString(2, n.getAgent());
		pwStmt.setString(3, n.getAuthor());
		pwStmt.setString(4, uniqueUrl);
		//Check if existed!
		pwStmt.setString(5, n.getContent());
		pwStmt.setString(6, n.getPicture());
		pwStmt.setString(7, n.getUpdateTime());
		pwStmt.setTimestamp(8, tt);	
		int i = pwStmt.executeUpdate();
		if(i<=0) {
			logger.error("Insert fail!");
			System.exit(0);
		}
		else {
			logger.info("Insert ok!");
		}
	}	
	
	public void selectValuesWorld(int t) throws SQLException, IOException {
		pwStmt3.setInt(1, t);
		ResultSet rs = pwStmt3.executeQuery();
		if(rs.next()) {
			logger.info("select OK");
			java.sql.Blob contentBlob = rs.getBlob("content");
			BufferedInputStream contentData = new BufferedInputStream (contentBlob.getBinaryStream());
			byte [] buf = new byte [contentData.available()];
			contentData.read(buf, 0, buf.length);
			String content = new String(buf, "UTF-8");
			System.out.println(content);
		}
		else {
			logger.error("select error");
			System.exit(0);
		}
	}
	
}
