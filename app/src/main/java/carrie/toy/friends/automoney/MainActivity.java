/*
 * Created by Christian Schabesberger on 02.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DownloadActivity.java is part of NewPipe.
 * <p>
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

package carrie.toy.friends.automoney;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;

import carrie.toy.friends.automoney.fragments.BackPressable;
import carrie.toy.friends.automoney.fragments.MainFragment;
import carrie.toy.friends.automoney.fragments.detail.VideoDetailFragment;
import carrie.toy.friends.automoney.fragments.list.search.SearchFragment;
import carrie.toy.friends.automoney.util.Constants;
import carrie.toy.friends.automoney.util.NavigationHelper;
import carrie.toy.friends.automoney.util.PermissionHelper;
import carrie.toy.friends.automoney.util.PreferenceUtil;
import carrie.toy.friends.automoney.util.ServiceHelper;
import carrie.toy.friends.automoney.util.StateSaver;
import carrie.toy.friends.automoney.util.ThemeHelper;
import gun0912.tedadhelper.backpress.OnBackPressListener;
import gun0912.tedadhelper.backpress.TedBackPressDialog;
import kr.co.inno.autocash.service.AutoServiceActivity;
import carrie.toy.friends.automoney.BuildConfig;
import carrie.toy.friends.automoney.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    private ActionBarDrawerToggle toggle = null;
    private DrawerLayout drawer = null;
    private NavigationView drawerItems = null;
    private TextView headerServiceView = null;
    private Context context;
    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        ThemeHelper.setTheme(this, ServiceHelper.getSelectedServiceId(this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            initFragments();
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        setupDrawer();
        auto_service();
    }
    private void auto_service() {
        Intent intent = new Intent(this, AutoServiceActivity.class);
        if(PreferenceUtil.getStringSharedData(this, PreferenceUtil.PREF_AD_STATUS, "N").equals("Y")){
            stopService(intent);
            startService(intent);
        }else{
            stopService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceUtil.setBooleanSharedData(this, PreferenceUtil.PREF_AD_VIEW, false);
    }

    private void setupDrawer() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        drawerItems = findViewById(R.id.navigation);

        for(StreamingService s : NewPipe.getServices()) {
            final String title = s.getServiceInfo().getName();
//            Log.i("dsu", "title : " + title);
            if(title.equals("YouTube")){
                final MenuItem item = drawerItems.getMenu()
                        .add(R.id.menu_services_group, s.getServiceId(), 0, getString(R.string.mode_video));
                item.setIcon(ServiceHelper.getIcon(s.getServiceId()));
            }else{
                final MenuItem item = drawerItems.getMenu()
                        .add(R.id.menu_services_group, s.getServiceId(), 0, getString(R.string.mode_audio));
                item.setIcon(ServiceHelper.getIcon(s.getServiceId()));
            }
        }

        drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(true);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        toggle.syncState();
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            private int lastService;

            @Override
            public void onDrawerOpened(View drawerView) {
                lastService = ServiceHelper.getSelectedServiceId(MainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (lastService != ServiceHelper.getSelectedServiceId(MainActivity.this)) {
                    new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
                }
            }
        });

        drawerItems.setNavigationItemSelectedListener(this::changeService);

        setupDrawerFooter();
        setupDrawerHeader();
    }

    public void restart(){

        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }

    public Handler handler = new Handler();
    private boolean changeService(MenuItem item) {
        if (item.getGroupId() == R.id.menu_services_group) {
            drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(false);
            ServiceHelper.setSelectedServiceId(this, item.getItemId());
            drawerItems.getMenu().getItem(ServiceHelper.getSelectedServiceId(this)).setChecked(true);
            finish();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    restart();
                }
            },500);
        } else {
            return false;
        }
        drawer.closeDrawers();
        return true;
    }

    private void setupDrawerFooter() {
        ImageButton settings = findViewById(R.id.drawer_settings);
        ImageButton downloads = findViewById(R.id.drawer_downloads);
        ImageButton history = findViewById(R.id.drawer_history);

        if(PreferenceUtil.getStringSharedData(this, PreferenceUtil.PREF_DOWNLOAD_STATUS, "Y").equals("Y")){
            downloads.setVisibility(View.VISIBLE);
        }else{
            downloads.setVisibility(View.GONE);
        }
        settings.setOnClickListener(view -> NavigationHelper.openSettings(this));
        downloads.setOnClickListener(view -> NavigationHelper.openDownloads(this));
        history.setOnClickListener(view ->
                NavigationHelper.openStatisticFragment(getSupportFragmentManager()));
    }

    public String appPackageName = "";
    private void setupDrawerHeader() {
        headerServiceView = findViewById(R.id.drawer_header_service_view);
        Button action = findViewById(R.id.drawer_header_action_button);
        action.setOnClickListener(view -> {
            /*Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://newpipe.schabi.org/blog/"));
            startActivity(intent);*/

            try {
                appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                Log.i("dsu", "appPackageName : " + appPackageName);
            }catch (NullPointerException e) {
            }catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }catch (Exception e) {
            }


            drawer.closeDrawers();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            StateSaver.clearStateFiles();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close drawer on return, and don't show animation, so its looks like the drawer isn't open
        // when the user returns to MainActivity
        drawer.closeDrawer(Gravity.START, false);
        /*try {
            String selectedServiceName = NewPipe.getService(
                    ServiceHelper.getSelectedServiceId(this)).getServiceInfo().getName();
            headerServiceView.setText(selectedServiceName);
        } catch (Exception e) {
            ErrorActivity.reportUiError(this, e);
        }*/

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "Theme has changed, recreating activity...");
            sharedPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
            // https://stackoverflow.com/questions/10844112/runtimeexception-performing-pause-of-activity-that-is-not-resumed
            // Briefly, let the activity resume properly posting the recreate call to end of the message queue
            new Handler(Looper.getMainLooper()).post(MainActivity.this::recreate);
        }

        if (sharedPreferences.getBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "main page has changed, recreating main fragment...");
            sharedPreferences.edit().putBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false).apply();
            NavigationHelper.openMainActivity(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        if (intent != null) {
            // Return if launched from a launcher (e.g. Nova Launcher, Pixel Launcher ...)
            // to not destroy the already created backstack
            String action = intent.getAction();
            if ((action != null && action.equals(Intent.ACTION_MAIN)) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) return;
        }

        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

     /*@Override
    public void onBackPressed() {

    }*/

    @Override
    public void onBackPressed() {
        if(!PreferenceUtil.getStringSharedData(this, PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
            if (DEBUG) Log.d(TAG, "onBackPressed() called");
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
            // If current fragment implements BackPressable (i.e. can/wanna handle back press) delegate the back press to it
            if (fragment instanceof BackPressable) {
                if (((BackPressable) fragment).onBackPressed()) return;
            }
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                TedBackPressDialog.startAdmobDialog(this, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                    @Override
                    public void onReviewClick() {
                        String packageName = "";
                        try {
                            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            packageName = getPackageName();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            Log.i("dsu", "packageName : " + packageName);
                        } catch (PackageManager.NameNotFoundException e) {
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                        }
                    }
                    @Override
                    public void onFinish() {
                        PreferenceUtil.setBooleanSharedData(context, PreferenceUtil.PREF_AD_VIEW, true);
                        finish();
                    }
                    @Override
                    public void onError(String errorMessage) {
                    }
                    @Override
                    public void onLoaded(int adType) {
                    }
                    @Override
                    public void onAdClicked(int adType) {
                    }
                });
            } else super.onBackPressed();
        }else{
            if (DEBUG) Log.d(TAG, "onBackPressed() called");

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
            // If current fragment implements BackPressable (i.e. can/wanna handle back press) delegate the back press to it
            if (fragment instanceof BackPressable) {
                if (((BackPressable) fragment).onBackPressed()) return;
            }


            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
            } else super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i: grantResults){
            if (i == PackageManager.PERMISSION_DENIED){
                return;
            }
        }
        switch (requestCode) {
            case PermissionHelper.DOWNLOADS_REQUEST_CODE:
                NavigationHelper.openDownloads(this);
                break;
            case PermissionHelper.DOWNLOAD_DIALOG_REQUEST_CODE:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
                if (fragment instanceof VideoDetailFragment) {
                    ((VideoDetailFragment) fragment).openDownloadDialog();
                }
                break;
        }
    }

    /**
     * Implement the following diagram behavior for the up button:
     * <pre>
     *              +---------------+
     *              |  Main Screen  +----+
     *              +-------+-------+    |
     *                      |            |
     *                      ▲ Up         | Search Button
     *                      |            |
     *                 +----+-----+      |
     *    +------------+  Search  |◄-----+
     *    |            +----+-----+
     *    |   Open          |
     *    |  something      ▲ Up
     *    |                 |
     *    |    +------------+-------------+
     *    |    |                          |
     *    |    |  Video    <->  Channel   |
     *    +---►|  Channel  <->  Playlist  |
     *         |  Video    <->  ....      |
     *         |                          |
     *         +--------------------------+
     * </pre>
     */
    private void onHomeButtonPressed() {
        // If search fragment wasn't found in the backstack...
        if (!NavigationHelper.tryGotoSearchFragment(getSupportFragmentManager())) {
            // ...go to the main fragment
            NavigationHelper.gotoMainFragment(getSupportFragmentManager());
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "]");
        super.onCreateOptionsMenu(menu);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (!(fragment instanceof VideoDetailFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_spinner).setVisibility(View.GONE);
        }

        if (!(fragment instanceof SearchFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_search_container).setVisibility(View.GONE);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);

            if(PreferenceUtil.getStringSharedData(this, PreferenceUtil.PREF_DOWNLOAD_STATUS, "Y").equals("Y")){
                menu.findItem(R.id.action_show_downloads).setVisible(true);
            }else{
                menu.findItem(R.id.action_show_downloads).setVisible(false);
            }

        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        updateDrawerNavigation();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onHomeButtonPressed();
                return true;
            case R.id.action_show_downloads:
                return NavigationHelper.openDownloads(this);
            case R.id.action_history:
                NavigationHelper.openStatisticFragment(getSupportFragmentManager());
                return true;
            /*case R.id.action_about:
                NavigationHelper.openAbout(this);
                return true;*/
            case R.id.action_settings:
                NavigationHelper.openSettings(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    private void initFragments() {
        if (DEBUG) Log.d(TAG, "initFragments() called");
        StateSaver.clearStateFiles();
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
            handleIntent(getIntent());
        } else NavigationHelper.gotoMainFragment(getSupportFragmentManager());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private void updateDrawerNavigation() {
        if (getSupportActionBar() == null) return;

        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (fragment instanceof MainFragment) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (toggle != null) {
                toggle.syncState();
                toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
            }
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onHomeButtonPressed());
        }
    }

    private void handleIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");

        if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
            String url = intent.getStringExtra(Constants.KEY_URL);
            int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
            String title = intent.getStringExtra(Constants.KEY_TITLE);
            switch (((StreamingService.LinkType) intent.getSerializableExtra(Constants.KEY_LINK_TYPE))) {
                case STREAM:
                    boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                    NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                    break;
                case CHANNEL:
                    NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
                case PLAYLIST:
                    NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
            }
        } else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
            String searchQuery = intent.getStringExtra(Constants.KEY_QUERY);
            if (searchQuery == null) searchQuery = "";
            int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
            NavigationHelper.openSearchFragment(getSupportFragmentManager(), serviceId, searchQuery);
        } else {
            NavigationHelper.gotoMainFragment(getSupportFragmentManager());
        }
    }
}
