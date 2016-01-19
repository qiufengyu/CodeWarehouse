import javax.swing.JFrame;

public class Token {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		UI ui = new UI();
		ui.setTitle("Seg");
		ui.setSize(650,250);
		ui.setLocationRelativeTo(null);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setVisible(true);
		/*
		Scanner input = new Scanner(System.in);
		TokenTest st = new TokenTest();
		while(true) {
			System.out.print("Input word (-1 to exit): ");
			String s = input.nextLine();
			if(s == null || s.equals("-1"))
				break;
			st.stemmer(s);
		}
		input.close();
		System.out.println("Thank you for using :D");
		*/
	}

}
