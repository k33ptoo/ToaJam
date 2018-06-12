package com.keeptoo.toajam.geoupdates.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.geoupdates.adapters.CustomInfoWindowAdapter;
import com.keeptoo.toajam.geoupdates.service.LocationUpdateService;
import com.keeptoo.toajam.home.activities.HomeActivity;
import com.keeptoo.toajam.models.Notes;
import com.keeptoo.toajam.models.Towers;
import com.keeptoo.toajam.utils.FConstants;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

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
    private Marker towLoc;
    private LovelyCustomDialog customDialog;
    private LovelyCustomDialog noteCustomDiag;

    public MapsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ly_map, container, false);

        context = container.getContext();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        if (!isGPSEnabled(context)) {
            enableLoc();
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.lyRootView), " Seems your Location is off, please turn it on to view nearby notes and tows", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.WHITE);
            //snackbar.setAction("Enable", view12 -> enableLoc());
            snackbar.show();

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

//show note dialog
        FloatingActionButton floatingActionButtonNotes = getActivity().findViewById(R.id.fabSharenote);
        floatingActionButtonNotes.setOnClickListener(view1 -> showNoteDialog());

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

        if (googleApiClient != null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.e(getClass().getName(), "Location result");

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(connectionResult -> Log.d("Location error", "Location error " + connectionResult.getErrorCode())).build();
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
            result.setResultCallback(result1 -> {
                final Status status = result1.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                            // getActivity().finish();
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.CANCELED:
                       // rebootActivity();
                        break;
                    case LocationSettingsStatusCodes.SUCCESS:
                       // rebootActivity();
                        break;
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

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        this.googleMap = googleMap;
//-------------load map items here-----------
        loadNotes();
        loadTows();

        googleMap.setTrafficEnabled(true);

        googleMap.setOnCameraMoveListener(() -> {
            if (towLoc != null) {
                towLoc.remove();
            }
            markerOptions = new MarkerOptions()
                    .title("Add Tow Here")
                    .snippet(" ..")
                    .position(new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude));
            towLoc = googleMap.addMarker(markerOptions);


        });

        //add tows here :

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()

        {
            @Override
            public void onCameraIdle() {


                googleMap.setOnMarkerClickListener(marker -> {

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
                });


                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        if (marker.getTitle().equals("Add Tow Here")) {

                            marker.getPosition();
                            try {


                                LatLng latLng = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                                showTowAddDialog(latLng, getKnownLocation(latLng));


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

    private void showTowAddDialog(LatLng latLng, final String city) {
        customDialog = new LovelyCustomDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ly_addtowservice, null);
        customDialog
                .setView(view)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle("Add Tow")
                .setMessage("Add Tow Service Here: " + city)
                .setListener(R.id.btn_update, v -> addTow(view, latLng, city))
                .setIcon(R.drawable.ic_tow_truck)
                .show();

    }

    private void addTow(View dialogView, LatLng latLng, final String city) {
        final TextInputEditText ed_name = dialogView.findViewById(R.id.ed_name);
        final TextInputEditText ed_phone = dialogView.findViewById(R.id.ed_phone);
        if (ed_name.getText() == null) {
            ed_name.setError("enter a valid name");

        } else if (ed_phone.getText().length() < 10 || ed_phone.getText() == null) {
            ed_phone.setError("enter a valid phone");
        } else {
            try {
                addTows(latLng, city, Integer.valueOf(ed_phone.getText().toString()), String.valueOf(ed_name.getText()), FConstants.COMPLETE_DATE(), true);

            } catch (NumberFormatException e) {
                ed_phone.setError(e.getMessage());
            }

        }

    }

    // new tower -add
    private void addTows(LatLng latLng, String locname, int phone, String uname, String datecreated, boolean verified) {
        String key = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country).push().getKey();
        String photourl = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        Towers towers = new Towers(uname, locname, phone, photourl, latLng.latitude, latLng.longitude, datecreated, true);


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, towers);
        Log.e(getClass().getName(), childUpdates.toString());

        FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.e(getClass().getName(), "Posting failed: ", databaseError.toException());
                } else if (databaseError == null) {
                    customDialog.dismiss();
                    Toast.makeText(getContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    private void loadTows() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country);
        reference.addValueEventListener(new ValueEventListener() {
            ArrayList<Towers> towers = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // towers to model
                    Towers ts = snapshot.getValue(Towers.class);

                    LatLng loc = new LatLng(ts.latitude, ts.longitude);
                    if (loc != null) {
                        if (ts.verified) {
                            googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_tow_marker))
                                    .position(loc).title("L: " + ts.location + "," + "\n" + "N: " + ts.name)
                                    .snippet("0" + ts.phone));
                            CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(getActivity());
                            googleMap.setInfoWindowAdapter(adapter);
                        }

                    }
                    //TODO : add tows
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadNotes() {
        final DatabaseReference noteReference = FirebaseDatabase.getInstance().getReference().child("notes").child(HomeActivity.Country);
        noteReference.addValueEventListener(new ValueEventListener() {
            ArrayList<Notes> notes = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Notes notes = snapshot.getValue(Notes.class);
                    //populate list

                    LatLng notLoc = new LatLng(notes.latitude, notes.longitude);

                    if (notLoc != null) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(notLoc).title(notes.location + " : " + notes.name)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_note_marker))
                                .snippet(String.valueOf(notes.note))).showInfoWindow();
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getKnownLocation(LatLng latLng) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
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


    //add note

    public void rebootActivity() {
        Intent i = getContext().getPackageManager()
                .getLaunchIntentForPackage(getContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    // show notes dialog
    private void showNoteDialog() {
        noteCustomDiag = new LovelyCustomDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ly_notes_add, null);
        noteCustomDiag
                .setView(view)
                .setTopColorRes(R.color.colorPrimary)
                .setTitle("Add notes to your route");


        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(context);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(location -> {

            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    String city = getKnownLocation(latLng);

                    noteCustomDiag.setMessage("Let others know what you encountered on " + "\n" + "[ " + city + " ]")
                            .setListener(R.id.btnAddnote, v -> insertNotes(view, new LatLng(location.getLatitude(), location.getLongitude()), city))
                            .setIcon(R.drawable.pin)
                            .show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, "Location is seems to be empty or off, please turn it on try again", Toast.LENGTH_SHORT).show();
            }

        });
    }


    //add notes
    private void insertNotes(View view, LatLng latLng, String city) {
        EditText editText = view.findViewById(R.id.ed_note);
        if (editText.getText().length() > 3) {
            String key = FirebaseDatabase.getInstance().getReference().child("tows").child(HomeActivity.Country).push().getKey();
            Map<String, Object> values = new HashMap<>();
            values.put("latitude", latLng.latitude);
            values.put("longitude", latLng.longitude);
            values.put("note", editText.getText().toString());
            values.put("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            values.put("location", city);
            values.put("timestamp", ServerValue.TIMESTAMP);
            values.put("date_created", FConstants.COMPLETE_DATE());
            values.put("post_id", key);
            values.put("photourl", String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()));

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, values);
            Log.e(getClass().getName(), childUpdates.toString());

            FirebaseDatabase.getInstance().getReference().child("notes").child(HomeActivity.Country).updateChildren(childUpdates, (databaseError, databaseReference) -> {

                if (databaseError != null) {
                    Toast.makeText(getContext(), "Failed to add note", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(getContext(), "Successfully added", Toast.LENGTH_SHORT).show();
                    noteCustomDiag.dismiss();
                }

            });
        } else
            editText.setError("Enter a valid note ;-)");
    }


}
