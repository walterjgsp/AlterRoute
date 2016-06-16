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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dwv.alterroute.Misc.VolleySingleton;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    //Route info visualization
    private LinearLayout container_info;
    private TextView distance_info;
    private TextView work_info;
    private TextView address_info;
    private TextView time_info;

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

                address_info.setText(autocomplete.getText().toString());

                fab_route.setVisibility(View.VISIBLE);
            }
        });

       autocomplete.setOnClickListener(new GeocoderAutoCompleteView.OnClickListener(){
           @Override
           public void onClick(View v) {
               map.clear();
               autocomplete.setText("");
               setGone();

               map.animateCamera(CameraUpdateFactory.newCameraPosition(
                       new CameraPosition.Builder()
                               .target(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()))  // set the camera's center position
                               .zoom(14)  // set the camera's zoom level
                               .tilt(20)  // set the camera's tilt
                               .build()),5000,null);
           }
       });

        container_info = (LinearLayout) view.findViewById(R.id.container_info);
        distance_info = (TextView) view.findViewById(R.id.distance_info);
        address_info = (TextView) view.findViewById(R.id.adress_info);
        work_info = (TextView) view.findViewById(R.id.work_info);
        time_info = (TextView) view.findViewById(R.id.time_info);

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
                .zoom(14)
                .tilt(20)
                .build();

        //destination = new Waypoint(latitude,longitude);
        destination = Position.fromCoordinates(latitude,longitude);

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }

    public void setVisibile(){
        container_info.setVisibility(View.VISIBLE);
        fab_route.setVisibility(View.GONE);
    }

    public void setGone(){
        container_info.setVisibility(View.INVISIBLE);
        fab_route.setVisibility(View.GONE);
    }

    public void setTime(double time){

        long millis = (long) time;

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if(days>0) {
            sb.append(days);
            sb.append("d");
        }
        if(hours>0) {
            sb.append(hours);
            sb.append("h");
        }
        if(minutes>0) {
            sb.append(minutes);
            sb.append("m");
        }
        if(seconds>0) {
            sb.append(seconds);
            sb.append("s");
        }

        Log.d(TAG,sb.toString()+" AQUI");

        time_info.setText(sb.toString());
    }

    public void setInfoDist(double dist){

        String ad = "m";
        if(dist>1000){
           dist=dist/1000;
            ad = "km";
        }

        StringBuffer dist_print = new StringBuffer();
        dist_print.append(String.format( "%.2f", dist )+ad);

        distance_info.setText(dist_print.toString());

    }

    public void setInfoWork(double work){

        String ad = "W";
        if(work>1000){
            work=work/1000;
            ad = "kW";
        }

        StringBuffer work_print = new StringBuffer();
        work_print.append(String.format( "%.2f", work )+ad);

        work_info.setText(work_print.toString());
        setVisibile();
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setAccessToken(mContext.getResources().getString(R.string.accessToken))
                .setOrigin(Position.fromCoordinates(origin.getLatitude(),origin.getLongitude()))
                .setDestination(Position.fromCoordinates(destination.getLatitude(),destination.getLongitude()))
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAlternatives(true)
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                }

                Log.d(TAG,"Quantity: "+response.body().getRoutes().size());
                // Print some info about the route
                Log.d(TAG,"Time: "+response.body().getRoutes().get(0).getDuration());
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

    private double calculateWork(ArrayList<Double> elevations){
        double work = 0;
        double h1 = elevations.get(0);

        for(int i=1;i<elevations.size();i++){
            work+=Math.abs(h1-elevations.get(i))*9.832;
            h1=elevations.get(i);
        }

        return work;
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]

        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();

        double distance = route.getDistance();
        double time = route.getDuration();

        StringBuffer elevCoordinates = new StringBuffer();

        elevCoordinates.append(coordinates.get(0).getLatitude()+","+coordinates.get(0).getLongitude());

        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {

            if(i>0)
                elevCoordinates.append("|"+coordinates.get(i).getLatitude()+","+coordinates.get(i).getLongitude());

            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        try {
            getElevationfromGoogleMaps(elevCoordinates.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        setInfoDist(distance);
        setTime(time);

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    public void getElevationfromGoogleMaps(String requisition) throws MalformedURLException {
        StringBuffer uri = new StringBuffer();
        uri.append("https://maps.googleapis.com/maps/api/elevation/json?locations=");
        uri.append(requisition);
        uri.append("&key=");
        uri.append(mContext.getResources().getString(R.string.accessTokenGoogle));

        URL url = new URL(uri.toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url.toString(), null, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //we are interested in the results

                        ArrayList<Double> elevations = new ArrayList<>();

                        try {
                            JSONArray results = response.getJSONArray("results");
                            //iterate through the results
                            for(int i = 0; i<results.length();i++)
                            {
                                JSONObject single = (JSONObject) results.get(i);
                                //for example, to get the location which is nested 2 levels in
                                double elevation = single.getDouble("elevation");
                                //within geometry object is location
                                JSONObject location = single.getJSONObject("location");

                                elevations.add(elevation);
                            }

                            double work = calculateWork(elevations);

                            Log.d(TAG,"Quant "+elevations.size()+" work: "+work);
                            setInfoWork(work);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("Error", error.toString());
                    }
                });

        VolleySingleton.getInstance(mContext).addToRequestQueue(jsObjRequest);
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
