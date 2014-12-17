package giulia.budgettracking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank_000 on 11/07/14.
 *
 * Per questioni “organizzative” del codice, i nomi dei campi e della tabella del database sono stati definiti in costanti in questa classe
 */
public class dataBase_string {

    public static final String DBNAME="bdgtrck.db";
    /* --- TABELLA ACCOUNT --- */
    public static final String TBL_ACCOUNT="account";
    public static final String A_FIELD_ID="_id";
    public static final String A_FIELD_NAME="nome";
    public static final String A_FIELD_DESC="descrizione";
    public static final String A_FIELD_CONTO="conto";
    public static final String A_FIELD_CONTOINIZIALE ="conto_iniziale";

    /* --- TABELLA OPERAZIONI --- */
    public static final String TBL_OPERAZIONI = "operazioni";
    public static final String O_FIELD_ID = "_id";
    public static final String O_FIELD_OGGETTO = "oggetto";
    public static final String O_FIELD_IMPORTO = "importo";
    public static final String O_FIELD_CATEGORIA = "categoria"; /*casa, lavoro, hobby, istruzione, trasporti , abbigliamento , salute, vacanza, animali,  bollette, multe, altro*/
    public static final String O_FIELD_DATA = "data";
    public static final String O_FIELD_LUOGO_LAT = "luogo_lat";         /*opzionale per l'utente*/
    public static final String O_FIELD_LUOGO_LON = "luogo_lon";
    public static final String O_FIELD_DESC = "descrizione";    /*opzionale per l'utente*/
    public static final String O_FIELD_FOTO = "foto";   /*[NOTA] implementazione opzionale*/
    public static final String O_FIELD_MEZZO ="mezzo";  /*Contanti, carta di credito, assegno, altro*/
    public static final String O_FIELD_TIPO = "tipo_operazione"; /*Entrata 0 - Spesa 1*/
    public static final String O_FIELD_FUTURA ="pianificata"; /*Se l'operazione è pianificata per il futuro*/
    public static final String O_FIELD_IDACCOUNT="idAccount";


    /* --- TABELLA CATEGORIE --- */
    public static final String TBL_CATEGORIA = "categoria";
    public static final String C_FIELD_ID = "_id";
    public static final String C_FIELD_NOME = "nome";
    public static final String C_FIELD_TIPO ="tipo";    /* 1 = pagamento , 2 = entrata */
    public static final int CAT_PAGAMENTO = 1;
    public static final int CAT_ENTRATA = 0;

    /* --- TABELLA OPERAZIONI PERIODICHE/FUTURE --- */
    public static final String TBL_PERIODICA = "operazione_periodica";
    public static final String P_FIELD_ID = "_id";
    public static final String P_FIELD_IDOP ="idOperazione";
    public static final String P_FIELD_PERIODO ="periodo"; /* 0 --> giornaliera , 1 --> settimanale , 2 --> mensile*/
    public static final String P_FIELD_DATA = "data";
    public static final String P_FIELD_NEXT = "next_data";
    public static final String P_FIELD_TIPO = "tipo_operazione"; /*Entrata 0 - Spesa 1*/

    public static final int PER_GIORNO = 0;
    public static final int PER_SETTIM = 1;
    public static final int PER_MENSILE= 2;



}




