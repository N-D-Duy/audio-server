package com.duynguyen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Config {
    public static int port;
    public static String esp32Ip;
    public static int esp32Port;
    public static int socketPort;
    public static String socketHost;


    public static boolean load() {
        //read config from file config.properties
        try (FileInputStream input = new FileInputStream("config.properties");) {
            Properties prop = new Properties();
            prop.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            port = Integer.parseInt(prop.getProperty("server.port"));
            esp32Ip = prop.getProperty("esp32.ip");
            esp32Port = Integer.parseInt(prop.getProperty("esp32.port"));
            socketPort = Integer.parseInt(prop.getProperty("websocket.port"));
            socketHost = prop.getProperty("websocket.host");

            for (String key : prop.stringPropertyNames()) {
                Log.info(key + ": " + prop.getProperty(key));
            }
            return true;
        } catch (IOException ex) {
            Log.error("Error loading config: " + ex.getMessage());
            return false;
        }
    }
}
