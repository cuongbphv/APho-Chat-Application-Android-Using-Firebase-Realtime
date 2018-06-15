package com.bphvcg.apho.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView iconShakeHands, iconLogo, iconSlogan;
    Animation fromTop, fromLeft, fromRight;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        iconShakeHands = (ImageView)findViewById(R.id.iconShakeHands);
        iconLogo = (ImageView)findViewById(R.id.iconLogo);
        iconSlogan = (ImageView)findViewById(R.id.iconSlogan);

        fromTop = AnimationUtils.loadAnimation(SplashScreenActivity.this,R.anim.anim_from_top_to_bottom);
        fromLeft = AnimationUtils.loadAnimation(SplashScreenActivity.this,R.anim.anim_from_left_to_right);
        fromRight = AnimationUtils.loadAnimation(SplashScreenActivity.this,R.anim.anim_from_right_to_left);

        iconShakeHands.setAnimation(fromTop);
        iconLogo.setAnimation(fromLeft);
        iconSlogan.setAnimation(fromRight);

        // Thiết lập thread set 3 giây cho màn hình chào
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                    if(readFile().equals("")) {
                        Intent iLogin = new Intent(SplashScreenActivity.this, LoginActivity.class);
                        startActivity(iLogin);
                        overridePendingTransition(R.anim.animation_in,R.anim.animation_out);
                        finish();
                    }
                    else
                    {
                        String messageAfterDecrypt = "";
                        try {
                            messageAfterDecrypt = AESCrypt.decrypt("123", readFile());
                        }catch (GeneralSecurityException e){
                            e.printStackTrace();
                        }
                        if(!messageAfterDecrypt.isEmpty()) {
                            String[] fulluser = messageAfterDecrypt.split("[ ]");
                            String email = fulluser[0].trim();
                            String password = fulluser[1].trim();
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(SplashScreenActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                Intent iLogin = new Intent(SplashScreenActivity.this, LoginActivity.class);
                                                startActivity(iLogin);
                                                overridePendingTransition(R.anim.animation_in,R.anim.animation_out);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(SplashScreenActivity.this,
                                                        MainActivity.class);
                                                String uid = firebaseAuth.getCurrentUser().getUid();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("UID", uid);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                        }

                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
//                finally {
//                    Intent iLogin = new Intent(SplashScreenActivity.this, LoginActivity.class);
//                    startActivity(iLogin);
//                    overridePendingTransition(R.anim.animation_in,R.anim.animation_out);
//                    finish();
//                }
            }
        });

        thread.start();
    }

    private String readFile() {
        try {
            // Mở một luồng đọc file.
            FileInputStream in = this.openFileInput("session.txt");
            if (in == null)
                return "";
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(s).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return "";
        }
    }
}
