package com.example.newproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 12;
    private static final String TAG = "Check";
    CallbackManager callbackManager;
    LoginButton loginButton;
    SignInButton signInButton;
        GoogleSignInClient googleSignInClient;
        TextView textView;
        EditText email,epass;
    private static final String EMAIL = "email";
        Button b1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 144;
    String PERMISSIONS[]={Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
    private FirebaseAuth mAuth;
    private String mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton=findViewById(R.id.login_button);
        b1=findViewById(R.id.button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        callbackManager=CallbackManager.Factory.create();
        textView=findViewById(R.id.textView6);
        email=findViewById(R.id.email);
        epass=findViewById(R.id.epass);

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Toast.makeText(MainActivity.this, "Error : "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MainActivity3.class);
                startActivity(i);
            }
        });
        signInButton=findViewById(R.id.signInButton);
        mAuth = FirebaseAuth.getInstance();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.example.googlesignin",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures){
//                MessageDigest md= MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.d("KeyHash",e.getMessage());
//        } catch (NoSuchAlgorithmException e) {
//            Log.d("KeyHash",e.getMessage());
//        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
            googleSignInClient= GoogleSignIn.getClient(this,gso);
        if(!hasPermissions(MainActivity.this,PERMISSIONS)){
            showPermissionDialog();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser==null){
            Toast.makeText(this, "Please Login", Toast.LENGTH_SHORT).show();
        }else {
            Intent i = new Intent(MainActivity.this,MainActivity2.class);
            startActivity(i);
            finish();

        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        signInWithCredential(credential);
    }

    private void signInWithCredential(AuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "onActivityResult", Toast.LENGTH_SHORT).show();
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                
                // ...
            }
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        signInWithCredential(credential);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null ) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "failed to resolve permission for "+permission, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }
    private void showPermissionDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Needs Location Access")
                .setCancelable(true)
                .setMessage("Need Location Access");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS,REQUEST_CODE_ASK_PERMISSIONS);
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //startActivty();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    Toast.makeText(getApplicationContext() , "Permissions are required for this app" , Toast.LENGTH_SHORT).show();
                    finish();
                } else {

                }
            }
        }
    }


}