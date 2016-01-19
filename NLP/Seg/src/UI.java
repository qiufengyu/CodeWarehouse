import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

@SuppressWarnings("serial")
public class UI extends JFrame{
	private JLabel jlb = new JLabel("  Sentence  ");
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
	public UI() throws Exception{
		SegTest st = new SegTest();
		
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
		add(jtaResult,BorderLayout.CENTER);		
		
		/** JTextField for the word Keyboard actionListener */
		jtfInput.addKeyListener(new KeyAdapter() {
			//Why released trigger?
			//The getText is up-to-date!
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				String sent=(jtfInput.getText()).toLowerCase();					
				//Enter
				if(keyCode == KeyEvent.VK_ENTER){
					//check
					if(sent != null) {
						Vector<String> v = st.seg(sent);
						String vectorContent = new String();
						int sz = v.size();
						vectorContent = vectorContent.concat(" [ ");
						for(int i=0; i<sz; i++) {
							vectorContent = vectorContent.concat(v.get(i));
							vectorContent = vectorContent.concat(", ");
						}
						vectorContent = vectorContent.substring(0, vectorContent.length()-2);
						vectorContent = vectorContent.concat(" ]");
						jtaResult.setText(vectorContent);
					}
				}
			}		
		});
		
		/** Search click action listener */
		jbtSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sent = (jtfInput.getText()).toLowerCase();
				if(sent != null) {
					Vector<String> v = st.seg(sent);
					String vectorContent = new String();
					int sz = v.size();
					vectorContent = vectorContent.concat(" [ ");
					for(int i=0; i<sz; i++) {
						vectorContent = vectorContent.concat(v.get(i));
						vectorContent = vectorContent.concat(", ");
					}
					vectorContent = vectorContent.substring(0, vectorContent.length()-2);
					vectorContent = vectorContent.concat(" ]");
					jtaResult.setText(vectorContent);
				}
			}
		});		
	}
	
}
