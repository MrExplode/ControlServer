package me.mrexplode.controlserver.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ServerListener implements Runnable {

	DatagramSocket udpSocket;
	
	public ServerListener() {
		try {
			udpSocket = new DatagramSocket(42069, InetAddress.getByName("0.0.0.0"));
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
	}

}
