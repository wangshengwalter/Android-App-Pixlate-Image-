package com.shengsheng.pixlate_image;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //global parameter
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "mainactivity";
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    //UI

    //first stage
    public ImageView imageView = null;
    public BottomNavigationView bottom_tool_bar = null;
    public ScrollView info_scroll = null;
    //parameter in 1 stage
    public boolean tool_bar_state = true;
    public boolean info_scroll_state = false;
    public ArrayList<Bitmap> image_version = new ArrayList<>();

    //second stage
    public HorizontalScrollView scrollView = null;
    public ImageButton sample = null;
    public ImageButton matrix = null;
    public ImageButton python = null;
    public ImageButton color_comb = null;
    public ImageButton image_hole_cut = null;

    public LinearLayout cancel_save_Layout = null;
    public TextView cancel = null;
    public ImageButton back = null;
    public ImageButton forward = null;
    public TextView save = null;
    //file store loc
    String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";

    //parameter in 2 stage
    public int cur_method = -1;
    public int image_index = 0;

    //third stage
    public LinearLayout parameter_layout = null;
    public Switch fixed_pixlated_image = null;
    public LinearLayout height_layout = null;
    public EditText h_et;
    public LinearLayout width_layout = null;
    public EditText w_et;
    public LinearLayout block_layout = null;
    public EditText blocksize_et;
    public LinearLayout color_diff_layout = null;
    public SeekBar color_diff_sb = null;
    public TextView color_diff_tv = null;
    public LinearLayout block_distance_layout = null;
    public EditText x_spacing_et = null;
    public EditText y_spacing_et = null;

    public LinearLayout delete_okey_Layout = null;
    public ImageButton delete = null;
    public TextView generate = null;
    public ImageButton okey = null;

    //parameter in 3 stage
    public boolean fixed_size = false;
    public int dest_h = 100;
    public int dest_w = 100;
    public int block_size = 10;
    public double color_range = 0.0;
    public int x_spacing = 10;
    public int y_spacing = 10;
    public boolean suspended = false;
    //store the generate image in short time
    public Bitmap new_image = null;


    //global parameter
    public ProgressDialog mpDialog;
    public Handler handler = null;
