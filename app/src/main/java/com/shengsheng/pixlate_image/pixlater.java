package com.shengsheng.pixlate_image;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;


import com.shengsheng.pixlate_image.color_combine.color_combine;
import com.shengsheng.pixlate_image.color_combine.color_combine_exception;
import com.shengsheng.pixlate_image.color_combine.color_dif_method;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class pixlater {
    private static final String TAG = "Mainactivity->Pixlater";
    private Bitmap src_image;
    private int src_w, src_h;
    private boolean fixed_size;
    private int block_size;
    private int dest_w, dest_h;

    private int total_pixel = 0;
    private int handle_pixel = 0;

    public pixlater(Bitmap src_image, boolean fixed_size, int block_size, int dest_w, int dest_h) {
        this.src_image = src_image;
        this.fixed_size = fixed_size;
        this.block_size = block_size;
        this.dest_w = dest_w;
        this.dest_h = dest_h;
        src_h = src_image.getHeight();
        src_w = src_image.getWidth();
    }

    public Bitmap example(){
        //采样法压缩
        int ratio = 1;
        if(fixed_size){
            ratio = src_w/dest_w;
        }
        else{
            ratio = block_size;
        }
        Log.d(TAG, "采样法压缩ratio:"+ ratio);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ratio;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src_image.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] byteArray = bos.toByteArray();

        Bitmap result = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        return result;
    }

    public Bitmap matrix(){
        //双线性采样
        Log.d(TAG, "双线性采样");
        int image_w = src_w/block_size;
        int image_h = src_h/block_size;
        if(fixed_size){
            return createScaledBitmap(src_image, dest_w, dest_h, true);
        }
        else{
            return createScaledBitmap(src_image, image_w, image_h, true);
        }
    }

    public Bitmap python_keepscale(MainActivity.control_pixlate con_pix_thread){
        int pix_h = src_h/block_size;
        int pix_w = src_w/block_size;

        if(src_h%block_size > 0){
            pix_h = src_h/block_size +1;
        }
        if(src_w%block_size > 0){
            pix_w = src_w/block_size +1;
        }

        ArrayList<Color> pixel_store = new ArrayList<>();
        for(int i = 0; i < pix_w; i++){
            for(int j = 0; j < pix_h; j++){
                pixel_store.add(change_block(i,j,block_size));
                handle_pixel++;
                if(con_pix_thread.pix_isinterrupt()){
                    Log.d(TAG, "挖洞: " + con_pix_thread.pix_isinterrupt());
                    return null;
                }
            }
        }

        Bitmap pixlate_image = Bitmap.createBitmap(pix_w*block_size, pix_h*block_size, src_image.getConfig());

        int num = 0;
        for(int i =0; i< pix_w*block_size; i++){
            for(int j = 0; j< pix_h*block_size; j++){
                num = (i/block_size)*pix_h + (j/block_size);
                pixlate_image.setPixel(i,j,pixel_store.get(num).toArgb());
                //Log.d(TAG, "i:"+i+" j:"+j+" num:"+num);
                //Log.d("pixlater", "pixlate:" + pix_thred.isInterrupted());
                if(con_pix_thread.pix_isinterrupt()){
                    Log.d(TAG, "颜色合并: " + con_pix_thread.pix_isinterrupt());
                    return null;
                }
            }
        }
        return pixlate_image;
    }

    public Bitmap color_combine(int group_num, double color_difference, MainActivity.control_pixlate con_pix_thread){

        int minCluster = group_num;
        double maxDistance = color_difference;

        color_combine<Number> color_c = null;

        try {
            float[][][] input_Pixels = getImagePixel_onlycolor(con_pix_thread);
            if(input_Pixels == null){
                return null;
            }
            else{
                color_c = new color_combine<>(input_Pixels,minCluster, maxDistance, new color_dif_method(1), con_pix_thread);
            }
        } catch (color_combine_exception e1) {
            Log.d(TAG, "颜色合并： "+ "Should not have failed on instantiation: " + e1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        float[][][] result = null;

        try {
            result = color_c.get_color_group();
        } catch (color_combine_exception e) {
            Log.d(TAG, "颜色合并： "+ "Should not have failed while performing clustering: " + e);
        }

        if(result == null){
            return null;
        }

        Bitmap bi = Bitmap.createBitmap(src_w, src_h, Bitmap.Config.ARGB_8888);

        int total_for_check = 0;

        for(int i = 0; i < src_w; i++){
            for(int j = 0; j < src_h; j++){
                Color c = Color.valueOf(result[i][j][0], result[i][j][1], result[i][j][2], result[i][j][3]);
                bi.setPixel(i, j, c.toArgb());
                total_for_check++;
            }
        }

        Log.d(TAG, "total pixel: " + total_for_check);

        //clear memory quickly
        result = null;
        color_c = null;

        //kill the thread
        con_pix_thread.pix_interrupt();
        con_pix_thread.interrupt();

        System.gc();

        return bi;
    }

    public Bitmap image_cut_hole(int x_diff, int y_diff, int block_size, MainActivity.control_pixlate con_pix_thread) throws IOException {
        ArrayList<Color> pixel_list = new ArrayList<>();

        int x = 0,y = 0;
        boolean y_get = false;
        boolean x_get = false;

        for (int j = 0; j < src_h; j++){
            if(j%(y_diff+block_size) < y_diff){
                y_get = false;
            }
            else{
                y_get = true;
                y++;
            }
            for (int i = 0; i < src_w; i++) {
                if(i%(x_diff+block_size) < x_diff){
                    x_get = false;
                }
                else{
                    x_get = true;
                    if(j == 0){
                        x++;
                    }
                }
                Color pixel = src_image.getColor(i,j);
                if(x_get && y_get){
                    pixel_list.add(pixel);
                }
            }
            if(con_pix_thread.pix_isinterrupt()){
                Log.d(TAG, "挖洞并合并: " + con_pix_thread.pix_isinterrupt());
                return null;
            }
        }
        return create_image(x, y,pixel_list,"image_cut");
    }

    //help function

    public Bitmap createScaledBitmap(@NonNull Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Matrix m = new Matrix();

        final int width = src.getWidth();
        final int height = src.getHeight();

        if (width != dstWidth || height != dstHeight) {
            final float sx = dstWidth / (float) width;
            final float sy = dstHeight / (float) height;
            m.setScale(sx, sy);
        }
        return Bitmap.createBitmap(src, 0, 0, width, height, m, filter);
    }

    public Color change_block(int i, int j, int block_size){
        List<Pair<Integer, Integer>> loc_store = new ArrayList<>();
        HashMap<Color, Integer> color_count = new HashMap<Color, Integer>();
        Color max_appear = new Color();
        int max_count = 0;

        for(int x = 0; x < block_size ; x++ ){
            for(int y = 0; y < block_size; y++ ){
                int pix_w = i*block_size+x;
                int pix_h = j*block_size+y;
                if(pix_w >= src_w){
                    pix_w = src_w-1;
                }
                if(pix_h >= src_h){
                    pix_h = src_h-1;
                }

                Pair store_pair = new Pair(pix_w, pix_h);
                loc_store.add(store_pair);
            }
        }
        for(int k = 0; k < loc_store.size(); k++){
            Color store = src_image.getColor(loc_store.get(k).first, loc_store.get(k).second);
            if(color_count.containsKey(store)){
                color_count.put(store, color_count.get(store)+1);
            }else{
                color_count.put(store, 1);
            }
        }
        for(Color key : color_count.keySet()){
            int count = color_count.get(key);
            if(count > max_count){
                max_count = count;
                max_appear = key;
            }
        }

        return max_appear;
    }

    private ArrayList<Pair<int[],float[]>> getImagePixel(MainActivity.control_pixlate con_pix_thread) throws Exception {
        ArrayList<Pair<int[],float[]>> ans = new ArrayList<>();
        Log.d(TAG,"width=" + src_w + ",height=" + src_h + ".");
        for (int i = 0; i < src_w; i++) {
            for (int j = 0; j < src_h; j++) {
                int[] loc = new int[2];
                loc[0] = i;
                loc[1] = j;
                Color pixel = src_image.getColor(i,j);
                float[] color = new float[]{pixel.red(), pixel.green(), pixel.blue(),pixel.alpha()};
                Pair<int[], float[]> store = new Pair<>(loc, color);
                ans.add(store);
                if(con_pix_thread.pix_isinterrupt()){
                    return null;
                }
            }
            //Log.d(TAG, "get image pixel (i):"+ i);
        }
        Log.d(TAG, "get image pixel (get all pixels already !!!!): "+ ans.size());
        return ans;
    }

    private float[][][] getImagePixel_onlycolor(MainActivity.control_pixlate con_pix_thread) throws Exception {
        float[][][] ans = new float[src_w][src_h][4];
        Log.d(TAG,"width=" + src_w + ",height=" + src_h + ".");
        for (int i = 0; i < src_w; i++) {
            for (int j = 0; j < src_h; j++) {
                Color pixel = src_image.getColor(i,j);
                float[] color = new float[]{pixel.red(), pixel.green(), pixel.blue(),pixel.alpha()};
                ans[i][j] = color;
                if(con_pix_thread.pix_isinterrupt()){
                    return null;
                }
            }
            //Log.d(TAG, "get image pixel (i):"+ i);
        }

        Log.d(TAG, "get image pixel (get all pixels already !!!!): "+ ans.length*ans[0].length);
        return ans;
    }

    public Bitmap create_image(int w, int h, ArrayList<Color> color_list, String file_name) throws IOException {
        Log.d(TAG, "create image: ("+ "w:  "+w + "h:  "+h + ")");
        Bitmap bi = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int cur_w = 0;
        int cur_h = 0;
        for(int i =0; i< color_list.size(); i++){
            cur_w = i%w;
            bi.setPixel(cur_w, cur_h,color_list.get(i).toArgb());
            if(cur_w+1 == w){
                cur_h++;
            }
        }
        return bi;
    }

    public Bitmap python(){
        int pix_h = src_h/block_size;
        int pix_w = src_w/block_size;
        total_pixel = pix_h*pix_w;
        handle_pixel = 0;

        if(src_h%block_size > 0){
            pix_h = src_h/block_size +1;
        }
        if(src_w%block_size > 0){
            pix_w = src_w/block_size +1;
        }
        Bitmap pixlate_image = Bitmap.createBitmap(pix_w, pix_h, src_image.getConfig());

        for(int i = 0; i < pix_w; i++){
            for(int j = 0; j < pix_h; j++){
                pixlate_image.setPixel(i, j, change_block(i,j,block_size).toArgb());
                handle_pixel++;
            }
        }

        return pixlate_image;
    }


}
