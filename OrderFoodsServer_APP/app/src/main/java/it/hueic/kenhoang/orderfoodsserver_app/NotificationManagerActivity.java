package it.hueic.kenhoang.orderfoodsserver_app;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.valdesekamdem.library.mdtoast.MDToast;

import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.MyReponse;
import it.hueic.kenhoang.orderfoodsserver_app.model.NotificationModel;
import it.hueic.kenhoang.orderfoodsserver_app.model.Sender;
import it.hueic.kenhoang.orderfoodsserver_app.remote.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationManagerActivity extends AppCompatActivity {
    MaterialEditText edMessage, edTitle;
    Button btnSend;

    APIService mService;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Notes : add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/food_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_notification_manager);
        //InitService
        mService = Common.getFCMClient();
        //InitView
        initView();
        //InitEvent
        initEvent();
    }

    private void initEvent() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create message
                NotificationModel notificationModel = new NotificationModel(edTitle.getText().toString(), edMessage.getText().toString());
                Sender toTopic = new Sender();
                toTopic.to = new StringBuilder("/topics/").append(Common.topicName).toString();
                toTopic.notification = notificationModel;
                mService.sendNotification(toTopic)
                        .enqueue(new Callback<MyReponse>() {
                            @Override
                            public void onResponse(Call<MyReponse> call, Response<MyReponse> response) {
                                if (response.isSuccessful())
                                    MDToast.makeText(NotificationManagerActivity.this, "Message sent", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            }

                            @Override
                            public void onFailure(Call<MyReponse> call, Throwable t) {
                                MDToast.makeText(NotificationManagerActivity.this, "Failed " + t.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                            }
                        });
            }
        });
    }

    private void initView() {
        edTitle = findViewById(R.id.edTitle);
        edMessage = findViewById(R.id.edMessage);
        btnSend = findViewById(R.id.btnSend);
    }
}
