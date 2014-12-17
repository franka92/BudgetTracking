package giulia.budgettracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import giulia.budgettracking.dataBase;
/**
 * Created by frank_000 on 11/07/14.
 */
public class databBaseManager {

    private dataBase database;
    private SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    /*Costruttore*/
    public databBaseManager(Context ctx){
        database=new dataBase(ctx);
    }

    /*Salva un nuovo account
    * @param n: nome del conto
    * @param c: valore del conto
    * @param cIniziale: conto iniziale
    * @param desc: descrizione associata al conto (opzionale)
    */
    public void salvaAccount(String n, Double c, Double cIniziale , String desc){
        /*Recupera un riferimento al database che ne permette anche la modifica*/
        SQLiteDatabase db = database.getWritableDatabase();
        /*Riempo la tabella*/
        ContentValues cv = new ContentValues();
        cv.put(dataBase_string.A_FIELD_ID,1);
        cv.put(dataBase_string.A_FIELD_NAME, n);
        cv.put(dataBase_string.A_FIELD_DESC, desc);
        cv.put(dataBase_string.A_FIELD_CONTO, c);
        cv.put(dataBase_string.A_FIELD_CONTOINIZIALE, cIniziale);
        try{
            db.insert(dataBase_string.TBL_ACCOUNT, null,cv);
        }
        catch (SQLiteException sqle){
            System.out.println("Errore nella creazione di un nuovo account.");
        }
    }

    /*Salva una nuova operazione
    * @param id: id dell'account a cui l'operazione è associata
    * @param ogg: oggetto (nome) associato all'operazione
    * @param imp: importo associato all'operazione
    * @param cat: categoria associata all'operazione
    * @param data: data in cui è stata effettuata l'operazione
    * @param latitudine: latitudine del luogo
    * @param longitudine: longitudine del luogo
    * @param desc: descrizione associata all'operazione (opzionale)
    * @param mezzo: mezzo di transazione con cui è stata effettuata l'operazione
    * @param tipo: tipo di operazione effettuata (entrata o uscita)
    * @param foto: foto associata all'operazione effettuata (opzionale)
    */
    public void salvaOperazione(int id ,String ogg, Double imp, int cat, String data, String latitudine, String longitudine, String desc, String mezzo , int tipo, String photo_uri, int pianificazione){
        SQLiteDatabase db = database.getWritableDatabase();
        /*Riempo la tabella*/
        ContentValues cv = new ContentValues();
        cv.put(dataBase_string.O_FIELD_IDACCOUNT, id);
        cv.put(dataBase_string.O_FIELD_OGGETTO, ogg);
        cv.put(dataBase_string.O_FIELD_IMPORTO, imp);
        cv.put(dataBase_string.O_FIELD_CATEGORIA, cat);
        cv.put(dataBase_string.O_FIELD_DATA, data);
        cv.put(dataBase_string.O_FIELD_DESC, desc);
        cv.put(dataBase_string.O_FIELD_MEZZO, mezzo);
        cv.put(dataBase_string.O_FIELD_TIPO, tipo);
        cv.put(dataBase_string.O_FIELD_LUOGO_LAT, latitudine);
        cv.put(dataBase_string.O_FIELD_LUOGO_LON,longitudine);
        cv.put(dataBase_string.O_FIELD_FOTO,photo_uri);
        cv.put(dataBase_string.O_FIELD_FUTURA,pianificazione);
        try{
            db.insert(dataBase_string.TBL_OPERAZIONI, null,cv);
        }
        catch (SQLiteException sqle){
            System.out.println("Errore nell'inserimento di una nuova operazione");
        }
    }
    /*Salva una nuova operazione periodica
    * @param id: id associato all'operazione (chiave esterna, relativa ad un'operazione inserita)
    * @param tipo: tipo di operazione (entrata o uscita)
    * @param periodo: ogni quanto l'operazione deve essere ripetuta
    * @param data: data in cui viene effettuata l'operazione
    * @param next: data successiva in cui l'operazione verrà ripetuta
    */
    public long salvaPeriodica(int id , int tipo , int periodo, String data , String next){
        SQLiteDatabase db = database.getWritableDatabase();
        /*Riempo la tabella*/
        ContentValues cv = new ContentValues();
        cv.put(dataBase_string.P_FIELD_IDOP,id);
        cv.put(dataBase_string.P_FIELD_TIPO, tipo);
        cv.put(dataBase_string.P_FIELD_PERIODO, periodo);
        cv.put(dataBase_string.P_FIELD_DATA, data);
        cv.put(dataBase_string.P_FIELD_NEXT, next);
        try{
            long result = db.insert(dataBase_string.TBL_PERIODICA, null,cv);
            return result;
        }
        catch (SQLiteException sqle){
            System.out.println("Errore nell'inserimento di un'operazione periodica");
        }
        return 0;
    }

