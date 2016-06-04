package com.dwv.alterroute;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.GeocodingFeature;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by walterjgsp on 02/06/16.
 */
public class FragmentMap  extends Fragment {

    public static final String TAG = "FragmentMap";

    private Context mContext;

    private MapView m_MapView;
    private MapboxMap map;

    private ImageButton fab_route;

    private DirectionsRoute currentRoute;

    //private Waypoint origin;
    private Position origin;
    // Plaza del Triunfo in Granada, Spain.
    //private Waypoint destination;
    private Position destination;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_map,container,false);

         /*Create MapView*/
        m_MapView = (MapView) view.findViewById(R.id.mapview);
        m_MapView.onCreate(savedInstanceState);

        m_MapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Customize map with markers, polylines, etc.
                mapboxMap.setMyLocationEnabled(true);

                //origin = new Waypoint(mapboxMap.getMyLocation().getLatitude(), mapboxMap.getMyLocation().getLongitude());
                origin = Position.fromCoordinates(mapboxMap.getMyLocation().getLatitude(), mapboxMap.getMyLocation().getLongitude());

                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(mapboxMap.getMyLocation().getLatitude(), mapboxMap.getMyLocation().getLongitude()))  // set the camera's center position
                                .zoom(14)  // set the camera's zoom level
                                .tilt(20)  // set the camera's tilt
                                .build()));
                map=mapboxMap;
            }
        });

        fab_route = (ImageButton) view.findViewById(R.id.fabButton);

        fab_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"Clicado");
                // Add origin and destination to the map
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
                        .title("Origem")
                        .snippet("User Location"));

                // Get route from API
                try {
                    getRoute(origin, destination);
                } catch (ServicesException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set up autocomplete widget
        final GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) view.findViewById(R.id.query);
        autocomplete.setAccessToken(mContext.getResources().getString(R.string.accessToken));
        autocomplete.setType(GeocodingCriteria.TYPE_ADDRESS);
        autocomplete.setProximity(origin);
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(GeocodingFeature feature) {

                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude(),autocomplete.getText().toString());

                fab_route.setVisibility(View.VISIBLE);
            }
        });

       autocomplete.setOnClickListener(new GeocoderAutoCompleteView.OnClickListener(){
           @Override
           public void onClick(View v) {
               map.clear();
               autocomplete.setText("");

               map.moveCamera(CameraUpdateFactory.newCameraPosition(
                       new CameraPosition.Builder()
                               .target(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()))  // set the camera's center position
                               .zoom(14)  // set the camera's zoom level
                               .tilt(20)  // set the camera's tilt
                               .build()));
           }
       });

        return view;
    }

    private void updateMap(double latitude, double longitude,String resultName) {
        // Build marker

        int i = resultName.indexOf(' ');

        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(resultName.substring(0,i))
                .snippet(resultName));

        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();

        //destination = new Waypoint(latitude,longitude);
        destination = Position.fromCoordinates(latitude,longitude);

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setAccessToken(mContext.getResources().getString(R.string.accessToken))
                .setOrigin(Position.fromCoordinates(origin.getLatitude(),origin.getLongitude()))
                .setDestination(Position.fromCoordinates(destination.getLatitude(),destination.getLongitude()))
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAlternatives(true)
                .build();

        Log.d(TAG,"origin:"+origin.getLatitude()+" "+origin.getLongitude());
        Log.d(TAG,"destination:"+destination.getLatitude()+" "+destination.getLongitude());

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(mContext, "Route is " +  currentRoute.getDistance() + " meters long.", Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(mContext, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialise your views
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        m_MapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_MapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        m_MapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_MapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        m_MapView.onSaveInstanceState(outState);
    }
}
