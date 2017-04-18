package comic.systems.ppefood;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import android.content.SharedPreferences;

public class AdapterSearch extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<DataSearch> data= Collections.emptyList();
    DataSearch current;
    int currentPos=0;

    // create constructor to initialize context and data sent from MainActivity
    public AdapterSearch(Context context, List<DataSearch> data){
        this.context    = context;
        inflater        = LayoutInflater.from(context);
        this.data       = data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.container_search, parent,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        DataSearch current = data.get(position);
        myHolder.itemView.setId(current.idP);
        myHolder.textTitre.setText(current.nomP);
        myHolder.textCat.setText("Catégorie: " + current.categorie);
        myHolder.textQte.setText("Quantité: " + current.quantite);
        myHolder.textPrix.setText(current.prixP);
        myHolder.textPrix.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        myHolder.textPrix.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        // load image into imageview using glide
        Glide.with(context).load("https://demo.comic.systems" + current.photoP)
                .placeholder(R.drawable.no_img)
                .error(R.drawable.no_img)
                .into(myHolder.laPhoto);
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textTitre;
        TextView textCat;
        TextView textQte;
        TextView textPrix;
        TextView idP;
        ImageView laPhoto;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textTitre   = (TextView) itemView.findViewById(R.id.textTitre);
            textCat     = (TextView) itemView.findViewById(R.id.textCat);
            textQte     = (TextView) itemView.findViewById(R.id.textQte);
            textPrix    = (TextView) itemView.findViewById(R.id.textPrix);
            idP         = (TextView) itemView.findViewById(R.id.idP);
            laPhoto     = (ImageView) itemView.findViewById(R.id.photoP);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v) {
            Toast.makeText(context, "Tu as cliqué sur le produit #" + v.getId(), Toast.LENGTH_SHORT).show();
            Intent leProd = new Intent(context, UnProduit.class);
            leProd.putExtra("numProd", "produit-"+v.getId());
            context.startActivity(leProd);
        }

    }

}