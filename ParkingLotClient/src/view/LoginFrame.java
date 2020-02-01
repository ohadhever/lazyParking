package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Button;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.JPasswordField;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class LoginFrame extends JFrame {
	public final int DEFAULT_FRAME_HEIGHT = 200;
	public final int DEFAULT_FRAME_WIDTH = 200;
	public final int VERTICAL_GAP = 10;
	public final int HORIZONTAL_GAP = 20;
	public final int UNRESIZABLE_COMPENSATION = -10; // when you set the frame to setResizable(false), it causes a bug that changes the computations of the component sizes
	private JTextField txtLogIn;
	private JPasswordField passwordField;
	private GUI parent;

	/**
	 * Create the frame.
	 */
	public LoginFrame(GUI parent) {
		this.parent = parent;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(new GridLayout(1, 2, HORIZONTAL_GAP, 0));
		int frameWidth;
		int frameHeight;
		try {
		    final Image backgroundImage = ImageIO.read(new File("lazy_parking_logo.jpg"));
		    frameHeight = backgroundImage.getHeight(null);
		    frameWidth = backgroundImage.getWidth(null) * 2 + HORIZONTAL_GAP;
		    JPanel logoPanel = new JPanel() {
		        @Override public void paintComponent(Graphics g) {
		            g.drawImage(backgroundImage, 0, 0, null);
		        }
		    };
		    add(logoPanel);
		} catch (IOException e) {
			frameWidth = DEFAULT_FRAME_WIDTH;
			frameHeight = DEFAULT_FRAME_HEIGHT;
		}
		getContentPane().setPreferredSize(new Dimension(frameWidth, frameHeight + UNRESIZABLE_COMPENSATION));
		
		JPanel loginPanel = new JPanel(new GridLayout(6, 1, 0, VERTICAL_GAP));
		loginPanel.setBackground(Color.WHITE);
		loginPanel.add(new JLabel("Sign In", SwingConstants.CENTER));
		loginPanel.add(new JLabel("USERNAME"));
		txtLogIn = new JTextField();
		txtLogIn.addActionListener(new LoginListener());
		loginPanel.add(txtLogIn);
		loginPanel.add(new JLabel("PASSWORD"));
		passwordField = new JPasswordField();
		passwordField.addActionListener(new LoginListener());
		loginPanel.add(passwordField);
		
		Button button = new Button("Log In");
		button.addActionListener(new LoginListener());
		button.setBackground(Color.LIGHT_GRAY);
		loginPanel.add(button);
		add(loginPanel);
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private class LoginListener implements ActionListener {
		@Override 
		public void actionPerformed(ActionEvent e) {
			LinkedList<GUIObserver> observers = parent.getObservers();
			String username = txtLogIn.getText();
			String password = new String(passwordField.getPassword());
			passwordField.setText("");
			for (GUIObserver observer : observers)
				observer.loginEvent(username, password);
		}
	}
	
}
