package com.tcc.admin.ticketbox;

/**
 * Created by jfbransou on 20/03/2017.
 */
public class Comprovante {

    private int _id;

    private String dia;

    private Horario horario;

    private String data_hora_registro;
    private String comprovante;

    public Comprovante(){}


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public String getData_hora_registro() {
        return data_hora_registro;
    }

    public void setData_hora_registro(String data_hora_registro) {
        this.data_hora_registro = data_hora_registro;
    }

    public String getComprovante() {
        return comprovante;
    }

    public void setComprovante(String comprovante) {
        this.comprovante = comprovante;
    }
}