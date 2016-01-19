package util;


public class DemoData {
	/*
	//显示会员数据 
	public static void showMember(){
		HashMap<Integer,Member> map_member=MemberExt.getMemberList();
		Iterator<Integer> it=map_member.keySet().iterator();
		System.out.println("============    会    员    积    分    信    息    =============\n");
		System.out.println("编号\t\t\t\t会员名称\t\t\t当前积分");
		while(it.hasNext()){
			Member member=map_member.get(it.next());
			System.out.println(member.getMemberid()+"\t\t\t\t"+member.getName()+
					"\t\t\t\t"+member.getScore());
		}
	}

	//显示商品数据
	public static void showGoods(){
		HashMap<Integer,Goods> map_goods=GoodsExt.getGoodsList();
		Iterator<Integer> it=map_goods.keySet().iterator();
		System.out.println("========================    商    品     信    息    ===========================\n");
		System.out.println("编号\t\t\t\t产品名称\t\t\t数量\t\t\t\t单价\t\t\t计量单位");
		while(it.hasNext()){
			Goods goods=map_goods.get(it.next());
			System.out.println(goods.getGoodid()+"\t\t\t\t"+goods.getName()+"\t\t\t\t"
			+goods.getStock()+"\t\t\t\t"+goods.getPrice()+"\t\t\t\t"+goods.getUnit());
		}
	}
	
	//初始化数据文件
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
	
	//写入用户数据
	public static void addUser(){
		HashMap<Integer,User> map_user=new HashMap<Integer,User>();
		
		//添加收银员、仓库管理员
			User user=new User();//收银员
			User user2=new User();//仓管员
			user.setUserid(1001);
			user.setAuthority(DEFINE.SYS_USER_SALER);
			user.setName("收银员1001");
			user.setPassword("1001");
			user2.setUserid(2001);
			user2.setAuthority(DEFINE.SYS_USER_MANAGER);
			user2.setName("仓管员2001");
			user2.setPassword("2001");
			map_user.put(1001, user);
			map_user.put(2001, user2);
		
		if(UserExt.setUserList(map_user)){
			System.out.println("初始化用户信息成功，初始收银编号1001，仓管编号2001，密码相同...");
			System.out.println("用户信息文件路径:"+FileTools.path_user);
		}else{
			System.out.println("初始化用户信息失败...");
		}
		
	}

	//写入会员数据
	public static void addMember(){
		HashMap<Integer,Member> map_member=new HashMap<Integer,Member>();
		
		Member member=new Member();
		member.setMemberid(1001);
		member.setName("会员1001");
		member.setScore(0);
		map_member.put(1001, member);
		
		Member member2=new Member();
		member2.setMemberid(1002);
		member2.setName("会员1002");
		member2.setScore(0);
		map_member.put(1002, member2);
		
		if(MemberExt.setMemberList(map_member)){
			System.out.println("初始化会员信息成功，初始会员编号1001、1002...");
			System.out.println("会员文件路径:"+FileTools.path_member);
		}else{
			System.out.println("初始化会员信息失败...");
		}
	}

	//写入商品数据
	public static void addGoods(){
		HashMap<Integer,Goods> map_goods=new HashMap<Integer,Goods>();
		
		Goods goods1=new Goods();
		goods1.setGoodid(1001);
		goods1.setName("香蕉");
		goods1.setPrice(3.5);
		goods1.setStock(100);
		goods1.setUnit("Kg");
		map_goods.put(1001, goods1);
		
		Goods goods2=new Goods();
		goods2.setGoodid(1002);
		goods2.setName("苹果");
		goods2.setPrice(5.5);
		goods2.setStock(100);
		goods2.setUnit("Kg");
		map_goods.put(1002, goods2);
		
		Goods goods3=new Goods();
		goods3.setGoodid(1003);
		goods3.setName("葡萄");
		goods3.setPrice(8.3);
		goods3.setStock(100);
		goods3.setUnit("Kg");
		map_goods.put(1003, goods3);
		
		if(GoodsExt.setGoodsList(map_goods)){
			System.out.println("初始化商品信息成功,产品编号1001、1002、1003...");
			System.out.println("商品信息文件路径:"+FileTools.path_goods);
		}else{
			System.out.println("初始化商品信息失败...");
		}
	}
	*/
}