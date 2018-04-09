package javy.od.swiry.lbsmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private LoginButton loginButton;
    private TextView isLogin;

    public void click(View view) {
        switch (view.getId()) {
            case R.id.bRegister:
                Intent Register = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(Register);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        isLogin = findViewById(R.id.isLogin);
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
