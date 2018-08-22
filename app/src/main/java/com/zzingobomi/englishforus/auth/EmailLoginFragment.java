package com.zzingobomi.englishforus.auth;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zzingobomi.englishforus.myitemmanage.AddItemFragment;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmailLoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // FirebaseAnalytics 관련
    public FirebaseAnalytics mFirebaseAnalytics;

    // Firebase 인증
    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;

    public EmailLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_login, container, false);

        mFirebaseAnalytics = ((MainActivity)getActivity()).mFirebaseAnalytics;

        mAuth = FirebaseAuth.getInstance();

        mEmailField = view.findViewById(R.id.email_login_email_field);
        mPasswordField = view.findViewById(R.id.email_login_password_field);

        view.findViewById(R.id.email_sign_in_button).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.email_sign_in_button) {
            if (!validateForm()) {
                return;
            }

            final MaterialDialog waitDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        waitDialog.dismiss();

                        FirebaseUser user = mAuth.getCurrentUser();

                        // 현재 사용자 이름, 포토는 null 이지만 사용자 관리에서 이름, 포토 등 업데이트 함수 있다.
                        // Name, email address, and profile photo Url
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        Uri photoUri = user.getPhotoUrl();

                        // Check if user's email is verified
                        boolean emailVerified = user.isEmailVerified();
                        if(emailVerified == false) {
                            mAuth.signOut();
                            Toast.makeText(getActivity(), "아직 이메일이 인증되지 않았습니다. 이메일을 인증해 주십시오.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // The user's ID, unique to the Firebase project. Do NOT use this value to
                        // authenticate with your backend server, if you have one. Use
                        // FirebaseUser.getToken() instead.
                        String uid = user.getUid();

                        Log.d(TAG, " name : " + name + " email : " + email + " photoUrl : " + photoUri + " emailVerified : " + emailVerified + " uid : " + uid);

                        // 로그인 성공
                        Toast.makeText(getActivity(), "로그인 되었습니다.", Toast.LENGTH_SHORT).show();

                        // 토큰 갱신하기
                        Log.d("TOKEN", "Email Refresh Token");
                        FirebaseTokenManager.getInstance().refreshToken(getContext(), user);

                        // Nav 메뉴에 유저 이름 넣기
                        ((MainActivity)getActivity()).setUserInfoNavHeader(name, photoUri);

                        // Login Analytics 쌓기
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, name);
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

                        // TODO: 우선 메인으로.. 후에 바로 문장관리
                        startActivity(new Intent(getActivity(), MainActivity.class));

                        /*
                        // 문장 입력 화면으로 전환 (모든 백스탭이 다 날라가는듯.. 좀 더 연구..)
                        // FragmentManager.popBackStack(String name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                            fm.popBackStack();
                        }

                        AddItemFragment addItemFragment = new AddItemFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.contnet_fragment_layout, addItemFragment)
                                .commit();
                                */

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getActivity(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();

                        waitDialog.dismiss();
                    }
                }
            });
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }
}
