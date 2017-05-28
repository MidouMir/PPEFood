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
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compte extends AppCompatActivity {
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
    private EditText cptPaiement;
    private ImageView iconePaiement;

    // final CreditCardView creditCardView = (CreditCardView) findViewById(R.id.creditCardView);

    protected void onCreate(final Bundle savedInstanceState) {
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
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        });

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        /*
        logout = (FloatingActionButton) findViewById(R.id.fabLogout);*/
        /*
        logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                confirmDialog();
            }
        });
        */

        user = getIntent().getExtras().getString("user");
        new AsyncFetch(user).execute();
        TextView titre = (TextView)findViewById(R.id.titreTitre);
        titre.setText(user);

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
        cptPaiement     = (EditText) findViewById(R.id.detailPaiement);

        iconePaiement   = (ImageView) findViewById(R.id.iconePaiement);

        RadioGroup rdgp = (RadioGroup)findViewById(R.id.radiogroup_libelle);
        final RadioButton rb1 = (RadioButton)findViewById(R.id.libelle_carte);
        final RadioButton rb2 = (RadioButton)findViewById(R.id.libelle_paypal);

        rdgp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb  = (RadioButton)findViewById(checkedId);
                if( rb.getText().equals("Carte bancaire") ){
                    cptPaiement.setText("");
                    cptPaiement.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                    iconePaiement.setImageResource(R.drawable.carte_credit);
                }else{
                    cptPaiement.setText("");
                    cptPaiement.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    iconePaiement.setImageResource(R.drawable.paypal);
                }
            }
        });

        cptPaiement.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(rb1.isChecked()){
                    if( s.toString().matches("") ){
                        iconePaiement.setImageResource(R.drawable.carte_credit);
                    }else{
                        switchIconePaiement();
                    }
                }else if(rb2.isChecked()) {
                    switchIconePaiement();
                }
            }
        });

        /*
        // vérifier pendant l'écriture
        cptPaiement.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(rb1.isChecked()){
                    if( s.toString().matches("") ){
                        iconePaiement.setImageResource(R.drawable.carte_credit);
                        cptPaiement.setError("Mode de paiement vide");
                        // Toast.makeText(Compte.this, "Contenu -> " + cptPaiement.getText(), Toast.LENGTH_LONG).show();
                    }else{
                        switchIconePaiement();
                        if (!paiementValidCB(cptPaiement.getText().toString(), switchModePaiement())) {
                            if (switchModePaiement().equals("VISA")) {
                                cptPaiement.setError("Votre VISA doit comporter 13 chiffres");
                            } else if (switchModePaiement().equals("MasterCard")) {
                                cptPaiement.setError("Votre MasterCard doit avoir entre 13 et 19 caractères");
                            } else {
                                cptPaiement.setError("Aucune carte ne commence par " + cptPaiement.getText().toString().charAt(0));
                            }
                        }
                    }
                }else if(rb2.isChecked()) {
                    switchIconePaiement();
                    if ( !paiementValidMail( s.toString() ) ){
                        cptPaiement.setError("Vérifiez l'adresse mail");
                    }
                }
            }
        });
        */

        // envoyer les donnéees du formulaire
        saveCmpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)  {
                if(rb1.isChecked()){
                    if(cptPaiement.getText().toString().equals("")){
                        iconePaiement.setImageResource(R.drawable.carte_credit);
                        cptPaiement.setError("Mode de paiement vide");
                    }else{
                        switchIconePaiement();
                        if (!paiementValidCB(cptPaiement.getText().toString(), switchModePaiement())) {
                            if (switchModePaiement().equals("VISA")) {
                                int calcul      = (13 - cptPaiement.getText().toString().length() );
                                String manque   = (calcul > 0) ? "Il manque " : "";
                                String trop     = (calcul > 0) ? "" : " en trop";
                                String numero   = String.valueOf( calcul ).replace("-", "");
                                String chiffre  = ( numero.equals("1") ) ? " chiffre" : " chiffres";
                                cptPaiement.setError(manque + numero + chiffre + trop);
                            } else if (switchModePaiement().equals("MasterCard")) {
                                int calcul1     = (13 - cptPaiement.getText().toString().length() );
                                String manque  = (calcul1 > 0) ? "Il manque entre " : "Entre ";
                                String trop    = (calcul1 > 0) ? "" : " en trop";
                                String numero1  = String.valueOf( calcul1 ).replace("-", "");
                                // String chiffre1 = ( numero1.equals("1") ) ? " chiffre" : " chiffres";
                                int calcul2     = (19 - cptPaiement.getText().toString().length() );
                                String numero2  = String.valueOf( calcul2 ).replace("-", "");
                                // String chiffre2 = ( numero1.equals("1") ) ? " chiffre" : " chiffres";
                                cptPaiement.setError(manque + numero1 + " et " + numero2 + " chiffres" + trop);
                                // cptPaiement.setError("Votre MasterCard doit avoir entre 13 et 19 caractères");
                            } else {
                                cptPaiement.setError("Aucune carte ne commence par " + cptPaiement.getText().toString().charAt(0));
                            }
                        }
                    }
                }else if(rb2.isChecked()) {
                    switchIconePaiement();
                    if (!paiementValidMail(cptPaiement.getText().toString())){
                        cptPaiement.setError("1-> Vérifiez l'adresse mail");
                    }
                }
                if(cptPaiement.getError() == null) {
                    new AsyncUpdate().execute(
                            "id-"+saveCmpt.getId(),
                            cptNom.getText().toString(),
                            cptPrenom.getText().toString(),
                            cptAdresse.getText().toString(),
                            cptComm.getText().toString(),
                            cptSiret.getText().toString(),
                            cptMail.getText().toString(),
                            cptType.getText().toString(),
                            cptPaiement.getText().toString(),
                            String.valueOf(switchModePaiement())
                    );
                    cptPaiement.clearFocus();
                }else{
                    // Toast.makeText(Compte.this, "Il y a une erreur...", Toast.LENGTH_LONG).show();
                    Snackbar snackbar = Snackbar.make(saveCmpt, "Il y a une erreur...", Snackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(Compte.this, R.color.colorPrimary));;
                    snackbar.show();
                }
            }
        });

    }

    public void switchIconePaiement() {
        final RadioButton rbp = (RadioButton)findViewById(R.id.libelle_paypal);
        if(rbp.isChecked()){
            iconePaiement.setImageResource(R.drawable.paypal);
        }else{
            switch ((cptPaiement.getText()).charAt(0)) {
                case '4':
                    iconePaiement.setImageResource(R.drawable.visa);
                    break;
                case '5':
                    iconePaiement.setImageResource(R.drawable.mastercard);
                    break;
                default:
                    iconePaiement.setImageResource(R.drawable.carte_credit);
            }
        }
    }

    public String switchModePaiement() {
        String resultat = "";
        if((cptPaiement.getText()).toString().contains("@")){
            resultat    = "PayPal";
        }else{
            switch ((cptPaiement.getText()).charAt(0)) {
                case '4':
                    resultat = "VISA";
                    break;
                case '5':
                    resultat = "MasterCard";
                    break;
                default:
                    resultat = "vide";
            }
        }
        return resultat;
    }

    // vérifier l'adresse mail
    private boolean paiementValidMail(String adresseMailPayPal) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(adresseMailPayPal);
        return matcher.matches();
    }

    // vérifier la carte
    private boolean paiementValidCB(String carteUtilisee, String leModePaiement) {
        if (carteUtilisee != null && leModePaiement.equals("VISA") && carteUtilisee.length() == 13) {
            return true;
        }else if (carteUtilisee != null && leModePaiement.equals("MasterCard") && carteUtilisee.length() >= 13 && carteUtilisee.length() <= 19) {
            return true;
        }
        return false;
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
                    String adresse = json_data.getString("adresse");
                    // String cpU = json_data.getString("cpU");
                    // String nbCommande = json_data.getString("nbCommande");
                    String nomPaiement = json_data.getString("nomPaiement");
                    String detailPaiement = json_data.getString("detailPaiement");

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

                    RadioButton libCard = (RadioButton) findViewById(R.id.libelle_carte);
                    RadioButton libPyPl = (RadioButton)findViewById(R.id.libelle_paypal);
                    // RadioButton libEspc = (RadioButton)findViewById(R.id.libelle_especes);

                    // Icone Paiement
                    ImageView compteIcone = (ImageView)findViewById(R.id.iconePaiement);
                    if(nomPaiement.equals("VISA")) {
                        compteIcone.setImageResource(R.drawable.visa);
                        libCard.setChecked(true);
                        // libEspc.setChecked(true);
                        libPyPl.setChecked(false);
                    }else if(nomPaiement.equals("MasterCard")) {
                        compteIcone.setImageResource(R.drawable.mastercard);
                        libCard.setChecked(true);
                        // libEspc.setChecked(true);
                        libPyPl.setChecked(false);
                    }else if(nomPaiement.equals("PayPal")) {
                        compteIcone.setImageResource(R.drawable.paypal);
                        libCard.setChecked(false);
                        // libEspc.setChecked(true);
                        libPyPl.setChecked(true);
                    }else{
                        compteIcone.setImageResource(R.drawable.carte_credit);
                        libCard.setChecked(false);
                        // libEspc.setChecked(true);
                        libPyPl.setChecked(true);
                    }

                    // Paiement
                    TextView compteCarte = (TextView)findViewById(R.id.detailPaiement);
                    compteCarte.setText("" + detailPaiement);

                    /*
                    // Type de compte
                    TextView compteCmd = (TextView)findViewById(R.id.produitCmd);
                    compteCmd.setText("Nombre de commandes: " + nbCommande);
                    */

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

    // Create class AsyncUpdate
    private class AsyncUpdate extends AsyncTask<String, String, String> {

        ProgressDialog pdLoading = new ProgressDialog(Compte.this);
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
                        .appendQueryParameter("typeCompte", params[7])
                        .appendQueryParameter("numeroCarte", params[8])
                        .appendQueryParameter("libelle", params[9]);
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
                Toast.makeText(Compte.this, "Impossible de mettre à jour votre compte ("+maj+")", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Compte.this, "Votre compte a bien été mis à jour !", Toast.LENGTH_LONG).show();
            }

        }
    }

    // back to exit
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
}
