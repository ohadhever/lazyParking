package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


@SuppressWarnings("serial")
public class AddDriverPanel extends JPanel implements ActionListener {
	private JTextField name;
	private JPasswordField password;
	private ExpirationDatePanel datePanel;
	private GUI parent;
	
	public AddDriverPanel(GUI parent) {
		this.parent = parent;
		setLayout(new GridLayout(3, 1));
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("name: "));
		name = new JTextField(20);
		namePanel.add(name);
		add(namePanel);
		JPanel passwordPanel = new JPanel();
		passwordPanel.add(new JLabel("password: "));
		password = new JPasswordField(20);
		passwordPanel.add(password);
		add(passwordPanel);
		datePanel = new ExpirationDatePanel();
		add(datePanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (JOptionPane.showConfirmDialog(parent.getActiveWindow(), this, "Add Driver", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			String nameStr = name.getText(); 
			if (!nameStr.matches("[A-Za-z]{2,}[ ]+[A-Za-z]{2,}")) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "enter the driver's first and last names. each name has to be at least 2 letters long and must include only letters", "INVALID NAME", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String names[] = nameStr.split(" ");
			if (names[0].length() + names[1].length() > 20) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "the combined length of the first and last names must not exceed 20 letters", "INVALID NAME", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String passwordStr = new String(password.getPassword());
			if (passwordStr.equals("")) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "the password must not be empty", "INVALID PASSWORD", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Date expirationDate = datePanel.getDate();
			if (!datePanel.isUnlimited() && expirationDate == null) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "the year must be a non-negative integer", "INVALID DATE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			LinkedList<GUIObserver> observers = parent.getObservers();
			for (GUIObserver observer : observers)
				observer.addDriverEvent(nameStr, passwordStr, expirationDate);
			
			name.setText("");
			password.setText("");
		}
	}
}