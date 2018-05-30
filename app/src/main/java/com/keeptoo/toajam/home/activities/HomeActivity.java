package com.keeptoo.toajam.home.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbb20.CountryCodePicker;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.authetication.SigninActivity;
import com.keeptoo.toajam.firebase_utils.FireBaseUtilities;
import com.keeptoo.toajam.geoupdates.activities.MapActivity;
import com.keeptoo.toajam.home.adapters.HomeViewPagerAdapter;
import com.keeptoo.toajam.home.fragments.UpdatesFragment;
import com.keeptoo.toajam.settings.SettingsActivity;
import com.keeptoo.toajam.utils.FConstants;
import com.keeptoo.toajam.utils.InteractionUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static String Country;
    @BindView(R.id.txt_user_name)
    TextView tv_name;
    @BindView(R.id.txt_user_email)
    TextView tv_email;
    @BindView(R.id.img_profile)
    CircleImageView iv_profile;
    FirebaseUser user;
    SessionManager sessionManager;
    UpdatesFragment updatesFragment = new UpdatesFragment();
    FragmentManager fm;
    InteractionUtils interactionUtils = new InteractionUtils();
    FloatingActionButton fab;
    Context context;
    private AdView mAdView;
    private Boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = HomeActivity.this;


        user = FirebaseAuth.getInstance().getCurrentUser();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showUpdateStatusDialog();
                showBottomSheet();

            }
        });

        sessionManager = new SessionManager(HomeActivity.this);


        Country = sessionManager.getCountry();

        fm = getFragmentManager();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View v = navigationView.getHeaderView(0);

        ButterKnife.bind(this, v);


        //setup viewpager
        ViewPager viewPager = findViewById(R.id.vp_home);
        HomeViewPagerAdapter viewPagerAdapter = new HomeViewPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout2 = findViewById(R.id.tab_home);
        tabLayout2.setupWithViewPager(viewPager);

        //end of viewpager

        //-----Handle signin start-----------

        if (!sessionManager.isUserLogedOut()) {
            if (!isNetworkAvailable()) {
                Toast.makeText(getApplicationContext(),
                        "Please check your connection", Toast.LENGTH_SHORT).show();
            } else {

                loadUserInfo();
                try {
                    loadAds();
                } catch (Exception e) {

                }
                Log.e(getClass().getName(), "Session Manager - " + sessionManager.getEmail());
            }

        } else {
            startActivity(new Intent(this, SigninActivity.class));
        }

        //-----Handle signin end-----------


        //introduction
        boolean firstTimeRun = interactionUtils.getFirstTimeRun(this, "home_tap");

        if (firstTimeRun == true) {
            showTapTargetSequence();
        }


    }


    private void loadAds() { //load ads
        final RelativeLayout layout_ad = findViewById(R.id.rel_adview);
        mAdView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final ImageView imageView = findViewById(R.id.expandedImage);
        mAdView.setAdListener(new AdListener() {
                                  @Override
                                  public void onAdFailedToLoad(int i) {

                                      imageView.setVisibility(View.VISIBLE);
                                      layout_ad.setVisibility(View.INVISIBLE);
                                      super.onAdFailedToLoad(i);
                                  }

                                  @Override
                                  public void onAdLoaded() {

                                      //show custom ad
                                      loadCustomAd(imageView);

                                      imageView.postDelayed(new Runnable() {
                                          @Override
                                          public void run() {
                                              imageView.setVisibility(View.INVISIBLE);
                                              layout_ad.setVisibility(View.VISIBLE);

                                              LottieAnimationView animationView = findViewById(R.id.animation_view);
                                              animationView.setAnimation("pinjump.json");
                                              animationView.loop(true);
                                              animationView.playAnimation();

                                          }
                                      }, 30000);


                                      super.onAdLoaded();
                                  }
                              }

        );
    }
