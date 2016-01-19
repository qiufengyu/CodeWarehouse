package entity;

public class GoodsExt {
	//goods�ļ�·��
	/*
	private static String path_goods=Tools.getValue(DEFINE.SYS_PATH)+Tools.getValue(DEFINE.SYS_DATA_GOODS);
	
	/*
	 * ��ȡ��Ʒ�б�
	 * ����ֵ:HashMap���͵�Goods�б�
	  
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,Goods> getGoodsList(){
		return (HashMap<Integer,Goods>) FileTools.readData(path_goods);
	}
	
	/*
	 * д����Ʒ�б�
	 * ����:map_goods��HashMap�������͵�Goods�б�
	 * ����ֵ:д��ɹ�����true��д��ʧ�ܷ���false
	  
	public static boolean setGoodsList(HashMap<Integer,Goods> map_goods){
		return FileTools.writeData(map_goods, path_goods);
	}
	
	//�ֹ������
	public static void Select(){
		while(true){
				
				int int_select=InputTools.getInt(Message.INPUT_SELECT_MANAGER, Message.ERR_SELECT);
				if(int_select==6){
					return;
				}
				switch(int_select){
				case 1:
					stockIn();
					break;
				case 2:
					stockOut();
					break;
				case 3:
					stockAdd();
					break;
				case 4:
					GoodsQueryAll();
					break;
				case 5:
					GoodsQueryByid();
					break;
				default:
					System.out.println(Message.ERR_SELECT);
					Select();
					break;
				}
		}
	}
	
	 //�ͻ�����Ʒ�������
	private static void stockIn(){
		Datas datas=new Datas();
		Goods goods=new Goods();
		datas.setFlags(DEFINE.SYS_GOODS_IN);

		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		goods.setStock(InputTools.getInt(Message.INPUT_STOCK, Message.ERR_STOCK));
		
		if(goods.getStock()<0){
			System.out.println(Message.ERR_STOCK);
			stockIn();
			return;
		}

		sendGoods(datas,goods);
	}
	
	/*
	 * ��������Ʒ�����
	 * ������datas���ݰ���������socket
	  
	public static void stockInOutCheck(Datas datas,Socket socket){
		//������ݰ��쳣���˳�
		if(!checkDatas(datas,socket)){
			return;
		}
		
		String flags=datas.getFlags();//��ȡ�ͻ��˷������ı�ʶ
		
		//����Ƿ��������Ʒ
		int goodsid=datas.getGoods().getGoodid();//��ȡ��Ʒid
		int stocknum=datas.getGoods().getStock();//��ȡ����
		HashMap<Integer,Goods> map_goods=checkContainsGoods(datas);
		
		if(map_goods!=null){
			//�����Ʒ����Ƿ��㹻
			if(checkStockEnough(datas)){
				Goods goods=map_goods.get(goodsid);
				//�����������Ǽӿ�棬����ǳ���������
				if(flags.equals(DEFINE.SYS_GOODS_IN)){
					goods.setStock(goods.getStock()+stocknum);
				}else{
					goods.setStock(goods.getStock()-stocknum);
				}
				map_goods.put(goodsid, goods);
				//д����Ʒ��Ϣ
				if(updateGoods(map_goods)){
					datas.setGoods(goods);
					datas.getGoods().setStock(stocknum);
					datas.setFlags(DEFINE.SYS_OPERATOR_SUCCESS);
				}else{
					datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
					datas.setGoods(null);
				}
			}else{
				datas.setFlags(DEFINE.SYS_STOCK_ENOUGH);
				datas.setGoods(null);
			}
		}else{
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			datas.setGoods(null);
		}
		
		SocketTools.sendData(datas, socket);
	}

	 //�ͻ�����Ʒ��������
	private static void stockOut(){
		Datas datas=new Datas();
		Goods goods=new Goods();
		datas.setFlags(DEFINE.SYS_GOODS_OUT);

		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		goods.setStock(InputTools.getInt(Message.INPUT_STOCK, Message.ERR_STOCK));
		
		if(goods.getStock()<0){
			System.out.println(Message.ERR_STOCK);
			stockOut();
			return;
		}

		sendGoods(datas,goods);
	}
	
	/*
	 * ������Ʒ���ݵ������� 
	 * ������datas���ݰ���goods��Ʒ
	  
	public static Datas sendGoods(Datas datas,Goods goods){
		datas.setGoods(goods);
		Datas datas2=(Datas) SocketTools.sendData(datas);
		
		if(datas2==null){
			System.out.println(Message.OPERATOR_FAIL);
		}
		
		if(datas2.getFlags().equals(DEFINE.SYS_OPERATOR_FAIL)){
			System.out.println(Message.OPERATOR_FAIL);
		}
		if(datas2.getFlags().equals(DEFINE.SYS_OPERATOR_SUCCESS)){
			System.out.println(Message.OPERATOR_SUCCESS);
		}
		if(datas2.getFlags().equals(DEFINE.SYS_STOCK_ENOUGH)){
			System.out.println(Message.ERR_STOCK_ENOUGH);
		}
		
		return datas2;
	}

	/*
	 * ������ݰ��쳣
	 * ������datas���ݰ���������socket
	 * ����ֵ�����ݰ���������true���쳣����false
	  
	private static boolean checkDatas(Datas datas,Socket socket){
		if(datas==null){
			datas=new Datas();
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			datas.setGoods(null);
			SocketTools.sendData(datas, socket);
			return false;
		}
		
		if(datas.getGoods()==null){
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			datas.setGoods(null);
			SocketTools.sendData(datas, socket);
			return false;
		}
		
		return true;
	}
	
	/*
	 * �����Ʒ�Ƿ����
	 * ������datas���ݰ�
	 * ����ֵ��HashMap�������͵�goods�б�
	
	private static HashMap<Integer,Goods> checkContainsGoods(Datas datas){
		int goodsid=datas.getGoods().getGoodid();
		HashMap<Integer,Goods> map_goods=getGoodsList();
		
		if(map_goods.containsKey(goodsid)){
			return map_goods;
		}
		
		return null;
	}
	
	//������Ʒ��Ϣ
	private static boolean updateGoods(HashMap<Integer,Goods> map_goods){
		if(FileTools.writeData(map_goods, path_goods)){
			return true;
		}
		
		return false;
	}
	

	 * �����������Ʒ���
	 * ������datas���ݰ�,������socket
	
	public static void stockAddCheck(Datas datas,Socket socket){
		if(!checkDatas(datas,socket)){
			return;
		}
		
		datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
		int goodsid=datas.getGoods().getGoodid();
		HashMap<Integer,Goods> map_goods=checkContainsGoods(datas);
		
		//�����Ʒ��������������
		if(map_goods==null){
			map_goods=getGoodsList();
			map_goods.put(goodsid, datas.getGoods());
			if(updateGoods(map_goods)){
				datas.setFlags(DEFINE.SYS_OPERATOR_SUCCESS);
			}else{
				datas.setGoods(null);
			}
		}else{
			datas.setGoods(null);
		}
		
		SocketTools.sendData(datas, socket);
	}

	//�ͻ��������Ʒ����
	private static void stockAdd(){
		Datas datas=new Datas();
		datas.setFlags(DEFINE.SYS_GOODS_ADD);
		Goods goods=new Goods();

		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		goods.setName(InputTools.getString(Message.INPUT_GOODSNAME, Message.ERR_GOODSNAME));
		goods.setPrice(InputTools.getDouble(Message.INPUT_PRICE, Message.ERR_PRICE));
		goods.setStock(InputTools.getInt(Message.INPUT_STOCK, Message.ERR_STOCK));
		goods.setUnit(InputTools.getString(Message.INPUT_UNIT, Message.ERR_UNIT));
		
		if(goods.getStock()<0){
			System.out.print(Message.ERR_STOCK);
			stockAdd();
			return;
		}
		if(goods.getPrice()<0){
			System.out.print(Message.ERR_PRICE);
			stockAdd();
			return;
		}
				
		sendGoods(datas,goods);
	}
	
	//������Ƿ��㹻
	private static boolean checkStockEnough(Datas datas){
		HashMap<Integer,Goods> map_goods=getGoodsList();
		int goodsid=datas.getGoods().getGoodid();//��Ʒid
		int stocknum=datas.getGoods().getStock();
		//�������⣬�򷵻�true
		if(datas.getFlags().equals(DEFINE.SYS_GOODS_IN)){
			return true;
		}
		if(map_goods.get(goodsid).getStock()>=stocknum){
			return true;
		}
		return false;
	}
	
	//��ʾ��Ʒ����
	public static void GoodsQueryAll(){
		Datas datas=new Datas();
		datas.setFlags(DEFINE.SYS_GOODS_QUERY_ALL);
		Datas datas2=sendGoods(datas,null);
		
		if(datas2==null){
			System.out.print(Message.ERR_OUTPUT);
			return;
		}
		
		HashMap<Integer,Goods> map_goods=datas2.getMap_goods();
		Iterator<Integer> it=map_goods.keySet().iterator();
		System.out.println("���\t\t\t\t��Ʒ����\t\t\t����\t\t\t\t����\t\t\t������λ");
		
		while(it.hasNext()){
			Goods goods=map_goods.get(it.next());
			System.out.println(goods.getGoodid()+"\t\t\t\t"+goods.getName()+"\t\t\t\t"
			+goods.getStock()+"\t\t\t\t"+goods.getPrice()+"\t\t\t\t"+goods.getUnit());
		}
	}
	

	 * ����������ȫ����Ʒ
	 * ������datas���ݰ���������socket
	 * ����ֵ����ѯ�ɹ�����datas��ʧ�ܷ���null
	
	public static void replyGoodsQueryAll(Datas datas,Socket socket){
		if(datas==null){
			datas=new Datas();
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			datas.setGoods(null);
			SocketTools.sendData(datas, socket);
			return;
		}
		
		datas.setMap_goods(getGoodsList());
		SocketTools.sendData(datas, socket);
	}
	//�ͻ��˰���Ʒ��Ų�ѯ��Ʒ
	public static void GoodsQueryByid(){
		Datas datas=new Datas();
		Goods goods=new Goods();
		
		datas.setFlags(DEFINE.SYS_GOODS_QUERY_BYID);
		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		Datas datas2=sendGoods(datas,goods);
		
		if(datas2.getFlags().equals(DEFINE.SYS_OPERATOR_SUCCESS)){
			Goods goods2=datas2.getGoods();
			System.out.println("���\t\t\t\t��Ʒ����\t\t\t����\t\t\t\t����\t\t\t������λ");
			System.out.println(goods2.getGoodid()+"\t\t\t\t"+goods2.getName()+"\t\t\t\t"
					+goods2.getStock()+"\t\t\t\t"+goods2.getPrice()+"\t\t\t\t"+goods2.getUnit()+"\n");
		}
	}
	

	 * ������������Ʒ��ѯ
	 * ������datas���ݰ���������socket
	 * ����ֵ����ѯ�ɹ�����datas�����򷵻�null

	public static void replyGoodsQueryByid(Datas datas,Socket socket){
		if(datas==null){
			datas=new Datas();
			datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
			datas.setGoods(null);
			SocketTools.sendData(datas, socket);
			return;
		}
		
		datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
		
		if(datas.getGoods()==null){		
			datas.setGoods(null);
			SocketTools.sendData(datas, socket);
			return;
		}
		
		int goodsid=datas.getGoods().getGoodid();
		HashMap<Integer,Goods> map_goods=getGoodsList();
		
		if(map_goods.containsKey(goodsid)){
			datas.setFlags(DEFINE.SYS_OPERATOR_SUCCESS);
			datas.setGoods(map_goods.get(goodsid));
		}else{
			datas.setGoods(null);
		}
		
		SocketTools.sendData(datas, socket);
	}
	*/
}
	