package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientController {

    private final ClientModel model;
    private final ClientView view;

    private String usuarioLogado = null;
    private String token = null; 
    private boolean running = true;

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
            view.showMessage("Conectado!");

            while (running) {
                if (token == null) { 
                    handlePreLoginMenu();
                } else {
                    handlePostLoginMenu();
                }
            }

        } catch (IOException e) {
            view.showMessage("Erro de comunicação: Conexão perdida com o servidor.");
        } catch (NumberFormatException e) {
            view.showMessage("Porta inválida!");
        } finally {
            try {
                model.close();
                view.close();
                view.showMessage("Conexão encerrada.");
            } catch (IOException e) {
                view.showMessage("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }

    private void handlePreLoginMenu() throws IOException {
        int choice = view.showPreLoginMenu();
        switch (choice) {
            case 1:
                doLogin();
                break;
            case 2:
                doCadastro();
                break;
            case 0:
                running = false;
                break;
            default:
                view.showMessage("Opção inválida.");
        }
    }

    private void handlePostLoginMenu() throws IOException {
        int choice = view.showPostLoginMenu(usuarioLogado);
        switch (choice) {
            // --- NOVOS CASES ---
            case 1: // Listar
                doListarProprioUsuario();
                break;
            case 2: // Atualizar
                doAtualizarDados();
                break;
            case 3: // Apagar
                doApagarDados();
                break;
            case 0: // Logout
                doLogout();
                break;
            default:
                view.showMessage("Opção inválida.");
        }
    }

    // --- Métodos de Operação ---

    /** Processa a resposta JSON genérica do servidor */
    private boolean processarResposta(String response) {
        if (response == null) {
            view.showMessage("Erro: O servidor não respondeu.");
            return false;
        }
        
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String status = jsonResponse.get("status").getAsString();
            String mensagem = jsonResponse.get("mensagem").getAsString();

            view.showMessage("[Servidor " + status + "]: " + mensagem);
            
            return status.equals("200") || status.equals("201");
            
        } catch (Exception e) {
            view.showMessage("Erro: Resposta mal formatada do servidor: " + response);
            return false;
        }
    }


    private void doLogin() throws IOException {
        String usuario = view.ask("Usuário");
        String senha = view.ask("Senha");

        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGIN");
        json.addProperty("usuario", usuario);
        json.addProperty("senha", senha);

        model.sendMessage(json.toString());
        String response = model.receiveMessage();
        
        if (response == null) {
             view.showMessage("Erro: O servidor não respondeu.");
             return;
        }
        
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String status = jsonResponse.get("status").getAsString();
            String mensagem = jsonResponse.get("mensagem").getAsString();
            view.showMessage("[Servidor " + status + "]: " + mensagem);

            if (status.equals("200")) {
                this.usuarioLogado = usuario;
                this.token = jsonResponse.get("token").getAsString(); 
                view.showMessage("Login realizado com sucesso!");
            }
        } catch (Exception e) {
            view.showMessage("Erro: Resposta mal formatada do servidor: " + response);
        }
    }

    private void doCadastro() throws IOException {
        String usuario = view.ask("Novo Usuário");
        String senha = view.ask("Nova Senha");

        // --- VALIDAÇÃO (CLIENT-SIDE) ---
        if (usuario.length() < 3 || senha.length() < 3) {
            view.showMessage("Erro: Usuário e senha devem ter no mínimo 3 caracteres.");
            return;
        }
        // --- FIM DA VALIDAÇÃO ---

        JsonObject json = new JsonObject();
        json.addProperty("operacao", "CRIAR_USUARIO");
        
        JsonObject usuarioObj = new JsonObject();
        usuarioObj.addProperty("nome", usuario);
        usuarioObj.addProperty("senha", senha);
        
        json.add("usuario", usuarioObj);

        model.sendMessage(json.toString());
        processarResposta(model.receiveMessage());
    }

    // --- NOVO MÉTODO ---
    private void doListarProprioUsuario() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LISTAR_PROPRIO_USUARIO");
        json.addProperty("token", this.token);
        
        model.sendMessage(json.toString());
        String response = model.receiveMessage();

        if (response == null) {
             view.showMessage("Erro: O servidor não respondeu.");
             return;
        }
        
        try {
            // Este método precisa de um processamento especial
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String status = jsonResponse.get("status").getAsString();
            String mensagem = jsonResponse.get("mensagem").getAsString();
            view.showMessage("[Servidor " + status + "]: " + mensagem);

            if (status.equals("200") && jsonResponse.has("usuario")) {
                String nomeUsuario = jsonResponse.get("usuario").getAsString();
                view.showMessage("--> Seus dados: " + nomeUsuario);
            }
        } catch (Exception e) {
            view.showMessage("Erro: Resposta mal formatada do servidor: " + response);
        }
    }

    private void doAtualizarDados() throws IOException {
        String novaSenha = view.ask("Nova senha");

        // --- VALIDAÇÃO (CLIENT-SIDE) ---
        if (novaSenha.length() < 3) {
            view.showMessage("Erro: A nova senha deve ter no mínimo 3 caracteres.");
            return;
        }
        // --- FIM DA VALIDAÇÃO ---

        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EDITAR_PROPRIO_USUARIO");
        json.addProperty("token", this.token); 
        
        JsonObject usuarioObj = new JsonObject();
        usuarioObj.addProperty("senha", novaSenha);
        
        json.add("usuario", usuarioObj);
        
        model.sendMessage(json.toString());
        processarResposta(model.receiveMessage());
    }
    
    private void doApagarDados() throws IOException {
        String confirm = view.ask("Tem certeza que quer apagar sua conta? (S/N)");
        if (!confirm.equalsIgnoreCase("S")) {
            view.showMessage("Operação cancelada.");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EXCLUIR_PROPRIO_USUARIO");
        json.addProperty("token", this.token); 
        
        model.sendMessage(json.toString());
        
        String response = model.receiveMessage();
        if (processarResposta(response)) {
             view.showMessage("Conta apagada com sucesso. Você será desconectado.");
             this.usuarioLogado = null; 
             this.token = null;
             this.running = false; // <-- LINHA ADICIONADA
        }
    }
    
    private void doLogout() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGOUT");
        json.addProperty("token", this.token); 
        
        model.sendMessage(json.toString());
        
        if (processarResposta(model.receiveMessage())) {
            this.usuarioLogado = null;
            this.token = null;
            view.showMessage("Logout realizado. Desconectando...");
            this.running = false; // <-- LINHA ADICIONADA
        }
    }
}