package com.bphvcg.apho.Fragments;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bphvcg.apho.R;

import java.io.File;

public class ImageMessageFragment extends DialogFragment {

    ImageView imageMessageFragment;
    Button btnCloseImageMessageFragment;

    //Được dùng khi khởi tạo dialog mục đích nhận giá trị
    public static ImageMessageFragment newInstance(String nameImage) {
        ImageMessageFragment dialog = new ImageMessageFragment();
        Bundle args = new Bundle();
        args.putString("nameImage", nameImage);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Hình ảnh tin nhắn");
        return inflater.inflate(R.layout.dialog_fragment_image, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // lấy giá trị tự bundle
        String nameImage = getArguments().getString("nameImage", "");
        imageMessageFragment = (ImageView)view.findViewById(R.id.imageMessageFragment);
        btnCloseImageMessageFragment = (Button)view.findViewById(R.id.btnCloseImageMessageFragment);

        File root = new File(Environment.getExternalStorageDirectory(), "apho_images");
        if (!root.exists()) {
            root.mkdirs();
        }
        final File gpxfile = new File(root, nameImage);
        if(gpxfile.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(gpxfile.getAbsolutePath());
            imageMessageFragment.setImageBitmap(bmp);
        }

        btnCloseImageMessageFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }
}
