package com.zzingobomi.englishforus.myitemmanage;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.zzingobomi.englishforus.auth.EmailCreateAccountFragment;
import com.zzingobomi.englishforus.vo.Item;
import com.zzingobomi.englishforus.MainActivity;
import com.zzingobomi.englishforus.R;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddItemFragment extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase 인증
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    // 화면 UI 부분
    private static LinearLayout mCreateItemLayout;
    private EditText mAddTitle_KO;
    private EditText mAddTitle_EN;
    private EditText mAddAddInfo;
    private Button mAddButton;
    private static LinearLayout mCreateResultLayout;


    public AddItemFragment() {
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

            View view = inflater.inflate(R.layout.fragment_add_item, container, false);

            mCreateItemLayout = view.findViewById(R.id.create_item_layout);
            mAddTitle_KO = view.findViewById(R.id.add_title_ko);
            mAddTitle_EN = view.findViewById(R.id.add_title_en);
            mAddAddInfo = view.findViewById(R.id.add_addinfo);
            mAddButton = view.findViewById(R.id.add_button);
            mCreateResultLayout = view.findViewById(R.id.create_result_layout);

            mCreateItemLayout.setVisibility(View.VISIBLE);
            mCreateResultLayout.setVisibility(View.GONE);


            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickAddItemBtn(v);
                }
            });
            return view;
    }

    private void onClickAddItemBtn(View v) {
        if (!validateForm()) {
            return;
        }

        mCreateItemLayout.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        String titleKo = mAddTitle_KO.getText().toString();
        String titleEn = mAddTitle_EN.getText().toString();
        String addInfo = mAddAddInfo.getText().toString();
        String regIdEmail = mUser.getEmail();
        String regDisplayName = mUser.getDisplayName();

        // API 보내기
        new HttpAddItemAsyncTask(this).execute("http://englishforus.zzingobomi.synology.me/itemapi/oneadditem"
                ,titleKo
                ,titleEn
                ,addInfo
                ,regIdEmail
                ,regDisplayName
        );
    }

    private boolean validateForm() {
        boolean valid = true;

        String titleKo = mAddTitle_KO.getText().toString();
        if (TextUtils.isEmpty(titleKo)) {
            mAddTitle_KO.setError("Required.");
            valid = false;
        } else {
            mAddTitle_KO.setError(null);
        }

        String titleEn = mAddTitle_EN.getText().toString();
        if (TextUtils.isEmpty(titleEn)) {
            mAddTitle_EN.setError("Required.");
            valid = false;
        } else {
            mAddTitle_EN.setError(null);
        }

        return valid;
    }

    //region 네트워크 영역

    private static class HttpAddItemAsyncTask extends AsyncTask<String, Void, String> {

        private WeakReference<AddItemFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpAddItemAsyncTask(AddItemFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.wait_progress_title)
                    .content(R.string.wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected String doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            String strResponse = "";

            String strUrl = params[0];
            String strTitleKo = params[1];
            String strTitleEn = params[2];
            String strAddInfo = params[3];
            String strRegIdEmail = params[4];
            String strRegDisplayName = params[5];
            Item item = new Item(strTitleKo, strTitleEn, strAddInfo, strRegIdEmail, strRegDisplayName);

            try {
                Gson gson = new Gson();
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8")
                        ,gson.toJson(item));

                Request request = new Request.Builder().url(strUrl).post(requestBody).build();
                Response response = client.newCall(request).execute();
                strResponse = response.body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return strResponse;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            waitDialog.dismiss();
            if(response.equals("SUCCESS")) {
                Log.d(TAG, "Response:SUCCESS");

                final AddItemFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                // 서버에서 추가된 ItemVO 를 주고..

                // 내 문장 관리 화면으로 돌아가기
                // 다시 문장들 요청하는건 너무 비효율 적이고.. 추가된 문장만 받아서 클라에서 제일 위에 추가해 주는게 좋을듯..?
                MyItemManageFragment myItemManageFragment = new MyItemManageFragment();
                fragment.getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contnet_fragment_layout, myItemManageFragment)
                        .commit();
            }
        }
    }

    //endregion
}
