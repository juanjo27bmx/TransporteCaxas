package unc.edu.pe.transportcax.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import unc.edu.pe.transportcax.model.Ruta;

public interface TransporteApi {

    // Aquí solo pedimos la lista de rutas
    @GET("rutas")
    Call<List<Ruta>> obtenerRutas();
}