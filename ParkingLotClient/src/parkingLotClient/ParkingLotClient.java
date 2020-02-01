package parkingLotClient;

import controller.Controller;

public class ParkingLotClient {
	public static final String SERVER_IP = "localhost";

	public static void main(String[] args) {
		new Controller(SERVER_IP);
	}

}
