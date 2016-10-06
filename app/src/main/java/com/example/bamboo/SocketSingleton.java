package com.example.bamboo.pandatalk;


import java.net.Socket;

public class SocketSingleton {
    private static Socket socket = null;

    public static void setSocket(Socket newSocket) {
        SocketSingleton.socket = newSocket;
    }

    public static Socket getSocket() {
        return SocketSingleton.socket;
    }
}
