package com.tcc.admin.ticketbox;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_PERMISSION = 0;
    private static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED )
            solicitarPermissao();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //verificamos o retorno da permissão de uso da camera
        if (requestCode == 12345) {
            //caso tenha permitido o uso, chamamos o método que chama a camera
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão OK.", Toast.LENGTH_SHORT).show();
            } else {
                finishAffinity();
                System.exit(0);
            }
        }
    }

    public void solicitarPermissao(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Permissão Câmera");
        alert.setMessage("Este aplicativo necessita de acesso à Câmera.");
        alert.setPositiveButton("Permitir", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id)
            {
                permissao();
            }
        });
        alert.setNegativeButton("Sair", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                finishAffinity();
                System.exit(0);
            }
        });

        alert.show();

    }

    public boolean permissao(){

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 12345);

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED )
            return true;
        else
            return false;

    }

    public void abrirComprovantes(View v){
        Intent intent = new Intent(this, ActivityListaComprovantes.class);
        startActivity(intent);
    }

    public void abrirConfiguracoesHorarios(View v){
        Intent intent = new Intent(this, ConfigurarHorario.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_configuracoes_horarios) {
            Intent intent = new Intent(this, ConfigurarHorario.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_relatorio) {
            Intent intent = new Intent(this, ActivityRelatorio.class);
            startActivity(intent);
            return true;
        }


        if (id == R.id.action_manual) {

            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("pdf");
            } catch (IOException e) {
                Log.e("tag", "Failed to get asset file list.", e);
            }
            if (files != null) for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open("pdf/" + filename);
                    File outFile = new File(getExternalStorageDirectory() + "/TicketBox/", filename);
                    //File outFile = new File(getExternalFilesDir(null), filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch(IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }

            String targetPdf = getExternalStorageDirectory() + "/TicketBox/Manual-TicketBox.pdf";
            File arquivoPdf = new File(targetPdf);

            if(arquivoPdf.exists())
                Log.i(TAG, "TicketBox: ARQUIVO PDF EXISTE." + arquivoPdf.getAbsolutePath());
            else
                Log.i(TAG, "TicketBox: ARQUIVO PDF NÃO EXISTE.");

            arquivoPdf = new File(arquivoPdf.getAbsolutePath());

            Uri path = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName(), arquivoPdf);

            // Setting the intent for pdf reader
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Não foi possível abrir o Manual: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return true;

        }

        if (id == R.id.action_sobre) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Sobre");
            alert.setIcon(R.drawable.ticketbox);
            alert.setMessage("TicketBox" +
                    "\n Versão 1.0" +
                    "\n Este aplicativo foi desenvolvido por: " +
                    "\n José Francisco Brandão de Sousa" +
                    "\n Guilherme Feitoza de Sousa Lima" +
                    "\n _______________________________" +
                    "\n  Contato: jfbransou@gmail.com");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id)
                {
                }
            });

            alert.show();

            return true;

        }

        if (id == R.id.action_sair) {
            finishAffinity();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

}
