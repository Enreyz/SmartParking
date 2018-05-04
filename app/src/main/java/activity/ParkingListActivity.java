package activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
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
import java.util.HashMap;

public class ParkingListActivity extends Activity {
    private static final String TAG = ParkingListActivity.class.getSimpleName();
    ListView userList;
    TextView header;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;
    private SessionManager session;
    private SQLiteHandler MyHelper;
    private SQLiteDatabase db;
    ArrayList<HashMap<String, String>> arrayList=new ArrayList<>();
    HashMap<String,String> map;
    SimpleAdapter adapter;
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
                TextView nameview=(TextView)findViewById(R.id.text1);
                TextView addressview=(TextView)findViewById(R.id.text2);
                TextView cnt_plcview=(TextView)findViewById(R.id.text3);
                String name=nameview.getText().toString();
                String address=addressview.getText().toString();
                String cnt_plc=cnt_plcview.getText().toString();
                intent.putExtra("name",name);
                intent.putExtra("address",address);
                intent.putExtra("count_places",cnt_plc);
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

                                String tempUid = jsonObject.getString("unique_id");

                                String tempName = jsonObject.getString("name");
                                String tempAddress =jsonObject.getString("address");
                                String tempCount = jsonObject.getString("count_places");
                                String tempCreated = jsonObject.getString("created_at");

                                MyHelper.addPlace(tempName, tempAddress, tempUid, tempCreated,tempCount);

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
           // Log.d(TAG, String.valueOf(userCursor.getCount()));
           /* userCursor.moveToFirst();
            while(!userCursor.isAfterLast())
            {
                map=new HashMap<>();
                map.put(MyHelper.KEY_NAME,userCursor.getString(userCursor.getColumnIndex(MyHelper.KEY_NAME)));
                map.put(MyHelper.KEY_ADDRESS,userCursor.getString(userCursor.getColumnIndex(MyHelper.KEY_ADDRESS)));
                map.put(MyHelper.KEY_COUNT_PLACE,userCursor.getString(userCursor.getColumnIndex(MyHelper.KEY_COUNT_PLACE)));
                arrayList.add(map);
                userCursor.moveToNext();
            }*/
            // определяем, какие столбцы из курсора будут выводиться в ListView
            String[] headers = new String[]{MyHelper.KEY_NAME, MyHelper.KEY_ADDRESS, MyHelper.KEY_COUNT_PLACE};

            //-----------
            // создаем адаптер, передаем в него курсор
            userAdapter = new SimpleCursorAdapter(ParkingListActivity.this, R.layout.list_item,
                    userCursor, headers, new int[]{R.id.text1, R.id.text2, R.id.text3}, 0);
            header.setText("Найдено элементов: " + String.valueOf(userCursor.getCount()));
            userList.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
            //------------
           /*adapter=new SimpleAdapter(ParkingListActivity.this, arrayList, R.layout.list_item, headers, new int[]{R.id.text1, R.id.text2, R.id.text3});
            header.setText("Найдено элементов: " + String.valueOf(userCursor.getCount()));
            userList.setAdapter(adapter);*/
        }
    }

}