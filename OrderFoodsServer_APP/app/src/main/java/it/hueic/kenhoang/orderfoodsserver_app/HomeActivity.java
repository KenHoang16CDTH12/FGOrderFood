package it.hueic.kenhoang.orderfoodsserver_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import it.hueic.kenhoang.orderfoodsserver_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoodsserver_app.adapter.CustomSliderAdapter;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.Banner;
import it.hueic.kenhoang.orderfoodsserver_app.model.Category;
import it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder.MenuViewHolder;
import it.hueic.kenhoang.orderfoodsserver_app.model.Token;
import it.hueic.kenhoang.orderfoodsserver_app.service.PicassoImageLoadingService;
import ss.com.bannerslider.Slider;
import ss.com.bannerslider.event.OnSlideClickListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //View
    TextView tvFullName, tvTitle;
    SwipeRefreshLayout swipeRefreshLayout;
    //Handle Data
    private DatabaseReference mCategoryData;
    private RecyclerView recycler_menu;
    private RecyclerView.LayoutManager mLayoutManger;
    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private StorageReference mStorage;
    private Category newCategory;
    private Uri saveUri;
    //Dialog components
    MaterialEditText edName;
    Button btnSelect, btnUpload;
    //Menu Item
    private boolean statusItemList = false;
    Menu menu;
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
        tvTitle.setText("Menu Management");
        setSupportActionBar(toolbar);
        //Init FireBase
        mCategoryData   = FirebaseDatabase.getInstance().getReference("Category");
        mStorage        = FirebaseStorage.getInstance().getReference();
        //Create adapter
        createAdapter();
        //Init Paper
        Paper.init(this);
        //Floating Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

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

        //Send token
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
                        Toast.makeText(HomeActivity.this, "Click", Toast.LENGTH_SHORT).show();
                        //We will send menu Id to food List Activity
                        Intent bannerIntent = new Intent(HomeActivity.this, BannerActivity.class);
                        startActivity(bannerIntent);
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

    private void updateToken(String token) {
            DatabaseReference tokenDB = FirebaseDatabase.getInstance().getReference("Tokens");
            Token data = new Token(token, true); //true because this token send from Server app
            tokenDB.child(Common.currentUser.getPhone()).setValue(data);
    }

    /**
     * Show dialog add new category
     */
    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Add new category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialog_add_menu = inflater.inflate(R.layout.dialog_add_new_menu, null);

        edName = dialog_add_menu.findViewById(R.id.edName);
        btnSelect = dialog_add_menu.findViewById(R.id.btnSelect);
        btnUpload = dialog_add_menu.findViewById(R.id.btnUpload);
        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //Let user select image from Gallery and save Uri of this image
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        alertDialog.setView(dialog_add_menu);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Here, just create new category
                if (newCategory != null) {
                    mCategoryData.push().setValue(newCategory);
                    Snackbar.make(findViewById(R.id.drawer_layout), "New category " + newCategory.getName() + " was added...", Snackbar.LENGTH_SHORT).show();
                }
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

    /**
     * Upload Image from device to StorageFireBase
     */
    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading ...");
            mDialog.show();
            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = mStorage.child("images/" + imgName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    newCategory = new Category(edName.getText().toString().toUpperCase(), uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()
                                                                / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " + progress + "%");
                        }
                    });

        }
    }

    /**
     * onActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Selected");
        }
    }

    private void chooseImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
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
                        Intent foodListIntent = new Intent(HomeActivity.this, FoodListActivity.class);
                        //Because CategoryId is key, so we just get key of this item
                        foodListIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
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
        if (id == R.id.action_view) {
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
            case R.id.nav_banner:
                Intent bannerIntent = new Intent(HomeActivity.this, BannerActivity.class);
                startActivity(bannerIntent);
                break;
            case R.id.nav_message:
                Intent notificationIntent = new Intent(HomeActivity.this, NotificationManagerActivity.class);
                startActivity(notificationIntent);
                break;
            case R.id.nav_menu:
                break;
            case R.id.nav_cart:
                break;
            case R.id.nav_orders:
                Intent ordersIntent = new Intent(HomeActivity.this, OrderStatusActivity.class);
                startActivity(ordersIntent);
                break;
            case R.id.nav_change_pass:
                //Change password
                showChangePasswordDialog();
                break;
            case R.id.nav_sign_out:
                //Remove remember user & password
                Paper.book().destroy();
                //Logout
                Intent signInIntent = new Intent(HomeActivity.this, SignInActivity.class);
                signInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signInIntent);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case Common.UPDATE:
                showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),
                        adapter.getItem(item.getOrder()));
                break;
            case Common.DELETE:
                deleteDialog(adapter.getRef(item.getOrder()).getKey());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteDialog(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Confirm remove");
        alertDialog.setMessage("Do you want remove category?");
        alertDialog.setIcon(R.drawable.ic_delete_forever_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //First, we need get all food in category
                DatabaseReference foods = FirebaseDatabase.getInstance().getReference("Foods");
                Query foodInCategory = foods.orderByChild("menuId").equalTo(key);
                foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //Remove category
                mCategoryData.child(key).removeValue();
                Snackbar.make(findViewById(R.id.drawer_layout), "Item removed!!!", Snackbar.LENGTH_SHORT).show();
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

    private void showUpdateDialog(final String key, final Category item) {
        //Just copy showDialog add and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("Update category");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialog_add_menu = inflater.inflate(R.layout.dialog_add_new_menu, null);

        edName = dialog_add_menu.findViewById(R.id.edName);
        btnSelect = dialog_add_menu.findViewById(R.id.btnSelect);
        btnUpload = dialog_add_menu.findViewById(R.id.btnUpload);
        //Set default name
        edName.setText(item.getName());
        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage(); //Let user select image from Gallery and save Uri of this image
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });
        alertDialog.setView(dialog_add_menu);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Update information
                item.setName(edName.getText().toString().toUpperCase());
                mCategoryData.child(key).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(R.id.drawer_layout), "Update success!!!", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(R.id.drawer_layout), "Update failed!!!", Snackbar.LENGTH_SHORT).show();
                        }
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

    private void changeImage(final Category item) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading ...");
            mDialog.show();
            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = mStorage.child("images/" + imgName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " + progress + "%");
                        }
                    });

        }
    }

    /**
     * Change password dialog
     */
    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(HomeActivity.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information.");
        alertDialog.setIcon(R.drawable.ic_security_black_24dp);
        View dialog_change_password = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        alertDialog.setView(dialog_change_password);

        final MaterialEditText edPass = dialog_change_password.findViewById(R.id.edPass);
        final MaterialEditText edNewPass = dialog_change_password.findViewById(R.id.edNewPass);
        final MaterialEditText edRepeatPass = dialog_change_password.findViewById(R.id.edRepeatPass);
        //Set font
        edPass.setTypeface(Common.setNabiLaFont(this));
        edNewPass.setTypeface(Common.setNabiLaFont(this));
        edRepeatPass.setTypeface(Common.setNabiLaFont(this));
        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Change password here

                //For use SpotsDialog, please use AlertDialog From android.app, not from v7 like above AlertDialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(HomeActivity.this).build();
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
