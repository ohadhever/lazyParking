package view;

import java.awt.Window;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class GUI {
	private LoginFrame loginFrame;
	private MainFrame mainFrame;
	private ExpiredUsersReportPanel expiredUsersReportPanel;
	private Window activeWindow;
	private LinkedList<GUIObserver> observers; 
	
	
	public GUI() {
		mainFrame = null;
		expiredUsersReportPanel = null;
		observers = new LinkedList<GUIObserver>();
		activeWindow = loginFrame = new LoginFrame(this);
	}
	
	
	public void addObserver(GUIObserver observer) {
		observers.add(observer);
	}
	
	
	public void removeObserver(GUIObserver observer) {
		observers.remove(observer);
	}
	
	
	public void displayError(String errorMsg, String title) {
		JOptionPane.showMessageDialog(activeWindow, errorMsg, title, JOptionPane.ERROR_MESSAGE);
	}
	
	
	public void displayNotice(String msg, String title) {
		JOptionPane.showMessageDialog(activeWindow, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	public void launchMainFrame(AllowedActions allowedActions, String username) {
		activeWindow = mainFrame = new MainFrame(allowedActions, this, username);
		loginFrame.setVisible(false);
		mainFrame.floorChanged();
	}
	
	
	public void setParkingSpot(int id, boolean isOccupied, boolean isReserved, String reservedFor) {
		if (mainFrame != null)
			mainFrame.setParkingSpot(id, isOccupied, isReserved, reservedFor);
	}
	
	public void revertToPreviousWindow() {
		if (activeWindow != null)
			activeWindow.dispose();
		if (mainFrame != null)
			activeWindow = mainFrame;
		else if (loginFrame != null)
			activeWindow = loginFrame;
	}
	
	public void deleteDriver(String msg, String title) {
		if (expiredUsersReportPanel != null)
			expiredUsersReportPanel.deleteHighlightedRow();
		
		displayNotice(msg, title);
	}
	
	public void updateDriverExpiration(String msg, String title) {
		if (expiredUsersReportPanel != null)
			expiredUsersReportPanel.deleteHighlightedRow();
		
		displayNotice(msg, title);
	}
	
	public void addExpiredUser(String username, Date expirationDate) {
		if (expiredUsersReportPanel != null)
			expiredUsersReportPanel.addRow(username, expirationDate);
	}
	
	void revertToLoginFrame() {
		if (mainFrame != null)
			mainFrame.dispose();
		mainFrame = null;
		for (GUIObserver observer : observers)
			observer.logoutEvent();
		if (loginFrame != null) {
			loginFrame.setVisible(true);
			activeWindow = loginFrame;
		}
	}
	
	void setActiveWindow(Window window) {
		activeWindow = window;
	}
	
	void setExpiredUsersReportPanel(ExpiredUsersReportPanel expiredUsersReportPanel) {
		this.expiredUsersReportPanel = expiredUsersReportPanel;
	}
	
	Window getActiveWindow() {
		return activeWindow;
	}
	
	LinkedList<GUIObserver> getObservers() {
		return observers;
	}
	
	JFrame getMainFrame() {
		return mainFrame;
	}
}
