package com.bphvcg.apho.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bphvcg.apho.Models.FriendRequest;
import com.bphvcg.apho.Models.User;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private Toolbar toolBarFindFriend;
    private ImageView btnBackFriendFragment;
    private EditText editTextPhoneNumber, editTextEmail;
    private RelativeLayout relativeSearchProfile;
    private CircleImageView avatarSearchProfile;
    private TextView nameSearchProfile;
    private ImageView btnSendRequest, btnRejectRequest;
    private View lineHorizontalFinish;
    private CircleImageView civAvatar;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference nodeRoot, nodeFriendRequest, nodeFriends;
    private List<User> userList;
    private User loginUser;
    private Intent iProfile;
    private String uidSearchFriend = "";
    private Bundle bundle;

    User user; // người dùng bạn bè

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        bundle = new Bundle();
        toolBarFindFriend = (Toolbar) findViewById(R.id.toolBarFindFriend);
        setSupportActionBar(toolBarFindFriend);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnBackFriendFragment = (ImageView)findViewById(R.id.iconBackFriendFragment);
        btnBackFriendFragment.setOnClickListener(this);

        civAvatar = findViewById(R.id.avatarSearchProfile);

        //làm việc với tìm kiếm
        //btnSearchByEmail = (ImageView)findViewById(R.id.btnSearchByEmail);
       // btnSearchByEmail.setOnClickListener(this);
        //btnSearchByPhoneNumber = (ImageView)findViewById(R.id.btnSearchByPhoneNumber);
        //btnSearchByPhoneNumber.setOnClickListener(this);

        editTextEmail = (EditText)findViewById(R.id.editTextSearchByEmail);
        editTextEmail.setOnTouchListener(this);
        editTextPhoneNumber = (EditText)findViewById(R.id.editTextSearchByPhoneNumber);
        editTextPhoneNumber.setOnTouchListener(this);

        relativeSearchProfile = (RelativeLayout)findViewById(R.id.relativeSearchProfile);

        avatarSearchProfile = (CircleImageView)findViewById(R.id.avatarSearchProfile);
        avatarSearchProfile.setOnClickListener(this);
        nameSearchProfile = (TextView)findViewById(R.id.nameSearchProfile);
        nameSearchProfile.setOnClickListener(this);

        btnSendRequest = (ImageView)findViewById(R.id.btnSendRequest);
        btnSendRequest.setOnClickListener(this);
        btnRejectRequest = (ImageView)findViewById(R.id.btnRejectRequest);
        btnRejectRequest.setOnClickListener(this);

        lineHorizontalFinish = findViewById(R.id.lineHorizontalFinish);

        userList = new ArrayList<>();
        loginUser = new User();

        nodeRoot = FirebaseDatabase.getInstance().getReference();
        nodeFriendRequest = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        nodeFriends = FirebaseDatabase.getInstance().getReference().child("friends");

        nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot nodeUsers = dataSnapshot.child("users");
                for(DataSnapshot nodeUIDs : nodeUsers.getChildren()){
                    User user = nodeUIDs.getValue(User.class);
                    userList.add(user);
                    if(user.getUsername().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        loginUser = user;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.editTextSearchByPhoneNumber:
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= editTextPhoneNumber.getRight() - editTextPhoneNumber.getTotalPaddingRight()) {
                        final String txtSearchByPhonenumber = editTextPhoneNumber.getText().toString().trim();
                        checkExistFriend(txtSearchByPhonenumber,1);
                        return true;
                    }
                }
                break;
            case R.id.editTextSearchByEmail:
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= editTextEmail.getRight() - editTextEmail.getTotalPaddingRight()) {
                        final String txtSearchByEmail = editTextEmail.getText().toString().trim();
                        checkExistFriend(txtSearchByEmail,2);
                        return true;
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
//            case R.id.btnSearchByPhoneNumber:
//                final String txtSearchByPhonenumber = editTextPhoneNumber.getText().toString().trim();
//                checkExistFriend(txtSearchByPhonenumber,1);
//                break;
//
//            case R.id.btnSearchByEmail:
//                final String txtSearchByEmail = editTextEmail.getText().toString().trim();
//                checkExistFriend(txtSearchByEmail,2);
//                break;

            case R.id.btnSendRequest:
                nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");

                        FriendRequest loggedInSent = null, loggedInReceived = null;

                        String keyLoggedInSent = "", keyLoggedInReceived = "";
                        String keyLoggedInSent2 = "", keyLoggedInReceived2 = "";

                        for(DataSnapshot nodeRequests : nodeFriendRequests.getChildren()){
                            FriendRequest frTemp = nodeRequests.getValue(FriendRequest.class);
                            if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getUidUserFriend().equals(uidSearchFriend)
                                    && frTemp.getStatus().equals("sent")){
                                // key đã gửi yêu cầu kết bạn
                                loggedInSent = frTemp;
                                keyLoggedInSent = nodeRequests.getKey();
                            }
                            if(frTemp.getUidUserLogin().equals(uidSearchFriend)
                                    && frTemp.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getStatus().equals("received")){
                                // key đã gửi yêu cầu kết bạn
                                keyLoggedInSent2 = nodeRequests.getKey();
                            }


                            // key nhận yêu cầu kết bạn
                            if(frTemp.getUidUserLogin().equals(uidSearchFriend)
                                    && frTemp.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getStatus().equals("sent")){

                                keyLoggedInReceived = nodeRequests.getKey();
                            }
                            // key nhận yêu cầu kết bạn
                            if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getUidUserFriend().equals(uidSearchFriend)
                                    && frTemp.getStatus().equals("received")){
                                loggedInReceived = frTemp;
                                keyLoggedInReceived2 = nodeRequests.getKey();
                            }
                        }

                        // chưa có yêu cầu kết bạn thì khi nhấn nút sẽ tạo request
                        if(loggedInSent == null && loggedInReceived == null){
                            loggedInSent = new FriendRequest(FirebaseAuth.getInstance().getUid(),uidSearchFriend,"sent");
                            nodeFriendRequest.push().setValue(loggedInSent);

                            // dùng tạm biến loggedInReceived
                            loggedInReceived = new FriendRequest(uidSearchFriend, FirebaseAuth.getInstance().getUid(), "received");
                            nodeFriendRequest.push().setValue(loggedInReceived);

                            btnSendRequest.setImageResource(R.drawable.send_request);
                            btnRejectRequest.setVisibility(View.INVISIBLE);
                        }
                        // đã gửi kết bạn, xóa yêu cầu kết bạn
                        else if(loggedInSent != null && loggedInReceived == null){
                            nodeFriendRequest.child(keyLoggedInSent).removeValue();
                            nodeFriendRequest.child(keyLoggedInSent2).removeValue();

                            btnSendRequest.setImageResource(R.drawable.not_send_request);
                        }
                        // đã nhận được yêu cầu kết bạn, nhấn đồng ý kết bạn
                        else if(loggedInSent == null && loggedInReceived != null){
                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date();

                            nodeFriends.child(FirebaseAuth.getInstance().getUid()).child(uidSearchFriend).setValue(dateFormat.format(date));
                            nodeFriends.child(uidSearchFriend).child(FirebaseAuth.getInstance().getUid()).setValue(dateFormat.format(date));

                            nodeFriendRequest.child(keyLoggedInReceived).removeValue();
                            nodeFriendRequest.child(keyLoggedInReceived2).removeValue();

                            btnSendRequest.setVisibility(View.INVISIBLE);
                            btnRejectRequest.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;

            case R.id.btnRejectRequest:
                nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot nodeRequests = dataSnapshot.child("friend_requests");

                        for(DataSnapshot nodeSingleRequests : nodeRequests.getChildren()) {
                            FriendRequest temp = nodeSingleRequests.getValue(FriendRequest.class);
                            if(temp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && temp.getUidUserFriend().equals(uidSearchFriend)
                                    && temp.getStatus().equals("received")){
                                nodeFriendRequest.child(nodeSingleRequests.getKey()).removeValue();
                            }
                            if(temp.getUidUserLogin().equals(uidSearchFriend)
                                    && temp.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                    && temp.getStatus().equals("sent")){
                                nodeFriendRequest.child(nodeSingleRequests.getKey()).removeValue();
                            }
                        }

                        btnSendRequest.setImageResource(R.drawable.not_send_request);
                        btnRejectRequest.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;

            case R.id.nameSearchProfile:
                startActivity(iProfile);

                // xem xét có finish activity không
                break;

            case R.id.avatarSearchProfile:
                startActivity(iProfile);

                // xem xét có finish activity không
                break;

            case R.id.iconBackFriendFragment:
                Intent intent = new Intent(FindFriendActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("ReturnTab", 1);
                bundle.putString("UID",FirebaseAuth.getInstance().getUid());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void checkExistFriend(final String searchField, int type){
        user = new User();
        boolean existFlag = false;
        if (type == 1){ // tìm bằng sdt
            for(User friend : userList){
                if(friend.getPhoneNumber().equals(searchField)){
                    user = friend;
                    existFlag = true;
                }
            }
        }
        else if(type == 2){ // tìm bằng email
            for(User friend : userList){
                if(friend.getUsername().equals(searchField)){
                    user = friend;
                    existFlag = true;
                }
            }
        }

        if(existFlag) { // có người bạn đó trong node users

            final String email = user.getUsername();
            final String phoneNumber = user.getPhoneNumber();

            relativeSearchProfile.setVisibility(View.VISIBLE);
            lineHorizontalFinish.setVisibility(View.VISIBLE);
            avatarSearchProfile.setVisibility(View.VISIBLE);
            // kiểm tra tìm email hoặc sdt của chính mình
            if (user.getUsername().equals(loginUser.getUsername()) && user.getPhoneNumber().equals(loginUser.getPhoneNumber())) {
                btnSendRequest.setVisibility(View.INVISIBLE);
                iProfile = new Intent(FindFriendActivity.this, MainActivity.class);
                bundle.putInt("ReturnTab", 2);
                bundle.putString("UID", FirebaseAuth.getInstance().getUid());
                getAvatar(FirebaseAuth.getInstance().getUid());
                nameSearchProfile.setVisibility(View.VISIBLE);
                nameSearchProfile.setText(user.getFullName());
                iProfile.putExtras(bundle);
            } else { // ngược lại tìm của người khác
                nodeRoot.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        ////////Check user có uid tìm kiếm theo ///////
                        DataSnapshot nodeUsers = dataSnapshot.child("users");
                        for (DataSnapshot nodeUIDs : nodeUsers.getChildren()) {
                            User user = nodeUIDs.getValue(User.class);
                            if (user.getUsername().equals(searchField)
                                    || user.getPhoneNumber().equals(searchField)) {
                                // mỗi lần tìm thì uidSearchField được gán nếu tìm thấy bạn bè
                                uidSearchFriend = nodeUIDs.getKey();
                            }
                        }
                        ////////////////////////////////////////////////

                        ///////Check user có trong danh sách friend_requests///////
                        DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");
                        FriendRequest loggedInSent = null, loggedInReceived = null;
                        for (DataSnapshot nodeRequests : nodeFriendRequests.getChildren()) {
                            FriendRequest frTemp = nodeRequests.getValue(FriendRequest.class);
                            if (frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getUidUserFriend().equals(uidSearchFriend)
                                    && frTemp.getStatus().equals("sent")) {
                                // gửi yêu cầu kết bạn
                                loggedInSent = frTemp;
                            }
                            if (frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                    && frTemp.getUidUserFriend().equals(uidSearchFriend)
                                    && frTemp.getStatus().equals("received")) {
                                // nhận yêu cầu kết bạn
                                loggedInReceived = frTemp;
                            }
                        }
                        /////////////////////////////////////////////////////////////

                        ////////Kiểm tra bạn có nằm trong danh sách bạn bè chưa/////
                        boolean isFriend = false;
                        DataSnapshot nodeMatchingFriends = dataSnapshot.child("friends");
                        for (DataSnapshot nodeSingleFriends : nodeMatchingFriends.getChildren()) {
                            Log.d("Check SingleFriends: ", nodeSingleFriends.getKey());
                            Log.d("check key uid: ",
                                    nodeSingleFriends.child(FirebaseAuth.getInstance().getUid()).child(uidSearchFriend).getKey());
                            if (nodeSingleFriends.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                if (nodeSingleFriends.hasChild(uidSearchFriend)) {
                                    isFriend = true;
                                }
                            }
                        }
                        ////////////////////////////////////////////////////////////

                        //////////////Check trạng thái tìm kiếm của người dùng///////
                        String status = dataSnapshot.child("status").child(uidSearchFriend).getValue(String.class);
                        /////////////////////////////////////////////////////////////

                        if(status.equals("Chỉ mình tôi")){
                            relativeSearchProfile.setVisibility(View.VISIBLE);
                            avatarSearchProfile.setVisibility(View.INVISIBLE);
                            btnSendRequest.setVisibility(View.INVISIBLE);
                            nameSearchProfile.setVisibility(View.VISIBLE);
                            nameSearchProfile.setText("Người dùng riêng tư");
                            lineHorizontalFinish.setVisibility(View.VISIBLE);
                        }
                        else if(status.equals("Bạn bè")){
                            if (isFriend) {
                                btnSendRequest.setVisibility(View.INVISIBLE);
                                btnRejectRequest.setVisibility(View.INVISIBLE);
                                getAvatar(uidSearchFriend);
                                nameSearchProfile.setVisibility(View.VISIBLE);
                                nameSearchProfile.setText(user.getFullName());
                            }
                            else{
                                relativeSearchProfile.setVisibility(View.VISIBLE);
                                avatarSearchProfile.setVisibility(View.INVISIBLE);
                                btnSendRequest.setVisibility(View.INVISIBLE);
                                nameSearchProfile.setVisibility(View.VISIBLE);
                                nameSearchProfile.setText("Người dùng chế độ bạn bè");
                                lineHorizontalFinish.setVisibility(View.VISIBLE);
                            }
                        }
                        else if(status.equals("Mọi người")){
                            if (loggedInSent == null && loggedInReceived == null) {
                                // đã là bạn bè của nhau
                                if (isFriend) {
                                    btnSendRequest.setVisibility(View.INVISIBLE);
                                    btnRejectRequest.setVisibility(View.INVISIBLE);
                                }
                                // chưa gửi yêu cầu kết bạn
                                else {
                                    btnSendRequest.setImageResource(R.drawable.not_send_request);
                                    btnSendRequest.setVisibility(View.VISIBLE);
                                }
                            }
                            // đã gửi yêu cầu kết bạn
                            else if (loggedInSent != null && loggedInReceived == null) {
                                btnSendRequest.setImageResource(R.drawable.send_request);
                                btnSendRequest.setVisibility(View.VISIBLE);
                                btnRejectRequest.setVisibility(View.INVISIBLE);
                            }
                            // nhận yêu cầu kết bạn
                            else if (loggedInSent == null && loggedInReceived != null) {
                                btnSendRequest.setImageResource(R.drawable.approve);
                                btnSendRequest.setVisibility(View.VISIBLE);
                                btnRejectRequest.setImageResource(R.drawable.reject);
                                btnRejectRequest.setVisibility(View.VISIBLE);
                            }

                            getAvatar(uidSearchFriend);
                            nameSearchProfile.setVisibility(View.VISIBLE);
                            nameSearchProfile.setText(user.getFullName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                iProfile = new Intent(FindFriendActivity.this, SearchProfileActivity.class);
                bundle.putString("Email", email);
                bundle.putString("PhoneNumber", phoneNumber);
                bundle.putString("From", "FindFriendActivity");
                iProfile.putExtras(bundle);
            }

        }
        else{
            relativeSearchProfile.setVisibility(View.VISIBLE);
            avatarSearchProfile.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.INVISIBLE);
            nameSearchProfile.setVisibility(View.VISIBLE);
            nameSearchProfile.setText("Không tồn tại");
            lineHorizontalFinish.setVisibility(View.VISIBLE);
        }
    }
    public void getAvatar(String uid)
    {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference ref = storageReference.child("avatar").child(uid+"avatar.jpg");
        try {
            final long ONE_MEGABYTE = 1024 * 1024;
            ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
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
                    // Handle any errors/
                }
            });
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

}
