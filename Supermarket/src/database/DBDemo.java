package database;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import database.EmployeeDB;
import database.GoodsDB;
import database.TradeDB;
import database.UsersDB;

public class DBDemo {
	private Connection connection;
	private UsersDB users;//会员数据库
	private EmployeeDB employees;//用户数据库
	@SuppressWarnings("unused")
	private TradeDB trades;//交易记录数据库
	private GoodsDB goods;//商品信息数据库
	public DBDemo() {
		try {
			linkdatabase();
			initialEmployeeDB();
			initialGoodsDB();
			initialUsersDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Database initial fail");
			System.exit(0);
		}
	}
	void linkdatabase() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermarket","root","root");
		System.out.println("Driver loaded!");
		users = new UsersDB(connection);
		employees = new EmployeeDB(connection);
		trades = new TradeDB(connection);
		goods = new GoodsDB(connection);
		System.out.println("Database connected!");
	}
	void initialGoodsDB() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(".//src//database//Goods.txt"),"UTF-8"));
		String line = null;
		while((line = br.readLine())!=null){
			String []strs = line.split("#");
			goods.insertGood(strs[0], strs[1], Integer.parseInt(strs[2]), Double.parseDouble(strs[3]));
		}
		br.close();
	}
	void initialEmployeeDB() throws IOException{
		FileReader reader = new FileReader(".//src//database//Employee.txt");
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while((line = br.readLine())!=null){
			String []strs = line.split(" ");
			int level = Integer.parseInt(strs[2]);
			employees.insertEmployee(strs[0], strs[1], level);
		}
		br.close();
	}
	void initialUsersDB() throws IOException{
		FileReader reader = new FileReader(".//src//database//User.txt");
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		while((line = br.readLine())!=null){
			String []strs = line.split(" ");
			users.insertUser(strs[0], Integer.parseInt(strs[1]));
		}
		br.close();
		reader.close();
	}
	public static void main(String []args){
		new DBDemo();
		System.out.println("Database initial success");
	}
}
