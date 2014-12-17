package giulia.budgettracking;

/**
 * Created by frank_000 on 14/10/2014.
 */

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class FragmentFiltraGiorno extends Fragment{

    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    ViewGroup root;
    String formatted;
    AsyncTask task;

    public static Fragment newInstance(Context context) {
        FragmentFiltraGiorno frag = new FragmentFiltraGiorno();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_giorno, null);
        /*Gestisco la scelta della data*/
        Calendar cal = Calendar.getInstance();
        TextView txt_date = (TextView) root.findViewById(R.id.txt_selectData);
        Button bt_aggiorna = (Button) root.findViewById(R.id.bt_aggiornaElenco);
        formatted = date_format.format(cal.getTime());// Output "2012-09-26"
        txt_date.setText(formatted);

        /*Gestione click sulla data*/
        txt_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

               FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = null;
                newFragment = new datePickerDialogFragment();
                //newFragment.show
                newFragment.show(ft, "dialog");
            }

        });

        bt_aggiorna.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TextView txt_date = (TextView) root.findViewById(R.id.txt_selectData);
                aggiornaListaOperazioni(txt_date.getText().toString());
            }
        });

        Button bt_generaPDF = (Button) root.findViewById(R.id.bt_generaPDF);
        bt_generaPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = new generaReportPDF().execute();


            }

        });

        Operazione o = new Operazione(this.getActivity());
        TableLayout tbl = (TableLayout) root.findViewById(R.id.table_listaOp_giorno);
        o.mostraOperazioni(tbl,'d',formatted);
        return root;
    }



    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStop () {
        if(task != null)
            task.cancel(true);
        super.onStop();
    }



    public void aggiornaListaOperazioni(String date){
        Operazione o = new Operazione(this.getActivity());
        TableLayout tbl = (TableLayout)getActivity().findViewById(R.id.table_listaOp_giorno);
        o.mostraOperazioni(tbl,'d',date);
    }


    private class generaReportPDF extends AsyncTask<Void, Void, Integer> {
        String result = null;

        @Override
        protected Integer doInBackground(Void... params) {
            TextView txt_date = (TextView) root.findViewById(R.id.txt_selectData);
            GeneraPDF gen = new GeneraPDF(getActivity().getApplicationContext());
            result = gen.creaPdf("giornaliero",(txt_date.getText().toString()));

            return 1;
        }

        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(Integer in) {
            if(result != null && getActivity().getApplicationContext()!= null)
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }






}