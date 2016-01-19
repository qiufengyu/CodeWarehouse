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
	private int serverPort = 8000;//�������˿�
	private Connection connection;
	private UsersDB users;//��Ա���ݿ�
	private EmployeeDB employees;//�û����ݿ�
	private TradeDB trades;//���׼�¼���ݿ�
	private GoodsDB goods;//��Ʒ��Ϣ���ݿ�
	public Server(){
		try {
				linkDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}//�������ݿ�
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
		        	Socket socket =  serverSocket.accept();//������������
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
		//System.out.println(Message.APP_NAME+"����������..\n.");
		//��ʼ������������
		//DemoData.init();
		System.out.println("Server On...");
		new Server();
	}

}