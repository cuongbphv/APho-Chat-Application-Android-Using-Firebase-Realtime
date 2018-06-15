package com.bphvcg.apho.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scottyab.aescrypt.AESCrypt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class ChangePassWordActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    EditText txtOldPassWord, txtNewPassWord, txtReNewPassWord;
    Button btnChangePassWord, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        txtOldPassWord = findViewById(R.id.editTextOldPassWord);
        txtNewPassWord = findViewById(R.id.editTextNewPassWord);
        txtReNewPassWord = findViewById(R.id.editTextReNewPassWord);

        btnChangePassWord = findViewById(R.id.buttonEditPassWord);
        btnChangePassWord.setOnClickListener(this);

        btnCancel = findViewById(R.id.buttonExitEditPassWord);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonEditPassWord:
                final String newpassword = txtNewPassWord.getText().toString().trim();
                final String renewpassword = txtReNewPassWord.getText().toString().trim();
                final String oldpassword = txtOldPassWord.getText().toString().trim();
                if(renewpassword.length()<6)
                    Toast.makeText(this, "Mật khẩu mới phải dài ít nhất 6 ký tự!", Toast.LENGTH_LONG).show();
                String messageAfterDecrypt = "";
                try {
                    messageAfterDecrypt = AESCrypt.decrypt("123", readFile());
                } catch (GeneralSecurityException e) {
                    //handle error - could be due to incorrect password or tampered encryptedMsg
                }

                if (messageAfterDecrypt != "" && user != null) {

                    String[] fulluser = messageAfterDecrypt.split("[ ]");
                    final String getoldpassword = fulluser[1].trim();

                    if (!oldpassword.equals(getoldpassword)) {
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng vui lòng kiểm tra lại!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    if (newpassword.equals(renewpassword)) {
                        user.updatePassword(newpassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ChangePassWordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ChangePassWordActivity.this, "Đổi mật khẩu thất bại vui vòng kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Mật khẩu mới nhập lại không đúng vui lòng kiểm tra lại!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    break;
                }
            case R.id.buttonExitEditPassWord:
                Intent iSettingFragment = new Intent(ChangePassWordActivity.this,MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("UID",firebaseAuth.getUid());
                bundle.putInt("ReturnTab",3);
                iSettingFragment.putExtras(bundle);
                startActivity(iSettingFragment);
                finish();
                break;
        }
    }

    private String readFile () {
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
