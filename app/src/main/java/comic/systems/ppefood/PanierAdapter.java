package comic.systems.ppefood;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import comic.systems.ppefood.model.PanierModele;

public class PanierAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<PanierModele> dataPanier = Collections.emptyList();
    DataSearch current;
    int currentPos=0;

    // create constructor to initialize context and data sent from MainActivity
    public PanierAdapter(Context context, List<PanierModele> dataPanier){
        this.context    = context;
        inflater        = LayoutInflater.from(context);
        this.dataPanier = dataPanier;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.container_panier, parent,false);
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
        myHolder.textTitre.setText(current.nomProduit);
        myHolder.textCat.setText("Catégorie: " + current.categorie);
        myHolder.textPrix.setText("Prix unitaire: " + current.prix + "€");
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
        TextView textQuantite;
        TextView idP;
        ImageView laPhoto;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textTitre   = (TextView) itemView.findViewById(R.id.textTitre);
            textCat     = (TextView) itemView.findViewById(R.id.textCat);
            textPrix    = (TextView) itemView.findViewById(R.id.textPrix);
            textQuantite= (TextView) itemView.findViewById(R.id.quantite);
            ajouter     = (ImageButton) itemView.findViewById(R.id.ajouter);
            idP         = (TextView) itemView.findViewById(R.id.idP);
            laPhoto     = (ImageView) itemView.findViewById(R.id.photoP);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            Toast.makeText(context, "Tu as cliqué sur le produit #" + v.getId(), Toast.LENGTH_SHORT).show();
            /*Intent leProd = new Intent(context, UnProduit.class);
            leProd.putExtra("numProd", "produit-"+v.getId());
            context.startActivity(leProd);
            */
        }

    }

}