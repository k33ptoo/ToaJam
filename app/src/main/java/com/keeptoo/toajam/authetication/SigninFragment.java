package com.keeptoo.toajam.authetication;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.keeptoo.toajam.authetication.classes.ApiUtils;
import com.keeptoo.toajam.authetication.model.CountryModel;
import com.keeptoo.toajam.authetication.service.CountryService;
import com.keeptoo.toajam.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.firebase.ui.auth.ui.ExtraConstants.EXTRA_IDP_RESPONSE;

public class SigninFragment extends Fragment {

    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final int RC_SIGN_IN = 100;
    private static String COUNTRY;
    FirebaseAuth auth;
    View mRootView;
    private CountryService countryService;

    public SigninFragment() {

    }

    public static void setCountry(String country) {

        COUNTRY = country;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        countryService = ApiUtils.getCountryService();
        loadCountry();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void loadCountry() {

        countryService.getCountry().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<CountryModel>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(getClass().getName(), "Error: " + throwable.getMessage());
            }

            @Override
            public void onNext(CountryModel countryModel) {

                setCountry(countryModel.getCountry());
                Log.e(getClass().getName(), "RXCountry: " + countryModel.getCountry());
            }
        });

    }

}
