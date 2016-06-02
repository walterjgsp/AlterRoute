package com.dwv.alterroute;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Created by walterjgsp on 02/06/16.
 */
public class FragmentMap  extends Fragment {

    public static final String TAG = "FragmentMap";

    private Context mContext;

    private MapView m_MapView;

    private double longitude;
    private double latitude;
    private Location userLocation;

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
                //mapboxMap.setMyLocationEnabled(true);

                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude))  // set the camera's center position
                                .zoom(14)  // set the camera's zoom level
                                .tilt(20)  // set the camera's tilt
                                .build()));

                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("Hello World!")
                        .snippet("Welcome to my marker."));
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        latitude = getArguments().getDouble(MainActivity.TAG_LATIT);
        longitude = getArguments().getDouble(MainActivity.TAG_LONGI);
        userLocation = new Location("UserLocation");
        userLocation.setLongitude(longitude);
        userLocation.setLatitude(latitude);

        Log.e(TAG,"Lat:"+latitude+" Long: "+longitude);
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
