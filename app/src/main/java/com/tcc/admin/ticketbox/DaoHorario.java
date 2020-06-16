package com.tcc.admin.ticketbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.Collections;

/**
 * Created by admin on 23/02/2017.
 */

public class DaoHorario {

    private static final String NOME_DO_BANCO = "TicketBox.db";
    private SQLiteDatabase banco;
    private BancoListaOpenHelper bancoOpenHelper;

    public DaoHorario(Context context) {
        bancoOpenHelper = new BancoListaOpenHelper(context, NOME_DO_BANCO,null, 1);
    }

    public void abrir() throws SQLException {
        banco = bancoOpenHelper.getWritableDatabase();
    }

    public void fechar() {
        if (banco != null)
            banco.close();
    }

    public void inserirHorario(String descricao) {
        ContentValues novoHorario = new ContentValues();
        novoHorario.put("descricao", descricao);
        banco.insert("horarios", null, novoHorario);
    }

    public void inserirHorario(Horario horario) {
        ContentValues novoHorario = new ContentValues();
        novoHorario.put("descricao", horario.getDescricao());
        banco.insert("horarios", null, novoHorario);
        Cursor cursor = banco.rawQuery("select last_insert_rowid()", null);
        if (cursor.moveToFirst()){
            int _id = (int) cursor.getLong(0);
            horario.set_id(_id);
        }
    }

    public void alterarHorario(long id, String descricao) {
        ContentValues horarioAlterado = new ContentValues();
        horarioAlterado.put("descricao", descricao);
        banco.update("horarios", horarioAlterado, "_id = " + id, null);
    }

    public void removerHorario(long id) {
        banco.delete("horarios", "_id = " + id, null);
    }

    public Cursor obterTodosOsHorarios() {
        return banco.query("horarios", null, null, null,
                null, null, "_id");
    }

    public Cursor obterHorario(int _id) {
        String[] campos =  {"descricao"};
        return banco.query("horarios", campos, "WHERE _id = " + _id, null,
                null, null, "_id");
    }

    public Horario buscarHorario(int _id){

        Horario horario = new Horario();

        String[] campos =  {"_id","descricao"};
        Cursor cursor = banco.query("horarios", campos, "_id = " + _id, null,
                null, null, "_id");

        cursor.moveToFirst();
        horario.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
        horario.setDescricao(cursor.getString(cursor.getColumnIndex("descricao")));

        return horario;
    }

    public Cursor buscarHorariosPorHora(String[] _ids){
        String[] campos =  {"_id","descricao"};
        String WHERE = "_id IN (" + TextUtils.join(",", Collections.nCopies(_ids.length, "?")) + ")";
        Cursor cursor = banco.query("horarios", campos, WHERE, _ids, null, null, "SUBSTR('0' || descricao, -5,5)");
        return cursor;
    }

    public Horario buscarHorario(String descricao){
        Horario horario = new Horario();
        String[] campos =  {"_id","descricao"};
        Cursor cursor = banco.query("horarios", campos, "descricao = '" + descricao + "'", null, null, null, "descricao");
        if (cursor.getCount() == 1){
            cursor.moveToFirst();
            horario.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            horario.setDescricao(cursor.getString(cursor.getColumnIndex("descricao")));
        }
        return horario;
    }

}