package com.example.filezip2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import net.lingala.zip4j.core.ZipFile;

public class Zip4Util {
    public static final String TAG = "sb";
    /*
    *添加文件夹到压缩包
    * folder 文件夹路径 destzipfile 压缩包路径 password 密码
     */
    public static void doZipfile(File folder, String destZipFile, String password) {
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(password);
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            zipFile.setFileNameCharset("GBK");
            zipFile.addFolder(folder, parameters);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
        }
    }
    /*
     *添加文件夹到压缩包第二种方法，其实还有第三种方法还不会报错，以流的方式添加，不过是一个个文件添加，没有文件夹。
     * folder 文件夹路径 dest 压缩包路径 password 密码
     */
    public static void AddFolder(String folder, String dest, String password) {
        ZipFile zipFile = null ;
        try {
            zipFile = new ZipFile(dest);
            zipFile.setFileNameCharset("GBK");
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            if (password != null) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                parameters.setPassword(password);
            }
            zipFile.addFolder(new File(folder), parameters);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
        }
    }

    /*
    *如果file是文件夹，使用如下方案
    * 遍历该文件夹，是文件就添加到数组，是文件夹就重复遍历
    * Arraylist<File> a=new Arraylist<File>();
    * file 文件路径 oldfile 压缩包路径 password 密码 handler 与主函数联系形成进度条
     */
    public  static void addFile(String file ,String oldFile,String password , Handler handler){
        InputStream is = null;
//        readFile(new File(file));
        try {
            ZipFile zipFile = new ZipFile(oldFile);
            zipFile.setFileNameCharset("GBK");
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);//压缩方式
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//压缩级别
            parameters.setFileNameInZip(file.substring(file.lastIndexOf("/")+1));
            parameters.setSourceExternalStream(true);
            if (password != null){
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);//加密方式
                parameters.setPassword(password.toCharArray());//设置密码
            }
//            if (password != null){
//                zipFile.addFolder("/vr/sb",parameters);//添加文件夹"/vr/sb"
//            }
           // zipFile.addFolder("/vr/sb",parameters);//添加文件夹"/vr/sb"
            is = new FileInputStream(file);//创建一个输入流的对象
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Bundle bundle = null;
                    Message msg = null;
                    try
                    {
                        int precentDone = 0;
                        if (handler == null)
                        {
                            return;
                        }
                        handler.sendEmptyMessage(CompressStatus.START);
                        do {
                            // 每隔50ms,发送一个进度出去
                            Thread.sleep(50);
                            precentDone +=2;
                            bundle = new Bundle();
                            bundle.putInt(CompressStatus.PERCENT, precentDone);
                            msg = new Message();
                            msg.what = CompressStatus.HANDLING;
                            msg.setData(bundle);
                            handler.sendMessage(msg); //通过 Handler将进度扔出去
                        } while (precentDone<99);
                            Thread.sleep(4000);
                            handler.sendEmptyMessage(CompressStatus.COMPLETED);
                    }
                    catch (InterruptedException e)
                    {
                        bundle = new Bundle();
                        bundle.putString(CompressStatus.ERROR_COM, e.getMessage());
                        msg = new Message();
                        msg.what = CompressStatus.ERROR;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    } finally {
                        Log.i("666", "爬");

                    }

                }
            });//资源调用结束失败
            thread.start();
            long startTime2 = System.currentTimeMillis();
            zipFile.addStream(is, parameters);//不通过压缩，通过流的方式添加文件到压缩包.
            long consumingTime2 = (System.currentTimeMillis()- startTime2);
            ToastUtil.showToast("导入"+consumingTime2+"毫秒");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*
    *获取文件的文件名，用list集合存储
    * dest 压缩包路径
     */
    public static  ArrayList<String> fileEntry(String dest) throws IOException {
        ArrayList<String> sum=new ArrayList<>();
        InputStream in = new BufferedInputStream(new FileInputStream(dest));
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry sb =null;
        try {
               while((sb = zin.getNextEntry())!= null) {
                       sum.add(sb.getName());
               }
            in.close();
            zin.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
      return  sum;
    }
    /**
     * 压缩
     * @param srcFile 文件或文件夹目录
     * @param dest 压缩包目录
     * @param passwd 密码
     * @throws ZipException 抛出异常
     */
    public static void zip(String srcFile, String dest, String passwd) throws ZipException, net.lingala.zip4j.exception.ZipException {

        File srcfile = new File(srcFile);

        //创建目标文件
        String destname = buildDestFileName(srcfile, dest);
        ZipParameters par = new ZipParameters();//新建对象
        par.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);//压缩方式
        par.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//压缩级别

        if (passwd != null)
        {
            par.setEncryptFiles(true);
            par.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);//加密方式
            par.setPassword(passwd.toCharArray());//将字符串转换成字符数组
        }

        ZipFile zipfile = new ZipFile(destname);

        if (srcfile.isDirectory())
        {
            zipfile.addFolder(srcfile, par);
        }
        else
        {
            zipfile.addFile(srcfile, par);
        }
    }
