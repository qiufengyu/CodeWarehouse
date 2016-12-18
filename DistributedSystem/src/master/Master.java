package master;

/**
 * Created by Godfray on 2016/12/10.
 */

import server.Server;
import server.Server2;
import util.MD5Util;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

import static util.Common.*;
import static util.Common.makeBarBySignAndLength;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import static util.Common.*;

/**
 * Created by Godfray on 2016/12/8.
 */
public class Master {

    static ServerSocket masterClientSocket;
    static ServerSocket masterServerSocket;
    static ServerSocket clientFile2MasterSocket;

    static ServerSocket beatsServerSocket;

    static int clientNo = 0;
    static Vector<ServerBlock> serverList;
    static Vector<ClientBlock> clientList;

    public static void main(String[] args) throws IOException {

        /*
        if (args.length != 1) {

            System.out.println("Usage: Master directory(args[1])");
            System.exit(-1);
        }
        */

        new Master();

    }

    public Master() throws IOException {

        masterClientSocket = new ServerSocket(clientMasterPort);
        masterServerSocket = new ServerSocket(serverMasterPort);
        clientFile2MasterSocket = new ServerSocket(clientFile2MasterPort);
        beatsServerSocket = new ServerSocket(serverBeatsPort);

        serverList = new Vector<>();
        clientList = new Vector<>();

        System.out.println("Master start at " + new Date());

        // listen and then handle a server
        ListenServerThread listenServerThread = new ListenServerThread();
        // listen and then handle a client
        ListenClientThread listenClientThread = new ListenClientThread();
        // check local files if they are the same
        LocalFileCheckThread localFileCheckThread = new LocalFileCheckThread();
        // when a client upload a file to server, then master will have 2 copies of the file
        ListenClientFile2MasterThread listenClientFile2MasterThread = new ListenClientFile2MasterThread();
        // check the servers in systems, if none, exit. (every 10min)
        CheckAvailableServer checkAvailableServer = new CheckAvailableServer();
        // close the idle server
        FreeServerClose freeServerClose = new FreeServerClose();
    }

    class CheckAvailableServer extends Thread {

