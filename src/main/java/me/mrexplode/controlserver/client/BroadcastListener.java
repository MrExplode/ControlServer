package me.mrexplode.controlserver.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/*
 * Server broadcast packet
 * byte[15] header
 * int32 length
 * byte[16] ip
 * int32 port
 */
public class BroadcastListener implements Runnable {
	
	private DatagramSocket udpSocket;
	private boolean running = true;
	
	private byte[] buffer = new byte[1024];
	
	public BroadcastListener() {
		try {
			udpSocket = new DatagramSocket(42069, InetAddress.getByName("0.0.0.0"));
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (running) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				udpSocket.receive(packet);
			} catch (IOException e) {
				System.err.println("Failed to recieve server broadcast packet!");
				e.printStackTrace();
				return;
			}
			
			//not our packet
			if (packet.getLength() < 39 || !new String(packet.getData(), 0, 15).equals("Control-Server\0")) {
				return;
			}
			
			ByteBuffer buffer1 = ByteBuffer.wrap(packet.getData(), 15, 4);
			int packetLength = buffer1.getInt();
			
			String serverIP = new String(packet.getData(), 19, 8);
			ByteBuffer buffer2 = ByteBuffer.wrap(packet.getData(), 27, 4);
		}
	}

}
