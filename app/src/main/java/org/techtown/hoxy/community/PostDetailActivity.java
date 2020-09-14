package org.techtown.hoxy.community;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.hoxy.MainActivity;
import org.techtown.hoxy.R;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import org.techtown.hoxy.RequestHttpURLConnection;

import org.techtown.hoxy.login.LoginActivity;
import org.techtown.hoxy.waste.MypageActivity;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class PostDetailActivity extends AppCompatActivity implements Serializable, NavigationView.OnNavigationItemSelectedListener {
   // private PostItem item;
    private int post_List_post_no;
    //private int post_detail_post_no;
    private CommentAdapter adapter;
    private String comment;
    private EditText othersComment;
    private ArrayList<CommentItem> items = new ArrayList<CommentItem>();
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private TextView userId;
    private String assess_userId;
    private TextView title;
    private TextView content;
    private ImageView userImage;
    private TextView post_reg_date;
    private ListView listView;
    private ImageButton writeButton;
    private ImageView content_image;
    private JSONArray ja_array;
    private Bitmap bitmap_img;
    private SharedPreferences sp;
    private String reg_user_id;
    private String assess_reg_userId;
    private String flag;
    private ArrayList<Integer> arrayReviewNo = new ArrayList<Integer>();
    private ArrayList<String> arrayReviewContent = new ArrayList<String>();
    private ArrayList<String> arrayReviewUser = new ArrayList<String>();
    private ArrayList<String> arrayReviewDate = new ArrayList<String>();
    private ArrayList<CommentItem> commentList = new ArrayList<CommentItem>();

    private MenuItem deleteItem;
    private MenuItem updateItem;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayoutPostWriteActivity();//init

        findView();//View들과 연결

        post_List_post_no = getIntent().getIntExtra("post_no", 0);
        assess_reg_userId = getIntent().getStringExtra("user_id");
        userAssess();
        connect_server();
        writeComment();
    }
    public void initLayoutPostWriteActivity() {           //레이아웃 정의
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setView_Drawer(toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_community, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navigationView.setNavigationItemSelectedListener(this);
        sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        setSupportActionBar(toolbar);
        setView_NavHeader();
        setView_Profile();

        setView_Drawer(toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_community, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    private void setView_Drawer(Toolbar toolbar) {
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return false;
    }

    public void findView(){
        userId = (TextView) findViewById(R.id.useridView);
        title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.title2);
        content_image = (ImageView) findViewById(R.id.content_image);
        userImage = (ImageView) findViewById(R.id.userImage);
        listView = (ListView) findViewById(R.id.detailCommentList);
        writeButton = (ImageButton) findViewById(R.id.writeButton);
        othersComment = (EditText) findViewById(R.id.othersComment);
        post_reg_date = (TextView) findViewById(R.id.reg_date);

    }
    public void userAssess(){
        sp=getSharedPreferences("profile", Activity.MODE_PRIVATE);
        assess_userId = sp.getString("name","");
        if(assess_reg_userId.equals(assess_userId)){
            flag ="REG";
        }
        else flag="NEW";
    }

    public void connect_server(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("board_no",post_List_post_no);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        NetworkTask networkTask = new NetworkTask(this, jsonObject.toString());
        networkTask.execute();

        //CommentList서버에서 받아오기
        Network_comment_select_task comment_select_task = new Network_comment_select_task("select_board_review");
        comment_select_task.execute();
    }
    public void writeComment(){
         /*
        텍스트 입력 후 버튼 선택시 서버에 댓글을 보냄
        * */
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCommentData();
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), PostListActivity.class);
        finish();
        startActivity(intent);
        super.onBackPressed();
    }

    public String request_post_data(String value) throws JSONException {
        //postAdapter = new PostAdapter();
        int TIMEOUT_VALUE = 1000;
        String result = "";
        String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/" + "select_board" + "/";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            URL url = new URL(str_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속
            //--------------------------
            //   전송 모드 설정 - 기본적인 설정이다
            //--------------------------

            http.setDefaultUseCaches(false);
            http.setDoInput(true);                         // 서버에서 읽기 모드 지정
            http.setDoOutput(true);                       // 서버로 쓰기 모드 지정

            http.setConnectTimeout(TIMEOUT_VALUE);
            http.setRequestMethod("POST");         // 전송 방식은 POST

            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();
            String currentlocationsend = "data=" + value;

            buffer.append(currentlocationsend);                 // php 변수에 값 대입
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
            result = result.replace("&#39;","\"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String request_post_data_image(String value) throws JSONException {
        //postAdapter = new PostAdapter();
        int TIMEOUT_VALUE = 1000;
        String result = "";
        String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/get_image/";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            URL url = new URL(str_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();   // 접속
            //--------------------------
            //   전송 모드 설정 - 기본적인 설정이다
            //--------------------------

            http.setDefaultUseCaches(false);
            http.setDoInput(true);                         // 서버에서 읽기 모드 지정
            http.setDoOutput(true);                       // 서버로 쓰기 모드 지정

            http.setConnectTimeout(TIMEOUT_VALUE);
            http.setRequestMethod("POST");         // 전송 방식은 POST

            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();
            String currentlocationsend = "data={\"image_name\":\"" + value + "\"}";

            buffer.append(currentlocationsend);                 // php 변수에 값 대입
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
            result = result.replace("&#39;","\"");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
    * 안드로이드에서 Post형식으로 서버에 데이터를 보내는 코드
    *
    * */

    class CommentAdapter extends BaseAdapter {
        ArrayList<CommentItem> commentItems;
        public CommentAdapter(ArrayList<CommentItem> commentItems)
        {
            this.commentItems = commentItems;
        }

        public CommentAdapter() {
        }

        @Override
        public int getCount() {
            return commentItems.size();
        }

        public void addItem(CommentItem item){
            commentItems.add(item);
        }
        @Override
        public Object getItem(int position) {
            return commentItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommentItemView view = null;
            if(convertView == null){
                view = new CommentItemView(getApplicationContext());
            }
            else{
                view = (CommentItemView) convertView;
            }
            CommentItem item = commentItems.get(position);
            view.setUserId(item.getUserId());
            view.setImage(item.getResId());
            view.setComment(item.getComment());
            view.setReg_date(item.getReg_date());
            return view;
        }
    }

    //actionbar 관련 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //해당 게시물을 등록한 사용자일 경우의 액션바 세팅
        if(flag.equals("REG")) {
            getMenuInflater().inflate(R.menu.detail_main, menu);
        }
        if (flag.equals("NEW")){
            getMenuInflater().inflate(R.menu.detail_main2, menu);
        }
        return true;
    }
    //actionbar 관련 코드
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(flag.equals("REG")) {
            int curId = item.getItemId();
           if(curId == R.id.menu_update){
                updatePost();
               return true;
           }
           if(curId == R.id.menu_delete){
               deletePost();
               return true;
           }
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

    //---------------------------------------
    /* 해당 게시글의 모든 정보를 받아오는 클래스*/
    //---------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public class NetworkTask extends AsyncTask<Void, Void, String> {
        String values;
        Context mcontext;

        NetworkTask(Context mcontext, String values) {
            this.mcontext = mcontext;
            this.values = values;
        }//생성자

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
        }//실행 이전에 작업되는 것들을 정의하는 함수

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            try {
                //서버로 게시글 번호를 주고 게시글 댓글 데이타를 받아옴.
                result = request_post_data(values);

                //get_image
                JSONArray temp_js = new JSONArray(result);
                JSONObject jsonObject = temp_js.getJSONObject(0);
                String file_name = jsonObject.getString("file_name");
                String image_str = request_post_data_image(file_name);
                bitmap_img = StringToBitmap(image_str);

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
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.
            if (result != "") {
                try {
                    ja_array= new JSONArray(result);
                } catch (JSONException e) {
                    //TODO Auto-generated catch block
                    e.printStackTrace();
                }
                    try {
                        JSONObject jsonObject = ja_array.getJSONObject(0);
                        // array에 해당 값들을 넣어줌.
                        //Time Setting
                        jsonObject.getString("board_no");
                        String time = jsonObject.getString("board_reg_date");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        userId.setText(jsonObject.getString("board_user_name"));
                        title.setText(jsonObject.getString("board_title"));
                        content.setText(jsonObject.getString("board_ctnt"));//content로 변경해야됨(주용이와 대화 필요)
                        content_image.setImageBitmap(bitmap_img);
                        userImage.setImageResource(R.drawable.user1);
                        post_reg_date.setText(jsonObject.getString("board_reg_date"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }//result not null
            else {
            }
        }//onPostExecute func()
    }//NetWorkTask Class

    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    public class Network_comment_select_task extends AsyncTask<String, String, String> {
        String sub_url = "";
        Network_comment_select_task(String sub_url){
            this.sub_url = sub_url;
        }
        @Override
        protected String doInBackground(String... params) {
            String res = "";
            try {
                String str = "";
                String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/" + sub_url + "/";
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
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                //--------------------------
                //   서버로 값 전송
                //--------------------------
                StringBuffer buffer = new StringBuffer();
                String data = "data=" + "{\"board_review_board_no\":"+ post_List_post_no + "}";
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
                res = res.replace("&#39;","\"");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return res;
        }
        @Override
        protected void onPostExecute(String result) {
            set_data(result);
        }
    }
    public void set_data(String data){
        try {
            String str_res = data;
            JSONArray ja_res = new JSONArray(str_res);

            if(ja_res != null) {
                for (int i = 0; i < ja_res.length(); i++) {
                    try {
                        JSONObject jo_data = ja_res.getJSONObject(i);
                        arrayReviewNo.add(jo_data.getInt("board_review_no"));
                        arrayReviewContent.add( jo_data.getString("board_review_ctnt"));
                        arrayReviewUser.add(jo_data.getString("board_review_user_name"));
                        arrayReviewDate.add(jo_data.getString("board_review_reg_date"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = 0; i < arrayReviewContent.size(); i++) {
                CommentItem commentItem = new CommentItem(R.drawable.user1, arrayReviewContent.get(i), arrayReviewUser.get(i), arrayReviewNo.get(i),arrayReviewDate.get(i));
                commentList.add(commentItem);
            }
            adapter = new CommentAdapter(commentList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void insertCommentData(){
        SharedPreferences sp=getSharedPreferences("profile", Activity.MODE_PRIVATE);
        String user_id = sp.getString("token","");

        comment = othersComment.getText().toString();
        othersComment.setText(null);

        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String review_reg_date = simpleDate.format(mDate);
        //서버로 보내기
        // URL 설정.

        JSONObject board_data = new JSONObject();
        try {
            board_data.put("board_review_board_no", post_List_post_no);
            board_data.put("board_review_ctnt", comment);
            board_data.put("board_review_reg_user_id", user_id);
            board_data.put("board_review_reg_date",review_reg_date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //--------------------------------
        /* 게시글 정보를 서버에 보냄   Part*/
        //--------------------------------
        Network_comment_insert_task comment_insert_task = new Network_comment_insert_task(board_data.toString());
        comment_insert_task.execute();

        Toast.makeText(PostDetailActivity.this, "댓글 등록 성공", Toast.LENGTH_SHORT).show();
    }
    //--------------------------------
    /* 댓글 정보를 서버에 보내는 Class*/
    //--------------------------------
    public class Network_comment_insert_task extends AsyncTask<Void, Void, String> {
        String values;
        Network_comment_insert_task(String values) {
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
            /*
            * 댓글 추가 후 댓글리스트 갱신을 위한 작업
            * */
            Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
            intent.putExtra("post_no",post_List_post_no);
            intent.putExtra("user_id",assess_reg_userId);
            startActivity(intent);
            finish();
        }
    }
    public String sendCommentWrite(String values) throws JSONException {
        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/insert_board_review/";
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
    } // sendCommentWrite

    public void deletePost(){
        JSONObject board_data = new JSONObject();
        try {
            board_data.put("board_no", post_List_post_no);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        network_delete_post_task delete_post_task = new network_delete_post_task(board_data.toString());
        delete_post_task.execute();
    }
    public class network_delete_post_task extends AsyncTask<Void, Void, String> {
        String values;

        network_delete_post_task(String values) {
            this.values = values;
        }//생성자

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progress bar를 보여주는 등등의 행위
        }//실행 이전에 작업되는 것들을 정의하는 함수

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            try {
                //서버로 게시글 번호를 주고 게시글 댓글 데이타를 받아옴.
                result = sendDeleteMessage(values);
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
            // 통신이 완료되면 호출됩니다.
            // 결과에 따른 UI 수정 등은 여기서 합니다.
            Intent intent = new Intent(getApplicationContext(), PostListActivity.class);
            //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();
        }//onPostExecute func()
    }//NetWorkTask Class

    public String sendDeleteMessage(String values) throws JSONException {
        String result = "";
        try {
            //--------------------------
            //   URL 설정하고 접속하기
            //--------------------------
            String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/delete_board/";
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
    public void updatePost(){

        Intent intent = new Intent(getApplicationContext(), PostWriteActivity.class);
        intent.putExtra("flag","update");
        intent.putExtra("board_no",post_List_post_no);
        //글쓰기 완료 후 전환 시 액티비티가 남지 않게 함
        startActivity(intent);
    }
}
