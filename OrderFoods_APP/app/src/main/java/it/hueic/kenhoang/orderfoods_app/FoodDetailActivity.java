package it.hueic.kenhoang.orderfoods_app;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.Arrays;
import java.util.Calendar;

import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.database.Database;
import it.hueic.kenhoang.orderfoods_app.model.Food;
import it.hueic.kenhoang.orderfoods_app.model.Order;
import it.hueic.kenhoang.orderfoods_app.model.Rating;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener {
    TextView food_name, food_price, food_description;
    KenBurnsView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;
    String foodId = "";

    DatabaseReference mDataFood;
    DatabaseReference mDataRating;

    Food currentFood;
    //Show comment
    Button btnShowComment;
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
        setContentView(R.layout.activity_food_detail);
        //InitFireBase
        mDataFood = FirebaseDatabase.getInstance().getReference("Foods");
        mDataRating = FirebaseDatabase.getInstance().getReference("Rating");
        //InitView
        initView();
        //Get food id from intent
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet(getBaseContext())) {
                getDetailFood(foodId);
                getRatingFood(foodId);
            }
            else {
                MDToast.makeText(FoodDetailActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                return;
            }
        }
        //InitEvent
        initEvent();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getRatingFood(String foodId) {
        Query foodRating = mDataRating.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(Math.round(average));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDetailFood(String foodId) {
        mDataFood.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                //Set image
                Picasso.get()
                        .load(currentFood.getImage())
                        .into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initEvent() {
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new Database(getBaseContext()).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            foodId,
                            currentFood.getName(),
                            numberButton.getNumber(),
                            currentFood.getPrice(),
                            currentFood.getDiscount(),
                            currentFood.getImage()
                    ));
                Snackbar.make(findViewById(R.id.coordinatorMain), "Added to cart ...", Toast.LENGTH_SHORT).show();
                btnCart.setCount(new Database(FoodDetailActivity.this).getCountCart(Common.currentUser.getPhone()));
            }
        });

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });

        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(FoodDetailActivity.this, CommentActivity.class);
                commentIntent.putExtra(Common.INTENT_FOOD_ID, foodId);
                startActivity(commentIntent);
            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite Ok", "Very Good", "Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetailActivity.this)
                .show();
    }

    private void initView() {
        numberButton        = findViewById(R.id.number_button);
        btnCart             = findViewById(R.id.btnCart);
        btnRating           = findViewById(R.id.btnRating);
        ratingBar           = findViewById(R.id.ratingBar);
        btnShowComment      = findViewById(R.id.btnShowComment);

        food_description    = findViewById(R.id.food_description);
        food_name           = findViewById(R.id.food_name);
        food_price          = findViewById(R.id.food_price);
        food_image          = findViewById(R.id.img_food);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //Get Rating  and upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments,
                String.valueOf(Calendar.getInstance().getTimeInMillis()));
        final String key = Common.currentUser.getPhone() + "food" + foodId;
        /*mDataRating.child(Common.currentUser.getPhone()).child(key)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    //Create new value
                if (!dataSnapshot.child(key).exists()) {
                    mDataRating.child(Common.currentUser.getPhone()).child(key).setValue(rating);
                } else {
                    mDataRating.child(Common.currentUser.getPhone()).child(key).removeValue();
                    mDataRating.child(Common.currentUser.getPhone()).child(key).setValue(rating);
                }
                MDToast.makeText(FoodDetailActivity.this, "Thanks you for submit rating !!!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
        mDataRating.child(key).setValue(rating);
        MDToast.makeText(FoodDetailActivity.this, "Thanks you for submit rating !!!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
        getRatingFood(foodId);
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCart.setCount(new Database(FoodDetailActivity.this).getCountCart(Common.currentUser.getPhone()));
    }
}
