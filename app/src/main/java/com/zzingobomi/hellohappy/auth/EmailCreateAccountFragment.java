package com.zzingobomi.hellohappy.auth;


import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.zzingobomi.hellohappy.MainActivity;
import com.zzingobomi.hellohappy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailCreateAccountFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase 인증
    private FirebaseAuth mAuth;

    private LinearLayout mEmailCreateLayout;
    private EditText mNameFiled;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPhotoUrlField;
    private LinearLayout mEmailCreateVerificationLayout;
    private Button mEmailVerificationButton;
    private Button mEmailVerificationEndButton;


    public EmailCreateAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email_create_account, container, false);

        mAuth = FirebaseAuth.getInstance();

        mEmailCreateLayout = view.findViewById(R.id.email_create_layout);
        mNameFiled = view.findViewById(R.id.email_create_name_field);
        mEmailField = view.findViewById(R.id.email_create_email_field);
        mPasswordField = view.findViewById(R.id.email_create_password_field);
        mPhotoUrlField = view.findViewById(R.id.email_create_photourl_field);
        mEmailCreateVerificationLayout = view.findViewById(R.id.email_create_verification_layout);
        mEmailVerificationButton = view.findViewById(R.id.email_create_verification_button);
        mEmailVerificationEndButton = view.findViewById(R.id.email_create_verification_end_button);

        view.findViewById(R.id.email_create_button).setOnClickListener(this);
        mEmailVerificationButton.setOnClickListener(this);
        mEmailVerificationEndButton.setOnClickListener(this);

        // 이메일 인증 화면은 우선 안보이게
        mEmailCreateVerificationLayout.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.email_create_button) {
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

            final String name = mNameFiled.getText().toString();
            final String email = mEmailField.getText().toString();
            final String password = mPasswordField.getText().toString();
            final String photoUrl = mPhotoUrlField.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");

                        // 가입한 유저 이름, photoUrl 업데이트
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(Uri.parse(photoUrl))
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String name = user.getDisplayName();
                                    String email = user.getEmail();
                                    if(user.getPhotoUrl() != null) {
                                        String photoUrl = user.getPhotoUrl().toString();
                                    }

                                    mEmailCreateLayout.setVisibility(View.GONE);
                                    mEmailCreateVerificationLayout.setVisibility(View.VISIBLE);

                                    waitDialog.dismiss();
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure - updateProfile", task.getException());
                                    waitDialog.dismiss();
                                }
                            }
                        });

                        // 가입 성공
                        //Toast.makeText(getActivity(), "회원가입 되었습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure - createUserWithEmailAndPassword", task.getException());
                        Toast.makeText(getActivity(), "회원가입에 실패했습니다. 비밀번호가 6자리 이상인지 또는 이미 회원가입이 되어있는지 확인해 주십시오.", Toast.LENGTH_SHORT).show();
                        waitDialog.dismiss();
                    }
                }
            });
        } else if(v.getId() == R.id.email_create_verification_button) {
            mEmailVerificationButton.setEnabled(false);
            mEmailVerificationEndButton.setVisibility(View.VISIBLE);

            // 얘도  null 체크 해줘야 하나;;
            final FirebaseUser notEmailVerifiedUuser = mAuth.getCurrentUser();
            notEmailVerifiedUuser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mEmailVerificationButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(),"인증 이메일을 " + notEmailVerifiedUuser.getEmail() + " 로 보냈습니다. 이메일에서 링크를 클릭하여 가입을 완료해 주십시오.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(getActivity(),"인증 이메일을 " + notEmailVerifiedUuser.getEmail() + " 로 보내기를 실패했습니다. 이메일을 다시 한번 확인해 주십시오.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else if(v.getId() == R.id.email_create_verification_end_button) {

            // 로그인 화면으로 전환 (모든 백스탭이 다 날라가는듯.. 좀 더 연구..)
            // FragmentManager.popBackStack(String name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            FragmentManager fm = getActivity().getSupportFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }

            LoginFragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contnet_fragment_layout, loginFragment)
                    .commit();

            /*
            // 여기서는 못 받아오고 다시 로그인을 해야 이메일이 인증되었다는걸 알 수 있나?
            mAuth = FirebaseAuth.getInstance();
            final FirebaseUser user = mAuth.getCurrentUser();
            if(user.isEmailVerified()) {

                // 문장 입력 화면으로 전환 (모든 백스탭이 다 날라가는듯.. 좀 더 연구..)
                // FragmentManager.popBackStack(String name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                FragmentManager fm = getActivity().getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }

                LoginFragment loginFragment = new LoginFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contnet_fragment_layout, loginFragment)
                        .commit();

                Toast.makeText(getActivity(), "가입을 축하합니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),"인증 이메일을 " + user.getEmail() + " 로 보냈습니다. 이메일에서 링크를 클릭하여 가입을 완료해 주십시오.", Toast.LENGTH_SHORT).show();
            }
            */
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = mNameFiled.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameFiled.setError("필수요소입니다.");
            valid = false;
        } else {
            mNameFiled.setError(null);
        }

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("필수요소입니다.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("필수요소입니다.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }
}
