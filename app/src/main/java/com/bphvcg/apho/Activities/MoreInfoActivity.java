package com.bphvcg.apho.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bphvcg.apho.Adapters.ListMediaStorageAdapter;
import com.bphvcg.apho.Models.Account;
import com.bphvcg.apho.Models.Message;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreInfoActivity extends AppCompatActivity implements ValueEventListener {

    ImageView btnBackToMessage;
    TextView textViewNameFriend, profileMoreInfoMessage;
    CircleImageView avatarMoreInfoProfile;
    Intent iMoreInfo;
    private DatabaseReference nodeRoot, nodeUsers, nodeStatus;
    private String uidFriend, nameFriend, emailFriend, phoneNumberFriend;
    Account friend = new Account();
    List<Message> listImages;
    ListView listShowImage;
    ListMediaStorageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info_message);

        iMoreInfo = getIntent();

        if (!iMoreInfo.getStringExtra("UID").isEmpty() && !iMoreInfo.getStringExtra("Name").isEmpty()) {
            uidFriend = iMoreInfo.getStringExtra("UID");
            nameFriend = iMoreInfo.getStringExtra("Name");

            textViewNameFriend = (TextView) findViewById(R.id.nameMoreInfoProfile);
            textViewNameFriend.setText(nameFriend);
            textViewNameFriend.setOnClickListener(goToFriendProfile);

            profileMoreInfoMessage = (TextView)findViewById(R.id.profileMoreInfoMessage);
            profileMoreInfoMessage.setOnClickListener(goToFriendProfile);

            // set avt cho bạn
            avatarMoreInfoProfile = (CircleImageView)findViewById(R.id.avatarMoreInfoProfile);
            getAvatar(avatarMoreInfoProfile,uidFriend);
            avatarMoreInfoProfile.setOnClickListener(goToFriendProfile);

            btnBackToMessage = (ImageView) findViewById(R.id.btnBackToMessage);
            btnBackToMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iChat = new Intent(MoreInfoActivity.this, ChatWithFriendActivity.class);
                    iChat.putExtra("UID_Friend",uidFriend);
                    iChat.putExtra("Name_Friend",nameFriend);
                    iChat.putExtra("From","MoreInfoMessage");
                    startActivity(iChat);
                    finish();
                }
            });

            listImages = new ArrayList<>();
            adapter = new ListMediaStorageAdapter(MoreInfoActivity.this,R.layout.item_image_in_list_storage,listImages);
            listShowImage = (ListView)findViewById(R.id.listImageStorage);
            listShowImage.setAdapter(adapter);


            nodeRoot = FirebaseDatabase.getInstance().getReference();
            nodeRoot.addValueEventListener(this);

            nodeUsers = FirebaseDatabase.getInstance().getReference().child("users").child(uidFriend);
            nodeUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getKey().equals(uidFriend)){
                        friend = dataSnapshot.getValue(Account.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    View.OnClickListener goToFriendProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            nodeStatus = FirebaseDatabase.getInstance().getReference().child("status").child(uidFriend);
            nodeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String status = dataSnapshot.getValue(String.class);

                    if(status.equals("Chỉ mình tôi")){
                        Toast.makeText(MoreInfoActivity.this,"Người dùng hiện đang thiết lập chế độ riêng tư!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent iFriendProfile = new Intent(MoreInfoActivity.this, SearchProfileActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("UID_Friend",uidFriend);
                        bundle.putString("Email",friend.getUsername());
                        bundle.putString("PhoneNumber",friend.getPhoneNumber());
                        bundle.putString("From","MoreInfoMessage");
                        bundle.putString("Name_Friend",nameFriend);
                        iFriendProfile.putExtras(bundle);
                        startActivity(iFriendProfile);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    };

    public void getAvatar(final CircleImageView civAvatar, String uidRequest)
    {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("avatar").child(uidRequest+"avatar.jpg");
        try {
            final long ONE_MEGABYTE = 1024 * 1024;
            ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    civAvatar.setImageBitmap(bmp);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if(((StorageException) exception).getErrorCode()==-13010) {
                        civAvatar.setImageResource(R.drawable.avatar_default);
                        return;
                    }
                }
            });

        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        listImages.clear();
        DataSnapshot nodeMessage = dataSnapshot.child("messages");
        for(DataSnapshot nodeSingleMessage : nodeMessage.getChildren()){
            Message temp = nodeSingleMessage.getValue(Message.class);
            if(temp.isImage()){
                if((temp.getUidSender().equals(FirebaseAuth.getInstance().getUid())
                        && temp.getUidReceiver().equals(uidFriend))
                            || (temp.getUidSender().equals(uidFriend)
                                && temp.getUidReceiver().equals(FirebaseAuth.getInstance().getUid()))){
                    listImages.add(temp);
                }
            }

            if((temp.getUidSender().equals(FirebaseAuth.getInstance().getUid())
                    && temp.getUidReceiver().equals(uidFriend))
                    || (temp.getUidSender().equals(uidFriend)
                    && temp.getUidReceiver().equals(FirebaseAuth.getInstance().getUid()))){
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
