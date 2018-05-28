package javy.od.swiry.lbsmobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javy.od.swiry.lbsmobile.models.Advert;
import javy.od.swiry.lbsmobile.models.AdvertCount;

public class EditAdvActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;

    private ImageView mGallery;
    private EditText mTitle;
    //private EditText mCategory;
    private EditText mDescription;
    private EditText mLocalization;
    private EditText mPhone;
    private EditText mPrice;
    private Context mContext;
    private StorageReference mStorageRef;
    private Uri file;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialog2;
    private FirebaseAuth firebase;
    private String uid;
    private static String count;
    private boolean imageAdded;
    private AdvertCount advertID;
    private DrawerLayout mDrawerLayout;
    private Spinner mCategory;
    private String date;
    private String ID;
    private boolean gotResult;
    private Advert mAdvert;
    private Timer timer;
    private boolean imageNotChanged;
    private Bitmap bitmap;
    private Timer timer2;
    private boolean gotResult2;
    private Timer timer3;
    private boolean gotResult3;
    private Timer timerResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_adv);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.menu);

        advertID = new AdvertCount();
        mContext = this;
        mGallery = findViewById(R.id.gallery);
        mTitle = findViewById(R.id.title);

        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(mContext, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }

        mCategory = findViewById(R.id.category);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Categories));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(myAdapter);


        mDescription = findViewById(R.id.description);
        mLocalization = findViewById(R.id.localization);
        mPhone = findViewById(R.id.phone);
        mPrice = findViewById(R.id.price);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        progressDialog2 = new ProgressDialog(this);

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
        fillData();
        galleryOnClick();
        categoryHandler();
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
    protected void onResume(){
        //Sprawdzenie czy użytkownik jest zalogowany
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(mContext, MainActivity.class));
            Toast.makeText(this,"Aby kontynuować musisz się zalogować",Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    public void fillData(){
        progressDialog2.setMessage("Wczytywanie danych");
        progressDialog2.show();
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
                progressDialog2.dismiss();
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
        mPrice.setText(mAdvert.getPrice());
        mTitle.setText(mAdvert.getTitle());
        date = mAdvert.getDate();
        mDescription.setText(mAdvert.getDescription());
        mLocalization.setText(mAdvert.getLocalization());
        mPhone.setText(mAdvert.getPhone());
        Glide.with(mContext).load(mAdvert.getUrl()).into(mGallery);

        String spinnerValue = mAdvert.getCategory();
        ArrayAdapter myAdap = (ArrayAdapter) mCategory.getAdapter();
        int spinnerPosition = myAdap.getPosition(spinnerValue);
        mCategory.setSelection(spinnerPosition);

        imageAdded = true;
        imageNotChanged = true;
        progressDialog2.dismiss();
    }

    public void Apply(View v)
    {
        if(imageAdded) {
            if(!mTitle.getText().toString().equals("") && !mDescription.getText().toString().equals("")
                    && !mPrice.getText().toString().equals("")) {
                progressDialog.setMessage("Aktualizowanie ogłoszenia");
                progressDialog.show();
                gotResult2 = false;
                try {
                    Advert advert = new Advert();
                    advert.setTitle(mTitle.getText().toString());
                    advert.setCategory(mCategory.getSelectedItem().toString());
                    advert.setPrice(mPrice.getText().toString());
                    advert.setDescription(mDescription.getText().toString());
                    advert.setLocalization(mLocalization.getText().toString());
                    advert.setPhone(mPhone.getText().toString());
                    advert.setDate(date);
                    advert.setTime(mAdvert.getTime());
                    // Write a message to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    DatabaseReference mDatabase = database.getReference();
                    mDatabase.child("adverts").child(ID).setValue(advert)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    gotResult2 = true;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    gotResult2 = true;
                                    Toast.makeText(mContext, "Nie udało się zaktualizować ogłoszenia", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                    timer2 = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            timer2.cancel();
                            if (!gotResult2) { //  Timeout
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(mContext, StartActivity.class);
                                intent.putExtra("fail","true");
                                startActivity(intent);
                            }
                        }
                    };
                    // Setting timeout of 10 sec to the request
                    timer2.schedule(timerTask, 10000L);
                    addImage();

                } catch (Exception e) {
                    Toast.makeText(mContext, "Nie udało się zaktualizować ogłoszenia", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this,"Musisz podać wymagane dane",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"Musisz dodać zdjęcie",Toast.LENGTH_SHORT).show();
        }
    }

    public void addImage() {
        if(!imageNotChanged) {
            gotResult3 = false;
            StorageReference images = mStorageRef.child(ID + ".jpg");

            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                bmp = resizeImage(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            //uploading the image
            images.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            gotResult3 = true;
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            mDatabase.child("adverts").child(ID).child("url").setValue(taskSnapshot.getDownloadUrl().toString());
                            Toast.makeText(mContext, "Zaktualizowano ogłoszenie", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(mContext, UserAdvActivity.class));
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            gotResult3 = true;
                            Toast.makeText(mContext, "Nie udało się zaktualizować ogłoszenia", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
            timer3 = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    timer3.cancel();
                    if (!gotResult3) { //  Timeout
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(mContext, StartActivity.class);
                        intent.putExtra("fail","true");
                        startActivity(intent);
                    }
                }
            };
            // Setting timeout of 10 sec to the request
            timer3.schedule(timerTask, 10000L);
        } else {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("adverts").child(ID).child("url").setValue(mAdvert.getUrl());
            Toast.makeText(mContext, "Zaktualizowano ogłoszenie", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(mContext,UserAdvActivity.class));
            progressDialog.dismiss();
        }

    }

    public Bitmap resizeImage(Bitmap bitmap) {
        int maxWidth = 1920;
        int maxHeight = 1920;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        bitmap = bitmap.createScaledBitmap(bitmap,width,height,true);
        return bitmap;
    }

    public void galleryOnClick(){
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int maxWidth = 512;
                int maxHeight = 512;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > height) {
                    // landscape
                    float ratio = (float) width / maxWidth;
                    width = maxWidth;
                    height = (int)(height / ratio);
                } else if (height > width) {
                    // portrait
                    float ratio = (float) height / maxHeight;
                    height = maxHeight;
                    width = (int)(width / ratio);
                } else {
                    // square
                    height = maxHeight;
                    width = maxWidth;
                }
                bitmap = bitmap.createScaledBitmap(bitmap,width,height,true);
                mGallery.setImageBitmap(bitmap);
                file = imageUri;
                imageAdded = true;
                imageNotChanged = false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void categoryHandler(){
        mCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {

            }
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }

    public void Delete(View v){
        StorageReference image = mStorageRef.child(ID + ".jpg");
        image.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("adverts").child(ID).removeValue();
        Toast.makeText(mContext,"Usunięto ogłoszenie",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(mContext,UserAdvActivity.class));
    }
}
