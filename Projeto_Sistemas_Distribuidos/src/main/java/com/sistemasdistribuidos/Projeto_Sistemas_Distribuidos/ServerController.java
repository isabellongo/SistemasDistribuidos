package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.*;
import java.net.Socket;

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

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    view.showMessage("[Cliente " + socket.getInetAddress() + "]: " + inputLine);
                    if (inputLine.equalsIgnoreCase("bye")) {
                        out.println("Conexão encerrada pelo servidor. Tchau!");
                        break;
                    }
                    // Resposta automática ou personalizada:
                    out.println(inputLine.toUpperCase());
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