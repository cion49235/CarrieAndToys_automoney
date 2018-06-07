package carrie.toy.friends.automoney.fragments.list.channel;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admixer.AdInfo;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;
import com.jakewharton.rxbinding2.view.RxView;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import carrie.toy.friends.automoney.App;
import carrie.toy.friends.automoney.BaseFragment;
import carrie.toy.friends.automoney.database.subscription.SubscriptionEntity;
import carrie.toy.friends.automoney.fragments.list.BaseListInfoFragment;
import carrie.toy.friends.automoney.info_list.InfoItemDialog;
import carrie.toy.friends.automoney.local.dialog.PlaylistAppendDialog;
import carrie.toy.friends.automoney.local.subscription.SubscriptionService;
import carrie.toy.friends.automoney.player.playqueue.ChannelPlayQueue;
import carrie.toy.friends.automoney.player.playqueue.PlayQueue;
import carrie.toy.friends.automoney.player.playqueue.SinglePlayQueue;
import carrie.toy.friends.automoney.report.UserAction;
import carrie.toy.friends.automoney.util.AnimationUtils;
import carrie.toy.friends.automoney.util.ExtractorHelper;
import carrie.toy.friends.automoney.util.ImageDisplayConstants;
import carrie.toy.friends.automoney.util.Localization;
import carrie.toy.friends.automoney.util.NavigationHelper;
import carrie.toy.friends.automoney.util.PermissionHelper;
import carrie.toy.friends.automoney.util.PreferenceUtil;
import gun0912.tedadhelper.backpress.OnBackPressListener;
import gun0912.tedadhelper.backpress.TedBackPressDialog;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import carrie.toy.friends.automoney.R;


public class ChannelFragment extends BaseListInfoFragment<ChannelInfo> implements InterstitialAdListener {

    private CompositeDisposable disposables = new CompositeDisposable();
    private Disposable subscribeButtonMonitor;
    private SubscriptionService subscriptionService;

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private View headerRootLayout;
    private ImageView headerChannelBanner;
    private ImageView headerAvatarView;
    private TextView headerTitleView;
    private TextView headerSubscribersTextView;
    private Button headerSubscribeButton;
    private View playlistCtrl;

    private LinearLayout headerPlayAllButton;
    private LinearLayout headerPopupButton;
    private LinearLayout headerBackgroundButton;

//    private MenuItem menuRssButton;

    public static ChannelFragment getInstance(int serviceId, String url, String name) {
        ChannelFragment instance = new ChannelFragment();
        instance.setInitialData(serviceId, url, name);
        return instance;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(activity != null
                && useAsFrontPage
                && isVisibleToUser) {
            setTitle(currentInfo != null ? currentInfo.getName() : name);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        subscriptionService = SubscriptionService.getInstance(activity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) disposables.clear();
        if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    protected View getListHeader() {
        headerRootLayout = activity.getLayoutInflater().inflate(R.layout.channel_header, itemsList, false);
        headerChannelBanner = headerRootLayout.findViewById(R.id.channel_banner_image);
        headerAvatarView = headerRootLayout.findViewById(R.id.channel_avatar_view);
        headerTitleView = headerRootLayout.findViewById(R.id.channel_title_view);
        headerSubscribersTextView = headerRootLayout.findViewById(R.id.channel_subscriber_view);
        headerSubscribeButton = headerRootLayout.findViewById(R.id.channel_subscribe_button);
        playlistCtrl = headerRootLayout.findViewById(R.id.playlist_control);


        headerPlayAllButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_all_button);
        headerPopupButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_popup_button);
        headerBackgroundButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_bg_button);

