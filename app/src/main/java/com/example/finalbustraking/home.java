package com.example.finalbustraking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class home extends AppCompatActivity {
    private View underline;
    private TextView textView1, textView2, textView3;
    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private int currentX = 0; // Store the current X position of the underline

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