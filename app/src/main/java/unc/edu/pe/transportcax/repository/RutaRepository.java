package unc.edu.pe.transportcax.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.network.RetrofitClient;
import unc.edu.pe.transportcax.network.TransporteApi;

public class RutaRepository {

    // método que se llama desde el ViewModel llama apenas nace
    public LiveData<List<Ruta>> obtenerRutasEnVivo() {
        // 1. Creamos la variable "viva" vacía
        MutableLiveData<List<Ruta>> listaRutasMutable = new MutableLiveData<>();

        // 2. Llamamos cliente Retrofit (usando el método getApi())
        TransporteApi api = RetrofitClient.getApi();

        // 3. Hacemos la petición a la nube (Render)
        api.obtenerRutas().enqueue(new Callback<List<Ruta>>() {
            @Override
            public void onResponse(Call<List<Ruta>> call, Response<List<Ruta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Inyectamos las 22 rutas en la variable viva
                    listaRutasMutable.setValue(response.body());
                    Log.d("API_EXITO", "Se descargaron " + response.body().size() + " rutas de la API");
                } else {
                    Log.e("API_ERROR", "El servidor respondió, pero hubo un error o está vacío");
                }
            }

            @Override
            public void onFailure(Call<List<Ruta>> call, Throwable t) {
                // Si no hay internet o Render está "dormido"
                Log.e("API_FALLA", "Falla de red: " + t.getMessage());
            }
        });

        // 4. Devolvemos la variable.
        // Aunque al principio está vacía, se llenará cuando internet responda (por eso es LiveData)
        return listaRutasMutable;
    }
}