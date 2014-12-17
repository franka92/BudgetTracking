package giulia.budgettracking;

import android.app.Dialog;
import android.database.Cursor;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by frank_000 on 23/08/2014.
 */
public class funzioni {




    /*Aumenta la data a seconda del tipo di peridiocità assegnato*/
    public static String nextDate(int tipo, String data){
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        String data_next ="";
        String[] data_split = data.split("-",3);
      /*  String data_formatted = date_format.format(data);*/
        Calendar c  = Calendar.getInstance();
        try {
            c.setTime(date_format.parse(data));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (tipo){
            case dataBase_string.PER_GIORNO:
                c.add(Calendar.DATE,1);
                break;
            case dataBase_string.PER_SETTIM:
                c.add(Calendar.DATE,7);
                break;
            case dataBase_string.PER_MENSILE:
                c.add(Calendar.MONTH,1);
                break;
        }
        data_next = date_format.format(c.getTime());
        return data_next;
    }

    /*Gestisce i click sui bottoni delle dialog*/
    public static void gestisciClickChiudiDialog(final Dialog d){
        Button chiudiDialog = (Button) d.findViewById(R.id.bt_okDettOp);
        chiudiDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();

            }
        });


    }








}
