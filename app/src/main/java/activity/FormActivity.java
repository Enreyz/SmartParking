package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.example.smartparking.R;
import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;
public class FormActivity extends Activity {

        private static final String TAG = activity.RegisterActivity.class.getSimpleName();
        private Button btnRegister;
       // private Button btnLinkToLogin;
        private EditText inputFullName;
        private EditText inputSecName;
        private ProgressDialog pDialog;
        private SessionManager session;
        private SQLiteHandler MyHelper;
        TextView vText;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_form);

            inputFullName = (EditText) findViewById(R.id.name);
            inputSecName = (EditText) findViewById(R.id.email);
            btnRegister = (Button) findViewById(R.id.btnRegister);
            vText=(TextView)findViewById(R.id.vText);
           // btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
            Intent intent=getIntent();
            String info=intent.getStringExtra("name");
            vText.setText("Parking info: "+ info);
            // Progress dialog
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);

            // Session manager
            session = new SessionManager(getApplicationContext());

            if (!session.isLoggedIn()) {
                logoutUser();
            }

            // SQLite database handler
            MyHelper = new SQLiteHandler(getApplicationContext());

            // Check if user is already logged in or not


            // Register Button Click event
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    /*String name = inputFullName.getText().toString().trim();
                    String secname = inputSecName.getText().toString().trim();

                    if (!name.isEmpty() && !secname.isEmpty()) {
                        //registerOrder(name, secname);*/
                        Intent intent = new Intent(FormActivity.this, KeyActivity.class);
                        startActivity(intent);
                        finish();
                    }/* else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your details!", Toast.LENGTH_LONG)
                                .show();
                    }
                }*/
            });

        }

    public void logoutUser() {
        session.setLogin(false);

        MyHelper.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(FormActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    }