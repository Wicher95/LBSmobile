package javy.od.swiry.lbsmobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Calendar;

import javy.od.swiry.lbsmobile.models.Advert;
import javy.od.swiry.lbsmobile.models.AdvertCount;

public class NewAdvActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;

    private ImageView mGallery;
    private EditText mTitle;
    private EditText mCategory;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_adv);
        advertID = new AdvertCount();
        mContext = this;
        mGallery = findViewById(R.id.gallery);
        galleryOnClick();
        mTitle = findViewById(R.id.title);
        mCategory = findViewById(R.id.category);
        mDescription = findViewById(R.id.description);
        mLocalization = findViewById(R.id.localization);
        mPhone = findViewById(R.id.phone);
        mPrice = findViewById(R.id.price);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
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

    public void Apply(View v)
    {
        if(imageAdded) {
            progressDialog.setMessage("Dodawanie ogłoszenia");
            progressDialog.show();
            try {
                FirebaseUser user = firebase.getCurrentUser();
                uid = user.getUid();
                Advert advert = new Advert();
                advert.setTitle(mTitle.getText().toString());
                advert.setCategory(mCategory.getText().toString());
                advert.setPrice(mPrice.getText().toString());
                advert.setDescription(mDescription.getText().toString());
                advert.setLocalization(mLocalization.getText().toString());
                advert.setPhone(mPhone.getText().toString());
                Calendar mCurrentDate = Calendar.getInstance();
                int month = mCurrentDate.get(Calendar.MONTH) + 1;
                int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
                String date = String.valueOf(day) + " " + String.valueOf(month);
                advert.setDate(date);
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
}
