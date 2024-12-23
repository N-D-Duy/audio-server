package com.duynguyen;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import static com.duynguyen.Server.audioDataQueue;

public class AudioWebSocketServer extends WebSocketServer {

    public AudioWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.info("New WebSocket connection: " + conn.getRemoteSocketAddress());
        Log.info("Active clients: " + getConnections().size());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.info("WebSocket closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.info("Received message: " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        byte[] audioData = new byte[message.remaining()];
        message.get(audioData);
        audioDataQueue.offer(audioData);
        Log.info("Received audio data: " + audioData.length + " bytes");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.error("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        Log.info("WebSocket server started on port " + getPort());
    }
}
