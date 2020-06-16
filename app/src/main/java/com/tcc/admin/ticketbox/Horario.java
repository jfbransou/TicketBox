package com.tcc.admin.ticketbox;

import java.util.Collection;

/**
 * Created by jfbransou on 20/03/2017.
 */

public class Horario {

    private int _id;

    private String descricao;

    private Collection<Comprovante> comprovantes;

    public Horario(){}

    public Horario( int _id, String descricao)
    {
        this._id = _id;
        this.descricao = descricao;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Collection<Comprovante> getComprovantes() {
        return comprovantes;
    }

    public void setComprovantes(Collection<Comprovante> comprovantes) {
        this.comprovantes = comprovantes;
    }

}
