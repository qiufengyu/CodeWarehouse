package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDB {
	private PreparedStatement FindUser;//查找用户
	private PreparedStatement InsertUser;//插入用户,帐号，会员等级
	private PreparedStatement UpdateLevel;
	@SuppressWarnings("unused")
	private Connection connection;
	public UsersDB(Connection connection){
		try {
			this.connection = connection;
			FindUser = connection.prepareStatement("select * from user where id = ?");
			InsertUser = connection.prepareStatement("insert into user values(?,?)");
			UpdateLevel = connection.prepareStatement("update user set level = ? where id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void insertUser(String Username,int level){
		try {
			InsertUser.setString(1, Username);
			InsertUser.setInt(2, level);
			InsertUser.executeUpdate();//更新数据库
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int findUser(String Username){
		try {
			FindUser.setString(1, Username);
			ResultSet resultSet = FindUser.executeQuery();
			if(resultSet.next()){
				return resultSet.getInt(2);
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	
	public boolean updateLevel(String id, int level) {
		try {
			UpdateLevel.setString(2, id);
			UpdateLevel.setInt(1, level);
			int i = UpdateLevel.executeUpdate();
			if(i>0)
				return true;
			else
				return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	
}
