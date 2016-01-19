package util;


public class SocketTools {
	/*
	private static String host=Tools.getValue(DEFINE.SYS_HOST);//服务器地址
	private static int port=Integer.parseInt(Tools.getValue(DEFINE.SYS_PORT));//服务器端口
	
	/*
	 * 客户端发送TCP数据包
	 * 注意：Socket发送接收要一次性完成，写成两个函数会导致socket关闭的错误
	 * 参数：obj发送数据对象
	 * 返回值：Object类型数据
	  
	public static Object sendData(Object obj){
		try {
			Socket socket=new Socket(host,port);
			OutputStream os=socket.getOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(os);
			oos.writeObject(obj);
			oos.flush();
			
			InputStream is=socket.getInputStream();
			ObjectInputStream ois=new ObjectInputStream(is);
			obj=ois.readObject();
			
			ois.close();
			is.close();
			oos.close();
			os.close();
			socket.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 服务器端发送TCP数据包
	 * 参数:obj:发送数据对象，socket服务器端的
	  
	public static void sendData(Object obj,Socket socket){
		try {
			OutputStream os=socket.getOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(os);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			os.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
	}
}
*/
}