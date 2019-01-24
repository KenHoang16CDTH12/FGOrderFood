package it.hueic.kenhoang.orderfoodsserver_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.hueic.kenhoang.orderfoodsserver_app.Interface.ItemClickListener;
import it.hueic.kenhoang.orderfoodsserver_app.common.Common;
import it.hueic.kenhoang.orderfoodsserver_app.model.Food;
import it.hueic.kenhoang.orderfoodsserver_app.adapter.viewholder.FoodVIewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodListActivity extends AppCompatActivity {
    private static final String TAG = FoodListActivity.class.getSimpleName();
    private RecyclerView recycler_food;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fab;
    private MaterialEditText edName, edDescription, edPrice, edDiscount;
    private Button btnSelect, btnUpload;
    private Food newFood;
    private MaterialSearchBar materialSearchBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    //Data
    DatabaseReference mFoodData;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodVIewHolder> adapter;
    //Storage Reference
    private StorageReference mStorageImage;
    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodVIewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    private Uri saveUri;
    private boolean isStatusSearch = false;
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
        setContentView(R.layout.activity_food_list);
        //InitFireBase
        mFoodData   = FirebaseDatabase.getInstance().getReference().child("Foods");
        mStorageImage = FirebaseStorage.getInstance().getReference();
        //InitView
        initView();
        //InitEvent
        initEvent();
        //Get Intent here
        if (getIntent() != null) categoryId = getIntent().getStringExtra("CategoryId");

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

    private void checkLoadFoodSwipe() {
        if (!categoryId.isEmpty() && categoryId != null) {
            //Check connect internet
            if (Common.isConnectedToInternet(getBaseContext())) loadListFood(categoryId);
            else {
                MDToast.makeText(FoodListActivity.this, "Please check your connection ...", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
                return;
            }
        }
    }

    private void initEvent() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFoodDialog();
            }
        });
    }

    private void showAddFoodDialog() {
        //Copy code from HomeActivity
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Add new food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialog_add_food = inflater.inflate(R.layout.dialog_add_new_food, null);

        edName = dialog_add_food.findViewById(R.id.edName);
        edDescription = dialog_add_food.findViewById(R.id.edDescription);
        edPrice = dialog_add_food.findViewById(R.id.edPrice);
        edDiscount = dialog_add_food.findViewById(R.id.edDiscount);

        btnSelect = dialog_add_food.findViewById(R.id.btnSelect);
        btnUpload = dialog_add_food.findViewById(R.id.btnUpload);
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
        alertDialog.setView(dialog_add_food);
        alertDialog.setIcon(R.drawable.ic_restaurant_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Here, just create new category
                if (newFood != null) {
                    mFoodData.push().setValue(newFood).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(findViewById(R.id.framFoodList), "New food " + newFood.getName() + " was added...", Snackbar.LENGTH_SHORT).show();
                                loadSuggest();
                            } else {
                                Snackbar.make(findViewById(R.id.framFoodList), "ERROR", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
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

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading ...");
            mDialog.show();
            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = mStorageImage.child("images/" + imgName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    newFood = new Food();
                                    newFood.setName(edName.getText().toString().toUpperCase());
                                    newFood.setDescription(edDescription.getText().toString());
                                    newFood.setPrice(edPrice.getText().toString());
                                    newFood.setDiscount(edDiscount.getText().toString());
                                    newFood.setMenuId(categoryId);
                                    newFood.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
     * OnActivityResult
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
                if (!TextUtils.isEmpty(text)){
                    isStatusSearch = true;
                    startSearch(text);
                }
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

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodVIewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodVIewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.tvFoodName.setText(model.getName());
                viewHolder.tvFoodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.get()
                        .load(model.getImage())
                        .into(viewHolder.imgFood);
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Code late
                    }
                });
            }

            @Override
            public FoodVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food_ref_menu, parent, false);
                return new FoodVIewHolder(itemView);
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
        adapter = new FirebaseRecyclerAdapter<Food, FoodVIewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodVIewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.tvFoodName.setText(model.getName());
                viewHolder.tvFoodPrice.setText(String.format("$ %s", model.getPrice().toString()));
                Picasso.get()
                        .load(model.getImage())
                        .into(viewHolder.imgFood);
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        //Code late
                    }
                });
            }

            @Override
            public FoodVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_food_ref_menu, parent, false);

                return new FoodVIewHolder(itemView);
            }
        };

        adapter.startListening();
        //Set Adapter
        recycler_food.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void initView() {
        recycler_food   = findViewById(R.id.recycler_food);
        fab             = findViewById(R.id.fab);
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case Common.UPDATE:
                if (!isStatusSearch)
                    showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),
                            adapter.getItem(item.getOrder()));
                else
                    showUpdateDialog(searchAdapter.getRef(item.getOrder()).getKey(),
                            searchAdapter.getItem(item.getOrder()));
                break;
            case Common.DELETE:
                if (!isStatusSearch) deleteDialog(adapter.getRef(item.getOrder()).getKey());
                else deleteDialog(searchAdapter.getRef(item.getOrder()).getKey());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteDialog(final String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Confirm remove");
        alertDialog.setMessage("Do you want remove category?");
        alertDialog.setIcon(R.drawable.ic_delete_forever_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Remove
                mFoodData.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadListFood(categoryId);
                            loadSuggest();
                            Snackbar.make(findViewById(R.id.framFoodList), "Item removed!!!", Snackbar.LENGTH_SHORT).show();
                            materialSearchBar.disableSearch();
                        } else {
                            Snackbar.make(findViewById(R.id.framFoodList), "Error", Snackbar.LENGTH_SHORT).show();
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

    private void showUpdateDialog(final String key, final Food item) {
        //Just copy showDialog add and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Update food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialog_add_food = inflater.inflate(R.layout.dialog_add_new_food, null);

        edName = dialog_add_food.findViewById(R.id.edName);
        edDescription = dialog_add_food.findViewById(R.id.edDescription);
        edPrice = dialog_add_food.findViewById(R.id.edPrice);
        edDiscount = dialog_add_food.findViewById(R.id.edDiscount);

        btnSelect = dialog_add_food.findViewById(R.id.btnSelect);
        btnUpload = dialog_add_food.findViewById(R.id.btnUpload);

        //Set default
        edName.setText(item.getName());
        edDescription.setText(item.getDescription());
        edPrice.setText(item.getPrice());
        edDiscount.setText(item.getDiscount());
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
        alertDialog.setView(dialog_add_food);
        alertDialog.setIcon(R.drawable.ic_restaurant_black_24dp);
        //Set button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //Update information
                    item.setName(edName.getText().toString().toUpperCase());
                    item.setDescription(edDescription.getText().toString());
                    item.setPrice(edPrice.getText().toString());
                    item.setDiscount(edDiscount.getText().toString());
                    item.setMenuId(categoryId);
                    mFoodData.child(key).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (isStatusSearch) startSearch(item.getName());
                                loadSuggest();
                                Snackbar.make(findViewById(R.id.framFoodList), "Update success!!!", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.framFoodList), "Update failed!!!", Snackbar.LENGTH_SHORT).show();
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

    private void changeImage(final Food item) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading ...");
            mDialog.show();
            String imgName = UUID.randomUUID().toString();
            final StorageReference imageFolder = mStorageImage.child("images/" + imgName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(FoodListActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
