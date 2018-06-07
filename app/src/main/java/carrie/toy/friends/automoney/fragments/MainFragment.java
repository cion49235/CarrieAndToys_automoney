package carrie.toy.friends.automoney.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.admixer.CustomPopup;
import com.admixer.CustomPopupListener;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.kiosk.KioskList;

import carrie.toy.friends.automoney.App;
import carrie.toy.friends.automoney.BaseFragment;
import carrie.toy.friends.automoney.fragments.list.channel.ChannelFragment;
import carrie.toy.friends.automoney.fragments.list.kiosk.KioskFragment;
import carrie.toy.friends.automoney.local.bookmark.BookmarkFragment;
import carrie.toy.friends.automoney.local.feed.FeedFragment;
import carrie.toy.friends.automoney.local.subscription.SubscriptionFragment;
import carrie.toy.friends.automoney.report.ErrorActivity;
import carrie.toy.friends.automoney.report.UserAction;
import carrie.toy.friends.automoney.util.KioskTranslator;
import carrie.toy.friends.automoney.util.NavigationHelper;
import carrie.toy.friends.automoney.util.PreferenceUtil;
import carrie.toy.friends.automoney.util.ServiceHelper;
import carrie.toy.friends.automoney.util.ThemeHelper;
import carrie.toy.friends.automoney.util.Utils;
import carrie.toy.friends.automoney.R;

public class MainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener, CustomPopupListener {

    public int currentServiceId = -1;
    private ViewPager viewPager;

    /*//////////////////////////////////////////////////////////////////////////
    // Constants
    //////////////////////////////////////////////////////////////////////////*/

    private static final int FALLBACK_SERVICE_ID = ServiceList.YouTube.getServiceId();
    private static final String FALLBACK_CHANNEL_URL = "https://www.youtube.com";
    private static final String FALLBACK_CHANNEL_NAME = "Music";
    private static final String FALLBACK_KIOSK_ID = "Trending";
    private static final int KIOSK_MENU_OFFSET = 2000;

