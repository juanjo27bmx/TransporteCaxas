package unc.edu.pe.transportcax.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://transportcaxserver.onrender.com/";

    private static Retrofit retrofit = null;

    public static TransporteApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())//convertidor de JSON a objetos
                    .build();
        }
        return retrofit.create(TransporteApi.class);
    }
}