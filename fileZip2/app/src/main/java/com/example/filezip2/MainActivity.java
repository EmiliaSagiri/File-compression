package com.example.filezip2;
import com.example.filezip2.ZipUtil.ZipListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class MainActivity extends Activity {
    private ProgressBar progressBar1;
    private TextView textView;
    private  Button btn;
    private  Button btn2;
    private static final String[] x={"/vr/1.jpeg","/vr/2.jpeg","/vr/3.jpeg","/vr/sb/sb.txt","/vr/sb/test.txt"};
    private static final String[] y={"1.jpeg","2.jpeg","3.jpeg","sb.txt","test.txt",};
    private TextView tv2 ;
    private TextView tv3 ;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    static List<Product> productList = new ArrayList<>();
    static List<Product> productList2 = new ArrayList<>();
    static ArrayList<String> list = new ArrayList<>();
    public final static String TAG = "666";
    public final static int LJJ = 1;
    public final static int SB = 2;
    @SuppressLint("HandlerLeak")
    /*
    *handler进度条
     */
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CompressStatus.START:
                    textView.setText("start!");
                    progressBar1.setVisibility(View.VISIBLE);
                    break;
                case CompressStatus.HANDLING:
                    Bundle b = msg.getData();
                    int percent = b.getInt(CompressStatus.PERCENT);
                    textView.setText(percent+ "%");
                    progressBar1.setProgress(percent);
//                    progressBar1.setProgress(Integer.parseInt(CompressStatus.PERCENT));
//                    progressBar1.setMax(100);
                    break;
                case CompressStatus.COMPLETED:
                    textView.setText("end!");
                    progressBar1.setVisibility(View.INVISIBLE);
                    break;
                case CompressStatus.ERROR:
                    Bundle bundle =msg.getData();
                    int error = bundle.getInt(String.valueOf(CompressStatus.ERROR));
                    textView.setText(error);
                    break;

            }
        };
    };
    /*
    *sb对应屏幕右边的recyclerview
    * ljj对应屏幕左边的recyclerview（添加前）
     */
    @SuppressLint("HandlerLeak")
    final Handler sb =new Handler() {
        public void handleMessage(Message msg2) {
            super.handleMessage(msg2);
            switch (msg2.what) {
                case LJJ:
                    try {
                        list = Zip4Util.fileEntry("/vr/test/a.zip");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Product a = null;
                        try {
                            a = new Product(list.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        productList2.add(a);
                    }
                    setData();
                    break;
                case SB:
                    try {
                        list = Zip4Util.fileEntry("/vr/test/a.zip");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Product b = null;
                        try {
                            b = new Product(list.get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        productList.add(b);
                    }
                    break;
            }

        };

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();//初始化
        TestThread thread1 = new TestThread();
        thread1.start();
        RecyclerView recyclerView1 = findViewById(R.id.names);
        MyAdapter adapter1 = new MyAdapter(productList);
        recyclerView1.setAdapter(adapter1);
        LinearLayoutManager layoutManagera = new LinearLayoutManager(this);
        recyclerView1.setLayoutManager(layoutManagera);
        layoutManagera.setOrientation(LinearLayoutManager.VERTICAL);
        View.OnClickListener myOnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                DownloadThread thread2 =new DownloadThread();
                switch (v.getId()) {
                    case R.id.add:
                        thread2.start();
                        productList2.clear();//清空数组，方便重复赋值
                        break;
                    case R.id.delete:
                        DeleteThread thread3 = new DeleteThread();
                        thread3.start();
                        productList2.clear();
                }
            }
        };
        btn.setOnClickListener(myOnClickListener);
        btn2.setOnClickListener(myOnClickListener);

    }
    /*
    *spinner控制下拉框，调用数组X 并给textview控件赋值
     */
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            tv2.setText(x[arg2]);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    /*
    *给第二个recyclerview赋值
     */
    public void setData(){
        RecyclerView recyclerView2 = findViewById(R.id.md5s);
        MyAdapter2 adapter2 = new MyAdapter2(productList2);
        recyclerView2.setAdapter(adapter2);
        LinearLayoutManager layoutManagerb = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(layoutManagerb);
        layoutManagerb.setOrientation(LinearLayoutManager.VERTICAL);
    }
    /*
    *给控件初始化
     */
    public void Initialize(){
        textView = findViewById(R.id.sb);
        btn = findViewById(R.id.add);
        btn2 = findViewById(R.id.delete);
        progressBar1 = findViewById(R.id.probar);
        tv2 = findViewById(R.id.Spinnertext);
        tv3 = findViewById(R.id.load);
        spinner = findViewById(R.id.hh);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,x);//设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//将adapter 添加到spinner中
        spinner.setAdapter(adapter);//添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());//设置默认值
        spinner.setVisibility(View.VISIBLE);
    }
    class DownloadThread extends Thread{
        public void run() {
                try {
                    DownloadThread.sleep(1000);
                    System.out.println("begin run");
                 Zip4Util.addFile(String.valueOf(tv2.getText()), "/vr/test/a.zip", null, handler);
                    //                  Zip4Util.AddFolder("vr/sb","vr/test/4.zip",null);
                   //                                   Zip4Util.zip("/vr/1.jpeg","/vr/test/a.zip",null);
                    Message message2 = new Message();
                    message2.what = LJJ;
                    sb.sendMessage(message2);

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }
    class DeleteThread extends Thread{

        public void run(){
            try {
                Zip4Util.deleteFile("/vr/test/a.zip", String.valueOf(tv2.getText()).substring(String.valueOf(tv2.getText()).lastIndexOf("/")+1));
                Message message2 = new Message();
                message2.what = LJJ;
                sb.sendMessage(message2);
            } catch (ZipException | net.lingala.zip4j.exception.ZipException e) {
                e.printStackTrace();
            }
        }

    }
    class TestThread extends Thread{

        public void run(){
            Message message1 = new Message();//利用handler传值到recyclerview
            message1.what=SB;
            sb.sendMessage(message1);
        }

    }
}