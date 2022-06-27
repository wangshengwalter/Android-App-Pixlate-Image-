package com.example.opencvproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {




    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String TAG = "mainactivity";
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    //UI
    public ImageView imageView = null;
    public TextView options;
    public TextView info;
    public Button generate;

    public RadioGroup radiogroup;

    public LinearLayout linearLayout;
        public LinearLayout linearLayout1;
            public TextView image_x;
            public TextView image_y;
        public LinearLayout linearLayout2;
            public EditText num_h;
            public EditText num_w;
        public LinearLayout linearLayout3;
            public TextView block_size_text;
            public EditText block_size_num;
    public Button sel_image;
    public Button okey_button;
    public Switch fixed_pixlated_image;


    //parameter
    public boolean sel_image_alr = false;
    public boolean fixed_size = false;

    public int method_index = 1;

    public Uri sel_image_uri = null;
    public Bitmap sel_image_bitmap = null;
    public Bitmap pixelate_image_bitmap = null;

    int h,w;
    public int pixel_image_h = 100;
    public int pixel_image_w = 100;
    public int block_size = 10;

    //file store loc
    String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.background));
        options = (TextView) findViewById(R.id.options);
        info = (TextView) findViewById(R.id.info);
        generate = (Button) findViewById(R.id.generate);


        //in linearLayout
        linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setVisibility(View.INVISIBLE);

        radiogroup = findViewById(R.id.radiogroup);
        linearLayout1 = findViewById(R.id.linearlayout1);
        linearLayout2 = findViewById(R.id.linearlayout2);
        image_x = findViewById(R.id.image_x);
        image_y = findViewById(R.id.image_y);
        num_h = (EditText) findViewById(R.id.nums_of_height);
        num_w = (EditText) findViewById(R.id.nums_of_width);
        linearLayout3 = findViewById(R.id.linearlayout3);
        block_size_text = findViewById(R.id.block_size_text);
        block_size_num = findViewById(R.id.block_size_num);

        sel_image = findViewById(R.id.sel_button);
        okey_button = findViewById(R.id.ok_button);
        fixed_pixlated_image = findViewById(R.id.switch1);




        info.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = null;
                        System.out.print("here..................1");
                        conn =(Connection) DBOpenHelper.getConn();
                        System.out.print("here..................2");
                        String sql = "SELECT * FROM products WHERE quantity_in_stock in (49,38,72)";
                        System.out.print(sql);
                        Statement st;
                        try {
                            st = (Statement) conn.createStatement();
                            ResultSet rs = st.executeQuery(sql);
                            while (rs.next()){

                                System.out.print("here..................");
                                //因为查出来的数据试剂盒的形式，所以我们新建一个javabean存储
                                Log.d(TAG,rs.getString(1));
                            }
                            st.close();
                            conn.close();
                        } catch ( SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });



        options.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selected = radiogroup.getCheckedRadioButtonId();
                switch(selected){
                    case R.id.radioButton1:
                        method_index = 1;
                        break;
                    case R.id.radioButton2:
                        method_index = 2;
                        break;
                    case R.id.radioButton3:
                        method_index = 3;
                        break;
                    case R.id.radioButton4:
                        method_index = 4;
                        break;
                }
            }
        });



        linearLayout1.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.VISIBLE);

        fixed_pixlated_image.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(fixed_pixlated_image.isChecked()){
                    fixed_size = true;
                    linearLayout1.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    linearLayout3.setVisibility(View.GONE);
                }
                else{
                    fixed_size = false;
                    linearLayout1.setVisibility(View.GONE);
                    linearLayout2.setVisibility(View.GONE);
                    linearLayout3.setVisibility(View.VISIBLE);
                }
            }
        });










        num_h.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pixel_image_h = 1;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pixel_image_h = 1;
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(num_h.getText());
                    pixel_image_h = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    System.out.println("can not get the value of height of pixel image");
                }
            }
        });

        num_w.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pixel_image_w = 1;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pixel_image_w = 1;
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(num_w.getText());
                    pixel_image_w = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    System.out.println("can not get the value of width of pixel image");
                }
            }
        });

        block_size_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                block_size = 10;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                block_size = 10;
            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String pixel_image_h_str = String.valueOf(block_size_num.getText());
                    block_size = Integer.parseInt(pixel_image_h_str);
                }catch (Exception ex){
                    System.out.println("can not get the value of width of pixel image");
                }
            }
        });


        sel_image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,3);

            }
        });

        okey_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sel_image_alr == false){
                    System.out.println("have not select the image");
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,3);
                }
                else{
                    linearLayout.setVisibility(View.INVISIBLE);
                    System.out.println("load image successfully"+" w: "+w+"h: "+h);
                }
            }
        });

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:       ");
                if(sel_image_alr == false){
                    System.out.println("have not select the image");
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,3);
                }
                //已经有图片
                else{
                    //是固定的图片大小
                    if(fixed_size){
                        Log.d(TAG, "generate onClick: detect fixed size");
                        if(w/h != pixel_image_w/pixel_image_h  || pixel_image_h/pixel_image_w != h/w){
                            System.out.println("ratio is not same"+" w: "+w+"  h: "+h);
                            //adjust the image
                            startCropActivity();
                        }
//                        else{
//                            System.out.println("ratio is same"+" w: "+w+" h: "+h+ " w/h:  "+ w/h + " goal ratio: "+ pixel_image_w/pixel_image_h);
//                            //pixel the image
//                            imageView.setImageBitmap(pixelate_image_bitmap);
//                        }
                        switch(method_index){
                            case 1:
                                pixlate_image_get_pixel_method(get_file_name());
                                break;
                            case 2:
                                pixlate_image_change_image_method(get_file_name());
                                break;
                            case 3:
                                pixlate_image_python(block_size);
                                break;
                            case 4:
                                break;
                            default:
                                break;
                        }

                        imageView.setImageBitmap(pixelate_image_bitmap);

                    }
                    else{
                        Log.d(TAG, "generate onClick: detect entire image  " + method_index);
                        switch(method_index){
                            case 1:
                                Log.d(TAG, "generate onClick: detect entire image: 11111111");
                                pixlate_image_get_pixel_method(get_file_name());
                                break;
                            case 2:
                                Log.d(TAG, "generate onClick: detect entire image: 22222222");
                                pixlate_image_change_image_method(get_file_name());
                                break;
                            case 3:
                                Log.d(TAG, "generate onClick: detect entire image: 33333333");
                                pixlate_image_python(block_size);
                                break;
                            case 4:
                                Log.d(TAG, "generate onClick: detect entire image: 44444444");
                                break;
                            default:
                                break;
                        }
                        imageView.setImageBitmap(pixelate_image_bitmap);
                    }
                }



            }
        });


    }

    public void pixlate_image_get_pixel_method(String bitmap_name){
//        store_file(dir,bitmap_name,sel_image_bitmap);
//        Log.d(TAG, "pixlate_image_get_pixel_method: ");
        //采样法压缩
        int ratio = block_size;
        if(fixed_size){
            ratio = w/pixel_image_w;
        }
        Log.d(TAG, "pixlate_image_get_pixel_method:   ratio:   "+ ratio);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ratio;

        Bitmap image = sel_image_bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] byteArray = bos.toByteArray();

        pixelate_image_bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
