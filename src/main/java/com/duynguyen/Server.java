package com.duynguyen;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.*;

public class Server {
    public static final BlockingQueue<byte[]> audioDataQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<byte[]> esp32Buffer = new LinkedBlockingQueue<>(1000);
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    public static final ConcurrentHashMap<Socket, Boolean> activeClients = new ConcurrentHashMap<>();

    private static volatile Socket esp32Socket;
    private static ServerSocket tcpServer;

    public static boolean init() {
        try {
            AudioWebSocketServer wsServer = new AudioWebSocketServer(Config.socketPort);
            wsServer.start();

            tcpServer = new ServerSocket(Config.port);
            Log.info("Server started on port " + Config.port);
            threadPool.execute(new TCPConnectionHandler());

            threadPool.execute(new ESP32DataSender());
            threadPool.execute(new DataProcessor());

            return true;
        } catch (Exception e) {
            Log.error("Error initializing server: " + e.getMessage());
            return false;
        }
    }

    static class TCPConnectionHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = tcpServer.accept();
                    threadPool.execute(() -> handleClient(socket));
                } catch (Exception e) {
                    Log.error("Error accepting connection: " + e.getMessage());
                }
            }
        }

        private void handleClient(Socket socket) {
            try {
                InputStream input = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int read = input.read(buffer);
                if (read == -1) {
                    socket.close();
                    return;
                }

                String clientType = new String(buffer, 0, read).trim();

                if ("ESP32".equalsIgnoreCase(clientType)) {
                    handleESP32Client(socket);
                } else if ("APPLICATION".equalsIgnoreCase(clientType)) {
                    handleApplicationClient(socket);
                } else {
                    Log.warn("Unknown client type: " + clientType);
                    socket.close();
                }
            } catch (Exception e) {
                Log.error("Error handling client: " + e.getMessage());
            }
        }

        private void handleESP32Client(Socket socket) {
            try {
                if (esp32Socket != null) {
                    esp32Socket.close();
                }
                audioDataQueue.clear();
                esp32Socket = socket;
                Log.info("ESP32 connected: " + socket.getRemoteSocketAddress());
                activeClients.put(socket, true);
            } catch (Exception e) {
                Log.error("Error handling ESP32 client: " + e.getMessage());
            }
        }

        private void handleApplicationClient(Socket socket) {
            try {
                Log.info("Application client connected: " + socket.getRemoteSocketAddress());
                activeClients.put(socket, true);

                try (DataInputStream dataInput = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                    byte[] rawBuffer = new byte[4096];

                    while (true) {
                        try {
                            int totalBytesRead = 0;
                            while (totalBytesRead < rawBuffer.length) {
                                int bytesRead = dataInput.read(rawBuffer, totalBytesRead, rawBuffer.length - totalBytesRead);
                                if (bytesRead == -1) throw new EOFException();
                                totalBytesRead += bytesRead;
                            }
                            byte[] audioData = Arrays.copyOf(rawBuffer, totalBytesRead);
                            audioDataQueue.offer(audioData);
                        } catch (EOFException e) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.error("Error handling Application client: " + e.getMessage());
            } finally {
                activeClients.remove(socket);
                try {
                    socket.close();
                    Log.info("Client disconnected: " + socket.getRemoteSocketAddress());
                } catch (Exception e) {
                    Log.error("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    static class DataProcessor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    byte[] audioData = audioDataQueue.take();
                    while (!esp32Buffer.offer(audioData)) {
                        esp32Buffer.poll();
                    }
                } catch (Exception e) {
                    Log.error("Error processing audio data: " + e.getMessage());
                }
            }
        }
    }

    static class ESP32DataSender implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    if (esp32Socket != null && !esp32Socket.isClosed()) {
                        byte[] audioData = esp32Buffer.take();
                        Log.info("Sent: " + Arrays.toString(audioData));
                        DataOutputStream output = new DataOutputStream(esp32Socket.getOutputStream());
                        output.write(audioData);
                        output.flush();
                    } else {
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    Log.error("Error sending data to ESP32: " + e.getMessage());
                    if (esp32Socket != null) {
                        try {
                            esp32Socket.close();
                        } catch (Exception ex) {
                            Log.error("Error closing failed ESP32 connection");
                        }
                        esp32Socket = null;
                    }
                }
            }
        }
    }
}
