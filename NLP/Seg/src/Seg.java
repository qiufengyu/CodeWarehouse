import javax.swing.JFrame;

public class Seg {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		UI ui = new UI();
		ui.setTitle("Seg");
		ui.setSize(650,450);
		ui.setLocationRelativeTo(null);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setVisible(true);
	}
		/*
		Scanner input = new Scanner(System.in);
		SegTest st = new SegTest();
		while(true) {
			System.out.print("Input chinese sentence (-1 to exit): ");
			String s = input.nextLine();
			if(s == null || s.equals("-1"))
				break;
			st.seg(s);
		}
		input.close();
		System.out.println("Thank you for using :D");
		*/
}
