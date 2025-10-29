package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.*;
import java.net.Socket;
import io.jsonwebtoken.Claims; 

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerController {

    // ... (construtor e start() iguais) ...
    private final ServerModel model;
    private final ServerView view;
    private final ServerDataModel dataModel;
    private volatile boolean running = true;

    public ServerController(ServerModel model, ServerView view) {
        this.model = model;
        this.view = view;
        this.dataModel = new ServerDataModel(); 
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
                new Thread(new ClientHandler(clientSocket, view, dataModel)).start();
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
        private final ServerDataModel dataModel;
        
        private final JwtService jwtService = new JwtService();

        // Construtor
        public ClientHandler(Socket socket, ServerView view, ServerDataModel dataModel) {
            this.socket = socket;
            this.view = view;
            this.dataModel = dataModel;
        }

        // --- Métodos de Resposta JSON (MODIFICADOS) ---

        /** Envia uma resposta JSON padrão e LOGA no console do servidor */
        private void sendResponse(PrintWriter out, String status, String mensagem) {
            JsonObject response = new JsonObject();
            response.addProperty("status", status);
            response.addProperty("mensagem", mensagem);
            
            // --- MODIFICAÇÃO AQUI ---
            String jsonResponse = response.toString();
            view.showMessage("[Servidor -> Cliente " + socket.getInetAddress() + "]: " + jsonResponse);
            out.println(jsonResponse);
            // --- FIM DA MODIFICAÇÃO ---
        }

        /** Envia uma resposta de sucesso no Login e LOGA no console do servidor */
        private void sendLoginSuccess(PrintWriter out, String token) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "200");
            response.addProperty("mensagem", "Sucesso: operação realizada com sucesso");
            response.addProperty("token", token);
            
            // --- MODIFICAÇÃO AQUI ---
            String jsonResponse = response.toString();
            view.showMessage("[Servidor -> Cliente " + socket.getInetAddress() + "]: " + jsonResponse);
            out.println(jsonResponse);
            // --- FIM DA MODIFICAÇÃO ---
        }
        
        /** Envia resposta de sucesso para LISTAR e LOGA no console do servidor */
        private void sendListarSuccess(PrintWriter out, String usuario) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "200");
            response.addProperty("mensagem", "Sucesso: operação realizada com sucesso");
            response.addProperty("usuario", usuario); 
            
            // --- MODIFICAÇÃO AQUI ---
            String jsonResponse = response.toString();
            view.showMessage("[Servidor -> Cliente " + socket.getInetAddress() + "]: " + jsonResponse);
            out.println(jsonResponse);
            // --- FIM DA MODIFICAÇÃO ---
        }

        // --- Validação (Sem alterações) ---
        
        private Claims validateTokenAndGetClaims(JsonObject jsonObject, PrintWriter out) {
            if (!jsonObject.has("token")) {
                sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (token)");
                return null;
            }
            String token = jsonObject.get("token").getAsString();
            
            Claims claims = jwtService.validateAndGetClaims(token);
            
            if (claims == null) {
                sendResponse(out, "401", "Erro: Token inválido (expirado ou assinatura incorreta)");
                return null;
            }
            return claims;
        }

        // --- Handlers para cada Operação (Sem alterações) ---

        private void handleLogin(JsonObject jsonObject, PrintWriter out) {
            try {
                if (!jsonObject.has("usuario") || !jsonObject.has("senha")) {
                    sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (usuario, senha)");
                    return;
                }
                String usuario = jsonObject.get("usuario").getAsString();
                String senha = jsonObject.get("senha").getAsString();
                
                UserData userData = dataModel.login(usuario, senha);
                
                if (userData != null) {
                    String token = jwtService.generateToken(userData.id(), userData.usuario(), userData.funcao());
                    sendLoginSuccess(out, token);
                } else {
                    sendResponse(out, "403", "Erro: sem permissão (usuário ou senha inválidos)");
                }
            } catch (IOException e) {
                sendResponse(out, "500", "Erro: Falha interna do servidor (IO)");
                view.showMessage("Erro ao ler DB em login: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas");
            }
        }
        
        private void handleCriarUsuario(JsonObject jsonObject, PrintWriter out) {
             try {
                if (!jsonObject.has("usuario")) { sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (objeto 'usuario')"); return; }
                JsonObject usuarioObj = jsonObject.getAsJsonObject("usuario");
                if (!usuarioObj.has("nome") || !usuarioObj.has("senha")) { sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (nome, senha)"); return; }
                
                String usuario = usuarioObj.get("nome").getAsString();
                String senha = usuarioObj.get("senha").getAsString();
                
                if (usuario.length() < 3 || senha.length() < 3) {
                    sendResponse(out, "405", "Erro: Campos inválidos, verifique o tipo e quantidade de caracteres (min 3)");
                    return;
                }
                
                String resultado = dataModel.cadastrar(usuario, senha);
                
                if (resultado.equals("sucesso")) {
                    sendResponse(out, "201", "Sucesso: Recurso cadastrado");
                } else if (resultado.equals("erro: usuario ja existe")) {
                    sendResponse(out, "409", "Erro: Recurso ja existe");
                } else {
                    sendResponse(out, "500", "Erro: Falha interna do servidor (cadastro falhou)");
                }
            } catch (IOException e) {
                sendResponse(out, "500", "Erro: Falha interna do servidor (IO)");
                view.showMessage("Erro ao escrever DB em cadastro: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(out, "405", "Erro: Campos inválidos, verifique o tipo");
            }
        }
        
        private void handleListarProprioUsuario(JsonObject jsonObject, PrintWriter out) {
            Claims claims = validateTokenAndGetClaims(jsonObject, out);
            if (claims == null) {
                return;
            }
            
            String usuarioDoToken = claims.get("usuario", String.class);
            
            if (usuarioDoToken == null) {
                sendResponse(out, "403", "Erro: sem permissão (token mal formado)");
                return;
            }
            
            sendListarSuccess(out, usuarioDoToken);
        }
        
        private void handleEditarProprioUsuario(JsonObject jsonObject, PrintWriter out) {
            Claims claims = validateTokenAndGetClaims(jsonObject, out);
            if (claims == null) {
                return; 
            }
            
            try {
                if (!jsonObject.has("usuario") || !jsonObject.getAsJsonObject("usuario").has("senha")) {
                    sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (usuario.senha)");
                    return;
                }
                
                String novaSenha = jsonObject.getAsJsonObject("usuario").get("senha").getAsString();
                
                if (novaSenha.length() < 3) {
                     sendResponse(out, "405", "Erro: Campos inválidos, verifique o tipo e quantidade de caracteres (min 3)");
                    return;
                }
                
                String usuarioDoToken = claims.get("usuario", String.class);
                
                String resultado = dataModel.atualizarDados(usuarioDoToken, usuarioDoToken, novaSenha);

                if (resultado.equals("sucesso")) {
                    sendResponse(out, "200", "Sucesso: operação realizada com sucesso");
                } else {
                    sendResponse(out, "404", "Erro: Recurso inexistente (usuário não encontrado no DB)");
                }
            } catch (IOException e) {
                sendResponse(out, "500", "Erro: Falha interna do servidor (IO)");
                view.showMessage("Erro ao escrever DB em atualizar: " + e.getMessage());
            } catch (Exception e) {
                sendResponse(out, "405", "Erro: Campos inválidos, verifique o tipo");
            }
        }
        
        private void handleExcluirProprioUsuario(JsonObject jsonObject, PrintWriter out) {
            Claims claims = validateTokenAndGetClaims(jsonObject, out);
            if (claims == null) {
                return;
            }
            
            try {
                String usuarioDoToken = claims.get("usuario", String.class);
                
                String resultado = dataModel.apagarDados(usuarioDoToken);
                
                if (resultado.equals("sucesso")) {
                    sendResponse(out, "200", "Sucesso: operação realizada com sucesso");
                } else {
                    sendResponse(out, "404", "Erro: Recurso inexistente (usuário não encontrado no DB)");
                }
            } catch (IOException e) {
                sendResponse(out, "500", "Erro: Falha interna do servidor (IO)");
                view.showMessage("Erro ao escrever DB em apagar: " + e.getMessage());
            }
        }
        
        private void handleLogout(JsonObject jsonObject, PrintWriter out) {
            
            sendResponse(out, "200", "Sucesso: Operação realizada com sucesso");
            view.showMessage("Cliente " + socket.getInetAddress() + " fez logout.");
        }

        // --- Método Run Principal (Sem alterações) ---
        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Log da mensagem RECEBIDA (já existia)
                    view.showMessage("[Cliente " + socket.getInetAddress() + "]: " + inputLine);
                    
                    try {
                        JsonElement elemento = JsonParser.parseString(inputLine);
                        JsonObject jsonObject = elemento.getAsJsonObject();
                        
                        if (!jsonObject.has("operacao")) {
                             sendResponse(out, "422", "Erro: Chaves faltantes ou invalidas (operacao)");
                             continue;
                        }
                        
                        String operacao = jsonObject.get("operacao").getAsString();
                        
                        switch (operacao.toUpperCase()) {
                            case "LOGIN":
                                handleLogin(jsonObject, out);
                                break;
                            case "CRIAR_USUARIO":
                                handleCriarUsuario(jsonObject, out);
                                break;
                            case "LISTAR_PROPRIO_USUARIO":
                                handleListarProprioUsuario(jsonObject, out);
                                break;
                            case "EDITAR_PROPRIO_USUARIO":
                                handleEditarProprioUsuario(jsonObject, out);
                                break;
                            case "EXCLUIR_PROPRIO_USUARIO":
                                handleExcluirProprioUsuario(jsonObject, out);
                                break;
                            case "LOGOUT":
                                handleLogout(jsonObject, out);
                                break;
                            default:
                                sendResponse(out, "400", "Erro: Operação não encontrada ou inválida");
                                break;
                        }
                        
                    } catch (Throwable t) { 
                        view.showMessage("--- ERRO FATAL NO HANDLER ---");
                        view.showMessage("CLASSE: " + t.getClass().getName());
                        view.showMessage("MENSAGEM: " + t.getMessage());
                        t.printStackTrace(System.err);
                        view.showMessage("------------------------------");
                        if (!socket.isOutputShutdown()) {
                            // Esta resposta também será logada pelo sendResponse
                            sendResponse(out, "500", "Erro: Falha interna do servidor (processamento JSON)");
                        }
                    }
                }
            } catch (IOException e) {
                // Cliente desconectou
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