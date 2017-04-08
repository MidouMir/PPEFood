package comic.systems.ppefood.network;

import java.util.List;

import comic.systems.ppefood.model.PanierModele;
import retrofit2.Call;
import retrofit2.http.GET;

public interface PanierApiInterface {

    @GET("panier")
    Call<List<PanierModele>> getInbox();

}