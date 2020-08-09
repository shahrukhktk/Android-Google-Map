package com.srkteam.maptestingapp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.IInterface;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.security.Permission;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_CODE = 9001;
    private boolean mLocationPermissionGranted;
    private FloatingActionButton floatingActionButton;
    private Double KUSTLAT = 33.523400;
    private Double KUSTLNG = 71.445774;
    private EditText searchAddress;
    private Button btnLocate;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(mLocationPermissionGranted)
        {
            Toast.makeText(this, "Ready To Map", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                }
            }
        }

        searchAddress = findViewById(R.id.searchlocation);
        btnLocate = findViewById(R.id.btnlocate);
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoLocate(view);
            }
        });

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMap != null)
                {
                    double bottomBoundary = KUSTLAT - 0.3;
                    double leftBoundary = KUSTLNG - 0.3;
                    double topBoundary = KUSTLAT + 0.3;
                    double rightBoundary = KUSTLNG + 0.3;

                    LatLngBounds kustBounds = new LatLngBounds(
                            new LatLng(bottomBoundary, leftBoundary),
                            new LatLng(topBoundary, rightBoundary)
                    );
                    //Zoom Out
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(kustBounds, 400, 400, 1));
//                    mMap.setLatLngBoundsForCameraTarget(kustBounds);
                    //Zoom In Focus to target
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(kustBounds.getCenter(), 16.0f));
                    showMarker(kustBounds.getCenter());

                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED )
        {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        gotoLocation(KUSTLAT, KUSTLNG);

        //UI CONTROL ON MAP
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void gotoLocation(Double lat, Double lng)
    {
        LatLng latLng = new LatLng(lat, lng);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10.0f);
        showMarker(latLng);

        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    private void showMarker(LatLng latLng)
    {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).title("Sydney");
        mMap.addMarker(markerOptions);
    }

    private void geoLocate(View view)
    {
        hideSoftKeyboard(view);

        String locationName = searchAddress.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
//             List<Address> addressList = geocoder.getFromLocationName(locationName, 3);
            List<Address> addressList = geocoder.getFromLocation(KUSTLAT, KUSTLNG, 3);
             if(addressList.size() > 0)
             {
                 Address address = addressList.get(0);
                 gotoLocation(address.getLatitude(), address.getLongitude());
                 mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));
             }
             //for loop for getting multiple address
             for(Address address : addressList)
             {
                 Log.d(TAG, "geoLocate: Address: "+address.getAddressLine(address.getMaxAddressLineIndex()));
             }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void hideSoftKeyboard(View view)
    {
        InputMethodManager iim = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        iim.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }


}