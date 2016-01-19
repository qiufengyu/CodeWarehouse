package entity;

public class UserExt {
	/*
	//用户数据文件
	private static String path_user=Tools.getValue(DEFINE.SYS_PATH)+Tools.getValue(DEFINE.SYS_DATA_USER);
	
	/*
	 * 读取用户列表
	 * 返回值:HashMap数组类型的User
	  
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,User> getUserList(){
		return (HashMap<Integer,User>) FileTools.readData(path_user);
	}
	
	/*
	 * 写入用户列表
	 * 参数:map_user是HashMap类型的User数组
	 * 返回值:写入成功返回true，写入失败返回false
	  
	public static boolean setUserList(HashMap<Integer,User> map_user){
		return FileTools.writeData(map_user, path_user);
	}
	
	/*
	 * 客户端登录请求
	  
	public static void login(){
		Datas datas=new Datas();
		User user=new User();

			int uid=InputTools.getInt(Message.INPUT_USERID, Message.ERR_USERID);
			user.setUserid(uid);
			user.setPassword(InputTools.getString(Message.INPUT_PASSWORD, Message.ERR_LOGIN));
			datas.setUser(user);
			datas.setFlags(DEFINE.SYS_LOGIN);
			Datas datas2=(Datas) SocketTools.sendData(datas);
			
			if(datas2==null){
				System.out.println(Message.ERR_LOGIN);
				login();
				return;
			}
			if(datas2.getFlags().equals(DEFINE.SYS_LOGINFAIL)){
				System.out.println(Message.ERR_LOGIN);
				login();
				return;
			}
			if(datas2.getFlags().equals(DEFINE.SYS_LOGINSUCCESS)){
				if(datas2.getUser().getAuthority().equals(DEFINE.SYS_USER_MANAGER)){
					System.out.println(Message.OUTPUT_START+Message.APP_WELCOM+Message.APP_NAME_STORE+Message.OUTPUT_START);
					GoodsExt.Select();
				}
				if(datas2.getUser().getAuthority().equals(DEFINE.SYS_USER_SALER)){
					System.out.println(Message.OUTPUT_START+Message.APP_WELCOM+Message.APP_NAME_SALER+Message.OUTPUT_START);
					new ShopExt();//初始化购物列表
					ShopExt.setUid(uid);
					ShopExt.Select();
				}
			}
}

	/*
	 * 服务器登录检测
	 * 参数：datas数据包,服务器socket
	  
	public static void loginCheck(Datas datas,Socket socket){
		datas.setFlags(DEFINE.SYS_LOGINFAIL);
		HashMap<Integer,User> map_user=UserExt.getUserList();
		User user=datas.getUser();
		User user_cmp=map_user.get(user.getUserid());
		if(user_cmp==null){
			System.out.println("客户端请求"+Message.ERR_LOGIN+",用户名:"+user.getUserid()+"\n");
			datas.setUser(null);
			SocketTools.sendData(datas,socket);
			return;
		}
		if(user.getUserid()==user_cmp.getUserid() && user.getPassword().equals(user_cmp.getPassword())){
			System.out.println("登录验证成功"+user_cmp.getName());
			datas.setFlags(DEFINE.SYS_LOGINSUCCESS);
			if(user_cmp.getAuthority().equals(DEFINE.SYS_USER_MANAGER)){
				datas.getUser().setAuthority(DEFINE.SYS_USER_MANAGER);
			}else{
				datas.getUser().setAuthority(DEFINE.SYS_USER_SALER);
			}
		}else{
			System.out.println("客户端请求"+Message.ERR_LOGIN+",用户名:"+user.getUserid()+"\n");
			datas.setUser(null);
		}
		SocketTools.sendData(datas,socket);
	}
*/
}