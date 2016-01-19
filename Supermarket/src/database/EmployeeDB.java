package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDB {
	private PreparedStatement FindEmployee;//查找雇员
	private PreparedStatement InsertEmployee;//插入用户,帐号，密码，等级
	private PreparedStatement VerifyEmployee;//验证用户名和密码
	private PreparedStatement LoginEmployee;//登录
	private PreparedStatement LogoutEmployee;//登出
	//private PreparedStatement AddEmployee; //添加
	private PreparedStatement EditEmployee; //编辑雇员信息
	private PreparedStatement DeleteEmployee; //删除雇员
	@SuppressWarnings("unused")
	private Connection connection;
	public EmployeeDB(Connection connection){
		try {
			this.connection = connection;
			FindEmployee = connection.prepareStatement("select * from employee where id = ?");
			InsertEmployee = connection.prepareStatement("insert into employee values(?,?,?,0)");
			VerifyEmployee = connection.prepareStatement("select * from employee where id = ? and password = ?");
			LoginEmployee = connection.prepareStatement("update employee set online = 1 where id = ?");
			LogoutEmployee = connection.prepareStatement("update employee set online = 0 where id = ?");
			EditEmployee = connection.prepareStatement("update employee set level = ? where id = ?");
			DeleteEmployee = connection.prepareStatement("delete from employee where id = ?");
			//AddEmployee = connection.prepareStatement("insert into employee values(?,?,?,0)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean insertEmployee(String Username,String Password,int level){
		try {
			InsertEmployee.setString(1, Username);
			InsertEmployee.setString(2, Password);
			InsertEmployee.setInt(3, level);
			int i = InsertEmployee.executeUpdate();//更新数据库
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
	
	public int verifyEmployee(String Username,String Password){
		try {
			VerifyEmployee.setString(1, Username);
			VerifyEmployee.setString(2, Password);
			ResultSet resultSet = VerifyEmployee.executeQuery();
			if(resultSet.next())return 1;
			else return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int findEmployeelevel(String Username){
		try {
			FindEmployee.setString(1, Username);
			ResultSet resultSet = FindEmployee.executeQuery();
			if(resultSet.next())return resultSet.getInt(3);
			else return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	public int findEmployeeonline(String Username){
		try {
			FindEmployee.setString(1, Username);
			ResultSet resultSet = FindEmployee.executeQuery();
			if(resultSet.next())return resultSet.getInt(4);
			else return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public int loginEmployee(String Username,String Password){
		int tag = verifyEmployee(Username,Password);
		if(tag==-1)return 0;//密码错误
		else if(tag==0)return -1;//重复登录
		else {
			try {
				LoginEmployee.setString(1, Username);
				LoginEmployee.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 1;
		}
	}
	
	public int logoutEmployee(String Username){
		int tag = findEmployeeonline(Username);
		if(tag==-1||tag==0)return -1;//已经登出
		else {
			try {
				LogoutEmployee.setString(1, Username);
				LogoutEmployee.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 1;
		}
	}
	
	public boolean editEmployee(String id, int auth) {
		try {
			EditEmployee.setString(2, id);
			EditEmployee.setInt(1, auth);
			int i = EditEmployee.executeUpdate();
			if(i > 0)
				return true;
			else
				return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean deleteEmployee(String id) {
		try {
			DeleteEmployee.setString(1, id);
			int i = DeleteEmployee.executeUpdate();
			if(i > 0)
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
