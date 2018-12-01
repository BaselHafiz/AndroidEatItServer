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
import com.bmacode17.androideatitserver.models.Banner;
import com.bmacode17.androideatitserver.models.Category;
import com.bmacode17.androideatitserver.models.Food;
import com.bmacode17.androideatitserver.viewHolders.BannerViewHolder;
import com.bmacode17.androideatitserver.viewHolders.FoodViewHolder;
import com.bmacode17.androideatitserver.viewHolders.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    private static final String TAG = "Basel";
    FirebaseDatabase database;
    DatabaseReference table_banner;
    RecyclerView recyclerView_banner;
    RecyclerView.LayoutManager layoutManager;
    String foodId = "";
    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    FirebaseStorage storage;
    StorageReference storageReference;
    MaterialEditText editText_foodNameBanner, editText_foodIdBanner;
    FButton button_selectBanner , button_uploadBanner;
    AlertDialog addNewBannerItemDialog;
    Banner newBannerItem;
    Uri saveUri;
    private final int PICK_IMAGE_REQUEST = 1;
    ProgressBar myProgressBar;
    AlertDialog alertDialog;
    RelativeLayout relativeLayout_activityBanner;
    FloatingActionButton fab_banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        relativeLayout_activityBanner = (RelativeLayout) findViewById(R.id.relativeLayout_activityBanner);

        // Init firebase
        database = FirebaseDatabase.getInstance();
        table_banner = database.getReference("banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fab_banner = (FloatingActionButton) findViewById(R.id.fab_banner);
        fab_banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewBannerItemDialog();
            }
        });

        // Load menu
        // Use firebase UI to bind data from Firebase to Recycler view
        recyclerView_banner = (RecyclerView) findViewById(R.id.recyclerView_banner);
        recyclerView_banner.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_banner.setLayoutManager(layoutManager);

        loadBannerItems();
    }

    private void loadBannerItems() {

        FirebaseRecyclerOptions<Banner> options = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(table_banner,Banner.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {

                holder.textView_bannerName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.imageView_bannerImage);
                final Banner clickedItem = model;
            }

            @Override
            public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_banner_item,parent,false);
                return new BannerViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_banner.setAdapter(adapter);
    }

    private void openAddNewBannerItemDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_banner_item, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Add new banner item");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_laptop_mac_black_24dp);

        editText_foodNameBanner = (MaterialEditText) dialogView.findViewById(R.id.editText_foodNameBanner);
        editText_foodIdBanner = (MaterialEditText) dialogView.findViewById(R.id.editText_foodIdBanner);
        button_selectBanner = (FButton) dialogView.findViewById(R.id.button_selectBanner);
        button_uploadBanner = (FButton) dialogView.findViewById(R.id.button_uploadBanner);

        button_selectBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();  // User select image from Gallary and save image's Uri
            }
        });

        button_uploadBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                newBannerItem = null;
            }
        });

        myAlertDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newBannerItem != null){
                    table_banner.push().setValue(newBannerItem);
                    Snackbar.make(relativeLayout_activityBanner,"New banner item " + newBannerItem.getName() + " was added ",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        loadBannerItems();
        addNewBannerItemDialog = myAlertDialog.create();
        addNewBannerItemDialog.show();
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK  &&  data !=null && data.getData() != null){

            saveUri = data.getData();
            Toast.makeText(this, "Image is selected", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){

            openUpdateBannerItemDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){

            deleteBannerItem(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void openUpdateBannerItemDialog(final String key, final Banner item) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_banner_item, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Update Banner item");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_laptop_mac_black_24dp);
        editText_foodNameBanner = (MaterialEditText) dialogView.findViewById(R.id.editText_foodNameBanner);
        editText_foodIdBanner = (MaterialEditText) dialogView.findViewById(R.id.editText_foodIdBanner);

        button_selectBanner = (FButton) dialogView.findViewById(R.id.button_selectBanner);
        button_uploadBanner = (FButton) dialogView.findViewById(R.id.button_uploadBanner);

        // set default name
        editText_foodNameBanner.setText(item.getName());
        editText_foodIdBanner.setText(item.getId());

        button_selectBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();  // User select image from Gallary and save image's Uri
            }
        });

        button_uploadBanner.setOnClickListener(new View.OnClickListener() {
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

        myAlertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // update information
                item.setName(editText_foodNameBanner.getText().toString());
                item.setId(editText_foodIdBanner.getText().toString());
                table_banner.child(key).setValue(item);
            }
        });

        loadBannerItems();
        addNewBannerItemDialog = myAlertDialog.create();
        addNewBannerItemDialog.show();
    }

    private void deleteBannerItem(String key) {

        table_banner.child(key).removeValue();
        Toast.makeText(this, "Banner item is deleted ! ", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(BannerActivity.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    // Set value for the new banner item if the image is uploaded and we can get a download link
                                    newBannerItem = new Banner();
                                    newBannerItem.setName(editText_foodNameBanner.getText().toString());
                                    newBannerItem.setId(editText_foodIdBanner.getText().toString());
                                    newBannerItem.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            alertDialog.dismiss();
                            Toast.makeText(BannerActivity.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
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

    private void changeImage(final Banner item) {

        if(saveUri != null){

            openAlertDialog();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            alertDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(BannerActivity.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadBannerItems();
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
}
