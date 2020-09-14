package org.techtown.hoxy.community;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.kakao.util.helper.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.techtown.hoxy.MainActivity;
import org.techtown.hoxy.R;
import org.techtown.hoxy.RequestHttpURLConnection;
import org.techtown.hoxy.waste.MypageActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

public class PostWriteActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{
    private final int REQUEST_WIDTH = 120;
    private final int REQUEST_HEIGHT = 160;
    final String TAG = getClass().getSimpleName();
    final static int TAKE_PICTURE = 1;
    private ImageView picture;
    private EditText contentsInput;
    private EditText commentTitle;
    private Intent intent;
    private Bitmap waste_bitmap;

    private Button saveButton;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private View nav_header_view;
    private Button cancelButton;
    private String files;
    private String file_name;
    private int post_no;
    private String contents;
    private String title;
    private String user_id;
    private String board_reg_date;
    private String flag;
    private SharedPreferences sp;

    private ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayoutPostWriteActivity();
        intent = new Intent(this.getIntent());
        set_inflate();

        call_the_camera();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = intent.getStringExtra("flag");
                if(flag.equals("update") ){
                    post_no = intent.getIntExtra("board_no",0);
                    updatePost();
                }
                else insertPostData();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked_CancelButton();
            }
        });
    }
    public void initLayoutPostWriteActivity() {           //레이아웃 정의
        setContentView(R.layout.activity_comment_write);
        setView_Toolbar();
        setView_NavHeader();
        setView_Drawer();
        setView_Profile();
    }
    private void setView_Drawer() {
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open , R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setView_NavHeader() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        nav_header_view = navigationView.getHeaderView(0);
    }

    private void setView_Toolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Community");
        toolbar.setTitleMargin(5, 0, 5, 0);
    }
    private void setView_Profile() {//은석
        profile = nav_header_view.findViewById(R.id.profileimage);
        sp=getSharedPreferences("profile", Activity.MODE_PRIVATE);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_community) {
            Intent intent = new Intent(getApplicationContext(), PostListActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            startActivity(intent);
            finish();
        }else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(getApplicationContext(), MypageActivity.class);
            startActivity(intent);
            finish();
        }
        drawer = findViewById(R.id.drawer_layout);//??
        drawer.closeDrawer(GravityCompat.START);

        return false;
    }

    // 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
        }
    }

    public void clicked_CancelButton(){
        intent = new Intent(getApplicationContext(), PostListActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(PostWriteActivity.this, "게시글 등록 취소", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        clicked_CancelButton();
        super.onBackPressed();
    }

    public void call_the_camera(){
        picture = (ImageView)findViewById(R.id.pictureView);
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.pictureView:
                        // 카메라 앱을 여는 소스
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // 카메라로 촬영한 영상을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == TAKE_PICTURE){
            if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                waste_bitmap = (Bitmap) intent.getExtras().get("data");
                if (waste_bitmap != null) {
                    picture.setImageBitmap(waste_bitmap);
                }
            }
        }

        waste_bitmap = bitmap_resize(waste_bitmap);
        files = encodeTobase64(waste_bitmap);
    }
    public static Bitmap bitmap_resize(Bitmap bitmap){
        int width = bitmap.getWidth(); // 축소시킬 너비
        int height = bitmap.getHeight(); // 축소시킬 높이

        if(bitmap.getWidth() > 1000 || bitmap.getHeight() > 1000) {
            width = width / 5;
            height = height / 5;
        }
        else return bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        String filePath = null;

        float bmpWidth = bitmap.getWidth();
        float bmpHeight = bitmap.getHeight();

        if (bmpWidth > width) {
            // 원하는 너비보다 클 경우의 설정
            float mWidth = bmpWidth / 10;
            float scale = width/ mWidth;
            bmpWidth *= (scale / 10);
            bmpHeight *= (scale / 10);
        }
        if (bmpHeight > height) {
            // 원하는 높이보다 클 경우의 설정
            float mHeight = bmpHeight / 10;
            float scale = height/ mHeight;
            bmpWidth *= (scale / 10);
            bmpHeight *= (scale / 10);
        }

        Bitmap resizedBmp = Bitmap.createScaledBitmap(bitmap, (int) bmpWidth, (int) bmpHeight, true);
        return resizedBmp;
    }
    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = encodeToString(b, NO_WRAP);
        return imageEncoded;
    }
    public void set_inflate(){
        contentsInput = (EditText) findViewById(R.id.contentsInput);
        commentTitle = (EditText) findViewById(R.id.commentTitle);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
    }

    public void input_data(){
        sp=getSharedPreferences("profile", Activity.MODE_PRIVATE);
        user_id = sp.getString("token","");

        contents = contentsInput.getText().toString();
        title = commentTitle.getText().toString();
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddhhmmss");
        board_reg_date = simpleDate.format(mDate);

        file_name = board_reg_date + user_id+".jpg";
    }
    /*
    입력된 게시글의 정보를 서버에 보냄
    * */
    public void insertPostData(){
        input_data();
        //서버로 보내기
        // URL 설정.
        String board_data = "";
        board_data = "{\"board_title\":\""+ title + "\"," +
                "\"board_ctnt\":\""+ contents + "\"," +
                "\"board_reg_user_no\":\""+ user_id + "\"," +
                "\"board_area_no\":"+ "1" + "," +
                "\"board_reg_date\":\""+ board_reg_date + "\"," +
                "\"files\":\""+ files + "\"," +
                "\"file_name\":\""+ file_name + "\"}";
        //--------------------------------
        /* 게시글 정보를 서버에 보냄   Part*/
        //--------------------------------
        NetworkTask networkTask = new NetworkTask(board_data);
        networkTask.execute();

        intent = new Intent(getApplicationContext(), PostListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        Toast.makeText(PostWriteActivity.this, "게시글 등록 성공", Toast.LENGTH_SHORT).show();
    }
    //--------------------------------
    /* 게시글 정보를 서버에 보내는 Class*/
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
                result = sendCommentWrite(values);
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
    public String sendCommentWrite(String values) throws JSONException {
        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/insert_board/";

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
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
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

    public void updatePost(){
        input_data();
        String board_data = "";
        board_data = "{" +"\"board_no\":\""+ post_no + "\"," +
                "\"board_title\":\""+ title + "\"," +
                "\"board_ctnt\":\""+ contents + "\"," +
                "\"board_area_no\":"+ "1" + "," +
                "\"files\":\""+ files + "\"," +
                "\"file_name\":\""+ file_name + "\"}";

        network_update_post_task update_post_task = new network_update_post_task(board_data.toString());
        update_post_task.execute();

        Intent intent = new Intent(getApplicationContext(), PostListActivity.class);
        //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
        startActivity(intent);
        finish();
    }
    public class network_update_post_task extends AsyncTask<Void, Void, String> {
        String values;

        network_update_post_task(String values) {
            this.values = values;
        }//생성자

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }//실행 이전에 작업되는 것들을 정의하는 함수

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            try {
                //서버로 게시글 번호를 주고 게시글 댓글 데이타를 받아옴.
                result = sendUpdateMessage(values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
        }// 백그라운드 작업 함수
        //---------------------------------------------
        /* 서버로 부터 받아온 게시글 댓글로 댓글 UI 작업  */
        //---------------------------------------------
        @Override
        protected void onPostExecute(String result) {
        }//onPostExecute func()
    }//NetWorkTask Class

    public String sendUpdateMessage(String values) throws JSONException {
        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/update_board/";
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
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();
            String regdata = "data=" + values;
            buffer.append(regdata);                // php 변수에 값 대입

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
    } // sendDeleteMessage
}