        public CheckAvailableServer() {
            start();
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(serverList.size() < 1) {
                    System.err.println("No server in the system!");
                    new Server();
                    // System.exit(-4);
                }

            }
        }

    }

    class FreeServerClose extends Thread {

        public FreeServerClose() {
            start();
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(ServerBlock sb: serverList) {
                    if(sb.getNumOfClients() < 1) {
                        System.err.println("Free server#" + sb.getServerID()+" stopping...");
                        serverList.remove(sb);
                        break;
                    }
                }

            }
        }

    }



    class HandleClientThread extends Thread {

        private Socket socket;
        private ObjectInputStream inputFromClient;
        private ObjectOutputStream outputToClient;
        private ServerBlock sbToClient;
        /**
         * Constructor
         */
        public HandleClientThread(Socket socket, int clientNo) throws IOException {
            this.socket=socket;
            //Create data input and output streams
            inputFromClient = new ObjectInputStream(this.socket.getInputStream());
            outputToClient = new ObjectOutputStream(this.socket.getOutputStream());

            sbToClient = new ServerBlock();
            sbToClient.setServerID(-1);

            String loginInfo = " "+new Date();
            loginInfo += "\t ServerIP: "+InetAddress.getLocalHost();
            outputToClient.writeObject(loginInfo);

            String clientInfo = " You are client No."+ clientNo+ ".\t Client IP: "+socket.getInetAddress();
            outputToClient.writeObject(clientInfo);
            outputToClient.flush();

            if(serverList.size() == 0) {
                System.out.println("No server, start new Server!");
                new Server();
            }


            //Find a server
            for(ServerBlock sb: serverList) {
                if(sb.getNumOfClients() < 5) {
                    sb.setNumOfClients(sb.getNumOfClients()+1);
                    sbToClient = sb;
                    break;
                }
            }

            System.err.println(sbToClient);

            /*
            //Find a server
            for(ServerBlock sb: serverList) {
                if(sb.getNumOfClients() < 5) {
                    sb.setNumOfClients(sb.getNumOfClients()+1);
                    sbToClient = sb;
                    break;
                }
            }
            */

            // if there is not enough server, start a new server
            // we have 2 server now...
            if(sbToClient.getServerID() == -1) {
                for(ServerBlock sb: serverList) {
                    if(sb.getServerID() == 1) {
                        new Server2();
                        System.out.println("Not enough server, start new Server2!");
                        break;
                    }
                    else {
                        new Server();
                        System.out.println("Not enough server, start new Server1!");
                        break;
                    }
                }
                for(ServerBlock sb: serverList) {
                    if(sb.getNumOfClients() < 5) {
                        sb.setNumOfClients(sb.getNumOfClients()+1);
                        sbToClient = sb;
                        break;
                    }
                }

            }


            System.err.println(sbToClient);


            ClientBlock cb = new ClientBlock();
            cb.setClientID(clientNo);
            cb.setServerID(sbToClient.getServerID());
            clientList.add(cb);

            outputToClient.writeObject(sbToClient);
            outputToClient.flush();

            start();
        }


        public void run() {

            try {
                String line = (String)inputFromClient.readObject();

                while(true) {
                    if(line.equalsIgnoreCase(end)) {
                        sbToClient.setNumOfClients(sbToClient.getNumOfClients()-1);
                        System.out.println("Client exit");
                        for(ClientBlock cbDelete: clientList) {
                            if(cbDelete.getClientID() == clientNo) {
                                clientList.remove(cbDelete);
                                break;
                            }
                        }
                        break;
                    }

                    if(line.equalsIgnoreCase(updateRequest)) {
                        System.out.println(line);
                        File f = new File("masterfile");
                        File[] files = f.listFiles();
                        String fileNames = "";
                        for(File file: files) {
                            fileNames = fileNames + file.getName() +"\t";
                        }
                        System.out.println(fileNames);
                        outputToClient.writeObject(fileNames);
                        outputToClient.flush();
                    }
                    else if(line.startsWith(downloadRequest)) {

                        System.out.println(line);

                        if(sbToClient != null) {
                            int currentID = sbToClient.getServerID();
                            boolean flag = false;
                            for(ServerBlock sbs: serverList) {
                                if(sbs.getServerID() == currentID) {
                                    flag = true;
                                }
                            }
                            if(flag) {
                                outputToClient.writeObject(sbToClient);
                            }
                            else {
                                for(ServerBlock sb: serverList) {
                                    if(sb.getNumOfClients() < 5) {
                                        sb.setNumOfClients(sb.getNumOfClients()+1);
                                        sbToClient = sb;
                                        break;
                                    }
                                }
                                outputToClient.writeObject(sbToClient);
                            }
                        }
                        else {
                            outputToClient.writeObject(null);
                        }

                    }
                    else if(line.startsWith(uploadRequest)) {

                        System.out.println(line);

                        if(sbToClient != null) {
                            int currentID = sbToClient.getServerID();
                            boolean flag = false;
                            for(ServerBlock sbs: serverList) {
                                if(sbs.getServerID() == currentID) {
                                    flag = true;
                                }
                            }
                            if(flag) {
                                outputToClient.writeObject(sbToClient);
                            }
                            else {
                                for(ServerBlock sb: serverList) {
                                    if(sb.getNumOfClients() < 5) {
                                        sb.setNumOfClients(sb.getNumOfClients()+1);
                                        sbToClient = sb;
                                        break;
                                    }
                                }
                                outputToClient.writeObject(sbToClient);
                            }
                        }
                        else {
                            outputToClient.writeObject(null);
                        }
                    }
                    // sbToClient.setNumOfClients(sbToClient.getNumOfClients()-1);
                    line = (String)inputFromClient.readObject();
                }
                outputToClient.flush();
                if(inputFromClient != null)
                    inputFromClient.close();
                if(outputToClient != null)
                    outputToClient.close();
                if(socket != null)
                    socket.close();

            } catch (IOException e) {
                // e.printStackTrace();
                if(sbToClient != null) {
                    sbToClient.setNumOfClients(sbToClient.getNumOfClients() - 1);
                    for (ClientBlock cbDelete : clientList) {
                        if (cbDelete.getClientID() == clientNo) {
                            clientList.remove(cbDelete);
                            break;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    class ListenBeatsThread extends Thread {

        int id;

        public ListenBeatsThread(int id) {
            this.id = id;
            start();
        }

        public void run() {
            try {
                while(true) {
                    Socket socket = beatsServerSocket.accept();
                    HandleBeatsThread handleBeatsThread = new HandleBeatsThread(socket, this.id);
                    // Thread.sleep(1000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    // check the running servers by beats
    class HandleBeatsThread extends Thread {

        private Socket beatsSocket;
        private int id;
        private DataInputStream dis;
        private DataOutputStream dos;

        public HandleBeatsThread(Socket socket, int id) {
            try {
                this.beatsSocket = socket;
                this.id = id;
                dos = new DataOutputStream(beatsSocket.getOutputStream());
                dis = new DataInputStream(beatsSocket.getInputStream());
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(6000);
                    for(ServerBlock sb: serverList) {
                        if(sb.getServerID() == id) {
                            /*
                            dos.writeInt(sb.getNumOfClients());
                            dos.flush();
                            System.err.println("write beat "+ sb.getNumOfClients());
                            String ack = dis.readUTF();
                            System.err.println(ack);
                            */
                            System.out.println("send urgent data");
                            beatsSocket.sendUrgentData(0xFF);
                            break;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    for (ServerBlock sb2 : serverList) {
                        if (sb2.getServerID() == id) {
                            System.err.println("this server#" + id + " exit!");
                            serverList.remove(sb2);
                            break;
                        }
                    }
                    break;

                } catch (InterruptedException e) {
                    // e.printStackTrace();
                    for (ServerBlock sb2 : serverList) {
                        if (sb2.getServerID() == id) {
                            System.err.println("this server#" + id + " exit!");
                            serverList.remove(sb2);
                            break;
                        }
                    }
                    break;
                }
            }


        }

    }

    class ListenClientFile2MasterThread extends Thread {

        public ListenClientFile2MasterThread() {
            start();
        }

        public void run() {
            while (true) {
                Socket client = null;
                try {
                    System.out.println("Waiting for client files....");
                    client = clientFile2MasterSocket.accept();
                    HandleClientFile2MasterThread clientFile2MasterThread = new HandleClientFile2MasterThread(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    class HandleClientFile2MasterThread extends Thread {

        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        public HandleClientFile2MasterThread(Socket socket) {
            this.socket = socket;

            try {
                this.dos = new DataOutputStream(this.socket.getOutputStream());
                this.dis = new DataInputStream(this.socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();
        }

        public void run() {
            try {
                while(true) {
                    String fName = dis.readUTF();
                    long fLength = dis.readLong();
                    String checkMD5 = dis.readUTF();
                    File ff = new File("masterfile\\" + fName);
                    FileOutputStream fos = new FileOutputStream(ff);
                    byte[] receiveBytes = new byte[1024];
                    int transLen = 0;
                    while (true) {
                        int read = 0;
                        read = dis.read(receiveBytes);
                        if (read < 0)
                            break;
                        transLen += read;
                        System.out.print("\r");
                        System.out.print(makeBarBySignAndLength(100 * (int) transLen / (int) fLength) + String.format(" %.2f%%", (float) transLen / fLength * 100));
                        // System.out.print("Progress: " + "%...\n");
                        fos.write(receiveBytes, 0, read);
                        fos.flush();
                    }
                    fos.close();

                    String localMD5 = MD5Util.getMD5(ff);

                    Socket checkSocket;
                    try {
                        checkSocket = new Socket("localhost", serverFile2MasterCheckPort);
                    } catch(IOException ee) {
                        checkSocket = new Socket("localhost", serverFile2MasterCheckPort2);
                    }
                    if(checkSocket != null) {
                        DataOutputStream dos2 = new DataOutputStream(checkSocket.getOutputStream());
                        if (localMD5.equalsIgnoreCase(checkMD5)) {
                            System.out.println("Receive from server ok!");
                            dos2.writeUTF("ok");
                            dos2.flush();
                            dos2.close();
                            checkSocket.close();
                            break;
                        }
                    }

                }
                dis.close();
                dos.close();
                socket.close();

            } catch (IOException e) {
                // e.printStackTrace();
                try {
                    dis.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

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
                    System.out.println("Listening clients....");
                    client = masterClientSocket.accept();
                    System.out.println(" Starting thread for Client "+ (clientNo+1) + " at " + new Date());
                    InetAddress inetAddress = client.getInetAddress();
                    System.out.println(" Client hostname: "+inetAddress.getHostName()+"\tClient IP: "+inetAddress.getHostAddress());
                    clientNo ++;
                    HandleClientThread clientThread = new HandleClientThread(client, clientNo);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    class ListenServerThread extends Thread {

        public ListenServerThread() {
            start();
        }

        public void run() {
            while(true) {
                Socket server = null;
                try {
                    System.out.println("Listening servers....");
                    server = masterServerSocket.accept();
                    // System.out.println("Acceptted!");
                    System.out.println("Starting thread for Server " + new Date());
                    InetAddress inetAddress = server.getInetAddress();
                    System.out.println("Server hostname: "+inetAddress.getHostName()+"\tServer IP: "+inetAddress.getHostAddress());
                    HandleServerThread serverThread = new HandleServerThread(server);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class HandleServerThread extends Thread {

        private Socket server;
        private ObjectInputStream inputFromServer;
        private ObjectOutputStream outputToServer;
        private int serverID;

        /**
         * Constructor
         */
        public HandleServerThread(Socket socket) throws IOException {
            server = socket;
            // Create data input and output streams
            // System.out.println("HandleServerThread");

            outputToServer = new ObjectOutputStream(server.getOutputStream());
            inputFromServer = new ObjectInputStream(server.getInputStream());

            // System.out.println(inputFromServer);

            /*
            String loginInfo = " " + new Date();
            System.out.println(loginInfo);
            outputToServer.writeObject(loginInfo);
            outputToServer.flush();
            */

            try {
                ServerBlock sb = (ServerBlock) inputFromServer.readObject();
                if(sb != null) {
                    serverID = sb.getServerID();
                    serverList.add(sb);
                    System.out.println(sb);
                    ListenBeatsThread listenBeatsThread = new ListenBeatsThread(serverID);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            start();
        }


        public void run() {
            try {
                String line = new String();
                while (true) {
                    System.out.println(line);
                    if(line.equalsIgnoreCase("update")) {
                        File f = new File("masterfile");
                        File[] files = f.listFiles();
                        String fileNames = "";
                        for(File file: files) {
                            fileNames = fileNames + file.getName() +"\t";
                        }
                        // System.out.println(fileNames);
                        outputToServer.writeObject(fileNames);
                        outputToServer.flush();
                    }
                    else if(line.startsWith("fetch")) {
                        String name = line.split("\t")[1];
                        File ff = new File("masterfile\\"+name);
                        ServerSocket fileServerSocket = new ServerSocket(masterFile2ServerPort);
                        Socket fileClientSocket = fileServerSocket.accept();
                        FileInputStream fis = new FileInputStream(ff);
                        DataOutputStream dos = new DataOutputStream(fileClientSocket.getOutputStream());
                        dos.writeUTF(ff.getName());
                        dos.flush();
                        dos.writeLong(ff.length());
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
                        if(dos != null)
                            dos.close();
                        if(fileServerSocket != null)
                            fileServerSocket.close();
                        if(fileClientSocket != null)
                            fileClientSocket.close();
                    }
                    // Thread.sleep(500);
                    line = (String)inputFromServer.readObject();
                }

            } catch (IOException e) {
                // e.printStackTrace();
                try {
                    server.close();
                    // this server can not use any more....
                    /*
                    for(ServerBlock sbDelete: serverList) {
                        if(sbDelete.getServerID() == serverID) {
                            serverList.remove(sbDelete);
                            break;
                        }
                    }
                    */


                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    class LocalFileCheckThread extends Thread {

        /**
         * Constructor
         */
        public LocalFileCheckThread() throws IOException {
            start();
        }


        public void run() {
            try {
                while(true) {
                    File path1 = new File("masterfile");
                    File path2 = new File("masterfile2");
                    HashSet<String> set1 = new HashSet<String>();
                    HashSet<String> set2 = new HashSet<String>();
                    File[] fList1 = path1.listFiles();
                    File[] fList2 = path2.listFiles();
                    for(File f1: fList1) {
                        set1.add(f1.getName());
                    }
                    for(File f2: fList2) {
                        set2.add(f2.getName());
                    }
                    for(String x1: set1) {
                        if(set2.contains(x1)) {
                            // check if they have same md5
                            File f1 = new File("masterfile\\"+x1);
                            File f2 = new File("masterfile2\\"+x1);
                            String md51 = MD5Util.getMD5(f1);
                            String md52 = MD5Util.getMD5(f2);
                            // System.out.println(f1.getName()+": "+md51+"\n"+f2.getName()+": "+md52);
                            if(md51.equalsIgnoreCase(md52)) // ok, files are the same
                                continue;
                            else {
                                System.out.println("(not same file "+ x1 +"!) Check from server...");
                                for(ServerBlock sb: serverList) {
                                    Socket fsocket = new Socket(sb.getServerName(), sb.getPortMaster());
                                    DataOutputStream dos = new DataOutputStream(fsocket.getOutputStream());  // output to server
                                    DataInputStream dis = new DataInputStream(fsocket.getInputStream());     // input from server
                                    dos.writeUTF(x1);
                                    System.err.println(x1);
                                    String result = dis.readUTF();
                                    System.out.println("result = "+result);
                                    if(result.equalsIgnoreCase("error")) {
                                        System.out.println("This server do not have file "+x1);
                                        break;
                                    }
                                    else {
                                        String ff = dis.readUTF();
                                        long fLength = dis.readLong();
                                        FileOutputStream fos = new FileOutputStream(new File("masterfilecache\\"+ff));

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


                                        // now start compare the three files
                                        String md53 = MD5Util.getMD5(new File("masterfilecache\\"+ff));
                                        if(md53.equalsIgnoreCase(md51)) { // serverfile2 is wrong
                                            System.out.println("masterfile2 is wrong");
                                            f2.delete();
                                            Files.copy(f1.toPath(), f2.toPath());
                                        }
                                        else if(md53.equalsIgnoreCase(md52)) {
                                            System.out.println("masterfile is wrong");
                                            f1.delete();
                                            try {
                                                Files.copy(f2.toPath(), f1.toPath());
                                            } catch (java.nio.file.FileAlreadyExistsException faee) {
                                                f1.delete();
                                            }
                                        }
                                        else {
                                            System.out.println("Both error, invalid file");
                                            f1.delete();
                                            f2.delete();
                                        }
                                    }
                                }
                            }

                        }
                        else {
                            System.out.println("(masterfile2 do not have file "+ x1 +"!) Fetch from server...");
                            for(ServerBlock sb: serverList) {
                                Socket fsocket = new Socket(sb.getServerName(), sb.getPortMaster());
                                DataOutputStream dos = new DataOutputStream(fsocket.getOutputStream());  // output to server
                                DataInputStream dis = new DataInputStream(fsocket.getInputStream());     // input from server
                                dos.writeUTF(x1);
                                String result = dis.readUTF();
                                System.out.println(result);
                                if(result.equalsIgnoreCase("error")) {
                                    System.out.println("This server do not have file "+x1);
                                    break;
                                }
                                else {
                                    String ff = dis.readUTF();
                                    long fLength = dis.readLong();
                                    FileOutputStream fos = new FileOutputStream(new File("masterfile2\\"+ff));

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
                                    // if(fsocket != null)
                                        // fsocket.close();
                                }
                            }
                        }
                    }
                    for(String x2: set2) {
                        if(set1.contains(x2)) {
                            continue;
                        }
                        else {
                            System.out.println("(masterfile do not have "+ x2 +"!)  Fetch from server...");
                            for(ServerBlock sb: serverList) {
                                Socket fSocket = new Socket(sb.getServerName(), sb.getPortMaster());
                                DataOutputStream dos = new DataOutputStream(fSocket.getOutputStream());  // output to server
                                DataInputStream dis = new DataInputStream(fSocket.getInputStream());     // input from server
                                dos.writeUTF(x2);
                                String result = dis.readUTF();
                                System.out.println(result);
                                if(result.equalsIgnoreCase("error")) {
                                    System.out.println("This server do not have file "+x2);
                                    break;
                                }
                                else {
                                    String ff = dis.readUTF();
                                    long fLength = dis.readLong();
                                    FileOutputStream fos = new FileOutputStream(new File("masterfile\\"+ff));

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
                                    if(fSocket != null)
                                        fSocket.close();
                                }
                            }
                        }
                    }

                    Thread.sleep(5000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("Unavailable");
                System.exit(3);
            }

        }
    }

}
