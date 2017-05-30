package comic.systems.ppefood;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import comic.systems.ppefood.model.FacturesModele;

public class Factures extends AppCompatActivity {

    private Context context;
    private RecyclerView mRVFish;
    private FacturesAdapter mAdapter;

    public static String PREFS_NAME     = "mapref";
    public static String PREF_USERNAME  = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factures);

        Toolbar toolbar = (Toolbar) findViewById(R.id.factures_header);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.push_down_out);
                // overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
            }
        });

        final Drawable upArrow = getResources().getDrawable(R.drawable.retour);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        String result = getIntent().getExtras().getString("dataFactures");
        int nbResult = 0;

        List<FacturesModele> data = new ArrayList<>();

        try {

            JSONArray jArray = new JSONArray(result);

            // Extract data from json and store into ArrayList as class objects
            for (int i = 0; i < jArray.length(); i++) {
                nbResult++;
                JSONObject json_data = jArray.getJSONObject(i);
                FacturesModele resData = new FacturesModele();
                resData.ligne       = json_data.getInt("ligne");
                resData.commande    = json_data.getString("commande");
                resData.utilisateur = json_data.getString("utilisateur");
                resData.dateF       = json_data.getString("dateF");
                resData.totalQte    = json_data.getString("totalQte");
                resData.totalHT     = json_data.getString("totalHT");
                resData.totalTVA    = json_data.getString("totalTVA");
                resData.totalTTC    = json_data.getString("totalTTC");
                data.add(resData);
            }

            // Setup and Handover data to recyclerview
            mRVFish = (RecyclerView) findViewById(R.id.lesResultats);
            mAdapter = new FacturesAdapter(Factures.this, data);
            mRVFish.addItemDecoration(new DividerItemDecoration(getApplicationContext()));
            mRVFish.setAdapter(mAdapter);
            mRVFish.setItemAnimator(new DefaultItemAnimator());
            mRVFish.setLayoutManager(new LinearLayoutManager(Factures.this));

            SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
            final String user = pref.getString(PREF_USERNAME, null);

        } catch (JSONException e) {
            // You to understand what actually error is and handle it appropriately
            Toast.makeText(Factures.this, "JSONException1 -> " + e.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Factures.this, "JSONException2 -> " + result.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(Factures.this, "JSONException3 -> " + new RuntimeException(e), Toast.LENGTH_LONG).show();
        }
    }

    // back to exit
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.push_down_out);
        // overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }
}