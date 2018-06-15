package com.bphvcg.apho.Adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bphvcg.apho.Fragments.ImageMessageFragment;
import com.bphvcg.apho.Models.Message;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerListMessageAdapter extends RecyclerView.Adapter<RecyclerListMessageAdapter.ViewHolder>  {

    private List<Message> listMessages;
    private int rMessageMine, rMessageFriend, rImageMessageMine, rImageMessageFriend, rAudioMessageMine, rAudioMessageFriend;
    Context context;
    private String mFileName = "";
    private  MediaPlayer mPlayer=null;
    private String currentAudio = "";
    public static ImageButton btnCurrentPlay = null;
    public static MediaPlayer getCurrentMedia = null;

    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    boolean isFinish = false; // kiểm tra audio đã hết chưa

    public RecyclerListMessageAdapter(List<Message> listMessages, Context context, int rMessageMine, int rMessageFriend,
                                      int rImageMessageMine, int rImageMessageFriend, int rAudioMessageMine, int rAudioMesageFriend) {
        this.listMessages = listMessages;
        this.rMessageMine = rMessageMine; // layout tin nhắn của bản thân
        this.rMessageFriend = rMessageFriend; // layout tin nhắn của bạn
        this.rImageMessageMine = rImageMessageMine; // layout tin nhắn ảnh của bản thân
        this.rImageMessageFriend = rImageMessageFriend; // layout tin nhắn ảnh của bạn
        this.rAudioMessageMine = rAudioMessageMine;
        this.rAudioMessageFriend = rAudioMesageFriend;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageMine, textViewMessageTimeMine, textViewMessageFriend, textViewMessageTimeFriend;
        TextView imageViewMessageMineTime, imageViewMessageFriendTime;
        ImageView imageViewMessageMine, imageViewMessageFriend;
        CircleImageView avatarSeen, avatarSeenImage, avatarSeenAudio;

        ImageButton btnPlayAudioMessageMine, btnPlayAudioMessageFriend;
        TextView textViewMessageTimeAudioMine, textViewMessageTimeAudioFriend;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewMessageMine = itemView.findViewById(R.id.textViewMessageMine);
            textViewMessageTimeMine = itemView.findViewById(R.id.textViewMessageTimeMine);
            textViewMessageFriend = itemView.findViewById(R.id.textViewMessageFriend);
            textViewMessageTimeFriend = itemView.findViewById(R.id.textViewMessageTimeFriend);

            imageViewMessageMine = itemView.findViewById(R.id.imageViewMessageMine);
            imageViewMessageFriend = itemView.findViewById(R.id.imageViewMessageFriend);
            imageViewMessageMineTime = itemView.findViewById(R.id.imageViewMessageTimeMine);
            imageViewMessageFriendTime = itemView.findViewById(R.id.imageViewMessageTimeFriend);

            btnPlayAudioMessageMine = itemView.findViewById(R.id.btnPlayAudioMessageMine);
            btnPlayAudioMessageFriend = itemView.findViewById(R.id.btnPlayAudioMessageFriend);

            textViewMessageTimeAudioMine = itemView.findViewById(R.id.textViewAudioMessageTimeMine);
            textViewMessageTimeAudioFriend = itemView.findViewById(R.id.textViewAudioMessageTimeFriend);

//            avatarSeen = itemView.findViewById(R.id.avatarSeen);
//            avatarSeenImage = itemView.findViewById(R.id.avatarSeenImage);
//            avatarSeenAudio = itemView.findViewById(R.id.avatarSeenAudio);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (listMessages.isEmpty())
            return 0;
        else if (listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && !listMessages.get(position).isImage() && !listMessages.get(position).isAudio()) // tin nhắn bản thân là text
            return 1;
        else if (!listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && !listMessages.get(position).isImage() && !listMessages.get(position).isAudio()) // tín nhắn của bạn là text
            return 2;
        else if (listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && listMessages.get(position).isImage()) // tin nhắn của bản thân là ảnh
            return 3;
        else if (!listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && listMessages.get(position).isImage()) //tin nhắn của bạn là ảnh
            return 4;
        else if (listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && listMessages.get(position).isAudio()) { // tin nhắn của bản thân là tin âm thanh
            return 5;
        } else if (!listMessages.get(position).getUidSender().equals(FirebaseAuth.getInstance().getUid())
                && listMessages.get(position).isAudio()) { // tin nhắn của bạn là tin âm thanh
            return 6;
        }
        return 0;
    }

    @Override
    public RecyclerListMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // tạo view tin nhắn text của bản thân
        View viewMessageMine = LayoutInflater.from(parent.getContext()).inflate(rMessageMine, parent, false);
        // tạo view tin nhắn text của bạn
        View viewMessageFriend = LayoutInflater.from(parent.getContext()).inflate(rMessageFriend, parent, false);
        // tạo view tin nhắn hình của bản thân
        View viewImageMessageMine = LayoutInflater.from(parent.getContext()).inflate(rImageMessageMine, parent, false);
        // tạo view tin nhắn hình của bạn
        View viewImageMessageFriend = LayoutInflater.from(parent.getContext()).inflate(rImageMessageFriend, parent, false);

        View viewAudioMessageMine = LayoutInflater.from(parent.getContext()).inflate(rAudioMessageMine, parent, false);

        View viewAudioMessageFriend = LayoutInflater.from(parent.getContext()).inflate(rAudioMessageFriend, parent, false);

        ViewHolder viewHolder = null;

        switch (viewType) {
            case 0:
                viewHolder = null;
                break;
            case 1:
                viewHolder = new ViewHolder(viewMessageMine);
                break;
            case 2:
                viewHolder = new ViewHolder(viewMessageFriend);
                break;
            case 3:
                viewHolder = new ViewHolder(viewImageMessageMine);
                break;
            case 4:
                viewHolder = new ViewHolder(viewImageMessageFriend);
                break;
            case 5:
                viewHolder = new ViewHolder(viewAudioMessageMine);
                break;
            case 6:
                viewHolder = new ViewHolder(viewAudioMessageFriend);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerListMessageAdapter.ViewHolder holder, int position) {
        if (listMessages.isEmpty()) {
            //holder.textViewMessageNow.setVisibility(View.VISIBLE);
        } else {
            final Message msg = listMessages.get(position);
            if (msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && !msg.isImage() && !msg.isAudio()) {
                holder.textViewMessageMine.setText(msg.getContent());
                holder.textViewMessageTimeMine.setText(msg.getTimeMessage().substring(11, 16));
            } else if (!msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && !msg.isImage() && !msg.isAudio()) {
                holder.textViewMessageFriend.setText(msg.getContent());
                holder.textViewMessageTimeFriend.setText(msg.getTimeMessage().substring(11, 16));
            } else if (msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && msg.isImage()) {
                getImageStorage(holder.imageViewMessageMine, msg.getUidSender(), msg.getUidReceiver(), msg.getContent());
                holder.imageViewMessageMineTime.setText(msg.getTimeMessage().substring(11, 16));
                final String nameImage = msg.getContent();
                holder.imageViewMessageMine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = ((Activity) context).getFragmentManager();
                        ImageMessageFragment imageMessageFragment = ImageMessageFragment.newInstance(nameImage);
                        imageMessageFragment.show(fm, null);
                    }
                });
            } else if (!msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && msg.isImage()) {
                getImageStorage(holder.imageViewMessageFriend, msg.getUidSender(), msg.getUidReceiver(), msg.getContent());
                holder.imageViewMessageFriendTime.setText(msg.getTimeMessage().substring(11, 16));
                final String nameImage = msg.getContent();
                holder.imageViewMessageFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = ((Activity) context).getFragmentManager();
                        ImageMessageFragment imageMessageFragment = ImageMessageFragment.newInstance(nameImage);
                        imageMessageFragment.show(fm, null);
                    }
                });
            } else if (msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && msg.isAudio()) { // tin am thanh cua ban than
                holder.textViewMessageTimeAudioMine.setText(msg.getTimeMessage().substring(11, 16));
                holder.btnPlayAudioMessageMine.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPlayPressed(holder.btnPlayAudioMessageMine,msg.getUidSender(),
                                msg.getUidReceiver(),msg.getContent());
                    }
                });
            } else if (!msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && msg.isAudio()) { // tin am thanh cua ban
                holder.textViewMessageTimeAudioFriend.setText(msg.getTimeMessage().substring(11, 16));
                holder.btnPlayAudioMessageFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPlayPressed(holder.btnPlayAudioMessageFriend,msg.getUidSender(),
                                msg.getUidReceiver(),msg.getContent());
                    }
                });
            }

            // nếu tin nhắn của bản thân và vị trí tin nhắn cuối
