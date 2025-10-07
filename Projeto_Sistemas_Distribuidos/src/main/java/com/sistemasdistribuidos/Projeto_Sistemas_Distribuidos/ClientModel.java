package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import com.google.gson.Gson;
import java.net.*;
import java.io.*;

@SuppressWarnings("unused")
public class ClientModel {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void connect(String serverIP, int serverPort) throws IOException {
        socket = new Socket(serverIP, serverPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null && !socket.isClosed()) socket.close();
    }
}    

