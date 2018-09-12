package com.zzingobomi.hellohappy;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.zzingobomi.hellohappy.auth.FirebaseTokenManager;
import com.zzingobomi.hellohappy.auth.LoginFragment;
import com.zzingobomi.hellohappy.myitemmanage.AddItemFragment;
import com.zzingobomi.hellohappy.myitemmanage.MyItemManageFragment;
import com.zzingobomi.hellohappy.ranking.RankingShowFragment;
import com.zzingobomi.hellohappy.study.StudyFragment;
import com.zzingobomi.hellohappy.vo.Item;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, StudyFragment.OnStudyCountPlusListener, AddItemFragment.OnAddItemPostExecuteListener {
    // FirebaseAnalytics 관련
    public FirebaseAnalytics mFirebaseAnalytics;

    // Firebase 인증 관련
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public GoogleApiClient mGoogleApiClient;

    private String mUsername;
    private Uri mPhotoUri;

    private NavigationView mNavigationView;

    private Fragment mCurrentFragment;

    // 공부 횟수(광고 관련)
    private int mCurrentStudyCount = 0;        // DB 에 저장.. 아니면 로컬에 저장..? 생각좀 해보자
    private int mMaxStudyAdsCount = 10;
    private InterstitialAd mInterstitialAd;

    MaterialDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상태바 색상 바꾸기
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.GRAY);
        }

        setContentView(R.layout.activity_main);

        // 파이어베이스 애널리틱스
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // 애드몹
        MobileAds.initialize(this, "ca-app-pub-5834632059694330~2579526498");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {
            // 첫화면 Home 으로
            goHome();
        } else {
            // 음 이 방법밖에 없나..
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Firebase 인증
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            mUsername = mFirebaseUser.getDisplayName();
            mPhotoUri = mFirebaseUser.getPhotoUrl();

            setUserInfoNavHeader(mUsername, mPhotoUri);

            // 토큰 갱신하기
            Log.d("TOKEN", "MainActivity Refresh Token");
            FirebaseTokenManager.getInstance().refreshToken(getApplicationContext(), mFirebaseUser);
        } else {
            setUserInfoNavHeader(getString(R.string.common_anonymous_name), null);
        }

        // 전면 광고
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }



    @Override
    public void onStudyCountPlusListener() {
        mCurrentStudyCount++;
        if(mCurrentStudyCount > 0 && (mCurrentStudyCount % mMaxStudyAdsCount) == 0) {
            if(mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_login) {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if(mFirebaseUser != null) {
                // 이메일이 인증되었는지 확인
                boolean emailVerified = mFirebaseUser.isEmailVerified();
                if(emailVerified == false) {
                    mFirebaseAuth.signOut();
                    Toast.makeText(this, getResources().getString(R.string.common_email_notauth), Toast.LENGTH_SHORT).show();
                    return false;
                }

                Toast.makeText(this, getResources().getString(R.string.common_already_login), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                onNavigationItemSelected(mNavigationView.getMenu().getItem(2));
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }

            return true;
        } else if(id == R.id.action_logout) {
            new MaterialDialog.Builder(this)
                    .title(R.string.common_logout_title)
                    .content(R.string.common_logout_content)
                    .positiveText(R.string.common_agree)
                    .negativeText(R.string.common_disagree)
                    .positiveColor(Color.BLACK)
                    .negativeColor(Color.BLACK)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mFirebaseAuth.signOut();
                            if(mGoogleApiClient.isConnected()) {
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                            }
                            mUsername = "";
                            setUserInfoNavHeader(getString(R.string.common_anonymous_name), null);
                            goHome();
                        }
                    })
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if(id==R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        } else if (id == R.id.nav_study) {
            fragment = new StudyFragment();
            title = "Study";
        } else if (id == R.id.nav_myitemmanage) {

            // 처음에 왜 한번 끊기는가.. Profiler 봐야 할듯.. 로그인 화면 만들때만 끊기는가?
            // <com.google.android.gms.common.SignInButton> 이 버튼을 제거하니 메모리도 적게 먹고 빨라지긴 했는데..
            // 그냥 이 버튼을 불러만 와도 엄청난 렉이;; 이전 FirebaseChatting 앱도 프로파일러 확인해 보자

            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if(mFirebaseUser != null) {
                // 이메일이 인증되었는지 확인
                boolean emailVerified = mFirebaseUser.isEmailVerified();
                if(emailVerified == false) {
                    mFirebaseAuth.signOut();
                    Toast.makeText(this, getResources().getString(R.string.common_email_notauth), Toast.LENGTH_SHORT).show();
                    return false;
                }

                fragment = new MyItemManageFragment();           // 여기서 메모리 누수가 나는듯..?
                title = "ItemManage";
            } else {
                //mGoogleApiClient.stopAutoManage(this);
                //mGoogleApiClient.disconnect();

                fragment = new LoginFragment();
                title = "Login";
            }

        } else if (id == R.id.nav_ranking) {
            fragment = new RankingShowFragment();
            title = "Ranking";
        } else if (id == R.id.nav_bymade) {
            Toast.makeText(this, "보미야 사랑해", Toast.LENGTH_SHORT).show();
        }

        mCurrentFragment = fragment;

        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contnet_fragment_layout, fragment)
                    .commit();
        }

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 꼭 필요한가..?
    public void goHome() {
        onNavigationItemSelected(mNavigationView.getMenu().getItem(0));
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    public void setUserInfoNavHeader(String name, Uri photoUri) {
        // 유저 이름
        View nav_header_view = mNavigationView.getHeaderView(0);
        TextView nav_header_name = (TextView)nav_header_view.findViewById(R.id.nav_header_name);
        nav_header_name.setText(name + " 님");

        // 유저 Photo
        ImageView nav_header_photo = (ImageView)nav_header_view.findViewById(R.id.nav_header_photo);
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        if(photoUri != null) {
            Glide.with(this)
                    .load(photoUri)
                    .apply(options)
                    .into(nav_header_photo);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_anonymous_user)
                    .apply(options)
                    .into(nav_header_photo);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
        FirebaseTokenManager.getInstance().stopTokenRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Firebase 인증
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            // 토큰 갱신하기
            FirebaseTokenManager.getInstance().refreshToken(getApplicationContext(), mFirebaseUser);
        } else {
            Log.d("TAG", "onResume Firebase User null");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Service error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("Rotation", "true");

        super.onSaveInstanceState(outState);
    }

    ///
    /// NewItem 추가 되었을때 호출되는 Listener
    ///
    @Override
    public void OnAddItemPostExecute(Item item) {
        if(mCurrentFragment instanceof MyItemManageFragment) {
            ((MyItemManageFragment) mCurrentFragment).addNewItem(item);
        }
    }
}
