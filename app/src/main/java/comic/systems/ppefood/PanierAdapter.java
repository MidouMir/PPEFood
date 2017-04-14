package comic.systems.ppefood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.vision.text.Text;

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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import comic.systems.ppefood.model.PanierModele;
import android.content.SharedPreferences;

public class PanierAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<PanierModele> dataPanier = Collections.emptyList();
    DataSearch current;
    int currentPos = 0;

    public static String PREF_USERNAME  = "user";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    // create constructor to initialize context and data sent from MainActivity
    public PanierAdapter(Context context, List<PanierModele> dataPanier){
        this.context    = context;
        inflater        = LayoutInflater.from(context);
        this.dataPanier = dataPanier;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.container_panier, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        PanierModele current = dataPanier.get(position);
        myHolder.itemView.setId(current.ligne);
        myHolder.idC.setText(current.commande);
        myHolder.textTitre.setText(current.nomProduit);
        myHolder.textCat.setText("Catégorie: " + current.categorie);
        //myHolder.textPrix.setText("Prix unitaire: " + current.prix + "€");
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String prixQuantite = formatter.format(Float.parseFloat(current.prix) * Float.parseFloat(current.quantite));
        String prixFloat    = formatter.format(Float.parseFloat(current.prix));
        myHolder.textPrix.setText(prixFloat);
        // myHolder.textPrix.setText(current.quantite+" x " + current.prix + "€ = " + prixQuantite + "€" );
                // Double.parseDouble(String.valueOf(Integer.parseInt(current.quantite) * Float.parseFloat(current.prix))) );
        myHolder.textQuantite.setText(current.quantite);
        // myHolder.textPrix.setText(current.prix);
        // myHolder.ajouter.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        //myHolder.ajouter.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // load image into imageview using glide
        Glide.with(context).load("https://demo.comic.systems" + current.urlPhoto)
                .placeholder(R.drawable.no_img)
                .error(R.drawable.no_img)
                .into(myHolder.laPhoto);
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return dataPanier.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textTitre;
        TextView textCat;
        TextView textPrix;
        ImageButton ajouter;
        ImageButton diminuer;
        TextView textQuantite;
        TextView idC;
        ImageView laPhoto;

        // create constructor to get widget reference
        public MyHolder(final View itemView) {
            super(itemView);
            textTitre   = (TextView) itemView.findViewById(R.id.textTitre);
            textCat     = (TextView) itemView.findViewById(R.id.textCat);
            textPrix    = (TextView) itemView.findViewById(R.id.textPrix);
            textQuantite= (TextView) itemView.findViewById(R.id.quantite);
            ajouter     = (ImageButton) itemView.findViewById(R.id.ajouter);
            diminuer    = (ImageButton) itemView.findViewById(R.id.diminuer);
            idC         = (TextView) itemView.findViewById(R.id.idC);
            laPhoto     = (ImageView) itemView.findViewById(R.id.photoP);
            itemView.setOnClickListener(this);

            ImageButton ajouter = (ImageButton) itemView.findViewById(R.id.ajouter);
            ajouter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Button totalPanier      = (Button) view.findViewById(R.id.totalPrix);
                    //Float prixPanier        = Float.parseFloat(totalPanier.getText().toString());
                    //Float prixProduit       = Float.parseFloat(String.valueOf(textPrix.getText().toString()));
                    //NumberFormat formatter  = NumberFormat.getNumberInstance();
                    //formatter.setMinimumFractionDigits(2);
                    //formatter.setMaximumFractionDigits(2);
                    //String lePrix = formatter.format(prixPanier + prixProduit);
                    // incrémenter le prix du panier
                    //totalPanier.setText(lePrix);

                    // Champs à envoyer
                    final String laQuantite = textQuantite.getText().toString();
                    final String leProduit  = textTitre.getText().toString();
                    String laCommande       = idC.getText().toString();

                    // Calculer la nouvelle quantité
                    String nouvelleQuantite = String.valueOf(Integer.parseInt(laQuantite) + 1);

                    // Lancer la requête pour mettre à jour la quantité
                    new QuantiteUpdate(laQuantite, leProduit, laCommande, "ajouter").execute();
                    textQuantite.setText( nouvelleQuantite );
                    // Toast.makeText(context, "+1 pour " + laCommande + "\nTotal: " /*+ lePrix*/, Toast.LENGTH_LONG).show();
                }
            });
            ImageButton diminuer = (ImageButton) itemView.findViewById(R.id.diminuer);
            diminuer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Champs à envoyer
                    final String laQuantite = textQuantite.getText().toString();
                    final String leProduit  = textTitre.getText().toString();
                    String laCommande       = idC.getText().toString();

                    // Calculer la nouvelle quantité
                    String nouvelleQuantite = String.valueOf(Integer.parseInt(laQuantite) - 1);

                    // Lancer la requête pour mettre à jour la quantité
                    new QuantiteUpdate(laQuantite, leProduit, laCommande, "diminuer").execute();

                    // Si la nouvelle quantité est 0, alors supprimer la ligne
                    if(nouvelleQuantite.equals("0")){
                        // supprimer l'affichage de la ligne
                        enlever(getPosition());
                    }else{
                        // modifier l'affichage du chiffre
                        textQuantite.setText( nouvelleQuantite );
                    }
                    // Toast.makeText(context, "-1", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            final TextView txtQ = (TextView) v.findViewById(R.id.quantite);
            final String laQuantite = textQuantite.getText().toString();

            TextView txtP = (TextView) v.findViewById(R.id.textTitre);
            String leProduit = txtP.getText().toString();

            // Toast.makeText(context, "Il y a " + laQuantite + " " +
            // leProduit + " dans votre panier", Toast.LENGTH_LONG).show();

            /*Intent leProd = new Intent(context, UnProduit.class);
            leProd.putExtra("numProd", "produit-"+v.getId());
            context.startActivity(leProd);
            */
        }

    }

    public void enlever(int position) {
        dataPanier.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataPanier.size());
    }

    // Create class AsyncFetch
    private class QuantiteUpdate extends AsyncTask<String, String, String> {

        // ProgressDialog pdLoading = new ProgressDialog(context);
        HttpURLConnection conn;
        URL url = null;
        String quantite;
        String nomProduit;
        String commande;
        String action;

        public QuantiteUpdate(String quantite, String nomProduit, String commande, String action){
            this.quantite   = quantite;
            this.nomProduit = nomProduit;
            this.commande   = commande;
            this.action     = action;
        }

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
                url = new URL("https://demo.comic.systems/android/panier_quantite_update");

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
                        .appendQueryParameter("quantite", String.valueOf(quantite))
                        .appendQueryParameter("nomProduit", String.valueOf(nomProduit))
                        .appendQueryParameter("commande", String.valueOf(commande))
                        .appendQueryParameter("action", String.valueOf(action));
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
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            if(result.contains("Erreur")) {
                Toast.makeText(context, "Problème de mise à jour.\n"+result, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, result  , Toast.LENGTH_LONG).show();
            }

        }

    }
}