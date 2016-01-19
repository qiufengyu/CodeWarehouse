package entity;

//��Ա��Ϣ������

public class MemberExt {
	/*
	//��Ա��Ϣ�ļ� 
	private static String path_member=Tools.getValue(DEFINE.SYS_PATH)+Tools.getValue(DEFINE.SYS_DATA_MEMBER);
	
	/*
	 * ��ȡ��Ա�б�
	 * ����ֵ:HashMap���͵�Member
	  
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,Member> getMemberList(){
		return (HashMap<Integer,Member>) FileTools.readData(path_member);
	}
	
	/*
	 * д���Ա�б�
	 * ����:map_member��HashMap�������͵�member
	 * ����ֵ:д��ɹ�����true��д��ʧ�ܷ���false
	  
	public static boolean setMemberList(HashMap<Integer,Member> map_member){
		return FileTools.writeData(map_member,path_member);
	}
	
	/*
	 * ����Ա�Ƿ����
	 * ���������ֱ��д������
	 * ������datas���ݰ���������socket
	  
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
	 * �ͻ��˷��ͻ�Ա����
	  
	public static Datas sendMember(Datas datas){
		return (Datas) SocketTools.sendData(datas);
	}
	*/
}
