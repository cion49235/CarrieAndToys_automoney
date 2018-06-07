package carrie.toy.friends.automoney.download;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.admixer.AdInfo;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;

import carrie.toy.friends.automoney.settings.NewPipeSettings;
import carrie.toy.friends.automoney.settings.SettingsActivity;
import carrie.toy.friends.automoney.util.ThemeHelper;
import carrie.toy.friends.automoney.R;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.ui.fragment.AllMissionsFragment;
import us.shandian.giga.ui.fragment.MissionsFragment;

public class DownloadActivity extends AppCompatActivity implements InterstitialAdListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Service
        Intent i = new Intent();
        i.setClass(this, DownloadManagerService.class);
        startService(i);

        ThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init_ui();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.downloads_title);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        // Fragment
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateFragments();
                getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        addInterstitialView();
    }

    private InterstitialAd interstialAd;
    public void addInterstitialView() {
        if(interstialAd == null) {
            AdInfo adInfo = new AdInfo("wf36608f");
//        	adInfo.setTestMode(false);
            interstialAd = new InterstitialAd(this);
            interstialAd.setAdInfo(adInfo, this);
            interstialAd.setInterstitialAdListener(this);
            interstialAd.startInterstitial();
        }
    }

    private TextView txt_path_video, txt_path_audio;
    private void init_ui(){
        txt_path_video = (TextView)findViewById(R.id.txt_path_video);
        txt_path_audio = (TextView)findViewById(R.id.txt_path_audio);
        txt_path_video.setText(getString(R.string.download_path_title) +"\n" + NewPipeSettings.getVideoDownloadPath(this));
        txt_path_audio.setText(getString(R.string.download_path_audio_title) +"\n" + NewPipeSettings. getAudioDownloadPath(this));
    }

    private void updateFragments() {

        MissionsFragment fragment = new AllMissionsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.download_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onInterstitialAdReceived(String s, InterstitialAd interstitialAd) {
        interstialAd = null;
    }

    @Override
    public void onInterstitialAdFailedToReceive(int i, String s, InterstitialAd interstitialAd) {
        interstialAd = null;
    }

    @Override
    public void onInterstitialAdClosed(InterstitialAd interstitialAd) {
        interstialAd = null;
    }

    @Override
    public void onInterstitialAdShown(String s, InterstitialAd interstitialAd) {

    }

    @Override
    public void onLeftClicked(String s, InterstitialAd interstitialAd) {

    }

    @Override
    public void onRightClicked(String s, InterstitialAd interstitialAd) {

    }
}
