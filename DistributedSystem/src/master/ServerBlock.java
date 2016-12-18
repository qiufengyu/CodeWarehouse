package master;

import java.io.Serializable;
import java.net.Socket;

/**
 * Created by Godfray on 2016/12/10.
 */

public class ServerBlock implements Serializable {

    int serverID;               // Server ID
    String serverName;          // Name or IP
    int portClient;             // port for client transmission
    int portMaster;             // port for client transmission
    int portClientCheck;        // port for client file check
    int numOfClients;           // at most 5
    Socket socket;


    public ServerBlock() {
        serverName = "localhost";
        portClient = 7776;
        numOfClients = 0;
        socket = null;
    }

    public String getServerName() {
        return serverName;
    }

    public int getPortClient() {
        return portClient;
    }

    public int getNumOfClients() {
        return numOfClients;
    }

    public int getServerID() {
        return serverID;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getPortClientCheck() {
        return portClientCheck;
    }

    public void setPortClientCheck(int portClientCheck) {
        this.portClientCheck = portClientCheck;
    }

    public void setPortClient(int portClient) {
        this.portClient = portClient;
    }

    public int getPortMaster() {
        return portMaster;
    }

    public void setPortMaster(int portMaster) {
        this.portMaster = portMaster;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setNumOfClients(int numOfClients) {
        this.numOfClients = numOfClients;
    }

    @Override
    public String toString() {
        return "ID: "+serverID + "\tName: "+serverName +"\nPort Client: "+portClient+"\nPort Master: "+ portMaster+"\nNumOfClients: "+numOfClients+"\nSocket:"+socket+"\n";
    }




}
