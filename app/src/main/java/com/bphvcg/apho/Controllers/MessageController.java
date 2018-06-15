package com.bphvcg.apho.Controllers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bphvcg.apho.Adapters.RecyclerListMessageAdapter;
import com.bphvcg.apho.Models.Message;
import com.bphvcg.apho.Models.RecentlyChat;
import com.bphvcg.apho.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MessageController {

    Context context;
    String uidFriend;
    Message msg;
    List<Message> listMessage;

    List<String> keyMessage;

    RecyclerView recyclerViewMessage;
    RecyclerListMessageAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    private DatabaseReference nodeInfoMine, nodeInfoFriend, nodeMessageUpdate;


    public MessageController(Context context, String uidFriend, RecyclerView recyclerViewMessage){
        this.context = context;
        this.uidFriend = uidFriend;
        this.recyclerViewMessage = recyclerViewMessage;

        msg = new Message();

        listMessage = new ArrayList<>();
        keyMessage = new ArrayList<>();

        layoutManager = new LinearLayoutManager(context);

        recyclerViewMessage.setLayoutManager(layoutManager);
        adapter = new RecyclerListMessageAdapter(listMessage, context, R.layout.item_message_mine,
                R.layout.item_message_friend, R.layout.item_message_image_mine, R.layout.item_message_image_friend,
                R.layout.item_message_audio_mine,R.layout.item_message_audio_friend);
        recyclerViewMessage.setAdapter(adapter);
    }

    public void scrollMessageEditText(){
        recyclerViewMessage.scrollToPosition(listMessage.size() - 1); // scroll tin nhắn mới nhất ngược lên
    }

    public void refreshMessage(DataSnapshot nodeMessage, String myName, String friendName){
        //listMessage.clear();

        for(DataSnapshot nodeSingleMessage : nodeMessage.getChildren()) {
            Message newMsg = nodeSingleMessage.getValue(Message.class);
            if ((newMsg.getUidSender().equals(FirebaseAuth.getInstance().getUid())
                    && newMsg.getUidReceiver().equals(uidFriend))
                    || (newMsg.getUidSender().equals(uidFriend)
                    && newMsg.getUidReceiver().equals(FirebaseAuth.getInstance().getUid()))) {
                if(!keyMessage.contains(nodeSingleMessage.getKey())){
                    listMessage.add(newMsg);
                    keyMessage.add(nodeSingleMessage.getKey());
                    adapter.notifyDataSetChanged(); // có thay đổi có tin nhắn mới gửi đến
                }
            }
        }

        // gọi refresh tin nhắn khi và chỉ khi người gửi đang trong activity chat
        // vì vậy để kiểm tra xem hoặc chưa xem sẽ lấy tin nhắn cuối cùng
        if(!listMessage.isEmpty())
        {
            Message temp = listMessage.get(listMessage.size() - 1);

            String typeFile = "text";
            if(temp.isImage()){
                typeFile = "image";
            }
            else if(temp.isAudio()){
                typeFile = "audio";
            }

            if(!temp.getUidSender().equals(FirebaseAuth.getInstance().getUid())){ // tin nhắn vừa load không phải của bản thân gửi
                //push thông tin cần thiết cho việc lấy danh sách gần đây
                nodeInfoMine = FirebaseDatabase.getInstance().getReference().child("more_info")
                        .child(temp.getUidReceiver()).child("last_messages");
                // push tin nhắn cuối để show ra khi lấy danh sách nhăn tin gần đây
                nodeInfoMine.child(temp.getUidSender()).setValue(new RecentlyChat(temp.getUidSender(),
                        temp.getUidSender(), friendName, temp.getContent(), typeFile, temp.getTimeMessage(),true));
                nodeInfoFriend = FirebaseDatabase.getInstance().getReference().child("more_info")
                        .child(temp.getUidSender()).child("last_messages"); // node last message của bạn
                nodeInfoFriend.child(temp.getUidReceiver()).setValue(new RecentlyChat(temp.getUidSender(),
                        temp.getUidReceiver(), myName, temp.getContent(), typeFile, temp.getTimeMessage(),true));

                nodeMessageUpdate = FirebaseDatabase.getInstance().getReference().child("messages")
                        .child(keyMessage.get(keyMessage.size() - 1));
                temp.setLastMessageSeen(true); // đưa tin nhắn cuối cùng về trạng thái đã xem

                nodeMessageUpdate.setValue(temp); // thay đổi trên database
            }
        }

        scrollMessageEditText();
    }
}
