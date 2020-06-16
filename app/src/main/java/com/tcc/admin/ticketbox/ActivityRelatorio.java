package com.tcc.admin.ticketbox;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by admin on 16/06/2017.
 */

public class ActivityRelatorio extends Activity{

    private String arquivoPdf;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relatorio);

        String[] meses = new String[] { "Janeiro", "Fevereiro",
                "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro",
                "Outubro", "Novembro", "Dezembro" };

        final ArrayList<String> mes = new ArrayList<String>();
        int mesAtual = Calendar.getInstance().get(Calendar.MONTH);
        for (int i = 0; i < meses.length; i++) {
            mes.add(meses[i]);
        }

        SpinnerAdapter adapterMes = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mes);
        Spinner spinMes = (Spinner)findViewById(R.id.mes);
        spinMes.setAdapter(adapterMes);
        spinMes.setSelection(mesAtual);

        final ArrayList<String> ano = new ArrayList<String>();
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = anoAtual; i >= 2015; i--) {
            ano.add(String.valueOf(i));
        }

        SpinnerAdapter adapterAno = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ano);
        Spinner spinAno = (Spinner)findViewById(R.id.ano);
        spinAno.setAdapter(adapterAno);
        spinAno.setSelection(0);

        final Button gerarRelatorio = (Button) findViewById(R.id.gerarRelatorio);
        final Button gerarPdf = (Button) findViewById(R.id.gerarPDF);

        gerarRelatorio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gerarRelatorio();
            }
        });

        gerarPdf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                gerarPdf();
            }
        });

        gerarRelatorio();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void gerarRelatorio(){

        TextView relatorio = (TextView) findViewById(R.id.relatorio);
        relatorio.setMovementMethod(ScrollingMovementMethod.getInstance());
        relatorio.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        String mes = ((Spinner)findViewById(R.id.mes)).getSelectedItem().toString();
        String ano = ((Spinner)findViewById(R.id.ano)).getSelectedItem().toString();

        String dados = "TICKETBOX" +
                "\n\nRELATÓRIO DE REGISTROS DE PONTOS" +
                "\nMÊS: " + mes.toUpperCase() +"/" + ano + "\n";

        String diaAtual;

        int idHorario;

        Calendar cal = Calendar.getInstance();
        int mesSelecionado = ((Spinner)findViewById(R.id.mes)).getSelectedItemPosition();
        cal.set(Calendar.MONTH, mesSelecionado);
        int anoSelecionado = Integer.parseInt((String) ((Spinner) findViewById(R.id.ano)).getSelectedItem());
        cal.set(Calendar.YEAR, anoSelecionado);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<Object>[] dia = new ArrayList[maxDay];

        DaoComprovante daoComprovante = new DaoComprovante(this);
        DaoHorario daoHorario = new DaoHorario(this);
        daoComprovante.abrir();
        daoHorario.abrir();

        // Aqui são geradas as séries, guardando-as no Array de ArrayList.
        for (int i = 1; i <= maxDay; i++) {
            dados += "\n"+i+": ";
            diaAtual = df.format(cal.getTime());
            Cursor cursor = daoComprovante.obterTodosOsComprovantesComFoto(diaAtual);
            dia[i-1] = new ArrayList<>();

            if (((cursor != null) && (cursor.getCount() > 0))) {
                cursor.moveToFirst();
                dados += "\n";
                do {
                    idHorario = cursor.getInt(cursor.getColumnIndex("id_horario"));
                    String descricaoHorario = daoHorario.buscarHorario(idHorario).getDescricao();

                    String date = cursor.getString(cursor.getColumnIndex("data_hora_registro"));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                    Date parsed = null;
                    try {
                        parsed = format.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String dataHoraRegistro = exibirDataHoraFormatada(parsed);
                    dados += "  |- " + descricaoHorario + " | " + dataHoraRegistro + "\n";
                } while (cursor.moveToNext());
            }else{
                dados += "*****";
            }

            /********************************
            * Código para simular vários registros por dia no Relatório PDF. Comente o trecho do IF acima!
            *
            * atribua um valor para a variavel registrosPorDia
            * **

            int registrosPorDia = 10

            dados += "\n";
            for(int j=1; j <= registrosPorDia; j++){
                dados += "  |- Entrada das " + j +" XX:XX | 09-12-2017 08:10" + "\n";
            }
            ***********************************/

            cal.set(Calendar.DAY_OF_MONTH, i + 1);
        }

        dados += "\n \n **** Fim do Relátorio ****";
        dados += "\n \n";

        daoHorario.fechar();
        daoComprovante.fechar();
        relatorio.setText(dados);

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void gerarPdf() {

        try {

            TextView relatorio = (TextView) findViewById(R.id.relatorio);

            File diretorio = new File(getExternalStorageDirectory() + "/TicketBox");
            boolean b = diretorio.mkdir();
            if(b || diretorio.exists()){
                Log.i(TAG, "TicketBox: Pasta TicketBox criada.");
            }else{
                Toast.makeText(getApplicationContext(), "Não foi possivel criar a pasta TicketBox no seu dispostivo.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "TicketBox: Não foi possivel criar a pasta TicketBox no seu dispostivo.");
                return;
            }

            String targetPdf = getExternalStorageDirectory() + "/TicketBox/relatorioTicketBox.pdf";
            File file = new File(targetPdf);

            /*if(file.exists()){
                arquivoPdf = file.getAbsolutePath();
                Log.i(TAG, "TicketBox: ARQUIVO PDF EXISTE." + file.getAbsolutePath());
            }
            else{
                Log.i(TAG, "TicketBox: ARQUIVO PDF NÃO EXISTE.");
            }*/

            arquivoPdf = file.getAbsolutePath();

            FileOutputStream out = null;
            out = new FileOutputStream(file);

            PdfDocument document = new PdfDocument();

            // 14 foi a proporção encontrada para uma boa altura da pagina do relatorio,
            // proporcional ao numero de linhas contidos no TextView
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, (relatorio.getLineCount()*14), 1).create();

            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            TextPaint tp = new TextPaint();
            StaticLayout sl = new StaticLayout(relatorio.getText(),tp,canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,false);

            sl.getHeight();

            canvas.save();
            canvas.translate(15,5);

            sl.draw(page.getCanvas());

            document.finishPage(page);
            document.writeTo(out);
            document.close();
            out.flush();

        } catch (FileNotFoundException e) {
            Log.i(TAG, "TicketBox: Ocorreu um erro durante a geração do relatório. Arquivo não encontrado.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "TicketBox: Ocorreu um erro durante a geração do relatório.");
            e.printStackTrace();
        }


        try {
            File pdfFile = new File(arquivoPdf);

            if(pdfFile.exists()){
                arquivoPdf = pdfFile.getAbsolutePath();
            }
            else{
                Toast.makeText(getApplicationContext(), "Não foi possivel criar o arquivo PDF.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "TicketBox: Arquivo PDF não existe.");
            }

            Uri path = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName(), pdfFile);

            // Setting the intent for pdf reader
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(pdfIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "TicketBox: Erro no Activity da geração do relatório." + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public String exibirDataHoraFormatada(Date dataHora){
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(dataHora);
    }
}

