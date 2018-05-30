package com.keeptoo.toajam.geoupdates.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.geoupdates.adapters.CustomInfoWindowAdapter;
import com.keeptoo.toajam.geoupdates.service.LocationUpdateService;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.models.Notes;
import com.keeptoo.toajam.models.Towers;
import com.keeptoo.toajam.utils.FConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final static int REQUEST_LOCATION = 199;
    private SupportMapFragment mapFragment;
    private Context context;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private MarkerOptions markerOptions;
    private SessionManager sessionManager;
    private Marker towLoc;
    private ArrayList<Towers> newTowers;

    public MapsFragment() {
        getTows();
        getNotes();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ly_map, container, false);
        context = container.getContext();

        sessionManager = new SessionManager(context);
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        if (!isGPSEnabled(context)) {

            enableLoc();
        }

        //check permission
        if (isPermissionGranted()) {

            if (mapFragment == null) {
                mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }
        }

        Intent serviceIntent = new Intent(context, LocationUpdateService.class);
        context.startService(serviceIntent);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null && !googleApiClient.isConnected())
            googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (response != ConnectionResult.SUCCESS) {
            Log.d(getClass().getName(), "Google Play Service Not Available");
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), response, 1).show();
        } else {
            Log.d(getClass().getName(), "Google play service available");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(getActivity(), REQUEST_LOCATION);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationMonitor();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17f));
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        this.googleMap = googleMap;
//-------------load map items here-----------
        getNotes();
        getTows();
        loadMarkers();


        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (towLoc != null) {
                    towLoc.remove();
                }
                markerOptions = new MarkerOptions()
                        .title("Add Tow Here")
                        .snippet(" ..")
                        .position(new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude));
                towLoc = googleMap.addMarker(markerOptions);


            }
        });

        //add tows here :

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()

        {
            @Override
            public void onCameraIdle() {


                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if (marker.getTitle().equals("Add Tow Here")) {
                            towLoc.showInfoWindow();

                        } else {
                            marker.showInfoWindow();
                            try {
                                towLoc.remove();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }

                });


                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        if (marker.getTitle().equals("Add Tow Here")) {

                            marker.getPosition();
                            try {

                                showAddTowDiag(marker.getPosition().latitude, marker.getPosition().longitude, getKnownLocation(marker.getPosition().latitude, marker.getPosition().longitude));
                                //   Toast.makeText(MapActivity.this, city, Toast.LENGTH_SHORT).show();
                                Log.e(getClass().getName(), "Featured Place : " + getKnownLocation(marker.getPosition().latitude, marker.getPosition().longitude));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (marker.getTitle().startsWith("L:")) {

                            String phone_no = marker.getSnippet();
                            Log.e(getClass().getName(), "Call phone: " + phone_no);

                            //launch dialer


                            try {
                                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                                        "tel", phone_no, null));
                                startActivity(phoneIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                    }
                });
            }
        });

        //zoom
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            rebootActivity();
        } else {
            rebootActivity();
        }
    }

    private void startLocationMonitor() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(40000)
                .setFastestInterval(20000)
                .setExpirationDuration(60000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {

                    //real-time movement:

                   /* realTimeData.runTransaction(new Transaction.Handler() {
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            LatLng currenloc = mutableData.getValue(LatLng.class);
                            if (currenloc == null) {

                                if (googleApiClient.isConnected()) {

                                    Log.e(getClass().getName(), "User is online");
                                    mutableData.setValue(new LatLng(location.getLatitude(), location.getLongitude()));
                                }
                                //set active
                            } else {
                                if (!googleApiClient.isConnected()) {
                                    Log.e(getClass().getName(), "User offline");
                                    mutableData.setValue(null);
                                }
                                //set inactive
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            if (databaseError != null) {
                                Log.e(getClass().getName(), "complete with error  " + databaseError.getMessage());
                            }
                        }
                    });
*/

                    try {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
        } catch (SecurityException e) {
            Log.d(getClass().getName(), e.getMessage());
        }

    }

    public boolean isPermissionGranted() {


        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(getClass().getName(), "Permission is granted");
                return true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(getClass().getName(), "Permission is granted");
            return true;
        }

    }

    //shows add tow dialog
    private void showAddTowDiag(final double lat, final double lng, final String city) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ly_addtowservice, null);
        dialogBuilder.setView(dialogView);
        final TextInputEditText ed_name = dialogView.findViewById(R.id.ed_name);
        final TextInputEditText ed_phone = dialogView.findViewById(R.id.ed_phone);
        final TextView tv_loca = dialogView.findViewById(R.id.txt_towlocation);
        tv_loca.setText("Add Tow Service Here: " + "\n" + city);
        final AlertDialog dialog = dialogBuilder.create();
        Button button = dialogView.findViewById(R.id.btn_update);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (ed_name.getText() == null) {
                    ed_name.setError("enter a valid name");

                } else if (ed_phone.getText().length() < 10 || ed_phone.getText() == null) {
                    ed_phone.setError("enter a valid phone");
                } else {
                    try {
                        addTows(lat, lng, city, Integer.valueOf(ed_phone.getText().toString()), String.valueOf(ed_name.getText()), FConstants.COMPLETE_DATE(), true);
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        ed_phone.setError(e.getMessage());
                    }

                }


            }
        });
        dialog.show();
    }

    // new tower -add
    private void addTows(double lat, double lng, String locname, int phone, String uname, String datecreated, boolean verified) {
        String key = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country).push().getKey();
        String photourl = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        Towers towers = new Towers(uname, locname, phone, photourl, lat, lng, datecreated, true);


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, towers);
        Log.e(getClass().getName(), childUpdates.toString());

        FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.e(getClass().getName(), "Posting failed: ", databaseError.toException());
                } else if (databaseError == null) {

                    Toast.makeText(getContext(), "Successful Post", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    public ArrayList<Towers> getTows() {

        newTowers = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country);
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Towers> newTowers1 = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // towers to model
                    Towers ts = snapshot.getValue(Towers.class);
                    newTowers1.add(ts);
                    Log.e(getClass().getName(), "GetTows ::" + ts.location);

                }
                newTowers = newTowers1;
                Log.e(getClass().getName(), "GetTows Size Before ::" + newTowers.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Log.e(getClass().getName(), "GetTows Size ::" + newTowers.size());
        return newTowers;
        //add markers
    }

    public ArrayList<Notes> getNotes() {
        final DatabaseReference noteReference = FirebaseDatabase.getInstance().getReference().child("notes").child(HomeActivity.Country);
        final ArrayList<Notes> notesArrayList = new ArrayList<>();
        noteReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Notes values = snapshot.getValue(Notes.class);
                    //populate list
                    notesArrayList.add(values);

                    Log.e(getClass().getName(), "NOTE:" + values.photourl);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return notesArrayList;
    }

    private String getKnownLocation(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String city = addresses.get(0).getAddressLine(1);
        String state = addresses.get(0).getAdminArea();
        String sublocality = addresses.get(0).getSubLocality();
        //   Toast.makeText(MapActivity.this, city, Toast.LENGTH_SHORT).show();
        Log.e(getClass().getName(), "Sublocality Place : " + sublocality);

        return addresses.get(0).getFeatureName();
    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void rebootActivity() {
        Intent i = getContext().getPackageManager()
                .getLaunchIntentForPackage(getContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void loadMarkers() {


        Log.e(getClass().getName(), "Marker Tow Size:" + getTows().size());
        Log.e(getClass().getName(), "Marker Note Size:" + getNotes().size());

        LatLng loc;
        for (Towers ts : getTows()) {
            loc = new LatLng(ts.latitude, ts.longitude);
            if (loc != null) {
                if (ts.verified) {
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_tow_marker))
                            .position(loc).title("L: " + ts.location + "," + "\n" + "N: " + ts.name)
                            .snippet("0" + ts.phone));
                    CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(getActivity());
                    googleMap.setInfoWindowAdapter(adapter);
                }

            } //clean up

            //custom markers


            for (Notes notes : getNotes()) {
                //location details

                LatLng notLoc = new LatLng(notes.latitude, notes.longitude);
                if (notLoc != null) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(loc).title(notes.location + " : " + notes.name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_note_marker))
                            .snippet(String.valueOf(notes.note))).showInfoWindow();
                }
            }

        }

    }

}
