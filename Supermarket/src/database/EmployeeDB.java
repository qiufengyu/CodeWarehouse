package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDB {
	private PreparedStatement FindEmployee;//���ҹ�Ա
	private PreparedStatement InsertEmployee;//�����û�,�ʺţ����룬�ȼ�
	private PreparedStatement VerifyEmployee;//��֤�û���������
	private PreparedStatement LoginEmployee;//��¼
	private PreparedStatement LogoutEmployee;//�ǳ�
	//private PreparedStatement AddEmployee; //���
	private PreparedStatement EditEmployee; //�༭��Ա��Ϣ
	private PreparedStatement DeleteEmployee; //ɾ����Ա
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
			int i = InsertEmployee.executeUpdate();//�������ݿ�
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
		if(tag==-1)return 0;//�������
		else if(tag==0)return -1;//�ظ���¼
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
		if(tag==-1||tag==0)return -1;//�Ѿ��ǳ�
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
