package org.techtown.hoxy.community;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

public class PostListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {
    private PostAdapter adapter;
    private Bundle data;
    private PostItem item;
    private ListView listView;
    private ArrayList<PostItem> items;
    private CommentItemView view;

    private EditText search_text;
    private Button search_button;
    //postItem의 객체정보 값을 갖는 array들
    private ArrayList<String> arrayregDate = new ArrayList<String>();
    ArrayList<String> arrayregUser = new ArrayList<String>();
    private ArrayList<String> arraytitle = new ArrayList<String>();
    private ArrayList<Integer> arrayPostNo = new ArrayList<Integer>();
    private ArrayList<Integer> arrayimage = new ArrayList<Integer>();
    private ArrayList<String> arrayContetnt = new ArrayList<String>();
    //PostItem 클래스 타입의 ArrayList
    private ArrayList<PostItem> postList = new ArrayList<PostItem>();
    private ArrayList<Bitmap> files = new ArrayList<Bitmap>();

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private EditText editTextFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayoutPostListActivity();

        listView = (ListView) findViewById(R.id.listView);
        data = new Bundle();

        //----------------------------
        /*      게시글을 전부 가져옴  */
        //----------------------------
        http_task http_task = new http_task("select_board_title");
        http_task.execute();
        set_button_action();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 101) {
            if (intent != null) {
                String contents = intent.getStringExtra("contents");
                String commentTitle = intent.getStringExtra("title");

                Toast.makeText(getApplicationContext(),"메뉴화면으로부터 응답 : "+ commentTitle, Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }
        }
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

    public class PostAdapter extends BaseAdapter {
        ArrayList<PostItem> postItems; // main으로부터 modellist들을 전달 받을 객체
        ArrayList<PostItem> arrayList; // filter 작업시 필요한 arrarylist
        public PostAdapter(ArrayList<PostItem> postItems)
        {
            this.postItems = postItems;
            this.arrayList = new ArrayList<PostItem>();
            this.arrayList.addAll(postItems);
        }

        @Override
        public int getCount() {
            return postItems.size();
        }

        public void addItem(PostItem item){
            postItems.add(item);
        }
        @Override
        public Object getItem(int position) {
            return postItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            view = null;
            if(convertView == null){
                view = new CommentItemView(getApplicationContext());
            }
            else{
                view = (CommentItemView) convertView;
            }
            PostItem item = postItems.get(position);
            view.setUserId(item.getUserId());
            view.setImage(item.getResId());
            view.setComment(item.getTitle());
            view.setReg_date(item.getReg_date());

            return view;
        }
        //filter 기능
        public void filter(String charText) {
            PostItem tempPostItem =  new PostItem();
            charText = charText.toLowerCase(Locale.getDefault()); //모두 소문자로 변환
            postItems.clear();
            if (charText.length() == 0) { //없을 경우
                postItems.addAll(arrayList); //모두 보여줌
            } else {
                for (PostItem postItem : arrayList) {
                    //내용과 일치하는 것이 있을 경우
                    if (postItem.getTitle().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        postItems.add(postItem);
                        //동일내용 중복 표시 방지
                        tempPostItem = postItem;
                    }
                    //모델의 내용과 일치하는 것이 있을 경우
                    if (postItem.getTitle().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        //중복검사
                        if(tempPostItem != postItem)
                        {
                            postItems.add(postItem);
                        }
                    }
                }
            }
            //listView 갱신
            notifyDataSetChanged();
        }//filter func
    }

    public void onCommand(String command,Bundle data){
        /*
         *postList 혹은 fab버튼을 click시 화면 전환을 위한 함수
         * 화면 전환을 위한
         * */
        if (command.equals("writeComment")) {
            // 액티비티를 띄우는 경우
            Intent intent = new Intent(getApplicationContext(), PostWriteActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra("flag","insert");
            startActivity(intent);
        }
        if (command.equals("showDetail")){
            String userid = sp.getString("token","");
            Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
            intent.putExtra("post_no",item.getPost_no());
            intent.putExtra("user_id",item.getUserId());
            startActivity(intent);
            finish();
        }
    }

    public void initLayoutPostListActivity() {           //레이아웃 정의
        setContentView(R.layout.activity_community_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        setSupportActionBar(toolbar);
        setView_NavHeader();
        setView_Profile();

        drawer = findViewById(R.id.drawer_layout);

        setView_Drawer(toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_community, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void set_button_action(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                item = (PostItem) adapter.getItem(position);

                onCommand("showDetail",data);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommand("writeComment",data);
            }
        });

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

    public class http_task extends AsyncTask<String, String, String> {
        String sub_url = "";
        http_task(String sub_url){
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
                        arrayPostNo.add(jo_data.getInt("board_no"));
                        arraytitle.add( jo_data.getString("board_title"));
                        arrayregUser.add(jo_data.getString("board_user_name"));
                        arrayregDate.add(jo_data.getString("board_reg_date"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = arraytitle.size() - 1; i >=0 ; i--) {
                PostItem postItem = new PostItem(R.drawable.user1, arraytitle.get(i), arrayregUser.get(i), arrayPostNo.get(i),arrayregDate.get(i));
                //bind all strings in an array
                postList.add(postItem);
            }
            adapter = new PostAdapter(postList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.postlist_menu, menu);
        //----------------------------
        /* 메뉴의 SearchView 설정  */
        //----------------------------
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.search_hint_query));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //검색어 완료시
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            //검색어 입력시(실질적인 검색 기능 구현 listview의 filter(s)함수를 통해서!)
            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    adapter.filter("");
                    listView.clearTextFilter();
                } else {
                    adapter.filter(s);
                }
                return true;
            }
        });//onQueryTextListener func()
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
}