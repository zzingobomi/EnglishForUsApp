package com.zzingobomi.englishforus.auth;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 1000;

    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleApiClient;

    MaterialDialog waitDialog;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 클릭 리스너 연결
        view.findViewById(R.id.sign_in_google).setOnClickListener(this);
        view.findViewById(R.id.sign_in_email).setOnClickListener(this);
        view.findViewById(R.id.create_account).setOnClickListener(this);

        mGoogleApiClient = ((MainActivity)getActivity()).mGoogleApiClient;

        mFirebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.sign_in_google) {

            // 구글 로그인 화면으로 전환
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);

        } else if(v.getId() == R.id.sign_in_email) {

            // 이메일 로그인 화면으로 전환
            EmailLoginFragment emailLoginFragment = new EmailLoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contnet_fragment_layout, emailLoginFragment)
                    .addToBackStack(null)
                    .commit();

        } else if(v.getId() == R.id.create_account) {

            // 이메일 회원가입 화면으로 전환
            EmailCreateAccountFragment createEmailAccountFragment = new EmailCreateAccountFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contnet_fragment_layout, createEmailAccountFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        waitDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.common_wait_progress_title)
                .content(R.string.common_wait_progress_content)
                .progress(true, 0)
                .cancelable(false)
                .show();

        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                waitDialog.dismiss();
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                if(task.isSuccessful()) {
                    waitDialog.dismiss();
                    Log.d(TAG, "signInWithCredential");

                    FirebaseUser user = mFirebaseAuth.getCurrentUser();

                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    Uri photoUri = user.getPhotoUrl();

                    // Nav 메뉴에 유저 이름 넣기
                    ((MainActivity)getActivity()).setUserInfoNavHeader(name, photoUri);

                    // 토큰 갱신하기
                    Log.d("TOKEN", "Google Refresh Token");
                    FirebaseTokenManager.getInstance().refreshToken(getContext(), user);

                    // TODO: 우선 메인으로.. 후에 바로 문장관리, 뺑글이 후에도 꽤 딜레이가 있네.. startActivity 문제..?
                    startActivity(new Intent(getActivity(), MainActivity.class));

                } else {
                    waitDialog.dismiss();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "signInWithCredential", task.getException());
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
