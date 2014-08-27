import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class PingClient {

	public static void main(String[] args) throws Exception {

		long totalrtt = 0;
		long maxrtt = -9999;
		long minrtt = 9999;
		int drops = 0;
		int retPacket;

		if (args.length != 2) { // check if number of arguments are correct
			System.out.println("Required arguments: host port");
			return;
		}
		String server = args[0]; // Read first argument from user
		String serport = args[1]; // Read second argument from user
		int serverPort = Integer.parseInt(serport);
		DatagramSocket socket = new DatagramSocket();
		// Create new datagram socket
		socket.setSoTimeout(1000);
		// Set socket timeout value. Read API for
		// DatagramSocket to do this

		InetAddress serverAddress = InetAddress.getByName(server);
		// Convert server toInetAddress format;
		// Check InetAddress API for this
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];

		int sentPING = 0;
		int waitingToReceivePING = 0;
		boolean infiniteLoop = true;
		while (infiniteLoop) {
			Long time = new Long(System.currentTimeMillis());
			String payload = "PING " + sentPING + " " + time + "\n";
			// Construct data payload for PING as per the instructions
			sendData = payload.getBytes(); // Convert payload into bytes
			DatagramPacket packet = new DatagramPacket(sendData,
					sendData.length, serverAddress, serverPort);
			// Create new datagram packet
			socket.send(packet);
			System.out.println("Packet " + "" + " sent");

			DatagramPacket reply = new DatagramPacket(receiveData,
					receiveData.length); // Create datagram packet for reply

			try {
				socket.receive(reply); // wait for incoming packet reply
				byte[] buf = reply.getData();
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				InputStreamReader isr = new InputStreamReader(bais);
				BufferedReader br = new BufferedReader(isr);
				String line = br.readLine();

				// extract packet sequence number into the variable retPacket
				retPacket = Integer.parseInt(line.split(" ")[1]);

				if (retPacket != waitingToReceivePING) {
					System.out.print("Received out of order packet");
					System.out.println();
					waitingToReceivePING++;
					sentPING++;
				} else {
					System.out.println("Received from "
							+ reply.getAddress().getHostAddress() + " ,"
							+ new String(line));
					System.out.println();
					waitingToReceivePING++;
					sentPING++;
					long rtt = new Long(System.currentTimeMillis()) - time;
					if (rtt < minrtt)
						minrtt = rtt;
					if (rtt > maxrtt)
						maxrtt = rtt;
					totalrtt = rtt + totalrtt;
					// calculate total, max and min rtt
				}
			} catch (SocketTimeoutException e) {
				System.out.println("Error: Request timed out");
				drops++;
			}

		}
		long avgrtt = totalrtt / (10 - drops);

		// print and store average, max, min rtt and drops
		System.out.println("average rtt: " + avgrtt + " maxrtt: " + maxrtt
				+ " minrtt: " + minrtt + " drops: " + drops);
	}
}