package com.keeptoo.toajam.authetication.service;

/**
 * Created by keeptoo on 5/28/2018.
 */

import com.keeptoo.toajam.authetication.model.CountryModel;

import retrofit2.http.GET;
import rx.Observable;

public interface CountryService {

    @GET("/json")
    Observable<CountryModel> getCountry();

}