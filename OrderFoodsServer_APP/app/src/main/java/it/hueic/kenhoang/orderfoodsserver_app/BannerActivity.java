package it.hueic.kenhoang.orderfoodsserver_app;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder.BannerViewHolder;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.Banner;
import it.hueic.kenhoang.orderfoodsserver_app.model.Category;
import it.hueic.kenhoang.orderfoodsserver_app.model.Food;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BannerActivity extends AppCompatActivity {
    private static final String TAG = BannerActivity.class.getSimpleName();
    private RecyclerView recycler_banner;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fab;
    DatabaseReference mBannerDB;
    private SwipeRefreshLayout swipeRefreshLayout;
    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;
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
        setContentView(R.layout.activity_banner);
        //Init Banner
        mBannerDB = FirebaseDatabase.getInstance().getReference("Banner");
        //Init View
        initView();
        //Init Events
        initEvent();
    }

    private void initEvent() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                checkLoadBanner();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkLoadBanner();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateUpdateDialog("", null, true);
            }
        });
    }

    private void checkLoadBanner() {
        //Check connect internet
        if (Common.isConnectedToInternet(this)) loadBanner();
        else {
            MDToast.makeText(BannerActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }

    private void loadBanner() {
        FirebaseRecyclerOptions<Banner> options = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(mBannerDB, Banner.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.tvBannerName.setText(model.getName());
                Picasso.get()
                        .load(model.getImage())
                        .into(holder.imgBanner);
            }

            @Override
            public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_banner, parent, false);
                return new BannerViewHolder(itemView);
            }
        };

        adapter.startListening();
        //Set Adapter
        recycler_banner.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initView() {
        recycler_banner = findViewById(R.id.recycler_banner);
        mLayoutManager = new LinearLayoutManager(this);
        recycler_banner.setHasFixedSize(true);
        recycler_banner.setLayoutManager(mLayoutManager);
        fab = findViewById(R.id.fab);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case Common.UPDATE:
                showCreateUpdateDialog(adapter.getRef(item.getOrder()).getKey(),
                        adapter.getItem(item.getOrder()), false);
                break;
            case Common.DELETE:
                deleteDialog(adapter.getRef(item.getOrder()).getKey());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showCreateUpdateDialog(final String key, final Banner item, final boolean status) {
        //Just copy showDialog add and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Update Banner");
        alertDialog.setMessage("Please fill full information");
        alertDialog.setIcon(R.drawable.ic_desktop_windows_black_24dp);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialog_update_banner = inflater.inflate(R.layout.dialog_update_banner, null);
        alertDialog.setView(dialog_update_banner);
        //InitView
        MaterialSpinner spCategory = dialog_update_banner.findViewById(R.id.categorySpinner);
        MaterialSpinner spFood = dialog_update_banner.findViewById(R.id.foodSpinner);
        ImageView imgFood = dialog_update_banner.findViewById(R.id.imgFood);
        if (!status) {
            Picasso.get()
                    .load(item.getImage())
                    .into(imgFood);
        }
        //Fill spinner
        final Banner banner = fillSpinner(spCategory, spFood, imgFood, item, status);
        alertDialog.setPositiveButton(status ? "CREATE" : "UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!status) {
                    if (banner != null) {
                        if (banner.getName() != null && banner.getId() != null && banner.getImage() != null) {
                            mBannerDB.child(key).setValue(banner).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Snackbar.make(findViewById(R.id.framBannerList), "Update Banner " + banner.getName() + " was added...", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                Snackbar.make(findViewById(R.id.framBannerList), "ERROR", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            MDToast.makeText(getBaseContext(), "No change!", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                        }
                    } else {
                        MDToast.makeText(getBaseContext(), "Please fill full information!", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                    }
                } else {
                    if (banner != null) {
                        if (banner.getName() != null && banner.getId() != null && banner.getImage() != null) {
                            mBannerDB.push().setValue(banner).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Snackbar.make(findViewById(R.id.framBannerList), "Create banner " + banner.getName() + " was added...", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                Snackbar.make(findViewById(R.id.framBannerList), "ERROR", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            MDToast.makeText(getBaseContext(), "No change!", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                        }
                    }
                }
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();


    }

    private Banner fillSpinner(final MaterialSpinner spCategory, final MaterialSpinner spFood, final ImageView imgFood, final Banner item, final boolean status) {
        final Banner model = new Banner();
        final ArrayList<String> listCategory = new ArrayList<>();
        final DatabaseReference mCategoryDB = FirebaseDatabase.getInstance().getReference("Category");
        final DatabaseReference mFoodDb = FirebaseDatabase.getInstance().getReference("Foods");
        final List<String> listFood = new ArrayList<>();
        //Default selected update
            mCategoryDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                        Category category = postSnapShot.getValue(Category.class);
                        listCategory.add(postSnapShot.getKey() + "@" + category.getName());
                        spCategory.setItems(listCategory);
                        if (!status) {
                            if (!listCategory.isEmpty()) {
                                int index = 0;
                                for (int i = 0; i < listCategory.size(); i++) {
                                    String[] map = listCategory.get(i).split("@");
                                    if (item.getMenuId().equals(map[0])) {
                                        index = i;
                                        break;
                                    }
                                }
                                //Default selected
                                spCategory.setSelectedIndex(index);
                                mFoodDb.orderByChild("menuId").equalTo(item.getMenuId())
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                listFood.clear();
                                                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                                                    Food food = postSnapShot.getValue(Food.class);
                                                    listFood.add(postSnapShot.getKey() + "@" + food.getName());
                                                }
                                                spFood.setItems(listFood);
                                                if (!listFood.isEmpty()) {
                                                    int indexFood = 0;
                                                    for (int i = 0; i < listFood.size(); i++) {
                                                        String[] map = listFood.get(i).split("@");
                                                        if (item.getId().equals(map[0])) {
                                                            indexFood = i;
                                                            break;
                                                        }
                                                    }
                                                    //Default selected
                                                    spFood.setSelectedIndex(indexFood);
                                                }
                                                mCategoryDB.removeEventListener(this);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        spCategory.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (!listCategory.isEmpty()) {
                    String[] arr = listCategory.get(position).split("@");
                    String key = arr[0];
                        mFoodDb.orderByChild("menuId").equalTo(key)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        listFood.clear();
                                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                                            Food food = postSnapShot.getValue(Food.class);
                                            listFood.add(postSnapShot.getKey() + "@" + food.getName());
                                        }
                                        spFood.setItems(listFood);
                                        if (!listFood.isEmpty()) spFood.setSelectedIndex(0);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
        });

        spFood.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (!listFood.isEmpty()) {
                    String[] arr = listFood.get(position).split("@");
                    final String key = arr[0];
                    mFoodDb.orderByKey().equalTo(key)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                                        Food food = postSnapShot.getValue(Food.class);
                                        model.setId(key);
                                        model.setImage(food.getImage());
                                        model.setName(food.getName());
                                        model.setMenuId(food.getMenuId());
                                        Picasso.get()
                                                .load(model.getImage())
                                                .into(imgFood);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });
        return model;
    }

    private void deleteDialog(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Confirm remove");
        alertDialog.setMessage("Do you want remove category?");
        alertDialog.setIcon(R.drawable.ic_delete_forever_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Remove
                mBannerDB.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(R.id.framBannerList), "Banner removed!!!", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(findViewById(R.id.framBannerList), "Error", Snackbar.LENGTH_SHORT).show();
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
}
