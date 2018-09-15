package com.zzingobomi.hellohappy;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zzingobomi.hellohappy.vo.Item;
import com.zzingobomi.hellohappy.vo.RankingManyItemVO;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Result;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // 해피의 문장
    TextView happy_title_ko;
    TextView happy_title_en;
    TextView happy_addinfo;

    // 홈화면의 순위표
    ImageView[] manyitem_medal_image = new ImageView[3];
    TextView[] manyitem_medal_text = new TextView[3];

    ImageView[] manyitem_user_photo = new ImageView[3];
    TextView[] manyitem_displayname = new TextView[3];
    TextView[] manyitem_count = new TextView[3];

    public interface HelloHappyService {
        @GET("itemapi/happyitem")
        Call<Item> getHappyItem();

        @POST("rankingapi/topregistuser")
        Call<List<RankingManyItemVO>> rankingManyItemList(@Body RequestBody body);
    }

    private AdView mAdViewHomeBanner;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 해피의 문장
        happy_title_ko = view.findViewById(R.id.happy_title_ko);
        happy_title_en = view.findViewById(R.id.happy_title_en);
        happy_addinfo = view.findViewById(R.id.happy_addinfo);

        // 순위표 View
        View home_ranking_1 = view.findViewById(R.id.home_ranking_1);
        View home_ranking_2 = view.findViewById(R.id.home_ranking_2);
        View home_ranking_3 = view.findViewById(R.id.home_ranking_3);

        manyitem_medal_image[0] = home_ranking_1.findViewById(R.id.ranking_manyitem_card_medal_image);
        manyitem_medal_image[1] = home_ranking_2.findViewById(R.id.ranking_manyitem_card_medal_image);
        manyitem_medal_image[2] = home_ranking_3.findViewById(R.id.ranking_manyitem_card_medal_image);

        manyitem_medal_text[0] = home_ranking_1.findViewById(R.id.ranking_manyitem_card_medal_text);
        manyitem_medal_text[1] = home_ranking_2.findViewById(R.id.ranking_manyitem_card_medal_text);
        manyitem_medal_text[2] = home_ranking_3.findViewById(R.id.ranking_manyitem_card_medal_text);

        manyitem_user_photo[0] = home_ranking_1.findViewById(R.id.ranking_manyitem_card_photo);
        manyitem_user_photo[1] = home_ranking_2.findViewById(R.id.ranking_manyitem_card_photo);
        manyitem_user_photo[2] = home_ranking_3.findViewById(R.id.ranking_manyitem_card_photo);

        manyitem_displayname[0] = home_ranking_1.findViewById(R.id.ranking_manyitem_card_displayname);
        manyitem_displayname[1] = home_ranking_2.findViewById(R.id.ranking_manyitem_card_displayname);
        manyitem_displayname[2] = home_ranking_3.findViewById(R.id.ranking_manyitem_card_displayname);

        manyitem_count[0] = home_ranking_1.findViewById(R.id.ranking_manyitem_card_count);
        manyitem_count[1] = home_ranking_2.findViewById(R.id.ranking_manyitem_card_count);
        manyitem_count[2] = home_ranking_3.findViewById(R.id.ranking_manyitem_card_count);

        // Admob
        mAdViewHomeBanner = view.findViewById(R.id.adview_home_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewHomeBanner.loadAd(adRequest);

        // TimeStamp(DB 시간) to Date(Java 시간) 를 위해 Gson
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = builder.create();

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://englishforus.zzingobomi.synology.me/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        HelloHappyService service = retrofit.create(HelloHappyService.class);

        // 해피의 문장 받아오기
        Call<Item> call = service.getHappyItem();
        call.enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if(response.isSuccessful()) {
                    Log.d("getHappyItem", "SUCCESS");
                    Item happyItem = response.body();

                    happy_title_ko.setText(happyItem.getTitle_ko());
                    happy_title_en.setText(happyItem.getTitle_en());
                    happy_addinfo.setText(happyItem.getAddinfo());
                } else {
                    Log.d("getHappyItem", response.message());
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.d("getHappyItem", t.toString());
                t.printStackTrace();
            }
        });

        // 문장개수 Top3 받아오기 -- 홈이 아닌 시작시에..? 홈화면 올때마다 받아오는듯..
        JsonObject json = new JsonObject();
        json.addProperty("rank", "3");
        RequestBody body = RequestBody.create(JSON, json.toString());

        Call<List<RankingManyItemVO>> rankingCall = service.rankingManyItemList(body);
        rankingCall.enqueue(new Callback<List<RankingManyItemVO>>() {
            @Override
            public void onResponse(Call<List<RankingManyItemVO>> call, Response<List<RankingManyItemVO>> response) {
                if(response.isSuccessful()) {
                    Log.d("rankingManyItemList", "SUCCESS");

                    List<RankingManyItemVO> results = response.body();
                    int idx = 0;
                    for(RankingManyItemVO vo : results) {
                        manyitem_medal_image[idx].setVisibility(View.VISIBLE);
                        manyitem_medal_text[idx].setVisibility(View.GONE);
                        if(idx == 0) {
                            Glide.with(getActivity())
                                    .load(R.drawable.medal_1)
                                    .into(manyitem_medal_image[idx]);
                        } else if(idx == 1) {
                            Glide.with(getActivity())
                                    .load(R.drawable.medal_2)
                                    .into(manyitem_medal_image[idx]);
                        } else {
                            Glide.with(getActivity())
                                    .load(R.drawable.medal_3)
                                    .into(manyitem_medal_image[idx]);
                        }

                        RequestOptions options = new RequestOptions();
                        options.circleCrop();
                        if(vo.getRegphotourl() != null) {
                            Glide.with(getActivity())
                                    .load(vo.getRegphotourl())
                                    .apply(options)
                                    .into(manyitem_user_photo[idx]);
                        } else {
                            Glide.with(getActivity())
                                    .load(R.drawable.ic_anonymous_user)
                                    .apply(options)
                                    .into(manyitem_user_photo[idx]);
                        }
                        manyitem_displayname[idx].setText(vo.getRegdisplayname());
                        manyitem_count[idx].setText(String.valueOf(vo.getCount()) + " 개");

                        idx++;
                    }
                } else {
                    Log.d("rankingManyItemList", response.message());
                }
            }

            @Override
            public void onFailure(Call<List<RankingManyItemVO>> call, Throwable t) {
                Log.d("rankingManyItemList", t.toString());
                t.printStackTrace();
            }
        });

        return view;
    }

}
