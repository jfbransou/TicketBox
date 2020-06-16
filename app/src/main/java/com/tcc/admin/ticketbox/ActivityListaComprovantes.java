package com.tcc.admin.ticketbox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.os.Environment.getExternalStorageDirectory;

public class ActivityListaComprovantes extends Activity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public final String APP_TAG = "TicketBox";
    private String absolutePathFile;

    private ListView listView;
    private Button btnDia;
    private int dia, mes, ano;
    static final int DIALOG_ID = 0;

    private AdapterListViewComprovante adapterListViewComprovante;
    private ArrayList<Comprovante> itens;
    DaoComprovante daoComprovante;
    DaoHorario daoHorario;
    private int idComprovante;
    private String diaAtual;
    Uri imageUri;

    Comprovante comprovanteSelecionado;

    SimpleDateFormat formatador;

    static String dataComprovante = "";
    static String data_hora_registro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_comprovantes);

        listView = (ListView) findViewById(R.id.list);
        btnDia = (Button) findViewById(R.id.btnDia);

        //Define o Listener quando alguem clicar no item.
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        final Calendar calendario = Calendar.getInstance();

        btnDia.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendario.getTime()));

        formatador = new SimpleDateFormat("yyyy-MM-dd");
        diaAtual = converterDataParaString(calendario.getTime());

        dia = calendario.get(Calendar.DAY_OF_MONTH);
        mes = calendario.get(Calendar.MONTH);
        ano = calendario.get(Calendar.YEAR);

        showDialogOnButtonClick();
    }

    public void alterarBtnDia(View v){

        String dia = String.valueOf(btnDia.getText());  // Start date
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        final Calendar calendario = Calendar.getInstance();
        try {
            calendario.setTime(formatador.parse(dia));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (R.id.btnProximoDia == v.getId()){
            calendario.add(Calendar.DATE, 1);
        }else if(R.id.btnDiaAnterior == v.getId()){
            calendario.add(Calendar.DATE, -1);
        }

        dia = formatador.format(calendario.getTime());
        btnDia.setText(dia);
        diaAtual = converterDataParaString(calendario.getTime());

        Log.i(TAG, "alterarBtnDia chamou creatListView");
        createListView();
    }

    // esta funcao é chamada quando o usuario clica em "Adicionar Horário"
    public void abrirConfiguracoesHorarios(View v){
        Intent intent = new Intent(this, ConfigurarHorario.class);
        startActivity(intent);
    }

    public void showDialogOnButtonClick(){
        btnDia.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if(id == DIALOG_ID)
            return new DatePickerDialog(this,dpickerListener,ano,mes,dia);
        return null;
    }

    private OnDateSetListener dpickerListener
            = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            dia = dayOfMonth;
            mes = month;
            ano = year;

            final Calendar calendario = Calendar.getInstance();
            calendario.set(Calendar.YEAR, ano);
            calendario.set(Calendar.MONTH, mes);
            calendario.set(Calendar.DAY_OF_MONTH, dia);

            Date date;
            date = calendario.getTime();
            btnDia.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
            diaAtual = converterDataParaString(date);

            Log.i(TAG, "alterarBtnDia chamaou creatListView");
            createListView();
        }
    };

    private void createListView(){

        daoComprovante = new DaoComprovante(this);
        daoComprovante.abrir();

        //Criamos nossa lista que preencherá o ListView
        itens = new ArrayList<Comprovante>();

        Cursor cursor = daoComprovante.obterTodosOsComprovantes(diaAtual);
        Log.i(TAG, " >>>>>>>>>>>>>>>>>>>>>>>>>>>>> OBTENDO TODOS OS COMPROVANTES. Total = " + cursor.getCount());
        if(((cursor != null) && (cursor.getCount() > 0))){
            itens = cursor2List(cursor);
            //Aqui criamos o adapter
            adapterListViewComprovante = new AdapterListViewComprovante(this, itens);
            //Aqui setamos o Adapter
            listView.setAdapter(adapterListViewComprovante);
            //Altera a cor quando a lista é selecionada
            listView.setCacheColorHint(Color.TRANSPARENT);
        }else{
            listView.setAdapter(null);
        }
        daoComprovante.fechar();
    }

    protected ArrayList<Comprovante> cursor2List(Cursor cursor){
        ArrayList<Comprovante> comprovantes = new ArrayList<>();
        DaoHorario daoHorario = new DaoHorario(this.getBaseContext());
        daoHorario.abrir();
        cursor.moveToFirst();
        do{
            Comprovante comprovante = new Comprovante();
            comprovante.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            comprovante.setDia(cursor.getString(cursor.getColumnIndex("dia")));
            comprovante.setHorario(daoHorario.buscarHorario(cursor.getInt(cursor.getColumnIndex("id_horario"))));
            comprovante.setData_hora_registro(cursor.getString(cursor.getColumnIndex("data_hora_registro")));
            comprovante.setComprovante(cursor.getString(cursor.getColumnIndex("comprovante")));
            comprovantes.add(comprovante);

        }while(cursor.moveToNext());
        daoHorario.fechar();
        comprovantes = ordenarComprovantesPorHora(comprovantes);
        return comprovantes;
    }

    // Para ordenar os Comprovantes por Horário
    public ArrayList<Comprovante> ordenarComprovantesPorHora(ArrayList<Comprovante> comprovantes) {

        Integer[] idsOrdenados = new Integer[comprovantes.size()];
        String[] ids =  new String[comprovantes.size()];
        ArrayList<Comprovante> comprovantesOrdenados = new ArrayList<>();

        for(int i=0; i < ids.length; i++){
            ids[i] = String.valueOf(comprovantes.get(i).getHorario().get_id());
        }

        daoHorario = new DaoHorario(this);
        daoHorario.abrir();
        Cursor cursor = daoHorario.buscarHorariosPorHora(ids);
        cursor.moveToFirst();
        int i = 0;
        do{
            idsOrdenados[i] = cursor.getInt(cursor.getColumnIndex("_id"));
            i++;
        }while(cursor.moveToNext());

        for (i=0; i < comprovantes.size(); i++) {
            for(int j=0; j < comprovantes.size(); j++){
                if(idsOrdenados[i] == comprovantes.get(j).getHorario().get_id()){
                    comprovantesOrdenados.add(comprovantes.get(j));
                    break;
                }
            }
        }
        return comprovantesOrdenados;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Comprovante comprovante = adapterListViewComprovante.getItem(position);

        TextView textHorario = (TextView) view.findViewById(R.id.textHorario);
        String horario = textHorario.getText().toString();

        if(comprovante.getComprovante() == null){
            idComprovante = comprovante.get_id();

            File diretorio = new File(getExternalStorageDirectory() + "/TicketBox");

            boolean b = diretorio.mkdir();

            if(b || diretorio.exists()){
                Log.i(TAG, "TicketBox: Pasta TicketBox criada ou já existente.("+ getExternalStorageDirectory() +")" + idComprovante);
            }else{
                Toast.makeText(getApplicationContext(), "Não foi possivel criar a pasta TicketBox no seu dispostivo.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "TicketBox: Não foi possivel criar a pasta TicketBox no seu dispostivo." + idComprovante);
                return;
            }

            String photoFileName = "temp.jpg";
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Create a File reference to access to future access
            File photoFile = getPhotoFileUri(photoFileName);
            absolutePathFile = photoFile.getAbsolutePath();

            imageUri = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName(), photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, 1234);
            }


        }else{

            Intent intent = new Intent(this, ActivityVisualizarImagem.class);
            intent.putExtra("horario", horario);
            intent.putExtra("arquivoComprovante", comprovante.getComprovante());
            startActivity(intent);

        }

    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "============================= failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        //Obtêm o item que foi selecionado.
        Comprovante comprovante = adapterListViewComprovante.getItem(position);
        comprovanteSelecionado = comprovante;

        idComprovante = comprovante.get_id();
        if(comprovante.getComprovante() != null){
            new removerFotoComprovante().show(ActivityListaComprovantes.super.getFragmentManager() ,"removerFotoComprovante");
        }else{
            new removerComprovante().show(ActivityListaComprovantes.super.getFragmentManager() ,"removerComprovante");
        }

        return true;
    }

    @SuppressLint("ValidFragment")
    public class removerFotoComprovante extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Remover a foto do comprovante?")
                    .setPositiveButton("Sim,remover!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            daoComprovante = new DaoComprovante(builder.getContext());
                            daoComprovante.abrir();
                            daoComprovante.alterarComprovante(idComprovante,null,null);

                            String arquivoComprovante = comprovanteSelecionado.getComprovante();

                            if(arquivoComprovante!=null){
                                File imgFile = new File(getExternalStorageDirectory() + "/TicketBox/"+arquivoComprovante);
                                if(imgFile.exists()){
                                    imgFile.delete();
                                }
                            }

                            daoComprovante.fechar();

                            Log.i(TAG, "removerFotoComprovante chamaou creatListView");
                            createListView();

                            Toast.makeText(getApplicationContext(), "Comprovante removido!", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Foto do comprovante removido! _id Comprovante = " + idComprovante);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    @SuppressLint("ValidFragment")
    public class removerComprovante extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Atenção! Remover o registro para este dia?")
                    .setPositiveButton("Sim,remover!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            daoComprovante = new DaoComprovante(builder.getContext());
                            daoComprovante.abrir();
                            daoComprovante.removerComprovante(idComprovante);
                            daoComprovante.fechar();

                            Log.i(TAG, "removerComprovante chamaou creatListView");
                            createListView();

                            Toast.makeText(getApplicationContext(), "Horário removido!", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Horário removido! _id Comprovante = " + idComprovante);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        data_hora_registro = converterDataHoraParaString(new Date());

        if(requestCode == 1234) {
            if(resultCode == Activity.RESULT_OK) {
                if(imageUri != null) {
                    Toast.makeText(this, "Informe a Data e Hora do Comprovante", Toast.LENGTH_LONG).show();
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.setCancelable(false);
                    newFragment.show(getFragmentManager(),"confirmarDataComprovante");
                }else{
                    Toast.makeText(this, "Sem foto do comprovante...", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Nenhum comprovante obtido.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public String converterDataParaString(Date dia){
        return new SimpleDateFormat("yyyy-MM-dd").format(dia);
    }

    public String converterDataHoraParaString(Date dataHora){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:00").format(dataHora);
    }

    public String exibirDataHoraFormatada(Date dataHora){
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataHora);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(ActivityListaComprovantes.super.getFragmentManager(), "timePicker");
    }

    @SuppressLint("ValidFragment")
    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.AM_PM, c.get(Calendar.AM_PM));
            String horaMinutoComprovante = new SimpleDateFormat("HH:mm:00").format(c.getTime());
            data_hora_registro = dataComprovante + " " + horaMinutoComprovante;

            DialogFragment newFragment = new confirmarDataHora();
            newFragment.setCancelable(false);
            newFragment.show(ActivityListaComprovantes.super.getFragmentManager() ,"confirmarDataHoraComprovante");

        }
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dataComprovante = sdf.format(c.getTime());
            new TimePickerFragment().show(getFragmentManager() ,"confirmarHoraMinutoComprovante");
        }
    }


    @SuppressLint("ValidFragment")
    public class confirmarDataHora extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            String date = data_hora_registro;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
            Date parsed = null;
            try {
                parsed = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            builder.setMessage("Confirma a Data e Hora do Registro? " + exibirDataHoraFormatada(parsed))
                    .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            daoComprovante = new DaoComprovante(builder.getContext());
                            daoComprovante.abrir();
                            if (data_hora_registro != null) {

                                //Bitmap bitmap = BitmapFactory.decodeFile(compressImage(String.valueOf(imageUri)));
                                Bitmap bitmap = BitmapFactory.decodeFile(compressImage(String.valueOf(absolutePathFile)));

                                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                                String nomeArquivo = "comprovante_" + timeStamp + ".jpg";
                                String caminho = getExternalStorageDirectory() + "/TicketBox/";

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                byte[] bytes = stream.toByteArray();
                                FileOutputStream fos;
                                try {
                                    fos = new FileOutputStream(caminho + nomeArquivo);
                                    fos.write(bytes);
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                daoComprovante.alterarComprovante(idComprovante, data_hora_registro, nomeArquivo);
                            }
                            daoComprovante.fechar();

                            Log.i(TAG, "confirmarDataHora chamaou creatListView");
                            createListView();

                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Diálago cancelado
                        }
                    })
                    .setNeutralButton("Alterar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DialogFragment newFragment = new DatePickerFragment();
                            newFragment.show(ActivityListaComprovantes.super.getFragmentManager(), "confirmarDataComprovante");
                        }
                    });
            // Cria o objeto AlertDialog e o retorna
            return builder.create();
        }
    }

    //*
    // Inicio Código para realizar a compreensão da imagem sem perder a resolução;
    // Fonte: https://stackoverflow.com/questions/28424942/decrease-image-size-without-losing-its-quality-in-android
    //
    // */
    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        // inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            // load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

            // write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }

    private String getRealPathFromURI(String contentURI) {

        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    //*
    // Fim do Código para realizar a compreensão da imagem sem perder a resolução;
    // */

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume chamaou creatListView");
        createListView();
    }

}
