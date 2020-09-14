package org.techtown.hoxy;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Integer.parseInt;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.hoxy.community.PostListActivity;
import org.techtown.hoxy.login.LoginActivity;
import org.techtown.hoxy.waste.ApplyInfo;
import org.techtown.hoxy.waste.MypageActivity;
import org.techtown.hoxy.waste.WasteInfoItem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class CodeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String code;
    private String size;
    private TextView codeView;
    private Button btn_clip;
    private Button btn_finish;
    private String total_fee;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private ArrayList<WasteInfoItem> waste_basket;
    private ApplyInfo info_apply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        codeView = findViewById(R.id.tv_codeview);
        btn_clip = findViewById(R.id.bt_clip);
        btn_finish = findViewById(R.id.bt_finish);
        Intent intent = getIntent();
        code = intent.getExtras().getString("code");
        size = intent.getExtras().getString("size");
        total_fee = intent.getExtras().getString("total_fee");
        info_apply = (ApplyInfo) intent.getSerializableExtra("info_apply");
        waste_basket = (ArrayList<WasteInfoItem>) intent.getSerializableExtra("wastebasket");
        Toolbar toolbar = findViewById(R.id.toolbar7);
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

        SharedPreferences sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        String user_id = sp.getString("token", "");

        JSONObject jo = new JSONObject();
        try {
            jo.put("apply_info_name", info_apply.getUser_name());
            jo.put("apply_info_address", info_apply.getAddress());
            jo.put("apply_info_phone", info_apply.getPhone_No());
            jo.put("total_size",size);
            jo.put("apply_info_waste_type_no", waste_basket.get(0).getWaste_No());
            jo.put("apply_info_fee", total_fee);
            jo.put("apply_info_code", code);
            jo.put("apply_info_user_no", user_id.toString());
            jo.put("apply_info_reg_date", info_apply.getApply_date());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NetworkTask networkTask = new NetworkTask(jo.toString());
        networkTask.execute();

        codeView.setText(code);
        btn_clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clip_click(code);
            }
        });
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(CodeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clip_click(String string) {
        String strLabel = "code";
        String strCopy = string;

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(strLabel, strCopy);
        clipboardManager.setPrimaryClip(clipData);
        toastMessage("code " + string + " 가 복사되었습니다.");
    }

    private void toastMessage(String string) {
        Toast myToast = Toast.makeText(this.getApplicationContext(), string, Toast.LENGTH_SHORT);
        myToast.show();
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

    //--------------------------------
    /* 결제 정보를 서버에 보내는 Class*/
    //--------------------------------
    public class NetworkTask extends AsyncTask<Void, Void, String> {
        String values;

        NetworkTask(String values) {
            this.values = values;
        }//생성자

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
        }//실행 이전 작업 정의 함수

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            try {
                //서버에 게시글 정보를 입력하는 함수 호출
                result = sendPaymentData(values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        } // 백그라운드 작업 함수

        @Override
        protected void onPostExecute(String result) {
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.
        }
    }

    public String sendPaymentData(String values) throws JSONException {

        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/insert_waste_apply_info/";

            URL url = new URL(str_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속

            //--------------------------
            //   전송 모드 설정 - 기본적인 설정이다
            //--------------------------
            http.setDefaultUseCaches(false);
            http.setDoInput(true);                         // 서버에서 읽기 모드 지정
            http.setDoOutput(true);                       // 서버로 쓰기 모드 지정
            http.setRequestMethod("POST");         // 전송 방식은 POST

            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();
            String regdata = "data=" + values;
            buffer.append(regdata);                 // php 변수에 값 대입

            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();

            //--------------------------
            //   서버에서 전송받기
            //--------------------------
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;


            while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
            }
            result = builder.toString();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return result;
    } // HttpPostDat
}

