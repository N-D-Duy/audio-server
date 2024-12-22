package com.duynguyen;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketIO {

    public static Socket socket;
    public static boolean isInitialized;
    public static boolean connected;

    public static void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        reconnect(1);
    }

    public static void listen() {
    }

    public static void connect() {
        if (connected) {
            return;
        }
        try {
            socket = IO.socket(Config.socketHost + ":" + Config.socketPort);
            socket.connect();
            listen();
            connected = true;
            Log.info("Connect to socket server successfully!");
        } catch (Exception e) {
            Log.error("Can not connect to socket server", e);
            reconnect(10000);
        }
    }

    public static void reconnect(long time) {
        (new Thread(() -> {
            try {
                Thread.sleep(time);
            } catch (Exception ex) {
                Log.error("Reconnect error", ex);
            }
            connect();
        })).start();
    }

    public static void disconnect() {
        socket.disconnect();
    }

}