        return headerRootLayout;
    }

    @Override
    protected void showStreamDialog(final StreamInfoItem item) {
        final Activity activity = getActivity();
        final Context context = getContext();
        if (context == null || context.getResources() == null || getActivity() == null) return;

        final String[] commands = new String[]{
                context.getResources().getString(R.string.enqueue_on_background),
                context.getResources().getString(R.string.enqueue_on_popup),
                context.getResources().getString(R.string.start_here_on_main),
                context.getResources().getString(R.string.start_here_on_background),
                context.getResources().getString(R.string.start_here_on_popup),
                context.getResources().getString(R.string.append_playlist)
        };

        final DialogInterface.OnClickListener actions = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int index = Math.max(infoListAdapter.getItemsList().indexOf(item), 0);
                switch (i) {
                    case 0:
                        NavigationHelper.enqueueOnBackgroundPlayer(context, new SinglePlayQueue(item));
                        break;
                    case 1:
                        NavigationHelper.enqueueOnPopupPlayer(activity, new SinglePlayQueue(item));
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
                        if (getFragmentManager() != null) {
                            PlaylistAppendDialog.fromStreamInfoItems(Collections.singletonList(item))
                                    .show(getFragmentManager(), TAG);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        new InfoItemDialog(getActivity(), item, commands, actions).show();
    }
    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar supportActionBar = activity.getSupportActionBar();
        if(useAsFrontPage && supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        } else {
            inflater.inflate(R.menu.menu_channel, menu);

            if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu +
                    "], inflater = [" + inflater + "]");
//            menuRssButton = menu.findItem(R.id.menu_item_rss);
        }
    }

    private void openRssFeed() {
        final ChannelInfo info = currentInfo;
        if(info != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getFeedUrl()));
            startActivity(intent);
        }
    }


    /*//////////////////////////////////////////////////////////////////////////
    // Channel Subscription
    //////////////////////////////////////////////////////////////////////////*/

    private static final int BUTTON_DEBOUNCE_INTERVAL = 100;

    private void monitorSubscription(final ChannelInfo info) {
        final Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                AnimationUtils.animateView(headerSubscribeButton, false, 100);
                showSnackBarError(throwable, UserAction.SUBSCRIPTION, NewPipe.getNameOfService(currentInfo.getServiceId()), "Get subscription status", 0);
            }
        };

        final Observable<List<SubscriptionEntity>> observable = subscriptionService.subscriptionTable()
                .getSubscription(info.getServiceId(), info.getUrl())
                .toObservable();

        disposables.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getSubscribeUpdateMonitor(info), onError));

        disposables.add(observable
                // Some updates are very rapid (when calling the updateSubscription(info), for example)
                // so only update the UI for the latest emission ("sync" the subscribe button's state)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<SubscriptionEntity>>() {
                    @Override
                    public void accept(List<SubscriptionEntity> subscriptionEntities) throws Exception {
                        updateSubscribeButton(!subscriptionEntities.isEmpty());
                    }
                }, onError));

    }

    private Function<Object, Object> mapOnSubscribe(final SubscriptionEntity subscription) {
        return new Function<Object, Object>() {
            @Override
            public Object apply(@NonNull Object o) throws Exception {
                subscriptionService.subscriptionTable().insert(subscription);
                return o;
            }
        };
    }

    private Function<Object, Object> mapOnUnsubscribe(final SubscriptionEntity subscription) {
        return new Function<Object, Object>() {
            @Override
            public Object apply(@NonNull Object o) throws Exception {
                subscriptionService.subscriptionTable().delete(subscription);
                return o;
            }
        };
    }

    private void updateSubscription(final ChannelInfo info) {
        if (DEBUG) Log.d(TAG, "updateSubscription() called with: info = [" + info + "]");
        final Action onComplete = new Action() {
            @Override
            public void run() throws Exception {
                if (DEBUG) Log.d(TAG, "Updated subscription: " + info.getUrl());
            }
        };

        final Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                onUnrecoverableError(throwable, UserAction.SUBSCRIPTION, NewPipe.getNameOfService(info.getServiceId()), "Updating Subscription for " + info.getUrl(), R.string.subscription_update_failed);
            }
        };

        disposables.add(subscriptionService.updateChannelInfo(info)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onComplete, onError));
    }

    private Disposable monitorSubscribeButton(final Button subscribeButton, final Function<Object, Object> action) {
        final Consumer<Object> onNext = new Consumer<Object>() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                if (DEBUG) Log.d(TAG, "Changed subscription status to this channel!");
            }
        };

        final Consumer<Throwable> onError = new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                onUnrecoverableError(throwable, UserAction.SUBSCRIPTION, NewPipe.getNameOfService(currentInfo.getServiceId()), "Subscription Change", R.string.subscription_change_failed);
            }
        };

        /* Emit clicks from main thread unto io thread */
        return RxView.clicks(subscribeButton)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .debounce(BUTTON_DEBOUNCE_INTERVAL, TimeUnit.MILLISECONDS) // Ignore rapid clicks
                .map(action)
                .subscribe(onNext, onError);
    }

    private Consumer<List<SubscriptionEntity>> getSubscribeUpdateMonitor(final ChannelInfo info) {
        return new Consumer<List<SubscriptionEntity>>() {
            @Override
            public void accept(List<SubscriptionEntity> subscriptionEntities) throws Exception {
                if (DEBUG)
                    Log.d(TAG, "subscriptionService.subscriptionTable.doOnNext() called with: subscriptionEntities = [" + subscriptionEntities + "]");
                if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();

                if (subscriptionEntities.isEmpty()) {
                    if (DEBUG) Log.d(TAG, "No subscription to this channel!");
                    SubscriptionEntity channel = new SubscriptionEntity();
                    channel.setServiceId(info.getServiceId());
                    channel.setUrl(info.getUrl());
                    channel.setData(info.getName(), info.getAvatarUrl(), info.getDescription(), info.getSubscriberCount());
                    subscribeButtonMonitor = monitorSubscribeButton(headerSubscribeButton, mapOnSubscribe(channel));
                } else {
                    if (DEBUG) Log.d(TAG, "Found subscription to this channel!");
                    final SubscriptionEntity subscription = subscriptionEntities.get(0);
                    subscribeButtonMonitor = monitorSubscribeButton(headerSubscribeButton, mapOnUnsubscribe(subscription));
                }
            }
        };
    }

    private void updateSubscribeButton(boolean isSubscribed) {
        if (DEBUG) Log.d(TAG, "updateSubscribeButton() called with: isSubscribed = [" + isSubscribed + "]");

        boolean isButtonVisible = headerSubscribeButton.getVisibility() == View.VISIBLE;
        int backgroundDuration = isButtonVisible ? 300 : 0;
        int textDuration = isButtonVisible ? 200 : 0;

        int subscribeBackground = ContextCompat.getColor(activity, R.color.subscribe_background_color);
        int subscribeText = ContextCompat.getColor(activity, R.color.subscribe_text_color);
        int subscribedBackground = ContextCompat.getColor(activity, R.color.subscribed_background_color);
        int subscribedText = ContextCompat.getColor(activity, R.color.subscribed_text_color);

        if (!isSubscribed) {
            headerSubscribeButton.setText(R.string.subscribe_button_title);
            AnimationUtils.animateBackgroundColor(headerSubscribeButton, backgroundDuration, subscribedBackground, subscribeBackground);
            AnimationUtils.animateTextColor(headerSubscribeButton, textDuration, subscribedText, subscribeText);
        } else {
            headerSubscribeButton.setText(R.string.subscribed_button_title);
            AnimationUtils.animateBackgroundColor(headerSubscribeButton, backgroundDuration, subscribeBackground, subscribedBackground);
            AnimationUtils.animateTextColor(headerSubscribeButton, textDuration, subscribeText, subscribedText);
        }

        AnimationUtils.animateView(headerSubscribeButton, AnimationUtils.Type.LIGHT_SCALE_AND_ALPHA, true, 100);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
        return ExtractorHelper.getMoreChannelItems(serviceId, url, currentNextPageUrl);
    }

    @Override
    protected Single<ChannelInfo> loadResult(boolean forceLoad) {
        return ExtractorHelper.getChannelInfo(serviceId, url, forceLoad);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void showLoading() {
        super.showLoading();

        BaseFragment.imageLoader.cancelDisplayTask(headerChannelBanner);
        BaseFragment.imageLoader.cancelDisplayTask(headerAvatarView);
        AnimationUtils.animateView(headerSubscribeButton, false, 100);
    }

    @Override
    public void handleResult(@NonNull ChannelInfo result) {
        super.handleResult(result);

        headerRootLayout.setVisibility(View.VISIBLE);
        BaseFragment.imageLoader.displayImage(result.getBannerUrl(), headerChannelBanner,
        		ImageDisplayConstants.DISPLAY_BANNER_OPTIONS);
        BaseFragment.imageLoader.displayImage(result.getAvatarUrl(), headerAvatarView,
        		ImageDisplayConstants.DISPLAY_AVATAR_OPTIONS);

        if (result.getSubscriberCount() != -1) {
            headerSubscribersTextView.setText(Localization.localizeSubscribersCount(activity, result.getSubscriberCount()));
            headerSubscribersTextView.setVisibility(View.VISIBLE);
        } else headerSubscribersTextView.setVisibility(View.GONE);

//        if (menuRssButton != null) menuRssButton.setVisible(!TextUtils.isEmpty(result.getFeedUrl()));

        playlistCtrl.setVisibility(View.VISIBLE);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
        }

        if (disposables != null) disposables.clear();
        if (subscribeButtonMonitor != null) subscribeButtonMonitor.dispose();
        updateSubscription(result);
        monitorSubscription(result);

        headerPlayAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
                    TedBackPressDialog.startAdmobDialog(activity, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                        @Override
                        public void onReviewClick() {
                            String packageName = "";
                            try {
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                                packageName = getActivity().getPackageName();
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
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                                packageName = getActivity().getPackageName();
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
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                                packageName = getActivity().getPackageName();
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

    private PlayQueue getPlayQueue() {
        return getPlayQueue(0);
    }

    private PlayQueue getPlayQueue(final int index) {
        final List<StreamInfoItem> streamItems = new ArrayList<>();
        for(InfoItem i : infoListAdapter.getItemsList()) {
            if(i instanceof StreamInfoItem) {
                streamItems.add((StreamInfoItem) i);
            }
        }
        return new ChannelPlayQueue(
                currentInfo.getServiceId(),
                currentInfo.getUrl(),
                currentInfo.getNextPageUrl(),
                streamItems,
                index
        );
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

    @Override
    public void handleNextItems(ListExtractor.InfoItemsPage result) {
        super.handleNextItems(result);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(), UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(serviceId),
                    "Get next page of: " + url, R.string.general_error);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // OnError
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;

        int errorId = exception instanceof ExtractionException ? R.string.parsing_error : R.string.general_error;
        onUnrecoverableError(exception, UserAction.REQUESTED_CHANNEL, NewPipe.getNameOfService(serviceId), url, errorId);
        return true;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        headerTitleView.setText(title);
    }
}
