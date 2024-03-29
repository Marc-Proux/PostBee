package com.example.mobile_app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.mobile_app.R;
import com.example.mobile_app.model.Token;
import com.example.mobile_app.model.UserStatic;
import com.example.mobile_app.model.item_post.ItemPost;
import com.example.mobile_app.model.item_post.ItemPostAdapter;
import com.example.mobile_app.model.RecyclerViewInterface;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements RecyclerViewInterface {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private List<ItemPost> posts = new ArrayList<ItemPost>();
    private int mPostStatus = 0;
    private Button mAddPostButton;
    private Button mProfileButton;
    private Button mModerationButton;
    private Button mSettingsButton;
    private String mTokenAccess = UserStatic.access;
    private int amount = 7;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mRecyclerView = findViewById(R.id.home_recyclerview_posts);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    receiveHomePage(amount);
                }
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.home_swiperefreshlayout_s2r);
        mAddPostButton = findViewById(R.id.home_menu_button_addpost);
        mProfileButton = findViewById(R.id.home_menu_button_profile);
        mModerationButton = findViewById(R.id.home_menu_button_moderation);
        mSettingsButton = findViewById(R.id.home_menu_button_settings);

        mModerationButton.setVisibility(UserStatic.getIs_staff().equals("true") ? View.VISIBLE : View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new ItemPostAdapter(posts, getApplicationContext(), this));

        mAddPostButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EditPostActivity.class)));
        mProfileButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        mModerationButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ModerationActivity.class)));
        mSettingsButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));

        receiveHomePage(amount);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            posts.clear();
            mRecyclerView.getAdapter().notifyDataSetChanged();
            receiveHomePage(amount);
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent homeActivityIntent = new Intent(HomeActivity.this, ViewPostActivity.class);
        homeActivityIntent.putExtra("ID", posts.get(position).getId());
        homeActivityIntent.putExtra("TITLE", posts.get(position).getTitle());
        homeActivityIntent.putExtra("AUTHOR", posts.get(position).getAuthor().getFull_name());
        homeActivityIntent.putExtra("DATE", posts.get(position).getDate());
        homeActivityIntent.putExtra("STATUS", mPostStatus);
        startActivity(homeActivityIntent);
    }

    @Override
    public void onBackPressed() {
    }

    public static List<ItemPost> convertObjectToList(Object obj) {
        if (obj != null) {
            List<ItemPost> list = new ArrayList<>();
            if (obj.getClass().isArray())
                list = Arrays.asList((ItemPost[]) obj);
            else if (obj instanceof Collection)
                list = new ArrayList<>((Collection<ItemPost>) obj);
            return list;
        }else
            return null;
    }

    public void receiveHomePage(int amount) {
        isLoading = true;
        new Thread(new Runnable() {
            public void run() {
                try {
                    Type type = new TypeToken<List<ItemPost>>(){}.getType();
                    String endUrl = "posts/?amount=" + amount + "&start=" + posts.size();
                    final List<ItemPost> receivedPosts = convertObjectToList(Token.connectToServer(endUrl,"GET", UserStatic.getAccess(),null,null,null,type));
                    // Changement des dates
                    for (ItemPost itemPost : receivedPosts){
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            String rawStringDate = itemPost.getDate();
                            Log.d("HomeActivity","rawStringDate : "+rawStringDate);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                            LocalDateTime date = LocalDateTime.parse(rawStringDate, formatter);
                            Log.d("HomeActivity","Conversion de la date : "+date.toString());
                            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
                            Log.d("HomeActivity","Conversion de la date : "+date.format(formatter2).toString());
                            itemPost.setDate(date.format(formatter2).toString());
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            posts.addAll(receivedPosts);
                            mRecyclerView.getAdapter().notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                } catch (Exception e) {
                    Log.e("HomeActivity", "Erreur dans HomeActivity", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });isLoading = false;
                }
            }
        }).start();
    }
}