package com.ghj.btcontrol.adapter;

import static com.ghj.btcontrol.data.BTCConstants.MY_TEXT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ghj.btcontrol.R;
import com.ghj.btcontrol.data.ConnectData;

import java.util.List;

public class AdapterConnect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    List<ConnectData> mDatas;

    public AdapterConnect(Context context, List<ConnectData> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MY_TEXT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_text, parent, false);
            return new HolderMyText(view);
        }
        else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_file, parent, false);
            return new HolderMyFile(view);
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
            ((HolderMyFile) holder).txtFilesize.setText("" + data.getFilesize());
            ((HolderMyFile) holder).txtProgress.setText("" + data.getState());
        }
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
