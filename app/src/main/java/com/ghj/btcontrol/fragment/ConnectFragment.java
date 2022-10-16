package com.ghj.btcontrol.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ghj.btcontrol.BaseFragmentActivity;
import com.ghj.btcontrol.R;

public class ConnectFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( getActivity() instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) getActivity()).addToFragmentStack(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putBoolean("RESCAN", true);
        getParentFragmentManager().setFragmentResult("ConnectFragment", bundle);
    }
}
