package com.bmacode17.androideatitserver.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;
import com.bmacode17.androideatitserver.models.Category;
import com.bmacode17.androideatitserver.models.Food;
import com.bmacode17.androideatitserver.viewHolders.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class FoodList extends AppCompatActivity {

    private static final String TAG = "Basel";
    FirebaseDatabase database;
    DatabaseReference table_food;
    RecyclerView recyclerView_foodList;
    RecyclerView.LayoutManager layoutManager;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    FirebaseStorage storage;
    StorageReference storageReference;
    MaterialEditText editText_foodName , editText_foodDescription , editText_foodPrice , editText_foodDiscount;
    FButton button_select , button_upload;
    AlertDialog addNewFoodDialog;
    Food newFood;
    Uri saveUri;
    private final int PICK_IMAGE_REQUEST = 1;
    ProgressBar myProgressBar;
    AlertDialog alertDialog;
    RelativeLayout relativeLayout_activityFoodList;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        relativeLayout_activityFoodList = (RelativeLayout) findViewById(R.id.relativeLayout_activityFoodList);

        // Init firebase
        database = FirebaseDatabase.getInstance();
        table_food = database.getReference("food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewFoodDialog();
            }
        });

        // Load menu
        // Use firebase UI to bind data from Firebase to Recycler view
        recyclerView_foodList = (RecyclerView) findViewById(R.id.recyclerView_foodList);
        recyclerView_foodList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_foodList.setLayoutManager(layoutManager);

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("categoryId");
        if(! categoryId.isEmpty() && categoryId !=null)
            loadFoodList(categoryId);
    }

    private void loadFoodList(String categoryId) {

        // Create query by categoryId
        Query query = table_food.orderByChild("menuId").equalTo(categoryId);  // like select * from foods where MenuId = categoryId

        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(query,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {

                holder.textView_foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.imageView_foodImage);
                final Food clickedItem = model;

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_food_list,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_foodList.setAdapter(adapter);
        // pc_0 - Using an unspecified index.
        // Consider adding '".indexOn": "menuId"' at food to your security and Firebase Database rules for better performance
        // do it in the firebase console website in Rules
    }

    public void openAlertDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.myprogressdialog, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setMessage("");
        myAlertDialog.setCancelable(true);
        myProgressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        alertDialog = myAlertDialog.create();
        alertDialog.show();

        try {
            Thread.sleep(2000);
        } catch (Exception e){}
    }

    private void openAddNewFoodDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_food, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Add new food");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        editText_foodName = (MaterialEditText) dialogView.findViewById(R.id.editText_foodName);
        editText_foodDescription = (MaterialEditText) dialogView.findViewById(R.id.editText_foodDescription);
        editText_foodPrice = (MaterialEditText) dialogView.findViewById(R.id.editText_foodPrice);
        editText_foodDiscount = (MaterialEditText) dialogView.findViewById(R.id.editText_foodDiscount);
        button_select = (FButton) dialogView.findViewById(R.id.button_select);
        button_upload = (FButton) dialogView.findViewById(R.id.button_upload);

        button_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();  // User select image from Gallary and save image's Uri
            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myAlertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newFood != null){
                    table_food.push().setValue(newFood);
                    Snackbar.make(relativeLayout_activityFoodList,"New food " + newFood.getName() + " was added ",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        addNewFoodDialog = myAlertDialog.create();
        addNewFoodDialog.show();
    }

    private void openUpdateFoodDialog(final String key, final Food item) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_food, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Update food");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        editText_foodName = (MaterialEditText) dialogView.findViewById(R.id.editText_foodName);
        editText_foodDescription = (MaterialEditText) dialogView.findViewById(R.id.editText_foodDescription);
        editText_foodPrice = (MaterialEditText) dialogView.findViewById(R.id.editText_foodPrice);
        editText_foodDiscount = (MaterialEditText) dialogView.findViewById(R.id.editText_foodDiscount);
        button_select = (FButton) dialogView.findViewById(R.id.button_select);
        button_upload = (FButton) dialogView.findViewById(R.id.button_upload);

        // set default name
        editText_foodName.setText(item.getName());
        editText_foodDescription.setText(item.getDescription());
        editText_foodPrice.setText(item.getPrice());
        editText_foodDiscount.setText(item.getDiscount());

        button_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();  // User select image from Gallary and save image's Uri
            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        myAlertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // update information
                item.setName(editText_foodName.getText().toString());
                item.setDescription(editText_foodDescription.getText().toString());
                item.setPrice(editText_foodPrice.getText().toString());
                item.setDiscount(editText_foodDiscount.getText().toString());
                table_food.child(key).setValue(item);
            }
        });

        addNewFoodDialog = myAlertDialog.create();
        addNewFoodDialog.show();
    }

    private void changeImage(final Food item) {

        if(saveUri != null){

            openAlertDialog();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            alertDialog.dismiss();
                            Toast.makeText(FoodList.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
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

                            alertDialog.dismiss();
                            Toast.makeText(FoodList.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            alertDialog.setMessage("Uploaded " + (int) progress + " %");
                        }
                    });
        }
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK  &&  data !=null && data.getData() != null){

            saveUri = data.getData();
            Toast.makeText(this, "Image is selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {

        if(saveUri != null){

            openAlertDialog();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            alertDialog.dismiss();
                            Toast.makeText(FoodList.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    // Set value for the new food if the image is uploaded and we can get a download link
                                    newFood = new Food(editText_foodName.getText().toString(),
                                            uri.toString(),
                                            editText_foodDescription.getText().toString(),
                                            editText_foodPrice.getText().toString(),
                                            editText_foodDiscount.getText().toString(),
                                            categoryId);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            alertDialog.dismiss();
                            Toast.makeText(FoodList.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            alertDialog.setMessage("Uploaded " + (int) progress + " %");
                        }
                    });
        }
    }

    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){

            openUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){

            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {

        table_food.child(key).removeValue();
        Toast.makeText(this, "Food is deleted ! ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter != null)
            adapter.stopListening();
    }
}
