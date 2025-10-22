package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.IOException;

import com.google.gson.Gson;
import com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos.Messages.*;

public class ClientController {

    private final ClientModel model;
    private final ClientView view;

    public ClientController(ClientModel model, ClientView view) {
        this.model = model;
        this.view = view;
    }

    public void start() {
        try {
            String serverIP = view.ask("Qual o IP do servidor? ");
            int serverPort = Integer.parseInt(view.ask("Qual a Porta do servidor? "));
            view.showMessage("Tentando conectar com host " + serverIP + " na porta " + serverPort);

            model.connect(serverIP, serverPort);
            view.showMessage("Conectado. Digite (\"bye\" para sair)");

            String userInput;
            while (true) {
                userInput = view.ask("Digite: ");
                String[] partes = userInput.split(" ");
                
                if (userInput.equalsIgnoreCase("bye"))
                    break;
                
                switch (partes[0].toUpperCase()) {
                
                	case "LOGIN":
                		String operacao = partes[0].toUpperCase();
                        String usuario = partes[1];
                        String senha = partes[2];
                        
                        LoginMessage mensagem = new LoginMessage(operacao, usuario, senha);
                        Gson gson = new Gson();
                        String jsonOutput = gson.toJson(mensagem);
                        System.out.println(jsonOutput);
                        model.sendMessage(jsonOutput);
                        break;
                	default:
                        System.err.println("Operação desconhecida recebida: " + partes[0]);
                        break;
                }


                String response = model.receiveMessage();
                view.showMessage("Servidor retornou: " + response);
            }

            model.close();
            view.close();
            view.showMessage("Conexão encerrada.");

        } catch (IOException e) {
            view.showMessage("Erro de comunicação: " + e.getMessage());
        } catch (NumberFormatException e) {
            view.showMessage("Porta inválida!");
        }
    }
}