    /*Aggiorna il saldo del conto dopo ogni operazione inserita
    * @param importo: importo da scalare/aggiungere al conto
    * @param tipo: tipo di operazione che stiamo considerando
    */
    public void aggiornaConto(Double importo, int tipo){
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cur_account = recuperaAccount();
        cur_account.moveToFirst();

        if(tipo == 0){ /*Entrata*/
            ContentValues cv = new ContentValues();
            cv.put(dataBase_string.A_FIELD_CONTO,cur_account.getDouble(2)+importo);
            db.update(dataBase_string.TBL_ACCOUNT, cv, dataBase_string.A_FIELD_ID + "=" + 1, null);

        }
        else{ /*Uscita*/
            ContentValues cv = new ContentValues();
            cv.put(dataBase_string.A_FIELD_CONTO,cur_account.getDouble(2)-importo);
            db.update(dataBase_string.TBL_ACCOUNT, cv, dataBase_string.A_FIELD_ID + "=" + 1, null);
        }
        cur_account.close();
    }
    /*Modifica l'operazione programmata e la setta come già eseguita
    * @param id: l'id dell'operazione da modificare
    */
    public void modificaProgrammata(int id){
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor c = findById(dataBase_string.TBL_OPERAZIONI,dataBase_string.O_FIELD_ID,id);
        if(c!=null){
            c.moveToNext();
            Double importo = c.getDouble(2);
            int tipo = c.getInt(8);
            aggiornaConto(importo,tipo);
            String filtro = dataBase_string.O_FIELD_ID+"=" + id;
            ContentValues args = new ContentValues();
            args.put(dataBase_string.O_FIELD_FUTURA,0);
            db.update(dataBase_string.TBL_OPERAZIONI, args, filtro, null);
            c.close();
        }


    }
    /*Modifica la data in cui effettuare nuovamente l'operazione e inserisce una copia dell'operazione nella tabella delle operazioni
    * già effettuate
    * @param id: id dell'operazione da modificare
    */
    public void modificaPeriodica(int id){
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor c = findById(dataBase_string.TBL_PERIODICA,dataBase_string.P_FIELD_ID,id);
        if(c.moveToNext()){
            int id_primaOP = c.getInt(1);
            int periodo = c.getInt(5);
            String data = c.getString(4);
            Cursor c_info = findById(dataBase_string.TBL_OPERAZIONI,dataBase_string.O_FIELD_ID,id_primaOP);
            if(c_info.moveToNext()){
                String oggetto = c_info.getString(1);
                Double importo = c_info.getDouble(2);
                int cat = c_info.getInt(3);
                String desc = c_info.getString(5);
                String mezzo = c_info.getString(7);
                int tipo = c_info.getInt(8);
                String latitudine = c_info.getString(9);
                String longitudine = c_info.getString(10);
                String photo_uri = c_info.getString(11);
                aggiornaConto(importo,tipo);
                salvaOperazione(1,oggetto,importo,cat,data,latitudine,longitudine,desc,mezzo,tipo,photo_uri,0);
                c_info.close();
            }
            String next_date = funzioni.nextDate(periodo, data);
            String filtro = dataBase_string.P_FIELD_ID+"="+ id;
            ContentValues args = new ContentValues();
            args.put(dataBase_string.P_FIELD_NEXT,next_date);
            db.update(dataBase_string.TBL_PERIODICA, args, filtro, null);
        }


    }

