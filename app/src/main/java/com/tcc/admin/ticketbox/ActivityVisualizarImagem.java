package com.tcc.admin.ticketbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by jfbransou on 28/03/2017.
 */

public class ActivityVisualizarImagem extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visualizar_comprovante);

        TextView titleTextView = (TextView) findViewById(R.id.textHorario);
        ImageView imageView = (ImageView) findViewById(R.id.fotoComprovante);

        String horario = getIntent().getStringExtra("horario");
        String arquivoComprovante = getIntent().getStringExtra("arquivoComprovante");

        if(arquivoComprovante!=null){
            File imgFile = new  File(getExternalStorageDirectory() + "/TicketBox/"+arquivoComprovante);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
                titleTextView.setText(horario);
                Toast.makeText(this, horario, Toast.LENGTH_LONG).show();
            }
        }
    }
}