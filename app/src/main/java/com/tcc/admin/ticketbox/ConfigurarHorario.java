package com.tcc.admin.ticketbox;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ConfigurarHorario extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnDateSelectedListener, TimePickerFragment.FragmentCallbacks {

    MaterialCalendarView calendario;
    int quantidadeDiasSelecionados = 0;
    TextView txtQuantidadeDiasSelecionados;
    DaoHorario daoHorario;
    Horario horario;
    DaoComprovante daoComprovante;
    Comprovante comprovante;
    String tipoHorario = "";
    Button btnSalvarHorarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracoes_horarios);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnSalvarHorarios = (Button) findViewById(R.id.btnSalvarHorarios);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipoRegistroComprovante, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        txtQuantidadeDiasSelecionados = (TextView)findViewById(R.id.txtQuantidadeDiasSelecionados);
        calendario = (MaterialCalendarView)findViewById(R.id.calendarView);
        configurarCalendario();

    }

    public void configurarCalendario()  {

        calendario.setSelectionColor(Color.parseColor("#00BCD4"));
        calendario.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        calendario.setOnDateChangedListener(this);
        calendario.setAllowClickDaysOutsideCurrentMonth(false);
        calendario.setShowOtherDates(0);

        SimpleDateFormat formatador = new SimpleDateFormat("yyyy/MM/dd");

        Date dataInicial = null;
        Date dataFinal = null;

        final Calendar cal = Calendar.getInstance();

        int mes = cal.get(Calendar.MONTH);
        int ano = cal.get(Calendar.YEAR);

        try {
            dataInicial = formatador.parse(formatador.format(cal.getTime()));
            String ultimoDia = formatador.format(new SimpleDateFormat("yyyy/MM/dd").parse(ano+"/"+(mes+1)+"/"+cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
            dataFinal = formatador.parse(ultimoDia);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar inicio = Calendar.getInstance();
        inicio.setTime(dataInicial);
        Calendar fim = Calendar.getInstance();
        fim.setTime(dataFinal);

        Calendar diaSemana = Calendar.getInstance();
        for (Date date = inicio.getTime(); !inicio.after(fim); inicio.add(Calendar.DATE, 1), date = inicio.getTime()) {
            String d = formatador.format(date);
            diaSemana.setTime(date);
            if(diaSemana.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && diaSemana.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY ){
                calendario.setDateSelected(new Date(d),true);
                quantidadeDiasSelecionados++;
            }
        }
        txtQuantidadeDiasSelecionados.setText("Quantidade de dias selecionados: " + quantidadeDiasSelecionados);
        habilitarBotaoSalvar();
    }

    public void habilitarBotaoSalvar(){
        String txtBtnHorario = String.valueOf(((Button)findViewById(R.id.btnHorario)).getText());
        if (quantidadeDiasSelecionados == 0){
            btnSalvarHorarios.setEnabled(false);
        }else if(txtBtnHorario.equalsIgnoreCase("Escolha o Horário")){
            btnSalvarHorarios.setEnabled(false);
        }else if(quantidadeDiasSelecionados != 0){
            btnSalvarHorarios.setEnabled(true);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        tipoHorario = parent.getItemAtPosition(pos).toString();
        consultarHorarioComprovante(calendario.getSelectedDates());
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(ConfigurarHorario.super.getFragmentManager(), "timePicker");
    }

    @Override
    public void TimeUpdated(String hora, String minuto) {
        consultarHorarioComprovante(calendario.getSelectedDates());
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        List<CalendarDay> dias = new ArrayList<>();
        if(selected == true){
            dias.add(date);
            consultarHorarioComprovante(dias);
            quantidadeDiasSelecionados++;
        }else{
            quantidadeDiasSelecionados--;
        }
        txtQuantidadeDiasSelecionados.setText("Quantidade de dias selecionados: " + quantidadeDiasSelecionados);
        habilitarBotaoSalvar();
    }

    public void consultarHorarioComprovante(List<CalendarDay> listaDeDias){

        Button btnHorario = ((Button) findViewById(R.id.btnHorario));
        String descricao = tipoHorario + " das " + btnHorario.getText();

        List<CalendarDay> dias = listaDeDias;

        CalendarDay[] diasOrdenados = dias.toArray(new CalendarDay[dias.size()]);
        // Ordenando os dias
        Collections.sort(Arrays.asList(diasOrdenados), new Comparator<CalendarDay>() {
            public int compare(CalendarDay o1, CalendarDay o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        horario = new Horario();
        horario.set_id(0);

        daoHorario = new DaoHorario(this);
        daoHorario.abrir();
        horario = daoHorario.buscarHorario(descricao);
        daoHorario.fechar();

        if(horario.get_id() != 0){
            Log.i(TAG, "Horário " + descricao + " já existente. ID do Horário = " + horario.get_id());
        }

        daoComprovante = new DaoComprovante(this);
        daoComprovante.abrir();
        String diaAsString = null;
        for (CalendarDay dia : diasOrdenados) {
            diaAsString = converterDataParaString(dia.getDate());
            comprovante = daoComprovante.buscarComprovante(horario.get_id(),diaAsString);
            if(comprovante.get_id() != 0){
                Log.i(TAG, "Comprovante já existente para a " + descricao + " do dia " + diaAsString
                        + ". id_horário = " + horario.get_id() + " e idComprovante = " + comprovante.get_id());
                calendario.setDateSelected(dia,false);
                quantidadeDiasSelecionados--;
            }
        }
        txtQuantidadeDiasSelecionados.setText("Quantidade de dias selecionados: " + quantidadeDiasSelecionados);
        habilitarBotaoSalvar();
        daoComprovante.fechar();
    }

    public void salvarHorarioComprovante(View view){

        Button btnHorario = ((Button) findViewById(R.id.btnHorario));
        String descricao = tipoHorario + " das " + btnHorario.getText();

        List<CalendarDay> dias = calendario.getSelectedDates();

        CalendarDay[] diasOrdenados = dias.toArray(new CalendarDay[dias.size()]);
        // Ordenando os dias
        Collections.sort(Arrays.asList(diasOrdenados), new Comparator<CalendarDay>() {
            public int compare(CalendarDay o1, CalendarDay o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        horario = new Horario();
        horario.set_id(0);

        daoHorario = new DaoHorario(this);
        daoHorario.abrir();
        horario = daoHorario.buscarHorario(descricao);

        if (horario.get_id() == 0){
            horario.setDescricao(descricao);
            daoHorario.inserirHorario(horario);
            Log.i(TAG, "Horário " + descricao + " INSERIDO com SUCESSO. id_horário = " + horario.get_id());
        }else if(horario.get_id() != 0){
            Log.i(TAG, "Horário " + descricao + " já existente. ID do Horário = " + horario.get_id());
        }
        daoHorario.fechar();

        daoComprovante = new DaoComprovante(this);
        daoComprovante.abrir();
        String diaAsString;
        int qntdeHorariosConfigurados = 0;
        for (CalendarDay dia : diasOrdenados) {
            diaAsString = converterDataParaString(dia.getDate());
            comprovante = daoComprovante.buscarComprovante(horario.get_id(),diaAsString);

            if (comprovante.get_id() == 0){

                comprovante.setDia(diaAsString);
                comprovante.setHorario(horario);
                daoComprovante.inserirComprovante(comprovante);
                calendario.setDateSelected(dia,false);
                qntdeHorariosConfigurados++;
                Log.i(TAG, "Comprovante da " + descricao + " para o dia " + diaAsString
                        + " INSERIDO com SUCESSO..... id_horário = " + horario.get_id() + " e idComprovante = " + comprovante.get_id());

            }else if(comprovante.get_id() != 0){
                Log.i(TAG, "Comprovante já existente para a " + descricao + " do dia " + diaAsString
                        + ". id_horário = " + horario.get_id() + " e idComprovante = " + comprovante.get_id());
            }
        }
        daoComprovante.fechar();
        quantidadeDiasSelecionados=0;
        txtQuantidadeDiasSelecionados.setText("Quantidade de dias selecionados: " + quantidadeDiasSelecionados);
        Toast.makeText(this, qntdeHorariosConfigurados + " horário(s) cadastrado(s)!", Toast.LENGTH_LONG).show();
        calendario.clearSelection();
        habilitarBotaoSalvar();
    }

    public String converterDataParaString(Date dia){
        return new SimpleDateFormat("yyyy-MM-dd").format(dia);
    }

}