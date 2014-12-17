package giulia.budgettracking;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ArrayAdapter;

import java.lang.reflect.Array;

/**
 * Created by frank_000 on 11/07/14.
 *
 * Classe per la creazione del database usato dall'applicazione.
 * Crea due tabelle:
 * 1.Account: tabella in cui saranno contenuti i dati relativi all'account creato
 * 2.Spesa: tabella in cui saranno contenuti i dati relativi a ciascuna spesa
 *
 */
public class dataBase extends SQLiteOpenHelper {


    /*Costruttore*/
    public dataBase(Context context) {
        super(context, dataBase_string.DBNAME, null, 1);
    }
    /*viene invocato nel momento in cui non si trova nello spazio dell’applicazione un database con nome indicato nel costruttore.*/
    @Override
    public void onCreate(SQLiteDatabase db){
        /*Creo la tabella per l'account*/
        String q="CREATE TABLE "+dataBase_string.TBL_ACCOUNT+
                " ( " + dataBase_string.A_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                dataBase_string.A_FIELD_NAME + " TEXT not null," +
                dataBase_string.A_FIELD_CONTO + " REAL not null," +
                dataBase_string.A_FIELD_CONTOINIZIALE + " REAL not null," +
                dataBase_string.A_FIELD_DESC + " TEXT)";
        db.execSQL(q);

        /*Creo la tabella per le categorie*/
        q = "CREATE TABLE " + dataBase_string.TBL_CATEGORIA + "( "
                + dataBase_string.C_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + dataBase_string.C_FIELD_NOME + " TEXT not null,"
                + dataBase_string.C_FIELD_TIPO + " INT not null)";
        db.execSQL(q);

        /*Popolo la tabella delle categorie*/
        popolaCategoria(db);

        /*Creo la tabella per le operazioni (pagamenti o entrate)*/
        q = "CREATE TABLE "+dataBase_string.TBL_OPERAZIONI+
                " ( " + dataBase_string.O_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                dataBase_string.O_FIELD_OGGETTO + " TEXT not null," +
                dataBase_string.O_FIELD_IMPORTO + " REAL not null," +
                dataBase_string.O_FIELD_CATEGORIA + " INTEGER not null," +
                dataBase_string.O_FIELD_DATA + " TEXT not null," +
                dataBase_string.O_FIELD_DESC + " TEXT," +
                dataBase_string.O_FIELD_IDACCOUNT + " INTEGER not null," +
                dataBase_string.O_FIELD_MEZZO + " TEXT not null," +
                dataBase_string.O_FIELD_TIPO + " INT not null,"+
                dataBase_string.O_FIELD_LUOGO_LAT + " TEXT," +
                dataBase_string.O_FIELD_LUOGO_LON + " TEXT,"+
                dataBase_string.O_FIELD_FOTO + " TEXT," +
                dataBase_string.O_FIELD_FUTURA + " INT not null,"
                + " FOREIGN KEY (" + dataBase_string.O_FIELD_CATEGORIA + ") REFERENCES " + dataBase_string.TBL_CATEGORIA + " (" + dataBase_string.C_FIELD_ID + "),"
                + " FOREIGN KEY (" + dataBase_string.O_FIELD_IDACCOUNT + ") REFERENCES " + dataBase_string.TBL_ACCOUNT + " (" + dataBase_string.A_FIELD_ID + "));";
        db.execSQL(q);

        /*Creo la tabella per le operazioni periodiche*/
        q = "CREATE TABLE " + dataBase_string.TBL_PERIODICA
                + " ( " + dataBase_string.P_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + dataBase_string.P_FIELD_IDOP + " INTEGER,"
                +dataBase_string.P_FIELD_TIPO + " INTEGER not null,"
                + dataBase_string.P_FIELD_DATA + " TEXT not null,"
                + dataBase_string.P_FIELD_NEXT + " TEXT not null,"
                + dataBase_string.P_FIELD_PERIODO + " INTEGER not null,"
                + " FOREIGN KEY (" + dataBase_string.P_FIELD_IDOP + ") REFERENCES " + dataBase_string.TBL_OPERAZIONI + " (" + dataBase_string.O_FIELD_ID + "));";
        db.execSQL(q);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {  }

    public void popolaCategoria(SQLiteDatabase d){

        int size = 16;
        Categoria[] cat = new Categoria[size];

        cat[0] = new Categoria("abbigliamento",1);
        cat[1] = new Categoria("animali",1);
        cat[2] = new Categoria("bollette",1);
        cat[3] = new Categoria("casa",1);
        cat[4] = new Categoria("hobby",1);
        cat[5] = new Categoria("istruzione",1);
        cat[6] = new Categoria("lavoro",1);
        cat[7] = new Categoria("multe",1);
        cat[8] = new Categoria("salute",1);
        cat[9] = new Categoria("trasporti",1);
        cat[10] = new Categoria("vacanze",1);
        cat[11] = new Categoria("altro",1);
        cat[12] = new Categoria("assegno",0);
        cat[13] = new Categoria("regalo",0);
        cat[14] = new Categoria("stipendio",0);
        cat[15] = new Categoria("altro",0);

        for (int i = 0 ; i<cat.length ; i++){
            /*Riempo la tabella*/
            ContentValues cv = new ContentValues();
            cv.put(dataBase_string.C_FIELD_NOME,cat[i].cat_name);
            cv.put(dataBase_string.C_FIELD_TIPO,cat[i].cat_type);
            try{
                d.insert(dataBase_string.TBL_CATEGORIA, null, cv);
            }
            catch (SQLiteException sqle){
                sqle.printStackTrace();
            }

        }


    }
}
