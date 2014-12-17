package giulia.budgettracking;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by frank_000 on 15/10/2014.
 */
public class Operazione {
    private databBaseManager db = null;
    Context context;
    TableLayout tabella = null;
    char tipo_visualizzazione;
    String data_filtro = null;
    Double totale_entrate;
    Double totale_uscite;

    public Operazione(Context cxt){
        db = new databBaseManager(cxt);
        this.context = cxt;
        totale_entrate = totale_uscite = 0.0;
    }

    /*Mostra la dialog con le informazioni relative all'operazione
    * @param id: id dell'operazione da mostrare
    * */
    public void showDialogInfoOperazione(final int id){
        final Dialog d;
        Cursor c = null;
        d = new Dialog(context);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(false);				//Toccando il display al di fuori della finestra essa non si chiuderà
        d.setContentView(R.layout.dialog_dettaglioperaz);	//Assegnamo un layout alla finestra
        c = db.findById(dataBase_string.TBL_OPERAZIONI,dataBase_string.O_FIELD_ID,id);
        if(c != null){
            c.moveToNext();
            final Double importo = c.getDouble(2);
            final int tipo_operazione = c.getInt(8);
            final TextView nome = (TextView) d.findViewById(R.id.txt_dettNomeOp);
            final TextView costo = (TextView) d.findViewById(R.id.txt_dettImportoOp);
            final TextView data = (TextView) d.findViewById(R.id.txt_dettDataOp);
            final TextView descrizione = (TextView) d.findViewById(R.id.txt_dettDescrizioneOp);
            final TextView categoria = (TextView) d.findViewById(R.id.txt_dettCategoriaOp);
            final TextView tipo = (TextView) d.findViewById(R.id.txt_dettTipoOp);
            final TextView mezzo = (TextView) d.findViewById(R.id.txt_dettMezzoOp);
            final TextView txt_luogo = (TextView) d.findViewById(R.id.txt_dettLuogoOp);
            Cursor crs_cat = db.findById(dataBase_string.TBL_CATEGORIA, dataBase_string.C_FIELD_ID,c.getInt(3));
            if(crs_cat != null){
                crs_cat.moveToNext();
                nome.setText(c.getString(1)); //Nome operazione
                costo.setText(importo.toString() +"€"); //Importo
                data.setText(c.getString(4)); //Data
                categoria.setText(crs_cat.getString(1)); //Categoria
                if(c.getString(5).length() >0){
                    descrizione.setText(c.getString(5));    //Descrizione
                }
                mezzo.setText(c.getString(7));  //Mezzo
                if( tipo_operazione == 0){
                    tipo.setText("Entrata");
                }
                else{
                    tipo.setText("Spesa");
                }
                final String latitude = c.getString(9);
                final String longitude = c.getString(10);
                String location = "";
                Button bt_google = (Button) d.findViewById(R.id.bt_googleMaps);
                if(latitude.length() > 0){
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                        if (null != listAddresses && listAddresses.size() > 0) {
                            location = listAddresses.get(0).getAddressLine(0);
                            txt_luogo.setText(location.toString());
                            bt_google.setVisibility(View.VISIBLE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    txt_luogo.setText("-");
                    bt_google.setVisibility(View.GONE);
                }

                if(c.getString(11) != null){/*C'è una foto associata all'operazione*/
                    String photo_path = c.getString(11);
                    ImageView view_foto = (ImageView) d.findViewById(R.id.view_fotoOperazione);
                    view_foto.setImageURI(Uri.parse(new File(photo_path).toString()));
                    File photo = new File(photo_path);
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath(),bitmapOptions);
                    BitmapFactory.Options bounds = new BitmapFactory.Options();
                    bounds.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photo.getAbsolutePath(), bounds);
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(photo.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

                    int rotationAngle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getWidth() / 2);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
                    view_foto.setImageBitmap( Bitmap.createScaledBitmap(rotatedBitmap, 300,500, false));

                }
                /*L'operazione è periodica?*/
                Cursor crs_p = db.findById(dataBase_string.TBL_PERIODICA,dataBase_string.P_FIELD_IDOP,id);
                if(crs_p.moveToNext()){/*E' periodica*/
                    TextView ripetizione = (TextView) d.findViewById(R.id.txt_dettRipeteOp);
                    ripetizione.setText(crs_p.getString(4));
                }
                crs_p.close();
                crs_cat.close();
                c.close();

                d.show();

                funzioni.gestisciClickChiudiDialog(d);

                /*Elimina la voce selezionata*/
                Button bt_elimina = (Button) d.findViewById(R.id.bt_eliminaOperazione);
                bt_elimina.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*Se c'era una foto associata all'operazione, la elimino dalla memoria esterna*/
                        Cursor c = db.findById(dataBase_string.TBL_OPERAZIONI, dataBase_string.O_FIELD_ID, id);
                        if(c.moveToNext()){
                            String path = c.getString(11);
                            if(path!= null){
                                File file = new File(path);
                                boolean deleted = file.delete();
                            }
                        }
                        /*Elimino l'operazione e aggiorno il conto*/
                        db.deleteOperation(id);

                        db.aggiornaConto(importo,(1-tipo_operazione)%2);
                        d.setCanceledOnTouchOutside(true);
                        d.dismiss();
                        /*Aggiorno la tabella con la lista delle operazioni*/
                        if(tipo_visualizzazione == 'l'){
                            mostraUltimeOperazioni(tabella,tipo_visualizzazione);
                        }
                        else{
                            mostraOperazioni(tabella,tipo_visualizzazione,data_filtro);
                        }


                    }
                });

