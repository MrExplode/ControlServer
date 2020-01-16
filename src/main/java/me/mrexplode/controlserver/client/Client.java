package me.mrexplode.controlserver.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/*
 * Protocol description
 * 
 * int32 packet length
 * int32 protocol version
 * int32[10] user values
 * byte[64] two floats. one float = 4 byte -> 32 bit
 */

public class Client implements Runnable {
    
    private float turn = 0f;
    private float throttle = 0f;
    private byte[] buffer = new byte[448];
    
    private State state;
    
    private boolean running = true;
    
    
    
    public Client() {
        try {
			state = new State(Type.CLIENT, InetAddress.getLocalHost().getHostAddress(), 42069);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        state.state = "Waiting";
        state.description = "Waiting for server commands";
    }

    @Override
    public void run() {
        ArrayList<Controller> gameControllers = (ArrayList<Controller>) Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers());
        
        Controller controller = gameControllers.get(0);
        Component steering = controller.getComponent(Identifier.Axis.X);
        Component throttle = controller.getComponent(Identifier.Axis.Z);
        
        long timer = 0;
        while (running) {
            if (System.currentTimeMillis() > timer + 50) {
                if (!controller.poll()) {
                    //controller not available, acts as disabled
                	this.state.state = "No controller";
                	this.state.description = "Controller either disconnected, or not available";
                    System.out.println("Controller disconnected!");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //controller available
                }
            }
        }
    }
    
    public State getState() {
    	return state;
    }

}
