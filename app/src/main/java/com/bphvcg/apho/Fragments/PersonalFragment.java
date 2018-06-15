package com.bphvcg.apho.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bphvcg.apho.Activities.EditProfileActivity;
import com.bphvcg.apho.Models.Account;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalFragment extends Fragment implements View.OnClickListener{

    ImageView btnEditPicture;
    private CircleImageView civAvatar;
    Button btnEditProfile;
    private Cursor cursor;
    private int columnIndex;
    private TextView txtPhoneNumber, txtDateOfBirth, txtAddress,txtGender, txtFullName, txtDescription;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    Account account;
    private String uid;
    private Uri filePath;
    private RelativeLayout relativeLayout;

    private boolean isNotify = false;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_personal, container, false);
        relativeLayout = v.findViewById(R.id.profile_layout);
        btnEditPicture = (ImageView)v.findViewById(R.id.btnEditPicture);
        btnEditPicture.setOnClickListener(this);
        civAvatar = v.findViewById(R.id.civAvatar);
        btnEditProfile = (Button)v.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(this);
        account = new Account();
        txtFullName = v.findViewById(R.id.name);
        txtPhoneNumber = v.findViewById(R.id.textViewPhoneNumber);
        txtDateOfBirth = v.findViewById(R.id.textViewDateofBirth);
        txtAddress = v.findViewById(R.id.textViewAddress);
        txtGender = v.findViewById(R.id.textViewGender);
        txtDescription = v.findViewById(R.id.textViewDescription);
        isNotify = false;

        registerForContextMenu(btnEditPicture);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(); // ở nút root gốc
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Bundle bundle = getArguments();
        uid = bundle.getString("UID");
        Log.d("Check Bundle: ",uid);

        // moi vao no goi cai nay nen xuat toast nay
        try {
            changeAvatar(isNotify);
            changeBackground(isNotify);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> node = dataSnapshot.getChildren();

                for (DataSnapshot child : node) {
                    if(child.getKey().equals(uid)) {
                        account = child.getValue(Account.class);
                    }
                }
                if (account != null) {
                    txtFullName.setText(account.getFullName());
                    txtPhoneNumber.setText(account.getPhoneNumber()+"");
                    txtDateOfBirth.setText(account.getDateOfBirth());
                    txtAddress.setText(account.getAddress());
                    String gender = account.isGender() ? "Nam" : "Nữ";
                    txtGender.setText(gender);
                    txtDescription.setText(account.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.change_avatar_and_background,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.optChangeAvatar:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                break;
            case R.id.optChangeAvatarWithCamera:
                Intent intentopencamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentopencamera,3);
                break;
            case R.id.optChangeBackground:
                Intent intent2 = new Intent();
                intent2.setType("image/*");
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent2, "Select Picture"), 2);
                break;
            case R.id.optChangeBackgroundWithCamera:
                Intent intentopencamera2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentopencamera2,4);
                break;
            case R.id.optDefaultAvatar:
                try{
                    deleteAvatar();
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }

                break;
            case R.id.optDefaultBackground:
                try{
                    deleteBackground();
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }

                break;
            case R.id.optCancel:
                getActivity().closeContextMenu();
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                uploadImageAvatar();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {

                uploadImageBackground();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 3 && resultCode == Activity.RESULT_OK
                && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            try {
                uploadImageAvatarCamera(bitmap);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 4 && resultCode == Activity.RESULT_OK
                && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            try {
                uploadImageBackgroundCamera(bitmap);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadImageAvatarCamera(Bitmap bitmap) {

        if(bitmap != null)
        {
            StorageReference ref = storageReference.child("avatar").child(uid+"avatar.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = ref.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(),"Không nhận được ảnh vui lòng kiểm tra lại!!",Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    isNotify = true;
                    changeAvatar(isNotify);
                }
            });
        }
    }
    private void uploadImageBackgroundCamera(Bitmap bitmap) {

        if(bitmap != null)
        {
            StorageReference ref = storageReference.child("background").child(uid+"background.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = ref.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(),"Không nhận được ảnh vui lòng kiểm tra lại!!",Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    isNotify = true;
                    changeBackground(isNotify);
                }
            });
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnEditPicture:
                getActivity().openContextMenu(btnEditPicture);
                break;

            case R.id.btnEditProfile:
                Intent iEditProfile = new Intent(getActivity(),EditProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("USERID",uid);
                bundle.putString("USERNAME",account.getUsername());
                bundle.putString("FULLNAME",account.getFullName());
                bundle.putBoolean("GENDER",account.isGender());
                bundle.putString("ADDRESS",account.getAddress());
                bundle.putString("PHONENUMBER",account.getPhoneNumber());
                bundle.putString("DATEOFBIRTH",account.getDateOfBirth());
                bundle.putString("DESCRIPTION",account.getDescription());
                iEditProfile.putExtra("BUNDLE",bundle);

                startActivity(iEditProfile);
                break;
        }
    }
    private void uploadImageAvatar() {

        if(filePath != null)
        {
            StorageReference ref = storageReference.child("avatar").child(uid+"avatar.jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            isNotify=true;
                            changeAvatar(isNotify);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
    }

    private void uploadImageBackground() {

        if(filePath != null)
        {
            StorageReference ref = storageReference.child("background").child(uid+"background.jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            isNotify = true;
                            changeBackground(isNotify);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
    }

    public void changeAvatar(final boolean isNotify)
    {
        StorageReference ref = storageReference.child("avatar").child(uid+"avatar.jpg");
        try {
            final File localFile = File.createTempFile("images", "jpg");

            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    // Bitmap bmp2 = bmp.createScaledBitmap(bmp,300,170,false);
                    civAvatar.setImageBitmap(bmp);
                    if(isNotify)
                        Toast.makeText(getActivity(), "Đổi hình đại diện thành công", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if(isNotify)
                        Toast.makeText(getActivity(),"Đổi hình đại diện Thất bại",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeBackground(final boolean isNotify)
    {
        StorageReference ref = storageReference.child("background").child(uid+"background.jpg");
        try {
            final File localFile = File.createTempFile("images", "jpg");

            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = (int)(((float)170/(float)1280)*displayMetrics.heightPixels);
                    int width = displayMetrics.widthPixels;
                    Bitmap bmp2 = bmp.createScaledBitmap(bmp,width,height,false);
                    Drawable temp = new BitmapDrawable(getResources(), bmp2);
                    relativeLayout.setBackground(temp);
                    if(isNotify)
                        Toast.makeText(getActivity(),"Đổi hình nền thành công",Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if(isNotify)
                        Toast.makeText(getActivity(),"Đổi hình nền thất bại",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteAvatar() {

        StorageReference storageRef = storage.getReference();
        StorageReference desertRef = storageRef.child("avatar").child(uid+"avatar.jpg");
        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                civAvatar.setImageResource(R.drawable.avatar_default);
                Toast.makeText(getActivity(), "Đổi hình đại diện thành công", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                if(((StorageException) exception).getErrorCode()==-13010) {
                    return;
                }
                Toast.makeText(getActivity(), "Đổi hình đại diện Thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void deleteBackground() {

        StorageReference storageRef = storage.getReference();
        StorageReference desertRef = storageRef.child("background").child(uid + "background.jpg");

        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                relativeLayout.setBackgroundResource(R.drawable.header_default);
                Toast.makeText(getActivity(), "Đổi hình nền thành công", Toast.LENGTH_SHORT).show();
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                if(((StorageException) exception).getErrorCode()==-13010) {

                    return;
                }

                Toast.makeText(getActivity(), "Đổi hình nền Thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
