package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

public class Validation extends AppCompatActivity {
    private String idF;

    public static String PREFS_NAME     = "mapref";
    public static String PREF_USERNAME  = "user";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.produit_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        idF  = getIntent().getExtras().getString("idF");
        new ChargementFacture(idF).execute();

        // Toast.makeText(this, "idF = " + idF, Toast.LENGTH_LONG).show();

        /*
        SwipeRefreshLayout mySwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        doYourUpdate();
                    }
                }
        );

    }

    private void doYourUpdate() {
        new ChargementFacture(idF).execute();
        */
    }

    private class ChargementFacture extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Validation.this);
        HttpURLConnection conn;
        URL url = null;
        String idF;

        public ChargementFacture(String idF){
            this.idF    = idF;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tChargement de la facture...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://demo.comic.systems/android/paiement_confirmation");

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
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("idF", idF);
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

        public class DataFacture {
            public int idF;
            public String dateF;
            public String totalQte;
            public String totalHT;
            public String totalTVA;
            public String totalTTC;
        }

        @Override
        protected void onPostExecute(String resultat) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            if(resultat.equals("no rows")) {
                new AlertDialog.Builder(Validation.this)
                    .setMessage("Il n'existe aucune facture pour cette commande. Vérifiez qu'elle soit bien payée !")
                    .setPositiveButton("D'accord", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            }else{
                try {
                    JSONArray jArray = new JSONArray(resultat);
                    // Extract data from json and store into ArrayList as class objects
                    JSONObject json_data = jArray.getJSONObject(0);
                    DataFacture resData = new DataFacture();
                    resData.idF         = json_data.getInt("idF");
                    resData.dateF       = json_data.getString("dateF");
                    resData.totalQte    = json_data.getString("totalQte");
                    resData.totalHT     = json_data.getString("totalHT");
                    resData.totalTVA    = json_data.getString("totalTVA");
                    resData.totalTTC    = json_data.getString("totalTTC");

                    // numéro de commande
                    TextView commande   = (TextView)findViewById(R.id.commande);
                    commande.setText("" + resData.idF);

                    // nombre de produits
                    TextView total      = (TextView)findViewById(R.id.quantite);
                    total.setText(resData.totalQte);

                    // numéro de commande
                    TextView date       = (TextView)findViewById(R.id.date);
                    date.setText(resData.dateF);

                    // prix ht du panier
                    TextView horstaxe   = (TextView)findViewById(R.id.ht);
                    horstaxe.setText(resData.totalHT);

                    // prix tva du panier
                    TextView tva        = (TextView)findViewById(R.id.tva);
                    tva.setText(resData.totalTVA);

                    // prix total TTC du panier
                    TextView ttc        = (TextView)findViewById(R.id.ttc);
                    ttc.setText(resData.totalTTC);

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(Validation.this, "Erreur de validation du paiement", Toast.LENGTH_LONG).show();
                    // Toast.makeText(Validation.this, "Erreur: " + e.toString(), Toast.LENGTH_LONG).show();
                    // Toast.makeText(Validation.this, "Exception -> " + resultat.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
