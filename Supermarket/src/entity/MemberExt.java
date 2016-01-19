package entity;

//会员信息操作类

public class MemberExt {
	/*
	//会员信息文件 
	private static String path_member=Tools.getValue(DEFINE.SYS_PATH)+Tools.getValue(DEFINE.SYS_DATA_MEMBER);
	
	/*
	 * 读取会员列表
	 * 返回值:HashMap类型的Member
	  
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,Member> getMemberList(){
		return (HashMap<Integer,Member>) FileTools.readData(path_member);
	}
	
	/*
	 * 写入会员列表
	 * 参数:map_member是HashMap数组类型的member
	 * 返回值:写入成功返回true，写入失败返回false
	  
	public static boolean setMemberList(HashMap<Integer,Member> map_member){
		return FileTools.writeData(map_member,path_member);
	}
	
	/*
	 * 检测会员是否存在
	 * 如果存在则直接写入数据
	 * 参数：datas数据包，服务器socket
	  
	public static void checkMemberScore(Datas datas,Socket socket){
		if(datas==null){
			datas=new Datas();
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			SocketTools.sendData(datas,socket);
			return;
		}
		
		datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
		if(datas.getMember()==null){
			datas.setMember(null);
			SocketTools.sendData(datas,socket);
			return;
		}
		
		int memberid=datas.getMember().getMemberid();
		double score=datas.getMember().getScore();
		HashMap<Integer,Member> map_member=getMemberList();
		
		if(map_member.containsKey(datas.getMember().getMemberid())){
			score+=map_member.get(memberid).getScore();
			datas.getMember().setScore(score);
			datas.getMember().setName(map_member.get(memberid).getName());
			map_member.put(memberid, datas.getMember());
			if(FileTools.writeData(map_member, path_member)){
				datas.setFlags(DEFINE.SYS_OPERATOR_SUCCESS);
			}else{
				datas.setMember(null);
			}
		}else{
			datas.setMember(null);
		}
		
		SocketTools.sendData(datas,socket);
	}

	/*
	 * 客户端发送会员数据
	  
	public static Datas sendMember(Datas datas){
		return (Datas) SocketTools.sendData(datas);
	}
	*/
}
