package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import database.EmployeeDB;
import database.GoodsDB;
import database.TradeDB;
import database.UsersDB;
import entity.Datas;
import entity.Goods;
import entity.User;
import util.DEFINE;


public class ServerThread extends Thread{
	private Socket socket=null;
	private InputStream is = null;
	private ObjectInputStream ois = null;
	private OutputStream os = null;
	private ObjectOutputStream oos= null;
	
	private UsersDB users;//会员数据库
	private EmployeeDB employees;//用户数据库
	private TradeDB trades;//交易记录数据库
	private GoodsDB goods;//商品信息数据库
	
	public ServerThread(Socket socket,UsersDB users, EmployeeDB employees,TradeDB trades,GoodsDB goods){
		this.socket=socket;
		this.users = users;
		this.employees = employees;
		this.trades = trades;
		this.goods = goods;
	}
	
	public void run(){
			try {
				is=socket.getInputStream();
				ois=new ObjectInputStream(is);
				os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				while(true){			
					Datas datas = (Datas) ois.readObject();
					System.out.println(datas);		
					System.out.println(datas.getFlags());
					//登录请求
					if(DEFINE.SYS_LOGIN.equals(datas.getFlags())){
						System.out.println("登录");
						User u = datas.getUser();
						int tag = employees.loginEmployee(u.getUserid(),u.getPassword());
						System.out.println(u.getUserid()+" "+u.getPassword());
						Datas outdata = new Datas();
						if(tag==0||tag==-1){//密码错误,重复登录，登录失败
							System.out.println("登录失败");
							outdata.setFlags(DEFINE.SYS_LOGINFAIL);
						}
						else{
							System.out.println("登录成功");
							u.setAuthority(employees.findEmployeelevel(u.getUserid()));
							outdata.setFlags(DEFINE.SYS_LOGINSUCCESS);
							outdata.setUser(u);
						}
						oos.writeObject(outdata);
					}
					else if(DEFINE.SYS_LOGOUT.equals(datas.getFlags())){//登出
						System.out.println("登出");
						User u = datas.getUser();
						int tag = employees.logoutEmployee(u.getUserid());
						Datas outdata = new Datas();
						if(tag==-1){//登出失败
							System.out.println("登出失败");
							outdata.setFlags(DEFINE.SYS_LOGOUTFAIL);
						}
						else{
							System.out.println("登出成功");
							outdata.setFlags(DEFINE.SYS_LOGOUTSUCCESS);
							outdata.setUser(u);
						}
						oos.writeObject(outdata);
					}
					else if(DEFINE.SYS_GOODS_INFO.equals(datas.getFlags())){//查询商品信息
						String getid=datas.getGoods().getGoodid();
						int getcount =datas.getGoods().getCount();
						Datas outdata = new Datas();
						Goods good = new Goods();
						good.setCount(getcount);
						good.setGoodid(getid);
						good.setPrice(goods.findGoodPrice(getid));
						if(goods.findGoodname(getid)!=null){
							good.setName(goods.findGoodname(getid));
							goods.updateGood(getid, getcount);
						}
						outdata.setGoods(good);
						
						oos.writeObject(outdata);
						
					}
					else if(DEFINE.SYS_MEMBER_QUERY.equals(datas.getFlags())){//查询会员
						String getid=datas.getUser().getUserid();
						Datas outdata = new Datas();
						User user = new User();
						user.setUserid(getid);
						user.setAuthority(users.findUser(getid));
						System.out.println(users.findUser(getid));
						outdata.setUser(user);
						
						oos.writeObject(outdata);
						
					}
	/*				else if(DEFINE.SYS_GOODS_OUT.equals(datas.getFlags())){//商品出库
						
					}
					else if(DEFINE.SYS_BALANCE.equals(datas.getFlags())){//账单
						
					}*/
					else if(DEFINE.SYS_ADD_TRADE.equals(datas.getFlags())){//添加交易记录
						String getid=datas.getTrade().getid();
						double cost = datas.getTrade().getCost();
						String getdate = datas.getTrade().getDate();						
						trades.insertTrade(getid,cost,getdate);
						
					}
					else if(DEFINE.SYS_RETURN_GOOD.equals(datas.getFlags())) { //退货申请
						Goods tempgood = new Goods();
						tempgood = datas.getGoods();
						String goodid = tempgood.getGoodid();
						int goodcount = tempgood.getCount();
						Datas tempdatas = new Datas();
						if(goods.updateGood(goodid, -goodcount)) {
							tempdatas.setFlags(DEFINE.SYS_RETURN_GOOD_SUCCESS);
							String returnid = "return";
							double price0 = goods.findGoodPrice(goodid);
							double amount = -price0*goodcount;
							Date d = new Date();						
							trades.insertTrade(returnid, amount, d.toString());	
						}
						else {
							tempdatas.setFlags(DEFINE.SYS_RETURN_GOOD_FAIL);
						}					
						tempdatas.setGoods(tempgood);
						oos.writeObject(tempdatas);				
					}
					else if(DEFINE.SYS_ADD_GOOD.equals(datas.getFlags())) { //添加商品申请
						Goods tempgoods = new Goods();
						tempgoods = datas.getGoods();
						String goodid = tempgoods.getGoodid();
						String goodname = tempgoods.getName();
						int goodcount = tempgoods.getCount();
						double goodprice = tempgoods.getPrice();
						Datas tempdatas = new Datas();
						if(goods.insertGood(goodid, goodname, goodcount, goodprice)) {
							tempdatas.setFlags(DEFINE.SYS_ADD_GOOD_SUCCESS);
						}
						else {
							tempdatas.setFlags(DEFINE.SYS_ADD_GOOD_FAIL);
						}		
						tempdatas.setGoods(tempgoods);
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_EDIT_GOOD.equals(datas.getFlags())) {//编辑商品
						Goods tempgoods = new Goods();
						tempgoods = datas.getGoods();
						String goodid = tempgoods.getGoodid();
						String goodname = tempgoods.getName();
						int goodcount = tempgoods.getCount();
						double goodprice = tempgoods.getPrice(); 
						Datas tempdatas = new Datas();
						tempdatas.setFlags(DEFINE.SYS_EDIT_GOOD_FAIL);
						if(!goodname.equals("")) {
							if(goods.updateGoodName(goodid, goodname))
								tempdatas.setFlags(DEFINE.SYS_EDIT_GOOD_SUCCESS);
						}
						if(!(goodcount == -1)) {
							if(goods.updateGoodNum(goodid, goodcount))
								tempdatas.setFlags(DEFINE.SYS_EDIT_GOOD_SUCCESS);
						}
						if(goodprice+1.0>0) {
							if(goods.updateGoodPrice(goodid, goodprice))
								tempdatas.setFlags(DEFINE.SYS_EDIT_GOOD_SUCCESS);
						}
						tempdatas.setGoods(tempgoods);
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_DELETE_GOOD.equals(datas.getFlags())) {//移除商品
						Goods tempgoods = new Goods();
						tempgoods = datas.getGoods();
						Datas tempdatas = new Datas();
						tempdatas.setFlags(DEFINE.SYS_DELETE_GOOD_FAIL);
						String goodid = tempgoods.getGoodid();
						if(!(goodid.equals(""))) {
							if(goods.deleteGood(goodid)) {
								tempdatas.setFlags(DEFINE.SYS_DELETE_GOOD_SUCCESS);
							}								
						}
						tempdatas.setGoods(tempgoods);
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_EDIT_EMPLOYEE.equals(datas.getFlags())) {//编辑员工信息
						User tempu = new User();
						tempu = datas.getUser();
						String uid = tempu.getUserid();
						int ulevel = tempu.getAuthority();
						Datas tempdatas = new Datas();
						tempdatas.setFlags(DEFINE.SYS_EDIT_EMPLOYEE_FAIL);
						int type = employees.findEmployeelevel(uid);
						if(ulevel != -1) {
							if(type == -1) {
								if(employees.insertEmployee(uid, uid, ulevel)) {
									tempdatas.setFlags(DEFINE.SYS_EDIT_EMPLOYEE_SUCCESS);
								}
							}
							else {
								if(employees.editEmployee(uid, ulevel)) {
									tempdatas.setFlags(DEFINE.SYS_EDIT_EMPLOYEE_SUCCESS);
								}							
							}
						}						
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_DELETE_EMPLOYEE.equals(datas.getFlags())) {//移除员工
						User tempu = new User();
						tempu = datas.getUser();
						String uid = tempu.getUserid();
						Datas tempdatas = new Datas();
						tempdatas.setFlags(DEFINE.SYS_DELETE_EMPLOYEE_FAIL);
						if(!uid.equals("")) {
							if(employees.deleteEmployee(uid)) {
								tempdatas.setFlags(DEFINE.SYS_DELETE_EMPLOYEE_SUCCESS);
							}
						}
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_SALE_QUERY.equals(datas.getFlags())) {
						int numOfTrade = trades.queryNumTrade();
						double sumOfTrade = trades.querySumTrade();
						Datas tempdatas = new Datas();
						tempdatas.setFlags(DEFINE.SYS_SALE_QUERY);
						tempdatas.setNumTrade(numOfTrade);
						tempdatas.setSumTrade(sumOfTrade);
						oos.writeObject(tempdatas);
					}
					else if(DEFINE.SYS_CUSTOMER.equals(datas.getFlags())) {
						String cusid = datas.getUser().getUserid();
						int oldlevel = users.findUser(cusid);
						users.updateLevel(cusid, oldlevel-1);
					}
					
				}
			} catch (ClassNotFoundException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}
	}

}