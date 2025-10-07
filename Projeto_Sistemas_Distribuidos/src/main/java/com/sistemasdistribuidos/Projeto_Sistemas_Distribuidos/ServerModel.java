package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import com.google.gson.Gson;
import java.net.*;
import java.io.*;

import java.io.*;
import java.net.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("unused")
public class ServerModel {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public Socket acceptClient() throws IOException {
        return serverSocket.accept();
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}