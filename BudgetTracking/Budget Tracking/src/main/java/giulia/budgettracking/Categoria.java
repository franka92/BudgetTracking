package giulia.budgettracking;

/**
 * Created by frank_000 on 13/07/14.
 */
public class Categoria {
    String cat_name;
    int cat_type;   /*Il tipo indica per quale tipo di operazione viene usato: 0 --> per i pagamenti , 1 --> per le entrate*/

    public Categoria(String s, int i){
        this.cat_name = s;
        this.cat_type = i;
    }

    public int getType() {
        return cat_type;
    }

    public String getName() {
        return cat_name;
    }
    @Override
    public String toString() {
        return  cat_name;
    }

}
