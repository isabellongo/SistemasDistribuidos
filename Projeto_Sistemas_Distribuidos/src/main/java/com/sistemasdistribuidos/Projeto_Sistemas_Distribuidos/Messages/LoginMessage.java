package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos.Messages;

import com.google.gson.annotations.SerializedName;

public class LoginMessage {

	@SerializedName("operacao")
    private String operacao;

    @SerializedName("usuario")
    private String usuario;

    @SerializedName("senha")
    private String senha;

    public LoginMessage(String operacao, String usuario, String senha) {
        this.operacao = operacao;
        this.usuario = usuario;
        this.senha = senha;
    }

    public String getOperacao() {
        return operacao;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }
}
