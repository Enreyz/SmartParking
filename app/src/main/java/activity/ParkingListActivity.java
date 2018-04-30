package activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ListView;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import helper.SQLiteHandler;
import helper.SessionManager;

import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.smartparking.R;

import java.util.ArrayList;

public class ParkingListActivity extends Activity {
    private static final String TAG = ParkingListActivity.class.getSimpleName();
    ListView userList;
    TextView header;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;
    private SessionManager session;
    private SQLiteHandler MyHelper;
    private SQLiteDatabase db;
    String HttpJSonURL = "http://172.20.10.3/android_login_api/list.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In onCreate at PLA ");
        setContentView(R.layout.activity_parkinglist);
        header = (TextView) findViewById(R.id.header);
        userList = (ListView) findViewById(R.id.list);
        MyHelper = new SQLiteHandler(getApplicationContext());
        MyHelper.deletePlaces();
        new SyncDB(ParkingListActivity.this).execute();
        //session manager


        Log.d(TAG, "In onCreate2 at PLA ");
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v,int i, long id ) {

                Intent intent = new Intent(ParkingListActivity.this, FormActivity.class);

                //String info=userAdapter.getItem((int) id).toString();
                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                intent.putExtra("name",name);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "In onResume at PLA ");
        //db = MyHelper.getReadableDatabase();
        //получаем данные из бд в виде курсора
        //userCursor = db.rawQuery("select * from " + MyHelper.TABLE_PLACES, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        userCursor.close();
    }

    public void logoutUser() {
        session.setLogin(false);

        MyHelper.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ParkingListActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class SyncDB extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalJSonResult;
        public SyncDB(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpServiceClass httpServiceClass = new HttpServiceClass(HttpJSonURL);

            try {
                httpServiceClass.ExecutePostRequest();

                if (httpServiceClass.getResponseCode() == 200) {

                    FinalJSonResult = httpServiceClass.getResponse();

                    if (FinalJSonResult != null) {

                        JSONArray jsonArray = null;
                        try {

                            jsonArray = new JSONArray(FinalJSonResult);
                            JSONObject jsonObject;

                            for (int i = 0; i < jsonArray.length(); i++) {

                                jsonObject = jsonArray.getJSONObject(i);

                                String tempName = jsonObject.getString("name");

                                String tempAddress = jsonObject.getString("address");
                                String tempUid =jsonObject.getString("unique_id");
                                String tempCreated = jsonObject.getString("created_at");

                                MyHelper.addPlace(tempName, tempAddress, tempUid, tempCreated);

                                /*String SQLiteDataBaseQueryHolder = "INSERT INTO "+SQLiteHelper.TABLE_NAME+" (subjectName,subjectFullForm) VALUES('"+tempSubjectName+"', '"+tempSubjectFullForm+"');";

                                MyHelper.execSQL(SQLiteDataBaseQueryHolder);*/

                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {

                    Toast.makeText(context, httpServiceClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)

        {
            userCursor = MyHelper.getPlaceDetails();
            // определяем, какие столбцы из курсора будут выводиться в ListView
            String[] headers = new String[]{MyHelper.KEY_NAME, MyHelper.KEY_ADDRESS};
            // создаем адаптер, передаем в него курсор
            userAdapter = new SimpleCursorAdapter(ParkingListActivity.this, android.R.layout.two_line_list_item,
                    userCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);

            header.setText("Найдено элементов: " + String.valueOf(userCursor.getCount()));
            userList.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
        }
    }

}