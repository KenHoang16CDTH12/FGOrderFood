package it.hueic.kenhoang.orderfoodsserver_app.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.hueic.kenhoang.orderfoodsserver_app.model.Request;
import it.hueic.kenhoang.orderfoodsserver_app.model.User;
import it.hueic.kenhoang.orderfoodsserver_app.remote.APIService;
import it.hueic.kenhoang.orderfoodsserver_app.remote.FCMRetrofitClient;
import it.hueic.kenhoang.orderfoodsserver_app.remote.IGeoCoordinates;
import it.hueic.kenhoang.orderfoodsserver_app.remote.RetrofitClient;

/**
 * Created by kenhoang on 29/01/2018.
 */

public class Common {
    public static User currentUser;
    public static Request currentRequest;
    public static String topicName = "News";
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final int PICK_IMAGE_REQUEST = 71;
    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String INTENT_MENU_ID = "CategoryId";
    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";

    /**
     * get Status request
     * @param status
     * @return
     */
    public static String convertCodeToStatus(String status) {
        String result = "";
        switch (status) {
            case "0":
                result = "Placed";
                break;
            case "1":
                result = "On my way";
                break;
            case "2":
                result = "Shipped";
                break;
            default:
                result = "Error";
                break;
        }
        return result;
    }

    /**
     * Get geo service
     * @return
     */
    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMClient() {
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    /**
     * Scale Bitmap use draw box on map
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaleBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth /(float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaleBitmap;
    }

    /**
     * Check connect internet (connected == true)
     * @param context
     * @return
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) return true;
                }
            }
        }
        return false;
    }

    /**
     * Set font nabila
     * @param activity
     * @return
     */
    public static Typeface setNabiLaFont(Activity activity) {
        Typeface face = Typeface.createFromAsset(activity.getAssets(), "fonts/NABILA.TTF");
        return face;
    }
}
