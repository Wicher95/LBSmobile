package javy.od.swiry.lbsmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private Button bLogin;
    private EditText etPassword;
    private EditText etUsername;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private LoginButton loginButton;
    private TextView isLogin;

    public void click(View view) {
        switch (view.getId()) {
            case R.id.Register:
                Intent Register = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(Register);
                break;
        }
    }

    public void login(View view) {
        switch (view.getId()) {
            case R.id.bLogin:
                loginUser();
                break;
        }
    }

    private void loginUser() {
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "Podany email jest nieprawidłowy ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Podane hasło jest nieprawidłowe", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Logowanie użytkowanika ...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Logowanie poprawne", Toast.LENGTH_SHORT).show();
                            finish();
                            Intent logo = new Intent(getApplicationContext(), ProgramActivity.class);
                            startActivity(logo);
                        } else {
                            Toast.makeText(MainActivity.this, "Podany login lub hasło są nieprawidłowe", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        isLogin = findViewById(R.id.isLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        bLogin = findViewById(R.id.bLogin);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null) {
            if (!accessToken.isExpired()) {
                Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void facebookLogin(View v)
    {
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>()
                {
                    @Override
                    public void onSuccess(LoginResult loginResult)
                    {
                        isLogin.setText("Zalogowano");
                        Toast.makeText(MainActivity.this,"Login sucess",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel()
                    {
                        isLogin.setText("Logowanie anulowano");
                        Toast.makeText(MainActivity.this,"Login canceled",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception)
                    {
                        isLogin.setText("Logowanie nie udane");
                        Toast.makeText(MainActivity.this,"Login failed",Toast.LENGTH_LONG).show();
                    }
                });

    }
}
