package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

public class UnProduit extends AppCompatActivity {
    private String numProd;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public ImageView menuOverflow;
    private FloatingActionButton fab;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produit);

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

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UnProduit.this, "Ajouté au panier", Toast.LENGTH_LONG).show();
            }
        });

        numProd = getIntent().getExtras().getString("numProd");
        new AsyncFetch(numProd).execute();
    }

    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(UnProduit.this);
        HttpURLConnection conn;
        URL url = null;
        String idP;

        public AsyncFetch(String numProd){
            this.idP=numProd;
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
                url = new URL("https://demo.comic.systems/android/unProduit");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
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
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("idP", numProd);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
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
        protected void onPostExecute(String idP) {

            //this method will be running on UI thread
            pdLoading.dismiss();
            if(idP.equals("no rows")) {
                Toast.makeText(UnProduit.this, "Cette fiche n'existe pas", Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONArray jArray = new JSONArray(idP);
                    // Extract data from json and store into ArrayList as class objects
                    JSONObject json_data = jArray.getJSONObject(0);
                    DataSearch resData = new DataSearch();
                    resData.idP = json_data.getInt("idP");
                    resData.nomP = json_data.getString("nomP");
                    resData.categorie = json_data.getString("categorie");
                    resData.quantite = json_data.getString("quantite");
                    resData.prixP = json_data.getString("prixP");
                    resData.photoP = json_data.getString("photoP");
                    resData.description = json_data.getString("description");

                    // nom du produit
                    TextView leProduit = (TextView)findViewById(R.id.produitTitre);
                    leProduit.setText(resData.nomP);

                    // catégorie du produit
                    TextView leProduitCat = (TextView)findViewById(R.id.produitCat);
                    leProduitCat.setText("Catégorie: " + resData.categorie);

                    // catégorie du produit
                    TextView leProduitPrix = (TextView)findViewById(R.id.produitPrix);
                    leProduitPrix.setText(resData.prixP + "€");

                    // images du produit
                    Glide.with(UnProduit.this).load("https://demo.comic.systems" + resData.photoP)
                            .placeholder(R.drawable.no_img)
                            .error(R.drawable.no_img)
                            .into((ImageView)findViewById(R.id.photoP));

                    // description du produit
                    TextView leProduitDesc = (TextView)findViewById(R.id.produitDesc);
                    leProduitDesc.setText("« " + resData.description + " »");

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(UnProduit.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(UnProduit.this, idP.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }

        private void showPopupMenu(View view) {
            // inflate menu
            PopupMenu popup = new PopupMenu(UnProduit.this, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_produit, popup.getMenu());
            popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
            popup.show();
        }

        class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

            public MyMenuItemClickListener() {
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_ajouter:
                        Toast.makeText(UnProduit.this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_more:
                        Toast.makeText(UnProduit.this, "Autres produits de la même catégorie" , Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }

    }
}