    /*Cancellazione in base all'id dell'account
    *@param id: id dell'account che si vuole eliminare
    */
    public boolean deleteAccount(long id){
        SQLiteDatabase db=database.getWritableDatabase();
        try{
            db.delete(dataBase_string.TBL_OPERAZIONI,dataBase_string.A_FIELD_ID+"=?", new String[]{Long.toString(id)});
            db.delete(dataBase_string.TBL_PERIODICA,dataBase_string.A_FIELD_ID+"=?", new String[]{Long.toString(id)});
            db.delete(dataBase_string.TBL_CATEGORIA,dataBase_string.A_FIELD_ID+"=?", new String[]{Long.toString(id)});
            if (db.delete(dataBase_string.TBL_ACCOUNT, dataBase_string.A_FIELD_ID+"=?", new String[]{Long.toString(id)})>0)
                return true;
            return false;
        }
        catch (SQLiteException sqle){
            return false;
        }

    }
    /*Recupera le informazioni relative all'account*/
    public Cursor recuperaAccount(){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            String q = "SELECT * FROM " + dataBase_string.TBL_ACCOUNT + " WHERE " + dataBase_string.A_FIELD_ID  + " ="+ 1 + ";";
            c = db.rawQuery(q, null);

        }
        catch(SQLiteException sqle){
            return null;
        }

