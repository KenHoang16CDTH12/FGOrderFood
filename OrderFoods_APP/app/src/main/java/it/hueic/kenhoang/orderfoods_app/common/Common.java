package it.hueic.kenhoang.orderfoods_app.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import it.hueic.kenhoang.orderfoods_app.model.User;
import it.hueic.kenhoang.orderfoods_app.remote.APIService;
import it.hueic.kenhoang.orderfoods_app.remote.IGeoCoordinates;
import it.hueic.kenhoang.orderfoods_app.remote.IGoogleService;
import it.hueic.kenhoang.orderfoods_app.remote.RetrofitClient;
import it.hueic.kenhoang.orderfoods_app.remote.RetrofitGeoClient;
import it.hueic.kenhoang.orderfoods_app.remote.RetrofitGoogleClient;

/**
 * Created by kenhoang on 26/01/2018.
 */

public class Common {
    public static User currentUser;

    public static String topicName = "News";

    public static String nameProduct = "";
    //Default String
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    public static final String REQUEST_PHONE_USER = "userPhone";
    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String INTENT_MENU_ID = "CategoryId";
    //Api url
    private static final String BASE_URL = "https://fcm.googleapis.com/";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapService() {
        return RetrofitGoogleClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    /**
     * Get geo service
     * @return
     */
    public static IGeoCoordinates getGeoCodeService() {
        return RetrofitGeoClient.getClient(GOOGLE_API_URL).create(IGeoCoordinates.class);
    }

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

    //This function will convert currency to number base on locale
    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat)format).setParseBigDecimal(true);
        return (BigDecimal) format.parse(amount.replace("[^\\d.,]", ""));
    }

    /**
     * Parse Time same facebook
     * @param distance
     * @param time
     * @param timeFull
     * @return
     */
    public static String parseTime(long distance, String time, String timeFull) {
        String result = "";
        if (distance < 60) result = (distance == 1) ? distance + " second ago." : distance + " seconds ago.";
        else if (distance >= 60 && distance < 3600) {
            int minute = Math.round(distance/60);
            result = (minute == 1) ? minute + " minute ago." : minute + " minutes ago.";
        }
        else if (distance >= 3600 && distance < 86400) {
            int hour = Math.round(distance/3600);
            result = (hour == 1) ? hour + " hour ago." : hour + " hours ago.";
        }
        else if (Math.round(distance/86400) == 1) {
            result = "Yesterday at " + time;
        }
        else {
            result = timeFull;
        }

        return result;
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

    public static void showSnackBar(String msg, Activity context, View view){
        Snackbar.make(context.findViewById(view.getId()), msg, Toast.LENGTH_SHORT)
                .show();
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
