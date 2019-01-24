package it.hueic.kenhoang.orderfoods_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.valdesekamdem.library.mdtoast.MDToast;

import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends AppCompatActivity {
    private MaterialEditText edPhone, edPass;
    private ProgressDialog mProgressbar;
    private Button btnSignIn;
    private DatabaseReference mDataUser;
    private CheckBox chkRemember;
    private TextView txtForgotPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //Notes : add this code before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NABILA.TTF")
                .setFontAttrId(R.attr.fontPath)
                .build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //InitViews
        initViews();
        //Init Paper
        Paper.init(this);
        //InitFireBase
        mDataUser = FirebaseDatabase.getInstance().getReference("User");
        //InitEvents
        initEvents();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    //Save user & password
                    if (chkRemember.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edPass.getText().toString());
                    }
                    mProgressbar.setMessage("Logging ...");
                    mProgressbar.show();
                    mDataUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if user not exist in database
                            if (dataSnapshot.child(edPhone.getText().toString()).exists()) {
                                //Get User information
                                mProgressbar.dismiss();
                                User user = dataSnapshot
                                        .child(edPhone.getText().toString())
                                        .getValue(User.class);
                                if (user.getPassword().equals(edPass.getText().toString())) {
                                    user.setPhone(edPhone.getText().toString());//Set phone
                                    Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                                    Common.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();
                                    mDataUser.removeEventListener(this);
                                } else {
                                    Snackbar.make(findViewById(R.id.relSignInMain), "Wrong password ...", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mProgressbar.dismiss();
                                Snackbar.make(findViewById(R.id.relSignInMain), "User not exists database ...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    MDToast.makeText(SignInActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    return;
                }
            }
        });

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPwdDialog();
            }
        });
    }

    /**
     * Forgot password dialog
     */
    private void showForgotPwdDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Forgot Password");
        alertDialog.setMessage("Enter your secure code");
        alertDialog.setIcon(R.drawable.ic_security_black_24dp);

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.dialog_forgot_password, null);
        alertDialog.setView(forgot_view);

        final MaterialEditText edPhone = forgot_view.findViewById(R.id.edPhone);
        final MaterialEditText edScureCode = forgot_view.findViewById(R.id.edSecureCode);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Check if user available
                mDataUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(edPhone.getText().toString()).exists()) {
                            User user = dataSnapshot.child(edPhone.getText().toString())
                                    .getValue(User.class);
                            if (user.getSecureCode().equals(edScureCode.getText().toString())) {
                                MDToast.makeText(getBaseContext(), "Your password: " + user.getPassword(), MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                            } else {
                                MDToast.makeText(getBaseContext(), "Wrong secure code !", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                            }
                        } else {
                            Snackbar.make(findViewById(R.id.relSignInMain), "Sorry, User not exist ...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();

    }

    private void initViews() {
        edPhone         = findViewById(R.id.edPhone);
        edPass          = findViewById(R.id.edPass);
        btnSignIn       = findViewById(R.id.btnSignIn);
        chkRemember     = findViewById(R.id.chkRemember);
        txtForgotPwd    = findViewById(R.id.txtForgotPwd);
        mProgressbar    = new ProgressDialog(this);
    }
}
