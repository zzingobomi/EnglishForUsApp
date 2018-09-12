package com.zzingobomi.hellohappy.myitemmanage;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.zzingobomi.hellohappy.auth.EmailCreateAccountFragment;
import com.zzingobomi.hellohappy.auth.FirebaseTokenManager;
import com.zzingobomi.hellohappy.vo.Item;
import com.zzingobomi.hellohappy.MainActivity;
import com.zzingobomi.hellohappy.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddItemFragment extends Fragment {
    private OnAddItemPostExecuteListener mListener;
    public interface OnAddItemPostExecuteListener {
        void OnAddItemPostExecute(Item item);
    }

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mListener = (OnAddItemPostExecuteListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity)context).getLocalClassName() + " 는 OnAddItemPostExecuteListener를 구현해야 합니다.");
        }
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

    private static class HttpAddItemAsyncTask extends AsyncTask<String, Void, Item> {

        private WeakReference<AddItemFragment> fragmentWeakReference;
        OkHttpClient client = new OkHttpClient();
        MaterialDialog waitDialog;

        HttpAddItemAsyncTask(AddItemFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
            waitDialog = new MaterialDialog.Builder(fragment.getActivity())
                    .title(R.string.common_wait_progress_title)
                    .content(R.string.common_wait_progress_content)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Item doInBackground(String... params) {

            // for debug worker thread
            if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            String strUrl = params[0];
            String strTitleKo = params[1];
            String strTitleEn = params[2];
            String strAddInfo = params[3];
            String strRegIdEmail = params[4];
            String strRegDisplayName = params[5];
            Item resultItem = null;
            //Item item = new Item(strTitleKo, strTitleEn, strAddInfo, strRegIdEmail, strRegDisplayName);

            try {
                JsonObject json = new JsonObject();
                json.addProperty("idtoken", FirebaseTokenManager.getInstance().getToken());
                json.addProperty("title_ko", strTitleKo);
                json.addProperty("title_en", strTitleEn);
                json.addProperty("addinfo", strAddInfo);
                json.addProperty("regidemail", strRegIdEmail);
                json.addProperty("regdisplayname", strRegDisplayName);
                RequestBody requestBody = RequestBody.create(JSON, json.toString());

                /*
                // 다른 좋은 방법 있기 전까지... 그냥 값 집어넣어서 보내기 ItemVO 에 맵핑하는 방법이 있을텐데..
                Gson gson = new Gson();
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8")
                        ,gson.toJson(item));
                        */

                Request request = new Request.Builder()
                        .url(strUrl)
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();

                if(response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("HttpAddItemAsyncTask", "UNAUTHORIZED");
                    return null;
                }

                // TimeStamp(DB 시간) to Date(Java 시간) 를 위해
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                Type listType = new TypeToken<Item>() {}.getType();
                resultItem = gson.fromJson(response.body().string(), listType);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultItem;
        }

        @Override
        protected void onPostExecute(Item resultItem) {
            super.onPostExecute(resultItem);
            waitDialog.dismiss();
            if(resultItem != null) {
                Log.d(TAG, "Response:SUCCESS");

                final AddItemFragment fragment = fragmentWeakReference.get();
                if(fragment == null || fragment.isDetached()) return;

                fragment.mListener.OnAddItemPostExecute(resultItem);
                fragment.getActivity().getSupportFragmentManager()
                        .popBackStack();
            }
        }
    }

    //endregion
}
