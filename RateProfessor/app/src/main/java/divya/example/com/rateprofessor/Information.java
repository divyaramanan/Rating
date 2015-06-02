package divya.example.com.rateprofessor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class Information extends ActionBarActivity {

    int professorId;
    String getProfessorInfo = "http://bismarck.sdsu.edu/rateme/instructor/";
    String getProfessorComments = "http://bismarck.sdsu.edu/rateme/comments/";
    Context currentContext;
    HttpClient httpclient;
    TextView fullNameView, officeView, phoneView, emailView, ratingView, commentView;
    EditText commentField;
    PostThread postTask;
    RequestQueue queue;
    DatabaseAdapter databaseHelp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        Bundle bundleHoldingId = getIntent().getExtras();
        professorId = bundleHoldingId.getInt("professorId");
        currentContext = this;
        queue = Volley.newRequestQueue(this);
        getProfessorInfo = getProfessorInfo + professorId;
        getProfessorComments = getProfessorComments + professorId;
        databaseHelp = new DatabaseAdapter(this);

        postTask = new PostThread();
        postTask.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_information, menu);
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

    public void onResume() {
        super.onResume();
        String userAgent = null;
        httpclient = AndroidHttpClient.newInstance(userAgent);


        boolean online = isOnline();

        if (online == true) {
            getComments();
            getInstructorDetails("Fetching Instructor Details");
        } else {
            fetchDetailsFromDatabase();
            fetchCommentsFromDatabase();
        }


    }
    public void onPause() {
        super.onPause();
        httpclient.getConnectionManager().shutdown();
    }



//Get Comments from URL AND CACHE IT
    public void getComments() {

        String url = getProfessorComments;
        Cache.Entry cachedData = queue.getCache().get(url);
        if (cachedData != null) {
            try {
                JSONArray commentsFromCache = new JSONArray(new String(cachedData.data, "UTF8"));

                displayProfessorComments(commentsFromCache);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
                public void onResponse(JSONArray response) {

                    displayProfessorComments(response);
                }
            };
            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.i("div", error.toString());
                }
            };

            JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
            queue.add(getRequest);

        }

    }
//Display Fetch comments in the Screen
    public void displayProfessorComments(JSONArray commentsJSONArray) {
        long queryResult = 0;
        List<String> commentDates = new ArrayList<String>();
        List<String> comments = new ArrayList<String>();


        try {

            for (int i = 0; i < commentsJSONArray.length(); i++) {
                JSONObject eachElementOfJsonArray = commentsJSONArray.getJSONObject(i);
                String commentDate = "Date:" + eachElementOfJsonArray.getString("date") + "\n";
                String comment = "Comment:" + eachElementOfJsonArray.getString("text") + "\n";
                String displayComment = commentDate + comment;
                commentView = (TextView) findViewById(R.id.textView16);
                commentView.append(displayComment + "\n");
                commentDates.add(eachElementOfJsonArray.getString("date"));
                comments.add(eachElementOfJsonArray.getString("text"));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        DBCommentTask task = new DBCommentTask();
        task.execute(commentDates,comments);
    }

    //Get the Instructor Details from URL AND CACHE IT
    public void getInstructorDetails(String progressbarText) {


        final ProgressDialog loadingProgressDetails = new ProgressDialog(currentContext);
        String url = getProfessorInfo;
        Cache.Entry cachedData = queue.getCache().get(url);

        if (cachedData != null) {

            try {
                JSONObject cachedProfessorDetails = new JSONObject(new String(cachedData.data, "UTF8"));
                displayProfessorDetails(cachedProfessorDetails);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {

                    loadingProgressDetails.dismiss();
                    displayProfessorDetails(response);
                }
            };
            Response.ErrorListener failure = new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    Log.i("div", error.toString());
                }
            };


            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null, success, failure);
            queue.add(getRequest);
            loadingProgressDetails.setTitle(progressbarText);
            loadingProgressDetails.show();


        }

    }