        return c;
    }
    /*Recupera le informazioni relative alle ultime 5 operazioni (in ordine di tempo, non di inserimento)*/
    public Cursor recuperaUltimiMovimenti(){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            String q = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI +" WHERE "+dataBase_string.O_FIELD_FUTURA+"=0 ORDER BY " + dataBase_string.O_FIELD_DATA +" DESC LIMIT 5;";
            c = db.rawQuery(q, null);


        }
        catch(SQLiteException sqle){
            return null;
        }

        return c;
    }
    /*Recupera tutti i movimenti in ordine di data*/
    public Cursor recuperaTuttiMovimenti(){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            String q = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI + " WHERE " +dataBase_string.O_FIELD_FUTURA+"=0 ORDER BY " + dataBase_string.O_FIELD_DATA +" DESC;";
            c = db.rawQuery(q, null);


        }
        catch(SQLiteException sqle){
            return null;
        }

        return c;
    }
    /*Recupera l'ultimo inserimento della tabella indicata
    * @param tbl_name: nome della tabella
    * @param field: nome del campo su cui si vuole effettuare un ordinamento
    */
    public Cursor recuperaUltimoInserimento(String tbl_name, String field){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            String q = "SELECT * FROM " + tbl_name + " ORDER BY " + field  + " DESC LIMIT 1;";
            c = db.rawQuery(q, null);
        }
        catch (SQLiteException sqle){
            return null;
        }
        return c;
    }
    /*Recupera l'elenco delle operazioni pianificate per la data corrente*/
    public Cursor recuperaOperazioniPianificate(){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            Calendar cal = Calendar.getInstance();
            String data_attuale = date_format.format(cal.getTime());
            String q = "SELECT * FROM "+ dataBase_string.TBL_OPERAZIONI +" WHERE "+dataBase_string.O_FIELD_FUTURA+"=1 AND "+dataBase_string.O_FIELD_DATA+" <='"+data_attuale+"';";
            c = db.rawQuery(q, null);
        }
        catch (SQLiteException sqle){
            return null;
        }
        return c;
    }
    /*Recupera l'elenco delle operazioni periodiche programmate per la data corrente*/
    public Cursor recuperaOperazioniPeriodiche(){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            Calendar cal = Calendar.getInstance();
            String data_attuale = date_format.format(cal.getTime());
            String q = "SELECT * FROM "+ dataBase_string.TBL_PERIODICA +" WHERE "+dataBase_string.P_FIELD_NEXT+" <='"+data_attuale+"';";
            c = db.rawQuery(q, null);
        }
        catch (SQLiteException sqle){
            return null;
        }
        return c;
    }


    /*Recupera l'intero contenuto di una tabella
    *@param tbl_name: nome della tabella
    */
    public Cursor query(String tbl_name)
    {
        Cursor crs = null;
        try{
            SQLiteDatabase db = database.getReadableDatabase();
            crs=db.query(tbl_name, null, null, null, null, null, null, null);
        }
        catch(SQLiteException sqle){
            return null;
        }
        return crs;
    }
    /*Recupera l'elemento che corrisponde all'id ricercato
    * @param tbl_name: nome della tabella
    * @param id_field: nome del campo in cui si effettua la ricerca
    * @param id: id che si vuole ricercare
    */
    public Cursor findById(String tbl_name, String id_field, int id){
        Cursor c = null;
        try{
            SQLiteDatabase db = database.getReadableDatabase();
            String q = "SELECT * FROM "+ tbl_name + " WHERE " + id_field + "=" + id +";";
            Log.i("query",q);
            c = db.rawQuery(q, null);
        }
        catch(SQLiteException sqle){
            return null;
        }
        return c;
    }

    /*Cancellazione in base all'id dell'operazione
    * @param id: id dell'operazione che si vuole cancellare
    */
    public boolean deleteOperation(int id){
        SQLiteDatabase db=database.getWritableDatabase();

        try{
            if (db.delete(dataBase_string.TBL_OPERAZIONI, dataBase_string.O_FIELD_ID+"=?", new String[]{Long.toString(id)})>0){
                /*Se l'operazione è periodica, elimino anche il riferimento nella relativa tabella*/
                db.delete(dataBase_string.TBL_PERIODICA, dataBase_string.P_FIELD_IDOP+"=?", new String[]{Long.toString(id)});
                return true;
            }
            return false;
        }
        catch (SQLiteException sqle){
            return false;
        }

    }
    /*Restituisce il totale delle entrate/uscite rispetto agli ultimi 30 giorni
    * @param type: tipo di operazione che si vuole considerare (entrata o uscita)
    */
    public Cursor countType(int type){
        Cursor c = null;
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH,-1);
        String data_limite = date_format.format(cal.getTime());
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            String q = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI + " WHERE " +dataBase_string.O_FIELD_FUTURA+"=0 AND "+ dataBase_string.O_FIELD_TIPO + " ="+ type + " AND "+dataBase_string.O_FIELD_DATA+" > '"+data_limite+"';";
            c = db.rawQuery(q, null);


        }
        catch(SQLiteException sqle){
            return null;
        }
        return c;
    }

    /*Recupera l'elenco delle spese
    * @param date: data relativa alle spese da recuperare
    * @param type: tipo di elenco da recuperare (giornaliero,settimanale,mensile..)
    */
    public Cursor getElencoSpese(String date, int type){
        Cursor c = null;
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String query = null;
        if(type == 0){/*Elenco per giorno*/
            query = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI + " WHERE "+dataBase_string.O_FIELD_FUTURA+"=0 AND " +dataBase_string.O_FIELD_DATA+" = '"+date+"';";
        }
        else if (type == 1){/*Elenco per settimana*/
            int day_week = cal.get(Calendar.DAY_OF_WEEK);
            int lun = Calendar.MONDAY;
            String data_inizio;
            String data_fine = date_format.format(cal.getTime());
            while(day_week != lun){
                cal.add(Calendar.DAY_OF_MONTH,-1);
                day_week--;
            }
            data_inizio = date_format.format(cal.getTime());
            query = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI + " WHERE "+dataBase_string.O_FIELD_FUTURA+"=0 AND " +dataBase_string.O_FIELD_DATA+" >= '"+data_inizio+"' AND " +dataBase_string.O_FIELD_DATA+" <='"+data_fine+"';";
        }
        else if(type == 2){/*Elenco per mese*/
            int month = cal.get(Calendar.MONTH)+1;
            int year = cal.get(Calendar.YEAR);
            String data_inizio = year+"-"+month+"-01";
            if(month <10){
                data_inizio = year+"-"+String.format("%07d", month)+"-01";
            }
            String data_fine = date_format.format(cal.getTime());
            query = "SELECT * FROM " + dataBase_string.TBL_OPERAZIONI + " WHERE "+dataBase_string.O_FIELD_FUTURA+"=0 AND " +dataBase_string.O_FIELD_DATA+" >= '"+data_inizio+"' AND " +dataBase_string.O_FIELD_DATA+" <='"+data_fine+"';";
        }
        else{
            return null;
        }

        try{
            SQLiteDatabase db = database.getWritableDatabase();
            c = db.rawQuery(query, null);
        }
        catch(SQLiteException sqle){
            return null;
        }
        return c;
    }

    /*Cancella tutto il database*/
    public int deleteAll(){
        try{
            SQLiteDatabase db = database.getWritableDatabase();
            db.delete(dataBase_string.TBL_ACCOUNT, null, null);
            db.delete(dataBase_string.TBL_OPERAZIONI, null, null);
            db.delete(dataBase_string.TBL_PERIODICA, null, null);
            return 1;
        }
        catch(SQLiteException sqle){
            return 0;
        }
    }


}