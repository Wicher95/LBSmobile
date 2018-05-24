package javy.od.swiry.lbsmobile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javy.od.swiry.lbsmobile.models.Advert;

public class MainMenuActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mAdvertList;
    private ArrayList<Uri> mImageList = new ArrayList<>();
    private ArrayList<Advert> listAdverts = new ArrayList<>();
    private File tmpFile;
    private ProgressDialog progressDialog;
    private boolean gotResult;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.menu);
        mAdvertList = findViewById(R.id.adverts);
        progressDialog = new ProgressDialog(this);

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
                            //startActivity(new Intent(MainActivity.this,AddShopActivity.class));
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
    protected void onResume()
    {
        super.onResume();
        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }
        generateAdv();
    }


    public void generateAdv() {
        progressDialog.setMessage("Wczytywanie ogłoszeń");
        progressDialog.show();
        DatabaseReference adverts = FirebaseDatabase.getInstance().getReference("adverts");
        adverts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gotResult = true;
                listAdverts.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Advert advert = ds.getValue(Advert.class);
                    if(advert != null) {
                        listAdverts.add(0,advert);
                    }
                }
                displayAdv();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                gotResult = true;
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
    public void displayAdv(){
        if(listAdverts.size() > 0) {
            CustomAdapter customAdapter = new CustomAdapter();
            mAdvertList.setAdapter(customAdapter);
        }
        progressDialog.dismiss();
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

            Glide.with(MainMenuActivity.this).load(listAdverts.get(position).getUrl()).into(mainImage);
            mainText.setText(listAdverts.get(position).getTitle());
            city.setText(listAdverts.get(position).getLocalization());
            date.setText(listAdverts.get(position).getDate());
            String sPrice = listAdverts.get(position).getPrice() + " zł";
            price.setText(sPrice);
            return convertView;
        }
    }
}
