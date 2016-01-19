package util;

import java.util.Properties;

public class Tools {
	@SuppressWarnings("unused")
	private static Properties p=new Properties();
	
	/*
	 * 读取配置文件 
	static{
		try {
			p.load(Tools.class.getClassLoader().getResourceAsStream(DEFINE.SYS_CONFIG));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 根据key获得资源文件的value
	 
	public static String getValue(String key){
		System.out.println(key);
		return p.getProperty(key);
	}
		 */
}