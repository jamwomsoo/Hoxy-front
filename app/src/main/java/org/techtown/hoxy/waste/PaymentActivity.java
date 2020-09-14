package org.techtown.hoxy.waste;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import org.techtown.hoxy.CodeActivity;
import org.techtown.hoxy.MainActivity;
import org.techtown.hoxy.R;
import org.techtown.hoxy.RequestHttpURLConnection;
import org.techtown.hoxy.community.PostListActivity;
import org.techtown.hoxy.login.LoginActivity;


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

import static java.lang.Integer.parseInt;


public class PaymentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private WebView webView; // 웹뷰 선언
    private String total_fee;
    private String size;
    private String name;
    private String user_name;
    public Context mContext;
    private ApplyInfo info_apply;
    private ProgressBar progressBar;

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    private ArrayList<WasteInfoItem> waste_basket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        Intent intent_get = getIntent();
        info_apply=(ApplyInfo)intent_get.getSerializableExtra("apply_info");
        user_name=intent_get.getExtras().getString("user");
        total_fee = intent_get.getExtras().getString("total_fee");
        size = intent_get.getExtras().getString("size");
        name = intent_get.getExtras().getString("name");
        waste_basket = (ArrayList<WasteInfoItem>) intent_get.getSerializableExtra("wastebasket");
        mContext=this.getApplicationContext();

        http_task http_task = new http_task("KakaoPay");
        http_task.execute();

        Toolbar toolbar = findViewById(R.id.toolbar6);
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
    }

    public JSONObject apply_info_json(String name, String total_fee, String size,String user_name) throws JSONException {
        JSONObject jo1 = new JSONObject();
        if (parseInt(size) >1) {
            name=name+" 외 "+(parseInt(size)-1);
        }
        jo1.put("name", name);
        jo1.put("total_fee", total_fee);
        jo1.put("size", size);
        jo1.put("user_name",user_name);

        return jo1;
    }

    public class http_task extends AsyncTask<String, String, String> {
        String sub_url = "";

        http_task(String sub_url) {
            this.sub_url = sub_url;
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "";
            try {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // runOnUiThread를 추가하고 그 안에 UI작업을 한다.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();

                String str = "";
                String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/KakaoPay/";
                URL url = new URL(str_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //--------------------------
                //   전송 모드 설정 - 기본적인 설정이다
                //--------------------------
                conn.setDefaultUseCaches(false);
                conn.setDoInput(true);                         // 서버에서 읽기 모드 지정
                conn.setDoOutput(true);                       // 서버로 쓰기 모드 지정
                conn.setRequestMethod("POST");         // 전송 방식은 POST

                // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
                //   서버로 값 전송
                //--------------------------
                StringBuffer buffer = new StringBuffer();
                String data = "data="+apply_info_json(name,total_fee,size,user_name).toString();
                buffer.append(data);

                OutputStreamWriter outStream = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();

                //--------------------------
                //   서버에서 전송받기
                //--------------------------
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                    builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
                }

                res = builder.toString();
                res = res.replace("&#39;", "\"");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return res;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String result) {
            JSONObject jo1 = new JSONObject();
            try {
                jo1 = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.setWebViewClient(new MyWebViewClient());

            try {
                String weburl = jo1.getString("next_redirect_app_url");
                webView.loadUrl(weburl);
            } catch (JSONException e) {
            }
        }
    }


    public class MyWebViewClient extends WebViewClient {
        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

        // 로딩이 시작될 때
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        // 리소스를 로드하는 중 여러번 호출
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        // 방문 내역을 히스토리에 업데이트 할 때
        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        // 로딩이 완료됬을 때 한번 호출
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        // 오류가 났을 경우, 오류는 복수할 수 없음
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            switch (errorCode) {
                case ERROR_AUTHENTICATION:
                    break;               // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:
                    break;                           // 잘못된 URL
                case ERROR_CONNECT:
                    break;                          // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:
                    break;    // SSL handshake 수행 실패
                case ERROR_FILE:
                    break;                                  // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:
                    break;               // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:
                    break;           // 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:
                    break;                              // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:
                    break;   // 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:
                    break;               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:
                    break;                          // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:
                    break;     // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:
                    break;                        // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:
                    break; // 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:
                    break;          // URI가 지원되지 않는 방식
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // SSL 에러가 발생해도 계속 진행!
        }

        // 확대나 크기 등의 변화가 있는 경우
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        // 잘못된 키 입력이 있는 경우
        // 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 기회를 줌
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent call_phone = new Intent(Intent.ACTION_CALL);
                call_phone.setData(Uri.parse(url));
            } else if (url.startsWith("sms:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
            } else if (url.startsWith("intent:")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (url.startsWith("app://")){
                Intent intent = new Intent(mContext, CodeActivity.class);

                String code=url.substring(6,url.length());
                intent.putExtra("code",code);
                intent.putExtra("wastebasket", waste_basket);
                intent.putExtra("total_fee",total_fee);
                intent.putExtra("size",size);
                intent.putExtra("info_apply", info_apply);
                startActivity(intent);
                return true;
            }

            else {
                view.loadUrl(url);
            }
            return true;
        }
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
            // Handle the camera action
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_community) {
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