package comic.systems.ppefood;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import comic.systems.ppefood.helper.DividerItemDecoration;
import comic.systems.ppefood.model.PanierModele;

public class Panier extends AppCompatActivity {

    private Context context;
    private RecyclerView mRVFish;
    private PanierAdapter mAdapter;
    private static Button totalPrix;
    private FloatingActionButton fab;
    private Float totalPanier;
    private Button ajouter;
    private Button diminuer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toast.makeText(Panier.this, "Payer ?", Toast.LENGTH_LONG).show();
                Snackbar snackbar = Snackbar
                        .make(fab, "Valider votre commande ?", Snackbar.LENGTH_LONG)
                        .setAction("Payer", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(Panier.this, "Page de paiement", Toast.LENGTH_SHORT).show();
                            }
                        });
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(Panier.this, R.color.colorPrimary));;
                snackbar.show();
            }
        });

        String result = getIntent().getExtras().getString("dataPanier");
        int nbResult = 0;

        List<PanierModele> data = new ArrayList<>();

        try {

            JSONArray jArray = new JSONArray(result);

            totalPanier = Float.valueOf(0);

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                nbResult++;
                JSONObject json_data = jArray.getJSONObject(i);
                PanierModele resData = new PanierModele();
                resData.ligne       = json_data.getInt("ligne");
                resData.commande    = json_data.getString("commande");
                resData.nomProduit  = json_data.getString("nomProduit");
                resData.categorie   = json_data.getString("categorie");
                resData.quantite    = json_data.getString("quantite");
                resData.prix        = json_data.getString("prix");
                resData.urlPhoto    = json_data.getString("urlPhoto");
                data.add(resData);

                // faire le calcul (prix x quantité)
                NumberFormat formatter = NumberFormat.getNumberInstance();
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
                Float prixQuantiteFloat    = Float.parseFloat(resData.prix) * Float.parseFloat(resData.quantite);

                // afficher dans le bouton en question
                totalPanier = totalPanier + prixQuantiteFloat;
            }

            // Setup and Handover data to recyclerview
            mRVFish = (RecyclerView) findViewById(R.id.lesResultats);
            mAdapter = new PanierAdapter(Panier.this, data);
            mRVFish.addItemDecoration(new DividerItemDecoration(getApplicationContext()));
            mRVFish.setAdapter(mAdapter);
            mRVFish.setItemAnimator(new DefaultItemAnimator());
            mRVFish.setLayoutManager(new LinearLayoutManager(Panier.this));

            totalPrix = (Button) findViewById(R.id.totalPrix);

            // formatter le prix total avec les centimes
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            String prixQuantite        = formatter.format(totalPanier);

            totalPrix.setText(String.valueOf(prixQuantite));

            /*
            // Afficher le nombre de produits différents dans le panier
            if( nbResult == 1){
                Toast.makeText(Panier.this, "Un seul résultat", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Panier.this, nbResult + " résultats", Toast.LENGTH_LONG).show();
            }
            */

        } catch (JSONException e) {
            // You to understand what actually error is and handle it appropriately
            Toast.makeText(Panier.this, "JSONException1 -> " + e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Panier.this, "JSONException2 -> " + result.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Panier.this, "JSONException3 -> " + new RuntimeException(e), Toast.LENGTH_LONG).show();
        }
    }

    public static Button getTotalPrix() {
        return totalPrix;
    }

    public static void setTotalPrix(Button totalPrix) {
        Panier.totalPrix = totalPrix;
    }
}