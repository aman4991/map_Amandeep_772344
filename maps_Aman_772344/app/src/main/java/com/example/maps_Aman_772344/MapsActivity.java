package com.example.maps_Aman_772344;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String MyPREFERENCES = "MyPrefs";  // use in shared prefrences

    private GoogleMap mMap;
    private double lat, lng;
    private PolygonOptions polygonOptions = new PolygonOptions(); // area  Colour
    private String title;
    private Gson gson = new Gson();  // use in shared prefrences
    public static LocalData localData = new LocalData();  // use in shared prefrences
    private SharedPreferences.Editor editor; // use in shared prefrences
    private  String data; // use in shared prefrences
    private Polygon polygon; // red line
    private int dragPosition = -1;
    private boolean isDragged = false;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //initializing Shared preference
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        data = sharedpreferences.getString("data", "");
        if (data.length() > 1) {
            try {
                localData = (gson.fromJson(data, LocalData.class));
            } catch (Exception e) {
                localData = new LocalData();
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        showAllMarkers();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lat = latLng.latitude;
                lng = latLng.longitude;

                if (localData.locationList.size() < 4) {
                    mMap.clear();
                    LocationAddress.getAddressFromLocation(latLng.latitude, latLng.longitude,
                            getApplicationContext(), new GeoCoderHandler());
                    if (!localData.locationList.isEmpty()) {
                        showAllMarkers();
                    }
                } else {
                    double latitude1 = localData.locationList.get(0).getLatitude();
                    double longitude1 = localData.locationList.get(0).getLongitude();
                    double latitude2 = localData.locationList.get(1).getLatitude();
                    double longitude2 = localData.locationList.get(1).getLongitude();
                    double latitude3 = localData.locationList.get(2).getLatitude();
                    double longitude3 = localData.locationList.get(2).getLongitude();
                    double latitude4 = localData.locationList.get(3).getLatitude();
                    double longitude4 = localData.locationList.get(3).getLongitude();

                    float distance = 0;

                    Location Line1 = new Location("Line1");
                    Line1.setLatitude(latitude1);
                    Line1.setLongitude(longitude1);

                    Location Line2 = new Location("Line2");
                    Line2.setLatitude(latitude2);
                    Line2.setLongitude(longitude2);

                    Location Line3 = new Location("Line3");
                    Line3.setLatitude(latitude3);
                    Line3.setLongitude(longitude3);

                    Location Line4 = new Location("Line4");
                    Line4.setLatitude(latitude4);
                    Line4.setLongitude(longitude4);

                    distance = Line1.distanceTo(Line2) / 1000; // in km
                    distance += Line2.distanceTo(Line3) / 1000; // in km
                    distance += Line3.distanceTo(Line4) / 1000; // in km
                    distance += Line4.distanceTo(Line1) / 1000; // in km

                    Toast.makeText(MapsActivity.this, "Total Distance" + distance, Toast.LENGTH_SHORT).show();

                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mMap.clear();
                isDragged = true;
                for (int i = 0; i < localData.locationList.size(); i++) {
                    if (marker.getTitle().equalsIgnoreCase(localData.locationList.get(i).getTitle())) {
                        dragPosition = i;
                    }
                }
                LatLng latLng = marker.getPosition();
                lat = latLng.latitude;
                lng = latLng.longitude;
                LocationAddress.getAddressFromLocation(latLng.latitude, latLng.longitude,
                        MapsActivity.this, new GeoCoderHandler());
            }
        });
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                double latitudeA = polyline.getPoints().get(0).latitude;
                double longitudeA = polyline.getPoints().get(0).longitude;
                double latitudeB = polyline.getPoints().get(1).latitude;
                double longitudeB = polyline.getPoints().get(1).longitude;

                float distance = 0;
                Location crntLocation = new Location("crntlocation");
                crntLocation.setLatitude(latitudeA);
                crntLocation.setLongitude(longitudeA);

                Location newLocation = new Location("newlocation");
                newLocation.setLatitude(latitudeB);
                newLocation.setLongitude(longitudeB);

                distance = crntLocation.distanceTo(newLocation) / 1000; // in km
                Toast.makeText(MapsActivity.this, "" + distance, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAllMarkers() {
        ArrayList<LatLng> place = new ArrayList<>();
        if (!localData.locationList.isEmpty()) {
            for (int i = 0; i < localData.locationList.size(); i++) {
                DetailsData data = localData.locationList.get(i);
                LatLng places = new LatLng(data.getLatitude(), data.getLongitude());
                place.add(places);
                mMap.addMarker(new MarkerOptions().position(places).title(data.getTitle()).draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(places, 9));
            }

            addPolyLines(place);
            countPolygonPoints(place);
        }
    }

    private void addPolyLines(ArrayList<LatLng> place) {
        mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(place)
                .color(Color.RED)
                .add(place.get(0)));
    }

    private void countPolygonPoints(ArrayList<LatLng> place) {
        polygonOptions.addAll(place);
        if (polygonOptions.getPoints().size() > 3) {
            polygonOptions.strokeWidth((float) 0.30);
            polygonOptions.fillColor(getResources().getColor(R.color.colorTransparentGreen));
            polygon = mMap.addPolygon(polygonOptions);
        }

    }

    private void saveInSharedPreference() {
        String data = gson.toJson(localData, LocalData.class);
        editor.putString("data", data);
        editor.apply();
    }

    private class GeoCoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            if (isDragged) {
                isDragged = false;
                DetailsData detailsData = new DetailsData();
                detailsData.setLongitude(lng);
                detailsData.setLatitude(lat);
                detailsData.setTitle(locationAddress);
                localData.locationList.set(dragPosition, detailsData);
                ArrayList<LatLng> places = new ArrayList<>();
                for (int i = 0; i < localData.locationList.size(); i++) {
                    LatLng latlng = new LatLng(localData.locationList.get(i).getLatitude(), localData.locationList.get(i).getLongitude());
                    places.add(latlng);
                }
                countPolygonPoints(places);
            } else {
                DetailsData detailsData = new DetailsData();
                detailsData.setLongitude(lng);
                detailsData.setLatitude(lat);
                detailsData.setTitle(locationAddress);
                localData.locationList.add(detailsData);
            }
            saveInSharedPreference();
            showAllMarkers();

        }
    }
}
