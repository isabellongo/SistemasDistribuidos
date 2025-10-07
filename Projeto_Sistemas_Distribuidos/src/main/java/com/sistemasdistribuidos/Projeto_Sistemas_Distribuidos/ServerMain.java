package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;


public class ServerMain {
    public static void main(String[] args) {
        ServerModel model = new ServerModel();
        ServerView view = new ServerView();
        ServerController controller = new ServerController(model, view);

        controller.start();
    }
}