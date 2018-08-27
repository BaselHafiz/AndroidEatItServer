package com.bmacode17.androideatitserver.remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by User on 28-Jul-18.
 */

public class RetrofitClient1 {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl){

        if(retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
