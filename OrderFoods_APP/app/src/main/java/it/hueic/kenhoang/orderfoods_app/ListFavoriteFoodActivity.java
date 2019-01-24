package it.hueic.kenhoang.orderfoods_app;

import android.content.Context;
import android.graphics.Color;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import it.hueic.kenhoang.orderfoods_app.Interface.RecyclerItemTouchHelperListtener;
import it.hueic.kenhoang.orderfoods_app.adapter.FavoriteAdapter;
import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.FavoriteViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.helper.RecyclerItemTouchHelperFavorite;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ListFavoriteFoodActivity extends AppCompatActivity implements RecyclerItemTouchHelperListtener {
    private static final String TAG = ListFavoriteFoodActivity.class.getSimpleName();
    private TextView tvTitle;
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    List<String> listIdFood = new ArrayList<>();
    Database localDB;
    FavoriteAdapter adapter;
    DatabaseReference foodData;
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
        setContentView(R.layout.activity_list_favorite_food);
        //InitFirebase
        foodData = FirebaseDatabase.getInstance().getReference("Foods");
        //Local DB
        localDB = new Database(this);
        listIdFood = localDB.getIdFoods();
        //InitView
        initView();
        //Swipe to remove
        ItemTouchHelper.SimpleCallback itemSimpleCallback = new RecyclerItemTouchHelperFavorite(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemSimpleCallback).attachToRecyclerView(recycler_food);
        //Check connect internet
        if (Common.isConnectedToInternet(getBaseContext())) loadListFood(listIdFood);
        else {
            MDToast.makeText(this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void loadListFood(List<String> listIdFood) {
        adapter = new FavoriteAdapter(listIdFood, this);
        recycler_food.setAdapter(adapter);
    }

    /**
     * FindViewByID setup Recycle
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Favorites");
        setSupportActionBar(toolbar);
        recycler_food   = findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        mLayoutManager  = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoriteViewHolder) {
            final String deleteItem = ((FavoriteAdapter) recycler_food.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromFavorites(deleteItem, Common.currentUser.getPhone());
            listIdFood = localDB.getIdFoods();
            //Make SnackBar
            Snackbar snackbar = Snackbar.make(findViewById(R.id.listFavoritefoodMain), "Removed from favorite!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem, Common.currentUser.getPhone());
                    listIdFood = localDB.getIdFoods();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
