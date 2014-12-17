package giulia.budgettracking;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by frank_000 on 21/08/2014.
 */
public class gestioneAccount extends FragmentActivity {
    private databBaseManager db = null;

    Dialog d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dettagliacc);
        db = new databBaseManager(this);
        TextView nome_conto = (TextView) findViewById(R.id.txt_dettNomeConto);
        TextView saldo_attuale = (TextView) findViewById(R.id.txt_dettSaldoAtt);
        TextView saldo_iniziale = (TextView) findViewById(R.id.txt_dettSaldoIniz);
        TextView descrizione = (TextView) findViewById(R.id.txt_dettDescrizione);

        Cursor c = db.recuperaAccount();
        try{
            while (c.moveToNext()){
               Double imp;
               nome_conto.setText(c.getString(1));
               imp = c.getDouble(2);
               saldo_attuale.setText(imp.toString()+"€");
               imp = c.getDouble(3);
               saldo_iniziale.setText(imp.toString()+"€");
               if(c.getString(4) != ""){
                   descrizione.setText(c.getString(4));
               }
            }
        }
        finally{
            c.close();
        }

        Button btElimina = (Button) findViewById(R.id.bt_eliminaAccount);
        btElimina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogElimina();

            }
        });
    }

    public void showDialogElimina(){
        d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(false);				//Toccando il display al di fuori della finestra essa non si chiuderà
        d.setContentView(R.layout.dialog_cancellaccount);	//Assegnamo un layout alla finestra
        d.show();

        Button annulla= (Button) d.findViewById(R.id.bt_annullaEliminaAccount);  //Recupero il bottone
        Button elimina= (Button) d.findViewById(R.id.bt_confermaEliminaAccount);  //Recupero il bottone

        annulla.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        } );

        elimina.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
            @Override
            public void onClick(View view) {
                int res = db.deleteAll();//Cancella tutti i dati dal dataBase --> bisogna anche azzerare tutti i campi (ma mi serve?)
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("KEY_FIRST_RUN", Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();
                if(res == 1){

                    d.dismiss();
                    /*Forzo la chiusura e successiva riapertura dell'app*/
                    Intent mStartActivity = new Intent(getApplicationContext(), main.class);
                    PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Errore nella cancellazione. Riprovare", Toast.LENGTH_SHORT).show();
                }


            }
        } );
    }


}
