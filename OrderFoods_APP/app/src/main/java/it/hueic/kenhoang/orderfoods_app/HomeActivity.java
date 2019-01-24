package it.hueic.kenhoang.orderfoods_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;

import com.andremion.counterfab.CounterFab;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.adapter.CustomSliderAdapter;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.MenuViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Banner;
import it.hueic.kenhoang.orderfoods_app.model.Category;
import it.hueic.kenhoang.orderfoods_app.model.Token;
import it.hueic.kenhoang.orderfoods_app.service.PicassoImageLoadingService;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.event.OnSlideClickListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    //View
    TextView tvFullName, tvTitle;
    CounterFab fab;
    private DatabaseReference mCategoryData;
    private RecyclerView recycler_menu;
    private RecyclerView.LayoutManager mLayoutManger;
    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean statusItemList = false;
    Menu menu;

    // Address
    Place homeAddress;
    //Slider
    List<Banner> banners;
    Slider mSlider;
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
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Menu");
        setSupportActionBar(toolbar);
        //Init FireBase
        mCategoryData   = FirebaseDatabase.getInstance().getReference("Category");
        //Create adapter
        createAdapter();
        //Init Paper
        Paper.init(this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set name for user
        View headerView = navigationView.getHeaderView(0);
        tvFullName      = headerView.findViewById(R.id.tvFullName);
        tvFullName.setText(Common.currentUser.getName());
        //Load menu
        recycler_menu    = findViewById(R.id.recycler_menu);
        mLayoutManger   = new LinearLayoutManager(this);
        if (statusItemList) {
            recycler_menu.setHasFixedSize(true);
            recycler_menu.setLayoutManager(mLayoutManger);
        } else {
            recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        }
        //Add animation recyclerview
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);
        //SwipeRefresh Layout
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                checkLoadMenuSwipe();

            }
        });
        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                checkLoadMenuSwipe();
            }
        });

        if (Common.currentUser != null) updateToken(FirebaseInstanceId.getInstance().getToken());

        //Set up Slider
        //Need call this function after you init database firebase
        setupSlider();
    }

    private void setupSlider() {
        mSlider = findViewById(R.id.slider);
        Slider.init(new PicassoImageLoadingService());
        banners = new ArrayList<>();
        final DatabaseReference mBannerDB = FirebaseDatabase.getInstance().getReference("Banner");
        mBannerDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                    Banner banner = postSnapShot.getValue(Banner.class);
                    //We will concat string name and id like
                    //PIZZA_01 => And we will use PIZZA for show description, 01 for food id to click
                    banners.add(banner);
                }
                mSlider.setAdapter(new CustomSliderAdapter(banners));


                mSlider.setOnSlideClickListener(new OnSlideClickListener() {
                    @Override
                    public void onSlideClick(int position) {
                        //We will send menu Id to food List Activity
                        Intent foodIntent = new Intent(HomeActivity.this, FoodDetailActivity.class);
                        foodIntent.putExtra(Common.INTENT_FOOD_ID, banners.get(position).getId());
                        startActivity(foodIntent);
                    }
                });

                //Remove event after finish
                mBannerDB.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkLoadMenuSwipe() {
        //Check connect internet
        if (Common.isConnectedToInternet(this)) loadMenu();
        else {
            MDToast.makeText(HomeActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    private void updateToken(String token) {
            DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("Tokens");
            Token data = new Token(token, false); //false because this token send from Client app
            tokenDB.child(Common.currentUser.getPhone()).setValue(data);
    }

    /**
     * Create adapter
     */

    private void createAdapter() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(mCategoryData, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.tvMenuName.setText(model.getName());
                Picasso.get()
                        .load(model.getImage())
                        .into(viewHolder.imgMenu);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get CategoryId and send to new Activity
                        Intent foodListIntent = new Intent(HomeActivity.this, ListFoodActivity.class);
                        //Because CategoryId is key, so we just get key of this item
                        foodListIntent.putExtra(Common.INTENT_MENU_ID, adapter.getRef(position).getKey());
                        startActivity(foodListIntent);
                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(statusItemList ? R.layout.item_category_menu : R.layout.item_category_menu_grid, parent, false);
                return new MenuViewHolder(itemView);
            }
        };
    }
    /**
     * Load Menu
     */
    private void loadMenu() {
        adapter.startListening();
        //Animation
        recycler_menu.scheduleLayoutAnimation();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_view:
                statusItemList = !statusItemList;
                if (statusItemList) {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.icon_view_list));
                    recycler_menu.setHasFixedSize(true);
                    recycler_menu.setLayoutManager(mLayoutManger);
                } else {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.icon_view_grid));
                    recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
                }
                checkLoadMenuSwipe();
                break;
            case R.id.action_search:
                startActivity(new Intent(HomeActivity.this, SearchFoodActivity.class));
                break;
        }
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_menu:
                checkLoadMenuSwipe();
                break;
            case R.id.nav_update_name:
                showChangeNameDialog();
                break;
            case R.id.nav_home_address:
                //Update home address function
                showHomeAddressDialog();
                break;
            case R.id.nav_fav:
                startActivity(new Intent(HomeActivity.this, ListFavoriteFoodActivity.class));
                break;
            case R.id.nav_cart:
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
                break;
            case R.id.nav_orders:
                startActivity(new Intent(HomeActivity.this, OrderStatusActivity.class));
                break;
            case R.id.nav_setting:
                showDialogSetting();
                break;
            /*case R.id.nav_change_pass:
                //Change password
                showChangePasswordDialog();
                break;*/
            case R.id.nav_log_out:
                //Remove remember user & password
                //Paper.book().destroy();
                //Logout
                AccountKit.logOut();
                Intent mainIntent = new Intent(HomeActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogSetting() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setIcon(R.drawable.ic_settings_black_24dp);
        View dialog_setting = getLayoutInflater().inflate(R.layout.dialog_setting, null);
        alertDialog.setView(dialog_setting);
        final CheckBox chkSubNews = dialog_setting.findViewById(R.id.chkSubNew);
        //Add code remember state of checkbox
        Paper.init(this);
        String isSubscribe = Paper.book().read("sub_new");
        if(isSubscribe == null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false")) {
            chkSubNews.setChecked(false);
        } else {
            chkSubNews.setChecked(true);
        }
        //Button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             dialogInterface.dismiss();
             if (chkSubNews.isChecked()) {
                 FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                 //Write value
                 Paper.book().write("sub_new", "true");
             } else {
                 FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                 //Write value
                 Paper.book().write("sub_new", "false");
             }



            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Change Name Dialog
     */
    private void showChangeNameDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("UPDATE NAME");
        alertDialog.setMessage("Please fill all information.");
        alertDialog.setIcon(R.drawable.ic_contacts_black_24dp);
        View dialog_change_name = getLayoutInflater().inflate(R.layout.dialog_update_name, null);
        alertDialog.setView(dialog_change_name);
        final MaterialEditText edName = dialog_change_name.findViewById(R.id.edName);
        edName.setTypeface(Common.setNabiLaFont(this));

        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Change name here

                //For use SpotsDialog, please use AlertDialog From android.app, not from v7 like above AlertDialog
                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(HomeActivity.this).build();
                waitingDialog.show();

                //Update Name
                final Map<String, Object> update_name = new HashMap<>();
                update_name.put("name", edName.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //DIsmiss dialog
                                waitingDialog.dismiss();
                                if (task.isSuccessful()) {
                                    MDToast.makeText(HomeActivity.this, "Name was updated!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                    tvFullName.setText(update_name.get("name").toString());
                                }
                            }
                        });

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Home Address Dialog
     */
    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("UPDATE ADDRESS");
        alertDialog.setMessage("Please fill all information.");
        alertDialog.setIcon(R.drawable.ic_home_black_24dp);
        View dialog_home_address = getLayoutInflater().inflate(R.layout.dialog_update_address_home, null);
        alertDialog.setView(dialog_home_address);
        final PlaceAutocompleteFragment edAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edAddress.getView().findViewById(R.id.place_autocomplete_search_button)
                .setVisibility(View.GONE);
        //Set hint for autocomplete edit text
        ((EditText) edAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your home address");
        ((EditText) edAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);
        // Get address from places autocomplete
        edAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                homeAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: " + status.getStatusMessage());
            }
        });
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Set new Home Address
                Common.currentUser.setHomeAddress(homeAddress.getAddress().toString());
                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                MDToast.makeText(HomeActivity.this, "Update Address successful", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                            }
                        });
                dialog.dismiss();
                //Fix crash fragment
                removeFragmentToFix();
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Fix crash fragment
                removeFragmentToFix();
            }
        });

        alertDialog.show();//Don't forget it :)))
    }

    /**
     * Remove fragment after dialog dismiss
     */
    private void removeFragmentToFix() {
        //Fix crash fragment
        //Remove fragment
        getFragmentManager().beginTransaction()
                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                .commit();
    }

    /**
     * Change password dialog
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information.");
        alertDialog.setIcon(R.drawable.ic_security_black_24dp);
        View dialog_change_password = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        alertDialog.setView(dialog_change_password);

        final MaterialEditText edPass = dialog_change_password.findViewById(R.id.edPass);
        final MaterialEditText edNewPass = dialog_change_password.findViewById(R.id.edNewPass);
        final MaterialEditText edRepeatPass = dialog_change_password.findViewById(R.id.edRepeatPass);
        edPass.setTypeface(Common.setNabiLaFont(this));
        edNewPass.setTypeface(Common.setNabiLaFont(this));
        edRepeatPass.setTypeface(Common.setNabiLaFont(this));

        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Change password here

                //For use SpotsDialog, please use AlertDialog From android.app, not from v7 like above AlertDialog
                final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(HomeActivity.this).build();
                waitingDialog.show();

                //Check old password
                if (edPass.getText().toString().equals(Common.currentUser.getPassword())) {
                    //Check new password and repeat password
                    if (edNewPass.getText().toString().equals(edRepeatPass.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", edNewPass.getText().toString());
                        //Make update
                        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference("User");
                        mUserDB.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        MDToast.makeText(HomeActivity.this, "Password was update ", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        waitingDialog.dismiss();
                                        MDToast.makeText(HomeActivity.this, "ERROR " + e.getMessage(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                                    }
                                });
                    } else {
                        waitingDialog.dismiss();
                        MDToast.makeText(HomeActivity.this, "New password doesn't match! ", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    }
                } else {
                    waitingDialog.dismiss();
                    MDToast.makeText(HomeActivity.this, "Wrong old password", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                }
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
