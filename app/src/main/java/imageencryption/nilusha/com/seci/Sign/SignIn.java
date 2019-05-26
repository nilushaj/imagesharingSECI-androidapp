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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kaopiz.kprogresshud.KProgressHUD;

import imageencryption.nilusha.com.seci.R;
import imageencryption.nilusha.com.seci.activity.MainActivity;


public class SignIn extends AppCompatActivity implements View.OnClickListener {
    private EditText email;
    private EditText password;
    private Button signin;
    private TextView signup;


    private KProgressHUD progressDialog;

    private FirebaseAuth firebaseAuth=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        email=(EditText)findViewById(R.id.input_email);
        password=(EditText)findViewById(R.id.input_password);
        signin=(Button)findViewById(R.id.btn_login);
        signup=(TextView)findViewById(R.id.link_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            //close this activity
            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("seciusers");

                myRef.child(firebaseAuth.getCurrentUser().getUid().toString()).child("veryfied").setValue(true);
                Intent intent=new Intent(SignIn.this,MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(),"please check your email and verify",Toast.LENGTH_LONG).show();
                firebaseAuth.getInstance().signOut();
            }
        }

        signin.setOnClickListener(this);
        signup.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        if(v==signin){
            AuthSignIn();
        }
        if(v==signup){
            Intent intent=new Intent(SignIn.this,SignUp.class);
            startActivity(intent);
        }

    }
    private void AuthSignIn(){
        boolean valid=true;

        if (email.getText().toString().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Enter a valid email address");
            valid = false;
        }
        if (password.getText().toString().isEmpty() || password.length() < 8 ) {
            password.setError("This password is too short. It must contain at least 8 characters.");
            valid = false;
        }
        if(valid==true){
            progressDialog = KProgressHUD.create(SignIn.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("Please wait")
                    .setDetailsLabel("Sign in")
                    .setCancellable(false)
                    .setAnimationSpeed(2)
                    .setSize(300,200)
                    .setDimAmount(0.5f)
                    .show();
            firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //if the task is successfull
                            if(task.isSuccessful()){
                                //start the profile activity
                                if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("seciusers");

                                    myRef.child(firebaseAuth.getCurrentUser().getUid().toString()).child("veryfied").setValue(true);

                                    finish();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Please verify your Email!",Toast.LENGTH_LONG).show();
                                    firebaseAuth.getInstance().signOut();
                                }
                                progressDialog.dismiss();
                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),"Incorrect Email or Password",Toast.LENGTH_LONG).show();

                            }
                        }
                    });






        }
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