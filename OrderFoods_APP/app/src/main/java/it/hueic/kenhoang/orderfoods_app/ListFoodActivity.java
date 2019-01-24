package it.hueic.kenhoang.orderfoods_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.FoodViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Food;
import it.hueic.kenhoang.orderfoods_app.model.Order;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ListFoodActivity extends AppCompatActivity {
    private static final String TAG = ListFoodActivity.class.getSimpleName();
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference mFoodData;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            } else {
                MDToast.makeText(ListFoodActivity.this, "Please install facebbok app ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };
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
        setContentView(R.layout.activity_list_food);
        //InitFacebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        //InitFireBase
        mFoodData   = FirebaseDatabase.getInstance().getReference().child("Foods");
        //Local DB
        localDB = new Database(this);
        //InitView
        initView();
        //Get Intent here
        if (getIntent() != null) categoryId = getIntent().getStringExtra(Common.INTENT_MENU_ID);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkLoadFoodSwipe();
                materialSearchBar.disableSearch();
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                checkLoadFoodSwipe();
            }
        });
        //SearchBar
        handleSearchBar();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void checkLoadFoodSwipe() {
        if (!categoryId.isEmpty() && categoryId != null) {
            //Check connect internet
            if (Common.isConnectedToInternet(getBaseContext())) loadListFood(categoryId);
            else {
                MDToast.makeText(ListFoodActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                return;
            }
        }
    }

    private void handleSearchBar() {
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user type their text, we will change suggest list
                List<String> suggest = new ArrayList<>();
                for(String search: suggestList) { //Loop in suggest List
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original adapter
                if (!enabled) recycler_food.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result of search adapter
                if (!TextUtils.isEmpty(text)) startSearch(text);
                else recycler_food.setAdapter(adapter);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    /**
     * Adapter search
     * @param text
     */
    private void startSearch(CharSequence text) {
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery( mFoodData.orderByChild("name").equalTo(String.valueOf(text)), Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.tvFoodName.setText(model.getName());
                viewHolder.tvFoodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.get()
                        .load(model.getImage())
                        .into(viewHolder.imgFood);
                final Food local = model;
                //Click Quick Cart
                quickCart(searchAdapter, position, viewHolder, model);
                //Click to share
                shareFacebook(searchAdapter, position, viewHolder, model);
                //Add favorite food
                favoriteFood(searchAdapter, position, viewHolder, model);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetailIntent = new Intent(ListFoodActivity.this, FoodDetailActivity.class);
                        foodDetailIntent.putExtra("FoodId", searchAdapter.getRef(position).getKey()); //Send Food Id to new activity
                        startActivity(foodDetailIntent);
                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food_ref_menu, parent, false);
                return new FoodViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recycler_food.setAdapter(searchAdapter);//Set adapter for Recycler View is Search result
    }

    /**
     * Fill data search bar
     */
    private void loadSuggest() {
        mFoodData.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Food item = snapshot.getValue(Food.class);
                            suggestList.add(item.getName());// Add name of food to suggest list
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(mFoodData.orderByChild("menuId").equalTo(categoryId), Food.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.tvFoodName.setText(model.getName());
                viewHolder.tvFoodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.get()
                        .load(model.getImage())
                        .into(viewHolder.imgFood);
                //Click Quick Cart
                quickCart(adapter, position, viewHolder, model);
                //Click to share
                shareFacebook(adapter, position, viewHolder, model);
                //Add favorite food
                favoriteFood(adapter, position, viewHolder, model);
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetailIntent = new Intent(ListFoodActivity.this, FoodDetailActivity.class);
                        foodDetailIntent.putExtra("FoodId", adapter.getRef(position).getKey()); //Send Food Id to new activity
                        startActivity(foodDetailIntent);
                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food_ref_menu, parent, false);

                return new FoodViewHolder(itemView);
            }
        };

        adapter.startListening();
        //Set Adapter
        recycler_food.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void quickCart(final FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter, final int position, FoodViewHolder viewHolder, final Food model) {
            viewHolder.btnQuickCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                    if (!isExists) {
                        new Database(getBaseContext()).addToCart(new Order(
                                Common.currentUser.getPhone(),
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ));
                    } else {
                        new Database(getBaseContext()).inCreaseCart(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                    }
                    Snackbar.make(findViewById(R.id.listfoodMain), "Added to cart ...", Snackbar.LENGTH_SHORT).show();
                }
            });

    }


    private void initView() {
        recycler_food   = findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        mLayoutManager  = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(mLayoutManager);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
    }

    /**
     * Share facebook
     * @param adapter
     * @param position
     * @param viewHolder
     * @param model
     */
    private void shareFacebook(FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter, int position, FoodViewHolder viewHolder, final Food model) {
        viewHolder.imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Click");
                Picasso.get()
                        .load(model.getImage())
                        .into(target);
            }
        });
    }

    /**
     * Favorite
     * @param adapter
     * @param position
     * @param viewHolder
     * @param model
     */
    private void favoriteFood(final FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter, final int position, final FoodViewHolder viewHolder, final Food model) {
        //Add Favorites
        if (localDB.isFavorite(this.adapter.getRef(position).getKey(), Common.currentUser.getPhone()))
            viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
        //Click to change state of Favorites
        viewHolder.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!localDB.isFavorite(ListFoodActivity.this.adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                    localDB.addToFavorites(ListFoodActivity.this.adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                    viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                    Common.showSnackBar("" + model.getName() + " was added to Favorites", ListFoodActivity.this, findViewById(R.id.listfoodMain));
                } else {
                    localDB.removeFromFavorites(ListFoodActivity.this.adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                    viewHolder.imgFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    Common.showSnackBar("" + model.getName() + " was remove from to Favorites", ListFoodActivity.this, findViewById(R.id.listfoodMain));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Fix click back on FoodDetail and get no item in food list
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
