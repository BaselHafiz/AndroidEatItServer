package com.bmacode17.androideatitserver.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;
import com.bmacode17.androideatitserver.models.Category;

import com.bmacode17.androideatitserver.models.Food;
import com.bmacode17.androideatitserver.models.Token;
import com.bmacode17.androideatitserver.viewHolders.FoodViewHolder;
import com.bmacode17.androideatitserver.viewHolders.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.UUID;

import info.hoang8f.widget.FButton;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Basel";
    FirebaseDatabase database;
    DatabaseReference table_category , table_food;
    FirebaseStorage storage;
    StorageReference storageReference;
    TextView textView_fullName;
    MaterialEditText editText_categoryName;
    FButton button_select , button_upload;
    RecyclerView recyclerView_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    AlertDialog addNewCategoryDialog;
    Category newCategory;
    Uri saveUri;
    private final int PICK_IMAGE_REQUEST = 1;
    ProgressBar myProgressBar;
    AlertDialog alertDialog;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        // Init firebase
        database = FirebaseDatabase.getInstance();
        table_category = database.getReference("category");
        table_food = database.getReference("food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewCategoryDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set Name for user
        View headerView = navigationView.getHeaderView(0);
        textView_fullName = (TextView) headerView.findViewById(R.id.textView_fullName);
        textView_fullName.setText(Common.currentUser.getName());

        // Load menu
        // Use firebase UI to bind data from Firebase to Recycler view
        recyclerView_menu = (RecyclerView) findViewById(R.id.recyclerView_menu);
        recyclerView_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_menu.setLayoutManager(layoutManager);

        loadMenu();

        // Send token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference table_token = db.getReference("token");
        Token data = new Token(token , true);  // True because this token is sent from the server
        table_token.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void openAddNewCategoryDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_category, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Add new category");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        editText_categoryName = (MaterialEditText) dialogView.findViewById(R.id.editText_categoryName);
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
                if(newCategory != null){
                    table_category.push().setValue(newCategory);
                    Snackbar.make(drawer,"New category " + newCategory.getName() + " was added",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        addNewCategoryDialog = myAlertDialog.create();
        addNewCategoryDialog.show();
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
                            Toast.makeText(Home.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Set value for the new category if the image is uploaded and we can get a download link
                                    newCategory = new Category(editText_categoryName.getText().toString() , uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            alertDialog.dismiss();
                            Toast.makeText(Home.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK  &&  data !=null && data.getData() != null){

            saveUri = data.getData();
            Toast.makeText(this, "Image is selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(table_category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {

                holder.textView_menuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.imageView_menuImage);
                final Category clickedItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        // Get categoryID and send it to new activity
                        Intent foodListIntent = new Intent(Home.this,FoodList.class);
                        // CategoryId is a key , so we just get the key of the clicked item
                        foodListIntent.putExtra("categoryId" , adapter.getRef(position).getKey());
                        startActivity(foodListIntent);
                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cardview_menu_item,parent,false);
                return new MenuViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView_menu.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {

            //Intent cartIntent = new Intent(Home.this , Cart.class);
            //startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {

            Intent orderStatusIntent = new Intent(Home.this , OrderStatus.class);
            startActivity(orderStatusIntent);

        } else if (id == R.id.nav_logout) {

            //Intent signInIntent = new Intent(Home.this , SignIn.class);
            //signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //startActivity(signInIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){

            openUpdateCategoryDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){

            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {

        // also , we need to delete all foods in the category
        Query foodInCategory = table_food.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot item:dataSnapshot.getChildren())
                    item.getRef().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        table_category.child(key).removeValue();
        Toast.makeText(this, "Category is deleted ! ", Toast.LENGTH_SHORT).show();
    }

    private void openUpdateCategoryDialog(final String key, final Category item) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.cardview_add_new_category, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setTitle("Update category");
        myAlertDialog.setMessage("Please fill full information");
        myAlertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        editText_categoryName = (MaterialEditText) dialogView.findViewById(R.id.editText_categoryName);
        button_select = (FButton) dialogView.findViewById(R.id.button_select);
        button_upload = (FButton) dialogView.findViewById(R.id.button_upload);

        // set default name
        editText_categoryName.setText(item.getName());

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
                item.setName(editText_categoryName.getText().toString());
                table_category.child(key).setValue(item);
            }
        });

        addNewCategoryDialog = myAlertDialog.create();
        addNewCategoryDialog.show();
    }

    private void changeImage(final Category item) {

        if(saveUri != null){

            openAlertDialog();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            alertDialog.dismiss();
                            Toast.makeText(Home.this, "Image is uploaded", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Home.this, " " + e.getMessage() , Toast.LENGTH_SHORT).show();
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

        loadMenu();
    }
}
