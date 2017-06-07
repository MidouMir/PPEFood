package comic.systems.ppefood;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import comic.systems.ppefood.model.PanierModele;

public class Paiement extends AppCompatActivity {

    private Context context;
    private RecyclerView mRVFish;
    private PanierAdapter mAdapter;
    private static Button card_choix;
    private static Button valider;
    private FloatingActionButton fab;
    private Float totalPanier;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        final String utilisateur = getIntent().getExtras().getString("user");

        new Recapitulatif(utilisateur).execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        card_choix = (Button) findViewById(R.id.card_choix);
        card_choix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent compte = new Intent(Paiement.this, Compte.class);
                compte.putExtra("user", utilisateur);
                startActivityForResult(compte, 0);
                overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            }
        });

        valider = (Button) findViewById(R.id.valider);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // id du compte
                TextView champ1             = (TextView)findViewById(R.id.compte);
                String compte               = champ1.getText().toString();
                // numero de la commande
                TextView champ2             = (TextView)findViewById(R.id.commande);
                String commande             = champ2.getText().toString();
                // méthode de paiement
                TextView champ3             = (TextView)findViewById(R.id.moyenPaiement);
                String typePaiement         = champ3.getText().toString();
                // nombre de produits achetés
                TextView champ4             = (TextView) findViewById(R.id.totalQte);
                String quantite             = champ4.getText().toString();
                // prix total HT
                TextView champ5             = (TextView) findViewById(R.id.totalPrix);
                String lePrix               = champ5.getText().toString();
                // boutique choisie
                TextView champ6             = (TextView) findViewById(R.id.codeMagasin);
                String boutique             = champ6.getText().toString();

                // Toast.makeText(Paiement.this, "Boutique" + boutique, Toast.LENGTH_LONG).show();

                new PaiementEffectuer(compte, commande, typePaiement, lePrix, quantite, boutique).execute();
            }
        });
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity( getIntent() );
    }

    // Create class AsyncFetch
    private class Recapitulatif extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Paiement.this);
        HttpURLConnection conn;
        URL url = null;
        String utilisateur;

        public Recapitulatif(String utilisateur){
            this.utilisateur    = utilisateur;
        }

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
                url = new URL("https://demo.comic.systems/android/paiement_get");

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
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("utilisateur", utilisateur);
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

        public class DataPaiement {
            public int idC;
            public String mailU;
            public int idU;
            public String totalQte;
            public String totalPrix;
            public String totalPrixTTC;
            public String adresseU;
            public String codeMag;
            public String nomMag;
            public String moyenPaiement;
            public String detailPaiement;
        }

        @Override
        protected void onPostExecute(String resultat) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            if(resultat.equals("no rows")) {
                Toast.makeText(Paiement.this, "Impossible de passer au paiement", Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONArray jArray = new JSONArray(resultat);
                    // Extract data from json and store into ArrayList as class objects
                    JSONObject json_data = jArray.getJSONObject(0);
                    DataPaiement resData = new DataPaiement();
                    resData.idC = json_data.getInt("idC");
                    resData.mailU = json_data.getString("mailU");
                    resData.idU = json_data.getInt("idU");
                    resData.totalQte = json_data.getString("totalQte");
                    resData.totalPrix = json_data.getString("totalPrix");
                    resData.totalPrixTTC = json_data.getString("totalPrixTTC");
                    resData.adresseU = json_data.getString("adresseU");
                    resData.codeMag = json_data.getString("codeMag");
                    resData.nomMag = json_data.getString("nomMag");
                    resData.moyenPaiement = json_data.getString("moyenPaiement");
                    resData.detailPaiement = json_data.getString("detailPaiement");

                    // numéro de compte
                    TextView compte     = (TextView)findViewById(R.id.compte);
                    compte.setText(String.valueOf(resData.idU));

                    // numéro de commande
                    TextView commande   = (TextView)findViewById(R.id.commande);
                    commande.setText(String.valueOf(resData.idC));

                    // numéro de commande
                    TextView numMagasin   = (TextView)findViewById(R.id.codeMagasin);
                    numMagasin.setText(String.valueOf(resData.codeMag));

                    // numéro de commande
                    TextView leMagasin   = (TextView)findViewById(R.id.magasin);
                    leMagasin.setText(String.valueOf(resData.nomMag));

                    // faire les calculs
                    TextView paiement   = (TextView)findViewById(R.id.moyenPaiement);
                    paiement.setText(String.valueOf(resData.moyenPaiement));

                    compte.setVisibility(View.GONE);
                    commande.setVisibility(View.GONE);
                    paiement.setVisibility(View.GONE);

                    // faire les calculs
                    Float floatPrix = Float.valueOf(resData.totalPrix);
                    Float floatTTC  = Float.valueOf(resData.totalPrixTTC);

                    NumberFormat formatter = NumberFormat.getNumberInstance();
                    formatter.setMinimumFractionDigits(2);
                    formatter.setMaximumFractionDigits(2);

                    String prixQuantite        = formatter.format(floatPrix);
                    String prixQuantiteTTC     = formatter.format(floatTTC);

                    // quantité totale de produits
                    TextView champTotalQte = (TextView)findViewById(R.id.totalQte);
                    champTotalQte.setText(resData.totalQte);

                    // adresse de livraison
                    TextView champAdresse = (TextView)findViewById(R.id.adresseLivraison);
                    champAdresse.setText(resData.adresseU);
                    // champAdresse.setId(resData.idU);

                    // prix total du panier
                    TextView champTotalPrix = (TextView)findViewById(R.id.totalPrix);
                    champTotalPrix.setText(prixQuantite);
                    // prix total TTC du panier
                    Button paiementValid = (Button)findViewById(R.id.totalPrixTTC);
                    paiementValid.setText(prixQuantiteTTC);

                    ImageButton lePaiement  = (ImageButton)findViewById(R.id.card_laMethode);
                    if((resData.moyenPaiement).equals("VISA")) {
                        lePaiement.setImageResource(R.drawable.paiement_visa);
                    }else if((resData.moyenPaiement).equals("MasterCard")) {
                        lePaiement.setImageResource(R.drawable.paiement_mastercard);
                    }else if((resData.moyenPaiement).equals("Maestro")) {
                        lePaiement.setImageResource(R.drawable.paiement_maestro);
                    }else if((resData.moyenPaiement).equals("PayPal")) {
                        lePaiement.setImageResource(R.drawable.paiement_paypal);
                    }

                    if(resData.nomMag.contains("Aucun")) {
                        Button validation = (Button) findViewById(R.id.valider);
                        validation.setEnabled(false);
                        validation.setText("Stock insuffisant");
                        validation.setTextColor(Color.parseColor("#d70202"));
                    }else{
                        Button validation = (Button) findViewById(R.id.valider);
                        validation.setEnabled(true);
                        validation.setText("Payer la commande");
                        validation.setTextColor(getApplication().getResources().getColor(R.color.colorPrimary));
                    }

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    // Toast.makeText(Paiement.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(Paiement.this, resultat.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private class PaiementEffectuer extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Paiement.this);
        HttpURLConnection conn;
        URL url = null;
        String compte;
        String commande;
        String typePaiement;
        String lePrix;
        String quantite;
        String boutique;

        public PaiementEffectuer(String compte, String commande, String typePaiement, String lePrix, String quantite, String boutique){
            this.compte         = compte;
            this.commande       = commande;
            this.typePaiement   = typePaiement;
            this.lePrix         = lePrix;
            this.quantite       = quantite;
            this.boutique       = boutique;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tPaiement en cours...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("https://demo.comic.systems/android/paiement_effectuer");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive  from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // add parameter to our above url
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("idU", compte)
                        .appendQueryParameter("idC", commande)
                        .appendQueryParameter("moyenPaiement", typePaiement)
                        .appendQueryParameter("totalPrix", lePrix)
                        .appendQueryParameter("boutique", boutique);
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
            pdLoading.dismiss();
            if(result.contains("Erreur")) {
                Toast.makeText(Paiement.this, "Problème lors du paiement...\n"+result, Toast.LENGTH_LONG).show();
            }else{
                Intent val = new Intent(Paiement.this, Validation.class);
                val.putExtra("idF", result);
                Paiement.this.startActivity(val);
                finish();
            }
        }
    }
}