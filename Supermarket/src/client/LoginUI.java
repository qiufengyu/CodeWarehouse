package client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;
import java.net.*;

import entity.Datas;
import entity.User;
import util.DEFINE;

@SuppressWarnings("serial")
public class LoginUI extends JFrame {
	//Components
	JLabel head;
	JLabel idLabel;
	JLabel pwdLabel;
	JTextField idTextField;
	JPasswordField pwdField;
	JButton loginIn;
	JButton cancle;	
	private static final Font fontc = new Font("Î¢ÈíÑÅºÚ",Font.PLAIN+Font.BOLD,14);
	private static final Font fonte = new Font("Calibri",Font.PLAIN,14);
	
	//Information
	String id;
	String pwd;
	
	public Socket socket ;//= new Socket("localhost", 8000);//·þÎñÆ÷¹Ì¶¨ip
	public ObjectInputStream inputFromServer;// = new ObjectInputStream(socket.getInputStream());
	public ObjectOutputStream outputToServer;// = new ObjectOutputStream(socket.getOutputStream());

	public LoginUI() {
		try {
	        socket = new Socket("localhost", 8000);//·þÎñÆ÷¹Ì¶¨ip
	        // 
	        outputToServer = new ObjectOutputStream(socket.getOutputStream());
	        inputFromServer = new ObjectInputStream(socket.getInputStream());
	    }
	    catch (IOException ex) {
	        System.out.println(ex.toString());
	    }
		setSize(380, 290);
		//background
		head = new JLabel();
		ImageIcon icon = new ImageIcon("images/log/bg.png");
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(), icon.getIconHeight(), Image.SCALE_DEFAULT));
		head.setBounds(0, 0, 380, 290);  
        head.setHorizontalAlignment(0);  
        head.setIcon(icon);         
        
        //Username and password
        idLabel = new JLabel(" µÇÂ¼ÕËºÅ");
        pwdLabel = new JLabel(" µÇÂ¼ÃÜÂë");
        idLabel.setFont(fontc);
        idLabel.setForeground(Color.WHITE);
        pwdLabel.setFont(fontc);
        pwdLabel.setForeground(Color.WHITE);
        idTextField = new JTextField();
        idTextField.setFont(fonte);
        idTextField.setText("User ID/Name");
        pwdField = new JPasswordField();
        idLabel.setBounds(20, 110, 100, 27);
        pwdLabel.setBounds(20, 145, 100, 27);
        idTextField.setBounds(110, 110, 215, 27);
        pwdField.setBounds(110, 145, 215, 27);
        head.add(idLabel);
        head.add(pwdLabel);
        head.add(idTextField);
        head.add(pwdField);
        
        ImageIcon img1 = new ImageIcon("images/log/p1.png");
        loginIn = new JButton(img1);
       	loginIn.setOpaque(false);
       	loginIn.setBounds(45, 200, 110, 27);
        ImageIcon img4 = new ImageIcon("images/log/p2.png");
        cancle = new JButton(img4);
       	cancle.setOpaque(false);
       	cancle.setBounds(205, 200, 110, 27);
       	
       	head.add(loginIn);
       	head.add(cancle);      	
        
        add(head);
        
        //display
        this.setTitle("µÇÂ¼");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        
        //Listeners
        idTextField.addMouseListener(
        	new MouseAdapter() {
        		public void mouseClicked(MouseEvent me) {
        			idTextField.setText("");
        		}
        	}
        );
        
        loginIn.addActionListener(
			new ActionListener() {
				@SuppressWarnings("deprecation")
				public void actionPerformed(ActionEvent e) {
					id = idTextField.getText();
					pwd = pwdField.getText();
					System.out.println(id+"\t"+pwd);
					Datas sendd = new Datas();
					Datas recvd = new Datas();
					sendd.setFlags("LOGIN");					
					User sendu = new User();
					sendu.setUserid(id);
					sendu.setPassword(pwd);
					sendd.setUser(sendu);
					int auth = 0;
					try {
						outputToServer.writeObject(sendd);
						try {
							recvd = (Datas) inputFromServer.readObject();
						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						String recvf = recvd.getFlags();
						if(recvf.equals(DEFINE.SYS_LOGINSUCCESS)) {
							User recvu = recvd.getUser();
							auth = recvu.getAuthority();
						}
						else {
							JOptionPane.showMessageDialog(null,"µÇÂ½Ê§°Ü£¡");
							System.exit(-1);
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						System.exit(-1);
					}
					
					if(auth == 1) {
						@SuppressWarnings("unused")
						SimpleUI sui = new SimpleUI(id);
					}
					else if(auth == 2) {
						@SuppressWarnings("unused")
						ManagerUI mui = new ManagerUI(id);
					}
					else if(auth == 3) {
						@SuppressWarnings("unused")
						AdministratorUI aui = new AdministratorUI(id);
					}
					else {
						System.exit(-1);
					}
					setInvisible();
				}
			}
		);
        
        idTextField.addKeyListener(
        	new KeyAdapter() {
        		@SuppressWarnings("deprecation")
				public void keyReleased(KeyEvent e) {
					int keyCode = e.getKeyCode();
					if(keyCode == KeyEvent.VK_ENTER) {
						id = idTextField.getText();
						pwd = pwdField.getText();
						System.out.println(id+"\t"+pwd);
						Datas sendd = new Datas();
						Datas recvd = new Datas();
						sendd.setFlags("LOGIN");					
						User sendu = new User();
						sendu.setUserid(id);
						sendu.setPassword(pwd);
						sendd.setUser(sendu);
						int auth = 0;
						try {
							outputToServer.writeObject(sendd);
							try {
								recvd = (Datas) inputFromServer.readObject();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String recvf = recvd.getFlags();
							if(recvf.equals(DEFINE.SYS_LOGINSUCCESS)) {
								User recvu = recvd.getUser();
								auth = recvu.getAuthority();
							}
							else {
								JOptionPane.showMessageDialog(null,"µÇÂ½Ê§°Ü£¡");
								System.exit(-1);
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							System.exit(-1);
						}
						
						if(auth == 1) {
							@SuppressWarnings("unused")
							SimpleUI sui = new SimpleUI(id);
						}
						else if(auth == 2) {
							@SuppressWarnings("unused")
							ManagerUI mui = new ManagerUI(id);
						}
						else if(auth == 3) {
							@SuppressWarnings("unused")
							AdministratorUI aui = new AdministratorUI(id);
						}
						else {
							System.exit(-1);
						}
						setInvisible();
					}
	        	}
	        }
        );
        
        pwdField.addKeyListener(
            	new KeyAdapter() {
            		@SuppressWarnings("deprecation")
					public void keyReleased(KeyEvent e) {
    					int keyCode = e.getKeyCode();
    					if(keyCode == KeyEvent.VK_ENTER) {
    						id = idTextField.getText();
    						pwd = pwdField.getText();
    						System.out.println(id+"\t"+pwd);
    						Datas sendd = new Datas();
    						Datas recvd = new Datas();
    						sendd.setFlags("LOGIN");					
    						User sendu = new User();
    						sendu.setUserid(id);
    						sendu.setPassword(pwd);
    						sendd.setUser(sendu);
    						int auth = 0;
    						try {
    							outputToServer.writeObject(sendd);
    							try {
    								recvd = (Datas) inputFromServer.readObject();
    							} catch (ClassNotFoundException e1) {
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    							}
    							String recvf = recvd.getFlags();
    							if(recvf.equals(DEFINE.SYS_LOGINSUCCESS)) {
    								User recvu = recvd.getUser();
    								auth = recvu.getAuthority();
    							}
    							else {
    								JOptionPane.showMessageDialog(null,"µÇÂ½Ê§°Ü£¡");
    								System.exit(-1);
    							}
    						} catch (IOException e1) {
    							// TODO Auto-generated catch block
    							e1.printStackTrace();
    							System.exit(-1);
    						}
    						
    						if(auth == 1) {
    							@SuppressWarnings("unused")
    							SimpleUI sui = new SimpleUI(id);
    						}
    						else if(auth == 2) {
    							@SuppressWarnings("unused")
    							ManagerUI mui = new ManagerUI(id);
    						}
    						else if(auth == 3) {
    							@SuppressWarnings("unused")
    							AdministratorUI aui = new AdministratorUI(id);
    						}
    						else {
    							System.exit(-1);
    						}
    						setInvisible();
    					}
    	        	}
    	        }
            );
        
        cancle.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setInvisible();
				}
			}
    	);
	}
	
	public void setInvisible() {
		this.setVisible(false);
	}
	
	public String getID() {
		return id;
	}

}