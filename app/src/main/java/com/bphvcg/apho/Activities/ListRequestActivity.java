package com.bphvcg.apho.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.bphvcg.apho.Adapters.ListRequestAdapter;
import com.bphvcg.apho.Models.Account;
import com.bphvcg.apho.Models.AccountRequest;
import com.bphvcg.apho.Models.FriendRequest;
import com.bphvcg.apho.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListRequestActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener{

    private ImageView btnBackListFriend;
    private ListView listViewListRequest;

    private List<AccountRequest> listAccountRequests;
    private ListRequestAdapter listRequestAdapter;

    private DatabaseReference nodeRoot;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_request);

        btnBackListFriend = (ImageView)findViewById(R.id.iconBackListFriend);
        btnBackListFriend.setOnClickListener(this);

        nodeRoot = FirebaseDatabase.getInstance().getReference();
        nodeRoot.addValueEventListener(this);

        listAccountRequests = new ArrayList<>();

        listViewListRequest = (ListView)findViewById(R.id.listViewListRequest);
        listRequestAdapter = new ListRequestAdapter(this, R.layout.item_request_in_list_request,listAccountRequests);
        listViewListRequest.setAdapter(listRequestAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iconBackListFriend:
                Intent intent = new Intent(ListRequestActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("ReturnTab", 1);
                bundle.putString("UID", FirebaseAuth.getInstance().getUid());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        listAccountRequests.clear();
        List<String> uidTemp = new ArrayList<>(); // tạo 1 list tạm

        DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");
        for(DataSnapshot singleRequest : nodeFriendRequests.getChildren()){
            FriendRequest frTemp = singleRequest.getValue(FriendRequest.class);

            if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                    && frTemp.getStatus().equals("received")){
                uidTemp.add(frTemp.getUidUserFriend()); // lấy ra được tất cả yêu cầu kết bạn tới nick đăng nhập
            }
        }

        DataSnapshot nodeUsers = dataSnapshot.child("users");
        for(DataSnapshot singleUser : nodeUsers.getChildren()){
            Account acc = singleUser.getValue(Account.class);
            if(uidTemp.contains(singleUser.getKey())){
                // kiểm tra nếu uid từ list lấy ra ở request giống uid trong users thì add vào list account request
                listAccountRequests.add(new AccountRequest(singleUser.getKey(),acc.getFullName(),
                                                                acc.getUsername(),acc.getPhoneNumber()));
            }
        }

        listRequestAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
