package com.keeptoo.toajam.authetication;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.Scopes;
import com.google.firebase.auth.FirebaseAuth;
import com.keeptoo.toajam.R;
import com.keeptoo.toajam.home.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.ui.ExtraConstants.EXTRA_IDP_RESPONSE;

public class SigninFragment extends Fragment {

    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final int RC_SIGN_IN = 100;
    private static String COUNTRY;
    FirebaseAuth auth;


    public SigninFragment() {

    }

    View mRootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNetworkAvailable()) {
            new AsyncTaskClass().execute();
        }
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.ly_signinwith, container, false);
        ButterKnife.bind(this, mRootView);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            startActivity(new Intent(getActivity().getApplicationContext(), HomeActivity.class));
            getActivity().finish();
        } else {
            showSignInScreen();
        }
        return mRootView;
    }

    private void showSignInScreen() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setProviders(getSelectedProviders())
                        .setTosUrl(GOOGLE_TOS_URL)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    @MainThread
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());


        return selectedProviders;
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        result.add(Scopes.DRIVE_FILE);
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar("Sign in error");
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            Intent in = new Intent(getActivity().getApplicationContext(), HomeActivity.class);
            in.putExtra(EXTRA_IDP_RESPONSE, IdpResponse.fromResultIntent(data));
            startActivity(in);


            // save logins

            new SessionManager(getActivity().getApplicationContext()).saveLoginDetails(auth.getUid(), auth.getCurrentUser().getEmail(), COUNTRY);

            //start activity
            getActivity().finish();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            //showSnackbar("Signin cancelled");
            Snackbar snackbar = Snackbar.make(mRootView, "Sign in cancelled due to internet connection, please retry", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Retry", new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    showSignInScreen();
                }
            });
            snackbar.show();

            // getActivity().finish();
            return;
        }

       /* if (resultCode == RES) {
            showSnackbar("No internet connection");
            return;
        }*/

        showSnackbar("Signin error");
    }

    @MainThread
    private void showSnackbar(String errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public static void setCountry(String country) {

        COUNTRY = country;
    }


    private class AsyncTaskClass extends AsyncTask<String, JSONObject, JSONObject> {


        @Override
        protected JSONObject doInBackground(String... strings) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://ip-api.com/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return new JSONObject(response.body().string());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(JSONObject data) {
            try {
                // super.onPostExecute(data);
                if (data.getString("country").length() != 0) {

                    setCountry(data.getString("country"));
                    Log.e("Country Post Execute: ", data.getString("country"));

                    // setCountry("");
                } else if (data.getString("country").length() == 0) {

                    Log.e("Country - Post Execute", "Nada");

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
