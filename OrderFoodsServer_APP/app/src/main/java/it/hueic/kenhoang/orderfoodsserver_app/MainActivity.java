package it.hueic.kenhoang.orderfoodsserver_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private Button mBtnSignIn;
    private TextView tvSlogan;
    DatabaseReference mDataUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Notes : add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NABILA.TTF")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);
        //InitViews
        initViews();
        //Init Paper
        Paper.init(this);
        //InitEvents
        initEvents();
        //CheckRemember
        checkRemember();
    }

    private void initEvents() {
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(signInIntent);
            }
        });

    }

    private void initViews() {
        tvSlogan   = findViewById(R.id.tvSlogan);
        mBtnSignIn = findViewById(R.id.btnSignIn);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        tvSlogan.setTypeface(face);
    }

    private void checkRemember() {
        //Check remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);

        if (user != null & pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty()) login(user, pwd);
        }
    }
    /**
     * Auto login
     * @param phone
     * @param pwd
     */
    private void login(final String phone, final String pwd) {
        //Just copy login code from SignInActivity
        //InitFireBase
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        if (Common.isConnectedToInternet(getBaseContext())) {
            //Save user & password
            final ProgressDialog mProgressbar = new ProgressDialog(MainActivity.this);
            mProgressbar.setMessage("Logging ...");
            mProgressbar.show();

            final String localPhone = phone;
            final String localPass = pwd;
            mDataUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(localPhone).exists()) {
                        mProgressbar.dismiss();
                        User user = dataSnapshot.child(localPhone).getValue(User.class);
                        user.setPhone(localPhone);
                        if (Boolean.parseBoolean(user.getIsStaff())) {
                            if (user.getPassword().equals(localPass)) {
                                // Login success
                                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                                Common.currentUser = user;
                                startActivity(homeIntent);

                            } else {
                                // Login failed
                                Snackbar.make(findViewById(R.id.relSignIn), "Wrong password ...", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            //No permission
                            Snackbar.make(findViewById(R.id.relSignIn), "Sorry, User not permission ...", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        //Not exists
                        mProgressbar.dismiss();
                        Snackbar.make(findViewById(R.id.relSignIn), "Sorry, User not exist in database ...", Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            MDToast.makeText(MainActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
