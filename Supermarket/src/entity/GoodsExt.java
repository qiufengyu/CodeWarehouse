package entity;

public class GoodsExt {
	//goods文件路径
	/*
	private static String path_goods=Tools.getValue(DEFINE.SYS_PATH)+Tools.getValue(DEFINE.SYS_DATA_GOODS);
	
	/*
	 * 读取商品列表
	 * 返回值:HashMap类型的Goods列表
	  
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,Goods> getGoodsList(){
		return (HashMap<Integer,Goods>) FileTools.readData(path_goods);
	}
	
	/*
	 * 写入商品列表
	 * 参数:map_goods是HashMap数组类型的Goods列表
	 * 返回值:写入成功返回true，写入失败返回false
	  
	public static boolean setGoodsList(HashMap<Integer,Goods> map_goods){
		return FileTools.writeData(map_goods, path_goods);
	}
	
	//仓管主入口
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
	
	 //客户端商品入库请求
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
	 * 服务器商品入库检测
	 * 参数：datas数据包，服务器socket
	  
	public static void stockInOutCheck(Datas datas,Socket socket){
		//如果数据包异常则退出
		if(!checkDatas(datas,socket)){
			return;
		}
		
		String flags=datas.getFlags();//获取客户端发送来的标识
		
		//检测是否有这个商品
		int goodsid=datas.getGoods().getGoodid();//获取商品id
		int stocknum=datas.getGoods().getStock();//获取数量
		HashMap<Integer,Goods> map_goods=checkContainsGoods(datas);
		
		if(map_goods!=null){
			//检测商品库存是否足够
			if(checkStockEnough(datas)){
				Goods goods=map_goods.get(goodsid);
				//如果是入库则是加库存，如果是出库则减库存
				if(flags.equals(DEFINE.SYS_GOODS_IN)){
					goods.setStock(goods.getStock()+stocknum);
				}else{
					goods.setStock(goods.getStock()-stocknum);
				}
				map_goods.put(goodsid, goods);
				//写入商品信息
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

	 //客户端商品出库请求
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
	 * 发送商品数据到服务器 
	 * 参数：datas数据包，goods商品
	  
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
	 * 检测数据包异常
	 * 参数：datas数据包，服务器socket
	 * 返回值：数据包正常返回true，异常返回false
	  
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
	 * 检测商品是否存在
	 * 参数：datas数据包
	 * 返回值：HashMap数组类型的goods列表
	
	private static HashMap<Integer,Goods> checkContainsGoods(Datas datas){
		int goodsid=datas.getGoods().getGoodid();
		HashMap<Integer,Goods> map_goods=getGoodsList();
		
		if(map_goods.containsKey(goodsid)){
			return map_goods;
		}
		
		return null;
	}
	
	//更新商品信息
	private static boolean updateGoods(HashMap<Integer,Goods> map_goods){
		if(FileTools.writeData(map_goods, path_goods)){
			return true;
		}
		
		return false;
	}
	

	 * 服务器添加商品检测
	 * 参数：datas数据包,服务器socket
	
	public static void stockAddCheck(Datas datas,Socket socket){
		if(!checkDatas(datas,socket)){
			return;
		}
		
		datas.setFlags(DEFINE.SYS_OPERATOR_FAIL);
		int goodsid=datas.getGoods().getGoodid();
		HashMap<Integer,Goods> map_goods=checkContainsGoods(datas);
		
		//如果商品不存在则可以添加
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

	//客户端添加商品请求
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
	
	//检测库存是否足够
	private static boolean checkStockEnough(Datas datas){
		HashMap<Integer,Goods> map_goods=getGoodsList();
		int goodsid=datas.getGoods().getGoodid();//商品id
		int stocknum=datas.getGoods().getStock();
		//如果是入库，则返回true
		if(datas.getFlags().equals(DEFINE.SYS_GOODS_IN)){
			return true;
		}
		if(map_goods.get(goodsid).getStock()>=stocknum){
			return true;
		}
		return false;
	}
	
	//显示商品数据
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
		System.out.println("编号\t\t\t\t产品名称\t\t\t数量\t\t\t\t单价\t\t\t计量单位");
		
		while(it.hasNext()){
			Goods goods=map_goods.get(it.next());
			System.out.println(goods.getGoodid()+"\t\t\t\t"+goods.getName()+"\t\t\t\t"
			+goods.getStock()+"\t\t\t\t"+goods.getPrice()+"\t\t\t\t"+goods.getUnit());
		}
	}
	

	 * 服务器返回全部商品
	 * 参数：datas数据包，服务器socket
	 * 返回值：查询成功返回datas，失败返回null
	
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
	//客户端按商品编号查询商品
	public static void GoodsQueryByid(){
		Datas datas=new Datas();
		Goods goods=new Goods();
		
		datas.setFlags(DEFINE.SYS_GOODS_QUERY_BYID);
		goods.setGoodid(InputTools.getInt(Message.INPUT_GOODSID, Message.ERR_GOODSID));
		Datas datas2=sendGoods(datas,goods);
		
		if(datas2.getFlags().equals(DEFINE.SYS_OPERATOR_SUCCESS)){
			Goods goods2=datas2.getGoods();
			System.out.println("编号\t\t\t\t产品名称\t\t\t数量\t\t\t\t单价\t\t\t计量单位");
			System.out.println(goods2.getGoodid()+"\t\t\t\t"+goods2.getName()+"\t\t\t\t"
					+goods2.getStock()+"\t\t\t\t"+goods2.getPrice()+"\t\t\t\t"+goods2.getUnit()+"\n");
		}
	}
	

	 * 服务器返回商品查询
	 * 参数：datas数据包，服务器socket
	 * 返回值：查询成功返回datas，否则返回null

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
	