//load custom advert

    private void loadCustomAd(final ImageView view) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(HomeActivity.Country).child("toolbar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.e(getClass().getName(), "AD URL " + uri);
                Picasso.with(getApplicationContext()).load(uri).into(view);
            }
        });


    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish(); // finish activity
            } else {
                //loadFrag(updatesFragment);
                Toast.makeText(this, "Press Back again to Exit.",
                        Toast.LENGTH_SHORT).show();
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 4 * 1000);

            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
       // updatesFragment.search(searchView);
        return true;
    }


    private void showTapTarget(int view, String title, String content, Drawable icon) {

        TapTargetView.showFor(HomeActivity.this,
                TapTarget.forView(findViewById(view), title, content)
                        .outerCircleColor(R.color.colorAccent)
                        .outerCircleAlpha(0.96f)
                        .targetCircleColor(R.color.colorWhite)
                        .titleTextSize(20)
                        .titleTextColor(R.color.colorWhite)
                        .descriptionTextSize(10)
                        .descriptionTextColor(R.color.colorWhite)
                        .textColor(R.color.colorWhite)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.common_google_signin_btn_text_light_default)
                        .icon(icon)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);

                    }
                });
    }


    private void showTapTargetSequence() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        TapTargetSequence sequence = new TapTargetSequence(this);
        sequence.continueOnCancel(true);

        sequence.targets(
           /*     //search
                TapTarget.forToolbarMenuItem(toolbar, R.id.search, "Search", "Search for specific update")
                        .dimColor(android.R.color.white)
                        .outerCircleColor(R.color.colorWhite)
                        .targetCircleColor(R.color.colorPrimary)
                        .icon(this.getResources().getDrawable(R.drawable.ic_action_plus, null))
                        .cancelable(false)
                        .textColor(android.R.color.white),
                //settings
                TapTarget.forToolbarMenuItem(toolbar, R.id.action_settings, "Settings", "Click to change notification settings")
                        .dimColor(android.R.color.white)
                        .outerCircleColor(R.color.colorWhite)
                        .targetCircleColor(R.color.colorPrimary)
                        .icon(this.getResources().getDrawable(R.drawable.ic_action_plus, null))
                        .cancelable(false)
                        .textColor(android.R.color.white),*/
                //add update
                TapTarget.forView(findViewById(R.id.fab), "Update", "Click to share traffic update")
                        .dimColor(R.color.colorPrimaryDark)
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(R.color.colorWhite)
                        .icon(ContextCompat.getDrawable(this,R.drawable.ic_action_plus))
                        .cancelable(false)
                        .textColor(android.R.color.white))

                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        interactionUtils.storeFirstTimeRun(HomeActivity.this, "home_tap");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();

        //TODO : Remove and use fragment instead
       /* if (id == R.id.nav_twitter) {
            // loadFrag(twitterFragment);
        }

        if (id == R.id.nav_updates) {
            //loadFrag(updatesFragment);
        } else*/
        if (id == R.id.nav_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_mylocation) {
            startActivity(new Intent(HomeActivity.this, MapActivity.class));

        } else if (id == R.id.nav_share) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Download ToaJam App na Utoe Jam" + "\n" + "https://play.google.com/store/apps/details?id=com.keeptoo.toajam");
            sendIntent.setType("text/plain");
            HomeActivity.this.startActivity(Intent.createChooser(sendIntent, ""));
        } else if (id == R.id.nav_signout) {


            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("ToaJam Sign Out");
            builder.setMessage("You sure you wanna signout?")
                    .setPositiveButton("Yeah! Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sessionManager.logOut();

                            // reboot app

                            FirebaseAuth.getInstance().signOut();
                            Intent intent = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();


                            //
                        }
                    })
                    .setNegativeButton("Nah", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            final AlertDialog dialog = builder.create();
            dialog.show();

        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showCountryDialog() {
        MaterialDialog.Builder p = new MaterialDialog.Builder(HomeActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.ly_selectcountry, null);
        p.customView(dialogView, true);
        final CountryCodePicker cpp = dialogView.findViewById(R.id.cpp_list);
        p.title("We would like to know your country");
        p.positiveText("OK");
        p.negativeText("Cancel");
        p.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                sessionManager.setCountry(cpp.getSelectedCountryName());
                rebootActivity();

            }
        });
        p.cancelable(false);
        p.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                rebootActivity();
            }
        });
        p.show();

    }

    public void rebootActivity() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    //use bottom sheet
    private void showBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        final View sheetView = this.getLayoutInflater().inflate(R.layout.ly_alertbottomsheet, null);
        bottomSheetDialog.setContentView(sheetView);
        final FloatingActionButton fab_showsheet = sheetView.findViewById(R.id.fab_showbottomsheet);
        ImageButton btn_share = sheetView.findViewById(R.id.button_post_update);


        TextView tv_user = sheetView.findViewById(R.id.txt_diagname);
        TextView tv_loc = sheetView.findViewById(R.id.tv_currentroad);
        CircleImageView imageView = sheetView.findViewById(R.id.iv_image);
        tv_user.setText(user.getDisplayName().toString());
        tv_loc.setText("How's traffic where you are?");

        if (user.getPhotoUrl() == null) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_action_icon_placeholder_white));

        } else
            Picasso.with(this).load(user.getPhotoUrl()).into(imageView);

        final TextInputEditText editText = sheetView.findViewById(R.id.field_update_text);

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate update info
                if (String.valueOf(editText.getText()).length() > 10) {
                    FireBaseUtilities f = new FireBaseUtilities();

                    // calculate date


                    // end date
                    if (isNetworkAvailable()) {
                        //sendPost(String.valueOf(editText.getText()), wholedate);
                        f.writeNewPost(user.getUid(), user.getDisplayName(), FConstants.COMPLETE_DATE(), String.valueOf(editText.getText()), String.valueOf(user.getPhotoUrl()), getApplicationContext());
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Update not successful, Check your connection", Toast.LENGTH_SHORT).show();
                    }


                } else
                    editText.setError("Please provide more update info..");


            }
        });


        fab_showsheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        bottomSheetDialog.show();
    }

    private void loadUserInfo() {

        if (user != null && Country.length() != 0) {
            // User is signed in
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photoUrl = String.valueOf(user.getPhotoUrl());
            String uid = user.getUid();

            //load ui profile info
            tv_email.setText(email);
            tv_name.setText(name);
            if (user.getPhotoUrl() != null) {
                Picasso.with(this).load(photoUrl).into(iv_profile);
            } else
                iv_profile.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_icon_placeholder_white));


            // add user to database

            Log.e(getClass().getName(), "Country After :" + Country);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("uid", uid);
            childUpdates.put("name", name);
            childUpdates.put("email", email);
            childUpdates.put("country", Country);


            // other properties here

            database.getReference("users").child(Country).child(user.getUid()).updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        // inform successful update
                    }
                }
            });
        } else
            showCountryDialog();
    }


}
