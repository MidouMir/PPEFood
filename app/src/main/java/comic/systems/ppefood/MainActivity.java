package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static String PREFS_NAME     = "mapref";
    public static String PREF_USERNAME  = "user";
    public static String PREF_PASSWORD  = "pass";
    public static String PREF_ACCOUNT   = "type";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    private EditText etUser;
    private EditText etPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Reference to variables
        etUser  = (EditText) findViewById(R.id.mail);
        etPass  = (EditText) findViewById(R.id.pass);

    }

    public void onStart(){
        super.onStart();
        //read username and password from SharedPreferences
        getUser();
    }

    // Triggers when LOGIN Button clicked
    public void checkLogin(View arg0) {

        // Get text from user and pass field
        final String user = etUser.getText().toString();
        final String pass = etPass.getText().toString();

        if (!isChampOk(user)) {
            //Set error message for email field
            etUser.setError("Minimum 4 caractères");
        }

        if (!isChampOk(pass)) {
            //Set error message for password field
            etPass.setError("Minimum 4 caractères");
        }

        if(isChampOk(user) && isChampOk(pass))
        {
            new AsyncLogin().execute(user,pass);
        }

    }

    private boolean isChampOk(String champ) {
        if (champ != null && champ.length() >= 4) {
            return true;
        }
        return false;
    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tChargement...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://demo.comic.systems/android/connexion");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("user", params[0])
                        .appendQueryParameter("pass", params[1]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (result.equalsIgnoreCase("false")){

                // If username and pass does not match display a error message
                Toast.makeText(MainActivity.this, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(MainActivity.this, "Oops! Il y a un problème de connexion...", Toast.LENGTH_LONG).show();

            }else{
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */

                Toast.makeText(MainActivity.this, "Bienvenue !", Toast.LENGTH_LONG).show();
                rememberMe(etUser.getText().toString(),etPass.getText().toString(), result);
                getUser();
                /*
                Intent myIntent = new Intent(MainActivity.this, Accueil.class);
                MainActivity.this.startActivity(myIntent);
                */

            }
        }

    }

    public void getUser(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String prefUser = pref.getString(PREF_USERNAME, null);
        String prefPass = pref.getString(PREF_PASSWORD, null);
        String prefType = pref.getString(PREF_ACCOUNT, null);

        if (prefUser != null || prefPass != null) {
            //directly show logout form
            showLogout(prefUser, prefType);
        }
    }

    public void rememberMe(String user, String pass, String type){
        //save username and password in SharedPreferences
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_USERNAME,user)
                .putString(PREF_PASSWORD,pass)
                .putString(PREF_ACCOUNT,type)
                .commit();
    }

    public void showLogout(String prefUser, String prefType){
        //display log out activity
        Intent intent   = new Intent(this, Accueil.class);
        intent.putExtra("user",prefUser);
        intent.putExtra("typeCompte",prefType);
        startActivity(intent);
        finish();
    }
}
