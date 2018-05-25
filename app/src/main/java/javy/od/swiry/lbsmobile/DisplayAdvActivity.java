package javy.od.swiry.lbsmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
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

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javy.od.swiry.lbsmobile.models.Advert;

public class DisplayAdvActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private String ID;
    private boolean gotResult;
    private ProgressDialog progressDialog;
    private Timer timer;
    private Advert mAdvert;
    private ImageView mImage;
    private TextView mPrice;
    private TextView mTitle;
    private TextView mDate;
    private MultiAutoCompleteTextView mDescription;
    private TextView mLocalization;
    private TextView mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_adv);

        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(mContext, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }

        mContext = this;
        progressDialog = new ProgressDialog(this);
        mImage = findViewById(R.id.image);
        mPrice = findViewById(R.id.price);
        mTitle = findViewById(R.id.title);
        mDate = findViewById(R.id.date);
        mDescription = findViewById(R.id.description);
        mLocalization = findViewById(R.id.localization);
        mPhone = findViewById(R.id.phone);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.menu);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        String itemTitle = String.valueOf(menuItem.getTitle());
                        if(itemTitle.equals("Dodaj ogłoszenie"))
                        {
                            startActivity(new Intent(mContext,NewAdvActivity.class));
                        }
                        else if(itemTitle.equals("Ogłoszenia"))
                        {
                            startActivity(new Intent(mContext,MainMenuActivity.class));
                        }
                        else if(itemTitle.equals("Moje ogłoszenia"))
                        {
                            startActivity(new Intent(mContext,UserAdvActivity.class));
                        }
                        else if(itemTitle.equals("Wiadomości"))
                        {
                            //startActivity(new Intent(MainActivity.this,AddShopActivity.class));
                        } else if(itemTitle.equals("Wyloguj się")) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(mContext, StartActivity.class));
                        }
                        return true;
                    }
                });

        ID = getIntent().getStringExtra("ID");
        downloadAdv();
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
            startActivity(new Intent(mContext, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }
        downloadAdv();
    }

    public void downloadAdv(){
        progressDialog.setMessage("Wczytywanie ogłoszenia");
        progressDialog.show();
        DatabaseReference adverts = FirebaseDatabase.getInstance().getReference("adverts");
        adverts.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gotResult = true;
                Advert advert = dataSnapshot.getValue(Advert.class);
                if(advert != null) {
                    mAdvert = advert;
                }
                displayAdv();
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
                    Intent intent = new Intent(mContext, StartActivity.class);
                    intent.putExtra("fail","true");
                    startActivity(intent);
                }
            }
        };
        // Setting timeout of 10 sec to the request
        timer.schedule(timerTask, 10000L);
    }
    public void displayAdv(){
        String price = mAdvert.getPrice() + " zł";
        mPrice.setText(price);
        mTitle.setText(mAdvert.getTitle());
        mDate.setText(mAdvert.getDate());
        mDescription.setText(mAdvert.getDescription());
        mLocalization.setText(mAdvert.getLocalization());
        mPhone.setText(mAdvert.getPhone());
        Glide.with(mContext).load(mAdvert.getUrl()).into(mImage);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
