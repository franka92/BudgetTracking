package giulia.budgettracking;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class main extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    SharedPreferences pref = null;
    private static String KEY_FIRST_RUN = "";
    private SharedPreferences.Editor editor;

    private databBaseManager db = new databBaseManager(this);

    static String nome = null;
    static Double saldo = null;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    private int controlloPianificate = 0;
    private SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        pref = getPreferences(MODE_PRIVATE);

        if(!pref.contains("KEY_FIRST_RUN")){//Primo avvio -> configurazione account
            KEY_FIRST_RUN = "first";
            Intent i = new Intent(this, AddAccount.class);
            startActivity(i);
        }
        else{
            /*Recupero i dati dal database*/
            db = new databBaseManager(this);
            Cursor c = db.query(dataBase_string.TBL_ACCOUNT);
            try{
                while (c.moveToNext()){
                    nome = c.getString(1);
                    saldo = c.getDouble(2);
                }
            }
            finally{
                c.close();
            }
        }
        Intent i_get = getIntent();
        editor = pref.edit();
        editor.putString("KEY_FIRST_RUN", KEY_FIRST_RUN);
        editor.commit();
        Cursor c = db.recuperaOperazioniPianificate();
        Cursor crs = db.recuperaOperazioniPeriodiche();
        if(c.moveToNext() || crs.moveToNext()){
            showDialogPianificate();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = null;
        switch (position) {
            case 0:
                //Riepilogo
                FragmentManager frag = main.this.getSupportFragmentManager();
                Fragment f = frag.findFragmentByTag("FRAGMENT_RIEPILOGO");
                if(f == null || !f.isVisible()){
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, PlaceholderFragment.newInstance(position + 1),"FRAGMENT_RIEPILOGO")
                            .commit();
                }
                else{
                    //doNothing
                }
                break;

            case 1:
                //Aggiungi operazione
                intent = new Intent(this, AddOperazione.class);
                intent.putExtra("tipoOperazione",1);/*Indica che stiamo aggiungendo un operazione*/
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(this, AddOperazione.class);
                intent.putExtra("tipoOperazione",0);/*Indica che stiamo aggiungendo un entrata*/
                startActivity(intent);
                break;
            case 3:
                //Visualizza elenco completo operazioni
                intent = new Intent(this, FiltraOperazioni.class);
                intent.putExtra("tipoFiltro",3);/*Indica che vogliamo le operazioni dell'ultima settimana*/
                startActivity(intent);
                break;
            case 4:
                //Visualizza Operazioni per data
                intent = new Intent(this, FiltraOperazioni.class);
                intent.putExtra("tipoFiltro",0);/*Indica che vogliamo le operazioni dell'ultima settimana*/
                startActivity(intent);
                break;
            case 5:
                //Visualizza Operazioni ultima settimana
                intent = new Intent(this, FiltraOperazioni.class);
                intent.putExtra("tipoFiltro",1);/*Indica che vogliamo le operazioni dell'ultima settimana*/
                startActivity(intent);
                break;
            case 6:
                //Visualizza Operazioni ultimo mese
                intent = new Intent(this, FiltraOperazioni.class);
                intent.putExtra("tipoFiltro",2);/*Indica che vogliamo le operazioni dell'ultimo mese*/
                startActivity(intent);
                break;
            case 7:
                //Visualizza Grafici e statistiche
                intent = new Intent(this, Grafici.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            openChart();
            aggiornaRiepilogo();
        }
    }



    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_riepilogo);/*Torna alla schermata principale*/
                break;
            case 2:
                mTitle = getString(R.string.title_addOperazione);
                break;
            case 3:
                mTitle = getString(R.string.title_addEntrata);
                break;
            case 4:
                mTitle = getString(R.string.title_visualizzaSpese);
                break;
            case 5:
                mTitle = getString(R.string.title_opGiornaliere);
                break;
            case 6:
                mTitle = getString(R.string.title_opSettimanali);
                break;
            case 7:
                mTitle = getString(R.string.title_opMensili);
                break;
            case 8:
                mTitle = getString(R.string.title_statistiche);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Solo se non è aperto il menù laterale a scomparsa*/
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.menu_addEntrata){
            /*Lancio l'activity per inserire una nuova entrata*/
            Intent i = new Intent(this,AddOperazione.class);
            i.putExtra("tipoOperazione",0);/*Indica che stiamo aggiungendo un entrata*/
            startActivity(i);
            /*Intent i = new Intent(this, addEntrata.class);
            startActivity(i);*/
        }
        else if(id == R.id.menu_addPagamento){
            /*Lancio l'activity per inserire un nuovo pagamento*/
            /*Intent i = new Intent(this, addPagamento.class);
            startActivity(i);*/
            Intent i = new Intent(this,AddOperazione.class);
            i.putExtra("tipoOperazione",1);/*Indica che stiamo aggiungendo un Pagamento*/
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        if(saldo == null){
            Intent i = new Intent(this, AddAccount.class);
            startActivity(i);
        }else{
            /*Aggiorniamo l'home con le informazioni*/
            aggiornaRiepilogo();
            /* mostraUltimeOperazioni();*/
            Operazione o = new Operazione(this);
            TableLayout tbl = (TableLayout) this.findViewById(R.id.table_listaOp_main);
            TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT,android.app.ActionBar.LayoutParams.WRAP_CONTENT);
            tbl.setLayoutParams(layoutRow);
            //tbl.setGravity(Gravity.CENTER_HORIZONTAL);
            o.mostraUltimeOperazioni(tbl,'l');
            /*Gestione click sul bottone per mostrare tutte le informazioni dell'account*/
            Button mostraDettagli = (Button) findViewById(R.id.bt_mostraInfoAcc);
            mostraDettagli.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mostraDettagliAccount();

                }
            });

            Button mostraTutteOperazioni = (Button) findViewById(R.id.bt_mostraTutteOp);
            mostraTutteOperazioni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAllOperazioni();

                }
            });

            /*GRAFICO*/
            openChart();
        }
        super.onResume();


    }

    private void openChart(){
        Double entrate = 0.0, uscite = 0.0;
        Cursor c = db.countType(0);
        ScrollView v = (ScrollView) findViewById(R.id.scroll_view);
        ViewGroup layout = (ViewGroup) v.findViewById(R.id.layout_grafico);
        layout.removeAllViews();

        try{
            while (c.moveToNext()){
                entrate += c.getDouble(2);
            }
        }
        finally{
            c.close();
        }
        c = db.countType(1);
        try{
            while (c.moveToNext()){
                uscite += c.getDouble(2);
            }
        }
        finally{
            c.close();
        }
        // Pie Chart Section Names
        String[] code = new String[] {"Entrate","Uscite" };

        // Pie Chart Section Value
        double[] distribution = { entrate,uscite} ;

        // Color of each Pie Chart Sections
        int[] colors = {Color.rgb(34,168,108), Color.rgb(255,0,0)};

        // Instantiating CategorySeries to plot Pie Chart
        CategorySeries distributionSeries = new CategorySeries(" Entrate Vs Uscite (ultimi 30 giorni)");
        for(int i=0 ;i < distribution.length;i++){
            // Adding a slice with its values and name to the Pie Chart
            distributionSeries.add(code[i], distribution[i]);
        }

        // Instantiating a renderer for the Pie Chart
        DefaultRenderer defaultRenderer  = new DefaultRenderer();
        for(int i = 0 ;i<distribution.length;i++){
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(colors[i]);
            seriesRenderer.setDisplayChartValues(true);
            // Adding a renderer for a slice
            defaultRenderer.addSeriesRenderer(seriesRenderer);
        }

        defaultRenderer.setChartTitle("Entrate Vs Uscite (ultimi 30 giorni)");
        defaultRenderer.setChartTitleTextSize(20);
        defaultRenderer.setZoomButtonsVisible(true);
        defaultRenderer.setZoomEnabled(true);
        defaultRenderer.setFitLegend(true);
        defaultRenderer.setPanEnabled(false);
        defaultRenderer.setShowLabels(false);
        defaultRenderer.setLegendTextSize(40);
        defaultRenderer.setLabelsTextSize(40);
        // Creating an intent to plot bar chart using dataset and multipleRenderer
        if(entrate == 0.0 && uscite == 0.0){
            TextView txt_nothing = new TextView(this);
            txt_nothing.setText("Non ci sono dati da mostrare");
            txt_nothing.setTextColor(Color.WHITE);
            txt_nothing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txt_nothing.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(txt_nothing);
        }
        else{
            GraphicalView chartView = ChartFactory.getPieChartView(getBaseContext(), distributionSeries , defaultRenderer);
            chartView.setLayoutParams(new android.app.ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, 500));


            layout.addView(chartView);
        }

    }


    public void showAllOperazioni(){
        Intent intent = new Intent(this, FiltraOperazioni.class);
        intent.putExtra("tipoFiltro",3);/*Indica che vogliamo le operazioni dell'ultima settimana*/
        startActivity(intent);
    }

    public void aggiornaRiepilogo(){
        recuperaDati(db);
         /*Riempo i campi relativi al saldo del conto*/
        TextView nome_conto = (TextView) findViewById(R.id.lb_nomeValue);
        TextView saldo_conto = (TextView) findViewById(R.id.lb_saldoValue);
        if(nome != null && saldo != null) {
            nome_conto.setText(nome);
            saldo_conto.setText(saldo.toString() + "€");
        }
    }

    public void mostraDettagliAccount(){
        Intent i = new Intent(this, gestioneAccount.class);
        startActivity(i);
    }

    public void recuperaDati(databBaseManager d){
        Cursor c = d.query(dataBase_string.TBL_ACCOUNT);
        try{
            while (c.moveToNext()){
                nome = c.getString(1);
                saldo = c.getDouble(2);
            }
        }
        finally{
            c.close();
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            /*Riempo i campi relativi al saldo del conto*/
            TextView nome_conto = (TextView) rootView.findViewById(R.id.lb_nomeValue);
            TextView saldo_conto = (TextView) rootView.findViewById(R.id.lb_saldoValue);
            if(saldo!= null && nome != null) {
                nome_conto.setText(nome);
                if (saldo_conto != null) {
                    saldo_conto.setText(saldo.toString() + "€");
                }
            }

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((main) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }



    }

    private void showDialogPianificate(){
        final Dialog d;
        Cursor crs_pianificate = null, crs_periodiche = null;
        d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCancelable(false);				//Toccando il display al di fuori della finestra essa non si chiuderà
        d.setContentView(R.layout.dialog_pianificate);	//Assegnamo un layout alla finestra
        crs_pianificate = db.recuperaOperazioniPianificate();
        crs_periodiche = db.recuperaOperazioniPeriodiche();
        TableLayout tbl = (TableLayout) d.findViewById(R.id.tbl_pianificate);
        Calendar cal = Calendar.getInstance();
        String data = date_format.format(cal.getTime());

        while(crs_pianificate.moveToNext()){
            String oggetto,tipo_stringa;
            Double importo;
            int tipo,id;

            oggetto = crs_pianificate.getString(1);
            importo = crs_pianificate.getDouble(2);
            tipo = crs_pianificate.getInt(8);
            id = crs_pianificate.getInt(0);
            if(tipo == 0){
                tipo_stringa = "Entrata";
            }
            else{
                tipo_stringa = "Uscita";
            }
            addRowDialog(tbl,oggetto,data,tipo_stringa,importo,tipo,id);
            db.modificaProgrammata(id);

        }
        crs_pianificate.close();
        while(crs_periodiche.moveToNext()){
            String oggetto,tipo_stringa;
            Double importo;
            int tipo,id, id_primaOp;
            id = crs_periodiche.getInt(0);
            id_primaOp = crs_periodiche.getInt(1);
            Cursor c = db.findById(dataBase_string.TBL_OPERAZIONI,dataBase_string.O_FIELD_ID,id_primaOp);
            db.modificaPeriodica(id);
            if(c.moveToNext()){
                oggetto = c.getString(1);
                importo = c.getDouble(2);
                tipo = c.getInt(8);
                id = c.getInt(0);

                if(tipo == 0){
                    tipo_stringa = "Entrata";
                }
                else{
                    tipo_stringa = "Uscita";
                }
                addRowDialog(tbl,oggetto,data,tipo_stringa,importo,tipo,id);

            }

        }
        crs_periodiche.close();
        TableRow row_bt = new TableRow(this);
        Button bt_chiudi = new Button(this);
        bt_chiudi.setText("Ok");
        bt_chiudi.setTextColor(Color.WHITE);
        bt_chiudi.setBackgroundResource(R.drawable.green_button);
        bt_chiudi.setGravity(Gravity.CENTER_HORIZONTAL);
        row_bt.addView(bt_chiudi);
        tbl.addView(row_bt);
        d.show();
        /*Al click su OK aggiungiamo le operazioni all'elenco*/
        bt_chiudi.setOnClickListener(new View.OnClickListener() {   //Gestisco l'evento onClick sul bottone
            @Override
            public void onClick(View view) {
                d.dismiss();
                onResume();

            }
        });
    }

    private void addRowDialog(TableLayout tbl,String oggetto, String data, String tipo_stringa, Double importo, int tipo, int id){
        TableRow row_oggetto = new TableRow(this);
        TableRow row_data = new TableRow(this);
        TableRow row_importo = new TableRow(this);
        TableRow row_tipo = new TableRow(this);
        TextView titolo_nome = new TextView(this);
        TextView titolo_data = new TextView(this);
        TextView titolo_importo = new TextView(this);
        TextView titolo_tipo = new TextView(this);
        TextView text_nome = new TextView(this);
        TextView text_data = new TextView(this);
        TextView text_importo = new TextView(this);
        TextView text_tipo = new TextView(this);

        titolo_nome.setText("Oggetto");
        titolo_nome.setTextAppearance(this,R.style.text_titolo_pianificate);
        titolo_data.setText("Data");
        titolo_data.setTextAppearance(this,R.style.text_titolo_pianificate);
        titolo_importo.setText("Importo");
        titolo_importo.setTextAppearance(this,R.style.text_titolo_pianificate);
        titolo_tipo.setText("Tipo");
        titolo_tipo.setTextAppearance(this,R.style.text_titolo_pianificate);
        text_nome.setText(oggetto);
        text_nome.setTextAppearance(this,R.style.text_contenuto_pianificate);
        text_data.setText(data);
        text_data.setTextAppearance(this,R.style.text_contenuto_pianificate);
        text_importo.setText(importo.toString()+"€");
        text_importo.setTextAppearance(this,R.style.text_contenuto_pianificate);
        text_tipo.setText(tipo_stringa);
        text_tipo.setTextAppearance(this,R.style.text_contenuto_pianificate);

        row_oggetto.addView(titolo_nome);
        row_oggetto.addView(text_nome);
        row_data.addView(titolo_data);
        row_data.addView(text_data);
        row_importo.addView(titolo_importo);
        row_importo.addView(text_importo);
        row_tipo.addView(titolo_tipo);
        row_tipo.addView(text_tipo);
        tbl.addView(row_oggetto);
        tbl.addView(row_data);
        tbl.addView(row_importo);
        tbl.addView(row_tipo);

        row_tipo.setBackgroundResource(R.drawable.bordo_tabella);
    }



}
