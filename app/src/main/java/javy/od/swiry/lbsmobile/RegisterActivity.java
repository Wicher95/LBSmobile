package javy.od.swiry.lbsmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
        private Button bRegister;
        private EditText etPassword;
        private EditText etUsername;

        private ProgressDialog progressDialog;

        private FirebaseAuth firebaseAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);

            progressDialog = new ProgressDialog(this);
            firebaseAuth = FirebaseAuth.getInstance();

            bRegister = findViewById(R.id.bRegister);
            etPassword = findViewById(R.id.etPassword);
            etUsername = findViewById(R.id.etUsername);

            bRegister.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view == bRegister) {
                registerUser();
            }
        }

        private void registerUser() {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Prosze wprowadz email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Prosze wprowadz haslo", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog.setMessage("Rejestrowanie użytkownika");
            progressDialog.show();


            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Rejestracja poprawna", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                progressDialog.dismiss();
                            } else if(!task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this, "Niezajestrowany, spróbuj jeszcze raz", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                            else {
                               // Toast.makeText(RegisterActivity.this, "Niezajestrowany, spróbuj jeszcze raz", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
    }