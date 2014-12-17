package giulia.budgettracking;

/**
 * Created by frank_000 on 14/10/2014.
 */
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class FragmentMeseSett extends Fragment {
    String finalTipo_pdf="";
    AsyncTask task;

    public static Fragment newInstance(Context context) {
        FragmentMeseSett frag = new FragmentMeseSett();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String strtext = getArguments().getString("message");
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_mese_sett, null);
        TextView titolo = (TextView) root.findViewById(R.id.lb_frag_mese_titolo);
        char tipo = 'a';
        String tipo_pdf = null;

        if(strtext == "settimana"){
           titolo.setText("OPERAZIONI DELL'ULTIMA SETTIMANA");
            tipo = 'w';
            tipo_pdf = "settimanale";
        }
        else if (strtext == "mese"){
            titolo.setText("OPERAZIONI DELL'ULTIMO MESE");
            tipo = 'm';
            tipo_pdf = "mensile";
        }
        else if(strtext == "totale"){
            titolo.setText("LISTA MOVIMENTI");
            tipo = 'a';
            tipo_pdf = "elenco_completo";
        }

        Operazione o = new Operazione(this.getActivity());
        TableLayout tbl = (TableLayout) root.findViewById(R.id.table_listaOp_meseSett);
        o.mostraOperazioni(tbl,tipo,null);

        Button bt_generaPDF = (Button) root.findViewById(R.id.bt_generaPDF);
        finalTipo_pdf = tipo_pdf;
        bt_generaPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = new generaReportPDF().execute();


            }

        });
        return root;
    }

    @Override
    public void onStop () {
        if(task != null)
            task.cancel(true);
        super.onStop();
    }



    private class generaReportPDF extends AsyncTask<Void, Void, Integer> {
        String result = null;

        @Override
        protected Integer doInBackground(Void... params) {
            GeneraPDF gen = new GeneraPDF(getActivity().getApplicationContext());
            result = gen.creaPdf(finalTipo_pdf,null);

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