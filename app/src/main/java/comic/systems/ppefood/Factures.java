package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import comic.systems.ppefood.helper.DividerItemDecoration;
import comic.systems.ppefood.model.FacturesModele;

public class Factures extends AppCompatActivity {

    private Context context;
    private RecyclerView mRVFish;
    private FacturesAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static String PREFS_NAME     = "mapref";
    public static String PREF_USERNAME  = "user";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factures);

        Toolbar toolbar = (Toolbar) findViewById(R.id.factures_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.push_down_out);
                // overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            }
        });

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        // génerer les factures
        genererFactures();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });
    }

    void genererFactures() {
        List<FacturesModele> data = new ArrayList<>();

        String result   = getIntent().getExtras().getString("dataFactures");
        int nbResult    = 0;

        try {

            JSONArray jArray = new JSONArray(result);

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                nbResult++;
                JSONObject json_data = jArray.getJSONObject(i);
                FacturesModele resData = new FacturesModele();
                resData.ligne       = json_data.getInt("ligne");
                resData.commande    = json_data.getString("commande");
                resData.utilisateur = json_data.getString("utilisateur");
                resData.dateF       = json_data.getString("dateF");
                resData.totalQte    = json_data.getString("totalQte");
                resData.totalHT     = json_data.getString("totalHT");
                resData.totalTVA    = json_data.getString("totalTVA");
                resData.totalTTC    = json_data.getString("totalTTC");
                data.add(resData);
            }

            // Setup and Handover data to recyclerview
            mRVFish = (RecyclerView) findViewById(R.id.lesResultats);
            mAdapter = new FacturesAdapter(Factures.this, data);
            mRVFish.addItemDecoration(new DividerItemDecoration(getApplicationContext()));
            mRVFish.setAdapter(mAdapter);
            mRVFish.setItemAnimator(new DefaultItemAnimator());
            mRVFish.setLayoutManager(new LinearLayoutManager(Factures.this));

            SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
            final String user = pref.getString(PREF_USERNAME, null);

        } catch (JSONException e) {
            // You to understand what actually error is and handle it appropriately
            Toast.makeText(Factures.this, "JSONException1 -> " + e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Factures.this, "JSONException2 -> " + result.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Factures.this, "JSONException3 -> " + new RuntimeException(e), Toast.LENGTH_LONG).show();
        }
    }

    // mettre à jour les factures
    private class majFactures extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Factures.this);
        HttpURLConnection conn;
        URL url = null;
        String utilisateur = getSharedPreferences(PREFS_NAME,MODE_PRIVATE).getString(PREF_USERNAME, null) ;

        /*
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tChargement...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        */

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://demo.comic.systems/android/factures");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // add parameter to our above url
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("utilisateur", String.valueOf(utilisateur));
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.toString();
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
                    return (result.toString());

                } else {
                    return("Connection error");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            // pdLoading.dismiss();
            if(result.equals("no rows")) {
                Toast.makeText(Factures.this, "Il n'y a aucune facture", Toast.LENGTH_LONG).show();
            }else{
                getIntent().removeExtra("dataFactures");
                getIntent().putExtra("dataFactures", result);
            }
        }
    }

    void refreshItems() {
        // Load items
        new majFactures().execute();

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        genererFactures();

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    // back to exit
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.push_down_out);
        // overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }
}