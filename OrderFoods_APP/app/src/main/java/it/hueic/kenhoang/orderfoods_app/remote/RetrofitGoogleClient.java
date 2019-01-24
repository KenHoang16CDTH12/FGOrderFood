package it.hueic.kenhoang.orderfoods_app.remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by kenhoang on 12/02/2018.
 */

public class RetrofitGoogleClient {
    private static Retrofit retrofit = null;
    public static Retrofit getGoogleClient(String baseURL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
