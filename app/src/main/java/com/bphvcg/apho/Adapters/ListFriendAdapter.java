package com.bphvcg.apho.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bphvcg.apho.Models.Account;
import com.bphvcg.apho.Models.AccountRequest;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListFriendAdapter extends BaseAdapter{

    Context context;
    int layout;
    HashMap<String,Account> hashMapFriends;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    public ListFriendAdapter(Context context, int layout, HashMap<String,Account> hashMapFriends){
        this.context = context;
        this.layout = layout;
        this.hashMapFriends = hashMapFriends;
    }

    public static class ViewHolder{
        CircleImageView avatarFriendInList;
        TextView nameFriendInList;
    }

    @Override
    public int getCount() {
        return hashMapFriends.size();
    }

    @Override
    public Object getItem(int position) {
        String key = hashMapFriends.keySet().toArray()[position].toString();
        Account acc = hashMapFriends.get(key);
        AccountRequest temp = new AccountRequest(key,acc.getFullName(),acc.getUsername(),acc.getPhoneNumber());
        return temp; // trả về 1 đối tường dùng cho khi click để chat
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = convertView;
        if(viewRow == null){ // kỹ thuật load view 1 item và các item sau sử dụng tag view có sẵn
            viewRow = inflater.inflate(layout,parent,false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.avatarFriendInList = (CircleImageView)viewRow.findViewById(R.id.avatarFriendInList);
            viewHolder.nameFriendInList = (TextView)viewRow.findViewById(R.id.nameFriendInList);
            viewRow.setTag(viewHolder); // chỉ khởi tạo 1 lần cho item friend trong list
        }

        ViewHolder viewHolder = (ViewHolder)viewRow.getTag();
        String key = hashMapFriends.keySet().toArray()[position].toString();
        getAvatar(viewHolder.avatarFriendInList,key);
        viewHolder.nameFriendInList.setText(hashMapFriends.get(key).getFullName());

        return viewRow;
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
