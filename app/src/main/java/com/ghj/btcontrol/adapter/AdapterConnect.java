package com.ghj.btcontrol.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ghj.btcontrol.R;

public class AdapterConnect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
