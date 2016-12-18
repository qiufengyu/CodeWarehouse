package server;

import master.ServerBlock;
import util.MD5Util;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import static util.Common.*;

public class Server2 {

    ServerSocket clientServerSocket;
    Socket serverMasterSocket;
    ServerSocket masterServerSocket;
    ServerSocket checkFileSocket;

    private ObjectOutputStream toMaster;
    private ObjectInputStream fromMaster;

    private int localClientNum;

    public static void main(String[] args) {
        /*
        if (args.length != 1) {
            System.out.println("Usage: Server directory(args[0])");
        }
        */
        new Server2();

    }

    public Server2() {
        String dir = "serverfile2";
        try {
            serverMasterSocket = new Socket("localhost", serverMasterPort);
            clientServerSocket = new ServerSocket(serverClientPort2);        // server2
            masterServerSocket = new ServerSocket(serverFile2MasterPort2);   // server2
            checkFileSocket = new ServerSocket(serverFile2MasterCheckPort2); // server2

            toMaster = new ObjectOutputStream(serverMasterSocket.getOutputStream());
            fromMaster = new ObjectInputStream(serverMasterSocket.getInputStream());

            localClientNum = 0;

            /*
            String info = (String) fromMaster.readObject();
            System.out.println(info);
            */

            ServerBlock serverBlock = new ServerBlock();
            serverBlock.setServerID(2);
            serverBlock.setServerName("localhost");
            serverBlock.setPortClient(serverClientPort2);            // server2
            serverBlock.setPortMaster(serverFile2MasterPort2);       // server2
            serverBlock.setPortClientCheck(clientFile2ServerPort2);  // server2
            serverBlock.setNumOfClients(0);

            toMaster.writeObject(serverBlock);
            toMaster.flush();

            // update with master
            toMaster.writeObject("update");
            toMaster.flush();

            String allFiles = (String)fromMaster.readObject();
            String[] items = allFiles.split("\t");
            for(String i: items) {
                File f = new File("serverfile2\\"+i); // server2
                if(!f.exists()) {
                    // fetch file from master
                    toMaster.writeObject("fetch\t"+i);
                    toMaster.flush();
                    Socket fileSocket = new Socket("localhost", masterFile2ServerPort);
                    DataInputStream dis = new DataInputStream(fileSocket.getInputStream());
                    String ff = dis.readUTF();
                    long fLength = dis.readLong();
                    FileOutputStream fos = new FileOutputStream(new File(dir+"\\"+ff));
                    System.out.println("Starting receiving file <"+ff+">, size of file = "+fLength);
                    byte[] receiveBytes = new byte[1024];
                    int transLen = 0;

                    while(true) {
                        int read = 0;
                        read = dis.read(receiveBytes);
                        System.out.println("read = "+read);
                        if(read < 0)
                            break;
                        transLen += read;
                        fos.write(receiveBytes, 0 , read);
                        fos.flush();
                    }

                    System.out.println("Receiving file <"+ff+"> success!");
                    if(fos != null)
                        fos.close();
                    if(dis != null)
                        dis.close();
                    // if(fileSocket != null)
                    // fileSocket.close();
                }
                System.out.println("Synchronized file "+ i);
            }

            ListenClientThread listenClientThread = new ListenClientThread();
            UpdateWithMasterThread updateWithMasterThread = new UpdateWithMasterThread();
            ListenMasterFileThread listenMasterFileThread = new ListenMasterFileThread();

            BeatCheckThread beatCheckThread = new BeatCheckThread();

            CheckNumOfClientThread checkNumOfClientThread = new CheckNumOfClientThread();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    class CheckNumOfClientThread extends Thread {
        public CheckNumOfClientThread() {
            start();
        }

        public void run() {
            try {
                while(true) {
                    Thread.sleep(600000);
                    System.err.println("#Clients = "+localClientNum);
                    if(localClientNum < 1) {
                        System.err.println("No clients! Now the server will stop...");
                        System.exit(5);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class BeatCheckThread extends Thread {

        Socket localSocket;
        DataOutputStream dos;
        DataInputStream dis;

        public BeatCheckThread() {
            try {
                localSocket = new Socket("localhost", serverBeatsPort);
                dos = new DataOutputStream(localSocket.getOutputStream());
                dis = new DataInputStream(localSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            start();
        }

        public void run() {
            try {
                while(true) {
                    Thread.sleep(600000);

                    int beat = dis.readInt();
                    localClientNum = beat;
                    System.err.println("receive "+beat);
                    dos.writeUTF("ack");
                    dos.flush();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Master dump");
                System.exit(-2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ListenMasterFileThread extends Thread {
        public ListenMasterFileThread() {
            start();
        }

        public void run() {
            while (true) {
                Socket master = null;
                try {
                    System.out.println("Listening Master....");
                    master = masterServerSocket.accept();
                    // System.out.println("Acceptted!");
                    HandleMasterFileThread masterThread = new HandleMasterFileThread(master);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class HandleMasterFileThread extends Thread {
        Socket socket;
        private DataOutputStream dos;
        private DataInputStream dis;


        public HandleMasterFileThread(Socket socket) {
            this.socket = socket;
            try {
                dos = new DataOutputStream(socket.getOutputStream()); // output to master
                dis = new DataInputStream(socket.getInputStream());   //input from server
            } catch (IOException e) {
                e.printStackTrace();
            }

            start();
        }

        public void run() {
            try {
                String line = new String();
                while (true) {
                    line = dis.readUTF();
                    System.out.println(line);
                    if(line != null) {
                        File f = new File("serverfile2\\"+line.trim());
                        if(f.exists()) {
                            dos.writeUTF("ok");
                            FileInputStream fis = new FileInputStream(f);
                            dos.writeUTF(f.getName());
                            dos.flush();
                            dos.writeLong(f.length());
                            dos.flush();
                            byte[] sendBytes = new byte[1024];
                            int length = 0;
                            while(true) {
                                length = fis.read(sendBytes, 0, sendBytes.length);
                                if(length < 0)
                                    break;
                                dos.write(sendBytes, 0, length);
                                dos.flush();
                            }
                            if(fis != null)
                                fis.close();
                        }
                        else {
                            dos.writeUTF("error");
                            break;
                        }
                        dos.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dis != null)
                        dis.close();
                    if (dos != null)
                        dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class UpdateWithMasterThread extends Thread {

        Socket updateSocket;

        private ObjectOutputStream updatetoMaster;
        private ObjectInputStream updatefromMaster;

        public UpdateWithMasterThread() {
            try {
                updateSocket = new Socket("localhost", serverMasterPort);
                updatetoMaster = new ObjectOutputStream(updateSocket.getOutputStream());
                updatefromMaster = new ObjectInputStream(updateSocket.getInputStream());
                updatetoMaster.writeObject(null);
                updatetoMaster.flush();
                System.out.println("Update with master");
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();
        }

        public void run() {
            // update with master
            try {
                while(true) {
                    updatetoMaster.writeObject("update");
                    updatetoMaster.flush();

                    String allFiles = (String) updatefromMaster.readObject();
                    String[] items = allFiles.split("\t");
                    for (String i : items) {
                        File f = new File("serverfile2\\" + i);
                        if (!f.exists()) {
                            // fetch file from master
                            updatetoMaster.writeObject("fetch\t" + i);
                            updatetoMaster.flush();
                            Socket fileSocket = new Socket("localhost", masterFile2ServerPort);
                            DataInputStream dis = new DataInputStream(fileSocket.getInputStream());
                            String ff = dis.readUTF();
                            long fLength = dis.readLong();
                            FileOutputStream fos = new FileOutputStream(new File("serverfile2\\" + ff));
                            System.out.println("Starting receiving file <" + ff + ">, size of file = " + fLength);
                            byte[] receiveBytes = new byte[1024];
                            int transLen = 0;

                            while (true) {
                                int read = 0;
                                read = dis.read(receiveBytes);
                                System.out.println("read = " + read);
                                if (read < 0)
                                    break;
                                transLen += read;
                                fos.write(receiveBytes, 0, read);
                                fos.flush();
                            }

                            System.out.println("Receiving file <" + ff + "> success!");
                            if (fos != null)
                                fos.close();
                            if (dis != null)
                                dis.close();
                            if (fileSocket != null)
                                fileSocket.close();
                        }
                        System.out.println("Synchronized file " + i);
                    }
                    Thread.sleep(6000);
                }
            }
            catch(UnknownHostException e){
                e.printStackTrace();
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    class ListenClientThread extends Thread {
        public ListenClientThread() {
            start();
        }

        public void run() {
            while(true) {
                Socket client = null;
                try {
                    System.out.println("Listening Clients....");
                    client = clientServerSocket.accept();
                    // System.out.println("Acceptted!");
                    System.out.println("Starting thread for Client " + new Date());
                    InetAddress inetAddress = client.getInetAddress();
                    System.out.println("Client hostname: "+inetAddress.getHostName()+"\tClient IP: "+inetAddress.getHostAddress());
                    localClientNum ++;
                    HandleClientThread clientThread = new HandleClientThread(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class HandleClientThread extends Thread {

        private Socket client;


        public HandleClientThread(Socket socket) {
            this.client = socket;
            start();
        }

        public void run() {
            try {
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                DataInputStream dis = new DataInputStream(client.getInputStream());

                String type = dis.readUTF().trim();

                if (type.equalsIgnoreCase("download")) {

                    String fileName = dis.readUTF();
                    String filePath = "serverfile2\\" + fileName;

                    File f = new File(filePath);

                    FileInputStream fis = new FileInputStream(f);

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
                        if (length < 0)
                            break;
                        System.out.println("length = " + length);
                        dos.write(sendBytes, 0, length);
                        dos.flush();
                    }
                    if (fis != null)
                        fis.close();

                    if (dos != null)
                        dos.close();

                }
                else if(type.equalsIgnoreCase("upload")) {
                    String f = new String();
                    long fLength = 0L;
                    while(true) {
                        f = dis.readUTF();                //file name
                        fLength = dis.readLong();         // file length
                        String checkMD5 = dis.readUTF();  // md5 of file
                        File ff = new File("serverfile2\\" + f);
                        FileOutputStream fos = new FileOutputStream(ff);
                        byte[] receiveBytes = new byte[1024];
                        int transLen = 0;
                        while (true) {
                            int read = 0;
                            read = dis.read(receiveBytes);
                            if (read < 0)
                                break;
                            transLen += read;
                            System.out.println("read = "+read);
                            System.out.print("\r");
                            System.out.print(makeBarBySignAndLength(100 * (int) transLen / (int) fLength) + String.format(" %.2f%%", (float) transLen / fLength * 100));
                            // System.out.print("Progress: " + "%...\n");
                            fos.write(receiveBytes, 0, read);
                            fos.flush();
                        }
                        // System.out.println();

                        if (fos != null)
                            fos.close();

                        Socket checkSocket = new Socket("localhost", clientFile2ServerPort);
                        DataOutputStream dos2 = new DataOutputStream(checkSocket.getOutputStream());

                        String localMD5 = MD5Util.getMD5(ff);

                        if(localMD5.equalsIgnoreCase(checkMD5)) {
                            dos2.writeUTF("OK");
                            dos2.flush();
                            break;
                        }
                        dos2.close();
                    }

                    if(dos != null)
                        dos.close();

                    // also upload the file to master!!!
                    Socket file2Master = new Socket("localhost", clientFile2MasterPort);
                    DataOutputStream dos2Master = new DataOutputStream(file2Master.getOutputStream());
                    // DataInputStream disFromMaster = new DataInputStream(file2Master.getInputStream());
                    FileInputStream fis2Master;
                    while(true) {
                        dos2Master.writeUTF(f);
                        dos2Master.flush();
                        dos2Master.writeLong(fLength);
                        dos2Master.flush();
                        File fff = new File("serverfile2\\" + f);
                        dos2Master.writeUTF(MD5Util.getMD5(fff));
                        dos2Master.flush();

                        fis2Master = new FileInputStream(fff);
                        byte[] sendBytes = new byte[1024];
                        int length = 0;
                        while (true) {
                            length = fis2Master.read(sendBytes, 0, sendBytes.length);
                            if (length < 0)
                                break;
                            dos2Master.write(sendBytes, 0, length);
                            dos2Master.flush();
                        }
                        if(dos2Master != null)
                            dos2Master.close();
                        if(fis2Master != null)
                            fis2Master.close();

                        Socket masterCheckSocket = checkFileSocket.accept();
                        DataInputStream disFromMaster = new DataInputStream(masterCheckSocket.getInputStream());

                        String r = disFromMaster.readUTF();
                        if (r.equalsIgnoreCase("ok")) {
                            break;
                        }

                        if (disFromMaster != null)
                            disFromMaster.close();
                    }
                }

                if (client != null)
                    client.close();
            } catch(IOException e) {
                // e.printStackTrace();
                localClientNum --;
            }
        }
    }
}