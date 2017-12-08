package com.keeptoo.toajam.home;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.authetication.SessionManager;
import com.keeptoo.toajam.authetication.SigninFragment;
import com.keeptoo.toajam.chat.ChatFragment;
import com.keeptoo.toajam.firebase_utils.FireBaseUtilities;
import com.keeptoo.toajam.geoupdates.activities.MapActivity;
import com.keeptoo.toajam.updates.UpdatesFragment;
import com.keeptoo.toajam.utils.FConstants;
import com.keeptoo.toajam.utils.InteractionUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    @BindView(R.id.txt_user_name)
    TextView tv_name;

    @BindView(R.id.txt_user_email)
    TextView tv_email;

    @BindView(R.id.img_profile)
    CircleImageView iv_profile;

    FirebaseUser user;

    public static String Country;

    SessionManager sessionManager;

    ChatFragment chatFragment = new ChatFragment();
    SigninFragment signinFragment = new SigninFragment();
    UpdatesFragment updatesFragment = new UpdatesFragment();
    FragmentManager fm;

    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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


        //-----Handle signin start-----------


        if (!sessionManager.isUserLogedOut()) {
            if (!isNetworkAvailable()) {
                loadFrag(updatesFragment);
                new InteractionUtils().showToast(getApplicationContext(), "Please check your connection", Toast.LENGTH_LONG);
            } else
                loadFrag(updatesFragment);

            loadUserInfo();
            Log.e(getClass().getName(), "Session Manager - " + sessionManager.getEmail());

        } else {
            loadFrag(signinFragment);
        }

        //-----Handle signin end-----------
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (exit) {
                finish(); // finish activity
            } else {
                loadFrag(updatesFragment);
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

    // fragment loader
    public void loadFrag(Fragment fragment) {
        fm.beginTransaction().replace(R.id.frame_container, fragment)
                .addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        updatesFragment.search(searchView);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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

       /* if (id == R.id.nav_chat) {
            loadFrag(chatFragment);
        }*/

        if (id == R.id.nav_updates) {
            loadFrag(updatesFragment);
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
            imageView.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_icon_placeholder_white, null));

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
                iv_profile.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_action_icon_placeholder_white, null));


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