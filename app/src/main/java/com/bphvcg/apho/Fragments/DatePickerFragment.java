package com.bphvcg.apho.Fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import com.bphvcg.apho.R;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int iYear = calendar.get(Calendar.YEAR);
        int iMonth = calendar.get(Calendar.MONTH);
        int iDay = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(),this,iYear,iMonth,iDay);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int iYear, int iMonth, int iDay) {
        EditText editBirthdayRE = (EditText) getActivity().findViewById(R.id.editTextDateOfBirth);
        editBirthdayRE.setText(iDay + "/" + (iMonth+1) + "/" + iYear);
    }
}
