package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;

public class ClientMain {
	public static void main(String[] args) {
        ClientModel model = new ClientModel();
        ClientView view = new ClientView();
        ClientController controller = new ClientController(model, view);

        controller.start();
    }
}
