package util;


public class FileTools {
	/*
	public static String path=Tools.getValue(DEFINE.SYS_PATH);//数据文件路径
	public static String path_user=path+Tools.getValue(DEFINE.SYS_DATA_USER);//用户数据文件
	public static String path_member=path+Tools.getValue(DEFINE.SYS_DATA_MEMBER);//会员数据文件
	public static String path_goods=path+Tools.getValue(DEFINE.SYS_DATA_GOODS);//商品数据文件
	
	/*
	 * 写入数据到文件
	 * 参数:obj将对象写入到文件,fileName文件的全名(包括路径)
	 * 返回值:true写入成功,false写入失败 
	  
	public static boolean writeData(Object obj,String fileName){
		String str_seek=getPathpole(fileName);
		String str=fileName.substring(0, fileName.lastIndexOf(str_seek));
		File dir=new File(str);
		if(!dir.exists()){
			//如果文件夹不存在则创建文件夹
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
	 * 读取文件数据
	 * 参数:fileName:文件全名(包括路径)
	 * 返回值:成功返回Object，失败返回null
	  
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
	 * 获得路径的斜杆
	 * 参数:path文件路径
	 * 返回值:/或\
	  
	public static String getPathpole(String path){
		String str_seek="/";
		if(path.contains("\\")){
			str_seek="\\";
		}
		return str_seek;
	}
	
	/*
	 * 递归创建文件夹
	 * 参数:path文件夹路径(要注意文件夹路径最后一个要补齐\或/)
	  
	public static void recursiveDir(String path){
		String str_seek=getPathpole(path);
		String str=path.substring(0, path.lastIndexOf(str_seek));
		File file_path=new File(str);
		if(!file_path.exists()){
			recursiveDir(str);
		}
		System.out.println("递归调用创建路径"+path);
		file_path.mkdir();
	}

	/*
	 * 判断指定文件是否存在
	 * 参数：fileName文件名(包含路径)
	 * 返回值：存在返回true，不存在返回false
	  
	public static boolean fileExists(String fileName){
		File file=new File(fileName);
		if(!file.exists()){
			return false;
		}
		return true;
	}
	*/
}