package imageencryption.nilusha.com.seci.activity;


import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;

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
import io.github.memfis19.annca.Annca;
import io.github.memfis19.annca.internal.configuration.AnncaConfiguration;

// encrypting images
public class Composer extends AppCompatActivity implements View.OnClickListener {
    private Button galelry;
    private Button camera;
    private Button enc;
    private Button sendkey;
    private Button sendimage;
    private EditText txtkey;
    private Spinner spntype;
    private ImageView img;
    String encrypted = "";
    int total = 0;
    int widthorg;
    int heightorg;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private static final int PICK_IMAGE = 1;
    private Button undo;
    private Uri fileUri = null;
    private static final int CAPTURE_MEDIA = 368;
    String filepath = "";
    private ArrayList<String> list = new ArrayList<>();
    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseAuth firebaseAuth = null;
    private TextView lbluser;
    private EditText username;
    private Toolbar mtoolbar;
    private String seciuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);
        galelry = (Button) findViewById(R.id.btngallery);
        camera = (Button) findViewById(R.id.btncamera);
        enc = (Button) findViewById(R.id.btnencrypt);
        sendkey = (Button) findViewById(R.id.sendkey);
        sendimage = (Button) findViewById(R.id.btnupload);
        undo = (Button) findViewById(R.id.btnUndo);
        txtkey = (EditText) findViewById(R.id.txtkey);
        spntype = (Spinner) findViewById(R.id.spntype);
        img = (ImageView) findViewById(R.id.image);
        lbluser = (TextView) findViewById(R.id.lbluser);
        username = (EditText) findViewById(R.id.txtusername);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        mtoolbar.setTitle("Composer");
        lbluser.setEnabled(false);
        username.setEnabled(false);
        galelry.requestFocus();

        firebaseAuth = FirebaseAuth.getInstance();

        //check user login status
        if (firebaseAuth.getCurrentUser() == null) {
            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                Intent intent = new Intent(Composer.this, SignIn.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "please check your email and verify", Toast.LENGTH_LONG).show();
                firebaseAuth.getInstance().signOut();
            }
        }

        DatabaseReference rootRef1 = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef1.child("seciusers").child(firebaseAuth.getCurrentUser().getUid().toString());
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                seciuser = dataSnapshot.child("seciusername").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error" + databaseError, Toast.LENGTH_LONG).show();
            }
        };
        usersRef.addListenerForSingleValueEvent(eventListener);

        //add items to spinner
        list.add("SECI");
        list.add("Save to Device");
        list.add("Other");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spntype.setAdapter(adapter);

        enc.setEnabled(false);
        sendkey.setEnabled(false);
        sendimage.setEnabled(false);
        undo.setEnabled(false);
        txtkey.setEnabled(false);
        spntype.setEnabled(false);

        galelry.setOnClickListener(this);
        camera.setOnClickListener(this);
        enc.setOnClickListener(this);
        sendkey.setOnClickListener(this);
        sendimage.setOnClickListener(this);
        undo.setOnClickListener(this);
        if (Build.VERSION.SDK_INT > 15) {
            askForPermissions(new String[]{
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSIONS);
        }
        spntype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Other")) {
                    lbluser.setVisibility(view.GONE);
                    username.setVisibility(view.GONE);
                } else if (selectedItem.equals("SECI")) {
                    lbluser.setVisibility(view.VISIBLE);
                    username.setVisibility(view.VISIBLE);
                    lbluser.setText("Enter User Name");
                    sendimage.setText("Send");

                } else if (selectedItem.equals("Save to Device")) {
                    lbluser.setVisibility(view.VISIBLE);
                    username.setVisibility(view.VISIBLE);
                    lbluser.setText("Enter File Name");
                    sendimage.setText("Save");


                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    //request permisson

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

    //upload encrypted image to firebase storage
    private void uploadImage() {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("seciusers");

        System.out.println("SECI USER" + seciuser);
        usersRef.orderByChild("seciusername").equalTo(username.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            String key = "";
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                key = child.getKey().toString();

                            }
                            final String timeStamp =
                                    new SimpleDateFormat("yyyyMMdd_HHmmss",
                                            Locale.getDefault()).format(new Date());

                            String imageFileName = "E_" + timeStamp + ".png";
                            final String datetime = new SimpleDateFormat("dd/MM/yyyy hh:mm a",
                                    Locale.getDefault()).format(new Date());
                            System.out.println("DATE " + datetime);

                            try {
                                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                        imageFileName);
                                Bitmap bm = ((BitmapDrawable) img.getDrawable()).getBitmap();
                                OutputStream outputStream = new FileOutputStream(file);
                                bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                outputStream.close();

                                ContentValues values = new ContentValues();

                                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                                values.put(MediaStore.MediaColumns.DATA, file.getPath());

                                Composer.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                                Uri filePath = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
                                if (filePath != null) {
                                    final KProgressHUD progressDialog = KProgressHUD.create(Composer.this)
                                            .setStyle(KProgressHUD.Style.PIE_DETERMINATE)
                                            .setLabel("Please wait Sending...")
                                            .setCancellable(false)
                                            .setDimAmount(0.5f)
                                            .setSize(300, 200)
                                            .setMaxProgress(100)
                                            .show();

                                    StorageReference ref = storageReference.child("images/" + key + "/" + imageFileName);
                                    final String finalKey = key;
                                    ref.putFile(filePath)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    try {

                                                        DatabaseReference myRef = rootRef.child("messages");

                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("message").
                                                                setValue(taskSnapshot.getDownloadUrl().toString());
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("from").
                                                                setValue(seciuser);
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("timestamp").
                                                                setValue(datetime);
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("id").
                                                                setValue(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp);
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("isImportant").
                                                                setValue(false);
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("isRead").
                                                                setValue(false);
                                                        myRef.child(finalKey).child("inbox").child(firebaseAuth.getCurrentUser().getUid().toString() + timeStamp).child("timeid").
                                                                setValue(timeStamp);


                                                        progressDialog.dismiss();
                                                        Toast.makeText(Composer.this, "Sending Success", Toast.LENGTH_LONG).show();
                                                    } catch (Exception e) {
                                                        Toast.makeText(Composer.this, "Sending Failed", Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(Composer.this, "Sending Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                                            .getTotalByteCount());
                                                    progressDialog.setProgress((int) progress);
                                                    progressDialog.setDetailsLabel((int) progress + "% Finished");
                                                }
                                            });
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {

                            sendimage.setEnabled(true);
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Composer.this, R.style.AppDialogThemeBaseActivity);
                            builder1.setTitle("Invalid User!");
                            builder1.setMessage("User didn't found try again!!");
                            builder1.setCancelable(false);

                            builder1.setNegativeButton(
                                    "Got It!",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Composer.this, "Coundn't connect to the server", Toast.LENGTH_LONG).show();
                    }

                });

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.btncamera) {
            CaptureImage();
        } else if (i == R.id.btngallery) {
            fileUri = null;
            showChoosingImageFile();
        } else if (i == R.id.btnencrypt) {

            if (fileUri == null) {
                Toast.makeText(getApplicationContext(), "please select an image", Toast.LENGTH_LONG).show();
            } else if (txtkey.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "please enter a key", Toast.LENGTH_LONG).show();
            } else {
                undo.setEnabled(true);
                enc.setEnabled(false);


                encryptingImage();


                //txtkey.setEnabled(false);
                sendkey.setEnabled(true);
                spntype.setEnabled(true);

                username.setEnabled(true);
                sendimage.setEnabled(true);
            }
        } else if (i == R.id.btnupload) {
            sendimage.setEnabled(false);
            String text = spntype.getSelectedItem().toString();
            if (text.equals("SECI")) {
                uploadImage();
            } else if (text.equals("Other")) {
                SendImage();
            } else if (text.equals("Save to Device")) {
                SaveImage();
            }

        } else if (i == R.id.btnUndo) {
            undo.setEnabled(false);
            enc.setEnabled(true);
            txtkey.setEnabled(true);
            sendkey.setEnabled(false);
            Glide.with(getApplicationContext())
                    .load(fileUri)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            widthorg = resource.getWidth();
                            heightorg = resource.getHeight();
                            System.out.println("RES ORIGINAL" + widthorg + "x" + heightorg);
                            img.setImageBitmap(resource);
                        }
                    });
            spntype.setEnabled(false);
            sendimage.setEnabled(false);
            lbluser.setEnabled(true);
            username.setEnabled(false);


        } else if (i == R.id.sendkey) {
            if (!txtkey.getText().toString().equals(null)) {
                SendKey();
            } else {
                Toast.makeText(getApplicationContext(), "please enter a key", Toast.LENGTH_LONG).show();
            }

        }

    }

    //save encrypted image to local storage
    private void SaveImage() {
        final KProgressHUD saveprogressDialog = KProgressHUD.create(Composer.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setDetailsLabel("Saving!!")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        String imageFileName = username.getText() + ".png";


        try {
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    imageFileName);
            Bitmap bm = ((BitmapDrawable) img.getDrawable()).getBitmap();
            OutputStream outputStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.MediaColumns.DATA, file.getPath());

            Composer.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


            Toast.makeText(Composer.this, "Saved Successfully", Toast.LENGTH_LONG).show();
            saveprogressDialog.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
            sendimage.setEnabled(true);
        }
    }

    //send secret key through user selected application
    private void SendKey() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, txtkey.getText().toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Select an Application"));
    }

    //send encrypted through user selected application
    private void SendImage() {


        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "E_" + timeStamp + ".png";


        try {
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    imageFileName);
            Bitmap bm = ((BitmapDrawable) img.getDrawable()).getBitmap();
            OutputStream outputStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.MediaColumns.DATA, file.getPath());

            this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
            System.out.println("OutPut :" + outputFileUri);
            intent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
            startActivity(Intent.createChooser(intent, "Select an Application"));
        } catch (IOException e) {
            e.printStackTrace();
            sendimage.setEnabled(true);
        }


    }

    //select image from gallery
    private void showChoosingImageFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //load image from gallery
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                fileUri = data.getData();

                Glide.with(getApplicationContext())
                        .load(fileUri)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                widthorg = resource.getWidth();
                                heightorg = resource.getHeight();
                                System.out.println("RES ORIGINAL" + widthorg + "x" + heightorg);
                                img.setImageBitmap(resource);
                            }
                        });
                txtkey.setEnabled(true);
                enc.setEnabled(true);
            } else {
                Toast.makeText(getApplicationContext(), "Unable to select an image", Toast.LENGTH_LONG).show();
            }
        }
        //load image from camera
        if (requestCode == CAPTURE_MEDIA) {
            if (resultCode == RESULT_OK) {


                filepath = data.getStringExtra(AnncaConfiguration.Arguments.FILE_PATH);
                System.out.println("Path*******" + filepath);
                fileUri = Uri.fromFile(new File(filepath));
                getWindow().setFormat(PixelFormat.TRANSLUCENT);
                Glide.with(getApplicationContext())
                        .load(fileUri)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                widthorg = resource.getWidth();
                                heightorg = resource.getHeight();
                                System.out.println("RES ORIGINAL" + widthorg + "x" + heightorg);
                                img.setImageBitmap(resource);
                            }
                        });
                txtkey.setEnabled(true);
                enc.setEnabled(true);


            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Camera cancelled!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to Capture!", Toast.LENGTH_LONG).show();
            }
        }
    }

    //encrypting process
    private void encryptingImage() {
        final KProgressHUD encprogressDialog = KProgressHUD.create(Composer.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setDetailsLabel("Encrypting!!")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        long startTime = SystemClock.currentThreadTimeMillis();

//        get original image height and width
        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        System.out.println("Resolution :" + String.valueOf(height) + "x" + String.valueOf(width));

//      get secret key
        String value = txtkey.getText().toString();

        int pix[] = new int[height * width];
        int R, G, B, A;
        char characters[][] = new char[height][width];
        int seed[][] = new int[height][width];
        int len = 0;
//        get pixels of original image and save to pix array
        bitmap.getPixels(pix, 0, width, 0, 0, width, height);

//        get seed no
        for (int cnt = 1; cnt <= value.length(); cnt++) {
            total = total + (cnt * (int) value.charAt(cnt - 1));
        }

//        generate random number matrix
        Random randomGenerator = new Random(total);
        int charcnt = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                if (charcnt == len) {
                    charcnt = 0;
                }
                int randomInt = randomGenerator.nextInt(255);

                seed[h][w] = randomInt;
//                generate character matrix using ascii values
                characters[h][w] = value.charAt(charcnt);
                charcnt++;
            }

        }

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int index = h * width + w;

                int ascii = (int) characters[h][w];
                int randomValue = seed[h][w];

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

