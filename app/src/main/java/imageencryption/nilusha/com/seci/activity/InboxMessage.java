package imageencryption.nilusha.com.seci.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import imageencryption.nilusha.com.seci.R;
import imageencryption.nilusha.com.seci.Sign.SignIn;

import static imageencryption.nilusha.com.seci.App.CHANNEL_1_ID;


public class InboxMessage extends AppCompatActivity implements View.OnClickListener{
    int widthorg;
    int heightorg;
    private Button dec;
    String image=null;
    String from=null;
    private Button save;
    private EditText txtkey;
    private TextView txtfrom;

    private ImageView img;
    private NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    String encrypted = "";
    int total=0;
    CircularProgressDrawable crl;
    Uri uri=null;
    private Button undo;
    private Uri fileUri = null;
    private static final int CAPTURE_MEDIA = 368;
    String filepath="";
    private Toolbar mtoolbar;
    private FirebaseAuth firebaseAuth=null;
    private DownloadManager dm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message);
        notificationManager = NotificationManagerCompat.from(this);
        final KProgressHUD decryptprogresbar = KProgressHUD.create(InboxMessage.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setDetailsLabel("Loading!!")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setSize(300,200)
                .setDimAmount(0.5f)
                .show();
        dec=(Button)findViewById(R.id.btnencrypt);

        undo=(Button)findViewById(R.id.btnUndo);
        save=(Button)findViewById(R.id.btndownload);
        txtkey=(EditText) findViewById(R.id.txtkey);
        txtfrom=(TextView) findViewById(R.id.lblusername);


        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()== null){
            //close this activity
            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                Intent intent=new Intent(InboxMessage.this,SignIn.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(),"please check your email and verify",Toast.LENGTH_LONG).show();
                firebaseAuth.getInstance().signOut();
            }
        }
        image= getIntent().getStringExtra("imagepath");
        from=getIntent().getStringExtra("from");


        img=(ImageView) findViewById(R.id.imageenc);
        img.requestFocus();
        crl= new CircularProgressDrawable(getApplicationContext());
        crl.setStrokeWidth(8f);
        crl.setCenterRadius( 60f);
        crl.setColorSchemeColors(R.color.black);
        crl.start();
        save.setEnabled(false);
        undo.setEnabled(false);
        Glide.with(this)
                .load(image)
                .asBitmap()
                .placeholder(crl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        int w = resource.getWidth();
                        int h = resource.getHeight();
                        System.out.println("RES ORIGINAL"+w+"x"+h);
                        img.setImageBitmap(resource);
                        decryptprogresbar.dismiss();
                    }
                });
        txtfrom.setText(from);

        dec.setOnClickListener(this);
        undo.setOnClickListener(this);
        save.setOnClickListener(this);



    }
    private void decrypt(){
        final ProgressDialog decrypt = new ProgressDialog(InboxMessage.this,R.style.AppDialogThemeBaseActivity);
        decrypt.setCanceledOnTouchOutside(false);
        decrypt.setMessage("Decrypting, please wait...");
        decrypt.show();
        Bitmap bitmap=((BitmapDrawable)img.getDrawable()).getBitmap();
        int height= bitmap.getHeight();
        int width=bitmap.getWidth();
        System.out.println("Resolution :"+String.valueOf(height)+"x"+String.valueOf(width));


        String value=txtkey.getText().toString();

        int pix[]=new int[height*width];
        int R,G,B,A;
        char characters[][]=new char[height][width];
        int seed[][]=new int[height][width];
        int len=0;
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);

        for(int cnt=1;cnt<=value.length();cnt++){
            total=total+(cnt*(int)value.charAt(cnt-1));
        }
        try {
            encrypted = AESUtils.encrypt(value);
            len=encrypted.length();

            Log.d("TEST", "encrypted:" + encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Random randomGenerator = new Random(total);
        int charcnt=0;
        for(int h=0;h<height;h++){
            for(int w=0;w<width;w++){
                if(charcnt==len){
                    charcnt=0;
                }
                int randomInt= randomGenerator.nextInt(255);

                seed[h][w]=randomInt;
                characters[h][w]=encrypted.charAt(charcnt);

                charcnt++;

            }

        }

        for(int h=0;h<height;h++){
            for(int w=0;w<width;w++){
                int index = h * width + w;

                int ascii = (int) characters[h][w];
                int seedno= seed[h][w];



                A= (pix[index] >> 24) & 0xff;
                //A= Color.alpha(pixel)  ^ randomInt;

                R = (pix[index] >> 16) & 0xff ^seedno^ ascii;

                G = (pix[index] >> 8) & 0xff ^seedno ^ ascii;

                B = pix[index] & 0xff ^seedno^ ascii;
                pix[index] = (A << 24) | (R << 16) | (G << 8) | B;
            }
        }


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



        decrypt.dismiss();



    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btnencrypt) {
            if(image!=null) {
                long startTime = SystemClock.currentThreadTimeMillis();
                decrypt();
                long timeInterval = SystemClock.currentThreadTimeMillis() - startTime;
                System.out.println("Decrypted time: " +timeInterval);
                dec.setEnabled(false);
                save.setEnabled(true);
                undo.setEnabled(true);
            }

        }
        else if (i == R.id.btnUndo) {

            Glide.with(this)
                    .load(image)
                    .asBitmap()
                    .placeholder(crl)
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
        else if(i == R.id.btndownload) {

            if (image != null){
                new Downloading(InboxMessage.this).execute();
                save.setEnabled(false);
            }else {
                Toast.makeText(InboxMessage.this, "No image to Download", Toast.LENGTH_SHORT).show();
            }


        }
    }
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
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.DATA, file.getPath());

                _act.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                SUCCESS=true;
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
                intent.setDataAndType(outputFileUri, "image/png");
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
            Intent intent2 = new Intent(InboxMessage.this, MainActivity.class);
            startActivity(intent2);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
