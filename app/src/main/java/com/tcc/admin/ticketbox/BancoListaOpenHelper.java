package com.tcc.admin.ticketbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 23/02/2017.
 */

public class BancoListaOpenHelper extends SQLiteOpenHelper {

    public BancoListaOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String criarTabelaHorarios = "CREATE TABLE horarios " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "descricao TEXT NOT NULL UNIQUE);";

        db.execSQL(criarTabelaHorarios);

        String criarTabelaComprovantes = "CREATE TABLE comprovantes " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "dia TEXT," +
                "id_horario INTEGER," +
                "data_hora_registro TEXT," +
                "comprovante TEXT,"+
                "unique(dia,id_horario),"+
                "FOREIGN KEY(id_horario) REFERENCES horarios(_id));";

        db.execSQL(criarTabelaComprovantes);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}