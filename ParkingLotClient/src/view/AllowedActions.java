package view;

public class AllowedActions { // a struct used for configuring the GUI MainFrame for different users
	final boolean CHANGE_FLOOR;
	final boolean ADD_DRIVER;
	final boolean CHANGE_EXPIRATION;
	final boolean REMOVE_DRIVER;
	final boolean EXPIRED_REPORT;
	final boolean OPEN_GATE;
	final boolean CHANGE_PW;
	final boolean RESERVE_SPOT;
	final boolean CHANGE_OTHERS_PW;
	final int NUM_OF_ALLOWED_ACTIONS;
	final int NUM_OF_MAX_ALLOWED_ACTIONS;
	// logout is always allowed
	
	public AllowedActions(boolean changeFloor, boolean addDriver, boolean changeExpiration, boolean removeDriver, boolean expiredReport, boolean openGate, boolean changePW, boolean reserveSpot, boolean changeOthersPW) {
		CHANGE_FLOOR = changeFloor;
		ADD_DRIVER = addDriver;
		CHANGE_EXPIRATION = changeExpiration;
		REMOVE_DRIVER = removeDriver;
		EXPIRED_REPORT = expiredReport;
		OPEN_GATE = openGate;
		CHANGE_PW = changePW;
		RESERVE_SPOT = reserveSpot;
		CHANGE_OTHERS_PW = changeOthersPW;
		int numOfAllowedActions = 1; // logout is always allowed
		
		if (CHANGE_FLOOR) numOfAllowedActions += 2;
		if (ADD_DRIVER) numOfAllowedActions++;
		if (CHANGE_EXPIRATION) numOfAllowedActions++;
		if (REMOVE_DRIVER) numOfAllowedActions++;
		if (EXPIRED_REPORT) numOfAllowedActions++;
		if (OPEN_GATE) numOfAllowedActions++;
		if (CHANGE_PW) numOfAllowedActions++;
		if (RESERVE_SPOT) numOfAllowedActions++;
		NUM_OF_ALLOWED_ACTIONS = numOfAllowedActions;
		NUM_OF_MAX_ALLOWED_ACTIONS = 9;
	}
}
