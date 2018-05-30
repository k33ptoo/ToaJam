package com.keeptoo.toajam.authetication.service;

/**
 * Created by keeptoo on 5/28/2018.
 */

import com.keeptoo.toajam.authetication.model.CountryModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CountryService {

    @GET("/json")
    Call<CountryModel> getCountry();

}