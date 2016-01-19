import java.awt.*;
import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

@SuppressWarnings("serial")
public class UI extends JFrame{
	private JLabel jlb = new JLabel(" words split by space ");
	private JTextField jtfInput = new JTextField();
	private JButton jbtSearch = new JButton("Test");
	private JTextArea jtaResult= new JTextArea();
	//For JLabel
	private static final Font font1 = new Font("Calibri",Font.PLAIN+Font.BOLD,14);
	//For Chinese
	private static final Font font3 = new Font("Î¢ÈíÑÅºÚ",Font.BOLD, 15);
	
	//Default Constructor
	/**
	 * @throws Exception
	 */
	public UI() throws Exception {
		
		SengenTest st = new SengenTest();
		
		//Components
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		jlb.setFont(font1);
		p1.add(jlb, BorderLayout.WEST);
		jtfInput.setFont(font3);
		p1.add(jtfInput, BorderLayout.CENTER);
		jbtSearch.setBackground(Color.WHITE);
		p1.add(jbtSearch,BorderLayout.EAST);
		add(p1,BorderLayout.NORTH);
		jtaResult.setFont(font3);		
		JScrollPane scrollPane = new JScrollPane(jtaResult);
		add(scrollPane,BorderLayout.CENTER);
		
		/** JTextField for the word Keyboard actionListener */
		jtfInput.addKeyListener(new KeyAdapter() {
			//Why released trigger?
			//The getText is up-to-date!
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				String sent=(jtfInput.getText()).trim();	
				//Enter
				if(keyCode == KeyEvent.VK_ENTER){
					//check
					if(sent != null) {
						// start time
						Date d = new Date();
					    long s1,s2;
					    s1 = d.getTime();		
						st.setWords(sent);							    
						st.run();
						// end time
					    d = new Date();
					    s2 = d.getTime();
					    String time = String.valueOf((s2-s1)/(double)1000);
						String timeString = "  Execution time is: "+time+ " seconds.";
						
						String content = st.showResults();
						content = content.concat(timeString);						
						jtaResult.setText(content);
					}
				}
			}		
		});
		
		/** Search click action listener */
		jbtSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sent=(jtfInput.getText()).trim();
				if(sent != null) {
					// start time
					Date d = new Date();
				    long s1,s2;
				    s1 = d.getTime();		
					st.setWords(sent);							    
					st.run();
					// end time
				    d = new Date();
				    s2 = d.getTime();
				    String time = String.valueOf((s2-s1)/(double)1000);
					String timeString = "Execution time is: "+time+ " seconds.";
					
					String content = st.showResults();
					content = content.concat(timeString);						
					jtaResult.setText(content);
				}
			}
		});		
	}
	
}