//        pixelate_image_bitmap = BitmapFactory.decodeFile(dir+bitmap_name+".jpg", options);
    }
    public void pixlate_image_change_image_method(String bitmap_name){
        int image_w = w/block_size;
        int image_h = h/block_size;
        if(fixed_size){
            pixelate_image_bitmap = createScaledBitmap(sel_image_bitmap, pixel_image_w, pixel_image_h, true);
        }
        else{
            pixelate_image_bitmap = createScaledBitmap(sel_image_bitmap, image_w, image_h, true);
        }

    }

    public Bitmap pixlate_image_python(int block_size){
        int pix_h = h/block_size;
        int pix_w = w/block_size;
        if(h%block_size > 0){
            pix_h = h/block_size +1;
        }
        if(w%block_size > 0){
            pix_w = w/block_size +1;
        }
        Bitmap pixlate_image = Bitmap.createBitmap(pix_w, pix_h, sel_image_bitmap.getConfig());

        for(int i = 0; i < pix_w; i++){
            for(int j = 0; j < pix_h; j++){
                pixlate_image.setPixel(i, j, change_block(i,j,block_size).toArgb());
            }
        }
        pixelate_image_bitmap = pixlate_image;
        return pixlate_image;
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
                if(pix_w >= w){
                    pix_w = w-1;
                }
                if(pix_h >= h){
                    pix_h = h-1;
                }

                Pair store_pair = new Pair(pix_w, pix_h);
                loc_store.add(store_pair);
            }
        }
        for(int k = 0; k < loc_store.size(); k++){
            Color store = sel_image_bitmap.getColor(loc_store.get(k).first, loc_store.get(k).second);
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

    public static String get_file_name(){
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String bitmap_name = simpleDate.format(now.getTime());
        return bitmap_name;
    }
    public void store_file(String dir, String file_name, Bitmap sel_image_bitmap){

        String state = Environment.getExternalStorageState();
        //如果状态不是mounted，无法读写
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("无法读写");
        }


        try {
            File file = new File(dir + file_name + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            sel_image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //缩小的办法
    public static Bitmap createScaledBitmap(@NonNull Bitmap src, int dstWidth, int dstHeight, boolean filter) {

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

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToLocal(Bitmap bitmap, String bitName) throws IOException {

        File file = new File(dir + bitName + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush();
                out.close();
                //保存图片后发送广播通知更新数据库
                // Uri uri = Uri.fromFile(file);
                // sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                this.sendBroadcast(intent);
                System.out.println("保存成功");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startCropActivity(){
        CropImage.activity(sel_image_uri)
            .setAspectRatio(pixel_image_w,pixel_image_h)
            .start(this);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                try {
                    sel_image_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    w=sel_image_bitmap.getWidth();
                    h=sel_image_bitmap.getHeight();
                } catch (IOException e) {
                    System.out.println("can not store the crop image");
                }

                imageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                System.out.println("can not get the crop image");
                Exception error = result.getError();
                System.out.println(error);
            }
        }

        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            sel_image_uri = data.getData();
            imageView.setImageURI(sel_image_uri);
            try {
                sel_image_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), sel_image_uri);
                sel_image_alr = true;
                w=sel_image_bitmap.getWidth();
                h=sel_image_bitmap.getHeight();
            } catch (IOException e) {
                System.out.println("can not get image from URI");
            }

        }

    }
}

