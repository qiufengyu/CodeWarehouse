package client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import util.DEFINE;

import java.util.Date;
import java.io.*;
import java.net.*;

import entity.Goods;
import entity.Trade;
import entity.User;
import entity.Datas;

@SuppressWarnings("serial")
public class SimpleUI extends JFrame {
	//Components
	JLabel bg;
	JLabel headTitle;
	JLabel userLabel;
	JTextField userInfo;
	JLabel customerLabel;
	JTextField customerInfo;
	JLabel dataLabel;
	JLabel goodIdLabel;
	JTextField goodIdInfo;
	JLabel goodNumLabel;
	JTextField goodNumInfo;
	JButton addItemButton;
	JButton resetItemButton;
	JLabel listTitle;
	JTextArea tarea;
	ScrollPane spanel;
	JLabel sumAmountLabel;
	JLabel sumMoneyLabel;
	JLabel sumAmountText;
	JLabel sumMoneyText;
	JLabel shouldMoneyLabel;
	JLabel shouldMoneyText;
	JLabel inputMoneyLabel;
	JTextField inputMoneyText;
	JLabel changeLabel;
	JLabel changeText;
	JLabel s1;
	JLabel s2;
	JLabel s3;
	JLabel s4;
	JLabel s5;
	Timer timer;
	
	JButton commit;
	JButton next;
	
	//discount global 
	int discount = -1;
	//constants
	private static final Font fontc = new Font("微软雅黑",Font.PLAIN+Font.BOLD,20);
	private static final Font fonte = new Font("Calibri",Font.PLAIN,16);
	private static final Font fontc1 = new Font("微软雅黑",Font.PLAIN,14);
	private static final Font fontc2 = new Font("微软雅黑",Font.PLAIN+Font.BOLD,17);
	private static final String splitline = "--------------------------------------------"
			+ "------------------------------------------------------------------"
			+ "------------------------------------";
	private static final String splitline2 = "--------------------------------------------"
			+ "------------------------------------------------------";
	
