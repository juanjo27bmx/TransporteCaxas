package unc.edu.pe.transportcax.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import unc.edu.pe.transportcax.model.Ruta;
import unc.edu.pe.transportcax.repository.RutaRepository;

// Al heredar de "ViewModel", Android sabe que esta clase no debe borrarse si el usuario gira la pantalla.
public class MapViewModel extends ViewModel {

    private RutaRepository repository;
    private LiveData<List<Ruta>> listaRutasLiveData;

    public MapViewModel() {
        // 1. Inicializamos al "trabajador" (Repositorio)
        repository = new RutaRepository();

        // 2. Apenas nace este ViewModel, le pedimos al repositorio que
        // vaya a internet a traer las rutas y las guarde en esta variable viva.
        listaRutasLiveData = repository.obtenerRutasEnVivo();
    }

    // 3. Este es el metodo que usará la pantalla (MainActivity)
    // para quedarse "observando" en qué momento llegan las combis.
    public LiveData<List<Ruta>> getListaRutasLiveData() {
        return listaRutasLiveData;
    }
}