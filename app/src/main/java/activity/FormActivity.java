package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;
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
        TextView date;
        TextView time;
        String years;
        String month;
        String day;
        String hours;
        String minutes;
        String date_order;
        String time_order;
        Calendar dateAndTime=Calendar.getInstance();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_form);
            inputFullName = (EditText) findViewById(R.id.name);
            inputSecName = (EditText) findViewById(R.id.secname);
            btnRegister = (Button) findViewById(R.id.btnRegister);
            vText=(TextView)findViewById(R.id.vText);
            vText2=(TextView)findViewById(R.id.vText2);
            vText3=(TextView)findViewById(R.id.vText3);
            date=(TextView)findViewById(R.id.date);
            time=(TextView)findViewById(R.id.time);
            Intent intent=getIntent();
            String nameplace=intent.getStringExtra("name");
            String address=intent.getStringExtra("address");
            String count_places=intent.getStringExtra("count_places");
            vText.setText("Parking name: "+ nameplace);
            vText2.setText("Parking address: "+ address);
            vText3.setText("count free places: " + count_places);
            // Progress dialog
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);

            //

            //session manager
            session = new SessionManager(getApplicationContext());
            if (!session.isLoggedIn()) {
                logoutUser();
            }

            // SQLite database handler
            db = new SQLiteHandler(getApplicationContext());


            // Register Button Click event
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String name = inputFullName.getText().toString().trim();
                    String surname = inputSecName.getText().toString().trim();

                    if (!name.isEmpty() && !surname.isEmpty()) {
                        registerOrder(name, surname, nameplace, date_order, time_order);

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Please enter your details!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });

           date.setOnClickListener(new View.OnClickListener() {
               public void onClick(View view) {
                   setDate(view);
               }
           });
           time.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    setTime(view);
                }
            });

        }

        /**
         * Function to store user in MySQL database will post params(tag, name,
         * email, password) to register url
         * */
        private void registerOrder(final String name, final String surname, final String nameplace, final String date_order, final String time_order) {
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
                                String date_s = order.getString("date_s");
                                String time_s = order.getString("time_s");
                                // Inserting row in users table
                                db.addOrder(name1, surname1, uid, created_at, date_s, time_s);

                                Toast.makeText(getApplicationContext(), "Order successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                                // Launch key activity
                                Intent intent = new Intent(FormActivity.this,
                                        KeyActivity.class);
                                startActivity(intent);
                                finish();
                                decrement(nameplace);
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
                    params.put("date_s", date_order);
                    params.put("time_s", time_order);
                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }

        //decrement

    private void decrement(final String name) {
        // Tag used to cancel the request
        String tag_string_req = "req_decrement";
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_DECREMENT, response -> {
            Log.d(TAG, "Decrement Response: " + response.toString());
            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                if (!error) {
                } else {
                    // Error occurred in decrement. Get the error
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
                Log.e(TAG, "Error 101: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to decrement url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void setDate(View v) {
        new DatePickerDialog(FormActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime(View v) {
        new TimePickerDialog(FormActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    private void setInitialDateTime() {

        date.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE));
        time.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t=new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            setInitialDateTime();
            time_order=String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
        }
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
            date_order=String.valueOf(year) + "-" + String.valueOf(monthOfYear+1) + "-" + String.valueOf(dayOfMonth);
        }
    };

    /*
    *
    *   private void increment(final String name) {
        // Tag used to cancel the request
        String tag_string_req = "req_increment";
        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_INCREMENT, response -> {
            Log.d(TAG, "Increment Response: " + response.toString());
            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                if (!error) {
                } else {
                    // Error occurred in increment. Get the error
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
                Log.e(TAG, "Error 102: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to increment url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    *
    * */

        private void showDialog() {
            if (!pDialog.isShowing())
                pDialog.show();
        }

        private void hideDialog() {
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    public void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(FormActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    }