//    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);

        //first stage
        imageView = findViewById(R.id.imageview_main);
        bottom_tool_bar = findViewById(R.id.nav_view);   //用来做触碰图片就弹出来tool bar
        info_scroll = findViewById(R.id.info_scrollview);

        //second stage
        scrollView = findViewById(R.id.scrollview);
        sample = findViewById(R.id.sample);
        matrix = findViewById(R.id.matrix);
        python = findViewById(R.id.python);
        color_comb = findViewById(R.id.color_combine);
        image_hole_cut = findViewById(R.id.image_cut_hole);

        cancel_save_Layout = findViewById(R.id.cancel_save_Layout);
        cancel = findViewById(R.id.cancel);
        back = findViewById(R.id.return_button);
        forward = findViewById(R.id.forward_button);
        save = findViewById(R.id.save);

        //third stage
        parameter_layout = findViewById(R.id.parameter_linearLayout);
        fixed_pixlated_image = findViewById(R.id.switch1);
        height_layout = findViewById(R.id.height_layout);
        h_et = findViewById(R.id.dest_h);
        width_layout = findViewById(R.id.width_layout);
        w_et = findViewById(R.id.dest_w);
        block_layout = findViewById(R.id.block_size_layout);
        blocksize_et = findViewById(R.id.block_size);
        color_diff_layout = findViewById(R.id.color_diff_layout);
        color_diff_sb = findViewById(R.id.color_diff_sb);
        color_diff_tv = findViewById(R.id.color_diff_tv);
        block_distance_layout = findViewById(R.id.block_distance_layout);
        x_spacing_et = findViewById(R.id.x_dist_et);
        y_spacing_et = findViewById(R.id.y_dist_et);

        delete_okey_Layout = findViewById(R.id.delete_okey_Layout);
        delete = findViewById(R.id.delete_button);
        generate = findViewById(R.id.generate);
        okey = findViewById(R.id.okey_button);


        //global parameter
        mpDialog = new ProgressDialog(MainActivity.this);
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mpDialog.setTitle("In Progress");
        mpDialog.setMessage("Processing...");
        mpDialog.setIndeterminate(true);


        //first stage go
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.test4));
        info_scroll.setVisibility(View.GONE);
        //second stage sleep
        scrollView.setVisibility(View.GONE);
        cancel_save_Layout.setVisibility(View.GONE);
        //third stage sleep
        parameter_layout.setVisibility(View.GONE);
        delete_okey_Layout.setVisibility(View.GONE);


        //first stage handle
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(tool_bar_state){
                    bottom_tool_bar.setVisibility(View.GONE);
                    tool_bar_state = false;
                }
                else{
                    bottom_tool_bar.setVisibility(View.VISIBLE);
                    tool_bar_state = true;
                }
            }
        });


        Animation anim=new AnimationUtils().loadAnimation(this,R.anim.anime);
        anim.setFillAfter(true);

        bottom_tool_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                switch(item.getItemId()) {
                    case R.id.image_selector:
                        info_scroll.setVisibility(View.GONE);
                        info_scroll_state=false;

                        startActivityForResult(intent,3);
                        return true;
                    case R.id.edit:
                        info_scroll.setVisibility(View.GONE);
                        info_scroll_state=false;

                        //check whether choose the image to edit
                        if(image_version.size() == 0){
                            //if don choose image, please choose the image\
                            Toast.makeText(MainActivity.this, "Please choose the image you want to pixlate", Toast.LENGTH_LONG).show();
                            startActivityForResult(intent,3);
                        }
                        else{
                            //end first stage
                            imageView.setClickable(false);
                            bottom_tool_bar.setVisibility(View.GONE);
                            imageView.startAnimation(anim);

                            //start second stage
                            scrollView.setVisibility(View.VISIBLE);
                            cancel_save_Layout.setVisibility(View.VISIBLE);
                        }
                        return true;
                    case R.id.info:
                        if(info_scroll_state){
                            info_scroll.setVisibility(View.GONE);
                            info_scroll_state = false;
                        }
                        else{
                            info_scroll.setVisibility(View.VISIBLE);
                            info_scroll_state = true;
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        Animation anim_larger=new AnimationUtils().loadAnimation(this,R.anim.anime_larger);
        anim.setFillAfter(true);


        //second stage handle..................................................................................................................................................................
        sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_image_version(image_index);
                //end second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
                //start third stage
                show_fixed_image_choice();
                parameter_layout.setVisibility(View.VISIBLE);
                delete_okey_Layout.setVisibility(View.VISIBLE);
                //cur method change
                cur_method = 1;
            }
        });
        matrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_image_version(image_index);
                //end second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
                //start third stage
                show_fixed_image_choice();
                parameter_layout.setVisibility(View.VISIBLE);
                delete_okey_Layout.setVisibility(View.VISIBLE);
                //cur method change
                cur_method = 2;
            }
        });
        python.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_image_version(image_index);
                //end second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
                //start third stage
                show_only_block();
                parameter_layout.setVisibility(View.VISIBLE);
                delete_okey_Layout.setVisibility(View.VISIBLE);
                //cur method change
                cur_method = 3;
            }
        });
        color_comb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_image_version(image_index);
                //end second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
                //start third stage
                show_only_color_diff();
                parameter_layout.setVisibility(View.VISIBLE);
                delete_okey_Layout.setVisibility(View.VISIBLE);
                //cur method change
                cur_method = 4;
            }
        });
        image_hole_cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_image_version(image_index);
                //end second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
                //start third stage
                show_block_spacing();
                parameter_layout.setVisibility(View.VISIBLE);
                delete_okey_Layout.setVisibility(View.VISIBLE);
                //cur method change
                cur_method = 5;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                if(image_index > 0){
                    image_index--;
                    Log.d(TAG, "curr index: "+ image_index);
                    imageView.setImageBitmap(image_version.get(image_index));
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                if(image_index < image_version.size()-1){
                    image_index++;
                    Log.d(TAG, "curr index: "+ image_index);
                    imageView.setImageBitmap(image_version.get(image_index));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //TODO 如果size > 0; 询问是否需要保存
                image_index = 0;
                clear_image_version(0);
                imageView.setImageBitmap(image_version.get(0));

                //start first stage
                imageView.setClickable(true);
                bottom_tool_bar.setVisibility(View.VISIBLE);
                imageView.startAnimation(anim_larger);

                //finish second stage
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start first stage
                imageView.setClickable(true);
                bottom_tool_bar.setVisibility(View.VISIBLE);
                imageView.startAnimation(anim_larger);

                /* finish second stage */
                scrollView.setVisibility(View.GONE);
                cancel_save_Layout.setVisibility(View.GONE);

                /* store the cur file */
                try {
                    saveImage(MainActivity.this, image_version.get(image_index), "Pixlated Image", generate_filename());
                    Toast.makeText(MainActivity.this, "Saved successfully.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ":( Can not save. Please report bug.", Toast.LENGTH_LONG).show();
                }
                image_version.clear();
                image_index = 0;
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.test4));
            }
        });


        /* third stage handle.................................................................................................................................................................. */
        fixed_pixlated_image.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(fixed_pixlated_image.isChecked()){
                    fixed_size = true;
                    height_layout.setVisibility(View.VISIBLE);
                    width_layout.setVisibility(View.VISIBLE);
                    block_layout.setVisibility(View.GONE);
                }
                else{
                    fixed_size = false;
                    height_layout.setVisibility(View.GONE);
                    width_layout.setVisibility(View.GONE);
                    block_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        h_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(h_et.getText());
                    dest_h = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    Log.e(TAG, "can not get the destination value of height of pixel image" + ex.getMessage());
                }
            }
        });
        w_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(w_et.getText());
                    dest_w = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    Log.e(TAG, "can not get the destination value of width of pixel image" + ex.getMessage());
                }
            }
        });

        blocksize_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(blocksize_et.getText());
                    block_size = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    Log.e(TAG, "can not get the block size" + ex.getMessage());
                }
            }
        });
        color_diff_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                color_range = (double)progress/100.0;
                color_diff_tv.setText("Color combine range: " + Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        x_spacing_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String x_spacing_string = String.valueOf(x_spacing_et.getText());
                    x_spacing = Integer.parseInt(x_spacing_string);
                }catch (Exception ex){
                    Log.e(TAG, "can not get the x spacing" + ex.getMessage());
                }
            }
        });
        y_spacing_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String y_spacing_string = String.valueOf(y_spacing_et.getText());
                    y_spacing = Integer.parseInt(y_spacing_string);
                }catch (Exception ex){
                    Log.e(TAG, "can not get the y spacing" + ex.getMessage());
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start second stage
                scrollView.setVisibility(View.VISIBLE);
                cancel_save_Layout.setVisibility(View.VISIBLE);

                /* end third stage */
                parameter_layout.setVisibility(View.GONE);
                delete_okey_Layout.setVisibility(View.GONE);

                imageView.setImageBitmap(image_version.get(image_version.size()-1));
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View view) {
                Log.d(TAG, "select generate");
                suspended = false;

                pixlater pix_class = new pixlater(image_version.get(image_version.size()-1), false, block_size, dest_w, dest_h);

                control_pixlate con_pix = new control_pixlate();
                new Thread(con_pix, "control p T").start();

                handle_image pixlate_thread = new handle_image(pix_class, con_pix);
                new Thread(pixlate_thread, "p T").start();

                mpDialog.show();
                handler = new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        mpDialog.dismiss();
                    }
                };
                mpDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(MainActivity.this, "The processing has been canceled.", Toast.LENGTH_LONG).show();
                        con_pix.pix_interrupt();
                        con_pix.interrupt();
                        suspended = true;
                        Log.d(TAG, " pix: "+con_pix.pix_isinterrupt() + " con_pix: "+con_pix.isinterrupt());
                    }
                });

            }
        });

        okey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start second stage
                scrollView.setVisibility(View.VISIBLE);
                cancel_save_Layout.setVisibility(View.VISIBLE);

                //end third stage
                parameter_layout.setVisibility(View.GONE);
                delete_okey_Layout.setVisibility(View.GONE);

                if(new_image != null){
                    if(!suspended){
                        Log.d(TAG, "okey button: the processing did not suspende");
                        image_version.add(new_image);
                        image_index = image_version.size()-1;
                    }
                    else{
                        Log.d(TAG, "okey button: the processing suspended");
                        imageView.setImageBitmap(image_version.get(image_version.size()-1));
                        image_index = image_version.size()-1;
                    }
                    //try to kill the trash memory
                    System.gc();
                }
            }
        });
    }



    public class control_pixlate implements  Runnable {
        boolean pix_flag = false;
        boolean flag = false;

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while(!flag){
            }
        }

        public synchronized void pix_interrupt() {
            pix_flag = true;
        }
        public synchronized boolean pix_isinterrupt() {
            return pix_flag;
        }

        public synchronized void interrupt() {
            flag = true;
        }
        public synchronized boolean isinterrupt() {
            return flag;
        }
    }


    public class handle_image implements Runnable {
        pixlater pix_class = null;
        control_pixlate con_pix = null;

        public handle_image(pixlater pix_class, control_pixlate con_pix) {
            set_method(pix_class);
            set_con_pix(con_pix);
        }

        private void set_con_pix(control_pixlate con_pix) {
            this.con_pix = con_pix;
        }
        private void set_method(pixlater class_name) {
            pix_class = class_name;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try{
                //process the image, before the pd disappear
                switch(cur_method){
                    case 1:
                        new_image = pix_class.example();
                        break;
                    case 2:
                        new_image = pix_class.matrix();
                        break;
                    case 3:
                        new_image = pix_class.python_keepscale(con_pix);
                        break;
                    case 4:
                        new_image = pix_class.color_combine(2, color_range,con_pix);
                        break;
                    case 5:
                        new_image = pix_class.image_cut_hole(x_spacing,y_spacing,block_size,con_pix);
                        break;
                    default:
                        break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(new_image == null){
                            new_image = BitmapFactory.decodeResource(getResources(), R.drawable.suspended);
                        }
                        imageView.setImageBitmap(new_image);
                    }
                });
                handler.sendEmptyMessage(0);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    public void show_fixed_image_choice(){
        fixed_pixlated_image.setVisibility(View.GONE);
        height_layout.setVisibility(View.GONE);
        width_layout.setVisibility(View.GONE);
        block_layout.setVisibility(View.VISIBLE);
        color_diff_layout.setVisibility(View.GONE);
        block_distance_layout.setVisibility(View.GONE);
    }
    public void show_only_block(){
        fixed_pixlated_image.setVisibility(View.GONE);
        height_layout.setVisibility(View.GONE);
        width_layout.setVisibility(View.GONE);
        block_layout.setVisibility(View.VISIBLE);
        color_diff_layout.setVisibility(View.GONE);
        block_distance_layout.setVisibility(View.GONE);
    }
    public void show_only_color_diff(){
        fixed_pixlated_image.setVisibility(View.GONE);
        height_layout.setVisibility(View.GONE);
        width_layout.setVisibility(View.GONE);
        block_layout.setVisibility(View.GONE);
        color_diff_layout.setVisibility(View.VISIBLE);
        block_distance_layout.setVisibility(View.GONE);
    }
    public void show_block_spacing(){
        fixed_pixlated_image.setVisibility(View.GONE);
        height_layout.setVisibility(View.GONE);
        width_layout.setVisibility(View.GONE);
        block_layout.setVisibility(View.VISIBLE);
        color_diff_layout.setVisibility(View.GONE);
        block_distance_layout.setVisibility(View.VISIBLE);
    }
    public void clear_image_version(int index){
        Log.d(TAG, "index:" + index);
        ArrayList<Bitmap> store = new ArrayList<>();
        for(int i =0; i <= index; i++){
            store.add(image_version.get(i));
        }
        image_version = store;
    }

    public static String generate_filename(){
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return simpleDate.format(now.getTime());
    }

//    public void store_file(String dir, String file_name, Bitmap sel_image_bitmap){
//        String state = Environment.getExternalStorageState();
//        //如果状态不是mounted，无法读写
//        if (!state.equals(Environment.MEDIA_MOUNTED)) {
//            System.out.println("无法读写");
//        }
//        try {
//            File file = new File(dir + file_name + ".png");
//            FileOutputStream out = new FileOutputStream(file);
//            sel_image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            out.close();
//            //保存图片后发送广播通知更新数据库
//            Uri uri = Uri.fromFile(file);
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
//            Toast.makeText(MainActivity.this, "The image you choose has been saved", Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



    private Uri saveImage(Context context, Bitmap bitmap, @NonNull String folderName, @NonNull String fileName) throws IOException {
        OutputStream fos;
        File imageFile = null;
        Uri imageUri = null;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM" + File.separator + folderName);
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + folderName;
            imageFile = new File(imagesDir);
            if (!imageFile.exists()) {
                imageFile.mkdir();
            }
            imageFile = new File(imagesDir, fileName + ".png");
            fos = new FileOutputStream(imageFile);
        }

        boolean saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        if (imageFile != null)  // pre Q
        {
            MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, null);
            imageUri = Uri.fromFile(imageFile);
        }

        return imageUri;
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();
//
//                try {
//                    Bitmap sel_image_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
//                } catch (IOException e) {
//                    System.out.println("can not store the crop image");
//                }
//
//                imageView.setImageURI(resultUri);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                System.out.println("can not get the crop image");
//                Exception error = result.getError();
//                System.out.println(error);
//            }
//        }

        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            Uri sel_image_uri = data.getData();
            imageView.setImageURI(sel_image_uri);
            try {
                Bitmap sel_image_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), sel_image_uri);
                //add this image to image_list
                int h = sel_image_bitmap.getHeight();
                int w = sel_image_bitmap.getWidth();
                int image_size = h * w;
                if(image_size > 2600000){
                    float ratio = (float) Math.sqrt(2600000.0/image_size);
                    Matrix m = new Matrix();
                    m.setScale(ratio, ratio);
                    Log.d(TAG, "w:"+w+" h:"+h);
                    sel_image_bitmap = Bitmap.createBitmap(sel_image_bitmap, 0, 0, w, h, m, true);
                }
                image_version.clear();
                image_version.add(sel_image_bitmap);

            } catch (IOException e) {
                System.out.println("can not get image from URI");
            }

        }

    }



    public void call_dialog(String lost_msg){
        AlertDialog dialog = new AlertDialog.Builder(this)
                //.setIcon(R.mipmap.icon)
//                .setTitle("Lost Information")
                .setMessage("Give up your editing")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "click no", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Okey", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "click yes", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
}