    /*//////////////////////////////////////////////////////////////////////////
    // Fragment's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentServiceId = ServiceHelper.getSelectedServiceId(activity);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        TabLayout tabLayout = rootView.findViewById(R.id.main_tab_layout);
        viewPager = rootView.findViewById(R.id.pager);

        /*  Nested fragment, use child fragment here to maintain backstack in view pager. */
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());

        tabLayout.setupWithViewPager(viewPager);

        int channelIcon = ThemeHelper.resolveResourceIdFromAttr(activity, R.attr.ic_channel);
        int whatsHotIcon = ThemeHelper.resolveResourceIdFromAttr(activity, R.attr.ic_hot);
        int bookmarkIcon = ThemeHelper.resolveResourceIdFromAttr(activity, R.attr.ic_bookmark);

        if (isSubscriptionsPageOnlySelected()) {
            tabLayout.getTabAt(0).setIcon(channelIcon);
            tabLayout.getTabAt(1).setIcon(bookmarkIcon);
        } else {
            tabLayout.getTabAt(0).setIcon(whatsHotIcon);
            tabLayout.getTabAt(1).setIcon(channelIcon);
            tabLayout.getTabAt(2).setIcon(bookmarkIcon);
        }

        //	  Custom Popup 시작
        CustomPopup.setCustomPopupListener(this);
        CustomPopup.startCustomPopup(getActivity(), "wf36608f");

        if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
            admob_ad(rootView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Custom Popup 종료
        CustomPopup.stopCustomPopup();
    }

    private com.google.android.gms.ads.AdView adView;
    private void admob_ad(View view){
        com.admixer.AdMixerManager.getInstance().setAdapterDefaultAppCode(com.admixer.AdAdapter.ADAPTER_ADMIXER, "wf36608f");
		adView = new com.google.android.gms.ads.AdView(getActivity());
		adView.setAdUnitId(getString(R.string.admob_banner_key));
		adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
		android.widget.RelativeLayout layout = (android.widget.RelativeLayout)view.findViewById(R.id.ad_layout);
		layout.addView(adView);
		// 기본 요청을 시작합니다.
		com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
		// 광고 요청으로 adView를 로드합니다.
		adView.loadAd(adRequest);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/
    Menu currentMenu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
        inflater.inflate(R.menu.main_fragment_menu, menu);
        if(PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_DOWNLOAD_STATUS, "Y").equals("Y")){
            menu.findItem(R.id.action_show_downloads).setVisible(true);
        }else{
            menu.findItem(R.id.action_show_downloads).setVisible(false);
        }

        if(PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_SEARCH_STATUS, "Y").equals("Y")){
            menu.findItem(R.id.action_search).setVisible(true);
        }else{
            menu.findItem(R.id.action_search).setVisible(false);
        }


        /*SubMenu kioskMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 200, getString(R.string.kiosk));
        try {
            createKioskMenu(kioskMenu, inflater);
        } catch (Exception e) {
            ErrorActivity.reportError(activity, e,
                    activity.getClass(),
                    null,
                    ErrorActivity.ErrorInfo.make(UserAction.UI_ERROR,
                            "none", "", R.string.app_ui_crash));
        }*/

        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                NavigationHelper.openSearchFragment(getFragmentManager(), ServiceHelper.getSelectedServiceId(activity), "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Tabs
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onStartedCustomPopup() {

    }

    @Override
    public void onWillShowCustomPopup(String s) {

    }

    @Override
    public void onShowCustomPopup(String s) {

    }

    @Override
    public void onWillCloseCustomPopup(String s) {

    }

    @Override
    public void onCloseCustomPopup(String s) {

    }

    @Override
    public void onHasNoCustomPopup() {

    }

    private class PagerAdapter extends FragmentPagerAdapter {
        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return isSubscriptionsPageOnlySelected() ? new SubscriptionFragment() : getMainPageFragment();
                case 1:
                    if(PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString(getString(R.string.main_page_content_key), getString(R.string.blank_page_key))
                            .equals(getString(R.string.subscription_page_key))) {
                        return new BookmarkFragment();
                    } else {
                        return new SubscriptionFragment();
                    }
                case 2:
                    return new BookmarkFragment();
                default:
                    return new BlankFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return getString(this.tabTitles[position]);
            return "";
        }

        @Override
        public int getCount() {
            return isSubscriptionsPageOnlySelected() ? 2 : 3;
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Main page content
    //////////////////////////////////////////////////////////////////////////*/

    private boolean isSubscriptionsPageOnlySelected() {
        return PreferenceManager.getDefaultSharedPreferences(activity)
                .getString(getString(R.string.main_page_content_key), getString(R.string.blank_page_key))
                .equals(getString(R.string.subscription_page_key));
    }
    String setMainPage = "blank_page";
    private Fragment getMainPageFragment() {
        if (getActivity() == null) return new BlankFragment();

        try {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            Log.i("dsu", "서비스아이디 : " + ServiceHelper.getSelectedServiceId(getActivity()));
            if(PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_MAIN_STATUS, "Y").equals("Y")){
                if(ServiceHelper.getSelectedServiceId(getActivity()) == 0){
                    setMainPage = getString(R.string.channel_page_key);
                }else{
                    setMainPage = getString(R.string.blank_page_key);
                }
            }else{
                setMainPage = getString(R.string.blank_page_key);
            }

            if (setMainPage.equals(getString(R.string.blank_page_key))) {
                return new BlankFragment();
            } else if (setMainPage.equals(getString(R.string.kiosk_page_key))) {
                int serviceId = preferences.getInt(getString(R.string.main_page_selected_service),
                        FALLBACK_SERVICE_ID);
                String kioskId = preferences.getString(getString(R.string.main_page_selectd_kiosk_id),
                        FALLBACK_KIOSK_ID);
                KioskFragment fragment = KioskFragment.getInstance(serviceId, kioskId);
                fragment.useAsFrontPage(true);
                return fragment;
            } else if (setMainPage.equals(getString(R.string.feed_page_key))) {
                FeedFragment fragment = new FeedFragment();
                fragment.useAsFrontPage(true);
                return fragment;
            } else if (setMainPage.equals(getString(R.string.channel_page_key))) {
                int serviceId = preferences.getInt(getString(R.string.main_page_selected_service),
                        FALLBACK_SERVICE_ID);
                String url = preferences.getString(getString(R.string.main_page_selected_channel_url),
                        FALLBACK_CHANNEL_URL + PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_CHANNEL, "/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ"));
                if(Utils.language(getActivity()).equals("ko_KR")){
                    url = preferences.getString(getString(R.string.main_page_selected_channel_url),
                            FALLBACK_CHANNEL_URL + PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_CHANNEL2, "/channel/UCwF16hfz5TLOkf6TzIcGYfw"));
                }else{
                    url = preferences.getString(getString(R.string.main_page_selected_channel_url),
                            FALLBACK_CHANNEL_URL + PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_CHANNEL, "/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ"));
                }
                String name = preferences.getString(getString(R.string.main_page_selected_channel_name),
                        FALLBACK_CHANNEL_NAME);
                ChannelFragment fragment = ChannelFragment.getInstance(serviceId, url, name);
                fragment.useAsFrontPage(true);
                return fragment;
            } else {
                return new BlankFragment();
            }

        } catch (Exception e) {
            ErrorActivity.reportError(activity, e,
                    activity.getClass(),
                    null,
                    ErrorActivity.ErrorInfo.make(UserAction.UI_ERROR,
                            "none", "", R.string.app_ui_crash));
            return new BlankFragment();
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Select Kiosk
    //////////////////////////////////////////////////////////////////////////*/

    private void createKioskMenu(Menu menu, MenuInflater menuInflater)
            throws Exception {
        StreamingService service = NewPipe.getService(currentServiceId);
        KioskList kl = service.getKioskList();
        int i = 0;
        for (final String ks : kl.getAvailableKiosks()) {
            menu.add(0, KIOSK_MENU_OFFSET + i, Menu.NONE,
                    KioskTranslator.getTranslatedKioskName(ks, getContext()))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            try {
                                NavigationHelper.openKioskFragment(getFragmentManager(), currentServiceId, ks);
                            } catch (Exception e) {
                                ErrorActivity.reportError(activity, e,
                                        activity.getClass(),
                                        null,
                                        ErrorActivity.ErrorInfo.make(UserAction.UI_ERROR,
                                                "none", "", R.string.app_ui_crash));
                            }
                            return true;
                        }
                    });
            i++;
        }
    }
}
