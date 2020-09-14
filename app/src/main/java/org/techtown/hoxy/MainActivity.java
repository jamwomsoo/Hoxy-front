package org.techtown.hoxy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.helper.log.Logger;


import org.techtown.hoxy.community.PostListActivity;
import org.techtown.hoxy.login.LoginActivity;
import org.techtown.hoxy.waste.MypageActivity;
import org.techtown.hoxy.waste.ResultActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;

    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    private String text_title;
    private String text_content1;
    private String text_content2;
    private String text_content3;
    private String text_content1_time;
    private String text_content2_time;
    private String text_content3_time;
    private TextView home_main_textView, content_textView, content2_textView, content3_textView, content1_time_textView, content2_time_textView, content3_time_textView;

    private ListView noticeList;
    private NoticeAdapter noticeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        setSupportActionBar(toolbar);

        setView_NavHeader();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        setView_Drawer(toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_community, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();

        navigationView.setNavigationItemSelectedListener(this);

        if (sp == null) {
        }
        setView_Profile();


        home_main_textView = findViewById(R.id.text_home); //혹시 어떻게 버리지??

        noticeList = findViewById(R.id.noticeList);

        noticeAdapter = new NoticeAdapter();
        noticeAdapter.addItem(new NoticeItem("폐기물 수거는 평일 오전 7시 ~ 오후 4시(월~토)까지이며, 일요일(공휴일)은 수거하지 않습니다."));
        noticeAdapter.addItem(new NoticeItem("배출일시는 수거 시간이 아니며, 수거는 사정에 따라 1~2일 소요됩니다."));
        noticeAdapter.addItem(new NoticeItem("배출 당일 또는 익일에 수거, 상황에 따라 수거가 지연될 수 있습니다."));
        noticeList.setAdapter(noticeAdapter);

        text_title = "혹시 어떻게 버리지..?";
        home_main_textView.setText(text_title);

        //갤러리로 넘어가기
        ImageButton imageButton = findViewById(R.id.galleryButton);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("intent_text", "image");
                startActivity(intent);
            }
        });
        //카메라 클릭시
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("intent_text", "camera");
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            onClickLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onClickLogout() {
        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
            }

            @Override
            public void onNotSignedUp() {
            }

            @Override
            public void onSuccess(Long result) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setView_NavHeader() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        nav_header_view = navigationView.getHeaderView(0);
        nav_header_id_text = (TextView) nav_header_view.findViewById(R.id.user_name);
        nav_header_id_text.setText(sp.getString("name", ""));


    }

    private void setView_Profile() {//은석
        profile = nav_header_view.findViewById(R.id.profileimage);

        String urlStr;
        urlStr = sp.getString("image_url", "");
        new Thread() {
            public void run() {
                try {
                    String urlStr = sp.getString("image_url", "");
                    URL url = new URL(urlStr);
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    final Bitmap bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    if (bm == null) {
                    }
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 사용하고자 하는 코드
                            if (bm != null) {
                                profile.setImageBitmap(bm);
                            } else return;
                        }
                    }, 0);


                } catch (IOException e) {
                }

            }
        }.start();
    }

    private void setView_Drawer(Toolbar toolbar) {
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_community) {
            Intent intent = new Intent(getApplicationContext(), PostListActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
            startActivity(intent);
            finish();
        }
        drawer = findViewById(R.id.drawer_layout);//??
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
