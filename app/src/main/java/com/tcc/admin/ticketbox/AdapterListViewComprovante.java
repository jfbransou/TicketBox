package com.tcc.admin.ticketbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by admin on 26/03/2017.
 */

public class AdapterListViewComprovante extends BaseAdapter{

    private LayoutInflater mInflater;
    private ArrayList<Comprovante> itens;
    DaoHorario daoHorario;

    public AdapterListViewComprovante(Context context, ArrayList<Comprovante> itens)
    {
        //Itens que preencheram o listview
        this.itens = itens;
        //responsavel por pegar o Layout do item.
        mInflater = LayoutInflater.from(context);
    }


    public int getCount() {
        return itens.size();
    }

    /**
     * Retorna o item de acordo com a posicao dele na tela.
     *
     */
    public Comprovante getItem(int position)
    {
        return itens.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        Comprovante comprovante;
        TextView textHorario;
        TextView data_hora_registro;
        ImageView fotoComprovante;

        view = mInflater.inflate(R.layout.itens_lista_comprovantes, null);
        comprovante = itens.get(position);
        textHorario = (TextView)view.findViewById(R.id.textHorario);
        data_hora_registro = (TextView)view.findViewById(R.id.data_hora_registro);
        fotoComprovante = (ImageView)view.findViewById(R.id.fotoComprovante);

        textHorario.setText(String.valueOf(comprovante.get_id()));

        if (comprovante.getData_hora_registro() != null){
            SimpleDateFormat formato1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formato2 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date dataHora = null;
            try {
                dataHora = formato1.parse(comprovante.getData_hora_registro());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            data_hora_registro.setText("Data e Hora: " + formato2.format(dataHora));
            data_hora_registro.setTextColor(Color.rgb(0,100,0));
        }

        String arquivoComprovante = comprovante.getComprovante();

        if(arquivoComprovante!=null){
            File imgFile = new  File(getExternalStorageDirectory() + "/TicketBox/"+arquivoComprovante);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                fotoComprovante.setImageBitmap(myBitmap);
            }
        }

        int id_horario = 0;
        id_horario = comprovante.getHorario().get_id();

        if(id_horario!=0){
            daoHorario = new DaoHorario(mInflater.getContext());
            daoHorario.abrir();
            textHorario.setText(daoHorario.buscarHorario(id_horario).getDescricao());
        }

        return view;
    }

}
