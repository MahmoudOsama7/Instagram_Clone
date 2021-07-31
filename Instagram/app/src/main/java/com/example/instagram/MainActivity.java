package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import Fragments.HomeFragment;
import Fragments.NotificationFragment;
import Fragments.ProfileFragment;
import Fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    FirebaseAuth mauth;
    private Fragment selectorFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mauth=FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId())  // menuitem id is the id for each botton of the bottons in the bottonnavigationview like home and add and search
                {
                    case R.id.nav_home :
                        selectorFragment = new HomeFragment();
                        break;
                    case R.id.nav_search :
                        selectorFragment = new SearchFragment();
                        break;
                    case R.id.nav_add :
                        selectorFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;
                    case R.id.nav_heart :
                        selectorFragment = new NotificationFragment();
                        break;
                    case R.id.nav_profile :
                        selectorFragment = new ProfileFragment();
                        break;
                }
                if(selectorFragment!=null)
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container , selectorFragment).commit();
                }
                return true;
            }
        });

        Bundle intent = getIntent().getExtras();
        if(intent!=null)
        {
            String profileId =intent.getString("PublisherId");
            getSharedPreferences("PROFILE",MODE_PRIVATE).edit().putString("profileId",profileId).apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
        else
        {
            // when loop is broken when one of the switch cases is selected so the loop will break so the next step to be executed is this step
            // as to return to home fragment after finishing everything , this also happened as true value when no icon is pressed in bottomnavigationview created
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container , new HomeFragment()).commit();
        }
    }

}