package com.bphvcg.apho.Fragments;

import android.app.DialogFragment;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bphvcg.apho.Interfaces.GetAudioFromRecordFragment;
import com.bphvcg.apho.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioMessageFragment  extends DialogFragment {
    TextView txtclock;
    ToggleButton btnRecord;
    private Button btnCancelRecord;
    private static final String TAG = "AudioRecordTest";
    private String nameAudio;
    private String mFileName;
    private MediaRecorder mRecorder;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    CountDownTimer t;
    int cnt;
    Uri uri;

    private GetAudioFromRecordFragment getAudioFromRecordFragment;

    //Được dùng khi khởi tạo dialog mục đích nhận giá trị
    public static AudioMessageFragment newInstance(String uidFriend) {
        AudioMessageFragment dialog = new AudioMessageFragment();
        Bundle args = new Bundle();
        args.putString("UID_Friend", uidFriend); // lay de push du lieu len storage
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Gửi tập tin âm thanh");
        return inflater.inflate(R.layout.dialog_fragment_audio, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtclock =  view.findViewById(R.id.txtclock);
        btnRecord = view.findViewById(R.id.record_button);
        btnCancelRecord = view.findViewById(R.id.btnCancelRecord);
        btnCancelRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        try{
            getAudioFromRecordFragment = (GetAudioFromRecordFragment)getActivity();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        nameAudio = UUID.randomUUID().toString(); // tên random
        //nameAudio = "cinchaomoinguoihihi";
        String myfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apho_audios/";

        File dir = new File(myfile);
        if(!dir.exists())
            dir.mkdirs();

        mFileName = myfile + nameAudio + ".mp3";

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        uri = Uri.fromFile(new File(mFileName));

        btnRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // Set enabled state
                // Start/stop recording
                onRecordPressed(isChecked);
            }
        });

        cnt = 0;
        t = new CountDownTimer( Long.MAX_VALUE , 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                cnt++;
                String time = new Integer(cnt).toString();

                long millis = cnt;
                int seconds = (int) (millis / 60);
                int minutes = seconds / 60;
                seconds     = seconds % 60;

                txtclock.setText(String.format("%d:%02d:%02d", minutes, seconds,millis));

            }

            @Override
            public void onFinish() {            }
        };

    }
    private void onRecordPressed(boolean shouldStartRecording) {

        if (shouldStartRecording) {
            startRecording();
            t.start();
        } else {
            stopRecording();
            //uploadRecord();
            t.cancel();
            showYesNoDialog();
        }

    }
    // bắt đầu ghi ấm với MediaRecorder
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Không thể bắt đầu ghi âm, vui lòng kiểm tra lại!");
        }
        mRecorder.start();
    }

    // dừng recording. giải phóng tài nguyên
    private void stopRecording() {
        if (null != mRecorder) {
            try{
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }catch(RuntimeException stopException){
                //handle cleanup here
                Toast.makeText(getActivity(),"Không lắng nghe được âm thanh vui lòng thử lại!!",Toast.LENGTH_LONG).show();
            }

        }

    }

    private void showYesNoDialog(){
        final MyDialogFragment yesNoAlert = MyDialogFragment.newInstance(
                "Data to Send");
        yesNoAlert.show(getFragmentManager(), "yesNoAlert");

        yesNoAlert.setOnYesNoClick(new MyDialogFragment.OnYesNoClick() {
            @Override
            public void onYesClicked() {
                uploadRecord();
                yesNoAlert.dismiss();
                getDialog().dismiss();
            }

            @Override
            public void onNoClicked() {
                yesNoAlert.dismiss();
                getDialog().dismiss();
            }
        });
    }
    private void uploadRecord() {

        if (uri != null) {
            String uidFriend = getArguments().getString("UID_Friend", "");
            storageReference = storage.getReference();
            StorageReference ref = storageReference.child("audio").child(FirebaseAuth.getInstance().getUid())
                    .child(uidFriend).child(nameAudio + ".mp3");
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            getAudioFromRecordFragment.getAudioName(nameAudio + ".mp3");
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

}
