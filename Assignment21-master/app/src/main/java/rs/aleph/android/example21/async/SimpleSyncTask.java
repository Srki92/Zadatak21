package rs.aleph.android.example21.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import rs.aleph.android.example21.R;
import rs.aleph.android.example21.fragments.ListFragment;

/**
 * Created by milossimic on 10/22/16.
 * AsyncTask klasa prima tri parametra prilikom specijalizacije
 * Korisnici sami definisu tip u zavisnosti od posla koji zele da obave.
 *
 * Prvi argument predstavlja ulazne parametre, ono so zelimo
 * da posaljemo zadatku. Recimo ime fajla koji zelimo da skinemo
 *
 * Drugi argument je indikator kako ce se meriti progres. Koliko je posla
 * zavrseno i koliko je opsla ostalo.
 *
 * Treci parametar je povratna vrednost, tj sta ce metoda doInBackground
 * vratiti kao poratnu vrednost metodi onPostExecute
 */
public class SimpleSyncTask extends AsyncTask<Void, Void, Void>{

    private Activity activity;
    private ListFragment.OnProductSelectedListener listener;

    public SimpleSyncTask(Activity activity) {
        this.activity = activity;
        listener = (ListFragment.OnProductSelectedListener) activity;
    }

    /**
     * Metoda se poziva pre samog starta pozadinskog zadatka
     * Sve pripreme odraditi u ovoj metodi, ako ih ima.
     */
    @Override
    protected void onPreExecute() {
    }

    /**
     * Posao koji se odvija u pozadini, ne blokira glavnu nit aplikacije.
     * Sav posao koji dugo traje izvrsavati unutar ove metode.
     */
    @Override
    protected Void doInBackground(Void... params) {

        //simulacija posla koji se obavlja u pozadini i traje duze vreme
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void fillProducts(){
        // Load product names from array resource
        String[] products = activity.getResources().getStringArray(R.array.product_names);

        // Create an ArrayAdaptar from the array of Strings
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, products);
        ListView listView = (ListView) activity.findViewById(R.id.products);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send the URL to the host activity
                listener.onProductSelected((int)id);
            }
        });
    }

    /**
     *
     * Kada se posao koji se odvija u pozadini zavrsi, poziva se ova metoda
     * Ako je potrebno osloboditi resurse ili obrisati elemente koji vise ne trebaju.
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(activity, "Sync done", Toast.LENGTH_SHORT).show();
        fillProducts();
    }
}
