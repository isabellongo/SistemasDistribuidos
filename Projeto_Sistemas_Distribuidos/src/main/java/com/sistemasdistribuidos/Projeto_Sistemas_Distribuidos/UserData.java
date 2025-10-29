package com.sistemasdistribuidos.Projeto_Sistemas_Distribuidos;


public final class UserData {

    private final String id;
    private final String usuario;
    private final String funcao;


    public UserData(String id, String usuario, String funcao) {
        this.id = id;
        this.usuario = usuario;
        this.funcao = funcao;
    }

    /**
     * Getter para o ID.
     */
    public String id() {
        return id;
    }

    /**
     * Getter para o nome de usuário.
     */
    public String usuario() {
        return usuario;
    }

    /**
     * Getter para a função (role).
     */
    public String funcao() {
        return funcao;
    }
    
    // NOTA: Os métodos chamam-se id(), usuario() e funcao()
    // para corresponder exatamente à forma como um 'record'
    // seria chamado, então você não precisa mudar mais nada
    // no ServerController ou ServerDataModel.
}