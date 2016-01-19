import javax.swing.JFrame;

public class Sengen {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		UI ui = new UI();
		ui.setTitle("Sengen");
		ui.setSize(650,450);
		ui.setLocationRelativeTo(null);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setVisible(true);
		/*
		String s = new String();
		Scanner input = new Scanner(System.in);
		s = input.nextLine();
		// start time
		Date d=new Date();
	    long s1,s2;
	    s1=d.getTime();			    
		SengenTest st = new SengenTest(s);
		st.run();
		input.close();
		
		// end time
	    d=new Date();
	    s2=d.getTime();
	    System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");
	    */
	}

}
