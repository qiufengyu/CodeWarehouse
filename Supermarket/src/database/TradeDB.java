package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradeDB {
	private PreparedStatement FindTrade;//���ҽ��׼�¼
	private PreparedStatement InsertTrade;//���뽻�׼�¼,�û��������׽�����ʱ��
	private PreparedStatement QueryNumTrade;//���׼�¼��
	private PreparedStatement QuerySumTrade;//�����ܶ�
	@SuppressWarnings("unused")
	private Connection connection;
	public TradeDB(Connection connection){
		try {
			this.connection = connection;
			FindTrade = connection.prepareStatement("select * from trade where id = ?");
			InsertTrade = connection.prepareStatement("insert into trade values(?,?,?)");
			QueryNumTrade = connection.prepareStatement("select count(*) from trade");
			QuerySumTrade = connection.prepareStatement("select sum(money) from trade");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void insertTrade(String Username,Double Sum,String Time){
		try {
			InsertTrade.setString(1, Username);
			InsertTrade.setDouble(3, Sum);
			InsertTrade.setString(2, Time);
			InsertTrade.executeUpdate();//�������ݿ�
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	int findTrade(String Username){//�������ѽ��
		try {
			FindTrade.setString(1, Username);
			ResultSet resultSet = FindTrade.executeQuery();
			if(resultSet.next())
				return resultSet.getInt(2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	
	public int queryNumTrade() {
		ResultSet rset;
		try {
			rset = QueryNumTrade.executeQuery();
			if(rset.next()) {
				return rset.getInt(1);
			}
			else
				return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}	
	
	
	public double querySumTrade() {
		ResultSet rset;
		try {
			rset = QuerySumTrade.executeQuery();
			if(rset.next()) {
				return rset.getDouble(1);
			}
			else
				return -1.0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1.0;
		}
	}	
}