                bt_google.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       /*Apre Google Maps centrato dalla latitudine e longitudine*/
                        String uri = "geo:<lat>,<long>?q=<"+latitude+">,<"+longitude+">";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                });



            }
            else{
                //Gestione errore
            }


        }
        else{
            //Gestione errore
        }

    }


    /*Mostra l'elenco delle operazioni
    * @param tbl: tabella da riempire
    * @param type: tipo di elenco da creare (giornaliero, settimanale, mensile..)
    * @param date: data a cui fare riferimento (utilizzata solo se type=giornaliero)
    */
    public void mostraOperazioni(TableLayout tbl, char type, String date){
        tabella = tbl;
        tipo_visualizzazione = type;
        data_filtro = date;
        Cursor crs = getList(type);
        creaTabella(crs,tbl);
    }

    /*Mostra l'elenco delle ultime operazioni
    * @param tbl: tabella da riempire
    * @param type: tipo di elenco da creare/visualizzare
    * */
    public void mostraUltimeOperazioni(TableLayout tbl, char type){
        tabella = tbl;
        tipo_visualizzazione = type;
        Cursor crs = db.recuperaUltimiMovimenti();
        creaTabella(crs,tbl);
    }

    /*Recupera l'elenco delle operazioni
    * @param type: tipo di elenco da recuperare
    * */
    public Cursor getList(char type){
        Cursor c = null;
        switch(type){
            case 'd':
                c = db.getElencoSpese(data_filtro, 0);
                break;
            case 'w':
                c = db.getElencoSpese(null, 1);
                break;
            case 'm':
                c = db.getElencoSpese(null, 2);
                break;
            case 'a':
                c = db.recuperaTuttiMovimenti();
                break;
        }
        return c;
    }

    /*Crea la tabella con l'elenco delle operazioni*/
    public void creaTabella(Cursor crs, TableLayout tbl){
        /*Recupero le textView in cui settare il totale dei pagamenti/accrediti*/
        ViewGroup vgroup = (ViewGroup) tbl.getParent().getParent();
        TableLayout table_totale = null;
        TextView txt_totale_entrate = null, txt_totale_uscite = null;
        View padre = (View)tbl.getParent();
        TableRow row_button = null;
        if(tipo_visualizzazione != 'l'){
            table_totale  = (TableLayout) vgroup.getChildAt(vgroup.indexOfChild(padre)+1);
            txt_totale_entrate = (TextView) table_totale.findViewById(R.id.txt_total_value);
            txt_totale_uscite = (TextView) table_totale.findViewById(R.id.txt_total2_value);
            tbl.removeAllViews();
        }
        else{
            while (tbl.getChildCount() > 1)
                tbl.removeView(tbl.getChildAt(0));
            row_button = (TableRow) tbl.getChildAt(0);
            tbl.removeView(tbl.getChildAt(0));
        }

        LayoutParams llp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if(crs.getCount()>=1){/*Crea Intestazione tabella*/
            TableRow row_intestazione = new TableRow(context);
            TextView nome_intestazione = new TextView(context);
            TextView data_intestazione = new TextView(context);
            TextView importo_intestazione = new TextView(context);
            TextView arrow_intestazione = new TextView(context);
            arrow_intestazione.setText("Tipo");
            nome_intestazione.setText("Oggetto");
            data_intestazione.setText("Data");
            importo_intestazione.setText("Importo");


            llp.setMargins(20, 0, 20, 0);//left top right bottom

            arrow_intestazione.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            nome_intestazione.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            data_intestazione.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            importo_intestazione.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);

            arrow_intestazione.setLayoutParams(llp);
            nome_intestazione.setLayoutParams(llp);
            data_intestazione.setLayoutParams(llp);
            importo_intestazione.setLayoutParams(llp);

            row_intestazione.addView(arrow_intestazione);
            row_intestazione.addView(nome_intestazione);
            row_intestazione.addView(data_intestazione);
            row_intestazione.addView(importo_intestazione);
            row_intestazione.setGravity(Gravity.CENTER);
            tbl.addView(row_intestazione);
            if(tipo_visualizzazione != 'l') table_totale.setVisibility(View.VISIBLE);
        }
        else{
            TableRow row_nothing = new TableRow(context);
            TextView row_contenuto = new TextView(context);
            row_contenuto.setText("Non ci sono operazioni da visualizzare");
            row_contenuto.setTextColor(Color.WHITE);
            row_nothing.addView(row_contenuto);
            row_nothing.setGravity(Gravity.CENTER);
            tbl.addView(row_nothing);
            if(tipo_visualizzazione != 'l') table_totale.setVisibility(View.INVISIBLE);
        }

        while(crs.moveToNext()){
            TableRow row = new TableRow(context);
            TextView nome = new TextView(context);
            TextView data = new TextView(context);
            TextView importo = new TextView(context);
            ImageView arrow = new ImageView(context);
            Map<String, Integer> map = new HashMap<String, Integer>();

            nome.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            nome.setLayoutParams(llp);
            data.setLayoutParams(llp);
            data.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);
            importo.setLayoutParams(llp);
            importo.setTextAppearance(tbl.getContext(), R.style.text_lista_operazioni);



            Double imp = crs.getDouble(2);
            final int id = crs.getInt(0);

            nome.setText(crs.getString(1));
            data.setText(crs.getString(4));

            if(crs.getInt(8) == 1){//Pagamento
                map.put("arrow", R.drawable.arrow_down);
                importo.setText("-" + imp.toString() + "€");
                nome.setTextAppearance(context, R.style.tipo_pagamento);
                data.setTextAppearance(context,R.style.tipo_pagamento);
                importo.setTextAppearance(context,R.style.tipo_pagamento);
                totale_uscite+=imp;
            }
            else{//Entrata
                map.put("arrow", R.drawable.arrow_up);
                importo.setText("+" + imp.toString() + "€");
                nome.setTextAppearance(context,R.style.tipo_entrata);
                data.setTextAppearance(context,R.style.tipo_entrata);
                data.setTextAppearance(context,R.style.tipo_entrata);
                importo.setTextAppearance(context,R.style.tipo_entrata);
                totale_entrate+=imp;
            }
            arrow.setImageResource(map.get("arrow"));
            row.addView(arrow);
            row.addView(nome);
            row.addView(data);
            row.addView(importo);
            row.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
                @Override
                public void onClick(View view) {
                    showDialogInfoOperazione(id);
                }
            });
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            row.setVerticalGravity(Gravity.CENTER_VERTICAL);
            tbl.addView(row);

        }
        if(tipo_visualizzazione != 'l') {
            txt_totale_entrate.setText(totale_entrate.toString() + "€");
            txt_totale_uscite.setText(totale_uscite.toString() + "€");
        }
        else{
            tbl.addView(row_button);
            if(crs.getCount()>= 5){
                tbl.findViewById(R.id.bt_mostraTutteOp).setVisibility(View.VISIBLE);
            }
            else{
                tbl.findViewById(R.id.bt_mostraTutteOp).setVisibility(View.INVISIBLE);
            }
        }
        crs.close();
        tbl.setGravity(Gravity.CENTER_HORIZONTAL);
    }

}
