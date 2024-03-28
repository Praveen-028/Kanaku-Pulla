//package com.example.kanakupulla;
//
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.MenuItem;
//import android.widget.Toolbar;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.google.android.material.navigation.NavigationView;
//
//public class dummy extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//    @Override
//    protected void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home_screen);
//        Toolbar toolbar=findViewById(R.id.mytoolbar);
//        toolbar.setTitle("ExpenseManger");
//        setSupportActionBar(toolbar);
//
//
//        DrawerLayout drawerLayout=findViewById(R.id.drawerlayout);
//
//        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(
//                this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawe_close
//        );
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView=findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//    }
//
//    @Override
//    public void onBackPressed() {
//
//        DrawerLayout drawerLayout=findViewById(R.id.drawerlayout);
//        if(drawerLayout.isDrawerOpen(GravityCompat.END)){
//            drawerLayout.closeDrawer(GravityCompat.END);
//        }
//        else {
//            super.onBackPressed();
//        }
//    }
//
//    public void displaySelectedListener(int itemId){
//        Fragment fragment=NULL;
//        switch (itemId)
//        {
//            case R.id.dashbord:
//
//                break;
//
//            case R.id.income:
//
//                break;
//
//            case R.id.expense:
//                break;
//        }
//        if(fragment !=null)
//        {
//            FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
//            ft.replace(R.id.main_frame,fragment);
//            ft.commit();
//        }
//        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);
//        drawerLayout.closeDrawer(GravityCompat.START);
//    }
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        displaySelectedListener(item.getItemId());
//        return true;
//    }
//}