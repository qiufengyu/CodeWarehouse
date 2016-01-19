package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradeDB {
	private PreparedStatement FindTrade;//查找交易记录
	private PreparedStatement InsertTrade;//插入交易记录,用户名，交易金额，交易时间
	private PreparedStatement QueryNumTrade;//交易记录数
	private PreparedStatement QuerySumTrade;//交易总额
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
			InsertTrade.executeUpdate();//更新数据库
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	int findTrade(String Username){//返回消费金额
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
