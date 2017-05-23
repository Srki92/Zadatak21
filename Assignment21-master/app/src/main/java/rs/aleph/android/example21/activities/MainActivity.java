package rs.aleph.android.example21.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import rs.aleph.android.example21.R;
import rs.aleph.android.example21.adapters.DrawerListAdapter;
import rs.aleph.android.example21.dialogs.AboutDialog;
import rs.aleph.android.example21.fragments.DetailFragment;
import rs.aleph.android.example21.fragments.ListFragment;
import rs.aleph.android.example21.fragments.ListFragment.OnProductSelectedListener;
import rs.aleph.android.example21.model.NavigationItem;
import rs.aleph.android.example21.async.SimpleSyncTask;

public class MainActivity extends AppCompatActivity implements OnProductSelectedListener {

    /* The click listner for ListView in the navigation drawer */
    /*
    *Ova klasa predstavlja reakciju na klik neke od stavki iz navigation drawer-a
    *Kljucni element je 'int position' argument koji nam kaze koji tacno element
    *je izabran. To nam je dovoljno da odredimo koju akciju zelimo da pozovemo.
    */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItemFromDrawer(position);
        }
    }

    /**Drawer potrebni elementi*/
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private RelativeLayout drawerPane;
    private CharSequence drawerTitle;
    private CharSequence title;

    private ArrayList<NavigationItem> navigationItems = new ArrayList<NavigationItem>();

    private AlertDialog dialog;

    private boolean landscapeMode = false;
    private boolean listShown = false;
    private boolean detailShown = false;

    private int productId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Draws navigation items
        //U navigation drawer postavimo koje to elemente zelimo da imamo. Ikonicu, naziv i krratak opis
        navigationItems.add(new NavigationItem(getString(R.string.drawer_home), getString(R.string.drawer_home_long), R.drawable.ic_action_product));
        navigationItems.add(new NavigationItem(getString(R.string.drawer_settings), getString(R.string.drawer_Settings_long), R.drawable.ic_action_settings));
        navigationItems.add(new NavigationItem(getString(R.string.drawer_about), getString(R.string.drawer_about_long), R.drawable.ic_action_about));

        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerList = (ListView) findViewById(R.id.navList);

        // Populate the Navigtion Drawer with options
        drawerPane = (RelativeLayout) findViewById(R.id.drawerPane);

        //Prethodno definisanu listu koja sadrzi iokne,naslov i opis svake stavke postavimo u navigation drawer
        DrawerListAdapter adapter = new DrawerListAdapter(this, navigationItems);

        // set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //Zelimo da ragujemo na izbog stavki unutar navigation drawer-a.
        //Prethodno smo definisali klasu koja ce na osnovu pozicije ragovati
        //Ovde povezujemo izbor stavke i kako ragovati
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        drawerList.setAdapter(adapter);

        // Enable ActionBar app icon to behave as action to toggle nav drawer
        //Prva stvar koja je potrebna za rad sa toolbar-om jeste da ga pronadjemo
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Nakon toga potrebno je aplikaciji dopustiti da koristi toolbar
        setSupportActionBar(toolbar);

        //Zbog mera predostroznosti.Proveriti da li je rad sa toolbar-om moguc.
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //Ako jeste dopustimo klik na tu ikonu da bi mogli da otvaramo/zatvaramo navigation drawer
            actionBar.setDisplayHomeAsUpEnabled(true);
            //postavljamo ikonicu untar toolbar-a
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setHomeButtonEnabled(true);
            //i prikazujemo ikonu
            actionBar.show();
        }

        /*
        *Zelimo da postignemo da se navigation drawer otvara/zatvara uspesno.
        *Potrebno je da damo kontekst(aktivnost) u kome se prikazuje 'this'
        *toolbar na kojem ce se prikazivati ikona kao i menjati naslov 'toolbar'
        *i dva teksta sta prikazivati kada je navigation drawer otvoren/zatvoren.
        */
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            //kada se navigation drawer ne prikazuje zelimo da reagujemo na taj dogadjaj
            public void onDrawerClosed(View view) {
                //postavimo naslov u toolbar
                getSupportActionBar().setTitle(title);
                //i obrisemo sadrzaj toolbar-a.
                //Ako svaka nova aktivnost ili fragment ima drugaciji sadrzaj toolbar-a
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            //reagujemo kada se navigation drawer otvori
            public void onDrawerOpened(View drawerView) {
                //postavimo naslov u toolbar
                getSupportActionBar().setTitle(drawerTitle);
                //i obrisemo sadrzaj toolbar-a.
                //Ako svaka nova aktivnost ili fragment ima drugaciji sadrzaj toolbar-a
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        if (savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ListFragment listFragment = new ListFragment();
            ft.add(R.id.displayList, listFragment, "List_Fragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            selectItemFromDrawer(0);
        }

        if (findViewById(R.id.displayDetail) != null) {
            landscapeMode = true;
            getFragmentManager().popBackStack();

            DetailFragment detailFragment = (DetailFragment) getFragmentManager().findFragmentById(R.id.displayDetail);
            if (detailFragment == null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                detailFragment = new DetailFragment();
                ft.replace(R.id.displayDetail, detailFragment, "Detail_Fragment1");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                detailShown = true;
            }
        }

        listShown = true;
        detailShown = false;
        productId = 0;
    }

    @Override
    public void setTitle(CharSequence title) {
        //Namena ove metode jeste da promeni naslov koji se prikazuje unutar
        //toolbar-a
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /*
    *Ova metoda reaguje na izbor neke od stavki iz navigation drawer-a
    *Na osnovu pozicije iz liste navigation drawer-a odredimo sta tacno
    *zelimo da odradimo.
    */
    private void selectItemFromDrawer(int position) {
        if (position == 0){

        } else if (position == 1){
            Intent settings = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settings);
        } else if (position == 2){
            if (dialog == null){
                dialog = new AboutDialog(MainActivity.this).prepareDialog();
            } else {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

            dialog.show();
        }

        //ovom liniom oznacavamo elemtn iz liste da je selektovano.
        //Pozadinska boja elementa ce biti promenjena.
       drawerList.setItemChecked(position, true);

       //Menjamo naslov
       setTitle(navigationItems.get(position).getTitle());

       //I bitna stvar.Kada odradimo neku akciju zatvorimo navigation drawer
       drawerLayout.closeDrawer(drawerPane);
    }

    @Override
    public void onProductSelected(int id) {

        productId = id;

        if (landscapeMode) {
            DetailFragment detailFragment = (DetailFragment) getFragmentManager().findFragmentById(R.id.displayDetail);
            detailFragment.updateProduct(id);
        } else {
            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setProduct(id);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.displayList, detailFragment, "Detail_Fragment2");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack("Detail_Fragment2");
            ft.commit();
            listShown = false;
            detailShown = true;
        }
    }

    @Override
    public void onBackPressed() {

        if (landscapeMode) {
            finish();
        } else if (listShown == true) {
            finish();
        } else if (detailShown == true) {
            getFragmentManager().popBackStack();
            ListFragment listFragment = new ListFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.displayList, listFragment, "List_Fragment");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            listShown = true;
            detailShown = false;
        }

    }

    /**Toolbar**/
    /**
    *Ova metoda 'ubacuje' sadrzaj 'R.menu.activity_item_detail' fajla unutar toolbar-a
    *Svaki pojedinacan element 'R.menu.activity_item_detail' fajla ce biti jedno dugme
    *na toolbar-u
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_item_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
    *Metoda koja je zaduzena za rekaciju tj dogadjaj kada se klikne na neki
    *od dugmica u toolbar-u. Pozivom 'item.getItemId()' dobijamo jedinstveni identifikator
    *kliknutog dugmeta na osnovu cega mozemo da odredimo kako da reagujemo.
    *Svaki element unutar 'R.menu.activity_item_detail' fajla ima polje id na osnovu koga
    *lako mozemo odrediti sta smo tacno kliknuli.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Toast.makeText(MainActivity.this, "Sinhronizacija pokrenuta u pozadini niti. dobro :)",Toast.LENGTH_SHORT).show();
                new SimpleSyncTask(MainActivity.this).execute();
                break;
            case R.id.action_add:
                try {
                    Toast.makeText(MainActivity.this, "Sinhronizacija pokrenuta u glavnoj niti. Nije dobro :(",Toast.LENGTH_SHORT).show();
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    /**End*Toolbar**/

}


