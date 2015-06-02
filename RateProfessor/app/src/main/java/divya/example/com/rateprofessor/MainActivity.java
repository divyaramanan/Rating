package divya.example.com.rateprofessor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    ListView professorList;
    ArrayList<String> professorArray = new ArrayList<String>();
    ArrayAdapter<String> professorArrayAdapter;
    Context currentContext;
    String getProfessorList = "http://bismarck.sdsu.edu/rateme/list";
    DatabaseAdapter databaseHelp;
    int professorId = 0;
    List<String> firstNames = new ArrayList<String>();
    List<String> lastNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        professorList = (ListView)findViewById(R.id.professorlist);
        professorArrayAdapter = new ArrayAdapter<String>(this,R.layout.professor_list_layout,professorArray);
        professorList.setAdapter(professorArrayAdapter);
        currentContext = this;
        databaseHelp = new DatabaseAdapter(this);
        if(isOnline() == true) {
            RateTask taskExecutor = new RateTask();
            taskExecutor.execute();
            DBGetList dbtask = new  DBGetList();
            dbtask.execute(firstNames,lastNames);
        }
        else{
            fetchListFromDB();
        }

        professorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {



            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 parent.getItemAtPosition(position);
                professorId = position+1;

                Intent intent = new Intent(currentContext,Information.class);
                intent.putExtra("professorId",position+1);
                startActivity(intent);
            }
        });
    }
//Check if User is ONLINE/OFFLINE
    public boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(currentContext.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//Fetch Instructor names from DATABASE if the user goes offline
    public void fetchListFromDB()
    {
        Cursor cursor = databaseHelp.selectList();
        while (cursor.moveToNext()) {

            String firstName = cursor.getString(0);
            String lastName = cursor.getString(1);
            String name = firstName+" "+lastName;
            professorArray.add(name);
        }
    }
// Fetch the Instructor Names from the URL
    public class RateTask extends AsyncTask<Void,Void,Void>{
        ProgressDialog loadingProgress;

        @Override
        protected Void doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(getProfessorList);
            try {
              HttpResponse response =  client.execute(getRequest);
                StatusLine responseStatus = response.getStatusLine();
                int statusCode = responseStatus.getStatusCode();
                   if(statusCode!=200){
                       return null;
                    }
                InputStream jsonStream = response.getEntity().getContent();
                BufferedReader reader =  new BufferedReader(new InputStreamReader(jsonStream));
                StringBuilder jsonHolder = new StringBuilder();
                String line;
                while((line = reader.readLine())!=null)
                {
                    jsonHolder.append(line);
                }
                String jsonProfessorList = jsonHolder.toString();
                 JSONArray jsonProfessorArray = new JSONArray(jsonProfessorList);
                for(int i=0;i<jsonProfessorArray.length();i++) {
                    JSONObject eachElementOfJsonArray = jsonProfessorArray.getJSONObject(i);
                    String firstName = eachElementOfJsonArray.getString("firstName");
                    String lastName = eachElementOfJsonArray.getString("lastName");
                    String name = firstName+" "+lastName;
                    professorArray.add(name);
                    firstNames.add(eachElementOfJsonArray.getString("firstName"));
                    lastNames.add(eachElementOfJsonArray.getString("lastName"));
                }



                } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress = new ProgressDialog(currentContext);
            loadingProgress.setTitle("Loading Professor List");
            loadingProgress.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingProgress.dismiss();
            professorArrayAdapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }
    }
// Insert Instructor List in Database, incase the user goes offline
    public class DBGetList extends AsyncTask<List<String>, Void, Void> {
        ProgressDialog loadingProgress;
        long queryResult=0;


        @Override
        protected Void doInBackground(List<String>... params) {

            databaseHelp.deleteEntireList();
        queryResult = databaseHelp.insertList(professorId, params[0], params[1]);

            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress = new ProgressDialog(currentContext);
            loadingProgress.setTitle("Loading Professor List");
            loadingProgress.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingProgress.dismiss();
            super.onPostExecute(aVoid);
        }
    }

}
