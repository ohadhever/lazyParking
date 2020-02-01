package view;

import java.awt.GridLayout;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


@SuppressWarnings("serial")
public class ReserveParkingSpotPanel extends JPanel {
	private JTextField name;
	private ExpirationDatePanel datePanel;
	private GUI parent;
	private int parkingSpotID;
	
	public ReserveParkingSpotPanel(GUI parent, int parkingSpotID) {
		this.parent = parent;
		this.parkingSpotID = parkingSpotID;
		setLayout(new GridLayout(3, 1));
		add(new JLabel("You are reserveing parking spot #" + parkingSpotID));
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Reserve for: "));
		name = new JTextField(20);
		namePanel.add(name);
		add(namePanel);
		datePanel = new ExpirationDatePanel();
		add(datePanel);
	}

	public void displayDialog() {
		if (JOptionPane.showConfirmDialog(parent.getActiveWindow(), this, "Reserve Parking Spot", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			String nameStr = name.getText(); 
			if (nameStr.length() == 0 || nameStr.length() > 20) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "the name of the person/group for whom the parking spot is reserved must not exceed 20 characters and can't be empty", "INVALID RESERVATION", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Date expirationDate = datePanel.getDate();
			if (!datePanel.isUnlimited() && expirationDate == null) {
				JOptionPane.showMessageDialog(parent.getActiveWindow(), "the year must be a non-negative integer", "INVALID DATE", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			LinkedList<GUIObserver> observers = parent.getObservers();
			for (GUIObserver observer : observers)
				observer.reserveParkingSpotEvent(parkingSpotID, nameStr, expirationDate);
			
			name.setText("");
		}
	}
}