package com.example.filezip2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import net.lingala.zip4j.core.ZipFile;

public class Zip4Util {
    public static final String TAG = "sb";
    public static  void addFile(String file ,String filename ,String oldFile, Handler handler){
        InputStream is = null;

        try {
            ZipFile zipFile = new ZipFile(oldFile);
            zipFile.setFileNameCharset("GBK");
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);//压缩方式
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//压缩级别
            parameters.setFileNameInZip(filename);//重命名
            parameters.setSourceExternalStream(true);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);//加密方式
            parameters.setPassword("ljj666");//设置密码
            is = new FileInputStream(file);//创建一个输入流的对象
            final ProgressMonitor progressMonitor = zipFile.getProgressMonitor();//利用progressmonitor监听zipfile的变化

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
                            Thread.sleep(10);
                            precentDone = progressMonitor.getPercentDone();
                            Log.i(TAG, String.valueOf(precentDone));
                            bundle = new Bundle();
                            bundle.putInt(CompressStatus.PERCENT, precentDone);
                            msg = new Message();
                            msg.what = CompressStatus.HANDLING;
                            msg.setData(bundle);
                            handler.sendMessage(msg); //通过 Handler将进度扔出去
                        } while (progressMonitor.getCurrentOperation() != -1);
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
            zipFile.setRunInThread(true);
            zipFile.addStream(is, parameters);//不通过压缩，通过流的方式添加文件到压缩包.


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
                        Thread.sleep(10);
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
        zfile.setRunInThread(true);
        zfile.extractAll(dest);
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

