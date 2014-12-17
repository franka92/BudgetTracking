package giulia.budgettracking;

/**
 * Created by frank_000 on 18/10/2014.
 */
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


public class GeneraPDF{
    private static Font catFont;
    private static Font subFont;
    private databBaseManager db = null;
    private Context cont;
    SimpleDateFormat sdf = new SimpleDateFormat();

    public GeneraPDF(Context cxt) {
        catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,Font.BOLD);
        subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,Font.BOLD);
        db = new databBaseManager(cxt);
        cont = cxt;
    }

    /*Funzione che genera un file pdf
    * @param type: tipo di report pdf che deve essere generato (totale, giornaliero, settimanale o mensile
    */
    public String creaPdf(String type,String data){
        String file_name = null;

        sdf.applyPattern("dd-MM-yyyy HH.mm.ss");
        String dataStr = sdf.format(new Date());
        file_name = "report_"+type+"_"+dataStr.substring(0,10);
        try {
            Document document = new Document();
            String file_path = Environment.getExternalStorageDirectory().getPath() + "/"+file_name+".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(file_path));
            document.open();
            addMetaData(document);
            addContent(document, type, data);
            document.close();
            return "Creato report pdf: "+file_path;
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

   /*Aggiunge i metadati al pdf generato
   * @param document: documento a cui aggiungere i metadati
   */
    private static void addMetaData(Document document) {
        document.addTitle("Reports");
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("Budget Tracking");
        document.addCreator("Giulia Franchini");
    }

    /*Aggiunge il contenuto del file pdf
    * @param document: il documento a cui aggiungere i vari dati
    * @param type: tipo di report generato
    * @param date: data in cui è stato generato il report
    */
    private void addContent(Document document, String type, String date) throws DocumentException {
        Cursor crs = null;
        String data_generazione = sdf.format(new Date());
        if(type == "elenco_completo"){
            crs = db.recuperaTuttiMovimenti();
        }
        else if(type == "giornaliero"){
            SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
            crs = db.getElencoSpese(date,0);
        }
        else if(type == "settimanale"){
            crs = db.getElencoSpese(null, 1);
        }
        else if(type == "mensile"){
            crs = db.getElencoSpese(null, 2);
        }
        Anchor anchor = new Anchor("Report "+type+" generato il "+data_generazione, catFont);
        Chapter catPart = new Chapter(new Paragraph(anchor), 0);
        catPart.add(new Paragraph(" "));
        catPart.setNumberDepth(0);
        while(crs.moveToNext()){
            Cursor c = null;
            String data = null,nome,descrizione,lat,lng,luogo="-",categoria,mezzo,tipo;
            int categoria_num;

            Double importo = crs.getDouble(2);
            nome = crs.getString(1);
            categoria_num = crs.getInt(3);
            lat = crs.getString(9);
            lng = crs.getString(10);
            c = db.findById(dataBase_string.TBL_CATEGORIA, dataBase_string.C_FIELD_ID, categoria_num);
            if(c.moveToNext()){
                categoria = c.getString(1);
            }
            else{
                categoria = "-";
            }
            c.close();
            data = crs.getString(4);
            descrizione = crs.getString(5);
            if(descrizione.length()<=0){
                descrizione = "-";
            }
            mezzo = crs.getString(7);

            if(crs.getInt(8) == 1) {/*Pagamento*/
                tipo = "pagamento";
            }
            else{/*Entrata*/
                tipo = "entrata";
            }
            if(lat.length()>0) {
                Geocoder geocoder = new Geocoder(cont, Locale.getDefault());
                try {
                    java.util.List<Address> listAddresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
                    if (null != listAddresses && listAddresses.size() > 0) {
                        luogo = listAddresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Paragraph subPara = new Paragraph(nome+" - "+tipo, subFont);
            Section subCatPart = catPart.addSection(subPara);
            List list = new List(true, false, 10);
            list.add(new ListItem("Data: " + data));
            list.add(new ListItem("Importo: "+importo.toString())+"€");
            list.add(new ListItem("Categoria: "+categoria));
            list.add(new ListItem("Mezzo di transazione: "+mezzo));
            list.add(new ListItem("Descrizione: "+descrizione));
            list.add(new ListItem("Luogo: "+luogo));
            subCatPart.add(list);

        }
        crs.close();
        document.add(catPart);
        document.newPage();
    }
}