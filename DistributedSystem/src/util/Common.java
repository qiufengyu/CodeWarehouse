package util;

/**
 * Created by Godfray on 2016/12/9.
 */
public interface Common {
    public static final String updateRequest = "updaterequest";
    public static final String uploadRequest = "uploadrequest";
    public static final String downloadRequest = "downloadrequest";
    public static final String end = "end";

    public static final String clientSuffix = "clientfile\\";
    public static final String serverSuffix = "serverfile\\";

    public static final int clientMasterPort = 9999;
    public static final int masterFile2ClientPort = 9998;
    public static final int clientFile2MasterPort = 9997;
    public static final int clientFile2MasterPort2 = 9993;

    public static final int masterFile2ServerPort = 9996;

    public static final int serverFile2MasterPort = 9995;
    public static final int serverFile2MasterPort2 = 9994;

    public static final int serverClientPort = 6667;
    public static final int serverClientPort2 = 6662;



    public static final int clientFile2ServerPort = 6666;
    public static final int clientFile2ServerPort2 = 6663;

    public static final int serverFile2MasterCheckPort = 6665;
    public static final int serverFile2MasterCheckPort2 = 6664;

    public static final int serverMasterPort = 10777;

    public static final int serverBeatsPort = 10776;




    public static String makeBarBySignAndLength(int barSign) {
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i=1; i<=64; i++) {
            if (i < barSign) {
                bar.append("=");
            } else if (i == barSign) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");
        return bar.toString();
    }
}