//      create encrypted image using pixel array
        Bitmap bmp = Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Glide.with(getApplicationContext())
                .load(stream.toByteArray())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        widthorg = resource.getWidth();
                        heightorg = resource.getHeight();
                        System.out.println("RES ORIGINAL" + widthorg + "x" + heightorg);
                        img.setImageBitmap(resource);
                    }
                });

        long timeInterval = SystemClock.currentThreadTimeMillis() - startTime;
//        process time research purpose
        System.out.println("Encrypted time: " + timeInterval);
        encprogressDialog.dismiss();


    }

    // access cammera activity
    public void CaptureImage() {
        AnncaConfiguration.Builder photo = new AnncaConfiguration.Builder(this, CAPTURE_MEDIA);
        photo.setMediaAction(AnncaConfiguration.MEDIA_ACTION_PHOTO);
        photo.setMediaQuality(AnncaConfiguration.MEDIA_QUALITY_HIGHEST);
        photo.setMediaResultBehaviour(AnncaConfiguration.PREVIEW);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Composer.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            ActivityCompat.requestPermissions(Composer.this,
                    new String[]{Manifest.permission.CAMERA},
                    1);

            return;
        }
        new Annca(photo.build()).launchCamera();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent2 = new Intent(Composer.this, MainActivity.class);
            startActivity(intent2);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


}
