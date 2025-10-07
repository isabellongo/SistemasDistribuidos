package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientView {

    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public String ask(String message) throws IOException {
        System.out.print(message);
        return br.readLine();
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void close() throws IOException {
        br.close();
    }
}