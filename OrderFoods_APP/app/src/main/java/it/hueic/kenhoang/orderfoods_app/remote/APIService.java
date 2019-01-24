package it.hueic.kenhoang.orderfoods_app.remote;

import it.hueic.kenhoang.orderfoods_app.model.MyReponse;
import it.hueic.kenhoang.orderfoods_app.model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by kenhoang on 09/02/2018.
 */

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA7eoU9m0:APA91bGn6Y3TVVfEIE9gfejpR3C-VRcbsAjBFjqMWyoWFk44zAe7LJ47nOIcqQqSuortbn7ILYOr5-EKbnfH3JTzEUVGKrpJfucCZnmQKxZr7uaIyp9-QCVo98O0cAzRv5RotRhLDotq"
            }
    )
    @POST("fcm/send")
    Call<MyReponse> sendNotification(@Body Sender body);
}
