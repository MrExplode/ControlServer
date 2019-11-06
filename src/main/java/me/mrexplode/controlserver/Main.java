package me.mrexplode.controlserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Main {
    
    private float turn = 0f;
    private float throttle = 0f;
    private byte[] buffer = new byte[64];
    private boolean server = true;
    
    
    public static void main(String[] args) {
        //new ConsoleStarter("ControlServer", "args goes here").start();
        System.out.println("Starting up ControlServer...");
        float[] floatA = {1.1f, 2.2f};
        for (float f : floatA) {
            System.out.print(f + " ");
        }
        System.out.println();
        byte[] byteA = float2byteArray(floatA);
        for (byte b : byteA) {
            System.out.print(b + " ");
        }
        System.out.println();
        
        float[] floatB = byte2floatArray(byteA);
        for (float f : floatB) {
            System.out.print(f + " ");
        }
        //new Main().run();
    }

    private void run() {
        try {
            DatagramSocket socket = new DatagramSocket(69420);
            
            while (true) {
                if (server) {
                    //send data
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                } else {
                    //recieve data
                }
                
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
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

}
