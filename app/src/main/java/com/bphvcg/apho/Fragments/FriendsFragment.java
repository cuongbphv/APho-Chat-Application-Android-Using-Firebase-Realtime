package com.bphvcg.apho.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bphvcg.apho.Activities.ChatWithFriendActivity;
import com.bphvcg.apho.Activities.FindFriendActivity;
import com.bphvcg.apho.Activities.ListRequestActivity;
import com.bphvcg.apho.Adapters.ListFriendAdapter;
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
import java.util.HashMap;
import java.util.List;

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private Toolbar toolbar;
    private ImageView btnAddFriend;
    private ListView listViewFriend;
    private TextView textViewNumberOfRequest;
    private Button btnNumberOfRequest;

    private HashMap<String,Account> hashMapFriends;
    private ListFriendAdapter listFriendAdapter;

    private DatabaseReference nodeRoot;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        toolbar = v.findViewById(R.id.toolBarSearch);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        textViewNumberOfRequest = (TextView)v.findViewById(R.id.textViewNumberOfRequest);
        textViewNumberOfRequest.setOnClickListener(this);
        btnNumberOfRequest = (Button)v.findViewById(R.id.btnNumberOfRequest);
        btnNumberOfRequest.setOnClickListener(this);

        btnAddFriend = (ImageView)v.findViewById(R.id.iconAddFriend);
        btnAddFriend.setOnClickListener(this);

        listViewFriend = (ListView)v.findViewById(R.id.listViewFriend);

        hashMapFriends = new HashMap<>();
        listFriendAdapter = new ListFriendAdapter(getActivity(), R.layout.item_friend_in_list_friend, hashMapFriends);
        listViewFriend.setAdapter(listFriendAdapter);

        listViewFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AccountRequest fr = (AccountRequest)parent.getAdapter().getItem(position);
                Intent iChat = new Intent(getActivity(), ChatWithFriendActivity.class);
                iChat.putExtra("UID_Friend",fr.getUid());
                iChat.putExtra("Name_Friend",fr.getFullName());
                iChat.putExtra("From","Friend_Fragment");
                startActivity(iChat);
            }
        });

        nodeRoot = FirebaseDatabase.getInstance().getReference();
        nodeRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hashMapFriends.clear();
                int i = 0;
                DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");
                for(DataSnapshot singleRequest : nodeFriendRequests.getChildren()){
                    FriendRequest frTemp = singleRequest.getValue(FriendRequest.class);

                    if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                            && frTemp.getStatus().equals("received")){
                        i++;
                    }
                }

                if(i>0){
                    textViewNumberOfRequest.setText("Có " + i + " lời mời kết bạn mới");
                    btnNumberOfRequest.setVisibility(View.VISIBLE);
                    btnNumberOfRequest.setText(i+"");
                }else{
                    textViewNumberOfRequest.setText("Không có lời mời kết bạn");
                    btnNumberOfRequest.setVisibility(View.INVISIBLE);
                }


                DataSnapshot nodeFriends = dataSnapshot.child("friends");
                List<String> uidFriends = new ArrayList<>();
                for(DataSnapshot nodeSingleFriend : nodeFriends.getChildren()){
                    if(nodeSingleFriend.getKey().equals(FirebaseAuth.getInstance().getUid())){
                        Iterable<DataSnapshot> Fields = nodeSingleFriend.getChildren();
                        for(DataSnapshot eachField : Fields){
                            uidFriends.add(eachField.getKey());
                        }
                    }
                }

                DataSnapshot nodeUsers = dataSnapshot.child("users");
                for(DataSnapshot nodeSingleUser : nodeUsers.getChildren()){
                    if(!nodeSingleUser.getKey().equals(FirebaseAuth.getInstance().getUid())){
                        if(uidFriends.contains(nodeSingleUser.getKey())){ // kiểm tra danh sách người dùng xem trùng uid thì lấy
                            Account account = nodeSingleUser.getValue(Account.class);
                            if(!hashMapFriends.containsValue(account)){ // kiểm tra trong danh sách bạn bè chưa có người bạn
                                hashMapFriends.put(nodeSingleUser.getKey(),account);
                            }
                        }
                    }
                }

                listFriendAdapter.notifyDataSetChanged();// nếu có thông báo kết bạn hoặc thêm bạn mới thì effect list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iconAddFriend:
                Intent iAddFriend = new Intent(getActivity(), FindFriendActivity.class);
                startActivity(iAddFriend);
                getActivity().finish();
                break;
            case R.id.textViewNumberOfRequest:
            case R.id.btnNumberOfRequest:
                Intent iListRequest = new Intent(getActivity(), ListRequestActivity.class);
                startActivity(iListRequest);
                getActivity().finish();
                break;
        }
    }
}