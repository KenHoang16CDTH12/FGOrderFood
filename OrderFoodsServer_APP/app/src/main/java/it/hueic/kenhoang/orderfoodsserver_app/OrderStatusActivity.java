package it.hueic.kenhoang.orderfoodsserver_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.valdesekamdem.library.mdtoast.MDToast;

import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.MyReponse;
import it.hueic.kenhoang.orderfoodsserver_app.model.NotificationModel;
import it.hueic.kenhoang.orderfoodsserver_app.model.Request;
import it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder.OrderViewHolder;
import it.hueic.kenhoang.orderfoodsserver_app.model.Sender;
import it.hueic.kenhoang.orderfoodsserver_app.model.Token;
import it.hueic.kenhoang.orderfoodsserver_app.remote.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatusActivity extends AppCompatActivity {
    private TextView tvTitle;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MaterialSpinner spinner;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    DatabaseReference mDataRequest;
    APIService mService;
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
        setContentView(R.layout.activity_order_status);
        //InitService
        mService = Common.getFCMClient();
        //InitFireBase
        mDataRequest = FirebaseDatabase.getInstance().getReference("Requests");
        //InitView
        initView();
        //load data
        loadOrders();//load all orders
    }

    /**
     * Load Orders (Adapter + Recycle_Order)
     */
    private void loadOrders() {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(mDataRequest , Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText("#" + adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef(position).getKey(),
                                adapter.getItem(position));
                    }
                });
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });
                viewHolder.btnDiretion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Open TrackingOrder (Google Map)
                        Intent trackingOrderIntent = new Intent(OrderStatusActivity.this, TrackingOrderActivity.class);
                        Common.currentRequest      = model;
                        startActivity(trackingOrderIntent);
                    }
                });
                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Open OrderDetailActivity
                        Intent orderDetailIntent = new Intent(OrderStatusActivity.this, OrderDetailActivity.class);
                        Common.currentRequest      = model;
                        orderDetailIntent.putExtra("OrderId", adapter.getRef(position).getKey());
                        startActivity(orderDetailIntent);
                    }
                });
            }

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_order, parent, false);
                return new OrderViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


    /**
     * Init view (findViewById, setup Toolbar, setup RecyclerView)
     */
    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle         = findViewById(R.id.tvTitle);
        tvTitle.setText("Order status");//Set title toolbar
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_order);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }


    /**
     * Dialog delete order (ref: onContextItemSelected -> DELETE)
     * @param key
     */
    private void deleteOrder(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatusActivity.this);
        alertDialog.setTitle("Confirm remove");
        alertDialog.setMessage("Do you want remove request?");
        alertDialog.setIcon(R.drawable.ic_delete_forever_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Remove
                mDataRequest.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(R.id.relativeOrder), "Item removed!!!", Snackbar.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        } else {
                            Snackbar.make(findViewById(R.id.relativeOrder), "Error", Snackbar.LENGTH_SHORT).show();
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

    /**
     * Dialog update order (ref: onContextItemSelected -> UPDATE)
     * @param key
     */
    private void showUpdateDialog(final String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatusActivity.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_update_order, null);
        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On my way", "Shipped");

        alertDialog.setView(view);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                mDataRequest.child(key).setValue(item);
                adapter.notifyDataSetChanged();
                //Notification
                sendOrderStatusToUser(key, item);
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

    private void sendOrderStatusToUser(final String key, final Request item) {
        DatabaseReference mTokenData = FirebaseDatabase.getInstance().getReference("Tokens");
        mTokenData.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class);

                            //Make raw payload
                            NotificationModel notificationModel = new NotificationModel("Ken Hoang", "Your order #" + key + " was updated '" + Common.convertCodeToStatus(item.getStatus()) + "'");
                            Sender content = new Sender(token.getToken(), notificationModel);

                            mService.sendNotification(content)
                                    .enqueue(new Callback<MyReponse>() {
                                        @Override
                                        public void onResponse(Call<MyReponse> call, Response<MyReponse> response) {
                                            if (response.code() == 200) {
                                                if (response.body().sucess == 0) {
                                                    MDToast.makeText(OrderStatusActivity.this, "Order was updated!", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS).show();
                                                } else {
                                                    MDToast.makeText(OrderStatusActivity.this, "Order was updated but failed to send notification! ", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyReponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
