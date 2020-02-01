package view;

import java.util.Date;
import java.util.LinkedList;

public interface GUIObserver {
	public void loginEvent(String username, String password);
	
	public void floorChangedEvent(LinkedList<Integer> unknownParkingSpots);
	
	public void logoutEvent();
	
	public void openGateEvent();
	
	public void cancelReservationEvent(int parkingSpotId);

	public void changePasswordEvent(String username, String password);
	
	public void deleteDriverEvent(String username);
	
	public void addDriverEvent(String name, String passwordStr, Date expirationDate);
	
	public void updateDriverExpirationEvent(String username, Date date);
	
	public void reserveParkingSpotEvent(int parkingSpotID, String reservedFor, Date expirationDate);
	
	public void requestForExpiredUsersEvent();
}
