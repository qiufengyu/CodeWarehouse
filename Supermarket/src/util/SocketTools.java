package util;


public class SocketTools {
	/*
	private static String host=Tools.getValue(DEFINE.SYS_HOST);//��������ַ
	private static int port=Integer.parseInt(Tools.getValue(DEFINE.SYS_PORT));//�������˿�
	
	/*
	 * �ͻ��˷���TCP���ݰ�
	 * ע�⣺Socket���ͽ���Ҫһ������ɣ�д�����������ᵼ��socket�رյĴ���
	 * ������obj�������ݶ���
	 * ����ֵ��Object��������
	  
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
	 * �������˷���TCP���ݰ�
	 * ����:obj:�������ݶ���socket�������˵�
	  
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