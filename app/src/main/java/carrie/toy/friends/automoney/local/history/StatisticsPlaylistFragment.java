package carrie.toy.friends.automoney.local.history;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.admixer.AdInfo;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import carrie.toy.friends.automoney.App;
import carrie.toy.friends.automoney.database.LocalItem;
import carrie.toy.friends.automoney.database.stream.StreamStatisticsEntry;
import carrie.toy.friends.automoney.info_list.InfoItemDialog;
import carrie.toy.friends.automoney.local.BaseLocalListFragment;
import carrie.toy.friends.automoney.player.playqueue.PlayQueue;
import carrie.toy.friends.automoney.player.playqueue.SinglePlayQueue;
import carrie.toy.friends.automoney.report.UserAction;
import carrie.toy.friends.automoney.util.NavigationHelper;
import carrie.toy.friends.automoney.util.OnClickGesture;
import carrie.toy.friends.automoney.util.PermissionHelper;
import carrie.toy.friends.automoney.util.PreferenceUtil;
import carrie.toy.friends.automoney.util.ThemeHelper;
import gun0912.tedadhelper.backpress.OnBackPressListener;
import gun0912.tedadhelper.backpress.TedBackPressDialog;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import carrie.toy.friends.automoney.R;

public class StatisticsPlaylistFragment
        extends BaseLocalListFragment<List<StreamStatisticsEntry>, Void> implements InterstitialAdListener {

    private View headerPlayAllButton;
    private View headerPopupButton;
    private View headerBackgroundButton;
    private View playlistCtrl;
    private View sortButton;
    private ImageView sortButtonIcon;
    private TextView sortButtonText;

    @State
    protected Parcelable itemsListState;

    /* Used for independent events */
    private Subscription databaseSubscription;
    private HistoryRecordManager recordManager;
    private CompositeDisposable disposables = new CompositeDisposable();

    private enum StatisticSortMode {
        LAST_PLAYED,
        MOST_PLAYED,
    }

    StatisticSortMode sortMode = StatisticSortMode.LAST_PLAYED;

    protected List<StreamStatisticsEntry> processResult(final List<StreamStatisticsEntry> results) {
        switch (sortMode) {
            case LAST_PLAYED:
                Collections.sort(results, (left, right) ->
                    right.latestAccessDate.compareTo(left.latestAccessDate));
                return results;
            case MOST_PLAYED:
                Collections.sort(results, (left, right) ->
                        ((Long) right.watchCount).compareTo(left.watchCount));
                return results;
            default: return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Creation
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordManager = new HistoryRecordManager(getContext());
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Views
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);
        setTitle(getString(R.string.title_last_played));
        if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
            admob_ad(rootView);
        }
    }

    @Override
    protected View getListHeader() {
        final View headerRootLayout = activity.getLayoutInflater().inflate(R.layout.statistic_playlist_control,
                itemsList, false);
        playlistCtrl = headerRootLayout.findViewById(R.id.playlist_control);
        headerPlayAllButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_all_button);
        headerPopupButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_popup_button);
        headerBackgroundButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_bg_button);
        sortButton = headerRootLayout.findViewById(R.id.sortButton);
        sortButtonIcon = headerRootLayout.findViewById(R.id.sortButtonIcon);
        sortButtonText = headerRootLayout.findViewById(R.id.sortButtonText);
        return headerRootLayout;
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        itemListAdapter.setSelectedListener(new OnClickGesture<LocalItem>() {
            @Override
            public void selected(LocalItem selectedItem) {
                if (selectedItem instanceof StreamStatisticsEntry) {
                    final StreamStatisticsEntry item = (StreamStatisticsEntry) selectedItem;
                    NavigationHelper.openVideoDetailFragment(getFragmentManager(),
                            item.serviceId, item.url, item.title);
                }
            }

            @Override
            public void held(LocalItem selectedItem) {
                if (selectedItem instanceof StreamStatisticsEntry) {
                    showStreamDialog((StreamStatisticsEntry) selectedItem);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Loading
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void startLoading(boolean forceLoad) {
        super.startLoading(forceLoad);
        recordManager.getStreamStatistics()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getHistoryObserver());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Destruction
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
        itemsListState = itemsList.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (itemListAdapter != null) itemListAdapter.unsetSelectedListener();
        if (headerBackgroundButton != null) headerBackgroundButton.setOnClickListener(null);
        if (headerPlayAllButton != null) headerPlayAllButton.setOnClickListener(null);
        if (headerPopupButton != null) headerPopupButton.setOnClickListener(null);

        if (databaseSubscription != null) databaseSubscription.cancel();
        databaseSubscription = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recordManager = null;
        itemsListState = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Statistics Loader
    ///////////////////////////////////////////////////////////////////////////

    private Subscriber<List<StreamStatisticsEntry>> getHistoryObserver() {
        return new Subscriber<List<StreamStatisticsEntry>>() {
            @Override
            public void onSubscribe(Subscription s) {
                showLoading();

                if (databaseSubscription != null) databaseSubscription.cancel();
                databaseSubscription = s;
                databaseSubscription.request(1);
            }

            @Override
            public void onNext(List<StreamStatisticsEntry> streams) {
                handleResult(streams);
                if (databaseSubscription != null) databaseSubscription.request(1);
            }

            @Override
            public void onError(Throwable exception) {
                StatisticsPlaylistFragment.this.onError(exception);
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private InterstitialAd interstialAd;
    public void addInterstitialView() {
        if(interstialAd == null) {
            AdInfo adInfo = new AdInfo("wf36608f");
//        	adInfo.setTestMode(false);
            interstialAd = new InterstitialAd(getActivity());
            interstialAd.setAdInfo(adInfo, getActivity());
            interstialAd.setInterstitialAdListener(this);
            interstialAd.startInterstitial();
        }
    }

    @Override
    public void handleResult(@NonNull List<StreamStatisticsEntry> result) {
        super.handleResult(result);
        if (itemListAdapter == null) return;

        playlistCtrl.setVisibility(View.VISIBLE);

        itemListAdapter.clearStreamItemList();

        if (result.isEmpty()) {
            showEmptyState();
            return;
        }

        itemListAdapter.addItems(processResult(result));
        if (itemsListState != null) {
            itemsList.getLayoutManager().onRestoreInstanceState(itemsListState);
            itemsListState = null;
        }
        headerPlayAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
                    TedBackPressDialog.startAdmobDialog(activity, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                        @Override
                        public void onReviewClick() {
                            String packageName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                packageName = activity.getPackageName();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            } catch (PackageManager.NameNotFoundException e) {
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                            }
                        }
                        @Override
                        public void onFinish() {
                            NavigationHelper.playOnMainPlayer(activity, getPlayQueue());
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
                }else{
                    NavigationHelper.playOnMainPlayer(activity, getPlayQueue());
                }
            }
        });
        headerPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
                    /*TedBackPressDialog.startAdmobDialog(activity, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                        @Override
                        public void onReviewClick() {
                            String packageName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                packageName = activity.getPackageName();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            } catch (PackageManager.NameNotFoundException e) {
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                            }
                        }
                        @Override
                        public void onFinish() {
                            NavigationHelper.playOnPopupPlayer(activity, getPlayQueue());
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
                    });*/
                    if (PermissionHelper.isPopupEnabled(getActivity())) {
                        addInterstitialView();
                    }
                    NavigationHelper.playOnPopupPlayer(activity, getPlayQueue());
                }else{
                    NavigationHelper.playOnPopupPlayer(activity, getPlayQueue());
                }
            }
        });
        headerBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
                    TedBackPressDialog.startAdmobDialog(activity, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                        @Override
                        public void onReviewClick() {
                            String packageName = "";
                            try {
                                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                                packageName = activity.getPackageName();
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            } catch (PackageManager.NameNotFoundException e) {
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                            }
                        }
                        @Override
                        public void onFinish() {
                            NavigationHelper.playOnBackgroundPlayer(activity, getPlayQueue());
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
                }else{
                    NavigationHelper.playOnBackgroundPlayer(activity, getPlayQueue());
                }
            }
        });
        sortButton.setOnClickListener(view -> toggleSortMode());

        hideLoading();
    }
    ///////////////////////////////////////////////////////////////////////////
    // Fragment Error Handling
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void resetFragment() {
        super.resetFragment();
        if (databaseSubscription != null) databaseSubscription.cancel();
    }

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;

        onUnrecoverableError(exception, UserAction.SOMETHING_ELSE,
                "none", "History Statistics", R.string.general_error);
        return true;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private void toggleSortMode() {
        if(sortMode == StatisticSortMode.LAST_PLAYED) {
            sortMode = StatisticSortMode.MOST_PLAYED;
            setTitle(getString(R.string.title_most_played));
            sortButtonIcon.setImageResource(ThemeHelper.getIconByAttr(R.attr.history, getContext()));
            sortButtonText.setText(R.string.title_last_played);
        } else {
            sortMode = StatisticSortMode.LAST_PLAYED;
            setTitle(getString(R.string.title_last_played));
            sortButtonIcon.setImageResource(ThemeHelper.getIconByAttr(R.attr.filter, getContext()));
            sortButtonText.setText(R.string.title_most_played);
        }
        startLoading(true);
    }

    private void showStreamDialog(final StreamStatisticsEntry item) {
        final Context context = getContext();
        final Activity activity = getActivity();
        if (context == null || context.getResources() == null || getActivity() == null) return;
        final StreamInfoItem infoItem = item.toStreamInfoItem();

        final String[] commands = new String[]{
                context.getResources().getString(R.string.enqueue_on_background),
                context.getResources().getString(R.string.enqueue_on_popup),
                context.getResources().getString(R.string.start_here_on_main),
                context.getResources().getString(R.string.start_here_on_background),
                context.getResources().getString(R.string.start_here_on_popup),
                context.getResources().getString(R.string.delete),
        };

        final DialogInterface.OnClickListener actions = (dialogInterface, i) -> {
            final int index = Math.max(itemListAdapter.getItemsList().indexOf(item), 0);
            switch (i) {
                case 0:
                    NavigationHelper.enqueueOnBackgroundPlayer(context, new SinglePlayQueue(infoItem));
                    break;
                case 1:
                    NavigationHelper.enqueueOnPopupPlayer(activity, new SinglePlayQueue(infoItem));
                    break;
                case 2:
                    NavigationHelper.playOnMainPlayer(context, getPlayQueue(index));
                    break;
                case 3:
                    NavigationHelper.playOnBackgroundPlayer(context, getPlayQueue(index));
                    break;
                case 4:
                    NavigationHelper.playOnPopupPlayer(activity, getPlayQueue(index));
                    break;
                case 5:
                    deleteEntry(index);
                    break;
                default:
                    break;
            }
        };

        new InfoItemDialog(getActivity(), infoItem, commands, actions).show();
    }

    private void deleteEntry(final int index) {
        final LocalItem infoItem = itemListAdapter.getItemsList()
                .get(index);
        if(infoItem instanceof StreamStatisticsEntry) {
            final StreamStatisticsEntry entry = (StreamStatisticsEntry) infoItem;
            final Disposable onDelete = recordManager.deleteStreamHistory(entry.streamId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            howManyDelted -> {
                                if(getView() != null) {
                                    Snackbar.make(getView(), R.string.one_item_deleted,
                                            Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            R.string.one_item_deleted,
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> showSnackBarError(throwable,
                                    UserAction.DELETE_FROM_HISTORY, "none",
                                    "Deleting item failed", R.string.general_error));

            disposables.add(onDelete);
        }
    }

    private PlayQueue getPlayQueue() {
        return getPlayQueue(0);
    }

    private PlayQueue getPlayQueue(final int index) {
        if (itemListAdapter == null) {
            return new SinglePlayQueue(Collections.emptyList(), 0);
        }

        final List<LocalItem> infoItems = itemListAdapter.getItemsList();
        List<StreamInfoItem> streamInfoItems = new ArrayList<>(infoItems.size());
        for (final LocalItem item : infoItems) {
            if (item instanceof StreamStatisticsEntry) {
                streamInfoItems.add(((StreamStatisticsEntry) item).toStreamInfoItem());
            }
        }
        return new SinglePlayQueue(streamInfoItems, index);
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

