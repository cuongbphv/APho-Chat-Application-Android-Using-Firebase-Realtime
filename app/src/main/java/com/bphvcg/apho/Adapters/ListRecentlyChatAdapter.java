package com.bphvcg.apho.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bphvcg.apho.Models.RecentlyChat;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListRecentlyChatAdapter extends BaseAdapter {

    Context context;
    int layout;
    List<RecentlyChat> recentlyChatList;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    public ListRecentlyChatAdapter(Context context, int layout, List<RecentlyChat> recentlyChatList){
        this.context = context;
        this.layout = layout;
        this.recentlyChatList = recentlyChatList;
    }

    public static class ViewHolder{
        CircleImageView avatarRecentlyChat;
        TextView nameRecentlychat, outlineContent, timeRecentlyChat;
        TextView notifyNewMessage;
    }

    @Override
    public int getCount() {
        return recentlyChatList.size();
    }

    @Override
    public Object getItem(int position) {
        return recentlyChatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = convertView;
        if(viewRow == null){
            viewRow = inflater.inflate(layout, parent,false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.avatarRecentlyChat = (CircleImageView)viewRow.findViewById(R.id.avatarRecentlyChat);
            viewHolder.nameRecentlychat = (TextView)viewRow.findViewById(R.id.nameRecentlyChat);
            viewHolder.outlineContent = (TextView)viewRow.findViewById(R.id.contentRecentlyChat);
            viewHolder.timeRecentlyChat = (TextView)viewRow.findViewById(R.id.timeRecentlyChat);
            viewHolder.notifyNewMessage = (TextView)viewRow.findViewById(R.id.notifyNewMessage);
            viewRow.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder)viewRow.getTag();

        getAvatar(viewHolder.avatarRecentlyChat,recentlyChatList.get(position).getUidRecentlyChat());

        viewHolder.nameRecentlychat.setText(recentlyChatList.get(position).getNameRecentlychat());
        viewHolder.timeRecentlyChat.setText(recentlyChatList.get(position).getLastMessageTime().substring(0,16));
        String outline = recentlyChatList.get(position).getContent();
        if(recentlyChatList.get(position).getType().equals("image")){
            viewHolder.outlineContent.setText("[Hình ảnh]");
        }
        else if(recentlyChatList.get(position).getType().equals("audio")){
            viewHolder.outlineContent.setText("[Âm thanh]");
        }
        else{
            if(outline.length() > 30)
                viewHolder.outlineContent.setText(outline.substring(0,30)+"...");
            else
                viewHolder.outlineContent.setText(outline);
        }

        if(recentlyChatList.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())){
            viewHolder.notifyNewMessage.setVisibility(View.INVISIBLE);
        }
        else{
            if(recentlyChatList.get(position).isSeen()){
                viewHolder.notifyNewMessage.setVisibility(View.INVISIBLE);
            }
            else{
                viewHolder.nameRecentlychat.setTypeface(null, Typeface.BOLD);
                viewHolder.outlineContent.setTypeface(null,Typeface.BOLD);
                viewHolder.notifyNewMessage.setVisibility(View.VISIBLE);
            }
        }

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
