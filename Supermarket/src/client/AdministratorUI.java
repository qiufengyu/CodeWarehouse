package client;


import java.awt.Font;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import util.DEFINE;
import entity.Datas;
import entity.Goods;
import entity.Trade;
import entity.User;

@SuppressWarnings("serial")
public class AdministratorUI extends JFrame{
	// socket?	
	
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
	
	//for Manager Components
	JButton returnButton;
	String returnid;
	String returnnum;
	
	JButton goodAdd;
	String addid;
	String addnum;
	String addname;
	String addprice;
	JButton goodEdit;
	String editid;
	String editnum;
	String editname;
	String editprice;
	JButton goodDelete;
	String deleteid;
	
	//for Administrator
	JButton employeeManager;
	String employeeid;
	String employeestate;
	
	JButton saleButton;
	
	//discount global 
	int discount = -1;
	
	//constants
	private static final Font fontc = new Font("΢���ź�",Font.PLAIN+Font.BOLD,20);
	private static final Font fonte = new Font("Calibri",Font.PLAIN,16);
	private static final Font fontc1 = new Font("΢���ź�",Font.PLAIN,14);
	private static final Font fontc2 = new Font("΢���ź�",Font.PLAIN+Font.BOLD,17);
	private static final Font fontc3 = new Font("΢���ź�",Font.PLAIN,12);
	private static final String splitline = "--------------------------------------------"
			+ "------------------------------------------------------------------"
			+ "------------------------------------";
	private static final String splitline2 = "--------------------------------------------"
			+ "------------------------------------------------------";
	
	private ObjectOutputStream outputToServer;
	private ObjectInputStream inputFromServer;
	private Socket socket;
	
