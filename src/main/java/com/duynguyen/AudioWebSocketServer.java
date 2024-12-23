package com.duynguyen;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioWebSocketServer extends WebSocketServer {
    private static final int BUFFER_SIZE = 4096;
    private final Map<WebSocket, BlockingQueue<byte[]>> clientBuffers = new ConcurrentHashMap<>();

    public AudioWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.info("Web client connected: " + conn.getRemoteSocketAddress());
        clientBuffers.put(conn, new LinkedBlockingQueue<>(BUFFER_SIZE));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.info("Web client disconnected: " + conn.getRemoteSocketAddress());
        clientBuffers.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        byte[] audioData = new byte[message.remaining()];
        message.get(audioData);
        Server.audioDataQueue.offer(audioData);

        clientBuffers.forEach((client, buffer) -> {
            if (client != conn && !buffer.offer(audioData)) {
                buffer.poll();
                buffer.offer(audioData);
            }
        });
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.info("Received text message: " + message);
        Server.audioDataQueue.offer(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.error("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        Log.info("WebSocket server started on port " + Config.socketPort);
    }
}