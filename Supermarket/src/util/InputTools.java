package util;

import java.util.Scanner;

/*
 * �������빤����
 * ֻ�з�����ȷ��ֵ�Ž�������
 */
public class InputTools {
	
	/*
	 * ���̻�ȡInt��ʽ��ֵ
	 * ������inputMessage������ǰ��Ϣ,errorMessage�ǳ��ִ���ʱ�����
	 * ����ֵ��int����
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
	 * ���̻�ȡdouble��ʽ��ֵ
	 * ������inputMessage������ǰ����ʾ��errorMessage�ǳ��ִ���ʱ�����
	 * ����ֵ��double����
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
	 * ���̻�ȡfloat��ʽ��ֵ
	 * ������inputMessage������ǰ����ʾ��Ϣ��errorMessage�ǳ��ִ���ʱ�����
	 * ����ֵ��float����
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
	 * ���̻�ȡlong��ʽ��ֵ
	 * ������inputMessage������ǰ����ʾ��errorMessage�ǳ��ִ���ʱ�����
	 * ����ֵ��long����
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
	 * ���̻�ȡString��ʽ��ֵ
	 * ������inputMessage������ǰ����ʾ��errorMessage�ǳ��ִ���ʱ�����
	 * ����ֵ��String����
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