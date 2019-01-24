package it.hueic.kenhoang.orderfoods_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 7171;
    private Button mBtnContinue;
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
        //Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);
        //printKeyHash();
        //FireBase Init
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        //InitViews
        initViews();
        //InitEvents
        initEvents();
        //Check session facebook account kit
        if (AccountKit.getCurrentAccessToken() != null) {
            //Create dialog
            final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
            waitingDialog.show();
            waitingDialog.setMessage("Please wait");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    //Copy code from exists user
                    //We will just login
                    //Login
                    mDataUser.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User localUser = dataSnapshot.getValue(User.class);
                                    //Copy code from LoginActivity
                                    Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);
                                    //Dismiss dialog
                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }
        /* Old code
        //Init Paper
        Paper.init(this);
        //CheckRemember
        checkRemember();
        */
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("it.hueic.kenhoang.orderfoods_app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature: info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", "printKeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void initEvents() {
      mBtnContinue.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
               startLoginSystem();
          }
      });
    }

    /**
     * Account Kit facebbok login
     */
    private void startLoginSystem() {
        Intent intentAccount = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intentAccount.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intentAccount, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null) {
                MDToast.makeText(this, "" + result.getError().getErrorType().getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                return;
            } else if(result.wasCancelled()) {
                MDToast.makeText(this, "Cancel", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                return;
            } else {
                if (result.getAccessToken() != null) {
                    //Show dialog
                    final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                    waitingDialog.show();
                    waitingDialog.setMessage("Please wait");
                    waitingDialog.setCancelable(false);

                    //Get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone = account.getPhoneNumber().toString();
                            //Check if exists on Firebase Users
                            mDataUser.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userPhone).exists()) {
                                                //If not exists
                                                //We will create new user and login
                                                User newUser = new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("Anonymous " + userPhone);
                                                newUser.setBalance("0.0");
                                                //Add to FireBase
                                                mDataUser.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    MDToast.makeText(MainActivity.this, "User register successful !", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

                                                                    //Login
                                                                    mDataUser.child(userPhone)
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    User localUser = dataSnapshot.getValue(User.class);
                                                                                    //Copy code from LoginActivity
                                                                                    Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                                                                                    Common.currentUser = localUser;
                                                                                    startActivity(homeIntent);
                                                                                    //Dismiss dialog
                                                                                    waitingDialog.dismiss();
                                                                                    finish();
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                } else {
                                                                    MDToast.makeText(MainActivity.this, "Register failed!", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                                                                }
                                                            }
                                                        });


                                            } else { //If exists
                                                //We will just login
                                                //Login
                                                mDataUser.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                //Copy code from LoginActivity
                                                                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                //Dismiss dialog
                                                                waitingDialog.dismiss();
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            MDToast.makeText(MainActivity.this, "" + accountKitError.getErrorType().getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                        }
                    });
                }
            }
        }
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
            final ProgressDialog mProgressbar = new ProgressDialog(MainActivity.this);
            mProgressbar.setMessage("Logging ...");
            mProgressbar.show();
            mDataUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        //Get User information
                        mProgressbar.dismiss();
                        User user = dataSnapshot
                                .child(phone)
                                .getValue(User.class);
                        if (user.getPassword().equals(pwd)) {
                            user.setPhone(phone);//Set phone
                            Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Snackbar.make(findViewById(R.id.relSignInMain), "Wrong password ...", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        mProgressbar.dismiss();
                        Snackbar.make(findViewById(R.id.relSignInMain), "User not exists database ...", Snackbar.LENGTH_SHORT).show();
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

    private void initViews() {
        tvSlogan   = findViewById(R.id.tvSlogan);
        mBtnContinue = findViewById(R.id.btnContinued);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        tvSlogan.setTypeface(face);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
