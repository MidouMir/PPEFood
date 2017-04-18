package comic.systems.ppefood;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import comic.systems.ppefood.helper.DividerItemDecoration;

public class Recherche extends AppCompatActivity {

    private Context context;
    private RecyclerView mRVFish;
    private AdapterSearch mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        String result = getIntent().getExtras().getString("data");
        int nbResult = 0;

        List<DataSearch> data = new ArrayList<>();

        try {

            JSONArray jArray = new JSONArray(result);

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                nbResult++;
                JSONObject json_data = jArray.getJSONObject(i);
                DataSearch resData = new DataSearch();
                resData.idP = json_data.getInt("idP");
                resData.nomP = json_data.getString("nomP");
                resData.categorie = json_data.getString("categorie");
                resData.quantite = json_data.getString("quantite");
                resData.prixP = json_data.getString("prixP");
                resData.photoP = json_data.getString("photoP");
                data.add(resData);
            }

            // Setup and Handover data to recyclerview
            mRVFish = (RecyclerView) findViewById(R.id.lesResultats);
            mRVFish.addItemDecoration(new DividerItemDecoration(getApplicationContext()));
            mAdapter = new AdapterSearch(Recherche.this, data);
            mRVFish.setAdapter(mAdapter);
            mRVFish.setLayoutManager(new LinearLayoutManager(Recherche.this));

            if( nbResult == 1){
                Toast.makeText(Recherche.this, "Un seul résultat", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Recherche.this, nbResult + " résultats", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            // You to understand what actually error is and handle it appropriately
            Toast.makeText(Recherche.this, e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Recherche.this, result.toString(), Toast.LENGTH_LONG).show();
        }
    }
}