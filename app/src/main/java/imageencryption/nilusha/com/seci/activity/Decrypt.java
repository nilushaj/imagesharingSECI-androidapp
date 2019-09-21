package imageencryption.nilusha.com.seci.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import imageencryption.nilusha.com.seci.R;
import imageencryption.nilusha.com.seci.Sign.SignIn;

import static imageencryption.nilusha.com.seci.App.CHANNEL_1_ID;

// decrypt images from local storage
public class Decrypt extends AppCompatActivity implements View.OnClickListener{
    private Button galelry;

    private Button dec;
    private ImageView img;
    int total=0;
    int widthorg;
    int heightorg;
    private EditText txtkey;
    private FirebaseAuth firebaseAuth=null;
    private static final int PICK_IMAGE = 1;
    private Button undo;
    private Uri fileUri = null;
    private Button save;
    private NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        notificationManager = NotificationManagerCompat.from(this);
        galelry=(Button)findViewById(R.id.btngallery);
        dec=(Button)findViewById(R.id.btnencrypt);
        img=(ImageView) findViewById(R.id.image);
        txtkey=(EditText) findViewById(R.id.txtkey);
        save=(Button)findViewById(R.id.btndownload);
        undo=(Button)findViewById(R.id.btnUndo);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()== null){
            //close this activity
            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                Intent intent=new Intent(Decrypt.this,SignIn.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(),"please check your email and verify",Toast.LENGTH_LONG).show();
                firebaseAuth.getInstance().signOut();
            }
        }


        dec.setEnabled(false);
        txtkey.setEnabled(false);
        undo.setEnabled(false);
        save.setEnabled(false);
        galelry.requestFocus();
        galelry.setOnClickListener(this);

        dec.setOnClickListener(this);

        undo.setOnClickListener(this);
        save.setOnClickListener(this);


    }
    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.btngallery) {
            if (Build.VERSION.SDK_INT > 15) {
                askForPermissions(new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        931);
            }

            fileUri=null;
            showChoosingImageFile();
        }
        if(i==R.id.btnencrypt){
            if(fileUri!=null) {
                decrypt();
                dec.setEnabled(false);
                save.setEnabled(true);
                undo.setEnabled(true);
            }
        }
        if(i==R.id.btnUndo){
            Glide.with(this)
                    .load(fileUri)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            int w = resource.getWidth();
                            int h = resource.getHeight();
                            System.out.println("RES ORIGINAL"+w+"x"+h);
                            img.setImageBitmap(resource);
                        }
                    });
            dec.setEnabled(true);
            save.setEnabled(false);
            undo.setEnabled(false);
        }
        if(i==R.id.btndownload){
            if (fileUri != null){
                new Downloading(Decrypt.this).execute();
                save.setEnabled(false);
            }else {
                Toast.makeText(Decrypt.this, "No image to Download", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // decrypt local storage image
    private void decrypt(){
        final ProgressDialog decryptDlg= new ProgressDialog(Decrypt.this,R.style.AppDialogThemeBaseActivity);
        decryptDlg.setCanceledOnTouchOutside(false);
        decryptDlg.setMessage("Decrypting, please wait...");
        decryptDlg.show();
        long startTime = SystemClock.currentThreadTimeMillis();

        //get encrypted image height and width
        Bitmap bitmap=((BitmapDrawable)img.getDrawable()).getBitmap();
        int height= bitmap.getHeight();
        int width=bitmap.getWidth();

        // get secret key
        String value=txtkey.getText().toString();

        int pix[]=new int[height*width];
        int R,G,B,A;
        char characters[][]=new char[height][width];
        int seed[][]=new int[height][width];
        int len=0;

        //save pixel from ecrypted image to pix array
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);
        // generate seed no
        for(int cnt=1;cnt<=value.length();cnt++){
            total=total+(cnt*(int)value.charAt(cnt-1));
        }


        //generate random number matrix
        Random randomGenerator = new Random(total);
        int charcnt=0;
        for(int h=0;h<height;h++){
            for(int w=0;w<width;w++){
                if(charcnt==len){
                    charcnt=0;
                }
                int randomInt= randomGenerator.nextInt(255);

                seed[h][w]=randomInt;
                //generate ascii value matrix
                characters[h][w]=value.charAt(charcnt);

                charcnt++;

            }

        }

        for(int h=0;h<height;h++){
            for(int w=0;w<width;w++){
                int index = h * width + w;

                int ascii = (int) characters[h][w];
                int randomValue= seed[h][w];

//              extract alpha value from pixel and do xor
                A = (pix[index] >> 24) & 0xff ^ ascii ^ randomValue;
//              extract red value from pixel and do xor
                R = (pix[index] >> 16) & 0xff ^ ascii ^ randomValue;
//              extract green value from pixel and do xor
                G = (pix[index] >> 8) & 0xff ^ ascii ^ randomValue;
//              extract blue value from pixel and do xor
                B = pix[index] & 0xff ^ ascii ^ randomValue;
//              save encrypted rgba values to pixel array
                pix[index] = (A << 24) | (R << 16) | (G << 8) | B;
            }
        }

        // create decrypted image using pix array
        Bitmap bmp = Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Glide.with(getApplicationContext())
                .load(stream.toByteArray())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        widthorg= resource.getWidth();
                        heightorg = resource.getHeight();
                        System.out.println("RES ORIGINAL"+widthorg+"x"+heightorg);
                        img.setImageBitmap(resource);
                    }
                });
        long timeInterval = SystemClock.currentThreadTimeMillis() - startTime;
        System.out.println("Decrypt time: " +timeInterval);

        decryptDlg.dismiss();




    }

    //permission request
    protected final void askForPermissions(String[] permissions, int requestCode) {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), requestCode);
        }
    }
    private void showChoosingImageFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);



    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                fileUri = data.getData();

                Glide.with(getApplicationContext())
                        .load(fileUri)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                widthorg= resource.getWidth();
                                heightorg = resource.getHeight();
                                System.out.println("RES ORIGINAL"+widthorg+"x"+heightorg);
                                img.setImageBitmap(resource);
                            }
                        });

                dec.setEnabled(true);
                txtkey.setEnabled(true);

            } else {
                Toast.makeText(getApplicationContext(), "Unable to select an image", Toast.LENGTH_LONG).show();
            }
        }
    }


    //download decrypt image async task
    public class Downloading extends AsyncTask<String, Void, Integer>
    {

        ProgressDialog progressDialog;
        Activity _act;
        boolean SUCCESS=true;
        Uri outputFileUri;



        public Downloading (Activity act) {
            _act = act;

        }



        @Override
        protected Integer doInBackground(String... strings) {

            PowerManager powerManager = (PowerManager) _act.getSystemService(Activity.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");

            wakeLock.acquire();
            String timeStamp =
                    new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
            String imageFileName = "D_" + timeStamp + ".png";
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()+"/";

            try{
                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        imageFileName);
                Bitmap bm=((BitmapDrawable)img.getDrawable()).getBitmap();
                OutputStream outputStream = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();

                outputFileUri = FileProvider.getUriForFile(_act, getApplicationContext().getPackageName() + ".provider", file);
                System.out.println("OutPut :"+outputFileUri);

                SUCCESS=true;
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.DATA, file.getPath());

                _act.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
            catch (IOException e){
                SUCCESS=false;
                e.printStackTrace();
            }



            return Integer.valueOf(0);
        }


        @Override
        protected void onPreExecute() {
            notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_photo_camera_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(_act.getResources(),R.drawable.ic_photo_camera_white_24dp))
                    .setContentTitle("Downloading")
                    .setContentText("Downloading in progress")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(false)
                    .setOnlyAlertOnce(true)
                    .setProgress(100, 0, false);

            notificationManager.notify(1, notification.build());
        }
        @Override
        protected void onPostExecute(Integer result) {
            if(SUCCESS==true){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(outputFileUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getActivity(_act, 0, intent, 0);
                notification.setContentTitle("Download")
                        .setContentText("Download Successfully")
                        .setProgress(0, 0, false)
                        .setLargeIcon(BitmapFactory.decodeResource(_act.getResources(),R.drawable.ic_photo_camera_white_24dp))
                        .setContentIntent(pendingIntent)
                        .setOngoing(false);
                notificationManager.notify(1, notification.build());
            }
            else{
                notification.setContentTitle("Download")
                        .setContentText("Download Failed!")
                        .setProgress(0, 0, false)
                        .setLargeIcon(BitmapFactory.decodeResource(_act.getResources(),R.drawable.ic_photo_camera_white_24dp))
                        .setOngoing(false);
                notificationManager.notify(1, notification.build());
            }
            save.setEnabled(true);
            super.onPostExecute(result);


        }

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent2 = new Intent(Decrypt.this, MainActivity.class);
            startActivity(intent2);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
