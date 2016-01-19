package entity;

import java.util.HashMap;

public class ShopExt {
	@SuppressWarnings("unused")
	private static double totalprice=0;//总金额
	@SuppressWarnings("unused")
	private static HashMap<Integer,Trade> map_shop=null;//存储购物信息
	@SuppressWarnings("unused")
	private static Member member=null;//会员信息
	@SuppressWarnings("unused")
	private static int uid=0;//收银员编号
	@SuppressWarnings("unused")
	private static String code="";//小票编号
	/*
	public static int getUid() {
		return uid;
	}
	
	public static void setUid(int uid) {
		ShopExt.uid = uid;
	}
	
	public ShopExt(){
		cleanShopList();
	}
	
	/*
	 * 初始化单号
	 * 清空购物列表
	  
	private static void cleanShopList(){
		code=getCode();
		totalprice=0;
		map_shop=null;
		member=null;
	}
	
	//获取小票号码
	private static String getCode(){
		Calendar ca=Calendar.getInstance();
		String year=String.valueOf(ca.get(Calendar.YEAR));
		String month=String.valueOf(ca.get(Calendar.MONTH)+1);
		String day=String.valueOf(ca.get(Calendar.DAY_OF_MONTH));
		String hour=String.valueOf(ca.get(Calendar.HOUR_OF_DAY));
		String minutes=String.valueOf(ca.get(Calendar.MINUTE));
		String seconds=String.valueOf(ca.get(Calendar.SECOND));
		return (year+month+day+hour+minutes+seconds);
	}
	
	//收银主入口
	public static void Select(){
		while(true){
			int int_select=InputTools.getInt(Message.INPUT_SELECT_SALER, Message.ERR_SELECT);
			if(int_select==4){
				return;
			}
			switch(int_select){
			case 1:
				shopIn();
				break;
			case 2:
				modifyShop();
				break;
			case 3:
				accounts();
				break;
			default:
				System.out.println(Message.ERR_SELECT);
				Select();
				break;
			}
		}
	}
	
	//添加购物信息
	private static void shopIn(){
		Datas datas=new Datas();
		Goods goods=new Goods();
		datas.setFlags(DEFINE.SYS_GOODS_OUT);

		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		goods.setStock(InputTools.getInt(Message.INPUT_STOCK, Message.ERR_STOCK));
		if(goods.getStock()<0){
			System.out.println(Message.ERR_STOCK);
			shopIn();
			return;
		}
		
		Datas datas2=GoodsExt.sendGoods(datas, goods);
		if(checkReceive(datas2)){
			updateShop(datas2);
			showShop();
			return;
		}
		shopIn();
	}
	
	//检测之前是否存在相同的商品数据
	private static boolean checkContainsShop(int key){
		if(map_shop==null){
			return false;
		}
		return map_shop.containsKey(key);
	}
	
	//修改商品数量
	@SuppressWarnings("unused")
	private static void modifyShop(){
		if(map_shop==null){
			System.out.println(Message.ERR_SHOP_EMPTY);
			return;
		}
		
		Datas datas=new Datas();
		Goods goods=new Goods();
		int goodsid=0;
		int stocknum=0;
		double price=0;
		int old_stocknum=0;//客户端旧数量
		int ser_stocknum=0;//服务器出入库数
		double cost=0;
		goodsid=InputTools.getInt(Message.INPUT_MODIFY_GOODSNUM, Message.ERR_SHOP_NOGOODS);
		
		if(!checkContainsShop(goodsid)){
			System.out.println(Message.ERR_SHOP_NOGOODS);
			return;
		}

		stocknum=InputTools.getInt(Message.INPUT_STOCK, Message.ERR_STOCK);
		old_stocknum=map_shop.get(goodsid).getStock();
		price=map_shop.get(goodsid).getPrice();
		
		/*
		 * 如果老数量比新数量大，则向服务器添加库存
		 * 否则要先要检测库存是否足够，
		 * 库存足够情况下再向服务器消减库存
		  
		Datas datas2=new Datas();
		Goods goods2=new Goods();
		goods2.setGoodid(goodsid);
		ser_stocknum=Math.abs(old_stocknum-stocknum);

		if(old_stocknum>stocknum){
			datas2.setFlags(DEFINE.SYS_GOODS_IN);
		}else{
			datas2.setFlags(DEFINE.SYS_GOODS_OUT);
		}
		
		goods2.setStock(ser_stocknum);
		Datas datas3=GoodsExt.sendGoods(datas2, goods2);
		
		if(datas3==null){
			System.out.println(Message.ERR_MODIFY);
			return;
		}
		if(datas3.getFlags().equals(DEFINE.SYS_OPERATOR_FAIL)){
			System.out.println(Message.ERR_MODIFY);
			return;
		}
		
		stocknum=stocknum-old_stocknum;
		cost=(double) stocknum*price;
		goods.setGoodid(goodsid);
		goods.setName(map_shop.get(goodsid).getName());
		goods.setPrice(price);
		goods.setStock(stocknum);
		goods.setUnit(map_shop.get(goodsid).getUnit());
		datas.setGoods(goods);
		
		updateShop(datas);
	}
	
	//操作map_goods
	private static void updateShop(Datas datas){
		Shop shop=new Shop();
		int goodsid=datas.getGoods().getGoodid();//商品编号
		int stocknum=datas.getGoods().getStock();//数量
		double price=datas.getGoods().getPrice();//单价
		double cost=(double) price*stocknum;//金额
		shop.setGoodid(goodsid);
		shop.setName(datas.getGoods().getName());
		shop.setUnit(datas.getGoods().getUnit());
		shop.setStock(stocknum);
		shop.setPrice(price);
		shop.setCost(cost);
		
		//如果之前存在相同商品则加到一起
		if(checkContainsShop(datas.getGoods().getGoodid())){
			shop.setCost(map_shop.get(goodsid).getCost()+cost);
			shop.setStock(map_shop.get(goodsid).getStock()+stocknum);
		}
		
		if(map_shop==null){
			map_shop=new HashMap<Integer,Shop>();
		}
		map_shop.put(goodsid, shop);
	}

	//检测返回数据是否则正常
	private static boolean checkReceive(Datas datas){
		if(datas.getFlags().equals(DEFINE.SYS_OPERATOR_FAIL)){
			return false;
		}
		if(datas.getFlags().equals(DEFINE.SYS_STOCK_ENOUGH)){
			return false;
		}
		return true;
	}

	//显示购物列表
	private static void showShop(){
		if(map_shop==null){
			System.out.println(Message.ERR_SHOP_EMPTY);
			return;
		}
		
		Iterator<Integer> it=map_shop.keySet().iterator();
		System.out.println("\n\t\t\t\t"+Message.APP_WELCOM+Message.APP_NAME);
		System.out.println("\n收银员编号: "+uid+"\t\t\t\t小票号: "+code);
		System.out.println("----------------------------------------------------------------------------------------------------------------------");
		System.out.println("编号\t\t\t\t产品名称\t\t\t数量\t\t\t单位\t\t\t单价\t\t\t金额");
		totalprice=0;
		int stocknum=0;
		
		while(it.hasNext()){
			Shop shop=map_shop.get(it.next());
			totalprice+=shop.getCost();
			stocknum+=shop.getStock();
			System.out.println(shop.getGoodid()+"\t\t\t\t"+shop.getName()+"\t\t\t\t"+shop.getStock()+"\t\t\t"
			+shop.getUnit()+"\t\t\t"+shop.getPrice()+"\t\t\t"+shop.getCost());
		}		
		System.out.println("----------------------------------------------------------------------------------------------------------------------");
		System.out.println("总数量:"+stocknum+"\t\t\t\t总金额"+(double)totalprice+"\n");
	}

	//结账
	private static void accounts(){
		if(map_shop==null){
			System.out.println(Message.ERR_SHOP_EMPTY);
			return;
		}
		
		showShop();	
		select_accounts();
	}
	
	//选择结账方式
	private static void select_accounts(){
		int int_select=InputTools.getInt(Message.INPUT_SELECT_ACCOUNT, Message.ERR_SELECT);
		switch(int_select){
		case 1:
			normal_accounts();
			break;
		case 2:
			member_accounts();
			break;
		default:
			select_accounts();
			break;
		}
	}
	
	//普通结账
	private static void normal_accounts(){
		double price=InputTools.getDouble(Message.INPUT_PAY, Message.ERR_PRICE_ENOUGH);
		if(price<totalprice){
			System.out.println(Message.ERR_PRICE_ENOUGH);
			normal_accounts();
			return;
		}
		
		showShop();
		if(member!=null){
			System.out.println("会员卡号:"+member.getMemberid()+"\t\t\t消费前积分:"+
					(member.getScore()-totalprice)+"\t\t\t\t消费后积分:"+member.getScore()+"\n");
		}
		System.out.println("实收现金:"+price+"\t\t\t\t找零:"+(price-totalprice)+"\n");
		cleanShopList();
	}
	
	//会员结账
	private static void member_accounts(){
		Datas datas=new Datas();
		Member member2=new Member();
		datas.setFlags(DEFINE.SYS_MEMBER_QUERY);
		member2.setMemberid(InputTools.getInt(Message.INPUT_MEMBERID, Message.ERR_MEMBERID));
		member2.setScore(totalprice);
		datas.setMember(member2);
		Datas datas2=MemberExt.sendMember(datas);
		
		if(datas2.getFlags().equals(DEFINE.SYS_OPERATOR_FAIL)){
			System.out.println(Message.ERR_MEMBERID);
			member_accounts();
			return;
		}
		
		member=datas2.getMember();
		normal_accounts();
	}
	*/
}
