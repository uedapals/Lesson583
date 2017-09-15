package jp.techacademy.takuro.ueda.lesson583;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private Cursor cursor;
    private ImageView imageview;
    private boolean mode;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //Android 6.0以降の場合
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M)

    {
        // パーミッションの許可状態を確認する
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo();
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
        // Android 5系以下の場合
    } else

    {
        getContentsInfo();
    }

        imageview = (ImageView)findViewById(R.id.imageView);

        final Button mPrevButton = (Button) findViewById(R.id.button1);

        final Button mNextButton = (Button) findViewById(R.id.button2);

        final Button mStartStopButton = (Button) findViewById(R.id.button3);

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor == null){
                    Toast.makeText(getApplicationContext(), "パーミッションが拒否されているので，アプリを終了します。", Toast.LENGTH_LONG).show();
                    finish();
                }else {

                    if (cursor.moveToPrevious()) {
                    } else {
                        cursor.moveToLast();
                    }
                    setImageview();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor == null) {
                    Toast.makeText(getApplicationContext(), "パーミッションが拒否されているので，アプリを終了します。", Toast.LENGTH_LONG).show();
                    finish();
                }else {

                    if (cursor.moveToNext()) {
                    } else {
                        cursor.moveToFirst();
                    }
                    setImageview();
                }
            }
        });

        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("ANDROID","StartStop");
                //ボタン制御（戻る・進む）
                if (cursor == null) {
                    Toast.makeText(getApplicationContext(), "パーミッションが拒否されているので，アプリを終了します。", Toast.LENGTH_LONG).show();
                    finish();
                }else {

                    if (mode) {
                        mStartStopButton.setText("再生");
                        mNextButton.setEnabled(true);
                        mPrevButton.setEnabled(true);
                        mode = false;
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                    } else {
                        //自動再生開始
                        mStartStopButton.setText("停止");
                        mNextButton.setEnabled(false);
                        mPrevButton.setEnabled(false);
                        mode = true;

                        // タイマーの作成
                        mTimer = new Timer();
                        // タイマーの始動
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (cursor.moveToNext()) {
                                        } else {
                                            cursor.moveToFirst();
                                        }
                                        setImageview();
                                    }
                                });
                            }
                        }, 2000, 2000);
                    }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

    }

    private void setImageview (){
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        imageview.setImageURI(imageUri);
    }
}


/*import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import java.util.Timer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import java.util.TimerTask;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Uri imageUri;
    Timer mTimer;
    ImageView mTimerText;
    double mTimerSec = 0.0;

    //イメージＩＤ格納
    long[] imageId;
    //取得イメージ最大数
    int imageIdCountMax;
    //現在表示イメージ
    int imageCountNow = -1;
    //自動再生フラグ 自動再生=True

    boolean mode = false;

    Handler mHandler = new Handler();
    Cursor cursor;

    Button mPrevButton;
    Button mNextButton;
    Button mStartStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    //Log.d("ANDROID","許可されなかった");
                    //許可されなかった場合の処理
                    new AlertDialog.Builder(this)
                            .setTitle("許可されなかったのでこのアプリは利用できません")
                            .setPositiveButton( "完了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("ANDROID",String.valueOf(which));
                                    finish();
                                }
                            }).show();
                }

                break;
            default:
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getContentsInfo();
    }

    //タイマー
    private void timerMode(boolean m,int second){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask(){
            @Override
            public void run(){
                mHandler.post(new Runnable(){
                    public void run(){
                        imageSet(1);
                    }
                });
            }
        }, 0, second);
    }

    /    (boolean m){
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //イメージ表示
    private void imageSet(int cnt){
        //イメージ現在値　増減
        imageCountNow=imageCountNow+cnt;
        if (imageCountNow >= imageIdCountMax){
            imageCountNow=0;
        } else if (imageCountNow < 0){
            imageCountNow = imageIdCountMax-1;
        }

        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId[imageCountNow]);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);


    }

    int result[]= new int[2];

    result[0]= URI 0;
    result[1]= URI 1;
    result[2]= URI 2;

    private void getContentsInfo() {

            // 画像の情報を取得する
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
            );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                Log.d("ANDROID", "URI : " + imageUri.toString());
            } while (cursor.moveToNext());
        }
        cursor.close();
        //ループ処理、URIやArraylist、ImageCountNowで番号付け

        imageCountNow=0;
        imageSet(0);

    }
}

//リスト作る


    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Uri imageUri;
    Timer mTimer;
    ImageView mTimerText;
    double mTimerSec = 0.0;

    Handler mHandler = new Handler();
    Cursor cursor;

    Button mStartButton;
    Button mPauseButton;
    Button mResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
        mStartButton = (Button) findViewById(R.id.button1);
        mStartButton.setOnClickListener(this);

        mPauseButton = (Button) findViewById(R.id.button2);
        mPauseButton.setOnClickListener(this);

        mResetButton = (Button) findViewById(R.id.button3);
        mResetButton.setOnClickListener(this);

    }
    @Override
     public void onClick(View v){
        if(v.getId()==R.id.button1) {
            //ボタン①の処理
            if (cursor.moveToFirst()) {
                do {
                    // indexからIDを取得し、そのIDから画像のURIを取得する
                    int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    Long id = cursor.getLong(fieldIndex);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        if (v.getId()==R.id.button2){
            if (cursor.move)
        }

     @Override
         public void onRequestPermissionsResult ( int requestCode, String permissions[],
         int[] grantResults){
         switch (requestCode) {
         case PERMISSIONS_REQUEST_CODE:
         if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContentsInfo();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    }
                }

     private void getContentsInfo () {
          // 画像の情報を取得する
          ContentResolver resolver = getContentResolver();
          cursor = resolver.query(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
              null, // 項目(null = 全項目)
              null, // フィルタ条件(null = フィルタなし)
              null, // フィルタ用パラメータ
              null // ソート (null ソートなし)
                    );

                }
}
*/