// DISPLAY PROFESSOR DETAILS ON SCREEN
    public void displayProfessorDetails(JSONObject professorDetailsJSONObject) {
        long queryResult = 0;
        try {


            String firstName = professorDetailsJSONObject.getString("firstName");
            String lastName = professorDetailsJSONObject.getString("lastName");
            String fullName = firstName + " " + lastName;
            fullNameView = (TextView) findViewById(R.id.textView2);
            fullNameView.setText(fullName);

            String office = professorDetailsJSONObject.getString("office");
            officeView = (TextView) findViewById(R.id.textView6);
            officeView.setText(office);

            String phone = professorDetailsJSONObject.getString("phone");
            phoneView = (TextView) findViewById(R.id.textView8);
            phoneView.setText(phone);

            String email = professorDetailsJSONObject.getString("email");
            emailView = (TextView) findViewById(R.id.textView10);
            emailView.setText(email);

            JSONObject rating = professorDetailsJSONObject.getJSONObject("rating");
            String averageRating = rating.getString("average");
            String totalRating = rating.getString("totalRatings");
            ratingView = (TextView) findViewById(R.id.textView12);
            ratingView.setText("Average:" + averageRating + " " + "Total:" + totalRating);
            Cursor cursor = databaseHelp.selectData(email);


            if (cursor.getCount() == 0) {

                queryResult = databaseHelp.insertData(professorId, firstName, lastName, office, phone, email, averageRating, totalRating);
            } else {

                queryResult = databaseHelp.updateData(email, Double.parseDouble(averageRating), Integer.parseInt(totalRating));
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//POSTING RATING AND COMMENTS
    class PostThread extends Thread {
        Handler handle;

        public PostThread() {

        }

        public void run() {
            Looper.prepare();
            handle = new Handler() {
                public void handleMessage(Message input) {


                    if (input.what == 0) {
                        HttpClient httpclient = new DefaultHttpClient();
                        RatingBar rating = (RatingBar) findViewById(R.id.ratingBar);
                        String ratingUrl = "http://bismarck.sdsu.edu/rateme/rating/" + professorId + "/" + rating.getRating();
                        HttpPost postMethod = new HttpPost(ratingUrl);

                        try {
                            HttpResponse responseBody = httpclient.execute(postMethod);


                        } catch (Throwable t) {
                            Log.i("div", t.toString());
                        }
                    }
                    if (input.what == 1) {
                        String commentUrl = "http://bismarck.sdsu.edu/rateme/comment/" + professorId;
                        commentField = (EditText) findViewById(R.id.editText);
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost postMethod = new HttpPost(commentUrl);
                        StringEntity comment;
                        try {
                            comment = new StringEntity(commentField.getText().toString(), HTTP.UTF_8);
                        } catch (UnsupportedEncodingException e) {
                            Log.i("div", e.toString());
                            return;
                        }
                        postMethod.setHeader("Content-Type", "application/json;charset=UTF-8");
                        postMethod.setEntity(comment);
                        try {
                            HttpResponse responseBody = httpclient.execute(postMethod);
                        } catch (Throwable t) {
                            Log.i("div", t.toString());
                        }
                        httpclient.getConnectionManager().shutdown();
                    }


                }
            };
            Looper.loop();

        }
    }
//WHEN USER CLICKS ON THE POST BUTTON
    public void postUserInputs(View button) {
        final ProgressDialog loadingProgressOfPost = new ProgressDialog(currentContext);
        if(isOnline() == false)
        {

            AlertDialog.Builder cdialog = new AlertDialog.Builder(currentContext);
            cdialog.setMessage("No Internet Connection to Complete the Action. Please try again");
            cdialog.setCancelable(true);
            cdialog.setNeutralButton("OK",null);
            cdialog.create().show();

        }
        else {
            postTask.handle.post(new Runnable() {
                @Override
                public void run() {

                    try {
                        loadingProgressOfPost.setTitle("Posting Your Inputs");
                        loadingProgressOfPost.show();
                        postTask.handle.sendEmptyMessage(0);
                        postTask.handle.sendEmptyMessage(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
            try {
                deleteCache(currentContext);
                getInstructorDetails("Posting Your Inputs");
                getComments();
                loadingProgressOfPost.dismiss();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
  //CLAERING USER INPUTS
    public void clearInputs(View button) {
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(0F);
        commentField = (EditText) findViewById(R.id.editText);
        commentField.setText("");
    }

//CHECK IF USER IS ONLINE OR OFFLINE
    public boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(currentContext.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

//FETCHING DETAILS FROM DATABASE IF USER IS OFFLINE
    public void fetchDetailsFromDatabase() {

        String fullName = "", office = "", phone = "", email = "";
        Double averageRating = 0.0;
        int totalRating = 0;
        Cursor cursor = databaseHelp.selectAllData(professorId);
        if (cursor.moveToNext()) {
            fullName = cursor.getString(0) + " " + cursor.getString(1);
            office = cursor.getString(2);
            phone = cursor.getString(3);
            email = cursor.getString(4);
            averageRating = cursor.getDouble(5);
            totalRating = cursor.getInt(6);

        }
        fullNameView = (TextView) findViewById(R.id.textView2);
        fullNameView.setText(fullName);


        officeView = (TextView) findViewById(R.id.textView6);
        officeView.setText(office);


        phoneView = (TextView) findViewById(R.id.textView8);
        phoneView.setText(phone);


        emailView = (TextView) findViewById(R.id.textView10);
        emailView.setText(email);


        ratingView = (TextView) findViewById(R.id.textView12);
        ratingView.setText("Average:" + averageRating + " " + "Total:" + totalRating);
    }
    //FETCHING COMMENTS FROM DATABASE IF USER IS OFFLINE
    public void fetchCommentsFromDatabase() {
        String Comments = "", CommentDate = "";
        int  COUNT = 0;

        Cursor cursor = databaseHelp.selectComments(professorId);
        while (cursor.moveToNext()) {
            COUNT++;
            CommentDate = cursor.getString(1);
            Comments = cursor.getString(2);
            String commentDate = "Date:" + CommentDate + "\n";
            String comment = "Comment:" + Comments + "\n";
            String displayComment = commentDate + comment;
            commentView = (TextView) findViewById(R.id.textView16);
            commentView.append(displayComment + "\n");

        }

    }
//REFRESHING CACHE
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

//FETCH COMMENTS FROM DATABASE
    public class DBCommentTask extends AsyncTask<List<String>, Void, Void> {
        ProgressDialog loadingProgress;
       long queryResult=0;

        @Override
        protected Void doInBackground(List<String>... params) {
            databaseHelp.deleteInstructorComments(professorId);
            queryResult = databaseHelp.insertComment(professorId, params[0], params[1]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress = new ProgressDialog(currentContext);
            loadingProgress.setTitle("Loading Professor Comments");
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