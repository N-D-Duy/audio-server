package com.duynguyen;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.*;

public class Server {
    private static final BlockingQueue<byte[]> audioDataQueue = new LinkedBlockingQueue<>();
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static final ConcurrentHashMap<Socket, Boolean> activeClients = new ConcurrentHashMap<>();

    public static boolean init() {
        SocketIO.init();
        Log.info("Server is initializing...");
        threadPool.execute(new Collect());
        threadPool.execute(new Sender());

        return true;
    }

    static class Collect implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(Config.port)) {
                Log.info("Collecting audio data...");

                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        Log.info("Client connected: " + clientSocket.getRemoteSocketAddress());
                        threadPool.execute(() -> handleClient(clientSocket));
                    } catch (Exception e) {
                        Log.error("Error accepting client connection: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.error("Error in Collect: " + e.getMessage());
            }
        }

        private void handleClient(Socket clientSocket) {
            try {
                activeClients.put(clientSocket, true);
                Log.info("Active clients: " + getActiveClientCount());
                try (InputStream inputStream = clientSocket.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byte[] audioData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                        if (audioDataQueue.offer(audioData)) {
                            Log.info("Received audio data from " +
                                    clientSocket.getRemoteSocketAddress() +
                                    " of size: " + bytesRead);
                        }
                    }
                }
            } catch (Exception e) {
                Log.error("Error handling client " +
                        clientSocket.getRemoteSocketAddress() +
                        ": " + e.getMessage());
            } finally {
                activeClients.remove(clientSocket);
                try {
                    clientSocket.close();
                    Log.info("Client disconnected: " + clientSocket.getRemoteSocketAddress());
                } catch (Exception e) {
                    Log.error("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    static class Sender implements Runnable {
        @Override
        public void run() {
            try (Socket socket = new Socket(Config.esp32Ip, Config.esp32Port)) {
                Log.info("Sending audio data...");
                OutputStream outputStream = socket.getOutputStream();

                while (true) {
                    byte[] audioData = audioDataQueue.take();
                    outputStream.write(audioData);
                    outputStream.flush();
                    Log.info("Sent audio data");
                }
            } catch (Exception e) {
                Log.error("Error in Sender: " + e.getMessage());
            }
        }
    }

    public static int getActiveClientCount() {
        return activeClients.size();
    }

    public static Set<Socket> getActiveClients() {
        return activeClients.keySet();
    }
}
