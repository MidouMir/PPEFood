package comic.systems.ppefood;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
import java.util.Collections;
import java.util.List;

import comic.systems.ppefood.model.FacturesModele;

public class FacturesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<FacturesModele> dataFactures = Collections.emptyList();
    DataSearch current;
    int currentPos = 0;

    public static String PREF_USERNAME  = "user";

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    // create constructor to initialize context and data sent from MainActivity
    public FacturesAdapter(Context context, List<FacturesModele> dataFactures){
        this.context        = context;
        inflater            = LayoutInflater.from(context);
        this.dataFactures   = dataFactures;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.container_factures, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        FacturesModele current = dataFactures.get(position);
        myHolder.itemView.setId(current.ligne);
        myHolder.idC.setText(current.commande);
        myHolder.commande.setText("Commande n°" + current.commande + " / " + current.totalQte + " produits");
        myHolder.dateF.setText("Datant du " + current.dateF);
        /*
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String prixQuantite = formatter.format(Float.parseFloat(current.total) * Float.parseFloat(current.quantite));
        String prixFloat    = formatter.format(Float.parseFloat(current.prix));
        myHolder.textPrix.setText(prixFloat);
        */
        myHolder.tva.setText("Prix HT: " + current.totalHT + "€ / TVA: " + current.totalTVA + "€");
        myHolder.totalTTC.setText(current.totalTTC);
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return dataFactures.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView commande;
        TextView dateF;
        TextView tva;
        Button totalTTC;
        ImageButton diminuer;
        TextView textQuantite;
        TextView idC;
        ImageView laPhoto;

        // create constructor to get widget reference
        public MyHolder(final View itemView) {
            super(itemView);
            commande    = (TextView) itemView.findViewById(R.id.commande);
            dateF       = (TextView) itemView.findViewById(R.id.dateF);
            tva         = (TextView) itemView.findViewById(R.id.tva);
            textQuantite= (TextView) itemView.findViewById(R.id.quantite);
            totalTTC    = (Button) itemView.findViewById(R.id.totalTTC);
            diminuer    = (ImageButton) itemView.findViewById(R.id.diminuer);
            idC         = (TextView) itemView.findViewById(R.id.idC);
            laPhoto     = (ImageView) itemView.findViewById(R.id.photoP);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            final TextView uneCommande  = (TextView) v.findViewById(R.id.idC);
            final String laCommande     = uneCommande.getText().toString();

            Toast.makeText(context, "Commande n°" + laCommande, Toast.LENGTH_SHORT).show();
        }

    }
}