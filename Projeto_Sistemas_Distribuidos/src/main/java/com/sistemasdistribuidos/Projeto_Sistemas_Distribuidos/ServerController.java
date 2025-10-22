package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos.Messages.LoginMessage;
import com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos.Databases.*;

public class ServerController {

    private final ServerModel model;
    private final ServerView view;
    private volatile boolean running = true;

    public ServerController(ServerModel model, ServerView view) {
        this.model = model;
        this.view = view;
    }

    public void start() {
        try {
            int port = Integer.parseInt(view.ask("Informe a porta para o servidor: "));
            model.start(port);
            view.showMessage("Servidor iniciado na porta " + port + "...");
            view.showMessage("Aguardando conexões de clientes...");

            while (running) {
                Socket clientSocket = model.acceptClient();
                view.showMessage("Novo cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, view)).start();
            }

        } catch (IOException e) {
            view.showMessage("Erro de I/O: " + e.getMessage());
        } catch (NumberFormatException e) {
            view.showMessage("Porta inválida!");
        } finally {
            try {
                model.stop();
                view.close();
                view.showMessage("Servidor encerrado.");
            } catch (IOException e) {
                view.showMessage("Erro ao encerrar servidor: " + e.getMessage());
            }
        }
    }


    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final ServerView view;

        public ClientHandler(Socket socket, ServerView view) {
            this.socket = socket;
            this.view = view;
        }

        public void login(JsonObject jsonObject, PrintWriter out) {
            Gson gson = new Gson();
    		LoginMessage msgLogin = gson.fromJson(jsonObject, LoginMessage.class);
    		String usuario = msgLogin.getUsuario();
    		String senha = msgLogin.getSenha();
    		//logica para verificar se o usuario e a senha estao no documento txt
    		out.println(msgLogin.getOperacao() + msgLogin.getUsuario() + msgLogin.getSenha());
        }
        
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    view.showMessage("[Cliente " + socket.getInetAddress() + "]: " + inputLine);
                    JsonElement elemento = JsonParser.parseString(inputLine);
                    JsonObject jsonObject = elemento.getAsJsonObject();
                    String operacao = jsonObject.get("operacao").getAsString();
                    
                    switch (operacao.toUpperCase()) {
                    	case "LOGIN":
                    		login(jsonObject, out);
                        break;
                    	default:
                            System.err.println("Operação desconhecida recebida: " + operacao.toUpperCase());
                            break;
                    }
                   
                }
            } catch (IOException e) {
                view.showMessage("Erro com cliente " + socket.getInetAddress() + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    view.showMessage("Cliente desconectado: " + socket.getInetAddress());
                } catch (IOException e) {
                    view.showMessage("Erro ao fechar conexão do cliente: " + e.getMessage());
                }
            }
        }
    }
}