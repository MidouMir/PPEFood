package comic.systems.ppefood;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
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
import java.util.List;

public class OneFragment extends Fragment{

    private String cat;
    private RecyclerView mRVFish;
    private AdapterSearch mAdapter;


    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public OneFragment(String cat) {
        // Required empty public constructor
        this.cat = cat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        // TextView tvTitle = (TextView) view.findViewById(R.id.fragmentTitre);
        // tvTitle.setText(cat);

        class AsyncFetch extends AsyncTask<String, String, String> {

            ProgressDialog pdLoading = new ProgressDialog(getContext());
            HttpURLConnection conn;
            URL url = null;
            String libelle;

            public AsyncFetch(String libelle){
                this.libelle=libelle;
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
                    url = new URL("https://demo.comic.systems/android/produitsTab");

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
                    Uri.Builder builder = new Uri.Builder().appendQueryParameter("libelle", libelle);
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
                pdLoading.dismiss();
                if(result.equals("no rows")) {
                    Toast.makeText(getContext(), "Aucun produit n'a été trouvé", Toast.LENGTH_LONG).show();
                }else{
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
                        mRVFish = (RecyclerView) view.findViewById(R.id.lesResultats);
                        mAdapter = new AdapterSearch(getContext(), data);
                        mRVFish.setAdapter(mAdapter);
                        mRVFish.setLayoutManager(new LinearLayoutManager(getContext()));

                    } catch (JSONException e) {
                        // You to understand what actually error is and handle it appropriately
                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        Toast.makeText(getContext(), result.toString(), Toast.LENGTH_LONG).show();
                    }
                }

            }
        }
        new AsyncFetch(cat).execute();
        return view;
    }


}