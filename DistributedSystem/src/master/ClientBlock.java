package master;

/**
 * Created by Godfray on 2016/12/12.
 */
public class ClientBlock {
    int clientID;
    int serverID;

    public ClientBlock() {
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }
}
