package com.keeptoo.toajam.geoupdates.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.geoupdates.adapters.CustomInfoWindowAdapter;
import com.keeptoo.toajam.geoupdates.adapters.NotesAdapter;
import com.keeptoo.toajam.geoupdates.adapters.TowsAdapter;
import com.keeptoo.toajam.geoupdates.service.GeofenceRegistrationService;
import com.keeptoo.toajam.geoupdates.service.LocationUpdateService;
import com.keeptoo.toajam.geoupdates.utililies.Constants;
import com.keeptoo.toajam.home.HomeActivity;
import com.keeptoo.toajam.models.Notes;
import com.keeptoo.toajam.models.Towers;
import com.keeptoo.toajam.settings.SettingsActivity;
import com.keeptoo.toajam.utils.FConstants;
import com.keeptoo.toajam.utils.InteractionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.keeptoo.toajam.geoupdates.utililies.Constants.AREA_LANDMARKS;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;

    private GoogleMap googleMap;

    private GeofencingRequest geofencingRequest;
    private GoogleApiClient googleApiClient;

    private boolean isMonitoring = false;

    private MarkerOptions markerOptions;
    private PendingIntent pendingIntent;

    private Marker towLoc;

    private ArrayList<Towers> tows;

    private FloatingActionButton addNote;

    private SessionManager sessionManager;

    private ArrayList<Notes> notes1 = new ArrayList<>();

    private RecyclerView recyclerView;

    private RecyclerView towRecyclerView;

    private Location mlocation;

    private NotesAdapter adapter;
    private TowsAdapter towsAdapter;


    final static int REQUEST_LOCATION = 199;


    private double lat;
    private double lng;

    private BottomSheetBehavior bottomSheetBehavior;

    private BottomSheetDialog bottomSheetDialog;

    private BottomSheetDialog towbottomSheetDialog;
    private Context context;

    private InteractionUtils interactionUtils = new InteractionUtils();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            rebootActivity();
        } else {
            rebootActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_frament);
        sessionManager = new SessionManager(this);
        context = MapActivity.this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Geo-Updates");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        if (!isGPSEnabled(this)) {

            enableLoc();

        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //start the service;
        Intent serviceIntent = new Intent(context, LocationUpdateService.class);
        context.startService(serviceIntent);

        TabLayout tabLayout = findViewById(R.id.tab_items);

        if (isPermissionGranted() == true) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        loadNoteMarkers();
                        bottomSheetDialog.show();
                    } else if (tab.getPosition() == 1) {
                        loadTowMarkers();
                        towbottomSheetDialog.show();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 0) {
                        loadNoteMarkers();
                        bottomSheetDialog.show();
                    } else if (tab.getPosition() == 1) {
                        loadTowMarkers();
                        towbottomSheetDialog.show();
                    }
                }
            });

            addNoteBottomSheet();
            showTowsBottomSheet();
        } else
            Toast.makeText(context, "Please allow location permission to proceed", Toast.LENGTH_SHORT).show();


        boolean firstTimeRun = interactionUtils.getFirstTimeRun(this,"map_tap");

        if (firstTimeRun == true) {
            showTapTargetSequence();
        }

    }


    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MapActivity.this)
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
                                status.startResolutionForResult(MapActivity.this, REQUEST_LOCATION);

                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }


    private void showTapTargetSequence() {

        TapTargetSequence sequence = new TapTargetSequence(this);
        sequence.continueOnCancel(true);

        sequence.targets(

                //add notes
                TapTarget.forView(findViewById(R.id.tab_items), "Notes", "See or share location based update/note\n Call a  tow closer to you")
                        .dimColor(R.color.colorPrimaryDark)
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(R.color.colorWhite)
                        .icon(this.getResources().getDrawable(R.drawable.ic_action_plus, null))
                        .cancelable(false)
                        .textColor(android.R.color.white))


                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        interactionUtils.storeFirstTimeRun(MapActivity.this,"map_tap");
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }


                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                }).start();


    }

    public void rebootActivity() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    private void addNoteBottomSheet() {


        bottomSheetDialog = new BottomSheetDialog(MapActivity.this);
        final View sheetView = this.getLayoutInflater().inflate(R.layout.ly_notes_list_bottomsheet, null);
        bottomSheetDialog.setContentView(sheetView);


        addNote = sheetView.findViewById(R.id.fab_addnote);
        recyclerView = sheetView.findViewById(R.id.map_recyclerView);
        bottomSheetBehavior = BottomSheetBehavior.from(sheetView.findViewById(R.id.btm_bottom));

        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        final TextInputEditText ed_note = sheetView.findViewById(R.id.ed_note);
        final TextView tv_loc = sheetView.findViewById(R.id.txt_currentlocationnote);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //      bottomSheetHeading.setText(getString(R.string.text_collapse_me));
                } else {
                    //     bottomSheetHeading.setText(getString(R.string.text_expand_me));
                }

                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");

                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    mlocation = location;
                    lat = mlocation.getLatitude();
                    lng = mlocation.getLongitude();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                tv_loc.setText("Let others know what you encountered on " + "\n" + "[ " + getKnownLocation(lat, lng) + " ]");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });


        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mlocation != null) {

                    if (ed_note.getText().length() < 1) {
                        ed_note.setError("");

                    } else {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {

                                        addNote(lat, lng, String.valueOf(ed_note.getText().toString()), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FConstants.COMPLETE_DATE(), getKnownLocation(lat, lng));

                                        ed_note.setText("");

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (NumberFormatException e) {
                            ed_note.setError(e.getMessage());
                        }

                    }

                } else
                    new InteractionUtils().showToast(getApplicationContext(), "Failed : check location permission", Toast.LENGTH_SHORT);

            }
        });

    }

    private void showTowsBottomSheet() {
        towbottomSheetDialog = new BottomSheetDialog(MapActivity.this);
        final View sheetView = this.getLayoutInflater().inflate(R.layout.ly_tows_bottomsheet, null);
        towbottomSheetDialog.setContentView(sheetView);

        towRecyclerView = sheetView.findViewById(R.id.lv_tows);
        bottomSheetBehavior = BottomSheetBehavior.from(sheetView.findViewById(R.id.btm_tows));
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    //      bottomSheetHeading.setText(getString(R.string.text_collapse_me));
                } else {
                    //     bottomSheetHeading.setText(getString(R.string.text_expand_me));
                }

                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.e("Bottom Sheet Behaviour", "STATE_COLLAPSED");

                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.e("Bottom Sheet Behaviour", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.e("Bottom Sheet Behaviour", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.e("Bottom Sheet Behaviour", "STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.e("Bottom Sheet Behaviour", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


    }


    public boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private void startLocationMonitor() {
        Log.d(TAG, "start location monitor");
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
            Log.d(TAG, e.getMessage());
        }

    }

    private void startGeofencing() {
        Log.d(TAG, "Start geofencing monitoring call");
        pendingIntent = getGeofencePendingIntent();
        geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                .addGeofence(getGeofence())
                .build();

        if (!googleApiClient.isConnected()) {
            Log.d(TAG, "Google API client not connected");
        } else {
            try {
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Successfully Geofencing Connected");
                        } else {
                            Log.d(TAG, "Failed to add Geofencing " + status.getStatus());
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        isMonitoring = true;
        invalidateOptionsMenu();
    }

    @NonNull
    private Geofence getGeofence() {
        LatLng latLng = AREA_LANDMARKS.get(Constants.GEOFENCE_ID_STAN_UNI);
        return new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_ID_STAN_UNI)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(latLng.latitude, latLng.longitude, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceRegistrationService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void stopGeoFencing() {
        pendingIntent = getGeofencePendingIntent();
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess())
                            Log.d(TAG, "Stop geofencing");
                        else
                            Log.d(TAG, "Not stop geofencing");
                    }
                });
        isMonitoring = false;
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);
        if (response != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Service Not Available");
            GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, response, 1).show();
        } else {
            Log.d(TAG, "Google play service available");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null && !googleApiClient.isConnected())
            googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_activity, menu);
        /*if (isMonitoring) {
            menu.findItem(R.id.action_start_monitor).setVisible(false);
            menu.findItem(R.id.action_stop_monitor).setVisible(true);
        } else {
            menu.findItem(R.id.action_start_monitor).setVisible(true);
            menu.findItem(R.id.action_stop_monitor).setVisible(false);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notes:
                //show all notes

                break;
            case R.id.action_settings:
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private String getKnownLocation(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String city = addresses.get(0).getAddressLine(1);
        String state = addresses.get(0).getAdminArea();
        String sublocality = addresses.get(0).getSubLocality();
        //   Toast.makeText(MapActivity.this, city, Toast.LENGTH_SHORT).show();
        Log.e(getClass().getName(), "Sublocality Place : " + sublocality);

        return addresses.get(0).getFeatureName();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        this.googleMap = googleMap;
//-------------load map items here-----------
        loadTowMarkers();
        loadNoteMarkers();


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

                            //direct call - future use

                           /* Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + phone_no));
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(callIntent);

                            Log.e(getClass().getName(), "Starting Call Task");*/
                           /* if (tows != null) {
                                for (Towers s : tows) {
                                    Log.e(getClass().getName(), "Loc Tow : " + s.location);
                                }
                            }*/

                        } else {
                            //do nothing
                        }


                    }
                });
            }
        });

        //zoom

    }


    public boolean isPermissionGranted() {


        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

    }

    private void showAddTowDiag(final double lat, final double lng, final String city) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
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


    private void initNotesRecy(final ArrayList<Notes> notesArrayList) {
        // set up the RecyclerView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(new LinearLayoutManager(MapActivity.this, LinearLayoutManager.VERTICAL, true));
                adapter = new NotesAdapter(MapActivity.this, notesArrayList);
                recyclerView.setAdapter(adapter);
                if (notesArrayList.size() > 0) {
                    recyclerView.smoothScrollToPosition(notesArrayList.size() - 1);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void initTowsRec(final ArrayList<Towers> towersArrayList) {
        // set up the RecyclerView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                towRecyclerView.setLayoutManager(new LinearLayoutManager(MapActivity.this, LinearLayoutManager.VERTICAL, true));
                towsAdapter = new TowsAdapter(MapActivity.this, towersArrayList);
                towRecyclerView.setAdapter(towsAdapter);
                if (towersArrayList.size() > 0) {
                    towRecyclerView.smoothScrollToPosition(towersArrayList.size() - 1);
                }
                towsAdapter.notifyDataSetChanged();

                final GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {


                        View v = towRecyclerView.findChildViewUnder(e.getX(), e.getY());
                        try {
                            final Towers towers = towsAdapter.mTows.get(towRecyclerView.getChildAdapterPosition(v));

                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                                    "tel", 0 + String.valueOf(towers.phone), null));
                            startActivity(phoneIntent);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        Log.e(getClass().getName(), "TOUCH INTERCEPT");

                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }
                });

                towRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                        final View child = rv.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && gestureDetector.onTouchEvent(e)) {

                        }
                        return false;
                    }

                    @Override
                    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                        Log.e(getClass().getName(), "TOUCH INTERCEPT");
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                });

            }
        });

    }


    //load existing towers
    private void loadTowMarkers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("tows").child(sessionManager.getCountry());
        final ArrayList<Towers> newTowers = new ArrayList<>();
        tows = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {


                        // towers to model
                        Towers ts = snapshot.getValue(Towers.class);
                        newTowers.add(ts);
                        Log.e(getClass().getName(), "Towers data: " + ts.name + " : " + ts.phone);


                        //custom markers

                        LatLng loc = new LatLng(ts.latitude, ts.longitude);
                        if (loc != null) {
                            if (ts.verified) {
                                googleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_tow_marker))
                                        .position(loc).title("L: " + ts.location + "," + "\n" + "N: " + ts.name)
                                        .snippet("0" + ts.phone));
                                CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(MapActivity.this);
                                googleMap.setInfoWindowAdapter(adapter);
                            }
                            Log.e(getClass().getName(), "Tow Loc : I  " + ts.location + " : " + ts.latitude + " : " + ts.longitude);
                        }
                    } catch (NumberFormatException e) {

                        Log.e(getClass().getName(), "Tow - Marker Error " + e.getMessage());

                    }
                }
                tows = newTowers;
                initTowsRec(tows);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //add markers
    }

    private void doNoteCleanUp() {
        // delete old notes
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notes").child(HomeActivity.Country);
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS);
        Query oldItems = reference.orderByChild("timestamp").endAt(cutoff);

        Log.e(getClass().getName(), "Note Old Clean INIT: " + oldItems.getRef() + " Cut Off: " + cutoff);

        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                  /*  Log.e(getClass().getName(), "Note Clean Up " + itemSnapshot.getRef());
                    itemSnapshot.getRef().removeValue();*/
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void loadNoteMarkers() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doNoteCleanUp();
            }
        }, 5000);
        DatabaseReference noteReference = FirebaseDatabase.getInstance().getReference().child("notes").child(sessionManager.getCountry());
        noteReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Notes> notes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    //location details
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lng = snapshot.child("longitude").getValue(Double.class);


                    Notes values = snapshot.getValue(Notes.class);

                    //populate list
                    notes.add(values);
                    //clean up

                    //custom markers
                    LatLng loc = new LatLng(lat, lng);

                    if (loc != null) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(loc).title(values.location + " : " + values.name)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_note_marker))
                                .snippet(String.valueOf(values.note))).showInfoWindow();
                    }


                }
                notes1 = notes;
                initNotesRecy(notes1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    // new tower -add
    private void addTows(double lat, double lng, String locname, int phone, String uname, String datecreated, boolean verified) {
        String key = FirebaseDatabase.getInstance().getReference().child("tows").child(sessionManager.getCountry()).push().getKey();
        String photourl = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        Towers towers = new Towers(uname, locname, phone, photourl, lat, lng, datecreated, true);


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, towers);
        Log.e(getClass().getName(), childUpdates.toString());

        FirebaseDatabase.getInstance().getReference().child("tows").child(sessionManager.getCountry()).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.e(getClass().getName(), "Posting failed: ", databaseError.toException());
                } else if (databaseError == null) {

                    Toast.makeText(MapActivity.this, "Successful Post", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    //add notes
    private void addNote(double lat, double lng, String note, String uname, String datecreated, String city) {
        String key = FirebaseDatabase.getInstance().getReference().child("tows").child(sessionManager.getCountry()).push().getKey();
        Map<String, Object> values = new HashMap<>();
        values.put("latitude", lat);
        values.put("longitude", lng);
        values.put("note", note);
        values.put("name", uname);
        values.put("location", city);
        values.put("timestamp", ServerValue.TIMESTAMP);
        values.put("date_created", datecreated);
        values.put("post_id", key);
        values.put("photourl", String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()));

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);
        Log.e(getClass().getName(), childUpdates.toString());

        FirebaseDatabase.getInstance().getReference().child("notes").child(sessionManager.getCountry()).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Log.e(getClass().getName(), "Posting failed: ", databaseError.toException());
                } else if (databaseError == null) {

                    Toast.makeText(MapActivity.this, "Successful Post", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google Api Client Connected");
        isMonitoring = true;
        startLocationMonitor();
       /* startGeofencing();*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isMonitoring = false;
        Log.e(TAG, "Connection Failed:" + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17f));
        }

    }
}
