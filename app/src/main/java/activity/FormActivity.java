package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import static java.lang.Integer.parseInt;

public class FormActivity extends Activity {

        private static final String TAG = activity.RegisterActivity.class.getSimpleName();
        private Button btnRegister;
       // private Button btnLinkToLogin;
        private EditText inputFullName;
        private EditText inputSecName;
        private ProgressDialog pDialog;
        private SessionManager session;
        private SQLiteHandler db;
        SQLiteDatabase datb;
        TextView vText;
        TextView vText2;
        TextView vText3;
        String query;
        String queryIns;
        Cursor cursor;
        String count;
        //SQLiteDatabase sqdb=db.getWritableDatabase();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_form);
            queryIns="INSERT INTO " + db.TABLE_PLACES + "(" + db.KEY_COUNT_PLACE + ") VALUES ('" + count + "')";

            query="SELECT" +  db.KEY_COUNT_PLACE + " FROM "+ db.TABLE_PLACES + "AS A " +
                    "INNER JOIN " + db.TABLE_ORDER + "AS B" + " ON " + "A." + db.KEY_UID + "=" + "B." + db.KEY_UID;

            inputFullName = (EditText) findViewById(R.id.name);
            inputSecName = (EditText) findViewById(R.id.secname);
            btnRegister = (Button) findViewById(R.id.btnRegister);
            vText=(TextView)findViewById(R.id.vText);
            vText2=(TextView)findViewById(R.id.vText2);
            vText3=(TextView)findViewById(R.id.vText3);
            // btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
            Intent intent=getIntent();
            String name=intent.getStringExtra("name");
            String address=intent.getStringExtra("address");
            String count_places=intent.getStringExtra("count_places");
            vText.setText("Parking name: "+ name);
            vText2.setText("Parking address: "+ address);
            vText3.setText("count free places: " + count_places);
            // Progress dialog
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);

            // Session manager
            session = new SessionManager(getApplicationContext());

            // SQLite database handler
            db = new SQLiteHandler(getApplicationContext());

            // Check if user is already logged in or not
           /*if (session.isLoggedIn()) {
                // User is already logged in. Take him to main activity
                Intent intent = new Intent(activity.FormActivity.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }*/

            // Register Button Click event
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String name = inputFullName.getText().toString().trim();
                    String surname = inputSecName.getText().toString().trim();

                    if (!name.isEmpty() && !surname.isEmpty()) {
                        registerOrder(name, surname);
                        Intent intent = new Intent(FormActivity.this,
                                KeyActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your details!", Toast.LENGTH_LONG)
                                .show();
                    }

                    //получение и изменение колличества мест в SQLite

                    /*SQLiteDatabase bd=db.getReadableDatabase();
                    cursor = bd.rawQuery(query,null);
                    /*cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                    count = cursor.getString(cursor.getColumnIndex(db.KEY_COUNT_PLACE));
                    int cnt_plc = parseInt(count);
                    cnt_plc--;
                    count = String.valueOf(cnt_plc);
                    sqdb.execSQL(queryIns);*/
                }
            });

            // Link to Login Screen
        /*    btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

                }
            });*/

        }

        /**
         * Function to store user in MySQL database will post params(tag, name,
         * email, password) to register url
         * */
        private void registerOrder(final String name, final String surname) {
            // Tag used to cancel the request
            String tag_string_req = "req_register";

            pDialog.setMessage("Registering ...");
            showDialog();

            StringRequest strReq = new StringRequest(Method.POST,
                    AppConfig.URL_ORDER, response -> {
                        Log.d(TAG, "Register Response: " + response.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                // User successfully stored in MySQL
                                // Now store the user in sqlite
                                String uid = jObj.getString("uid");
                                JSONObject order = jObj.getJSONObject("order");
                                String name1 = order.getString("name");
                                String surname1 = order.getString("surname");
                                String created_at = order.getString("created_at");


                                // Inserting row in users table
                                db.addOrder(name1, surname1, uid, created_at);

                                Toast.makeText(getApplicationContext(), "Order successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                                // Launch login activity
                            } else {

                                // Error occurred in registration. Get the error
                                // message
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Registration Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting params to register url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("name", name);
                    params.put("surname", surname);


                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }

        private void showDialog() {
            if (!pDialog.isShowing())
                pDialog.show();
        }

        private void hideDialog() {
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    public void logoutOrder() {
        session.setLogin(false);

        db.deleteOrders();

        // Launching the login activity
        Intent intent = new Intent(FormActivity.this, ParkingListActivity.class);
        startActivity(intent);
        finish();
    }
    }