package imageencryption.nilusha.com.seci.Sign;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import imageencryption.nilusha.com.seci.R;
import imageencryption.nilusha.com.seci.activity.MainActivity;


public class SignUp extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "SignUp";
    private static final int RC_SIGN_IN = 9001;
    private KProgressHUD progressDialog;
    private EditText usernamesignup;
    private EditText useremail;
    private EditText userpassword;
    private EditText conpassword;
    private Button signup;
    private FirebaseAuth firebaseAuth=null;
    boolean unavailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Views
        usernamesignup = (EditText) findViewById(R.id.input_name);
        userpassword = (EditText) findViewById(R.id.input_password);
        conpassword = (EditText) findViewById(R.id.conpassword);
        useremail = (EditText) findViewById(R.id.input_email);
        signup = (Button) findViewById(R.id.btn_signup);

        // Button listeners
        //txtFbLogin=(LoginButton)findViewById(R.id.login_button);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null){
            //close this activity
            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                System.out.println("email"+firebaseAuth.getCurrentUser().getEmail());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("seciusers");

                myRef.child(firebaseAuth.getCurrentUser().getUid().toString()).child("veryfied").setValue(true);
                Intent intent=new Intent(SignUp.this,MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(),"please check your email and verify",Toast.LENGTH_LONG).show();
                firebaseAuth.getInstance().signOut();
            }
        }

        //findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.btn_signup).setOnClickListener(this);
        findViewById(R.id.link_login).setOnClickListener(this);



    }



    // [START onActivityResult]



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signup:
                accountCreat();
                break;
            case R.id.link_login:
                Intent intent=new Intent(SignUp.this,SignIn.class);
                startActivity(intent);


        }



    }
    public void accountCreat(){
        final String name = usernamesignup.getText().toString();
        final String email = useremail.getText().toString();
        final String password = userpassword.getText().toString();
        final String confpassword = conpassword.getText().toString();
        boolean valid=true;
        if (name.isEmpty()) {
            usernamesignup.setError("username is required");
            valid = false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            useremail.setError("enter a valid email address");
            valid = false;
        }
        if (password.isEmpty() || password.length() < 8 ) {
            userpassword.setError("This password is too short. It must contain at least 8 characters.");
            valid = false;
        }
        if (password.isEmpty() || password.length() < 8 ) {
            userpassword.setError("This password is too short. It must contain at least 8 characters.");
            valid = false;
        }
        if(!confpassword.equals(password)){
            conpassword.setError("Passwords are not matching");
            valid = false;
        }
        if(valid==true){
            progressDialog =  KProgressHUD.create(SignUp.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("Please wait")
                    .setDetailsLabel("Sign up")
                    .setCancellable(false)
                    .setSize(300,200)
                    .setAnimationSpeed(2)
                    .setSize(300,200)
                    .setDimAmount(0.5f)
                    .show();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checking if success
                            if(task.isSuccessful()){
                                checkUser();

                                //display some message here


                            }else{
                                //display some message here
                                Toast.makeText(getApplicationContext(),"Already Registered",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }

                        }
                    });

        }

    }
    public void sendVeryfcation(){
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        System.out.println("Email Veryfication");
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        // Re-enable button


                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Verification email sent to " + user.getEmail(),Toast.LENGTH_LONG).show();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("seciusers");
                            myRef.child(firebaseAuth.getCurrentUser().getUid().toString()).child("seciusername").setValue(usernamesignup.getText().toString());
                            myRef.child(firebaseAuth.getCurrentUser().getUid().toString()).child("veryfied").setValue(false);

                            Intent intent=new Intent(SignUp.this,SignIn.class);
                            startActivity(intent);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Failed to veryfy!" + user.getEmail(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    public void checkUser(){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("seciusers");



        usersRef.orderByChild("seciusername").equalTo(usernamesignup.getText().toString().trim())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            System.out.println("USER :"+dataSnapshot);
                            usernamesignup.setError("username is not available");
                            unavailable=true;
                            delete();
                            firebaseAuth.getInstance().signOut();
                            progressDialog.dismiss();

                        } else {
                            System.out.println("USER : Email eka yawapan");
                            sendVeryfcation();
                        }
                        System.out.println("USER : ane manda!!");


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });




    }

    public void delete(){
        final FirebaseUser userdl = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(useremail.getText().toString(), userpassword.getText().toString());

        // Prompt the user to re-provide their sign-in credentials
        userdl.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userdl.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {


                                            progressDialog.dismiss();

                                        }
                                    }
                                });

                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
