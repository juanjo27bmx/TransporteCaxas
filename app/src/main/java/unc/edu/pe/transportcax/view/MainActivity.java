package unc.edu.pe.transportcax.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Importaciones de Firebase y Retrofit para el modo Offline
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

// Importaciones de TU proyecto (basado en tu estructura)
import unc.edu.pe.transportcax.R;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.network.RetrofitClient;
import unc.edu.pe.transportcax.network.TransporteApi;
import unc.edu.pe.transportcax.view.fragments.AlertasFragment;
import unc.edu.pe.transportcax.view.fragments.InicioFragment;
import unc.edu.pe.transportcax.view.fragments.RutasFragment;
import unc.edu.pe.transportcax.view.fragments.UsuarioFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 1. Cargar el Mapa por defecto al iniciar
        if (savedInstanceState == null) {
            cambiarFragmento(new InicioFragment());
        }

        // 2. Configurar el menú inferior
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragmentSeleccionado = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                fragmentSeleccionado = new InicioFragment();
            } else if (id == R.id.nav_rutas) {
                fragmentSeleccionado = new RutasFragment();
            } else if (id == R.id.nav_alertas) {
                fragmentSeleccionado = new AlertasFragment();
            } else if (id == R.id.nav_perfil) {
                fragmentSeleccionado = new UsuarioFragment();
            }

            if (fragmentSeleccionado != null) {
                cambiarFragmento(fragmentSeleccionado);
            }
            return true;
        });

        // 3. ¡MAGIA OFFLINE! Ejecutamos la sincronización de rutas en segundo plano al abrir la app
        sincronizarRutasConFirebase();
    }

    private void cambiarFragmento(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Método que descarga las rutas de tu API y las guarda en Firebase
    private void sincronizarRutasConFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Usamos directamente el método getApi() de tu RetrofitClient
        TransporteApi api = RetrofitClient.getApi();
        Call<List<Ruta>> call = api.obtenerRutas();

        call.enqueue(new Callback<List<Ruta>>() {
            @Override
            public void onResponse(Call<List<Ruta>> call, Response<List<Ruta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ruta> rutasDesdeApi = response.body();

                    // Recorremos las rutas de la API y las guardamos en Firebase
                    for (Ruta ruta : rutasDesdeApi) {

                        // Usamos el ID de la ruta para nombrar el documento en Firebase
                        String idDocumento = String.valueOf(ruta.getRutaID());

                        db.collection("rutas_oficiales")
                                .document(idDocumento)
                                .set(ruta, SetOptions.merge()) // Actualiza sin borrar datos anteriores
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseSync", "Ruta " + idDocumento + " guardada en modo offline");
                                });
                    }
                    Toast.makeText(MainActivity.this, "Rutas actualizadas para uso sin internet 📶", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ruta>> call, Throwable t) {
                Log.e("FirebaseSync", "No se pudo conectar a la API. Usando caché de Firebase.", t);
            }
        });
    }
}