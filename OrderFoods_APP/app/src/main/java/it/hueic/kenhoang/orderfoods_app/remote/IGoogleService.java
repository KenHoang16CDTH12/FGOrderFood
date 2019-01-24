package it.hueic.kenhoang.orderfoods_app.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by kenhoang on 12/02/2018.
 */

public interface IGoogleService {
    @GET
    Call<String> getAddressName(@Url String url);
}
