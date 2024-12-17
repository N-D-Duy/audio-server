package com.duynguyen;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private static final BlockingQueue<byte[]> audioDataQueue = new LinkedBlockingQueue<>();

    public static boolean init() {
        Log.info("Server is initializing...");

        Thread collectThread = new Thread(new Collect());
        collectThread.start();

        Thread senderThread = new Thread(new Sender());
        senderThread.start();

        return true;
    }

    static class Collect implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(Config.port)) {
                byte[] buffer = new byte[1024];
                Log.info("Collecting audio data...");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    if(audioDataQueue.offer(packet.getData())){
                        Log.info("Received audio data");
                    } else {
                        Log.error("Failed to add audio data to queue");
                    }
                }
            } catch (Exception e) {
                Log.error("Error in Collect: " + e.getMessage());
            }
        }
    }

    static class Sender implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                Log.info("Sending audio data...");

                while (true) {
                    byte[] audioData = audioDataQueue.take();
                    InetAddress address = InetAddress.getByName(Config.esp32Ip);
                    DatagramPacket packet = new DatagramPacket(audioData, audioData.length, address, Config.esp32Port);
                    socket.send(packet);
                    Log.info("Sent audio data");
                }
            } catch (Exception e) {
                Log.error("Error in Sender: " + e.getMessage());
            }
        }
    }
}
