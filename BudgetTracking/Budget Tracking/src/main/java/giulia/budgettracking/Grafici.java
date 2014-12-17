package giulia.budgettracking;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by frank_000 on 22/10/2014.
 */
public class Grafici extends FragmentActivity {

    private databBaseManager db = null;
    SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    private int settimana = 0;
    private int mese = 1;
    private String [] giorni_settimana = {"Lunedì","Martedì","Mercoledì","Giovedì","Venerdì","Sabato","Domenica"};
    private int [] color_pag = new int [12];
    private int [] color_ent = new int [4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        db = new databBaseManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafici);
        openBarChart(settimana);
        openBarChart(mese);
        openPieChart();
    }


    /*Crea un BarChart
    * @param type: tipo di grafico che deve essere generato (Settimanale o Mensile)
    * */
    private void openBarChart(int type){
        String [] asse_x = null;
        Double [] entrate = null;
        Double [] uscite = null;
        Double tot_entrate = 0.0;
        Double tot_uscite = 0.0;
        String [] elenco_date = null;
        String x_title = null;
        Cursor c = null;
        Calendar cal = Calendar.getInstance(Locale.ITALY);
        int day_of_month, day_of_week;

        /*View*/
        ScrollView v = (ScrollView) findViewById(R.id.scroll_view);
        TextView txt_totale_entrate = null;
        TextView txt_totale_uscite = null;
        ViewGroup layout = null;

        if(type == settimana){
            day_of_week = cal.getActualMaximum(Calendar.DAY_OF_WEEK);
            elenco_date = new String[day_of_week];
            entrate = new Double[day_of_week];
            uscite = new Double[day_of_week];
            asse_x = new String[day_of_week];
            x_title = "Giorni della settimana";

            /*Creo un array con le date dei giorni della settimana, da lunedì a domenica*/
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            for (int i = 0; i < elenco_date.length; i++) {
                elenco_date[i] = date_format.format(cal.getTime());
                asse_x[i] = giorni_settimana[i];
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            c = db.getElencoSpese(null,1);

            /*Recupero le View in cui inserire il grafico*/
            layout = (ViewGroup) v.findViewById(R.id.grafico_settimana);
            txt_totale_entrate = (TextView) v.findViewById(R.id.txt_total_value);
            txt_totale_uscite = (TextView) v.findViewById(R.id.txt_total2_value);

        }
        else if(type == mese){
            day_of_month = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            entrate = new Double[day_of_month];
            uscite = new Double[day_of_month];
            asse_x = new String[day_of_month];
            elenco_date = new String[day_of_month];
            x_title = "Giorni del mese";

            /*Creo un array con le date dei giorni del mese corrente*/
            cal.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
            for (int i = 0; i < day_of_month; i++) {
                elenco_date[i] = date_format.format(cal.getTime());
                asse_x[i] = ""+cal.get(Calendar.DAY_OF_MONTH);
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            c = db.getElencoSpese(null,2);

            /*Recupero le View in cui inserire il grafico*/
            layout = (ViewGroup) v.findViewById(R.id.grafico_mese);
            txt_totale_entrate = (TextView) v.findViewById(R.id.txt_total_mese_value);
            txt_totale_uscite = (TextView) v.findViewById(R.id.txt_total2_mese_value);
        }

        /*Inizializzo l'array dei valori a 0*/
        for (int i=0;i<entrate.length;i++){
            entrate[i] = 0.0;
            uscite[i] = 0.0;
        }

        try{
            while (c.moveToNext()){
                String data_operazione = c.getString(4);
                int index = Arrays.asList(elenco_date).indexOf(data_operazione);
                if(c.getInt(8) == 0){//Entrata

                    entrate[index] += c.getDouble(2);
                    tot_entrate += c.getDouble(2);
                }
                else{//Uscita
                    uscite[index] += c.getDouble(2);
                    tot_uscite += c.getDouble(2);
                }
            }
        }
        finally{
            c.close();
        }

        /*Creo le serie per le entrate e le uscite*/
        XYSeries incomeSeries = createSeries("Entrate",entrate);
        XYSeries expenseSeries = createSeries("Uscite",uscite);

        /*Creo un dataset per ciascuna serie*/
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expenseSeries);

        /*Stile delle serie*/
        XYSeriesRenderer incomeRenderer = customizeSeries(Color.rgb(0, 204, 0));
        XYSeriesRenderer expenseRenderer = customizeSeries(Color.rgb(255, 0, 0));

        /*Stile dell'intero grafico*/
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setXLabels(0);
        multiRenderer.setXAxisMin(-0.5);
        multiRenderer.setXTitle(x_title);
        multiRenderer.setYTitle("Importo in euro");
        multiRenderer.setZoomButtonsVisible(true);
        for(int i=0; i< asse_x.length;i++){
            multiRenderer.addXTextLabel(i, asse_x[i]);
        }

        /*
        *[Nota] L'ordine in cui aggiungere i dataseries al dataset e i renderers al mutilpeRenderer deve essere lo stesso
        */
        multiRenderer.addSeriesRenderer(incomeRenderer);
        multiRenderer.addSeriesRenderer(expenseRenderer);
        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setBackgroundColor(Color.rgb(45,47,49));

        // Creating an intent to plot bar chart using dataset and multipleRenderer
        GraphicalView chartView = ChartFactory.getBarChartView(getBaseContext(),dataset,multiRenderer, BarChart.Type.DEFAULT);

        chartView.setBackgroundColor(Color.rgb(45,47,49));
        chartView.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 500));
        layout.addView(chartView);

        txt_totale_entrate.setText(tot_entrate.toString() + "€");
        txt_totale_uscite.setText(tot_uscite.toString() + "€");


    }
    /*Crea un array con l'elenco dei colori da utilizzare nei PieChart*/
    public void riempiArrayColori(){
        color_pag[0] = Color.rgb(255,61,61);
        color_pag[1] = Color.rgb(255,61,211);
        color_pag[2] = Color.rgb(159,61,255);
        color_pag[3] = Color.rgb(61,68,255);
        color_pag[4] = Color.rgb(61,217,255);
        color_pag[5] = Color.rgb(61,255,198);
        color_pag[6] = Color.rgb(61,255,74);
        color_pag[7] = Color.rgb(211,255,61);
        color_pag[8] = Color.rgb(255,198,61);
        color_pag[9] = Color.rgb(255,255,255);
        color_pag[10] = Color.rgb(130,198,212);
        color_pag[11] = Color.rgb(51,0,0);

        color_ent[0] = Color.rgb(255,61,61);
        color_ent[1] = Color.rgb(159,61,255);
        color_ent[2] = Color.rgb(61,255,198);
        color_ent[3] = Color.rgb(211,255,61);
    }

    /*Crea i grafici a torta per le entrate e i pagamenti*/
    private void openPieChart(){
        Cursor c = null;
        ScrollView v = (ScrollView) findViewById(R.id.scroll_view);
        /*Associazione tra categoria e colore che la rappresenta nel grafico*/
        HashMap<String,Integer> colors_p = new HashMap<String,Integer>();
        HashMap<String,Integer> colors_e = new HashMap<String,Integer>();
        /*Associazione tra categoria e totale delle spese/entrate relative ad essa*/
        HashMap<String,Double> totali_pagamenti = new HashMap<String,Double>();
        HashMap<String,Double> totali_entrate = new HashMap<String,Double>();
        /*Associazione tra l'id della categoria e il nome della stessa categoria*/
        HashMap<Integer,String> cat_pagamenti = new HashMap<Integer,String>();
        HashMap<Integer,String> cat_entrate = new HashMap<Integer,String>();
        /*Array con i nomi delle varie categorie*/
        String[] cat_pag = getResources().getStringArray(R.array.categoria_pagamenti);
        String[] cat_ent = getResources().getStringArray(R.array.categoria_entrate);

        /*Creo l'associazione tra le categoria e i colori*/
        riempiArrayColori();
        for(int i=0;i<cat_pag.length;i++){
            colors_p.put(cat_pag[i],color_pag[i]);
        }
        for(int i=0;i<cat_ent.length;i++){
            colors_e.put(cat_ent[i],color_ent[i]);
        }

        c = db.query(dataBase_string.TBL_CATEGORIA);

        try{
            while (c.moveToNext()){
                if((c.getInt(2)) == 0){ /*Entrata*/
                    cat_entrate.put(c.getInt(0),c.getString(1));
                }
                else{/*Uscita*/
                    cat_pagamenti.put(c.getInt(0),c.getString(1));
                }
            }
        }
        finally{
            c.close();
        }

        c = db.recuperaTuttiMovimenti();
        try{
            while (c.moveToNext()){
                if((c.getInt(8)) == 0) { /*Entrata*/
                    int categoria = c.getInt(3);
                    String cat = cat_entrate.get(categoria);
                    if(!totali_entrate.containsKey(cat)){
                        totali_entrate.put(cat,c.getDouble(2));
                    }
                    else{
                        Double tot_parz = totali_entrate.get(cat);
                        tot_parz += c.getDouble(2);
                        totali_entrate.put(cat,tot_parz);
                    }
                }
                else{/*Uscita*/
                    int categoria = c.getInt(3);
                    String cat = cat_pagamenti.get(categoria);
                    if(!totali_pagamenti.containsKey(cat)){
                        totali_pagamenti.put(cat,c.getDouble(2));
                    }
                    else{
                        Double tot_parz = totali_pagamenti.get(cat);
                        tot_parz += c.getDouble(2);
                        totali_pagamenti.put(cat,tot_parz);
                    }
                }
            }

        }
        finally{
            c.close();
        }


        // Pie Chart Section Names
        String[] code_pagamenti = new String[totali_pagamenti.size()];
        String[] code_entrate = new String[totali_entrate.size()];
        Double[] distribution_pagamenti = new Double[totali_pagamenti.size()];
        Double[] distribution_entrate = new Double[totali_entrate.size()];
        int[] colors_pagamenti = new int[totali_pagamenti.size()];
        int[] colors_entrate = new int[totali_entrate.size()];
        int index_p = 0;
        int index_e = 0;

        Double totale_spese = 0.0;
        Double totale_entrate = 0.0;

        for (Map.Entry<String,Double> entry : totali_pagamenti.entrySet()){

            code_pagamenti[index_p] = entry.getKey();
            distribution_pagamenti[index_p] = entry.getValue();
            totale_spese += distribution_pagamenti[index_p];
            colors_pagamenti[index_p] = colors_p.get(code_pagamenti[index_p]);
            index_p++;

        }
        for (Map.Entry<String,Double> entry : totali_entrate.entrySet()){
            code_entrate[index_e] = entry.getKey();
            distribution_entrate[index_e] = entry.getValue();
            totale_entrate += distribution_entrate[index_e];
            colors_entrate[index_e] = colors_e.get(code_entrate[index_e]);
            index_e++;
        }

        /*Creo le view con i grafici*/
        ViewGroup layout = (ViewGroup) v.findViewById(R.id.grafico_categoria);
        GraphicalView chartView = disegnaGrafico(colors_pagamenti,code_pagamenti,distribution_pagamenti);
        if(totale_spese != 0){
            layout.addView(chartView);
            TableLayout tbl_spese = (TableLayout) findViewById(R.id.tbl_spesa_cat);
            creaTabellaCategorie(totali_pagamenti, totale_spese,tbl_spese);
        }
        else{
            TextView txt_nothing = new TextView(this);
            txt_nothing.setText("Non ci sono dati da mostrare");
            txt_nothing.setGravity(Gravity.CENTER_HORIZONTAL);
            txt_nothing.setTextColor(Color.WHITE);
            txt_nothing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(txt_nothing);
        }

        layout = (ViewGroup) v.findViewById(R.id.grafico_categoria_entrata);
        chartView = disegnaGrafico(colors_entrate,code_entrate,distribution_entrate);
        if(totale_entrate != 0){
            layout.addView(chartView);
            TableLayout tbl_entrate = (TableLayout) findViewById(R.id.tbl_entrate_cat);
            creaTabellaCategorie(totali_entrate, totale_entrate,tbl_entrate);
        }
        else{
            TextView txt_nothing = new TextView(this);
            txt_nothing.setText("Non ci sono dati da mostrare");
            txt_nothing.setGravity(Gravity.CENTER_HORIZONTAL);
            txt_nothing.setTextColor(Color.WHITE);
            txt_nothing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(txt_nothing);
        }
    }
    /*Crea la tabella con la percentuale di spese per ogni categoria
    * @param totali: elenco dei totali per ciascuna categoria
    * @param tot_complessivo: somma totale complessiva di tutte le categorie
    * @param table_totale: tabella da riempire
    */
    public void creaTabellaCategorie (Map<String,Double> totali, Double tot_complessivo, TableLayout table_totale){
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow row_intestazione = new TableRow(this);
        TextView nome_intestazione = new TextView(this);
        TextView totale_intestazione = new TextView(this);
        TextView percentuale_intestazione = new TextView(this);
        llp.setMargins(20, 0, 20, 0);//left top right bottom

        /*Creo l'intestazione della tabella*/
        nome_intestazione.setText("Categoria");
        totale_intestazione.setText("Totale €");
        percentuale_intestazione.setText("Percentuale %");

        nome_intestazione.setLayoutParams(llp);
        totale_intestazione.setLayoutParams(llp);
        percentuale_intestazione.setLayoutParams(llp);

        nome_intestazione.setTextAppearance(this, R.style.text_lista_operazioni);
        totale_intestazione.setTextAppearance(this, R.style.text_lista_operazioni);
        percentuale_intestazione.setTextAppearance(this, R.style.text_lista_operazioni);

        row_intestazione.setBackgroundColor(Color.WHITE);
        nome_intestazione.setTextColor(Color.BLACK);
        totale_intestazione.setTextColor(Color.BLACK);
        percentuale_intestazione.setTextColor(Color.BLACK);

        row_intestazione.addView(nome_intestazione);
        row_intestazione.addView(totale_intestazione);
        row_intestazione.addView(percentuale_intestazione);
        table_totale.addView(row_intestazione);

        /*Riempo la tabella*/
        for (Map.Entry<String,Double> entry : totali.entrySet()){
            TableRow row = new TableRow(this);
            TextView nome = new TextView(this);
            TextView totale_cat = new TextView(this);
            TextView percentuale = new TextView(this);
            Double perc_value = 0.0;

            nome.setText(entry.getKey());
            totale_cat.setText(entry.getValue().toString());
            perc_value = ((entry.getValue()/tot_complessivo)*100);
            perc_value = Math.floor(perc_value*100);
            perc_value = perc_value/100;
            percentuale.setText(perc_value.toString()+"%");

            nome.setLayoutParams(llp);
            totale_cat.setLayoutParams(llp);
            percentuale.setLayoutParams(llp);
            nome.setTextAppearance(this, R.style.text_totali_cat);
            totale_cat.setTextAppearance(this, R.style.text_totali_cat);
            percentuale.setTextAppearance(this, R.style.text_totali_cat);

            row.addView(nome);
            row.addView(totale_cat);
            row.addView(percentuale);
            table_totale.addView(row);

        }

    }
    /*Crea la view con il grafico
    * @param colori: elenco dei colori da utilizzare
    * @param coda: elenco delle categorie
    * @param distribuzione: elenco dei totali delle categorie
    *
    * @return GraphicalView: view contenente il grafico
    * */
    public GraphicalView disegnaGrafico(int [] colori,String[] coda, Double[] distribuzione){
        // Instantiating CategorySeries to plot Pie Chart
        CategorySeries distributionSeries = new CategorySeries("");
        for(int i=0 ;i < distribuzione.length;i++){
            // Adding a slice with its values and name to the Pie Chart
            distributionSeries.add(coda[i], distribuzione[i]);
        }

        // Instantiating a renderer for the Pie Chart
        DefaultRenderer defaultRenderer  = new DefaultRenderer();
        for(int i = 0 ;i<distribuzione.length;i++){
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(colori[i]);
            seriesRenderer.setDisplayChartValues(true);

            // Adding a renderer for a slice
            defaultRenderer.addSeriesRenderer(seriesRenderer);
        }

        defaultRenderer.setChartTitleTextSize(20);
        defaultRenderer.setFitLegend(true);
        defaultRenderer.setPanEnabled(false);
        defaultRenderer.setShowLabels(true);
        defaultRenderer.setLegendTextSize(40);
        defaultRenderer.setLabelsTextSize(40);

        // Creating an intent to plot bar chart using dataset and multipleRenderer
        GraphicalView chartView = ChartFactory.getPieChartView(getBaseContext(), distributionSeries , defaultRenderer);
        chartView.setLayoutParams(new android.app.ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, 500));
        return chartView;
    }

    /*Crea una serie per il grafico
    * @param label: label della serie
    * @param value: valori della serie
    */
    public XYSeries createSeries(String label,Double [] value){
        XYSeries series = new XYSeries(label);
        for(int i=0;i<value.length;i++){
            series.add(i,value[i]);
        }
        return  series;
    }

    public XYSeriesRenderer customizeSeries(int color){
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(color);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);
        renderer.setDisplayChartValues(true);
        return renderer;
    }

}
