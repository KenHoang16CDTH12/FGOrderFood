package it.hueic.kenhoang.orderfoods_app;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import it.hueic.kenhoang.orderfoods_app.adapter.ViewHolder.CommentViewHolder;
import it.hueic.kenhoang.orderfoods_app.common.Common;
import it.hueic.kenhoang.orderfoods_app.model.Rating;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CommentActivity extends AppCompatActivity {
    RecyclerView recycler_comment;
    RecyclerView.LayoutManager layoutManager;

    DatabaseReference mRatingDB;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseRecyclerAdapter<Rating, CommentViewHolder> adapter;

    TextView tvNumberRate;
    RatingBar ratingBar;

    String foodId = "";
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
        setContentView(R.layout.activity_comment);
        //InitFireBase
        mRatingDB = FirebaseDatabase.getInstance().getReference("Rating");
        //InitView
        initView();
        //InitEvent
        initEvent();
    }

    private void getRatingFood(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Float.parseFloat(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                    tvNumberRate.setText(String.valueOf(count));
                } else {
                    ratingBar.setRating(0);
                    tvNumberRate.setText("0");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initEvent() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                checkloadComment();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkloadComment();
            }
        });
    }

    private void checkloadComment() {
        if (getIntent() != null) foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
        if (!foodId.isEmpty() && foodId != null) {
            //Create request query
            Query query = mRatingDB.orderByChild("foodId").equalTo(foodId);
            getRatingFood(query);
            loadComment(query, foodId);
        }
    }

    private void loadComment(Query query, String foodId) {
        FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                .setQuery(query, Rating.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Rating, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Rating model) {
                holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                holder.tvUserPhone.setText(model.getUserPhone());
                holder.tvComment.setText(model.getComment());

                String timeStamp = "";
                Calendar calendar = Calendar.getInstance();
                long tsPost = Long.parseLong(model.getTimestamp())/1000;
                long tsReply = calendar.getTimeInMillis()/1000;
                long distance = tsReply - tsPost;
                //Log.d("DISTANCE", String.valueOf(distance));
                calendar.setTimeInMillis(Long.parseLong(model.getTimestamp()));
                int mYear   = calendar.get(Calendar.YEAR);
                int mMonth  = calendar.get(Calendar.MONTH);
                int mDay    = calendar.get(Calendar.DAY_OF_MONTH);
                int mHour   = calendar.get(Calendar.HOUR_OF_DAY);
                int mMinute = calendar.get(Calendar.MINUTE);
                String time = (mHour < 10 ? "0" + mHour : mHour) +
                        ":" +
                        (mMinute < 10 ? "0" + mMinute : mMinute);
                String timeFull = (mDay < 10 ? "0" + mDay : mDay) +
                        "/" +
                        (mMonth < 10 ? "0" + (mMonth + 1) : (mMonth + 1)) +
                        "/" +
                        mYear +
                        " " +
                        (mHour < 10 ? "0" + mHour : mHour) +
                        ":" +
                        (mMinute < 10 ? "0" + mMinute : mMinute);
                timeStamp   = Common.parseTime(distance, time, timeFull);

                holder.timeComment.setText(timeStamp);
            }
            @Override
            public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_comment, parent, false);
                return new CommentViewHolder(itemView);
            }
        };

        loadCommentStatus();
    }

    private void loadCommentStatus() {
        adapter.startListening();
        recycler_comment.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initView() {
        recycler_comment = findViewById(R.id.recycler_comment);
        layoutManager = new LinearLayoutManager(this);
        recycler_comment.setHasFixedSize(true);
        recycler_comment.setLayoutManager(layoutManager);
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        ratingBar = findViewById(R.id.ratingBar);
        tvNumberRate = findViewById(R.id.numberRate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