	// Sockets
	private ObjectOutputStream outputToServer;
	private ObjectInputStream inputFromServer;
	private Socket socket;
	
	
	@SuppressWarnings("deprecation")
	public SimpleUI(String id) {
		try {
	        socket = new Socket("localhost", 8000);//服务器固定ip   
	        outputToServer = new ObjectOutputStream(socket.getOutputStream());
	        inputFromServer = new ObjectInputStream(socket.getInputStream());
	    }
	    catch (IOException ex) {
	        System.out.println(ex.toString());
	    }
		setSize(600, 730);
		bg = new JLabel();
		// ImageIcon icon = new ImageIcon("images/bg1.jpg");
		// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
		bg.setBounds(0, 0, 600, 730);  
        bg.setHorizontalAlignment(0);  
        // bg.setIcon(icon); 
        
        //head title
        ImageIcon headic = new ImageIcon("images/title.png");
		headTitle = new JLabel(headic);
		headTitle.setFont(fontc);
		headTitle.setBounds(200,10,200,25);
		bg.add(headTitle);
		
		//日期
		Date d = new Date();
		int year = d.getYear()+1900;
		int month = d.getMonth();
		int day = d.getDay();
		int daynum = d.getDate();
		int hour = d.getHours();
		int minute = d.getMinutes();
		int second = d.getSeconds();
		String ds = String.valueOf(year)+"/"+String.valueOf(month)+"/"+String.valueOf(daynum);
		String hh = "";
		String mm = "";
		String ss = "";
		if(hour<10) {
			hh = "0"+ String.valueOf(hour);
		}
		else {
			hh = String.valueOf(hour);
		}
		if(minute<10) {
			mm = "0"+ String.valueOf(minute);
		}
		else {
			mm = String.valueOf(minute);
		}
		if(second<10) {
			ss = "0"+String.valueOf(second);
		}
		else {
			ss = String.valueOf(second);
		}
		String ds2 = hh+" : "+mm + " : "+ss;
		String ds3 = getDayString(day);
		String ddd = ds+"    "+ds2+"    "+ds3;		
		dataLabel = new JLabel(ddd);
		dataLabel.setFont(fonte);
		dataLabel.setBounds(190, 40, 220, 20);
		bg.add(dataLabel);
		
		//split line
		s1 = new JLabel(splitline);
		s1.setBounds(0, 55, 600, 20);
		bg.add(s1);
		
		//操作员
		userLabel = new JLabel("操作员：");
		userLabel.setFont(fontc1);
		userLabel.setBounds(25, 80, 75, 25);
		userInfo = new JTextField();
		userInfo.setText(id);
		userInfo.setFont(fonte);
		userInfo.setEditable(false);
		userInfo.setBounds(85, 80, 125, 25);
		bg.add(userLabel);
		bg.add(userInfo);
		
		//会员信息
		customerLabel = new JLabel("会员编号：");
		customerLabel.setFont(fontc1);
		customerLabel.setBounds(300, 80, 105, 25);
		customerInfo = new JTextField();
		customerInfo.setText("");
		customerInfo.setFont(fonte);
		customerInfo.setEditable(true);
		customerInfo.setBounds(410, 80, 125, 25);
		bg.add(customerLabel);
		bg.add(customerInfo);	
		
		//split line2
		s2 = new JLabel(splitline);
		s2.setBounds(0, 115, 600, 20);
		bg.add(s2);
		
		// goodIdLable;
		// goodNumLabel;
		// 商品编号
		goodIdLabel = new JLabel("商品编号：");
		goodIdLabel.setFont(fontc1);
		goodIdLabel.setBounds(25, 140, 105, 25);
		goodIdInfo = new JTextField();
		goodIdInfo.setFont(fonte);
		goodIdInfo.setEditable(true);
		goodIdInfo.setBounds(110, 140, 200, 25);
		bg.add(goodIdLabel);
		bg.add(goodIdInfo);
		
		// 商品数量
		goodNumLabel = new JLabel("商品数量：");
		goodNumLabel.setFont(fontc1);
		goodNumLabel.setBounds(25, 176, 105, 25);
		goodNumInfo = new JTextField();
		goodNumInfo.setFont(fonte);
		goodNumInfo.setEditable(true);
		goodNumInfo.setBounds(110, 176, 200, 25);
		bg.add(goodNumLabel);
		bg.add(goodNumInfo);
		
		//buttons for add and reset
		addItemButton = new JButton("添加");
		addItemButton.setFont(fontc1);
		addItemButton.setBounds(365, 158, 80, 25);
		resetItemButton = new JButton("重置");
		resetItemButton.setFont(fontc1);
		resetItemButton.setBounds(470, 158, 80, 25);
		bg.add(addItemButton);
		bg.add(resetItemButton);
		
		//split line3
		s3 = new JLabel(splitline);
		s3.setBounds(0, 206, 600, 20);
		bg.add(s3);
		
		//listTitle
		listTitle = new JLabel("销售清单");
		listTitle.setFont(fontc2);
		listTitle.setBounds(255, 225, 80, 25);
		bg.add(listTitle);
		
		//textArea+Scrollpanel
		tarea = new JTextArea();
		tarea.setEditable(false);
		tarea.setFont(fontc1);
		tarea.setText(" 编号\t名称\t\t数量\t单价\t小计\n"+splitline2+"\n");
		spanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		spanel.add(tarea);
		spanel.setBounds(0, 260, 600, 250);
		bg.add(spanel);
		
		//split line4
		s4 = new JLabel(splitline);
		s4.setBounds(0, 520, 600, 20);
		bg.add(s4);
		
		//Sum Label
		sumAmountLabel = new JLabel("数量合计： ");
		sumAmountLabel.setFont(fontc2);
		sumAmountLabel.setBounds(60, 540, 150, 25);
		
		sumAmountText = new JLabel();
		sumAmountText.setFont(fontc2);
		sumAmountText.setText("0");
		//有geText方法，从String转换成Integer，加数量后在重新setText
		sumAmountText.setBounds(150, 540, 100, 25);
		
		sumMoneyLabel = new JLabel("金额合计： ￥ ");
		sumMoneyLabel.setFont(fontc2);
		sumMoneyLabel.setBounds(310, 540, 150, 25);
		
		sumMoneyText = new JLabel();
		sumMoneyText.setFont(fontc2);
		sumMoneyText.setText("0000.00");
		//有geText方法，从String转换成Integer，加数量后在重新setText
		sumMoneyText.setBounds(425, 540, 160, 25);
		
		// Money should be
		shouldMoneyLabel = new JLabel("应收金额： ￥ ");
		shouldMoneyLabel.setFont(fontc2);
		shouldMoneyLabel.setBounds(60, 570, 150, 25);
		
		shouldMoneyText = new JLabel();
		shouldMoneyText.setFont(fontc2);
		shouldMoneyText.setText("0000.00");
		shouldMoneyText.setBounds(175, 570, 160, 25);
		
		//实收金额
		inputMoneyLabel = new JLabel("实收金额： ￥ ");
		inputMoneyLabel.setFont(fontc2);
		inputMoneyLabel.setBounds(310, 570, 150, 25);
		inputMoneyText = new JTextField();
		inputMoneyText.setFont(fonte);
		inputMoneyText.setEditable(true);
		inputMoneyText.setBounds(425, 570, 130, 25);		
		
		//找零相关
		changeLabel = new JLabel("找零金额： ￥ ");
		changeLabel.setFont(fontc2);
		changeLabel.setBounds(60, 600, 150, 25);
		
		changeText = new JLabel();
		changeText.setFont(fontc2);
		changeText.setText("0.00");
		changeText.setBounds(175, 600, 160, 25);
		
		//add components
		bg.add(sumAmountLabel);
		bg.add(sumAmountText);
		bg.add(sumMoneyLabel);
		bg.add(sumMoneyText);
		bg.add(shouldMoneyLabel);
		bg.add(shouldMoneyText);
		bg.add(inputMoneyLabel);
		bg.add(inputMoneyText);
		bg.add(changeLabel);
		bg.add(changeText);
		
		//commit button
		commit = new JButton("结算");
		commit.setFont(fontc1);
		commit.setBounds(360, 605, 80, 28);
		
		next = new JButton("下一位");
		next.setFont(fontc1);
		next.setBounds(460, 605, 80, 28);
		
		bg.add(commit);	
		bg.add(next);
				
		
		//split line4
		s5 = new JLabel(splitline);
		s5.setBounds(0, 630, 600, 20);
		bg.add(s5);
		
		//Timer: update time
		timer = new Timer(1000, new TimerListener());
		timer.start();
		
		add(bg);	
		
		this.setTitle("收银系统");
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		
		addItemButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String id = goodIdInfo.getText();
						String num = goodNumInfo.getText();	
						boolean flag = false;
						if(!(id.equals("")||num.equals(""))) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							Goods goods = new Goods();

							goods.setGoodid(id);;
							goods.setCount( Integer.parseInt(num));
							sendd.setGoods(goods);
							sendd.setFlags("GOODINFO");
							
							double moneyincrement = 0;//Updata with 小计
							
							try {
								outputToServer.writeObject(sendd);
								try {
									recvd = (Datas) inputFromServer.readObject();
								} catch (ClassNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								String getid=recvd.getGoods().getGoodid();
								String getname = recvd.getGoods().getName();
								double getprice=recvd.getGoods().getPrice();
								int getcount =recvd.getGoods().getCount();
								moneyincrement=getprice*Integer.parseInt(num);
								// 先向服务器请求，得到单价、商品名称，计算出小计，先填入textarea
								flag = (getprice==0.0);
								if(!flag) {
									if(getname.length()>8)
										tarea.append(" "+getid+"\t"+getname+"\t"+getcount+"\t"+getprice+"\t"+getprice*Integer.parseInt(num)+"\n");						
									else 
										tarea.append(" "+getid+"\t"+getname+"\t\t"+getcount+"\t"+getprice+"\t"+getprice*Integer.parseInt(num)+"\n");
								}// 更新数量与总计
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}		
							
							if(!flag) {
								int numincrement = Integer.parseInt(num);
								String oldnum = sumAmountText.getText();
								int currentnum = numincrement+Integer.parseInt(oldnum);
								String curnum = String.valueOf(currentnum);
								// System.out.println(curnum);
								sumAmountText.setText(curnum);
								// sumMoneythesame
								
								String oldmoney = sumMoneyText.getText();
								double currentmoney = moneyincrement+Double.parseDouble(oldmoney);
								String curmoney = String.format("%.2f", currentmoney);
								// System.out.println(curmoney);
								sumMoneyText.setText(curmoney);
								// Should money:
								// discount get from database!
											
								if(discount==-1){
									Datas sendp = new Datas();
									Datas recvp = new Datas();
									User user= new User();
	
									String userid = customerInfo.getText();
									user.setUserid(userid);
									sendp.setUser(user);
									sendp.setFlags("MEMBERQUERY");
									
									try {
										outputToServer.writeObject(sendp);
										try {
											recvp = (Datas) inputFromServer.readObject();
										} catch (ClassNotFoundException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										discount = recvp.getUser().getAuthority();
										if(discount==0)
											discount=10;
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									
								}
								System.out.println(discount);
								System.out.println(currentmoney);
								double shouldmoney = currentmoney*(double)discount/10.0;
								String curshouldmoney = String.format("%.2f", shouldmoney);
								shouldMoneyText.setText(curshouldmoney);
							}							
						}
						goodIdInfo.setText("");
						goodNumInfo.setText("");
						
					}
				}
			); 
		
		resetItemButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						goodIdInfo.setText("");
						goodNumInfo.setText("");
					}
				}
			); 
		
		commit.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Send message to server, and check
						if(!inputMoneyText.getText().equals("")) {
							String shouldm = shouldMoneyText.getText();
							double shouldmd = Double.parseDouble(shouldm);
							String realm = inputMoneyText.getText();
							double realmd = Double.parseDouble(realm);
							double changed = realmd-shouldmd;
							String changes = String.format("%.2f", changed);
							changeText.setText(changes);
							
							Datas sendd = new Datas();
							
							Trade trade = new Trade();
							trade.setid(customerInfo.getText());
							trade.setCost(shouldmd);
							Date d=new Date();
							trade.setDate(d.toString());
							
							sendd.setTrade(trade);
							sendd.setFlags("ADDTRADE");
							
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
						if(!userInfo.getText().equals("")) {
							String userid = customerInfo.getText();
							Datas sendd = new Datas();
							sendd.setFlags(DEFINE.SYS_CUSTOMER);
							User u = new User();
							u.setUserid(userid);
							sendd.setUser(u); //here u is the customer
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}							
						}
					}
				}
			); 
		
		next.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// reset
						customerInfo.setText("");
						goodIdInfo.setText("");
						goodNumInfo.setText("");
						tarea.setText(" 编号\t名称\t\t数量\t单价\t小计\n"+splitline2+"\n");
						sumAmountText.setText("0");
						sumMoneyText.setText("0000.00");
						shouldMoneyText.setText("0000.00");
						inputMoneyText.setText("");
						changeText.setText("0.00");
						discount=-1;
					}
				}
			); 
	}

	
	private String getDayString(int t) {
		String temp = "";
		if(t == 0) {
			temp = temp+"Sunday";
		}
		else if(t == 1) {
			temp = temp+"Monday";
		}
		else if(t == 2) {
			temp = temp+"Tuesday";
		}
		else if(t == 3) {
			temp = temp+"Wednesday";
		}
		else if(t == 4) {
			temp = temp+"Thursday";
		}
		else if(t == 5) {
			temp = temp+"Friday";
		}
		else {
			temp = temp+"Saturday";
		}
		return temp;
	}
	
	//Inner class
	@SuppressWarnings("deprecation")
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Date dtemp = new Date();
			int year = dtemp.getYear()+1900;
			int month = dtemp.getMonth();
			int day = dtemp.getDay();
			int daynum = dtemp.getDate();
			int hour = dtemp.getHours();
			int minute = dtemp.getMinutes();
			int second = dtemp.getSeconds();
			String ds = String.valueOf(year)+"/"+String.valueOf(month)+"/"+String.valueOf(daynum);
			String hh = "";
			String mm = "";
			String ss = "";
			if(hour<10) {
				hh = "0"+ String.valueOf(hour);
			}
			else {
				hh = String.valueOf(hour);
			}
			if(minute<10) {
				mm = "0"+ String.valueOf(minute);
			}
			else {
				mm = String.valueOf(minute);
			}
			if(second<10) {
				ss = "0"+String.valueOf(second);
			}
			else {
				ss = String.valueOf(second);
			}
			String ds2 = hh+" : "+mm + " : "+ss;
			String ds3 = getDayString(day);
			String ddd = ds+"    "+ds2+"    "+ds3;		
			dataLabel.setText(ddd);
			
		}
	}
}
