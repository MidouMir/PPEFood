package comic.systems.ppefood.network;

import android.support.v7.app.AppCompatActivity;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PanierApi extends AppCompatActivity {
    public static final String BASE_URL = "http://api.androidhive.info/json/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}