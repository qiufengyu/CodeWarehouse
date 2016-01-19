package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GoodsDB {
	private PreparedStatement FindGood;//������Ʒ
	private PreparedStatement InsertGood;//������Ʒ��Ϣ����Ʒ���id����Ʒ��name������count������price
	private PreparedStatement UpdateGood;//��Ʒ�۳�count
	private PreparedStatement UpdateGoodName;//��Ʒ���Ƹ���
	private PreparedStatement UpdateGoodPrice;//��Ʒ���۸���
	private PreparedStatement DeleteGood;//��Ʒ���۸���
	@SuppressWarnings("unused")
	private Connection connection;
	public GoodsDB(Connection connection){
		try {
			this.connection = connection;
			FindGood = connection.prepareStatement("select * from good where id = ?");
			InsertGood = connection.prepareStatement("insert into good values(?,?,?,?)");
			UpdateGood = connection.prepareStatement("update good set count = ? where id = ?");
			UpdateGoodName = connection.prepareStatement("update good set name = ? where id = ?");
			UpdateGoodPrice = connection.prepareStatement("update good set price = ? where id = ?");
			DeleteGood = connection.prepareStatement("delete from good where id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean insertGood(String id, String name, int count, double price){
		try {
			InsertGood.setString(1, id);
			InsertGood.setString(2, name);
			InsertGood.setInt(3, count);
			InsertGood.setDouble(4, price);
			int i = InsertGood.executeUpdate();
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
	
	int findGoodCount(String id){//������Ʒʣ������
		try {
			FindGood.setString(1, id);
			ResultSet resultSet = FindGood.executeQuery();
			if(resultSet.next())
				return resultSet.getInt(3);
			else
				return -1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public String findGoodname(String id){
		try {
			FindGood.setString(1, id);
			ResultSet resultSet = FindGood.executeQuery();		
			if(resultSet.next()){
				return resultSet.getString(2);
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public double findGoodPrice(String id){//��ѯ��Ʒ����
		try {
			FindGood.setString(1, id);
			ResultSet resultSet = FindGood.executeQuery();
			if(resultSet.next()){
				return resultSet.getDouble(4);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public boolean updateGood(String id, int deccount){//�۳���Ʒ����
		try {
			UpdateGood.setString(2,id);
			UpdateGood.setInt(1,findGoodCount(id)-deccount);
			int i = UpdateGood.executeUpdate();
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
	
	public boolean updateGoodNum(String id, int num){//ֱ�ӳ���Ʒ����
		try {
			UpdateGood.setString(2, id);
			UpdateGood.setInt(1, num);
			int i = UpdateGood.executeUpdate();
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
	
	public boolean updateGoodName(String id, String name) {
		try {
			UpdateGoodName.setString(2,id);
			UpdateGoodName.setString(1, name);
			int i = UpdateGoodName.executeUpdate();
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
	
	public boolean updateGoodPrice(String id, double p) {
		try {
			UpdateGoodPrice.setString(2, id);
			UpdateGoodPrice.setDouble(1, p);
			int i = UpdateGoodPrice.executeUpdate();
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
	
	public boolean deleteGood(String id) {
		try {
			DeleteGood.setString(1, id);
			int i = DeleteGood.executeUpdate();
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
