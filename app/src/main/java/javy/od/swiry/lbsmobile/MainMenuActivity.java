package javy.od.swiry.lbsmobile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javy.od.swiry.lbsmobile.models.Advert;

public class MainMenuActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mAdvertList;
    private ArrayList<Advert> listAdverts = new ArrayList<>();
    private ArrayList<Advert> listFiltered = new ArrayList<>();
    private ProgressDialog progressDialog;
    private boolean gotResult;
    private Timer timer;
    public static String searchCategory;
    private String searchText;
    private ImageView mBackground;
    private static boolean firstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference.goOffline();
            DatabaseReference.goOnline();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.menu);
        mAdvertList = findViewById(R.id.adverts);
        progressDialog = new ProgressDialog(this);
        searchText = "";
        mBackground = findViewById(R.id.background);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();

                        String itemTitle = String.valueOf(menuItem.getTitle());
                        if(itemTitle.equals("Dodaj ogłoszenie"))
                        {
                            startActivity(new Intent(MainMenuActivity.this,NewAdvActivity.class));
                        }
                        else if(itemTitle.equals("Ogłoszenia"))
                        {
                            //startActivity(new Intent(MainActivity.this,MapsActivity.class));
                        }
                        else if(itemTitle.equals("Moje ogłoszenia"))
                        {
                            startActivity(new Intent(MainMenuActivity.this,UserAdvActivity.class));
                        }
                        else if(itemTitle.equals("Wiadomości"))
                        {
                            //startActivity(new Intent(MainActivity.this,AddShopActivity.class));
                        } else if(itemTitle.equals("Wyloguj się")) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainMenuActivity.this, StartActivity.class));
                        }
                        return true;
                    }
                });
        generateAdv();
        listHandler();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference.goOffline();
            DatabaseReference.goOnline();
        }
        mBackground.setVisibility(View.GONE);
        if(searchCategory == null && searchText.equals("")) {
            setTitle("Ogłoszenia");
            displayAdv();
        } else if(searchCategory != null || !searchText.equals("")) {
            setTitle(searchCategory);
            filterResults(searchText);
        }
    }

    @Override
    protected void onStop() {
        searchCategory = null;
        super.onStop();
    }

    //Otwieranie bocznego menu przyciskiem z toolbara
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                stripAccents(query);
                searchText = query;
                filterResults(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")) {
                    searchText = "";
                    filterResults(searchText);
                }
                return false;
            }
        });

        MenuItem filterItem = menu.findItem(R.id.menuFilter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mBackground.setVisibility(View.VISIBLE);
                startActivity(new Intent(MainMenuActivity.this, Pop.class));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void filterResults(String query) {
        if(!progressDialog.isShowing()) {
            progressDialog.show();
        }
        listFiltered.clear();
        for(Advert a:listAdverts)
        {
            String x = stripAccents(a.getTitle().toLowerCase());
            if(x.contains(query.toLowerCase())) {
                if(searchCategory != null) {
                    if (a.getCategory().equals(searchCategory)) {
                        listFiltered.add(a);
                    }
                } else {
                    listFiltered.add(a);
                }
            }
        }
        displayFilteredAdv();
    }

    public static String stripAccents(String s)
    {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        s = s.replace("ł","l");
        s = s.replace("Ł", "L");
        return s;
    }


    public void displayFilteredAdv(){
        if(listFiltered.size() > 0) {
            Collections.sort(listFiltered,TimeComparator);
            FilteredCustomAdapter customAdapter = new FilteredCustomAdapter();
            mAdvertList.setAdapter(customAdapter);
        } else {
            mAdvertList.setAdapter(null);
            Toast.makeText(getApplicationContext(),"Brak wyników",Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    public void generateAdv() {
        progressDialog.setMessage("Wczytywanie ogłoszeń");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        gotResult = false;
        DatabaseReference adverts = FirebaseDatabase.getInstance().getReference("adverts");
        adverts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gotResult = true;
                listAdverts.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Advert advert = ds.getValue(Advert.class);
                    if(advert != null) {
                        advert.setID(ds.getKey());
                        listAdverts.add(0,advert);
                    }
                }
                if(searchCategory != null || !searchText.equals("")) {
                    filterResults(searchText);
                    if(progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } else {
                    displayAdv();
                    if(progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                gotResult = true;
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                if (!gotResult) { //  Timeout
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainMenuActivity.this, StartActivity.class);
                    intent.putExtra("fail","true");
                    startActivity(intent);
                }
            }
        };
        // Setting timeout of 10 sec to the request
        timer.schedule(timerTask, 10000L);
    }
    public void displayAdv() {
        if(listAdverts.size() > 0) {
            Collections.sort(listAdverts,TimeComparator);
            CustomAdapter customAdapter = new CustomAdapter();
            mAdvertList.setAdapter(customAdapter);
        }
    }

    private void listHandler() {
        mAdvertList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int i, long l) {
                TextView textView = v.findViewById(R.id.advID);
                String advID = (String)textView.getText();
                Intent intent = new Intent(getApplicationContext(), DisplayAdvActivity.class);
                intent.putExtra("ID", advID);
                startActivity(intent);
            }
        });
    }



    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listAdverts.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.adv_adapter, null);
            TextView advID = convertView.findViewById(R.id.advID);
            ImageView mainImage = convertView.findViewById(R.id.image);
            TextView mainText = convertView.findViewById(R.id.name);
            TextView city = convertView.findViewById(R.id.city);
            TextView date = convertView.findViewById(R.id.date);
            TextView price = convertView.findViewById(R.id.price);

            advID.setText(listAdverts.get(position).getID());
            Glide.with(MainMenuActivity.this).load(listAdverts.get(position).getUrl()).into(mainImage);
            mainText.setText(listAdverts.get(position).getTitle());
            city.setText(listAdverts.get(position).getLocalization());
            date.setText(listAdverts.get(position).getDate());
            String sPrice = listAdverts.get(position).getPrice() + " zł";
            price.setText(sPrice);
            return convertView;
        }
    }

    public class FilteredCustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.adv_adapter, null);
            TextView advID = convertView.findViewById(R.id.advID);
            ImageView mainImage = convertView.findViewById(R.id.image);
            TextView mainText = convertView.findViewById(R.id.name);
            TextView city = convertView.findViewById(R.id.city);
            TextView date = convertView.findViewById(R.id.date);
            TextView price = convertView.findViewById(R.id.price);

            advID.setText(listFiltered.get(position).getID());
            Glide.with(MainMenuActivity.this).load(listFiltered.get(position).getUrl()).into(mainImage);
            mainText.setText(listFiltered.get(position).getTitle());
            city.setText(listFiltered.get(position).getLocalization());
            date.setText(listFiltered.get(position).getDate());
            String sPrice = listFiltered.get(position).getPrice() + " zł";
            price.setText(sPrice);
            return convertView;
        }
    }

    public Comparator<Advert> TimeComparator
            = new Comparator<Advert>() {
        public int compare(Advert d1, Advert d2) {
            if (d1.getTime() != null && d2.getTime() != null) {
                return (int)(Long.valueOf(d2.getTime()) - Long.valueOf(d1.getTime()));
            } else {
                return 0;
            }
        }
    };
}
