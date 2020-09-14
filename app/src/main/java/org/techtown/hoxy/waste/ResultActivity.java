package org.techtown.hoxy.waste;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;


import com.bumptech.glide.Glide;
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
import org.techtown.hoxy.community.PostListActivity;
import org.techtown.hoxy.community.PostWriteActivity;
import org.techtown.hoxy.login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static int TAKE_PICTURE = 1;
    private static final int REQUEST_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private String intent_text;
    private ImageView waste_ImageView;
    private TextView waste_textView;
    private Button again_button, next_button;

    private Bitmap waste_bitmap = null;        //사진이 저장되는 변수
   private Bitmap bitmap2 =null;
    private String trashName;
    private String send_data;
    private String files;
    private String deep_learning_answer;

    private int position = 0;
    private ArrayList<WasteInfoItem> waste_basket;
    private ProgressBar progressBar;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SharedPreferences sp;
    private View nav_header_view;
    private TextView nav_header_id_text;
    private ImageView profile;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

private Bitmap rated_bitmap;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = findViewById(R.id.toolbar3);
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

        //인텐트 받아오기
        Intent intent_get = getIntent();
        intent_text = Objects.requireNonNull(intent_get.getExtras()).getString("intent_text");
        String wasteInfoItems = (String) intent_get.getSerializableExtra("wasteInfoItems");
        waste_basket = (ArrayList<WasteInfoItem>) intent_get.getSerializableExtra("wastebasket");
        position = intent_get.getExtras().getInt("position");
        waste_textView = findViewById(R.id.textView);
        waste_textView.setText("사진이 없습니다.");

        again_button = findViewById(R.id.button);
        next_button = findViewById(R.id.button2);
        next_button.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progressBar2);
        //갤러리로 이동
        if (intent_text.equals("image")) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_CODE);
        }
        //카메라로 이동
        else if (intent_text.equals("camera")) {
            sendTakePhotoIntent();
        }
        waste_ImageView = (ImageView) findViewById(R.id.imageView);

        // 다시
        again_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(ResultActivity.this, ResultActivity.class);
                intent.putExtra("intent_text", intent_text);
                intent.putExtra("position", position);
                intent.putExtra("wasteInfoItems", deep_learning_answer);
                if(waste_basket == null) {
                    waste_basket = new ArrayList<WasteInfoItem>();
                }
                intent.putExtra("wastebasket", waste_basket);

                startActivity(intent);
            }
        });

        // 다음
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waste_basket == null) {
                    waste_basket = new ArrayList<WasteInfoItem>();
                    WasteImage.initBitmaps();
                }

                Intent intent2 = new Intent(ResultActivity.this, WasteInfoActivity.class);
                intent2.putExtra("intent_text", intent_text);
                intent2.putExtra("wasteInfoItems", deep_learning_answer);
                intent2.putExtra("wastebasket", waste_basket);
                intent2.putExtra("position", position);
                WasteImage.setBitmaps(bitmap2);

                startActivity(intent2);
            }
        });
    }//onCreate

    private String imageFilePath;
    private String photoUri;
    private Uri photoUri2;

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                photoUri2 = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri2);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = encodeToString(b, NO_WRAP);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (intent_text.equals("image")) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    try {
                        // 선택한 이미지에서 비트맵 생성
                        InputStream in = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                        waste_bitmap = BitmapFactory.decodeStream(in);
                        bitmap2 = waste_bitmap;
                        image_send(waste_bitmap);
                        assert in != null;
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }// try, catch
                }// if result_ok
            }// if requestcode
        }// if image

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }
            rated_bitmap = rotate(bitmap, exifDegree);
            waste_bitmap = rated_bitmap;
            bitmap2 = waste_bitmap;
            image_send(waste_bitmap);
        }
    }
    public void image_send(Bitmap waste_bitmap) {
        SharedPreferences sp = getSharedPreferences("profile", Activity.MODE_PRIVATE);
        String user_id = sp.getString("token", "");

        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar time = Calendar.getInstance();
        String format_time1 = format1.format(time.getTime());

        String file_name = format_time1 + user_id + ".jpg";
        waste_bitmap = PostWriteActivity.bitmap_resize(waste_bitmap);
        files = encodeTobase64(waste_bitmap);

        int area_no = 1;
        JSONObject jo_data = new JSONObject();

        send_data = "{\"area_no\":1, \"file_name\": \""+file_name+"\",\"files\":\""+files+"\"}";

        http_task http_task = new http_task("select_waste_type");
        http_task.execute();
        // 이미지 표시
        Glide.with(this).load(waste_bitmap).into(waste_ImageView);
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
                                waste_textView.setText("이미지 검색중...");
                            }
                        });
                    }
                }).start();

                String str = "";
                String str_URL = "http://" + RequestHttpURLConnection.server_ip + ":" + RequestHttpURLConnection.server_port + "/" + sub_url + "/"; //select_waste_type
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
                String data = "data=" + send_data;

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
                deep_learning_answer = res;

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // runOnUiThread를 추가하고 그 안에 UI작업을 한다.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonArray = new JSONArray(deep_learning_answer);
                                String waste_name = "";
                                waste_name = jsonArray.getJSONObject(0).getString("waste_type_kor_name");
                                waste_textView.setText(waste_name);
                                next_button.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).start();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
        builder.setTitle("메인화면으로 돌아가시겠습니까?")        // 제목 설정
                .setMessage("현재 신청리스트가 없어집니다.")        // 메세지 설정
                .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                    public void onClick(DialogInterface dialog, int whichButton){
                        finish();
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        startActivity(intent);
                        //원하는 클릭 이벤트를 넣으시면 됩니다.
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정, 왼쪽 버튼입니다.
                    public void onClick(DialogInterface dialog, int whichButton){
                        //원하는 클릭 이벤트를 넣으시면 됩니다.
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();    // 알림창 띄우기
    }
}
