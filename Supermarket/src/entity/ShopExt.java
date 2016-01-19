package entity;

import java.util.HashMap;

public class ShopExt {
	@SuppressWarnings("unused")
	private static double totalprice=0;//�ܽ��
	@SuppressWarnings("unused")
	private static HashMap<Integer,Trade> map_shop=null;//�洢������Ϣ
	@SuppressWarnings("unused")
	private static Member member=null;//��Ա��Ϣ
	@SuppressWarnings("unused")
	private static int uid=0;//����Ա���
	@SuppressWarnings("unused")
	private static String code="";//СƱ���
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
	 * ��ʼ������
	 * ��չ����б�
	  
	private static void cleanShopList(){
		code=getCode();
		totalprice=0;
		map_shop=null;
		member=null;
	}
	
	//��ȡСƱ����
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
	
	//���������
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
	
	//��ӹ�����Ϣ
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
	
	//���֮ǰ�Ƿ������ͬ����Ʒ����
	private static boolean checkContainsShop(int key){
		if(map_shop==null){
			return false;
		}
		return map_shop.containsKey(key);
	}
	
	//�޸���Ʒ����
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
		int old_stocknum=0;//�ͻ��˾�����
		int ser_stocknum=0;//�������������
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
		 * ������������������������������ӿ��
		 * ����Ҫ��Ҫ������Ƿ��㹻��
		 * ����㹻���������������������
		  
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
	
	//����map_goods
	private static void updateShop(Datas datas){
		Shop shop=new Shop();
		int goodsid=datas.getGoods().getGoodid();//��Ʒ���
		int stocknum=datas.getGoods().getStock();//����
		double price=datas.getGoods().getPrice();//����
		double cost=(double) price*stocknum;//���
		shop.setGoodid(goodsid);
		shop.setName(datas.getGoods().getName());
		shop.setUnit(datas.getGoods().getUnit());
		shop.setStock(stocknum);
		shop.setPrice(price);
		shop.setCost(cost);
		
		//���֮ǰ������ͬ��Ʒ��ӵ�һ��
		if(checkContainsShop(datas.getGoods().getGoodid())){
			shop.setCost(map_shop.get(goodsid).getCost()+cost);
			shop.setStock(map_shop.get(goodsid).getStock()+stocknum);
		}
		
		if(map_shop==null){
			map_shop=new HashMap<Integer,Shop>();
		}
		map_shop.put(goodsid, shop);
	}

	//��ⷵ�������Ƿ�������
	private static boolean checkReceive(Datas datas){
		if(datas.getFlags().equals(DEFINE.SYS_OPERATOR_FAIL)){
			return false;
		}
		if(datas.getFlags().equals(DEFINE.SYS_STOCK_ENOUGH)){
			return false;
		}
		return true;
	}

	//��ʾ�����б�
	private static void showShop(){
		if(map_shop==null){
			System.out.println(Message.ERR_SHOP_EMPTY);
			return;
		}
		
		Iterator<Integer> it=map_shop.keySet().iterator();
		System.out.println("\n\t\t\t\t"+Message.APP_WELCOM+Message.APP_NAME);
		System.out.println("\n����Ա���: "+uid+"\t\t\t\tСƱ��: "+code);
		System.out.println("----------------------------------------------------------------------------------------------------------------------");
		System.out.println("���\t\t\t\t��Ʒ����\t\t\t����\t\t\t��λ\t\t\t����\t\t\t���");
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
		System.out.println("������:"+stocknum+"\t\t\t\t�ܽ��"+(double)totalprice+"\n");
	}

	//����
	private static void accounts(){
		if(map_shop==null){
			System.out.println(Message.ERR_SHOP_EMPTY);
			return;
		}
		
		showShop();	
		select_accounts();
	}
	
	//ѡ����˷�ʽ
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
	
	//��ͨ����
	private static void normal_accounts(){
		double price=InputTools.getDouble(Message.INPUT_PAY, Message.ERR_PRICE_ENOUGH);
		if(price<totalprice){
			System.out.println(Message.ERR_PRICE_ENOUGH);
			normal_accounts();
			return;
		}
		
		showShop();
		if(member!=null){
			System.out.println("��Ա����:"+member.getMemberid()+"\t\t\t����ǰ����:"+
					(member.getScore()-totalprice)+"\t\t\t\t���Ѻ����:"+member.getScore()+"\n");
		}
		System.out.println("ʵ���ֽ�:"+price+"\t\t\t\t����:"+(price-totalprice)+"\n");
		cleanShopList();
	}
	
	//��Ա����
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
