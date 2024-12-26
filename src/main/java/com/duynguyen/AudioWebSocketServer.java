package com.duynguyen;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class AudioWebSocketServer extends WebSocketServer {

    public AudioWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.info("Web client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.info("Web client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        try {
            Log.info("Received binary message: " + message.remaining() + " bytes");

            byte[] bytes = new byte[message.remaining()];
            message.get(bytes);

            Server.audioDataQueue.offer(bytes);
            Log.info("Processed " + bytes.length + " bytes");
        } catch (Exception e) {
            Log.error("Error processing binary message: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.warn("Received text message but audio data should be binary");
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
