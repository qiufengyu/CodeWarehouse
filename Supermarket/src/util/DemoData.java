package util;


public class DemoData {
	/*
	//��ʾ��Ա���� 
	public static void showMember(){
		HashMap<Integer,Member> map_member=MemberExt.getMemberList();
		Iterator<Integer> it=map_member.keySet().iterator();
		System.out.println("============    ��    Ա    ��    ��    ��    Ϣ    =============\n");
		System.out.println("���\t\t\t\t��Ա����\t\t\t��ǰ����");
		while(it.hasNext()){
			Member member=map_member.get(it.next());
			System.out.println(member.getMemberid()+"\t\t\t\t"+member.getName()+
					"\t\t\t\t"+member.getScore());
		}
	}

	//��ʾ��Ʒ����
	public static void showGoods(){
		HashMap<Integer,Goods> map_goods=GoodsExt.getGoodsList();
		Iterator<Integer> it=map_goods.keySet().iterator();
		System.out.println("========================    ��    Ʒ     ��    Ϣ    ===========================\n");
		System.out.println("���\t\t\t\t��Ʒ����\t\t\t����\t\t\t\t����\t\t\t������λ");
		while(it.hasNext()){
			Goods goods=map_goods.get(it.next());
			System.out.println(goods.getGoodid()+"\t\t\t\t"+goods.getName()+"\t\t\t\t"
			+goods.getStock()+"\t\t\t\t"+goods.getPrice()+"\t\t\t\t"+goods.getUnit());
		}
	}
	
	//��ʼ�������ļ�
	public static void init(){
		if(!FileTools.fileExists(FileTools.path_user)){
			addUser();
		}
		
		if(!FileTools.fileExists(FileTools.path_member)){
			addMember();
		}
		
		if(!FileTools.fileExists(FileTools.path_goods)){
			addGoods();
		}
		
		showGoods();
		System.out.println("\n\n");
		showMember();
	}
	
	//д���û�����
	public static void addUser(){
		HashMap<Integer,User> map_user=new HashMap<Integer,User>();
		
		//�������Ա���ֿ����Ա
			User user=new User();//����Ա
			User user2=new User();//�ֹ�Ա
			user.setUserid(1001);
			user.setAuthority(DEFINE.SYS_USER_SALER);
			user.setName("����Ա1001");
			user.setPassword("1001");
			user2.setUserid(2001);
			user2.setAuthority(DEFINE.SYS_USER_MANAGER);
			user2.setName("�ֹ�Ա2001");
			user2.setPassword("2001");
			map_user.put(1001, user);
			map_user.put(2001, user2);
		
		if(UserExt.setUserList(map_user)){
			System.out.println("��ʼ���û���Ϣ�ɹ�����ʼ�������1001���ֹܱ��2001��������ͬ...");
			System.out.println("�û���Ϣ�ļ�·��:"+FileTools.path_user);
		}else{
			System.out.println("��ʼ���û���Ϣʧ��...");
		}
		
	}

	//д���Ա����
	public static void addMember(){
		HashMap<Integer,Member> map_member=new HashMap<Integer,Member>();
		
		Member member=new Member();
		member.setMemberid(1001);
		member.setName("��Ա1001");
		member.setScore(0);
		map_member.put(1001, member);
		
		Member member2=new Member();
		member2.setMemberid(1002);
		member2.setName("��Ա1002");
		member2.setScore(0);
		map_member.put(1002, member2);
		
		if(MemberExt.setMemberList(map_member)){
			System.out.println("��ʼ����Ա��Ϣ�ɹ�����ʼ��Ա���1001��1002...");
			System.out.println("��Ա�ļ�·��:"+FileTools.path_member);
		}else{
			System.out.println("��ʼ����Ա��Ϣʧ��...");
		}
	}

	//д����Ʒ����
	public static void addGoods(){
		HashMap<Integer,Goods> map_goods=new HashMap<Integer,Goods>();
		
		Goods goods1=new Goods();
		goods1.setGoodid(1001);
		goods1.setName("�㽶");
		goods1.setPrice(3.5);
		goods1.setStock(100);
		goods1.setUnit("Kg");
		map_goods.put(1001, goods1);
		
		Goods goods2=new Goods();
		goods2.setGoodid(1002);
		goods2.setName("ƻ��");
		goods2.setPrice(5.5);
		goods2.setStock(100);
		goods2.setUnit("Kg");
		map_goods.put(1002, goods2);
		
		Goods goods3=new Goods();
		goods3.setGoodid(1003);
		goods3.setName("����");
		goods3.setPrice(8.3);
		goods3.setStock(100);
		goods3.setUnit("Kg");
		map_goods.put(1003, goods3);
		
		if(GoodsExt.setGoodsList(map_goods)){
			System.out.println("��ʼ����Ʒ��Ϣ�ɹ�,��Ʒ���1001��1002��1003...");
			System.out.println("��Ʒ��Ϣ�ļ�·��:"+FileTools.path_goods);
		}else{
			System.out.println("��ʼ����Ʒ��Ϣʧ��...");
		}
	}
	*/
}