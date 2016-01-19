package client;

public class Client{ 

	public static void main(String[] args) {
		//runClient();
		// SimpleUI sui = new SimpleUI();
		@SuppressWarnings("unused")
		//AdministratorUI aui = new AdministratorUI("d");
		// ManagerUI mui = new ManagerUI("dd");
		LoginUI lui = new LoginUI();
	}
	/*
	public static void runClient(){
		int int_select=0;
		
		int_select=InputTools.getInt(null, Message.ERR_SELECT);
		if(int_select==2){
			return;
		}
		
		if(int_select!=1){
			System.out.println(Message.ERR_SELECT);
			runClient();
		}
		UserExt.login();//ÓÃ»§µÇÂ¼
		System.out.println(Message.OUTPUT_START+Message.OUTPUT_START+Message.OUTPUT_START);
	}
	*/
}