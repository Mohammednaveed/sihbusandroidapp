package com.example.finalbustraking;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class home extends AppCompatActivity {
    private View underline;
    private TextView textView1, textView2, textView3;
    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private int currentX = 0; // Store the current X position of the underline
    private List<String> suggestions = new ArrayList<>(); // Declare suggestions as a class-level variable

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (!SharedPreferencesHelper.isLoggedIn(this)) {
            // User is not logged in, redirect to the login screen
            Intent intent = new Intent(home.this, login.class);
            startActivity(intent);
            finish();
        }
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        underline = findViewById(R.id.underline_view);
        textView1 = findViewById(R.id.spot_btn);
        textView2 = findViewById(R.id.bus_stop_btn);
        textView3 = findViewById(R.id.pass_btn);
        NavigationView navigationView = findViewById(R.id.nav_view);
        AutoCompleteTextView sourceAutoCompleteTextView = findViewById(R.id.sourceEditText);
        AutoCompleteTextView destinationAutoCompleteTextView = findViewById(R.id.destinationEditText);
        Button searchButton = findViewById(R.id.searchButton);


        // Create an array to hold the suggestions
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        sourceAutoCompleteTextView.setAdapter(sourceAdapter);
        destinationAutoCompleteTextView.setAdapter(destinationAdapter);



// Initialize the underline to the initial position (textView1)

        underline.setTranslationX(currentX);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int itemId = item.getItemId();
                if (itemId == R.id.logout) {
                    // Show a confirmation dialog before logging out
                    AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                    builder.setTitle("Logout");
                    builder.setMessage("Are you sure you want to logout?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferencesHelper.setLoggedIn(home.this, false);

                            Intent intent = new Intent(home.this, login.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing, just close the dialog
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();


            } else if (itemId == R.id.Shared_app) {// Handle the click on the second item
                    Toast.makeText(home.this, "hello", Toast.LENGTH_SHORT).show();
                    // Add cases for other menu items as needed
                }

                // Close the drawer after handling the item click
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
           // Not needed

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the navigation drawer when the menu_icon is clicked
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUnderline(textView1);
            }
        });

        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUnderline(textView2);
            }
        });

        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUnderline(textView3);
            }
        });
        sourceAutoCompleteTextView.setThreshold(0); // Set the threshold to 0 to show suggestions immediately
        destinationAutoCompleteTextView.setThreshold(0);

        sourceAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceAutoCompleteTextView.showDropDown();
            }
        });

        destinationAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationAutoCompleteTextView.showDropDown();
            }
        });

// Limit the number of suggestions shown in the drop-down list
        sourceAutoCompleteTextView.setDropDownHeight(400); // Adjust the height as needed
        destinationAutoCompleteTextView.setDropDownHeight(400);

        sourceAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSource = sourceAdapter.getItem(position);
                // Handle the selected source
            }
        });

        destinationAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDestination = destinationAdapter.getItem(position);
                // Handle the selected destination
            }
        });


    }

    private void fetchSuggestionsFromFirestore(String query, ArrayAdapter<String> adapter) {
        // Check if the query has at least three characters
        if (query.length() >= 3) {
            // Replace 'journeyDetails' with the name of your Firestore collection
            db.collection("journeyDetails")
                    .whereGreaterThanOrEqualTo("source", query)
                    .whereLessThanOrEqualTo("source", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        suggestions.clear(); // Clear previous suggestions
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String source = document.getString("source");
                            suggestions.add(source);
                        }
                        adapter.notifyDataSetChanged(); // Notify the adapter about the new data
                    })
                    .addOnFailureListener(e -> {
                        Timber.tag("Firestore").d(e, "Error getting source suggestions");
                    });

            // You can also fetch the "destination" in a similar way.
            db.collection("journeyDetails")
                    .whereGreaterThanOrEqualTo("destination", query)
                    .whereLessThanOrEqualTo("destination", query + "\uf8ff")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String destination = document.getString("destination");
                            suggestions.add(destination);
                        }
                        adapter.notifyDataSetChanged(); // Notify the adapter about the new data
                    })
                    .addOnFailureListener(e -> {
                        Timber.tag("Firestore").d(e, "Error getting destination suggestions");
                    });
        } else {
            // Clear suggestions if the query does not have at least three characters
            suggestions.clear();
            adapter.notifyDataSetChanged();
        }
    }



    private void moveUnderline(final View targetView) {
        int startX = currentX; // Start from the current X position
        int endX = targetView.getLeft() + (targetView.getWidth() / 2) - (underline.getWidth() / 2);

        ValueAnimator animator = ValueAnimator.ofInt(startX, endX);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                underline.setTranslationX(value);
                currentX = value; // Update the current X position
            }
        });

        animator.setDuration(200);
        animator.start();
    }
}