//            if(msg.getUidSender().equals(FirebaseAuth.getInstance().getUid()) && (position == (listMessages.size() - 1))){
//                if(msg.isLastMessageSeen() && !msg.isAudio() && !msg.isAudio()){
//                    getAvatarSeen(holder.avatarSeen,listMessages.get(listMessages.size() - 1).getUidReceiver());
//                    holder.avatarSeen.setVisibility(View.VISIBLE);
//                }
//                else if(msg.isLastMessageSeen() && msg.isImage()){
//                    getAvatarSeen(holder.avatarSeenImage,listMessages.get(listMessages.size() - 1).getUidReceiver());
//                    holder.avatarSeenImage.setVisibility(View.VISIBLE);
//                }
//                else if(msg.isLastMessageSeen() && msg.isAudio()){
//                    getAvatarSeen(holder.avatarSeenAudio,listMessages.get(listMessages.size() - 1).getUidReceiver());
//                    holder.avatarSeenAudio.setVisibility(View.VISIBLE);
//                }
//            }
        }
    }

    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    public void getAvatarSeen(final CircleImageView avtSeen, String uidFriend) {
        StorageReference ref = storageReference.child("avatar").child(uidFriend + "avatar.jpg");
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "apho_images");
            if (!root.exists()) {
                root.mkdirs();
            }

            final File gpxfile = new File(root, uidFriend + "avatar.jpg");

            if (gpxfile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(gpxfile.getAbsolutePath());
                avtSeen.setImageBitmap(bmp);
            } else {
                ref.getFile(gpxfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(gpxfile.getAbsolutePath());
                        avtSeen.setImageBitmap(bmp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        avtSeen.setImageResource(R.drawable.avatar_default);
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void getImageStorage(final ImageView imageView, String senderUid, String receiverUid, String nameImage) {
        StorageReference ref = storageReference.child(senderUid).child(receiverUid).child(nameImage);
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "apho_images");
            if (!root.exists()) {
                root.mkdirs();
            }

            final File gpxfile = new File(root, nameImage);

            if (gpxfile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(gpxfile.getAbsolutePath());
                imageView.setImageBitmap(bmp);
            } else {
                ref.getFile(gpxfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bmp = BitmapFactory.decodeFile(gpxfile.getAbsolutePath());
                        imageView.setImageBitmap(bmp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        imageView.setImageResource(R.drawable.not_found_image);
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void onPlayPressed(final ImageButton btnPlay, String uidSender, String uidReceiver, String nameAudio) {
        if(!nameAudio.equals(currentAudio)&& mPlayer!=null)
        {
            btnCurrentPlay.setImageResource(R.drawable.icon_play_message_audio);
            stopPlaying();
        }
        if (mPlayer == null) {
            getAudio(uidSender, uidReceiver, nameAudio);
            btnPlay.setImageResource(R.drawable.icon_stop_audio_message);
            currentAudio = nameAudio;
            getCurrentMedia = mPlayer;
            btnCurrentPlay = btnPlay;
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlay.setImageResource(R.drawable.icon_play_message_audio);
                    stopPlaying();
                }
            });
        } else {
            if(mPlayer.isPlaying()) {
                mPlayer.pause();
                btnPlay.setImageResource(R.drawable.icon_pause_audio_message);
            }
            else if(!mPlayer.isPlaying()) {
                mPlayer.start();
                btnPlay.setImageResource(R.drawable.icon_stop_audio_message);
            }
            else{
                stopPlaying();
            }
        }
    }

    private  void stopPlaying() {
        if (null != mPlayer) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
            currentAudio="";
            btnCurrentPlay=null;
            getCurrentMedia=null;
        }
    }

    public void getAudio(String senderUid, String receiverUid,String nameAudio) {
        mPlayer = new MediaPlayer();
        StorageReference ref = storageReference.child("audio").child(senderUid).child(receiverUid).child(nameAudio);
        try {

            final File localFile = File.createTempFile("audio", "mp3");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    mFileName = localFile.getAbsolutePath();
                    try {
                        mPlayer.setDataSource(mFileName);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e("Error", "Couldn't prepare and start MediaPlayer");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context,"Không tải được tập tin âm thanh, vui lòng kiểm tra lại",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}