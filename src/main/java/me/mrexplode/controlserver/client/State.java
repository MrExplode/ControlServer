package me.mrexplode.controlserver.client;


public class State {
	
	/**
	 * The type of the state reporting device.
	 */
	public Type type;
	
	/**
	 * Actual information about the current state of the device.
	 */
	public String state;
	
	/**
	 * Additional information about the current state of the device.
	 */
	public String description;
	
	/**
	 * The IP4 address of the device.
	 */
	public String ip;
	
	/**
	 * The communication port for the device.
	 */
	public int port;
	
	public State(Type type, String ip, int port) {
		this.type = type;
	}
	
}
