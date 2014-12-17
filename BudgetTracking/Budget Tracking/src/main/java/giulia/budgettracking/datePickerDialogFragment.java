package giulia.budgettracking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by frank_000 on 13/07/14.
 */
public class datePickerDialogFragment  extends DialogFragment {
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");

    public datePickerDialogFragment(){

    }
    protected DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
            Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            TextView txt_date = (TextView) getActivity().findViewById(R.id.txt_selectData);
            String formatted = date_format.format(cal.getTime());// Output "2012-09-26"
            txt_date.setText(formatted);
            Calendar cal_attuale = Calendar.getInstance();
            CheckBox check_rip = (CheckBox) getActivity().findViewById(R.id.chek_ripetizioneOperazione);
            if(check_rip != null){
                if(date_format.format(cal_attuale.getTime()).compareTo(formatted) < 0){
                    if(check_rip.isChecked()){
                        check_rip.setChecked(false);
                    }
                    check_rip.setEnabled(false);
                }
                else{
                    check_rip.setEnabled(true);
                }
            }
        }
    };

    /*Mostra il calendario*/
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar cal = Calendar.getInstance();
        return new DatePickerDialog(getActivity(),mDateSetListener,
                cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

    }


}