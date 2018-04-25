package javy.od.swiry.lbsmobile;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuthException;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity{
private Button bRegister;
private EditText etPassword;
private EditText etUsername;

private ProgressDialog progressDialog;

private FirebaseAuthException firebaseAuthException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

       firebaseAuthException= FirebaseAuthException.getInstance();

        progressDialog = new ProgressDialog(this);

        bRegister = findViewById(R.id.bRegister);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);


        private void registerUser(){
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Prosze wprowadz email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Prosze wprowadz haslo", Toast.LENGTH_SHORT).show();
            return;
        }
            progressDialog.setMessage("Rejestrowanie użytkownika");
            progressDialog.show();

    }

        @Override
        public void onClick(View view) {
            if (view == bRegister) {
                registerUser();
            }

    }
}
