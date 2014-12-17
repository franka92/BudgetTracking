package giulia.budgettracking;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by frank_000 on 09/07/14.
 *
 * Activity che gestisce la creazione di un nuovo account.
 */
public class AddAccount extends Activity {

    Dialog d;
    private databBaseManager db = null;
    CursorAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addaccount);
        d = new Dialog(this);        //Istanzio un oggetto Dialog passando un riferimento al context
        d.setTitle("Benvenuto!");				//imposto il titolo della finestra
        d.setCancelable(false);				//Toccando il display al di fuori della finestra essa non si chiuderà
        d.setContentView(R.layout.dialog);	//Assegnamo un layout alla finestra
        TextView txt = (TextView) d.findViewById(R.id.text_intro);
        txt.setText(readTxt());
        d.show();                           //Visualizziamo la finestra
        Button createAccount= (Button) d.findViewById(R.id.bt_inizia);  //Recupero il bottone

        createAccount.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        } );

        db = new databBaseManager(this);


        /*Gestione onClick sul bottone "Conferma Account" --> Salvataggio dati su database*/
        Button confermaAccount = (Button) this.findViewById(R.id.btConfAcc);
        confermaAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor c = db.query(dataBase_string.TBL_ACCOUNT);
                salva(view);
                c.close();
                /*Chiamo l'activity principale*/
                Intent i_main = new Intent(getApplicationContext(),main.class);
                i_main.putExtra("accountcreato",1);
                startActivity(i_main);
            }
        });


        /*Gestione onClick sul bottone "Annulla" --> azzera i campi*/
        Button annulla = (Button) this.findViewById(R.id.btAnnAcc);
        annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name = (EditText) findViewById(R.id.txtNomeAcc);
                EditText conto = (EditText) findViewById(R.id.txtContoAcc);
                EditText desc = (EditText) findViewById(R.id.txtDescAcc);
                name.setText(null, null);
                conto.setText(null,null);
                desc.setText(null, null);
            }
        });
    }

    /*Salvo i dati relativi all'account nel database*/
    public void salva(View v){
        /*Recupero i dati dall'input*/
        EditText name = (EditText) findViewById(R.id.txtNomeAcc);
        EditText conto = (EditText) findViewById(R.id.txtContoAcc);
        EditText desc = (EditText) findViewById(R.id.txtDescAcc);
        /*Controllo che i campi obbligatori siano stati riempiti*/
        if (name.length()>0 && conto.length()>0){
                db.salvaAccount(name.getEditableText().toString(), Double.parseDouble(conto.getText().toString()), Double.parseDouble(conto.getText().toString()) , desc.getEditableText().toString());
        }
        else{
            Toast.makeText(this, "Devi inserire tutti i campi obbligatori!!", Toast.LENGTH_LONG).show();
        }
    }

    /*Legge il file di testo contentente le istruzioni iniziali*/
    private String readTxt(){

        InputStream inputStream = getResources().openRawResource(R.raw.intro);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }

}
