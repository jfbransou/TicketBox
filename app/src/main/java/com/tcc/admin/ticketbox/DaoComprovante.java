package com.tcc.admin.ticketbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by admin on 23/02/2017.
 */

public class DaoComprovante {

    private static final String NOME_DO_BANCO = "TicketBox.db";
    private SQLiteDatabase banco;
    private BancoListaOpenHelper bancoOpenHelper;
    private Context context;

    public DaoComprovante(Context context) {
        this.context = context;
        bancoOpenHelper = new BancoListaOpenHelper(context, NOME_DO_BANCO,null, 1);
    }

    public void abrir() throws SQLException {
        banco = bancoOpenHelper.getWritableDatabase();
    }

    public void fechar() {
        if (banco != null)
            banco.close();
    }

    public void inserirComprovante(Comprovante comprovante) {
        ContentValues novoComprovante = new ContentValues();
        novoComprovante.put("dia", String.valueOf(comprovante.getDia()));
        novoComprovante.put("id_horario", comprovante.getHorario().get_id());
        novoComprovante.putNull("data_hora_registro");
        novoComprovante.put("comprovante", comprovante.getComprovante());
        banco.insert("comprovantes", null, novoComprovante);
        Cursor cursor = banco.rawQuery("select last_insert_rowid()", null);
        if (cursor.moveToFirst()){
            int _id = (int) cursor.getLong(0);
            comprovante.set_id(_id);
        }
    }

    public void alterarComprovante(long id, String data_hora_registro, String comprovante) {
        ContentValues comprovanteAlterado = new ContentValues();
        comprovanteAlterado.put("data_hora_registro",data_hora_registro);
        comprovanteAlterado.put("comprovante", comprovante);
        banco.update("comprovantes", comprovanteAlterado, "_id = " + id, null);
        Log.i(TAG, "Comprovante alterado. _id Comprovante: " + id);
    }

    public void removerComprovante(long id) {
        banco.delete("comprovantes", "_id = " + id, null);
    }

    public Cursor obterTodosOsComprovantes(String dia) {
        String[] campos =  {"_id","dia","id_horario","data_hora_registro","comprovante"};
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false,"comprovantes", campos, "dia = '" + dia +"'", null, null,"_id",null);
        Log.i(TAG, "Query executada em obterTodosOsComprovantes: " + sqlQry);
        Cursor cursor =  banco.query("comprovantes", campos, "dia = '" + dia +"'", null, null, null, "_id");
        return  cursor;
    }

    public Cursor obterTodosOsComprovantesComFoto(String dia) {
        String[] campos =  {"_id","dia","id_horario","data_hora_registro","comprovante"};
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false,"comprovantes", campos, "dia = '" + dia +"'", null, null,"_id",null);
        Log.i(TAG, "Query executada em obterTodosOsComprovantesComFoto: " + sqlQry);
        Cursor cursor =  banco.query("comprovantes", campos, "dia = '" + dia +"' AND comprovante IS NOT NULL", null, null, null, "_id");
        return  cursor;
    }

    public Comprovante buscarComprovante(int id_horario, String dia){
        Comprovante comprovante = new Comprovante();
        DaoHorario daoHorario = new DaoHorario(context);
        daoHorario.abrir();
        String[] campos =  {"_id","dia","id_horario","data_hora_registro","comprovante"};
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false,"comprovantes", campos, "id_horario = " + id_horario + " AND dia = '" + dia + "'" , null, null,"_id",null);
        Log.i(TAG, "Queey em buscarComprovante: "+ sqlQry);

        Cursor cursor = banco.query("comprovantes", campos, "id_horario = " + id_horario + " AND dia = '" + dia + "'" , null, null, null, "dia");
        if (cursor.getCount() == 1){
            cursor.moveToFirst();
            comprovante.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            comprovante.setHorario(daoHorario.buscarHorario(cursor.getInt(cursor.getColumnIndex("id_horario"))));
            comprovante.setDia(cursor.getString(cursor.getColumnIndex("dia")));
            comprovante.setData_hora_registro(cursor.getString(cursor.getColumnIndex("data_hora_registro")));
            comprovante.setComprovante(cursor.getString(cursor.getColumnIndex("comprovante")));
        }
        daoHorario.fechar();
        return comprovante;
    }

}
