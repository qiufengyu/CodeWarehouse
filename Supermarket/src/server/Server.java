package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

import database.EmployeeDB;
import database.GoodsDB;
import database.TradeDB;
import database.UsersDB;
import server.ServerThread;

public class Server {
	private ServerSocket serverSocket;
	@SuppressWarnings("unused")
	private int ClientCount = 0;
	private int serverPort = 8000;//服务器端口
	private Connection connection;
	private UsersDB users;//会员数据库
	private EmployeeDB employees;//用户数据库
	private TradeDB trades;//交易记录数据库
	private GoodsDB goods;//商品信息数据库
	public Server(){
		try {
				linkDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}//连接数据库
		listen();		
	}
	public void linkDatabase() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/supermarket","root","root");
		System.out.println("Driver loaded!");
		users = new UsersDB(connection);
		employees = new EmployeeDB(connection);
		trades = new TradeDB(connection);
		goods = new GoodsDB(connection);
		System.out.println("Database connected!");
		
	  }
	public void listen(){
		  try {
		        serverSocket = new ServerSocket(serverPort);
		        while(true){
		        	Socket socket =  serverSocket.accept();//接收连接请求
		        	ClientCount+=1;
		        	System.out.println("A new Client connected...");
		        	new ServerThread(socket,users,employees,trades,goods).start();
		        }
		  }
		  catch(IOException ex) {
		      System.err.println(ex);
		    }
	  }
	public static void main(String[] args) {
		//System.out.println(Message.APP_NAME+"服务器启动..\n.");
		//初始化服务器数据
		//DemoData.init();
		System.out.println("Server On...");
		new Server();
	}

}