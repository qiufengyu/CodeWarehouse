package util;


public class FileTools {
	/*
	public static String path=Tools.getValue(DEFINE.SYS_PATH);//�����ļ�·��
	public static String path_user=path+Tools.getValue(DEFINE.SYS_DATA_USER);//�û������ļ�
	public static String path_member=path+Tools.getValue(DEFINE.SYS_DATA_MEMBER);//��Ա�����ļ�
	public static String path_goods=path+Tools.getValue(DEFINE.SYS_DATA_GOODS);//��Ʒ�����ļ�
	
	/*
	 * д�����ݵ��ļ�
	 * ����:obj������д�뵽�ļ�,fileName�ļ���ȫ��(����·��)
	 * ����ֵ:trueд��ɹ�,falseд��ʧ�� 
	  
	public static boolean writeData(Object obj,String fileName){
		String str_seek=getPathpole(fileName);
		String str=fileName.substring(0, fileName.lastIndexOf(str_seek));
		File dir=new File(str);
		if(!dir.exists()){
			//����ļ��в������򴴽��ļ���
			recursiveDir(fileName);
		}
		
		File file=new File(fileName);
		try {
			FileOutputStream fos= new FileOutputStream(file);
			ObjectOutputStream oos=new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * ��ȡ�ļ�����
	 * ����:fileName:�ļ�ȫ��(����·��)
	 * ����ֵ:�ɹ�����Object��ʧ�ܷ���null
	  
	public static Object readData(String fileName){
		File file=new File(fileName);
		if(!file.exists()){
			return null;
		}
		try {
			FileInputStream fis=new FileInputStream(file);
			ObjectInputStream ois=new ObjectInputStream(fis);
			Object obj=ois.readObject();
			ois.close();
			fis.close();
			return obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/*
	 * ���·����б��
	 * ����:path�ļ�·��
	 * ����ֵ:/��\
	  
	public static String getPathpole(String path){
		String str_seek="/";
		if(path.contains("\\")){
			str_seek="\\";
		}
		return str_seek;
	}
	
	/*
	 * �ݹ鴴���ļ���
	 * ����:path�ļ���·��(Ҫע���ļ���·�����һ��Ҫ����\��/)
	  
	public static void recursiveDir(String path){
		String str_seek=getPathpole(path);
		String str=path.substring(0, path.lastIndexOf(str_seek));
		File file_path=new File(str);
		if(!file_path.exists()){
			recursiveDir(str);
		}
		System.out.println("�ݹ���ô���·��"+path);
		file_path.mkdir();
	}

	/*
	 * �ж�ָ���ļ��Ƿ����
	 * ������fileName�ļ���(����·��)
	 * ����ֵ�����ڷ���true�������ڷ���false
	  
	public static boolean fileExists(String fileName){
		File file=new File(fileName);
		if(!file.exists()){
			return false;
		}
		return true;
	}
	*/
}