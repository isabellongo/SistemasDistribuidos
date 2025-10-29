package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class ServerDataModel {

    private static final Path DB_PATH = Paths.get("Clientes.txt");
    private static final Object FILE_LOCK = new Object(); 

    // ... (construtor continua igual) ...
    public ServerDataModel() {
        try {
            if (!Files.exists(DB_PATH)) {
                Files.createFile(DB_PATH);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao criar o arquivo de banco de dados: " + e.getMessage());
        }
    }

    /**
     * Tenta autenticar um usuário.
     * @return Um objeto UserData se o login for bem-sucedido, null caso contrário.
     */
    public UserData login(String usuario, String senha) throws IOException {
        synchronized (FILE_LOCK) {
            List<String> linhas = Files.readAllLines(DB_PATH, StandardCharsets.UTF_8);
            for (String linha : linhas) {
                String[] partes = linha.split(";");
                // Agora esperamos 4 partes: id;usuario;senha;funcao
                if (partes.length == 4 && partes[1].equals(usuario) && partes[2].equals(senha)) {
                    // Retorna os dados necessários para o JWT
                    return new UserData(partes[0], partes[1], partes[3]);
                }
            }
            return null; // Falha no login
        }
    }

    /**
     * Cadastra um novo usuário.
     */
    public String cadastrar(String usuario, String senha) throws IOException {
        synchronized (FILE_LOCK) {
            if (usuario.length() < 3 || senha.length() < 3) {
                return "erro: dados invalidos"; 
            }

            List<String> linhas = Files.readAllLines(DB_PATH, StandardCharsets.UTF_8);
            
            int maxId = 0;
            for (String linha : linhas) {
                String[] partes = linha.split(";");
                if (partes.length >= 1) {
                    // Checa se o usuário já existe
                    if (partes[1].equals(usuario)) {
                        return "erro: usuario ja existe";
                    }
                    // Encontra o ID máximo
                    try {
                        int id = Integer.parseInt(partes[0]);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        // ignora linha mal formatada
                    }
                }
            }
            
            int novoId = maxId + 1;
            String funcao = "user"; // Define a função padrão

            // Formato: id;usuario;senha;funcao
            String novaLinha = String.format("%d;%s;%s;%s%s", 
                novoId, usuario, senha, funcao, System.lineSeparator());
                
            Files.writeString(DB_PATH, novaLinha, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            return "sucesso";
        }
    }

    /**
     * Atualiza os dados de um usuário (apenas senha).
     * O 'usuarioAntigo' agora é usado para ENCONTRAR a linha.
     */
    public String atualizarDados(String usuarioAntigo, String novoUsuario, String novaSenha) throws IOException {
        synchronized (FILE_LOCK) {
            // A lógica de atualizar o nome de usuário não está no protocolo,
            // mas a validação da senha sim.
            if (novaSenha.length() < 3) {
                return "erro: dados invalidos"; 
            }
            
            List<String> linhas = Files.readAllLines(DB_PATH, StandardCharsets.UTF_8);
            boolean encontrado = false;
            List<String> novasLinhas = new java.util.ArrayList<>();
            
            for (String linha : linhas) {
                String[] partes = linha.split(";");
                // partes[1] é o 'usuario'
                if (partes.length == 4 && partes[1].equals(usuarioAntigo)) {
                    // Mantém id, usuario, funcao. Muda apenas a senha (partes[2])
                    novasLinhas.add(String.format("%s;%s;%s;%s", partes[0], partes[1], novaSenha, partes[3]));
                    encontrado = true;
                } else {
                    novasLinhas.add(linha);
                }
            }

            if (!encontrado) {
                return "erro: usuario nao encontrado para atualizar";
            }

            Files.write(DB_PATH, novasLinhas, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "sucesso";
        }
    }
    
    // ... (o método apagarDados(String usuario) continua igual, pois ele só
    // precisa do nome de usuário para filtrar a linha) ...
    
    public String apagarDados(String usuario) throws IOException {
        synchronized (FILE_LOCK) {
            List<String> linhas = Files.readAllLines(DB_PATH, StandardCharsets.UTF_8);
            
            // Filtra pelo nome de usuário (partes[1])
            List<String> novasLinhas = linhas.stream()
                .filter(linha -> {
                    String[] partes = linha.split(";");
                    return !(partes.length >= 2 && partes[1].equals(usuario));
                })
                .collect(Collectors.toList());

            if (novasLinhas.size() == linhas.size()) {
                return "erro: usuario nao encontrado para apagar";
            }

            Files.write(DB_PATH, novasLinhas, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "sucesso";
        }
    }
}