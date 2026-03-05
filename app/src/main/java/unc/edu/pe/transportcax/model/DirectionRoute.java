package unc.edu.pe.transportcax.model;
import com.google.gson.annotations.SerializedName;

public class DirectionRoute {
    @SerializedName("overview_polyline")
    private OverviewPolyline overviewPolyline;
    public OverviewPolyline getOverviewPolyline() { return overviewPolyline; }
}