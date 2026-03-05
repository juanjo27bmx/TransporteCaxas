package unc.edu.pe.transportcax.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DirectionsResponse {
    @SerializedName("routes")
    private List<DirectionRoute> routes;
    public List<DirectionRoute> getRoutes() { return routes; }
}