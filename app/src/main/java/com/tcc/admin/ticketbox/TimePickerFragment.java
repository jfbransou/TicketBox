package com.tcc.admin.ticketbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by admin on 12/04/2017.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(),
                AlertDialog.THEME_HOLO_DARK,this,hour,minute,true);

    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String hora = ((((hourOfDay < 10) ? "0" : "") + hourOfDay));
        String minuto = ((((minute < 10) ? "0" : "") + minute));
        Button btnHorario = (Button) getActivity().findViewById(R.id.btnHorario);
        btnHorario.setText(hora + ":" + minuto);
        mCallbacks.TimeUpdated(hora, minuto);
    }

    /**
     * Interface
     */
    private FragmentCallbacks mCallbacks;

    public interface FragmentCallbacks {
        void TimeUpdated(String hora, String minuto);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (FragmentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity deve implementar o Segundo Fragmento.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

}
