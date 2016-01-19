package util;

import java.util.Scanner;

/*
 * 键盘输入工具类
 * 只有返回正确的值才结束输入
 */
public class InputTools {
	
	/*
	 * 键盘获取Int格式的值
	 * 参数：inputMessage是输入前信息,errorMessage是出现错误时的输出
	 * 返回值：int类型
	 */
	public static int getInt(String inputMessage,String errorMessage){
		int num=0;
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		
		try{
			if(inputMessage!=null){
				System.out.print(inputMessage);
			}
			num=input.nextInt();
			return num;
		}catch(Exception e){
			if(errorMessage!=null){
				System.out.print(errorMessage);
			}
			return getInt(inputMessage,errorMessage);
		}
	}
	
	/*
	 * 键盘获取double格式的值
	 * 参数：inputMessage是输入前的提示，errorMessage是出现错误时的输出
	 * 返回值：double类型
	 */
	public static double getDouble(String inputMessage,String errorMessage){
		double num=0;
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		
		try{
			if(inputMessage!=null){
				System.out.print(inputMessage);
			}
			num=input.nextDouble();
			return num;
		}catch(Exception e){
			if(errorMessage!=null){
				System.out.print(errorMessage);
			}
			return getDouble(inputMessage,errorMessage);
		}
	}
	
	/*
	 * 键盘获取float格式的值
	 * 参数：inputMessage是输入前的提示信息，errorMessage是出现错误时的输出
	 * 返回值：float类型
	 */
	public static float getFloat(String inputMessage,String errorMessage){
		float num=0;
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		
		try{
			if(inputMessage!=null){
				System.out.print(inputMessage);
			}
			num=input.nextFloat();
			return num;
		}catch(Exception e){
			if(errorMessage!=null){
				System.out.print(errorMessage);
			}
			return getFloat(inputMessage,errorMessage);
		}
	}
	
	/*
	 * 键盘获取long格式的值
	 * 参数：inputMessage是输入前的提示，errorMessage是出现错误时的输出
	 * 返回值：long类型
	 */
	public static long getLong(String inputMessage,String errorMessage){
		long num=0;
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		
		try{
			if(inputMessage!=null){
				System.out.print(inputMessage);
			}
			num=input.nextLong();
			return num;
		}catch(Exception e){
			if(errorMessage!=null){
				System.out.print(errorMessage);
			}
			return getLong(inputMessage,errorMessage);
		}
	}
	
	/*
	 * 键盘获取String格式的值
	 * 参数：inputMessage是输入前的提示，errorMessage是出现错误时的输出
	 * 返回值：String类型
	 */
	public static String getString(String inputMessage,String errorMessage){
		StringBuffer str=new StringBuffer();
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		
		try{
			if(inputMessage!=null){
				System.out.print(inputMessage);
			}
			str.append(input.next());
			return str.toString();
		}catch(Exception e){
			if(errorMessage!=null){
				System.out.print(errorMessage);
			}
			return getString(inputMessage,errorMessage);
		}
	}
}