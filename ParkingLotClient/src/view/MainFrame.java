package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	public final int VERTICAL_GAP = 10;
	public final int UNRESIZABLE_COMPENSATION = -10; // when you set the frame to setResizable(false), it causes a bug that changes the computations of the component sizes
	public final int NUM_OF_FLOORS = 2;
	public final int PARKING_SPOT_WIDTH = 42; // in pixels
	public final int PARKING_SPOT_HEIGHT = 82; // in pixels
	private int currentFloor;
	private Floor[] floors;
	private MapPanel currentCenterPanel;
	private HashMap<Integer, ParkingSpot> idToSpot;
	private LinkedList<GUIObserver> observers;
	private GUI parent;
	private JButton upBtn;
	private JButton downBtn;
	private JPanel actionsPanel;
	private AllowedActions allowedActions;

	
	public MainFrame(AllowedActions allowedActions, GUI parent, String username) {
		this.allowedActions = allowedActions;
		this.parent = parent;
		observers = parent.getObservers();
		idToSpot = new HashMap<Integer, ParkingSpot>();
		currentFloor = 0;
		constructFloors();
		currentCenterPanel = new MapPanel(floors[currentFloor]);
		add(currentCenterPanel, BorderLayout.CENTER);
		constructActionsPanel(username);
		add(actionsPanel, BorderLayout.EAST);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() { // does the same as the logout button
			@Override
			public void windowClosing(WindowEvent event) {
				if (JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure you want to log out?", "Log out", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					parent.revertToLoginFrame();
			}
		});
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	
	public void setParkingSpot(int id, boolean isOccupied, boolean isReserved, String reservedFor) {
		ParkingSpot spot = idToSpot.get(id);
		spot.isOccupied = isOccupied;
		spot.isReserved = isReserved;
		spot.reservedFor = reservedFor;
		spot.isValid = true;
		if (spot.floor == currentFloor) {
			currentCenterPanel.revalidate();
			currentCenterPanel.repaint();
		}
	}
	
	
	private void constructActionsPanel(String username) {
		final int DEFAULT_BUTTON_WIDTH = 120;
		final int DEFAULT_BUTTON_HEIGHT = 50;
		Dimension buttonDimension = new Dimension(DEFAULT_BUTTON_WIDTH, DEFAULT_BUTTON_HEIGHT);
		actionsPanel = new JPanel(new GridLayout(allowedActions.NUM_OF_MAX_ALLOWED_ACTIONS, 1, 0, VERTICAL_GAP));
		
		if (allowedActions.CHANGE_FLOOR) {
			upBtn = new JButton() {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					int width = upBtn.getWidth();
					int height = upBtn.getHeight();
					int x[] = {0, width / 2, width};
					int y[] = {height, 0, height};
					Color previousColor = g.getColor();
					if (this.isEnabled())
						g.setColor(Color.RED);
					else
						g.setColor(Color.LIGHT_GRAY);
					g.fillPolygon(x, y, 3);
					g.setColor(previousColor);
				}
			};
			upBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentFloor++;
					if (currentFloor == NUM_OF_FLOORS - 1) // the top floor
						upBtn.setEnabled(false);
					floorChanged();
					if (currentFloor > 0) // not the bottom floor
						downBtn.setEnabled(true);
				}
			});
			
			downBtn = new JButton() {
				@Override
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					int width = downBtn.getWidth();
					int height = downBtn.getHeight();
					int x[] = {0, width / 2, width};
					int y[] = {0, height, 0};
					Color previousColor = g.getColor();
					if (this.isEnabled())
						g.setColor(Color.RED);
					else
						g.setColor(Color.LIGHT_GRAY);

					g.fillPolygon(x, y, 3);
					g.setColor(previousColor);
				}
			};
			downBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentFloor--;
					if (currentFloor == 0) // the bottom floor
						downBtn.setEnabled(false);
					floorChanged();
					if (currentFloor < NUM_OF_FLOORS - 1) // not the top floor
						upBtn.setEnabled(true);
				}
			});
			
			upBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(upBtn);
			downBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(downBtn);
			if (currentFloor == 0)
				downBtn.setEnabled(false);
			if (currentFloor == NUM_OF_FLOORS - 1)
				upBtn.setEnabled(false);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.ADD_DRIVER) {
			JButton addDriverBtn = new JButton("Add Driver");
			AddDriverPanel addDriverPanel = new AddDriverPanel(parent); 
			addDriverBtn.setPreferredSize(buttonDimension);
			addDriverBtn.addActionListener(addDriverPanel);
			actionsPanel.add(addDriverBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.CHANGE_EXPIRATION) {
			JButton changeExpBtn = new JButton("<html><center>Change Driver<br>Expiration Date</center></html>");
			changeExpBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String username = JOptionPane.showInputDialog(MainFrame.this, "username:", "Change Driver Expiration Date", JOptionPane.QUESTION_MESSAGE);
					if (username != null && !username.equals("")) {
						JPanel updateExpirationPanel = new JPanel(new BorderLayout());
						updateExpirationPanel.add(new JLabel("Set new expiration date for " + username), BorderLayout.NORTH);
						ExpirationDatePanel expirationDatePanel = new ExpirationDatePanel();
						updateExpirationPanel.add(expirationDatePanel, BorderLayout.CENTER);
						Date date = null;
						if (JOptionPane.showConfirmDialog(MainFrame.this, updateExpirationPanel, "Update expiration", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
							if (expirationDatePanel.isUnlimited() || (date = expirationDatePanel.getDate()) != null)
								for (GUIObserver observer : observers)
									observer.updateDriverExpirationEvent(username, date);
							else
								JOptionPane.showMessageDialog(MainFrame.this, "the year must be a non-negative integer", "INVALID DATE", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			changeExpBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(changeExpBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.REMOVE_DRIVER) {
			JButton removeDriverBtn = new JButton("Remove Driver");
			removeDriverBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String username = JOptionPane.showInputDialog(MainFrame.this, "username to remove:", "Remove Driver", JOptionPane.QUESTION_MESSAGE);
					if (username != null && !username.equals(""))
						if (JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure you want to delete " + username + "?", "Delete Driver", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
							for (GUIObserver observer : observers)
								observer.deleteDriverEvent(username);
				}
			});
			removeDriverBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(removeDriverBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.EXPIRED_REPORT) {
			JButton expiredRprtBtn = new JButton("<html><center>Expired Users<br>Report</center></html>");
			expiredRprtBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(MainFrame.this, new ExpiredUsersReportPanel(parent), "Expired Users Report", JOptionPane.PLAIN_MESSAGE);
					parent.setExpiredUsersReportPanel(null);
				}
			});
			expiredRprtBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(expiredRprtBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.OPEN_GATE) {
			JButton openGateBtn = new JButton("Open Gate");
			openGateBtn.setBackground(Color.RED);
			openGateBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure you want to open the gate?", "Open Gate", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						for (GUIObserver observer : observers)
							observer.openGateEvent();
				}
			});
			openGateBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(openGateBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		if (allowedActions.CHANGE_PW) {
			JButton changePWBtn = new JButton("<html><center>Change<br>Password</center></html>");
			changePWBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new ChangePasswordDialog(username, allowedActions.CHANGE_OTHERS_PW, parent);
				}
			});
			changePWBtn.setPreferredSize(buttonDimension);
			actionsPanel.add(changePWBtn);
		}
		else
			actionsPanel.add(new JLabel());
		
		JButton logoutBtn = new JButton("Log Out");
		logoutBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure you want to log out?", "Log out", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					parent.revertToLoginFrame();
			}
		});
		logoutBtn.setPreferredSize(buttonDimension);
		actionsPanel.add(logoutBtn);
	}
	
	
	private void constructFloors() {
		floors = new Floor[NUM_OF_FLOORS];
		int k = 0;
		for (int i = 0; i < floors.length; i++) {
			floors[i] = new Floor();
			try {
				floors[i].image = ImageIO.read(new File("floor.jpg"));
			} catch (IOException e) {
				floors[i].image = null;
				JOptionPane.showMessageDialog(this, "The parking lot image for floor " + i + " could not be loaded!", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
			floors[i].rows = new ParkingRow[2];
			floors[i].rows[0] = new ParkingRow();
			floors[i].rows[0].upperLeftX = 6;
			floors[i].rows[0].upperLeftY = 3;
			floors[i].rows[0].parkingSpots = new ParkingSpot[19];
			for (int j = 0; j < floors[i].rows[0].parkingSpots.length; j++)
				floors[i].rows[0].parkingSpots[j] = new ParkingSpot(++k, i);
			floors[i].rows[1] = new ParkingRow();
			floors[i].rows[1].upperLeftX = 200;
			floors[i].rows[1].upperLeftY = 130;
			floors[i].rows[1].parkingSpots = new ParkingSpot[14];
			for (int j = 0; j < floors[i].rows[1].parkingSpots.length; j++)
				floors[i].rows[1].parkingSpots[j] = new ParkingSpot(++k, i);
		}
	}
	
	
	void floorChanged() { // PACKAGE PRIVATE so GUI could call it once MainFrame is completely constructed
		LinkedList<Integer> unknownParkingSpots = new LinkedList<Integer>();
		for (int i = 0; i < floors[currentFloor].rows.length; i++)
			for (int j = 0; j < floors[currentFloor].rows[i].parkingSpots.length; j++)
				if (!floors[currentFloor].rows[i].parkingSpots[j].isValid)
					unknownParkingSpots.add(floors[currentFloor].rows[i].parkingSpots[j].id);
		remove(currentCenterPanel);
		currentCenterPanel = new MapPanel(floors[currentFloor]);
		add(currentCenterPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
		if (!unknownParkingSpots.isEmpty())
			for (GUIObserver observer : observers)
				observer.floorChangedEvent(unknownParkingSpots);
	}
	
	
	private class ParkingSpot {
		public boolean isValid;
		public int id;
		public boolean isOccupied;
		public boolean isReserved;
		public String reservedFor;
		public int floor;
		
		public ParkingSpot(int id, int floor) {
			this.id = id;
			this.floor = floor;
			isValid = false;
			isOccupied = false;
			reservedFor = null;
			idToSpot.put(id, this);
		}
	}
	
	
	private class ParkingRow {
		public int upperLeftX;
		public int upperLeftY;
		public ParkingSpot[] parkingSpots;
	}
	
	
	private class Floor {
		public ParkingRow[] rows;
		public Image image;
	}
	
	
	private class MapPanel extends JPanel {
		public final int DEFAULT_WIDTH = 294;
		public final int DEFAULT_HEIGHT = 809;
		private Floor floor;
		private Image car;
		
		public MapPanel(Floor floor) {
			this.floor = floor;
			try {
				car = ImageIO.read(new File("car.png"));
			} catch (IOException e) {
				car = null;
				JOptionPane.showMessageDialog(this, "The car image could not be loaded!", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
			if (floor.image != null)
				setPreferredSize(new Dimension(floor.image.getWidth(null) + UNRESIZABLE_COMPENSATION, floor.image.getHeight(null) + UNRESIZABLE_COMPENSATION));
			else
				setPreferredSize(new Dimension(DEFAULT_WIDTH + UNRESIZABLE_COMPENSATION, DEFAULT_HEIGHT + UNRESIZABLE_COMPENSATION));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton()==MouseEvent.BUTTON1 && allowedActions.RESERVE_SPOT) { // BUTTON1 = left mouse button
						int x=e.getX();
						int y=e.getY();
					
						for (int i = 0; i < floor.rows.length; i++)
							if (x >= floor.rows[i].upperLeftX && x <= floor.rows[i].upperLeftX + PARKING_SPOT_HEIGHT && y >= floor.rows[i].upperLeftY && y <= floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * floor.rows[i].parkingSpots.length) {
								ParkingSpot spot = floor.rows[i].parkingSpots[(y - floor.rows[i].upperLeftY) / PARKING_SPOT_WIDTH];
								if (spot.isValid) {
									if (spot.isReserved) {
										if (JOptionPane.showConfirmDialog(MainFrame.this, "You are cancelling the reservation for parking spot " + spot.id, "Cancel Reservation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
											for (GUIObserver observer : observers)
												observer.cancelReservationEvent(spot.id);
									}
									else {
										ReserveParkingSpotPanel reserveParkingSpotPanel = new ReserveParkingSpotPanel(parent, spot.id);
										reserveParkingSpotPanel.displayDialog();
									}
								}
							}
								
					}
				}
			});
		}
		
		@Override
		public void paintComponent(Graphics g) {
			if (floor.image != null)
				g.drawImage(floor.image, 0, 0, null);
			else
				g.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
			ParkingSpot spot;
			for (int i = 0; i < floor.rows.length; i++)
				for (int j = 0; j < floor.rows[i].parkingSpots.length; j++) {
					spot = floor.rows[i].parkingSpots[j];
					if (spot.isValid) {
						if (spot.isReserved) {
							Color previousColor = g.getColor();
							g.setColor(Color.YELLOW);
							g.fillRect(floor.rows[i].upperLeftX, floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * j, PARKING_SPOT_HEIGHT, PARKING_SPOT_WIDTH);
							g.setColor(previousColor);
							g.drawString("Reserved for:", floor.rows[i].upperLeftX, floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * j + PARKING_SPOT_WIDTH / 2);
							g.drawString(spot.reservedFor, floor.rows[i].upperLeftX, floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * j + PARKING_SPOT_WIDTH / 2 + g.getFontMetrics().getHeight());
						}
						if (spot.isOccupied)
							if (car != null)
								g.drawImage(car, floor.rows[i].upperLeftX, floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * j, null);
							else {
								Color previousColor = g.getColor();
								g.setColor(Color.GREEN);
								g.fillRect(floor.rows[i].upperLeftX, floor.rows[i].upperLeftY + PARKING_SPOT_WIDTH * j, PARKING_SPOT_HEIGHT, PARKING_SPOT_WIDTH);
								g.setColor(previousColor);
							}
					}
				}		
		}
	}
	
	
}
