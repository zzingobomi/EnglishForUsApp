package com.zzingobomi.englishforus.myitemmanage;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyItemManageFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase 인증
    FirebaseAuth mAuth;
    FirebaseUser mUser;


    public MyItemManageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser == null) {
            // 로그인 안되어 있음.. 다시 돌아가기
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_my_item_manage, container, false);



        return view;
    }

}
