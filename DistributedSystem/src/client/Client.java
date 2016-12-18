package client;

import master.ServerBlock;
import util.MD5Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static util.Common.*;

/**
 * Created by Godfray on 2016/12/8.
 */
public class Client extends JFrame {

    private JTextArea textArea = new JTextArea();

    private JButton loginButton = new JButton("Login");
    private JButton refreshButton = new JButton("Refresh");
    private JButton uploadButton = new JButton("Upload");
    private JButton downloadButton = new JButton("Download");
    private JButton exitButton = new JButton("Exit");


    private JList<MyListItem> fileList = new JList<MyListItem>();
    private final DefaultListModel<MyListItem> model = new DefaultListModel<MyListItem>();
    private JScrollPane scrollPane;

    private ObjectOutputStream toMaster;
    private ObjectInputStream fromMaster;
    private Thread t = null;
    private Socket clientMasterSocket;
    private Socket clientServerSocket;

    private ServerSocket clientCheckServerSocket1;
    private ServerSocket clientCheckServerSocket2;

    private ServerBlock sbClient;

    public static void main(String[] args) {
        /*
        if (args.length != 2) {
            System.out.println("Usage: Client host(args[0]) directory(args[1])");
        }
        */

        new Client();
    }

    public Client() {
        String host = "localhost";
        String dir = "clientfile";
        setLayout(new BorderLayout());
        setTitle("Client");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p0= new JPanel();
        p0.setLayout(new GridLayout(2,1));

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        p0.add(new JScrollPane(textArea));

        fileList.setModel(model);
        fileList.setCellRenderer(new MyCellRenderer());
        p0.add(new JScrollPane(fileList));

        add(p0, BorderLayout.CENTER);

        JPanel p1= new JPanel();
        p1.setLayout(new GridLayout(1,5));
        p1.add(loginButton);
        p1.add(refreshButton);
        p1.add(uploadButton);
        p1.add(downloadButton);
        p1.add(exitButton);

        add(p1,BorderLayout.SOUTH);
        setVisible(true);


        loginButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            clientMasterSocket = new Socket(host, clientMasterPort);
                            toMaster = new ObjectOutputStream(clientMasterSocket.getOutputStream());
                            fromMaster = new ObjectInputStream(clientMasterSocket.getInputStream());

                            textArea.append((String)fromMaster.readObject()+'\n');
                            textArea.append((String)fromMaster.readObject()+'\n');

                            sbClient = (ServerBlock)fromMaster.readObject();

                            if(sbClient.getServerID()<0) {
                                textArea.append("No server available! \n");
                                JOptionPane.showMessageDialog(null, "No server available", "Unavailable", JOptionPane.ERROR_MESSAGE);
                                System.exit(-3);
                            }
                            textArea.append(" -------- Server Info -------\n");
                            textArea.append(sbClient.toString()+"\n");

                            clientCheckServerSocket1 = new ServerSocket(clientFile2ServerPort);
                            clientCheckServerSocket2 = new ServerSocket(clientFile2ServerPort2);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        } finally {
                            loginButton.setEnabled(false);
                        }
                    }
                }
        );

        refreshButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(clientMasterSocket == null)
                            System.exit(2);
                        try {
                            toMaster.writeObject(updateRequest);
                            toMaster.flush();
                            String allFiles = (String)fromMaster.readObject();
                            // textArea.append(allFiles+'\n');
                            String[] items = allFiles.split("\t");
                            model.clear();
                            for(String i: items) {
                                if(i.length()>=2) {
                                    MyListItem myIt = new MyListItem(i);
                                    model.addElement(myIt);
                                }
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
        );

        uploadButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (clientMasterSocket == null)
                            System.exit(2);
                        try {
                            JFileChooser jfc = new JFileChooser(dir);
                            jfc.showDialog(new JLabel(), "Select");
                            jfc.setVisible(true);
                            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                            File f = jfc.getSelectedFile();

                            DataOutputStream dos = null;
                            DataInputStream dis;
                            Socket fileSocket = null;
                            FileInputStream fis = null;

                            try {
                                if (f.isFile()) {
                                    // send to server
                                    textArea.append("Select file " + f);
                                    toMaster.writeObject("uploadrequest\t" + f.getName());

                                    ServerBlock sb = (ServerBlock)fromMaster.readObject();
                                    System.out.println(sb);

                                    if(sb == null) {
                                        textArea.append("Data error!");
                                        return;
                                    }

                                    fileSocket = new Socket(sb.getServerName(), sb.getPortClient());
                                    dos = new DataOutputStream(fileSocket.getOutputStream());
                                    dis = new DataInputStream(fileSocket.getInputStream());

                                    fis = new FileInputStream(f);

                                    dos.writeUTF("upload");
                                    dos.flush();

                                    while(true) {
                                        dos.writeUTF(f.getName());
                                        dos.flush();
                                        dos.writeLong(f.length());
                                        dos.flush();
                                        dos.writeUTF(MD5Util.getMD5(f));
                                        dos.flush();

                                        byte[] sendBytes = new byte[1024];
                                        int length = 0;
                                        while (true) {
                                            length = fis.read(sendBytes, 0, sendBytes.length);
                                            if (length < 0) {
                                                break;
                                            }
                                            dos.write(sendBytes, 0, length);
                                            dos.flush();
                                        }
                                        if(dos != null)
                                            dos.close();
                                        if(fis != null)
                                            fis.close();

                                        // Socket from server
                                        int p = sb.getPortClientCheck();
                                        int id = sb.getServerID();
                                        Socket fromServer;
                                        if(id == 1) {
                                            fromServer = clientCheckServerSocket1.accept();
                                        }
                                        else if(id == 2) {
                                            fromServer = clientCheckServerSocket2.accept();
                                        }
                                        else {
                                            fromServer = null;
                                        }
                                        if(fromServer != null) {
                                            DataInputStream dis2 = new DataInputStream(fromServer.getInputStream());
                                            String r = dis2.readUTF();
                                            if(r.equalsIgnoreCase("ok")) {
                                                System.out.println("Receive ok!");
                                                dis.close();
                                                fromServer.close();
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            } catch (ClassNotFoundException e1) {
                                e1.printStackTrace();
                            } finally {
                                if (fis != null)
                                    fis.close();
                                if (dos != null)
                                    dos.close();
                                if(fileSocket != null)
                                    fileSocket.close();
                            }
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }


                    }
                }
        );


        downloadButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(clientMasterSocket == null)
                            System.exit(2);
                        try {
                            String fileName = fileList.getSelectedValue().getString();
                            toMaster.writeObject(downloadRequest+"\t"+fileName);
                            toMaster.flush();

                            ServerBlock sb = (ServerBlock)fromMaster.readObject();
                            System.out.println(sb);

                            if(sb == null) {
                                textArea.append("Data error!");
                                return;
                            }

                            while(true) {
                                Socket fileSocket = new Socket(sb.getServerName(), sb.getPortClient());
                                DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                                DataInputStream dis = new DataInputStream(fileSocket.getInputStream());
                                dos.writeUTF("download");
                                dos.flush();
                                dos.writeUTF(fileName);
                                dos.flush();
                                // dos.writeUTF(fileName);

                                String f = dis.readUTF();
                                long fLength = dis.readLong();
                                String checkMD5 = dis.readUTF();
                                System.out.println(checkMD5);
                                File fff = new File(dir + "\\" + f);
                                FileOutputStream fos = new FileOutputStream(fff);
                                System.out.println("Starting receiving file <" + f + ">, size of file = " + fLength);
                                byte[] receiveBytes = new byte[1024];
                                int transLen = 0;
                                textArea.append("Starting receiving file <" + f + ">, size of file = " + fLength + "\n");

                                while (true) {
                                    int read = 0;
                                    read = dis.read(receiveBytes);
                                    System.out.println("read = " + read);
                                    if (read < 0)
                                        break;
                                    transLen += read;
                                    // textArea.append(".");
                                    // textArea.append("Progress: " + 100 * transLen / fLength + "%...\n");
                                    fos.write(receiveBytes, 0, read);
                                    fos.flush();
                                }

                                if (fos != null)
                                    fos.close();

                                String localMD5 = MD5Util.getMD5(fff);
                                if(localMD5.equalsIgnoreCase(checkMD5)) {
                                    textArea.append("Receiving file <" + f + "> success!\n");
                                    System.out.println("Receiving file <" + f + "> success!");
                                    if(dis != null)
                                        dis.close();
                                    if(fileSocket != null)
                                        fileSocket.close();
                                    break;
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
        );

        exitButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            toMaster.writeObject("end");
                            toMaster.flush();
                            if(clientMasterSocket != null) {
                                clientMasterSocket.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            System.exit(2);
                        }
                    }
                }
        );


        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                try {
                    toMaster.writeObject("end");
                    toMaster.flush();
                    if(clientMasterSocket != null) {
                        clientMasterSocket.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    System.exit(2);
                }
            }
        });
    }

    public void run() {
        /*
        while(true) {
            try {

                // Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        */

    }

    private class MyListItem {
        String text;

        public MyListItem(String text) {
            this.text=text;
        }

        public String getString() {
            return text;
        }

        public void setString(String s) {
            text=s;
        }

    }

    private class MyCellRenderer extends JLabel implements ListCellRenderer {
        private final Color color1 = new Color(176,196,222);
        //for the JLabel default to be transparent, so setOpaque true to set the background color
        //http://huangqiqing123.iteye.com/blog/1678208
        public MyCellRenderer() {
            this.setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            MyListItem myItem=(MyListItem)value;
            this.setText(myItem.getString());
            if(isSelected) {
                this.setBackground(color1);
            }
            else {
                this.setBackground(Color.WHITE);
            }

            return this;
        }


    }
}
