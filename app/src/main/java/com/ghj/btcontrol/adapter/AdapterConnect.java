package com.ghj.btcontrol.adapter;

import static com.ghj.btcontrol.data.BTCConstants.MY_FILE;
import static com.ghj.btcontrol.data.BTCConstants.MY_TEXT;
import static com.ghj.btcontrol.data.BTCConstants.YOUR_TEXT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ghj.btcontrol.R;
import com.ghj.btcontrol.data.ConnectData;
import com.ghj.btcontrol.util.Util;

import java.util.List;

public class AdapterConnect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    String mRemoteName;
    List<ConnectData> mDatas;
    IConnectListener mListener;

    public AdapterConnect(Context context, String remoteName, List<ConnectData> datas) {
        this.mContext = context;
        this.mRemoteName = remoteName;
        this.mDatas = datas;
    }

    public void setConnectListener(IConnectListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MY_TEXT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_text, parent, false);
            return new HolderMyText(view);
        }
        else if(viewType == MY_FILE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_file, parent, false);
            return new HolderMyFile(view);
        }
        else if(viewType == YOUR_TEXT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_your_text, parent, false);
            return new HolderYourText(view);
        }
        else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_your_file, parent, false);
            return new HolderYourFile(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConnectData data = mDatas.get(position);

        if(holder instanceof HolderMyText) {
            ((HolderMyText) holder).txtText.setText(data.getText());
        }
        else if(holder instanceof HolderMyFile) {
            ((HolderMyFile) holder).txtFilename.setText(data.getFilename());
            ((HolderMyFile) holder).txtFilesize.setText(Util.CalculateFileSize(data.getFilesize()));
            ((HolderMyFile) holder).txtProgress.setText(data.getState());
        }
        else if(holder instanceof HolderYourText) {
            ((HolderYourText) holder).txtDeviceName.setText(mRemoteName);
            ((HolderYourText) holder).txtText.setText(data.getText());
        }
        else if(holder instanceof HolderYourFile) {
            ((HolderYourFile) holder).txtDeviceName.setText(mRemoteName);
            ((HolderYourFile) holder).txtFilename.setText(data.getFilename());
            ((HolderYourFile) holder).txtFilesize.setText(Util.CalculateFileSize(data.getFilesize()));
            ((HolderYourFile) holder).txtProgress.setText(data.getState());
        }

        holder.itemView.setOnClickListener(v -> {
            if(mListener != null) {
                mListener.onMessageClick(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getDataType();
    }

    public void addItem(ConnectData item) {
        this.mDatas.add(item);
    }


    public class HolderMyFile extends RecyclerView.ViewHolder {
        TextView txtFilename;
        TextView txtFilesize;
        TextView txtProgress;

        public HolderMyFile(@NonNull View itemView) {
            super(itemView);
            txtFilename = itemView.findViewById(R.id.txtFilename);
            txtFilesize = itemView.findViewById(R.id.txtFilesize);
            txtProgress = itemView.findViewById(R.id.txtProgress);
        }
    }

    public class HolderMyText extends RecyclerView.ViewHolder {
        TextView txtText;

        public HolderMyText(@NonNull View itemView) {
            super(itemView);
            txtText = itemView.findViewById(R.id.txtText);
        }
    }

    public class HolderYourFile extends RecyclerView.ViewHolder {
        TextView txtDeviceName;
        TextView txtFilename;
        TextView txtFilesize;
        TextView txtProgress;

        public HolderYourFile(@NonNull View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            txtFilename = itemView.findViewById(R.id.txtFilename);
            txtFilesize = itemView.findViewById(R.id.txtFilesize);
            txtProgress = itemView.findViewById(R.id.txtProgress);
        }
    }

    public class HolderYourText extends RecyclerView.ViewHolder {
        TextView txtDeviceName;
        TextView txtText;

        public HolderYourText(@NonNull View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtDeviceName);
            txtText = itemView.findViewById(R.id.txtText);
        }
    }
}
