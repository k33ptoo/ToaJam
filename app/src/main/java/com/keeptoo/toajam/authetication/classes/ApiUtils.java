package com.keeptoo.toajam.authetication.classes;

import com.keeptoo.toajam.authetication.service.CountryService;

/**
 * Created by keeptoo on 5/28/2018.
 */
public class ApiUtils {

    public static final String BASE_URL = "http://ip-api.com/";

    public static CountryService getCountryService() {
        return RetrofitClient.getClient(BASE_URL).create(CountryService.class);
    }
}