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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javy.od.swiry.lbsmobile.models.Advert;
import javy.od.swiry.lbsmobile.models.AdvertCount;

public class NewAdvActivity extends AppCompatActivity {

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
    private FirebaseAuth firebase;
    private String uid;
    private static String count;
    private boolean imageAdded;
    private AdvertCount advertID;
    private DrawerLayout mDrawerLayout;
    private Spinner mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_adv);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = findViewById(R.id.menu);

        advertID = new AdvertCount();
        mContext = this;
        mGallery = findViewById(R.id.gallery);
        galleryOnClick();
        mTitle = findViewById(R.id.title);

        mCategory = findViewById(R.id.category);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Categories));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategory.setAdapter(myAdapter);
        categoryHandler();

        mDescription = findViewById(R.id.description);
        mLocalization = findViewById(R.id.localization);
        mPhone = findViewById(R.id.phone);
        mPrice = findViewById(R.id.price);
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
                            //startActivity(new Intent(NewAdvActivity.this,NewAdvActivity.class));
                        }
                        else if(itemTitle.equals("Ogłoszenia"))
                        {
                            startActivity(new Intent(NewAdvActivity.this,MainMenuActivity.class));
                        }
                        else if(itemTitle.equals("Moje ogłoszenia"))
                        {
                            startActivity(new Intent(NewAdvActivity.this,UserAdvActivity.class));
                        }
                        else if(itemTitle.equals("Wiadomości"))
                        {
                            //startActivity(new Intent(MainActivity.this,AddShopActivity.class));
                        } else if(itemTitle.equals("Wyloguj się")) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(NewAdvActivity.this, StartActivity.class));
                        }
                        return true;
                    }
                });

        firebase = FirebaseAuth.getInstance();
        FirebaseUser user = firebase.getCurrentUser();
        uid = user.getUid();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("advertID/" + uid);
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    count = ds.getValue(String.class);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void Apply(View v)
    {
        if(imageAdded) {
            if(!mTitle.getText().toString().equals("") && !mDescription.getText().toString().equals("")
                    && !mPrice.getText().toString().equals("")) {
                progressDialog.setMessage("Dodawanie ogłoszenia");
                progressDialog.show();
                try {
                    FirebaseUser user = firebase.getCurrentUser();
                    uid = user.getUid();
                    Advert advert = new Advert();
                    advert.setTitle(mTitle.getText().toString());
                    advert.setCategory(mCategory.getSelectedItem().toString());
                    advert.setPrice(mPrice.getText().toString());
                    advert.setDescription(mDescription.getText().toString());
                    advert.setLocalization(mLocalization.getText().toString());
                    advert.setPhone(mPhone.getText().toString());
                    Calendar mCurrentDate = Calendar.getInstance();
                    int month = mCurrentDate.get(Calendar.MONTH);
                    String monthName = new DateFormatSymbols(Locale.getDefault()).getShortMonths()[month];
                    String capMonthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                    int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
                    String date = String.valueOf(day) + " " + String.valueOf(capMonthName);
                    advert.setDate(date);
                    advert.setTime(String.valueOf(mCurrentDate.getTimeInMillis()));
                    // Write a message to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                    advertID = new AdvertCount();
                    if (count != null) {
                        String x = String.valueOf(Integer.valueOf(count) + 1);
                        advertID.setCount(x);
                    } else {
                        advertID.setCount("0");
                    }

                    DatabaseReference mDatabase = database.getReference();

                    mDatabase.child("advertID").child(uid).setValue(advertID);

                    mDatabase.child("adverts").child(uid + "_" + advertID.getCount()).setValue(advert)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    addImage();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewAdvActivity.this, "Nie udało się dodać ogłoszenia", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(NewAdvActivity.this, "Nie udało się dodać ogłoszenia", Toast.LENGTH_SHORT).show();
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
        StorageReference images = mStorageRef.child(uid+"_"+advertID.getCount()+".jpg");
        images.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("adverts").child(uid+"_"+advertID.getCount()).child("url").setValue(taskSnapshot.getDownloadUrl().toString());
                        Toast.makeText(NewAdvActivity.this, "Dodano ogłoszenie", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(NewAdvActivity.this,MainMenuActivity.class));
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(NewAdvActivity.this, "Nie udało się dodać ogłoszenia", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
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
        /*Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
        startActivityForResult(chooserIntent, PICK_IMAGE);*/
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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
}
