package org.techtown.hoxy.waste;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.hoxy.MainActivity;
import org.techtown.hoxy.R;
import org.techtown.hoxy.community.PostListActivity;
import org.techtown.hoxy.login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class WasteInfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    String select_name, select_size, select_fee;
    int select_no;
    ArrayList<String> spinnerArray = new ArrayList<String>();
    private Button next_button, cancle_button, again_button;
    private TextView waste_code_textView, waste_fee_textView;
    private Spinner waste_size_spinner;
    private String intent_text;
    private JSONArray wasteInfoItems;
    private int position;
    private ArrayList<Integer> waste_type_no = new ArrayList<>();
    private ArrayList<String> waste_name = new ArrayList<>(), waste_fee = new ArrayList<>(), waste_size = new ArrayList<>();
    private ArrayList<WasteInfoItem> waste_basket;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private byte[] waste_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wasteinfo);

        waste_size_spinner = (Spinner) findViewById(R.id.spinner);
        waste_code_textView = findViewById(R.id.textView2);
        waste_fee_textView = findViewById(R.id.textView4);
        cancle_button = findViewById(R.id.button3);
        again_button = findViewById(R.id.button6);
        next_button = findViewById(R.id.button5);

        Toolbar toolbar = findViewById(R.id.toolbar4);
        sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        setSupportActionBar(toolbar);
        setView_NavHeader();
        setView_Profile();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setView_Drawer(toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_community, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navigationView.setNavigationItemSelectedListener(this);

        //전 화면에서 받아오기
        Intent intent_get = getIntent();
        intent_text = intent_get.getExtras().getString("intent_text");
        String temp_wasteInfoItems = (String) intent_get.getSerializableExtra("wasteInfoItems");
        waste_basket = (ArrayList<WasteInfoItem>) intent_get.getSerializableExtra("wastebasket");

        try {
            wasteInfoItems = new JSONArray(temp_wasteInfoItems);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        position = intent_get.getExtras().getInt("position");

        for (int i = 0; i < wasteInfoItems.length(); i++) {
            try {
                JSONObject jo_data = wasteInfoItems.getJSONObject(i);
                waste_name.add(jo_data.getString("waste_type_kor_name"));
                waste_type_no.add(jo_data.getInt("waste_type_no"));
                waste_size.add(jo_data.getString("waste_type_size"));
                waste_fee.add(jo_data.getString("waste_type_fee"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //스피너 아이템 추가
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, waste_size);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        waste_size_spinner.setAdapter(adapter);
        waste_size_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                waste_code_textView.setText(waste_name.get(position));
                waste_fee_textView.setText(waste_fee.get(position));
                select_fee = waste_fee.get(position);
                select_size = waste_size.get(position);
                select_name = waste_name.get(position);
                select_no = waste_type_no.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // 취소
        cancle_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(WasteInfoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        // 다음
        next_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WasteInfoActivity.this, WasteApplyActivity.class);
                intent.putExtra("position", position);
                addBasket(select_name, select_fee, select_size, select_no);
                intent.putExtra("wastebasket", waste_basket);
                startActivity(intent);
            }
        });
    }

    //한번 더
    public void OnClickHandler(View view) {
        final CharSequence[] items = {"카메라", "갤러리", "취소"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("방법 선택")        // 제목 설정
                .setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정

                    public void onClick(DialogInterface dialog, int index) {
                        if (index == 0) {
                            Intent intent = new Intent(WasteInfoActivity.this, ResultActivity.class);
                            intent.putExtra("intent_text", "camera");
                            intent.putExtra("position", ++position);
                            intent.putExtra("wasteInfoItems", wasteInfoItems.toString());
                            addBasket(select_name, select_fee, select_size, select_no);
                            intent.putExtra("wastebasket", waste_basket);
                            startActivity(intent);
                        } else if (index == 1) {
                            Intent intent = new Intent(WasteInfoActivity.this, ResultActivity.class);
                            intent.putExtra("intent_text", "image");
                            intent.putExtra("position", ++position);
                            intent.putExtra("wasteInfoItems", wasteInfoItems.toString());
                            addBasket(select_name, select_fee, select_size, select_no);
                            intent.putExtra("wastebasket", waste_basket);
                            startActivity(intent);
                        } else {
                            dialog.cancel();
                        }
                    }
                });
        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }

    private void addBasket(String name, String fee, String size, int no) {
        WasteInfoItem wasteInfoItem = new WasteInfoItem(name, size, Integer.parseInt(fee), no);
        waste_basket.add(wasteInfoItem);
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

    private void setView_Profile() {
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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            startActivity(intent);
            finish();
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

