package com.duynguyen;

public class Main {
    public static void main(String[] args) {
        if (Config.load()) {
            if(Server.init()){
                Log.info("Server is running...");
            } else {
                Log.error("Server failed to start...");
            }
        } else {
            Log.error("Failed to load config...");
        }
    }
}