	@SuppressWarnings("deprecation")
	public AdministratorUI(String id) {	
		try {
	        socket = new Socket("localhost", 8000);//�������̶�ip
	        outputToServer = new ObjectOutputStream(socket.getOutputStream());
	        inputFromServer = new ObjectInputStream(socket.getInputStream());
	    }
	    catch (IOException ex) {
	        System.out.println(ex.toString());
	    }
		setSize(600, 730);
		bg = new JLabel();
		//ImageIcon icon = new ImageIcon("images/bg1.jpg");
		//icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
		bg.setBounds(0, 0, 600, 730);  
        bg.setHorizontalAlignment(0);  
       // bg.setIcon(icon); 
        
        //head title
        ImageIcon headic = new ImageIcon("images/title.png");
		headTitle = new JLabel(headic);
		headTitle.setFont(fontc);
		headTitle.setBounds(200,10,200,25);
		bg.add(headTitle);
		
		//����
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
		
		//����Ա
		userLabel = new JLabel("����Ա��");
		userLabel.setFont(fontc1);
		userLabel.setBounds(25, 80, 75, 25);
		userInfo = new JTextField();
		userInfo.setText(id);
		userInfo.setFont(fonte);
		userInfo.setEditable(false);
		userInfo.setBounds(85, 80, 125, 25);
		bg.add(userLabel);
		bg.add(userInfo);
		
		//��Ա��Ϣ
		customerLabel = new JLabel("��Ա��ţ�");
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
		// ��Ʒ���
		goodIdLabel = new JLabel("��Ʒ��ţ�");
		goodIdLabel.setFont(fontc1);
		goodIdLabel.setBounds(25, 140, 105, 25);
		goodIdInfo = new JTextField();
		goodIdInfo.setFont(fonte);
		goodIdInfo.setEditable(true);
		goodIdInfo.setBounds(110, 140, 200, 25);
		bg.add(goodIdLabel);
		bg.add(goodIdInfo);
		
		// ��Ʒ����
		goodNumLabel = new JLabel("��Ʒ������");
		goodNumLabel.setFont(fontc1);
		goodNumLabel.setBounds(25, 176, 105, 25);
		goodNumInfo = new JTextField();
		goodNumInfo.setFont(fonte);
		goodNumInfo.setEditable(true);
		goodNumInfo.setBounds(110, 176, 200, 25);
		bg.add(goodNumLabel);
		bg.add(goodNumInfo);
		
		//buttons for add and reset
		addItemButton = new JButton("���");
		addItemButton.setFont(fontc1);
		addItemButton.setBounds(365, 158, 80, 25);
		resetItemButton = new JButton("����");
		resetItemButton.setFont(fontc1);
		resetItemButton.setBounds(470, 158, 80, 25);
		bg.add(addItemButton);
		bg.add(resetItemButton);
		
		//split line3
		s3 = new JLabel(splitline);
		s3.setBounds(0, 206, 600, 20);
		bg.add(s3);
		
		//listTitle
		listTitle = new JLabel("�����嵥");
		listTitle.setFont(fontc2);
		listTitle.setBounds(255, 225, 80, 25);
		bg.add(listTitle);
		
		//textArea+Scrollpanel
		tarea = new JTextArea();
		tarea.setEditable(false);
		tarea.setFont(fontc1);
		tarea.setText(" ���\t����\t\t����\t����\tС��\n"+splitline2+"\n");
		spanel = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		spanel.add(tarea);
		spanel.setBounds(0, 260, 600, 250);
		bg.add(spanel);
		
		//split line4
		s4 = new JLabel(splitline);
		s4.setBounds(0, 520, 600, 20);
		bg.add(s4);
		
		//Sum Label
		sumAmountLabel = new JLabel("�����ϼƣ� ");
		sumAmountLabel.setFont(fontc2);
		sumAmountLabel.setBounds(60, 540, 150, 25);
		
		sumAmountText = new JLabel();
		sumAmountText.setFont(fontc2);
		sumAmountText.setText("0");
		//��geText��������Stringת����Integer����������������setText
		sumAmountText.setBounds(150, 540, 100, 25);
		
		sumMoneyLabel = new JLabel("���ϼƣ� �� ");
		sumMoneyLabel.setFont(fontc2);
		sumMoneyLabel.setBounds(310, 540, 150, 25);
		
		sumMoneyText = new JLabel();
		sumMoneyText.setFont(fontc2);
		sumMoneyText.setText("0000.00");
		//��geText��������Stringת����Integer����������������setText
		sumMoneyText.setBounds(425, 540, 160, 25);
		
		// Money should be
		shouldMoneyLabel = new JLabel("Ӧ�ս� �� ");
		shouldMoneyLabel.setFont(fontc2);
		shouldMoneyLabel.setBounds(60, 570, 150, 25);
		
		shouldMoneyText = new JLabel();
		shouldMoneyText.setFont(fontc2);
		shouldMoneyText.setText("0000.00");
		shouldMoneyText.setBounds(175, 570, 160, 25);
		
		//ʵ�ս��
		inputMoneyLabel = new JLabel("ʵ�ս� �� ");
		inputMoneyLabel.setFont(fontc2);
		inputMoneyLabel.setBounds(310, 570, 150, 25);
		inputMoneyText = new JTextField();
		inputMoneyText.setFont(fonte);
		inputMoneyText.setEditable(true);
		inputMoneyText.setBounds(425, 570, 130, 25);		
		
		//�������
		changeLabel = new JLabel("����� �� ");
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
		
		//commit and next button
		commit = new JButton("����");
		commit.setFont(fontc1);
		commit.setBounds(360, 605, 80, 28);
		
		next = new JButton("��һλ");
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
		
		//return Button
		returnButton = new JButton("�˻�");
		returnButton.setFont(fontc3);
		returnButton.setBounds(30, 650, 80, 28);		
		bg.add(returnButton);	
		
		//Goods Manager
		goodAdd = new JButton("�����Ʒ");
		goodAdd.setFont(fontc3);
		goodAdd.setBounds(130, 650, 90, 28);		
		
		goodDelete = new JButton("ɾ����Ʒ");
		goodDelete.setFont(fontc3);
		goodDelete.setBounds(240, 650, 90, 28);	
		
		goodEdit = new JButton("�޸���Ʒ");
		goodEdit.setFont(fontc3);
		goodEdit.setBounds(350, 650, 90, 28);	
		
		bg.add(goodAdd);
		bg.add(goodDelete);
		bg.add(goodEdit);	
		
		//ad
		employeeManager = new JButton("��Ա����");
		employeeManager.setFont(fontc3);
		employeeManager.setBounds(460, 650, 90, 28);
		bg.add(employeeManager);
		
		//sales
		saleButton = new JButton("���۲�ѯ");
		saleButton.setFont(fontc3);
		saleButton.setBounds(460, 22, 90, 28);
		bg.add(saleButton);
		
		add(bg);	
		
		this.setTitle("����ϵͳ");
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
						
						double moneyincrement = 0;//Updata with С��
						
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
							// ������������󣬵õ����ۡ���Ʒ���ƣ������С�ƣ�������textarea
							flag = (getprice==0.0);
							if(!flag) {
								if(getname.length()>8)
									tarea.append(" "+getid+"\t"+getname+"\t"+getcount+"\t"+getprice+"\t"+getprice*Integer.parseInt(num)+"\n");						
								else 
									tarea.append(" "+getid+"\t"+getname+"\t\t"+getcount+"\t"+getprice+"\t"+getprice*Integer.parseInt(num)+"\n");						
							}// �����������ܼ�
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
										
							if(discount==-1) {
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
			}); 
		
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
						tarea.setText(" ���\t����\t\t����\t����\tС��\n"+splitline2+"\n");
						sumAmountText.setText("0");
						sumMoneyText.setText("0000.00");
						shouldMoneyText.setText("0000.00");
						inputMoneyText.setText("");
						changeText.setText("0.00");
						discount=-1;
					}
				}
			); 
		
		returnButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unused")
						ReturnGoods rg = new ReturnGoods();
					}
				}
			); 
		
		goodAdd.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unused")
						AddGoods ag = new AddGoods();
					}
				}
			); 
		
		goodEdit.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unused")
						EditGoods eg = new EditGoods();
					}
				}
			); 
		
		goodDelete.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unused")
						DeleteGoods eg = new DeleteGoods();
					}
				}
			); 
		
		employeeManager.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unused")
						EmployeeManager em = new EmployeeManager();
					}
				}
			); 
		
		saleButton.addActionListener(
			new ActionListener() {
				@SuppressWarnings("unused")
				public void actionPerformed(ActionEvent e) {
					Datas sendd = new Datas();
					Datas recvd = new Datas();
					sendd.setFlags(DEFINE.SYS_SALE_QUERY);
					try {
						outputToServer.writeObject(sendd);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						recvd = (Datas) inputFromServer.readObject();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					double sum = recvd.getSumTrade();
					int num = recvd.getNumTrade();
					ResultUI rui = new ResultUI(String.valueOf(num),String.valueOf(sum));
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
	
	//Inner class
	class ReturnGoods extends JFrame {
		JLabel bg1;
		JLabel idLabel;
		JLabel numLabel;
		JTextField idText;
		JTextField numText;
		JButton enterButton;
		JButton cancelButton;
		
		public ReturnGoods() {
			setSize(380, 180);
			bg1 = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg1.setBounds(0, 0, 380, 180);  
	        bg1.setHorizontalAlignment(0);  
	        // bg1.setIcon(icon); 
	        
	        idLabel = new JLabel("��Ʒ��ţ�");
	        idLabel.setFont(fontc1);
	        idLabel.setBounds(30, 20, 105, 25);
	        idText = new JTextField();
	        idText.setFont(fonte);
	        idText.setEditable(true);
	        idText.setBounds(115, 20, 220, 25);
			bg1.add(idLabel);
			bg1.add(idText);
			
			// ��Ʒ����
			numLabel = new JLabel("��Ʒ������");
			numLabel.setFont(fontc1);
			numLabel.setBounds(30, 55, 105, 25);
			numText = new JTextField();
			numText.setFont(fonte);
			numText.setEditable(true);
			numText.setBounds(115, 55, 220, 25);
			bg1.add(numLabel);
			bg1.add(numText);
			
			//buttons
			enterButton = new JButton("ȷ��");
			cancelButton = new JButton("ȡ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(80, 94, 80, 29);
			cancelButton.setFont(fontc1);
			cancelButton.setBounds(215, 94, 80, 29);
			bg1.add(cancelButton);
			bg1.add(enterButton);
			
			this.add(bg1);
			this.setTitle("�˻�ϵͳ");
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			
			enterButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						returnid = idText.getText();
						returnnum = numText.getText();
						if(!(returnid.equals("") || returnnum.equals(""))) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							Goods goods = new Goods();
							//send to sever
							goods.setGoodid(returnid);;
							goods.setCount(Integer.parseInt(returnnum));
							sendd.setGoods(goods);
							sendd.setFlags(DEFINE.SYS_RETURN_GOOD);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_RETURN_GOOD_SUCCESS)) {
								 JOptionPane.showMessageDialog(null, "�˻���ʾ", "�˻��ɹ�", JOptionPane.PLAIN_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "�˻���ʾ", "�˻�ʧ��", JOptionPane.ERROR_MESSAGE);
							}
							idText.setText("");
							numText.setText("");
							setVisible(false);
						}
						else {
							JOptionPane.showMessageDialog(null, "�˻���ʾ", "�˻�ʧ�� - ����������", JOptionPane.ERROR_MESSAGE);
							idText.setText("");
							numText.setText("");
						}
					}
				}
			); 
			
			cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						idText.setText("");
						numText.setText("");
						setVisible(false);
					}
				}
			); 
		}
	}

	//Inner class
	class AddGoods extends JFrame {
		JLabel bg1;
		JLabel idLabel;
		JLabel numLabel;
		JTextField idText;
		JTextField numText;
		JLabel nameLabel;
		JTextField nameText;
		JLabel priceLabel;
		JTextField priceText;
		JButton enterButton;
		JButton cancelButton;
		
		public AddGoods() {
			setSize(380, 265);
			bg1 = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg1.setBounds(0, 0, 380, 265);  
	        bg1.setHorizontalAlignment(0);  
	        // bg1.setIcon(icon); 
	        
	        idLabel = new JLabel("��Ʒ��ţ�");
	        idLabel.setFont(fontc1);
	        idLabel.setBounds(30, 20, 105, 25);
	        idText = new JTextField();
	        idText.setFont(fonte);
	        idText.setEditable(true);
	        idText.setBounds(115, 20, 220, 25);
			bg1.add(idLabel);
			bg1.add(idText);
			
			// ��Ʒ����
			numLabel = new JLabel("��Ʒ������");
			numLabel.setFont(fontc1);
			numLabel.setBounds(30, 55, 105, 25);
			numText = new JTextField();
			numText.setFont(fonte);
			numText.setEditable(true);
			numText.setBounds(115, 55, 220, 25);
			bg1.add(numLabel);
			bg1.add(numText);
			
			// ��Ʒ����
			nameLabel = new JLabel("��Ʒ���ƣ�");
			nameLabel.setFont(fontc1);
			nameLabel.setBounds(30, 90, 105, 25);
			nameText = new JTextField();
			nameText.setFont(fontc1);
			nameText.setEditable(true);
			nameText.setBounds(115, 90, 220, 25);
			bg1.add(nameLabel);
			bg1.add(nameText);
			
			// ��Ʒ����
			priceLabel = new JLabel("��Ʒ���ۣ�");
			priceLabel.setFont(fontc1);
			priceLabel.setBounds(30, 125, 105, 25);
			priceText = new JTextField();
			priceText.setFont(fonte);
			priceText.setEditable(true);
			priceText.setBounds(115, 125, 220, 25);
			bg1.add(priceLabel);
			bg1.add(priceText);
			
			//buttons
			enterButton = new JButton("ȷ��");
			cancelButton = new JButton("ȡ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(80, 170, 80, 29);
			cancelButton.setFont(fontc1);
			cancelButton.setBounds(215, 170, 80, 29);
			bg1.add(cancelButton);
			bg1.add(enterButton);
			
			this.add(bg1);
			this.setTitle("��Ʒ��Ϣϵͳ - �����Ʒ");
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			
			enterButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addid = idText.getText();
						addnum = numText.getText();
						addprice = priceText.getText();
						addname = nameText.getText();
						if(!(addid.equals("")||addnum.equals("") || addprice.equals("") || addname.equals(""))) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							Goods goods = new Goods();
							//send to sever
							goods.setGoodid(addid);;
							goods.setCount(Integer.parseInt(addnum));
							goods.setName(addname);
							goods.setPrice(Double.parseDouble(addprice));
							sendd.setGoods(goods);
							sendd.setFlags(DEFINE.SYS_ADD_GOOD);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_ADD_GOOD_SUCCESS)) {
								JOptionPane.showMessageDialog(null, "��ӳɹ�", "�����ʾ", JOptionPane.PLAIN_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "���ʧ��", "�����ʾ", JOptionPane.ERROR_MESSAGE);
							}
							idText.setText("");
							numText.setText("");
							priceText.setText("");
							nameText.setText("");
							setVisible(false);
						}
						else {
							JOptionPane.showMessageDialog(null, "���ʧ�� - ���������ݣ�", "�����ʾ", JOptionPane.ERROR_MESSAGE);
							idText.setText("");
							numText.setText("");
							priceText.setText("");
							nameText.setText("");
						}						
					}
				}
			); 
			
			cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						idText.setText("");
						numText.setText("");
						priceText.setText("");
						nameText.setText("");
						setVisible(false);
					}
				}
			); 
		}
	}
	
	//Inner class
	class EditGoods extends JFrame {
		JLabel bg1;
		JLabel idLabel;
		JLabel numLabel;
		JTextField idText;
		JTextField numText;
		JLabel nameLabel;
		JTextField nameText;
		JLabel priceLabel;
		JTextField priceText;
		JButton enterButton;
		JButton cancelButton;
		
		public EditGoods() {
			setSize(380, 265);
			bg1 = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg1.setBounds(0, 0, 380, 265);  
	        bg1.setHorizontalAlignment(0);  
	        // bg1.setIcon(icon); 
	        
	        idLabel = new JLabel("��Ʒ��ţ�");
	        idLabel.setFont(fontc1);
	        idLabel.setBounds(30, 20, 105, 25);
	        idText = new JTextField();
	        idText.setFont(fonte);
	        idText.setEditable(true);
	        idText.setBounds(115, 20, 220, 25);
			bg1.add(idLabel);
			bg1.add(idText);
			
			// ��Ʒ����
			numLabel = new JLabel("��Ʒ������");
			numLabel.setFont(fontc1);
			numLabel.setBounds(30, 55, 105, 25);
			numText = new JTextField();
			numText.setFont(fonte);
			numText.setEditable(true);
			numText.setBounds(115, 55, 220, 25);
			bg1.add(numLabel);
			bg1.add(numText);
			
			// ��Ʒ����
			nameLabel = new JLabel("��Ʒ���ƣ�");
			nameLabel.setFont(fontc1);
			nameLabel.setBounds(30, 90, 105, 25);
			nameText = new JTextField();
			nameText.setFont(fontc1);
			nameText.setEditable(true);
			nameText.setBounds(115, 90, 220, 25);
			bg1.add(nameLabel);
			bg1.add(nameText);
			
			// ��Ʒ����
			priceLabel = new JLabel("��Ʒ���ۣ�");
			priceLabel.setFont(fontc1);
			priceLabel.setBounds(30, 125, 105, 25);
			priceText = new JTextField();
			priceText.setFont(fonte);
			priceText.setEditable(true);
			priceText.setBounds(115, 125, 220, 25);
			bg1.add(priceLabel);
			bg1.add(priceText);
			
			//buttons
			enterButton = new JButton("ȷ��");
			cancelButton = new JButton("ȡ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(80, 170, 80, 29);
			cancelButton.setFont(fontc1);
			cancelButton.setBounds(215, 170, 80, 29);
			bg1.add(cancelButton);
			bg1.add(enterButton);
			
			this.add(bg1);
			this.setTitle("��Ʒ��Ϣϵͳ - �༭��Ʒ");
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			
			enterButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						editid = idText.getText();
						editnum = numText.getText();
						editprice = priceText.getText();
						editname = nameText.getText();
						if(!(editid.equals(""))) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							Goods goods = new Goods();
							//send to sever
							goods.setGoodid(editid);
							if(!editnum.equals("")) {
								goods.setCount(Integer.parseInt(editnum));
							}
							else {
								goods.setCount(-1);
							}
							goods.setName(editname);
							if(!editprice.equals("")) {
								goods.setPrice(Double.parseDouble(editprice));
							}
							else {
								goods.setPrice(-1.0);
							}
							sendd.setGoods(goods);
							sendd.setFlags(DEFINE.SYS_EDIT_GOOD);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_EDIT_GOOD_SUCCESS)) {
								 JOptionPane.showMessageDialog(null, "�༭�ɹ�", "�༭��ʾ", JOptionPane.PLAIN_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "�༭ʧ��", "�༭��ʾ", JOptionPane.ERROR_MESSAGE);
							}
							idText.setText("");
							numText.setText("");
							priceText.setText("");
							nameText.setText("");
							setVisible(false);
						}
						else {
							JOptionPane.showMessageDialog(null, "�༭ʧ�� - ���������ݣ�", "�༭��ʾ", JOptionPane.ERROR_MESSAGE);
							idText.setText("");
							numText.setText("");
							priceText.setText("");
							nameText.setText("");
						}
					}
				}
			); 
			
			cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						idText.setText("");
						numText.setText("");
						priceText.setText("");
						nameText.setText("");
						setVisible(false);
					}
				}
			); 
		}
	}

	//Inner class
	class DeleteGoods extends JFrame {
		JLabel bg1;
		JLabel idLabel;
		JTextField idText;
		JButton enterButton;
		JButton cancelButton;
		
		public DeleteGoods() {
			setSize(380, 145);
			bg1 = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg1.setBounds(0, 0, 380, 145);  
	        bg1.setHorizontalAlignment(0);  
	        // bg1.setIcon(icon); 
	        
	        idLabel = new JLabel("��Ʒ��ţ�");
	        idLabel.setFont(fontc1);
	        idLabel.setBounds(30, 20, 105, 25);
	        idText = new JTextField();
	        idText.setFont(fonte);
	        idText.setEditable(true);
	        idText.setBounds(115, 20, 220, 25);
			bg1.add(idLabel);
			bg1.add(idText);
			
			//buttons
			enterButton = new JButton("ȷ��");
			cancelButton = new JButton("ȡ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(80, 60, 80, 29);
			cancelButton.setFont(fontc1);
			cancelButton.setBounds(215, 60, 80, 29);
			bg1.add(cancelButton);
			bg1.add(enterButton);
			
			this.add(bg1);
			this.setTitle("��Ʒ��Ϣϵͳ - ɾ����Ʒ");
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			
			enterButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						deleteid = idText.getText();
						if(!deleteid.equals("")) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							Goods goods = new Goods();
							//send to sever
							goods.setGoodid(deleteid);;
							sendd.setGoods(goods);
							sendd.setFlags(DEFINE.SYS_DELETE_GOOD);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_DELETE_GOOD_SUCCESS)) {
								 JOptionPane.showMessageDialog(null, "ɾ���ɹ�", "ɾ����ʾ", JOptionPane.PLAIN_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "ɾ��ʧ��", "ɾ����ʾ", JOptionPane.ERROR_MESSAGE);
							}
							idText.setText("");
							setVisible(false);
						}
						else {
							JOptionPane.showMessageDialog(null, "ɾ��ʧ�� - �������ţ�", "ɾ����ʾ", JOptionPane.ERROR_MESSAGE);
							idText.setText("");
						}
					}
				}
			); 
			
			cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						idText.setText("");
						setVisible(false);
					}
				}
			); 
		}
	}

	//Inner class
	class EmployeeManager extends JFrame {
		JLabel bg1;
		JLabel idLabel;
		JLabel stateLabel;
		JTextField idText;
		JTextField stateText;
		JButton enterButton;
		JButton cancelButton;
		JButton deleteButton;
		
		public EmployeeManager() {
			setSize(380, 180);
			bg1 = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg1.setBounds(0, 0, 380, 180);  
	        bg1.setHorizontalAlignment(0);  
	        // bg1.setIcon(icon); 
	        
	        idLabel = new JLabel("Ա����ţ�");
	        idLabel.setFont(fontc1);
	        idLabel.setBounds(30, 20, 105, 25);
	        idText = new JTextField();
	        idText.setFont(fonte);
	        idText.setEditable(true);
	        idText.setBounds(115, 20, 220, 25);
			bg1.add(idLabel);
			bg1.add(idText);
			
			// Ա��״̬
			stateLabel = new JLabel("Ա��״̬��");
			stateLabel.setFont(fontc1);
			stateLabel.setBounds(30, 55, 105, 25);
			stateText = new JTextField();
			stateText.setFont(fonte);
			stateText.setEditable(true);
			stateText.setBounds(115, 55, 220, 25);
			bg1.add(stateLabel);
			bg1.add(stateText);
			
			//buttons
			enterButton = new JButton("ȷ��");
			cancelButton = new JButton("ȡ��");
			deleteButton = new JButton("ɾ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(30, 94, 80, 29);
			cancelButton.setFont(fontc1);
			cancelButton.setBounds(250, 94, 80, 29);
			deleteButton.setFont(fontc1);
			deleteButton.setBounds(140, 94, 80, 29);
			bg1.add(cancelButton);
			bg1.add(enterButton);
			bg1.add(deleteButton);
			
			this.add(bg1);
			this.setTitle("��Ա����ϵͳ");
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(true);
			
			enterButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						employeeid = idText.getText();
						employeestate = stateText.getText();
						if(!employeeid.equals("")) {
							int state = -1;
							if(!employeestate.equals("")) {
								state = Integer.parseInt(employeestate);
							}
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							//sendd.setFlags(DEFINE.SYS_EDIT_EMPLOEE);
							User u = new User();
							u.setUserid(employeeid);
							u.setAuthority(state);
							sendd.setFlags(DEFINE.SYS_EDIT_EMPLOYEE);
							sendd.setUser(u);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_EDIT_EMPLOYEE_SUCCESS)) {
								 JOptionPane.showMessageDialog(null, "�༭�ɹ�", "�༭��ʾ", JOptionPane.PLAIN_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "�༭ʧ��", "�༭��ʾ", JOptionPane.ERROR_MESSAGE);
							}
							idText.setText("");
							stateText.setText("");
							setVisible(false);
						}
						else {
							JOptionPane.showMessageDialog(null, "�޸�ʧ�� - ����������", "�޸���Ϣ", JOptionPane.ERROR_MESSAGE);
							idText.setText("");
							stateText.setText("");
						}
					}
				}
			); 
			
			cancelButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						idText.setText("");
						stateText.setText("");
						setVisible(false);
					}
				}
			);
			
			deleteButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String empid = idText.getText();
						if(!empid.equals("")) {
							Datas sendd = new Datas();
							Datas recvd = new Datas();
							//sendd.setFlags(DEFINE.SYS_EDIT_EMPLOEE);
							User u = new User();
							u.setUserid(empid);
							sendd.setFlags(DEFINE.SYS_DELETE_EMPLOYEE);
							sendd.setUser(u);
							try {
								outputToServer.writeObject(sendd);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String returnFlag = recvd.getFlags();
							if(returnFlag.equals(DEFINE.SYS_DELETE_EMPLOYEE_FAIL)) {
								JOptionPane.showMessageDialog(null, "ɾ��ʧ��", "ɾ����ʾ", JOptionPane.ERROR_MESSAGE);
							}
							else {
								JOptionPane.showMessageDialog(null, "ɾ���ɹ�", "ɾ����ʾ", JOptionPane.PLAIN_MESSAGE);
							}
						}
						else {
							JOptionPane.showMessageDialog(null, "ɾ��ʧ�� - ��������", "ɾ����ʾ", JOptionPane.ERROR_MESSAGE);
						}
						idText.setText("");
						stateText.setText("");
						setVisible(false);
					}
				}
			);
		}
	}
	
	//Inner class
	class ResultUI extends JFrame{
		JLabel bg;
		JLabel headresult;
		JLabel label1;
		JLabel label2;
		JLabel label3;
		JButton enterButton;
		
		public ResultUI(String s1, String s2) {
			setSize(380, 270);
			bg = new JLabel();
			// ImageIcon icon = new ImageIcon("images/log/bg.png");
			// icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
			bg.setBounds(0, 0, 380, 270);  
	        bg.setHorizontalAlignment(0);  
	        // bg.setIcon(icon); 
	        
	        //head title
	        ImageIcon headic = new ImageIcon("images/title.png");
	        headresult = new JLabel(headic);
	        headresult.setFont(fontc);
	        headresult.setBounds(85,25,200,25);
			bg.add(headresult);
			
	        ImageIcon moneyicon = new ImageIcon("images/icon.png");
	        label1 = new JLabel(moneyicon);
	        label1.setBounds(40, 75, 80, 80);
	        String s1append = "���������� "+s1;
	        label2 = new JLabel(s1append);
	        label2.setFont(fontc2);
	        label2.setBounds(155, 75, 190, 30);
	        String s2append = "���׽� "+s2;
	        label3 =new JLabel(s2append);
	        label3.setFont(fontc2);
	        label3.setBounds(155, 115, 190, 30);
	        
	        enterButton = new JButton("ȷ��");
			enterButton.setFont(fontc1);
			enterButton.setBounds(145, 175, 80, 29);
			bg.add(enterButton);
	        
	        bg.add(label1);
	        bg.add(label2);
	        bg.add(label3);
	        
	        add(bg);
	        //display
	        this.setTitle("���ײ�ѯ");
	        this.setLocationRelativeTo(null);
	        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        this.setVisible(true);
	        
	        //listeners
	        enterButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							setVisible(false);
						}
					}
				); 
		}	
	}
}