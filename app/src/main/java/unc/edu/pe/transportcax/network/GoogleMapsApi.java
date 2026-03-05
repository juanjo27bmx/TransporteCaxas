package unc.edu.pe.transportcax.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import unc.edu.pe.transportcax.model.DirectionsResponse;

public interface GoogleMapsApi {
    // 1. Método para la ruta (vehículos)
    @GET("maps/api/directions/json")
    Call<DirectionsResponse> obtenerRutaPorCalles(
            @Query("origin") String origen,
            @Query("destination") String destino,
            @Query("waypoints") String paraderosIntermedios,
            @Query("key") String apiKey
    );

    // 2. MÉTODO para la caminata (Peatonal)
    @GET("maps/api/directions/json")
    Call<DirectionsResponse> obtenerRutaCaminando(
            @Query("origin") String origen,
            @Query("destination") String destino,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );
}