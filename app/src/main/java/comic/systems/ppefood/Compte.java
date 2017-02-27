package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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

public class Compte extends AppCompatActivity {
    private String user;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public ImageView menuOverflow;
    private FloatingActionButton logout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compte);

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

        logout = (FloatingActionButton) findViewById(R.id.fabLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                confirmDialog();
            }
        });

        user = getIntent().getExtras().getString("user");
        new AsyncFetch(user).execute();
        TextView titre = (TextView)findViewById(R.id.titreTitre);
        titre.setText(user);
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Compte.this);
        builder
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(Compte.this, "Déconnexion", Toast.LENGTH_LONG).show();
                        SharedPreferences sharedPrefs =getSharedPreferences(MainActivity.PREFS_NAME,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.clear();
                        editor.commit();
                        user="";
                        //show login form
                        Intent intent=new Intent(Compte.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Compte.this);
        HttpURLConnection conn;
        URL url = null;
        String user;

        public AsyncFetch(String user){
            this.user=user;
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
                url = new URL("https://demo.comic.systems/android/monCompte");
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
                Uri.Builder builder = new Uri.Builder().appendQueryParameter("user", user);
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
        protected void onPostExecute(String user) {
            //this method will be running on UI thread
            pdLoading.dismiss();
            if(user.equals("no rows")) {
                Toast.makeText(Compte.this, "Cette fiche n'existe pas", Toast.LENGTH_LONG).show();
            }else{
                try {
                    JSONArray jArray = new JSONArray(user);
                    // Extract data from json and store into ArrayList as class objects
                    JSONObject json_data = jArray.getJSONObject(0);
                    int idU = json_data.getInt("idU");
                    String nom = json_data.getString("nomU");
                    String prenom = json_data.getString("prenomU");
                    String siret = json_data.getString("siret");
                    String commentaires = json_data.getString("commentaires");
                    String typeCompte = json_data.getString("typeCompte");
                    String adresseFacturation = json_data.getString("adresseFacturation");
                    String cpU = json_data.getString("cpU");
                    String nbCommande = json_data.getString("nbCommande");

                    // Nom
                    TextView compteNom = (TextView)findViewById(R.id.produitTitre);

                    // Adresse
                    TextView compteAdresse = (TextView)findViewById(R.id.produitDesc);

                    // Type de compte
                    TextView compteType = (TextView)findViewById(R.id.produitCat);
                    compteType.setText("Compte " + typeCompte);

                    // Type de compte
                    TextView compteCmd = (TextView)findViewById(R.id.produitCmd);
                    compteCmd.setText("Nombre de commandes: " + nbCommande);

                    if(typeCompte.equals("Entreprise")){
                        compteNom.setText(nom);
                        compteAdresse.setText("Adresse de facturation:\n"+adresseFacturation+"\n\n"+commentaires+"\n\nNuméro de SIRET: "+siret);
                    }else {
                        compteNom.setText(prenom + " " + nom);
                        compteAdresse.setText("Code postal défini: " + cpU);
                    }

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(Compte.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(Compte.this, user.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }

        private void showPopupMenu(View view) {
            // inflate menu
            PopupMenu popup = new PopupMenu(Compte.this, view);
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
                        Toast.makeText(Compte.this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_more:
                        Toast.makeText(Compte.this, "Autres produits de la même catégorie" , Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }

    }
}
