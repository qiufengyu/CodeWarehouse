package util;

import java.util.Properties;

public class Tools {
	@SuppressWarnings("unused")
	private static Properties p=new Properties();
	
	/*
	 * ��ȡ�����ļ� 
	static{
		try {
			p.load(Tools.class.getClassLoader().getResourceAsStream(DEFINE.SYS_CONFIG));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * ����key�����Դ�ļ���value
	 
	public static String getValue(String key){
		System.out.println(key);
		return p.getProperty(key);
	}
		 */
}