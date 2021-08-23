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
import android.widget.Button;
import android.widget.ProgressBar;
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

public class MainActivity extends Activity {
    private ProgressBar progressBar1;
    private TextView textView;
    private  Button btn;
    static List<Product> productList = new ArrayList<>();
    static List<Product> productList2 = new ArrayList<>();
    public final static String TAG = "666";
    @SuppressLint("HandlerLeak")
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.sb);
        btn = findViewById(R.id.add);
        progressBar1 = findViewById(R.id.probar);
        RecyclerView recyclerView1 = findViewById(R.id.names);
        MyAdapter adapter1 = new MyAdapter(productList);
        recyclerView1.setAdapter(adapter1);

        LinearLayoutManager layoutManagera = new LinearLayoutManager(this);
        recyclerView1.setLayoutManager(layoutManagera);
        layoutManagera.setOrientation(LinearLayoutManager.VERTICAL);


        int size = 0;
        try {
            size = Zip4Util.getSize("/vr/ASR/2.zip");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < size; i++) {
            Product a = null;
            try {
                a = new Product(Zip4Util.fileEntry("/vr/ASR/2.zip").get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            productList.add(a);
        }

        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Zip4Util.addFile("/vr/2.jpeg", "2.jpeg", "/vr/ASR/2.zip", null, handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                th2.start();
                Log.i(TAG, String.valueOf(productList2));
                int size = 0;
                try {
                    size = Zip4Util.getSize("/vr/ASR/2.zip");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < size; i++) {
                    Product a = null;
                    try {
                        a = new Product(Zip4Util.fileEntry("/vr/ASR/2.zip").get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    productList2.add(a);
                }
                setData();
            }

        });

    }
    public void setData(){
        RecyclerView recyclerView2 = findViewById(R.id.md5s);
        MyAdapter2 adapter2 = new MyAdapter2(productList2);
        recyclerView2.setAdapter(adapter2);
        LinearLayoutManager layoutManagerb = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(layoutManagerb);
        layoutManagerb.setOrientation(LinearLayoutManager.VERTICAL);
    }



}