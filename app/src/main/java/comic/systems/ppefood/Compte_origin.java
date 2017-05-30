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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import movile.com.creditcardguide.model.IssuerCode;
import movile.com.creditcardguide.view.CreditCardView;

public class Compte_origin extends AppCompatActivity {
    private String user;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public ImageView menuOverflow;
    private Button saveCmpt;
    private Button logout;
    private EditText cptNom;
    private EditText cptPrenom;
    private EditText cptAdresse;
    private EditText cptComm;
    private EditText cptSiret;
    private EditText cptMail;
    private TextView cptType;

    // final CreditCardView creditCardView = (CreditCardView) findViewById(R.id.creditCardView);

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

        /*logout = (FloatingActionButton) findViewById(R.id.fabLogout);*/
        logout = (Button) findViewById(R.id.btnLogout);
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

        /*
        creditCardView.chooseFlag(IssuerCode.VISACREDITO);
        creditCardView.setTextExpDate("12/19");
        creditCardView.setTextNumber("5555 4444 3333 1111");
        creditCardView.setTextOwner("Felipe Silvestre");
        creditCardView.setTextCVV("432");
        */

        // Warning: this is for development purposes only and should never be done outside of this example app.
        // Failure to set FLAG_SECURE exposes your app to screenshots allowing other apps to steal card information.
        // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        // enregistrer les informations
        saveCmpt        = (Button) findViewById(R.id.btnSave);
        // trouver les champs
        cptNom          = (EditText) findViewById(R.id.compteNom);
        cptPrenom       = (EditText) findViewById(R.id.comptePrenom);
        cptAdresse      = (EditText) findViewById(R.id.compteAdresse);
        cptComm         = (EditText) findViewById(R.id.compteDetails);
        cptSiret        = (EditText) findViewById(R.id.compteSiret);
        cptMail         = (EditText) findViewById(R.id.titreTitre);
        cptType         = (TextView) findViewById(R.id.compteType);
        // envoyer les donnéees du formulaire
        saveCmpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                new AsyncUpdate().execute(
                        "id-"+saveCmpt.getId(),
                        cptNom.getText().toString(),
                        cptPrenom.getText().toString(),
                        cptAdresse.getText().toString(),
                        cptComm.getText().toString(),
                        cptSiret.getText().toString(),
                        cptMail.getText().toString(),
                        cptType.getText().toString()
                );
            }
        });
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Compte_origin.this);
        builder
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui",  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(Compte_origin.this, "Déconnexion", Toast.LENGTH_LONG).show();
                        SharedPreferences sharedPrefs =getSharedPreferences(MainActivity.PREFS_NAME,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.clear();
                        editor.commit();
                        user="";
                        //show login form
                        Intent intent=new Intent(Compte_origin.this, MainActivity.class);
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

        ProgressDialog pdLoading = new ProgressDialog(Compte_origin.this);
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
                Toast.makeText(Compte_origin.this, "Cette fiche n'existe pas", Toast.LENGTH_LONG).show();
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
                    String adresse = json_data.getString("adresse");
                    String cpU = json_data.getString("cpU");
                    String nbCommande = json_data.getString("nbCommande");

                    Button compteValid      = (Button)findViewById(R.id.btnSave);
                    compteValid.setId(idU);

                    // Nom
                    TextView compteNom      = (TextView)findViewById(R.id.compteNom);
                    TextView comptePrenom   = (TextView)findViewById(R.id.comptePrenom);

                    // Adresse
                    TextView compteAdresse  = (TextView)findViewById(R.id.compteAdresse);
                    TextView compteDetailsT = (TextView)findViewById(R.id.titreDetails);
                    TextView compteDetails  = (TextView)findViewById(R.id.compteDetails);
                    TextView compteSiretT   = (TextView)findViewById(R.id.titreSiret);
                    TextView compteSiret    = (TextView)findViewById(R.id.compteSiret);

                    compteAdresse.setText(adresse);

                    // Type de compte
                    TextView compteType = (TextView)findViewById(R.id.compteType);
                    compteType.setText("Compte " + typeCompte);

                    // Type de compte
                    TextView compteCmd = (TextView)findViewById(R.id.produitCmd);
                    compteCmd.setText("Nombre de commandes: " + nbCommande);

                    if(typeCompte.equals("Entreprise")){
                        compteNom.setText(nom);
                        comptePrenom.setVisibility(View.GONE);
                        compteDetailsT.setText("Complément");
                        compteDetails.setText(commentaires);
                        compteSiretT.setText("Numéro de SIRET");
                        compteSiret.setText(siret);
                    }else {
                        comptePrenom.setText(prenom);
                        comptePrenom.setWidth(50);
                        compteNom.setText(nom);
                        compteNom.setWidth(50);
                        compteDetailsT.setVisibility(View.GONE);
                        compteDetails.setVisibility(View.GONE);
                        compteSiretT.setVisibility(View.GONE);
                        compteSiret.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
                    Toast.makeText(Compte_origin.this, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(Compte_origin.this, user.toString(), Toast.LENGTH_LONG).show();
                }
            }

        }

        private void showPopupMenu(View view) {
            // inflate menu
            PopupMenu popup = new PopupMenu(Compte_origin.this, view);
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
                        Toast.makeText(Compte_origin.this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_more:
                        Toast.makeText(Compte_origin.this, "Autres produits de la même catégorie" , Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        }

    }

    // Create class AsyncUpdate
    private class AsyncUpdate extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Compte_origin.this);
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
                url = new URL("https://demo.comic.systems/android/majCompte");
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
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", params[0])
                        .appendQueryParameter("nom", params[1])
                        .appendQueryParameter("prenom", params[2])
                        .appendQueryParameter("adresse", params[3])
                        .appendQueryParameter("commentaires", params[4])
                        .appendQueryParameter("siret", params[5])
                        .appendQueryParameter("mail", params[6])
                        .appendQueryParameter("typeCompte", params[7]);
                /*
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("nom", nom)
                        .appendQueryParameter("prenom", prenom)
                        .appendQueryParameter("mail", mail)
                        .appendQueryParameter("adresse", adresse)
                        .appendQueryParameter("commentaires", commentaires)
                        .appendQueryParameter("siret", siret)
                        .appendQueryParameter("typeCompte", typeCompte);*/
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
        protected void onPostExecute(String maj) {
            //this method will be running on UI thread
            pdLoading.dismiss();
            if(maj.contains("erreur")) {
                Toast.makeText(Compte_origin.this, "Impossible de mettre à jour votre compte ("+maj+")", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Compte_origin.this, "Votre compte a bien été mis à jour !", Toast.LENGTH_LONG).show();
            }

        }
    }
}
