package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientView {

    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Mostra o menu principal (pré-login) e retorna a escolha do usuário.
     */
    public int showPreLoginMenu() {
        System.out.println("\n--- BEM-VINDO ---");
        System.out.println("1. Login");
        System.out.println("2. Cadastrar novo usuário (CRIAR_USUARIO)");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
        try {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return -1; // Opção inválida
        }
    }

    /**
     * Mostra o menu de usuário logado e retorna a escolha.
     */
    public int showPostLoginMenu(String usuario) {
        System.out.println("\n--- MENU PRINCIPAL (Logado como: " + usuario + ") ---");
        // --- NOVA OPÇÃO ---
        System.out.println("1. Ver meus dados (LISTAR_PROPRIO_USUARIO)");
        System.out.println("2. Atualizar minha senha (EDITAR_PROPRIO_USUARIO)");
        System.out.println("3. Apagar minha conta (EXCLUIR_PROPRIO_USUARIO)");
        System.out.println("0. Logout (LOGOUT)");
        System.out.print("Escolha uma opção: ");
        try {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return -1; // Opção inválida
        }
    }

    public String ask(String message) throws IOException {
        System.out.print(message + ": ");
        return br.readLine();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void close() throws IOException {
        br.close();
    }
}