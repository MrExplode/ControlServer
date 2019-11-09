package me.mrexplode.controlserver;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fazecast.jSerialComm.SerialPort;

import me.mrexplode.consolestarter.ConsoleStarter;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class Main {
    
    private float turn = 0f;
    private float throttle = 0f;
    private byte[] buffer = new byte[64];
    private boolean server = true;
    
    private String ip = "127.0.0.1";
    private int port = 42069;
    private SerialPort serialPort;
    @SuppressWarnings("unused")
    private static String controllerName = "Generic   USB  Joystick";
    
    
    public static void main(String[] args) {
        new ConsoleStarter("ControlServer", "serial", "server", "127.0.0.1", "42069").start();
        System.out.println("Starting up ControlServer...");
        System.out.println("Mode: " + args[1]);
        System.out.println("Communication protocol: " + args[0]);
        System.out.println("target ip: " + args[2]);
        System.out.println("Target port: " + args[3]);
        //loadLibraries("D:\\pjano\\Documents\\Eclipse\\git\\lib", ".dll");
        if (args[0].equals("serial")) {
            new Main(args[1].equals("server"), args[2], Integer.valueOf(args[3])).runSerial();
        } else {
            new Main(args[1].equals("server"), args[2], Integer.valueOf(args[3])).runUDP();
        }
    }
    
    public Main(boolean server, String ip, int port) {
        this.server = server;
        this.ip = ip;
        this.port = port;
    }
    
    public void runSerial() {
        long timer = 0;
        long statTimer = 0;
        if (server) {
            ArrayList<Controller> gameControllers = (ArrayList<Controller>) Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers())
                    .stream().filter(x -> (x.getType() == Controller.Type.GAMEPAD) || x.getType() == Controller.Type.WHEEL || x.getType() == Controller.Type.STICK).collect(Collectors.toList());
            Controller controller = gameControllers.get(0);
            Component comp1 = controller.getComponent(Identifier.Axis.X);
            Component comp2 = controller.getComponent(Identifier.Axis.RZ);
            
            this.serialPort = SerialPort.getCommPort("todo");
            this.serialPort.openPort();
            Runtime.getRuntime().addShutdownHook(new Thread(()->serialPort.closePort()));
            while (true) {
                if (System.currentTimeMillis() - timer > 50) {
                    timer = System.currentTimeMillis();
                    
                    if (!controller.poll()) {
                        System.out.println("Controller disconnected!\nStopping...");
                        break;
                    }
                    
                    this.turn = comp1.getPollData();
                    this.throttle = comp2.getPollData();
                    
                    serialWrite(float2byteArray(turn, throttle));
                }
                
                //statistics
                if (System.currentTimeMillis() - statTimer > 1000) {
                    statTimer = System.currentTimeMillis();
                    System.out.println("Sent data to [" + serialPort.getPortDescription() + "]: turn=" + turn + " throttle=" + throttle);
                }
            }
            serialPort.closePort();
        } else {
            throw new UnsupportedOperationException("Client mode is not supported in Serial mode!");
        }
    }

    public void runUDP() {
        try {
            DatagramSocket socket = new DatagramSocket((server ? port -1 : port));
            long timer = 0;
            long statTimer = 0;
            if (server) {
                ArrayList<Controller> gameControllers = (ArrayList<Controller>) Arrays.asList(ControllerEnvironment.getDefaultEnvironment().getControllers())
                        .stream().filter(x -> (x.getType() == Controller.Type.GAMEPAD) || x.getType() == Controller.Type.WHEEL || x.getType() == Controller.Type.STICK).collect(Collectors.toList());
                Controller controller = gameControllers.get(0);
                Component comp1 = controller.getComponent(Identifier.Axis.X);
                Component comp2 = controller.getComponent(Identifier.Axis.RZ);
                
                while (true) {
                  //25ms sending time
                    if (System.currentTimeMillis() - timer > 50) {
                        timer = System.currentTimeMillis();
                        
                        //poll controller data
                        if (!controller.poll()) {
                            System.out.println("Controller disconnected!\nStopping...");
                            break;
                        }
                        
                        this.turn = comp1.getPollData();
                        this.throttle = comp2.getPollData();
                        
                        //send data
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), port);
                        packet.setData(float2byteArray(turn, throttle));
                        socket.send(packet);
                    }
                    
                    //stat timing, 1 sec
                    if (System.currentTimeMillis() - statTimer > 1000) {
                        statTimer = System.currentTimeMillis();
                        System.out.println("Sent data to [" + ip + ":" + port + "]: turn=" + turn + " throttle=" + throttle);
                    }
                }
            } else {
                //recieve data
                while (true) {
                    timer = System.currentTimeMillis();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    long recInterval = System.currentTimeMillis() - timer;
                    float[] data = byte2floatArray(packet.getData());
                    System.out.println("Recieved data from [" + packet.getAddress().getHostAddress() + ":" + packet.getPort() + "] turn=" + data[0] + " throttle=" + data[1] + " packet interval=" + recInterval + "ms");
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void serialWrite(byte[] data) {
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
        this.serialPort.writeBytes(buffer, buffer.length);
    }
    
    private static byte[] float2byteArray(float... args) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * args.length);
        
        for (float f : args) {
            buffer.putFloat(f);
        }
        
        return buffer.array();
    }
    
    private static float[] byte2floatArray(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        FloatBuffer fb = buffer.asFloatBuffer();
        float[] fa = new float[fb.limit()];
        fb.get(fa);
        return fa;
    }
    
    private static void loadLibraries(String folder, String type) {
        String[] files = new File(folder).list();
        for (String file : files) {
            if (file.endsWith(type)) {
                System.out.println("Loading " + file);
                System.load(folder + "\\" + file);
            }
        }
        System.out.println("Successfully loaded libraries!");
    }

}
