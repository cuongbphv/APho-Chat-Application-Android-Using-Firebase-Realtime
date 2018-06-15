package com.bphvcg.apho.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bphvcg.apho.Activities.SearchProfileActivity;
import com.bphvcg.apho.Models.AccountRequest;
import com.bphvcg.apho.Models.FriendRequest;
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
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListRequestAdapter extends BaseAdapter{

    Context context;
    int layout;
    List<AccountRequest> listRequests;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private DatabaseReference nodeRoot;

    public ListRequestAdapter(Context context, int layout,List<AccountRequest> listRequest){
        this.context = context;
        this.layout = layout;
        this.listRequests = listRequest;
    }

    public static class ViewHolder{
        CircleImageView avatarRequestInListRequest;
        TextView nameRequestInListRequest;
        ImageView btnApproveRequestInListRequest, btnRejectRequestInListRequest;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewRow = convertView;
        if(viewRow == null){
            viewRow = inflater.inflate(layout,parent,false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.avatarRequestInListRequest = (CircleImageView)viewRow.findViewById(R.id.avatarRequestInListRequest);
            viewHolder.nameRequestInListRequest = (TextView)viewRow.findViewById(R.id.nameRequestInListRequest);
            viewHolder.btnApproveRequestInListRequest = (ImageView)viewRow.findViewById(R.id.btnApproveRequestInListRequest);
            viewHolder.btnRejectRequestInListRequest = (ImageView)viewRow.findViewById(R.id.btnRejectRequestInListRequest);
            viewRow.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder)viewRow.getTag();

        if(listRequests != null){
            final int posItem = position;

            getAvatar(viewHolder.avatarRequestInListRequest,listRequests.get(position).getUid());
            viewHolder.nameRequestInListRequest.setText(listRequests.get(position).getFullName());

            viewHolder.btnApproveRequestInListRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nodeRoot = FirebaseDatabase.getInstance().getReference();
                    nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DataSnapshot nodeFriendRequests = dataSnapshot.child("friend_requests");
                            for(DataSnapshot nodeRequests : nodeFriendRequests.getChildren()) {
                                FriendRequest frTemp = nodeRequests.getValue(FriendRequest.class);
                                if(frTemp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                        && frTemp.getUidUserFriend().equals(listRequests.get(posItem).getUid())
                                        && frTemp.getStatus().equals("received")){
                                    nodeRoot.child("friend_requests").child(nodeRequests.getKey()).removeValue();
                                }
                                if(frTemp.getUidUserLogin().equals(listRequests.get(posItem).getUid())
                                        && frTemp.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                        && frTemp.getStatus().equals("sent")){
                                    nodeRoot.child("friend_requests").child(nodeRequests.getKey()).removeValue();
                                }
                            }

                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date();
                            nodeRoot.child("friends").child(FirebaseAuth.getInstance().getUid())
                                    .child(listRequests.get(posItem).getUid()).setValue(dateFormat.format(date));
                            nodeRoot.child("friends").child(listRequests.get(posItem).getUid())
                                    .child(FirebaseAuth.getInstance().getUid()).setValue(dateFormat.format(date));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            viewHolder.btnRejectRequestInListRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nodeRoot = FirebaseDatabase.getInstance().getReference();
                    nodeRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DataSnapshot nodeRequests = dataSnapshot.child("friend_requests");

                            for(DataSnapshot nodeSingleRequests : nodeRequests.getChildren()) {
                                FriendRequest temp = nodeSingleRequests.getValue(FriendRequest.class);
                                if(temp.getUidUserLogin().equals(FirebaseAuth.getInstance().getUid())
                                        && temp.getUidUserFriend().equals(listRequests.get(posItem).getUid())
                                        && temp.getStatus().equals("received")){
                                    nodeRoot.child("friend_requests").child(nodeSingleRequests.getKey()).removeValue();
                                }
                                if(temp.getUidUserLogin().equals(listRequests.get(posItem).getUid())
                                        && temp.getUidUserFriend().equals(FirebaseAuth.getInstance().getUid())
                                        && temp.getStatus().equals("sent")){
                                    nodeRoot.child("friend_requests").child(nodeSingleRequests.getKey()).removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            viewHolder.avatarRequestInListRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iProfile = new Intent(context, SearchProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("UID",listRequests.get(posItem).getUid());
                    bundle.putString("Email", listRequests.get(posItem).getEmail());
                    bundle.putString("PhoneNumber", listRequests.get(posItem).getPhoneNumber());
                    bundle.putString("From","ListRequestActivity");
                    iProfile.putExtras(bundle);
                    context.startActivity(iProfile);
                }
            });
            viewHolder.nameRequestInListRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iProfile = new Intent(context, SearchProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("UID",listRequests.get(posItem).getUid());
                    bundle.putString("Email", listRequests.get(posItem).getEmail());
                    bundle.putString("PhoneNumber", listRequests.get(posItem).getPhoneNumber());
                    bundle.putString("From","ListRequestActivity");
                    iProfile.putExtras(bundle);
                    context.startActivity(iProfile);
                }
            });
        }
        else{
            viewHolder.avatarRequestInListRequest.setVisibility(View.INVISIBLE);
            viewHolder.btnApproveRequestInListRequest.setVisibility(View.INVISIBLE);
            viewHolder.btnRejectRequestInListRequest.setVisibility(View.INVISIBLE);
            viewHolder.nameRequestInListRequest.setText("Không có lời mời kết bạn");
        }

        return viewRow;
    }

    @Override
    public int getCount() {
        return listRequests.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void getAvatar(final CircleImageView civAvatar, String uidRequest)
    {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference ref = storageReference.child("avatar").child(uidRequest+"avatar.jpg");
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

}