/*
*这个没啥用
 */
    public static  void testFile(String dest,Handler handler) throws IOException, net.lingala.zip4j.exception.ZipException {

        ZipFile zipFile = new ZipFile(dest);
        zipFile.setFileNameCharset("GBK");
        ArrayList<File> list= new ArrayList<>();
        list.add(new File("/vr/2.jpeg"));
        list.add(new File("/vr/3.jpeg"));

        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);//压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//压缩级别
        parameters.setSourceExternalStream(true);

        parameters.setEncryptFiles(true);//设置密码
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);//加密方式
        parameters.setPassword("ljj666");

        final ProgressMonitor progressMonitor =zipFile.getProgressMonitor();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Bundle bundle = null;
                Message msg = null;
                try
                {
                    int precentDone;
                    if (handler == null)
                    {
                        return;
                    }
                    handler.sendEmptyMessage(CompressStatus.START);
                    do {
                        // 每隔50ms,发送一个进度出去
                        Thread.sleep(50);
                        precentDone = progressMonitor.getPercentDone();
                        Log.i(TAG, String.valueOf(precentDone));
                        bundle = new Bundle();
                        bundle.putInt(CompressStatus.PERCENT, precentDone);
                        msg = new Message();
                        msg.what = CompressStatus.HANDLING;
                        msg.setData(bundle);
                        handler.sendMessage(msg); //通过 Handler将进度扔出去
                    } while (precentDone<100);
                    if(progressMonitor.getCurrentOperation() != -1){
                        handler.sendEmptyMessage(CompressStatus.COMPLETED);
                    }
                }
                catch (InterruptedException e)
                {
                    bundle = new Bundle();
                    bundle.putString(CompressStatus.ERROR_COM, e.getMessage());
                    msg = new Message();
                    msg.what = CompressStatus.ERROR;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    e.printStackTrace();
                } finally {
                    Log.i("666", "爬");

                }

            }
        });//资源调用结束失败
        thread.start();
        zipFile.setRunInThread(true);
        zipFile.addFiles(list,parameters);
    }
    /**
     * 解压
     * @param zipfile 压缩包文件
     * @param dest 目标文件
     * @param passwd 密码
     * @throws ZipException 抛出异常
     */
    public static void unZip(String zipfile, String dest, String passwd,Handler handler) throws ZipException, net.lingala.zip4j.exception.ZipException {
        ZipFile zfile = new ZipFile(zipfile);
        if (!zfile.isValidZipFile())
        {
            throw new ZipException("压缩文件不合法，可能已经损坏！");
        }

        File file = new File(dest);
        if (file.isDirectory() && !file.exists())
        {
            file.mkdirs();
        }

        if (zfile.isEncrypted())
        {
            zfile.setPassword(passwd.toCharArray());
        }
        final ProgressMonitor progressMonitor = zfile.getProgressMonitor();//利用progressmonitor监听zipfile的变化

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Bundle bundle = null;
                Message msg = null;
                try
                {
                    int precentDone;
                    if (handler == null)
                    {
                        return;
                    }
                    handler.sendEmptyMessage(CompressStatus.START);
                    do {
                        // 每隔50ms,发送一个进度出去
                        Thread.sleep(50);
                        precentDone = progressMonitor.getPercentDone();
                        Log.i(TAG, String.valueOf(precentDone));
                        bundle = new Bundle();
                        bundle.putInt(CompressStatus.PERCENT, precentDone);
                        msg = new Message();
                        msg.what = CompressStatus.HANDLING;
                        msg.setData(bundle);
                        handler.sendMessage(msg); //通过 Handler将进度扔出去
                    } while (precentDone<100);
                    if(progressMonitor.getCurrentOperation() != -1){
                        handler.sendEmptyMessage(CompressStatus.COMPLETED);
                    }
                }
                catch (InterruptedException e)
                {
                    bundle = new Bundle();
                    bundle.putString(CompressStatus.ERROR_COM, e.getMessage());
                    msg = new Message();
                    msg.what = CompressStatus.ERROR;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    e.printStackTrace();
                } finally {
                    Log.i("666", "爬");

                }

            }
        });//资源调用结束失败
        long startTime = System.currentTimeMillis();
        thread.start();
        zfile.setRunInThread(true);
        zfile.extractAll(dest);
        long consumingTime = (System.currentTimeMillis()- startTime);
        ToastUtil.showToast("解压"+consumingTime+"毫秒");
    }


    public static String buildDestFileName(File srcfile, String dest)
    {
        if (dest == null)
        {
            if (srcfile.isDirectory())
            {
                dest = srcfile.getParent() + File.separator + srcfile.getName() + ".zip";
            }
            else
            {
                String filename = srcfile.getName().substring(0, srcfile.getName().lastIndexOf("."));
                dest = srcfile.getParent() + File.separator + filename + ".zip";
            }
        }
        else
        {
            createPath(dest);//路径的创建
            if (dest.endsWith(File.separator))
            {
                String filename = "";
                if (srcfile.isDirectory())
                {
                    filename = srcfile.getName();
                }
                else
                {
                    filename = srcfile.getName().substring(0, srcfile.getName().lastIndexOf("."));
                }
                dest += filename + ".zip";
            }
        }
        return dest;
    }

    private static void createPath(String dest)
    {
        File destDir = null;
        if (dest.endsWith(File.separator))
        {
            destDir = new File(dest);//给出的是路径时
        }
        else
        {
            destDir = new File(dest.substring(0, dest.lastIndexOf(File.separator)));
        }

        if (!destDir.exists())
        {
            destDir.mkdirs();
        }
    }
}

