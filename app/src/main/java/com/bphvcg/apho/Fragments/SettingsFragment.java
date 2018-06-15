package com.bphvcg.apho.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bphvcg.apho.Activities.ChangePassWordActivity;
import com.bphvcg.apho.Activities.LoginActivity;
import com.bphvcg.apho.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileOutputStream;

public class SettingsFragment extends Fragment  implements View.OnClickListener{
    private Button btnLogout, btnChangePassWord;
    private Button btnPrivatePolicy, btnAboutApp;

    String[] listChoicePolicy = new String[]{"Chỉ mình tôi","Bạn bè","Mọi người"};;
    FirebaseAuth firebaseAuth;

    private DatabaseReference nodeMoreInfo, nodeMoreInfo2;

    String status = "";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        btnLogout = v.findViewById(R.id.btnLogout);
        btnChangePassWord = v.findViewById(R.id.btnAccountPolicy);
        btnLogout.setOnClickListener(this);
        btnChangePassWord.setOnClickListener(this);;

        btnPrivatePolicy = v.findViewById(R.id.btnPrivatePolicy);
        btnPrivatePolicy.setOnClickListener(this);

        btnAboutApp = v.findViewById(R.id.btnAboutApp);

        btnAboutApp.setOnClickListener(this);

        return v;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPrivatePolicy:
                nodeMoreInfo = FirebaseDatabase.getInstance().getReference().child("status")
                        .child(FirebaseAuth.getInstance().getUid());
                nodeMoreInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        status = dataSnapshot.getValue(String.class);

                        int pos = 3;
                        for(int i = 0; i < listChoicePolicy.length; i++)
                            if(listChoicePolicy[i].equals(status))
                                pos = i;

                        showDialog(pos);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                break;
            case R.id.btnLogout:
                firebaseAuth.getInstance().signOut();
                saveFile();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;
            case  R.id.btnAccountPolicy:
                Intent intent2 = new Intent(getActivity(), ChangePassWordActivity.class);
                startActivity(intent2);
                break;
            case R.id.btnAboutApp:
                Toast.makeText(getActivity(),"Bùi Phan Viết Cường - 15110173\nNguyễn Mạnh Cường - 15110174",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
    public  void saveFile()
    {
        try {

            // Mở một luồng ghi file.
            FileOutputStream out = getActivity().openFileOutput("session.txt", Context.MODE_PRIVATE);
            // Ghi dữ liệu.
            String fulluser = "";
            out.write(fulluser.getBytes());
            out.close();
        } catch (Exception e) {
            Toast.makeText(getActivity(),"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    String changeStatus;

    public void showDialog(int pos){
        final AlertDialog.Builder mBuider = new AlertDialog.Builder(getActivity());
        mBuider.setTitle("Chọn thiết lập riêng tư");
        mBuider.setSingleChoiceItems(listChoicePolicy, pos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeStatus = listChoicePolicy[which];
            }
        });

        mBuider.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nodeMoreInfo2 = FirebaseDatabase.getInstance().getReference().child("status")
                        .child(FirebaseAuth.getInstance().getUid());
                nodeMoreInfo2.setValue(changeStatus);
                Toast.makeText(getActivity(),"Cập nhật thiết lập riêng tư thành công!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        mBuider.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog mDialog = mBuider.create();
        mDialog.show();
    }
}
