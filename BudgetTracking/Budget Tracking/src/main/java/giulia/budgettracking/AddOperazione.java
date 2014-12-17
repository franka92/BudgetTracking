package giulia.budgettracking;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by frank_000 on 08/10/2014.
 */
public class AddOperazione extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener{

    private databBaseManager db = null;
    private String mCurrentPhotoPath = null;
    private Uri photoUri;
    LocationClient mLocationClient;
    Location mCurrentLocation;
    String luogo_longitudine = "";
    String luogo_latitudine = "";
    Dialog d;
    int id_luogo_selezionato = -1;
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    int op_type;
    JSONArray elenco_indirizzi = new JSONArray();
    AsyncTask task;

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        String location = "";
        mCurrentLocation = mLocationClient.getLastLocation();
        if(mCurrentLocation != null) {
            TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
            row.setVisibility(View.VISIBLE);
            double longitude = mCurrentLocation.getLongitude();
            double latitude = mCurrentLocation.getLatitude();
            luogo_latitudine = String.valueOf(latitude);
            luogo_longitudine = String.valueOf(longitude);
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (null != listAddresses && listAddresses.size() > 0) {
                    for (int i = 0; i < listAddresses.size(); i++) {
                        location = listAddresses.get(i).getAddressLine(i);
                    }
                    location = listAddresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditText l = (EditText) findViewById(R.id.txt_luogoOperazione);
            l.setText(location);
        }
        else{
            TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
            row.setVisibility(View.GONE);
            Toast.makeText(this,"Impossibile rilevare la posizione",Toast.LENGTH_SHORT).show();
        }



    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnesso. Provare a riconnettersi",
                Toast.LENGTH_SHORT).show();
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
        row.setVisibility(View.GONE);
        Toast.makeText(this,"Impossibile connettersi",Toast.LENGTH_SHORT).show();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
            if(task != null)
                task.cancel(true);
        super.onStop();
    }

    /*Recupera latitudine e longitudine dell'indirizzo dato
    * @param indirizzo: stringa con l'indirizzo da ricercare
    * @return JSONObject: oggetto JSON con l'elenco dei risultati trovati
    */
    public static JSONObject getLatLongFromGivenAddress(String indirizzo) {
        String address = indirizzo.replace(" ","%20");
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                address + "&sensor=false&language=it";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        op_type = i.getIntExtra("tipoOperazione", -1);/*-1 è il valore di default, se va storto qualcosa*/
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(this, this, this);
        d = new Dialog(this);

        Spinner spinner;
        db = new databBaseManager(this);
        setContentView(R.layout.activity_addoperazione);
        TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
        row.setVisibility(View.GONE);
        TextView titolo = (TextView)findViewById(R.id.lb_operazioneTitolo);

        /*Riempo lo spinner con la lista delle categorie, in base al tipo di operazione che si sta inserendo*/
        if(op_type == 0){/*Entrata*/
            titolo.setText("AGGIUNGI ENTRATA");
            /*Creo lo spinner per la lista delle categorie*/
            spinner = (Spinner) findViewById(R.id.sp_catOperazione);
            riempiSpinnerCat(spinner, dataBase_string.CAT_ENTRATA);
        }
        else if(op_type == 1){/*Pagamento*/
            titolo.setText("AGGIUNGI PAGAMENTO");
            /*Creo lo spinner per la lista delle categorie*/
            spinner = (Spinner) findViewById(R.id.sp_catOperazione);
            riempiSpinnerCat(spinner, dataBase_string.CAT_PAGAMENTO);
        }

        /*Gestisco la scelta della data*/
        Calendar cal = Calendar.getInstance();
        TextView txt_date = (TextView) findViewById(R.id.txt_selectData);
        Button bt_foto = (Button) findViewById(R.id.bt_addFoto);
        String formatted = date_format.format(cal.getTime());// Output "2012-09-26"
        txt_date.setText(formatted);

        /*Gestione click sulla data*/
        txt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                DialogFragment newFragment = null;
                newFragment = new datePickerDialogFragment();
                newFragment.show(ft, "dialog");
            }
        });


        /*Spinner mezzo di transazione*/
        ArrayAdapter<CharSequence> adapterMezzo = ArrayAdapter.createFromResource(this, R.array.array_mezzi,R.layout.my_spinner);
        adapterMezzo.setDropDownViewResource(R.layout.my_spinner_drop);
        Spinner spMezzo = (Spinner) findViewById( R.id.sp_mOperazione);
        spMezzo.setAdapter( adapterMezzo );

        /*Gestione ripetizione pagamento*/
        CheckBox check_rip = (CheckBox) findViewById(R.id.chek_ripetizioneOperazione);
        check_rip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Spinner sp_rip = (Spinner) findViewById(R.id.sp_ripOperazione);
                if(isChecked){
                    sp_rip.setVisibility(View.VISIBLE);
                    ArrayAdapter<CharSequence> adapterPeriodo = ArrayAdapter.createFromResource(getBaseContext(), R.array.array_periodico, R.layout.my_spinner );
                    adapterPeriodo.setDropDownViewResource(R.layout.my_spinner_drop);
                    Spinner spMezzo = (Spinner) findViewById( R.id.sp_ripOperazione);
                    spMezzo.setAdapter( adapterPeriodo );
                }
                else{
                    sp_rip.setVisibility(View.INVISIBLE);
                }

            }
        });



        /*Gestisco gli eventi dei bottoni*/

        /*Gestione click sul bottone "Aggiungi Foto"*/
        bt_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (photoFile != null) {
                        photoUri = Uri.fromFile(photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(takePictureIntent, 1);
                    }

                }
            }
        });

        /*Gestione onClick sul bottone "Conferma Operazione" --> Salvataggio dati su database*/
        Button confermaPagamento = (Button) findViewById(R.id.bt_confOperazione);
        confermaPagamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor c = db.query(dataBase_string.TBL_OPERAZIONI);
                Boolean res = salva(view);
                c.close();
                if(res == true){
                    /*Azzero i campi*/
                    azzeraCampi();
                }
            }
        });

        /*Gestione onClick sul bottone "Annulla" --> azzera tutti i campi*/
        Button bt_azzera = (Button) findViewById(R.id.bt_annOperazione);
        bt_azzera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                azzeraCampi();
                if(mCurrentPhotoPath != null){
                    File file = new File(mCurrentPhotoPath);
                    boolean deleted = file.delete();
                    mCurrentPhotoPath = null;
                }
            }
        });


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        EditText txt = (EditText) findViewById(R.id.txt_luogoOperazione);
        try{
            txt.setText(elenco_indirizzi.getJSONObject(id_luogo_selezionato).getString("indirizzo"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        super.onResume();
    }


    /*OnActivityResult dell'Intent richiamato per le foto
    * @param requestCode: Identifica da chi arriva il risultato
    * @param resultCode: l'int relativo al result restituito dall'activity
    * @param data: intent che può restituire i dati al chiamante
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ImageView view_foto = (ImageView) findViewById(R.id.view_foto);
            File photo = new File(photoUri.getPath());
            try {
                Bitmap photo_bit = MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.fromFile(photo));
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(photoUri.getPath(), bounds);
                ExifInterface exif = new ExifInterface(photoUri.getPath());
                String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

                Matrix matrix = new Matrix();
                matrix.setRotate(rotationAngle, (float) photo_bit.getWidth() / 2, (float) photo_bit.getWidth() / 2);
                Bitmap rotatedBitmap = Bitmap.createBitmap(photo_bit, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
                view_foto.setImageBitmap(Bitmap.createScaledBitmap(rotatedBitmap,300,500, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(storageDir + "/budgetTracking");
        boolean success = false;
        if(!folder.exists()){
            success = folder.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                folder     /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /*Salvo i dati relativi alla spesa nel database*/
    public boolean salva(View v) {
        /*Recupero i dati dall'input*/
        EditText name = (EditText) findViewById(R.id.txt_oggettoOperazione);
        EditText conto = (EditText) findViewById(R.id.txt_importoOperazione);
        EditText desc = (EditText) findViewById(R.id.txt_descOperazione);
        TextView data = (TextView) findViewById(R.id.txt_selectData);
        Spinner spinner_mezzo = (Spinner) findViewById(R.id.sp_mOperazione);
        Spinner spinner_cat = (Spinner) findViewById(R.id.sp_catOperazione);
        CheckBox check_ripetiz = (CheckBox) findViewById(R.id.chek_ripetizioneOperazione);
        Categoria categ = (Categoria) spinner_cat.getSelectedItem();
        String cat = categ.cat_name;
        String mezzo = spinner_mezzo.getSelectedItem().toString();

        /*Controllo che i campi obbligatori siano stati riempiti: Nome, importo, data, categoria e mezzo sono obbligatori! */
        if (name.length() > 0 && conto.length() > 0 && data.length() > 0 && cat.length() > 0 && mezzo.length() > 0) {

            Calendar cal = Calendar.getInstance();
            String data_attuale = date_format.format(cal.getTime());// Output "2012-09-26"
            String data_operaz = data.getText().toString();

            if (data_attuale.compareTo(data_operaz) < 0) {/*Operazione futura --> inserisco l'operazione del database con periodica = 1*/
                db.salvaOperazione(1, name.getEditableText().toString(), Double.parseDouble(conto.getText().toString()), categ.cat_type, data.getText().toString(), luogo_latitudine,luogo_longitudine,desc.getEditableText().toString(), mezzo, op_type,mCurrentPhotoPath,1);
            } else {
                db.salvaOperazione(1, name.getEditableText().toString(), Double.parseDouble(conto.getText().toString()), categ.cat_type, data.getText().toString(), luogo_latitudine,luogo_longitudine,desc.getEditableText().toString(), mezzo, op_type,mCurrentPhotoPath,0);
                db.aggiornaConto(Double.parseDouble(conto.getText().toString()),op_type);
                if (check_ripetiz.isChecked()) { /*Devo inserire anche nella tabella delle operazioni periodiche*/
                    Spinner sp_ripetiz = (Spinner) findViewById(R.id.sp_ripOperazione);
                    int tipo_ripetiz = sp_ripetiz.getSelectedItemPosition();
                    int id_esterno = -1;
                    String next_date = funzioni.nextDate(tipo_ripetiz, data_operaz);
                    Cursor c = db.recuperaUltimoInserimento(dataBase_string.TBL_OPERAZIONI, dataBase_string.O_FIELD_ID);
                    if (c.moveToFirst()) {
                        id_esterno = c.getInt(0);
                        db.salvaPeriodica(id_esterno,op_type, tipo_ripetiz, data_operaz, next_date);
                        c.close();
                    }
                }

            }
            return true;
        }
        else{
            Toast.makeText(this, "Devi inserire tutti i campi obbligatori!!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /*Riporta tutti i campi dell'activity al valore originale*/
    public void azzeraCampi(){
        EditText name = (EditText) findViewById(R.id.txt_oggettoOperazione);
        EditText conto = (EditText) findViewById(R.id.txt_importoOperazione);
        EditText desc = (EditText) findViewById(R.id.txt_descOperazione);
        EditText luogo = (EditText) findViewById(R.id.txt_luogoOperazione);
        RadioButton rd_luogo_a = (RadioButton) findViewById(R.id.radio_corrente);
        RadioButton rd_luogo_m = (RadioButton) findViewById(R.id.radio_manuale);
        TableRow row_luogo = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
        Spinner spinner_mezzo = (Spinner) findViewById(R.id.sp_mOperazione);
        Spinner spinner_cat = (Spinner) findViewById(R.id.sp_catOperazione);
        TextView data = (TextView) findViewById(R.id.txt_selectData);
        Calendar cal = Calendar.getInstance();
        CheckBox ripet = (CheckBox) findViewById(R.id.chek_ripetizioneOperazione);
        String formatted = date_format.format(cal.getTime());
        ImageView view_foto = (ImageView) findViewById(R.id.view_foto);

        name.setText(null, null);
        conto.setText(null,null);
        desc.setText(null, null);
        luogo.setText(null,null);
        row_luogo.setVisibility(View.GONE);
        rd_luogo_a.setChecked(false);
        rd_luogo_m.setChecked(false);
        spinner_mezzo.setSelection(0);
        spinner_cat.setSelection(0);
        ripet.setChecked(false);
        data.setText(formatted);
        view_foto.setImageDrawable(null);

    }

    /*Popola lo spinner con l'elenco delle categorie
    * @param spinner: spinner da riempire
    * @param op_type: tipo di operazione (entrata o pagamento)
    */
    public void riempiSpinnerCat(Spinner spinner, int op_type){
        /*Creo l'adapter per lo spinner*/
        ArrayAdapter adapter = new ArrayAdapter<Categoria>(this, R.layout.my_spinner);
        adapter.setDropDownViewResource(R.layout.my_spinner_drop);
        Cursor c;
        /*Popolo lo spinner*/
        spinner.setAdapter(adapter);
        c = db.query(dataBase_string.TBL_CATEGORIA);

        try{
            while (c.moveToNext()){
                if((c.getInt(2)) == op_type){ /*Inserisco solo le categorie per questo tipo di transazione*/
                    Categoria cat = new Categoria(c.getString(1),c.getInt(0));
                    adapter.add(cat);
                }
            }
        }
        finally{
            c.close();
        }
    }

    /*Gestisce la selezione dei radio button relativi al luogo*/
    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_corrente:
                if (checked){
                    RadioButton rd_manual = (RadioButton) findViewById(R.id.radio_manuale);
                    if(rd_manual.isChecked()){
                        rd_manual.setChecked(false);
                    }
                    mLocationClient.connect();
                }
                else{
                    mLocationClient.disconnect();
                    EditText txt_luogo = (EditText) findViewById(R.id.txt_luogoOperazione);
                    txt_luogo.setText("");
                }

                break;
            case R.id.radio_manuale:
                if (checked){
                    RadioButton rd_corrente = (RadioButton) findViewById(R.id.radio_corrente);
                    if(rd_corrente.isChecked()){
                        rd_corrente.setChecked(false);
                        mLocationClient.disconnect();
                    }
                    showDialogLuogo();
                }
                else{
                    RadioButton rd_manual = (RadioButton) findViewById(R.id.radio_manuale);
                    rd_manual.setChecked(false);
                    EditText txt_luogo = (EditText) findViewById(R.id.txt_luogoOperazione);
                    txt_luogo.setText("");
                }
                break;
        }
    }

    /*Mostra la Dialog per la ricerca manuale del luogo*/
    public void showDialogLuogo(){
        d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(false);
        d.setContentView(R.layout.dialog_cercaluogo);
        Button bt_cercaLuogo = (Button) d.findViewById(R.id.bt_cercaluogoOperazione);
        Button bt_annulla = (Button) d.findViewById(R.id.bt_annullaLuogo);
        d.show();

        bt_annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton rd_manual = (RadioButton) findViewById(R.id.radio_manuale);
                rd_manual.setChecked(false);
                TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
                row.setVisibility(View.GONE);
                d.dismiss();
            }
        });

        bt_cercaLuogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                task = new cercaLuogo().execute();
            }
        });






    }

    private class cercaLuogo extends AsyncTask<Void, Void, Integer> {
        JSONObject jsn_obj;
        @Override
        protected Integer doInBackground(Void... params) {
            EditText txt_luogo = (EditText) d.findViewById(R.id.txt_cercaluogoOperazione);
            String indirizzo = txt_luogo.getText().toString();
            jsn_obj = getLatLongFromGivenAddress(indirizzo);
            return 1;
        }

        protected void onProgressUpdate(Integer... progress) {

        }
        @Override
        protected void onPostExecute(Integer in) {
            JSONArray results = null;
            try {
                results = jsn_obj.getJSONArray("results");
                elenco_indirizzi = new JSONArray();
                for (int i = 0; i < results.length(); i++) {
                   try {
                        JSONObject address_components = results.getJSONObject(i);
                        String indirizzo,latitudine, longitudine;
                        JSONObject geometry = address_components.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        indirizzo = address_components.getString("formatted_address");
                        latitudine = location.getString("lat");
                        longitudine = location.getString("lng");
                        JSONObject indirizzo_completo = new JSONObject();
                        indirizzo_completo.put("indirizzo",indirizzo);
                        indirizzo_completo.put("lat", latitudine);
                        indirizzo_completo.put("lng",longitudine);
                        elenco_indirizzi.put(indirizzo_completo);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                creaTabellaLuoghi(elenco_indirizzi);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*Crea la tabella con l'elenco dei luoghi
    * @param jsna: Array JSON con l'elenco dei luoghi da inserire in tabella
    * */
    public void creaTabellaLuoghi(JSONArray jsna) throws JSONException {
        TableLayout tbl = (TableLayout) d.findViewById(R.id.tbl_elencoLuoghi);
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        llp.setMargins(5, 0, 5, 5);//left top right bottom
        tbl.removeAllViews();
        if(jsna.length()>0){
            TextView lb_selezionaLuogo = (TextView) d.findViewById(R.id.lb_selezionaLuogo);
            lb_selezionaLuogo.setVisibility(View.VISIBLE);
            for(int i=0;i<jsna.length();i++){
                TableRow row = new TableRow(this);
                TextView txt = new TextView(this);
                JSONObject jsnobj = jsna.getJSONObject(i);
                txt.setText(jsnobj.getString("indirizzo"));
                txt.setTextAppearance(this, R.style.text_lista_operazioni);
                row.setId(i);
                row.setLayoutParams(llp);
                row.addView(txt);
                row.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
                    @Override
                    public void onClick(View view) {
                        id_luogo_selezionato = view.getId();
                        try{
                            luogo_latitudine = (elenco_indirizzi.getJSONObject(id_luogo_selezionato).getString("lat"));
                            luogo_longitudine = (elenco_indirizzi.getJSONObject(id_luogo_selezionato).getString("lng"));
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TableRow row = (TableRow) findViewById(R.id.row_luogoOpAutomatico);
                        row.setVisibility(View.VISIBLE);
                        d.dismiss();
                    }
                });
                tbl.addView(row);
            }
        }
        else{
            TextView lb_selezionaLuogo = (TextView) d.findViewById(R.id.lb_selezionaLuogo);
            lb_selezionaLuogo.setVisibility(View.INVISIBLE);
            TableRow row = new TableRow(this);
            TextView txt = new TextView(this);
            txt.setText("Nessun risultato trovato");
            txt.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            row.addView(txt);
            tbl.addView(row);
        }


    }


}
