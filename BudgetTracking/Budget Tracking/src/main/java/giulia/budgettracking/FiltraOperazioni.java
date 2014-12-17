package giulia.budgettracking;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class FiltraOperazioni extends ActionBarActivity {
    int tipo_filtro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        tipo_filtro = i.getIntExtra("tipoFiltro", -1);/*-1 è il valore di default, se va storto qualcosa*/

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtra_operazioni);
        if (savedInstanceState == null) {
            if(tipo_filtro == 0){/*Fragment giorno*/
                FragmentFiltraGiorno fragobj = new FragmentFiltraGiorno();
                getFragmentManager().beginTransaction().add(R.id.container,fragobj).commit();
            }
            else if(tipo_filtro == 1){/*Fragment settimana*/
                Bundle bundle = new Bundle();
                bundle.putString("message", "settimana");
                //set Fragmentclass Arguments
                FragmentMeseSett fragobj = new FragmentMeseSett();
                fragobj.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragobj)
                        .commit();
            }
            else if(tipo_filtro == 2){/*Fragment mese*/
                Bundle bundle = new Bundle();
                bundle.putString("message", "mese");
                //set Fragmentclass Arguments
                FragmentMeseSett fragobj = new FragmentMeseSett();
                fragobj.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragobj)
                        .commit();
            }
            else if(tipo_filtro == 3){/*Fragment totale*/
                Bundle bundle = new Bundle();
                bundle.putString("message", "totale");
                //set Fragmentclass Arguments
                FragmentMeseSett fragobj = new FragmentMeseSett();
                fragobj.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragobj)
                        .commit();
            }
        }
    }

}
