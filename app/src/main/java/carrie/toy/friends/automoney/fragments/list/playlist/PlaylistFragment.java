package carrie.toy.friends.automoney.fragments.list.playlist;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.admixer.AdInfo;
import com.admixer.InterstitialAd;
import com.admixer.InterstitialAdListener;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import carrie.toy.friends.automoney.App;
import carrie.toy.friends.automoney.BaseFragment;
import carrie.toy.friends.automoney.NewPipeDatabase;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistRemoteEntity;
import carrie.toy.friends.automoney.fragments.list.BaseListInfoFragment;
import carrie.toy.friends.automoney.info_list.InfoItemDialog;
import carrie.toy.friends.automoney.local.playlist.RemotePlaylistManager;
import carrie.toy.friends.automoney.player.playqueue.PlayQueue;
import carrie.toy.friends.automoney.player.playqueue.PlaylistPlayQueue;
import carrie.toy.friends.automoney.player.playqueue.SinglePlayQueue;
import carrie.toy.friends.automoney.report.UserAction;
import carrie.toy.friends.automoney.util.AnimationUtils;
import carrie.toy.friends.automoney.util.ExtractorHelper;
import carrie.toy.friends.automoney.util.ImageDisplayConstants;
import carrie.toy.friends.automoney.util.NavigationHelper;
import carrie.toy.friends.automoney.util.PermissionHelper;
import carrie.toy.friends.automoney.util.PreferenceUtil;
import carrie.toy.friends.automoney.util.ThemeHelper;
import gun0912.tedadhelper.backpress.OnBackPressListener;
import gun0912.tedadhelper.backpress.TedBackPressDialog;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import carrie.toy.friends.automoney.R;


public class PlaylistFragment extends BaseListInfoFragment<PlaylistInfo> implements InterstitialAdListener {

    private CompositeDisposable disposables;
    private Subscription bookmarkReactor;
    private AtomicBoolean isBookmarkButtonReady;

    private RemotePlaylistManager remotePlaylistManager;
    private PlaylistRemoteEntity playlistEntity;
    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private View headerRootLayout;
    private TextView headerTitleView;
    private View headerUploaderLayout;
    private TextView headerUploaderName;
    private ImageView headerUploaderAvatar;
    private TextView headerStreamCount;
    private View playlistCtrl;

    private View headerPlayAllButton;
    private View headerPopupButton;
    private View headerBackgroundButton;

    private MenuItem playlistBookmarkButton;

    public static PlaylistFragment getInstance(int serviceId, String url, String name) {
        PlaylistFragment instance = new PlaylistFragment();
        instance.setInitialData(serviceId, url, name);
        return instance;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        isBookmarkButtonReady = new AtomicBoolean(false);
        remotePlaylistManager = new RemotePlaylistManager(NewPipeDatabase.getInstance(getContext()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    protected View getListHeader() {
        headerRootLayout = activity.getLayoutInflater().inflate(R.layout.playlist_header, itemsList, false);
        headerTitleView = headerRootLayout.findViewById(R.id.playlist_title_view);
        headerUploaderLayout = headerRootLayout.findViewById(R.id.uploader_layout);
        headerUploaderName = headerRootLayout.findViewById(R.id.uploader_name);
        headerUploaderAvatar = headerRootLayout.findViewById(R.id.uploader_avatar_view);
        headerStreamCount = headerRootLayout.findViewById(R.id.playlist_stream_count);
        playlistCtrl = headerRootLayout.findViewById(R.id.playlist_control);

        headerPlayAllButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_all_button);
        headerPopupButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_popup_button);
        headerBackgroundButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_bg_button);


        return headerRootLayout;
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        infoListAdapter.useMiniItemVariants(true);
    }

    @Override
    protected void showStreamDialog(final StreamInfoItem item) {
        final Context context = getContext();
        final Activity activity = getActivity();
        if (context == null || context.getResources() == null || getActivity() == null) return;

        final String[] commands = new String[]{
                context.getResources().getString(R.string.enqueue_on_background),
                context.getResources().getString(R.string.enqueue_on_popup),
                context.getResources().getString(R.string.start_here_on_main),
                context.getResources().getString(R.string.start_here_on_background),
                context.getResources().getString(R.string.start_here_on_popup),
        };

        final DialogInterface.OnClickListener actions = (dialogInterface, i) -> {
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
                default:
                    break;
            }
        };

        new InfoItemDialog(getActivity(), item, commands, actions).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu +
                "], inflater = [" + inflater + "]");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist, menu);

        playlistBookmarkButton = menu.findItem(R.id.menu_item_bookmark);
        updateBookmarkButtons();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBookmarkButtonReady != null) isBookmarkButtonReady.set(false);

        if (disposables != null) disposables.clear();
        if (bookmarkReactor != null) bookmarkReactor.cancel();

        bookmarkReactor = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (disposables != null) disposables.dispose();

        disposables = null;
        remotePlaylistManager = null;
        playlistEntity = null;
        isBookmarkButtonReady = null;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
        return ExtractorHelper.getMorePlaylistItems(serviceId, url, currentNextPageUrl);
    }

    @Override
    protected Single<PlaylistInfo> loadResult(boolean forceLoad) {
        return ExtractorHelper.getPlaylistInfo(serviceId, url, forceLoad);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.menu_item_openInBrowser:
                openUrlInBrowser(url);
                break;*/
            /*case R.id.menu_item_share:
                shareUrl(name, url);
                break;*/
            case R.id.menu_item_bookmark:
                onBookmarkClicked();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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


    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void showLoading() {
        super.showLoading();
        AnimationUtils.animateView(headerRootLayout, false, 200);
        AnimationUtils.animateView(itemsList, false, 100);

        BaseFragment.imageLoader.cancelDisplayTask(headerUploaderAvatar);
        AnimationUtils.animateView(headerUploaderLayout, false, 200);
    }

    @Override
    public void handleResult(@NonNull final PlaylistInfo result) {
        super.handleResult(result);

        AnimationUtils.animateView(headerRootLayout, true, 100);
        AnimationUtils.animateView(headerUploaderLayout, true, 300);
        headerUploaderLayout.setOnClickListener(null);
        if (!TextUtils.isEmpty(result.getUploaderName())) {
            headerUploaderName.setText(result.getUploaderName());
            if (!TextUtils.isEmpty(result.getUploaderUrl())) {
                headerUploaderLayout.setOnClickListener(v ->
                        NavigationHelper.openChannelFragment(getFragmentManager(),
                                result.getServiceId(), result.getUploaderUrl(),
                                result.getUploaderName())
                );
            }
        }

        playlistCtrl.setVisibility(View.VISIBLE);

        BaseFragment.imageLoader.displayImage(result.getUploaderAvatarUrl(), headerUploaderAvatar,
                ImageDisplayConstants.DISPLAY_AVATAR_OPTIONS);
        headerStreamCount.setText(getResources().getQuantityString(R.plurals.videos,
                (int) result.getStreamCount(), (int) result.getStreamCount()));

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(), UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(result.getServiceId()), result.getUrl(), 0);
        }

        remotePlaylistManager.getPlaylist(result)
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getPlaylistBookmarkSubscriber());

        remotePlaylistManager.onUpdate(result)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {/* Do nothing*/}, this::onError);

        headerPlayAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferenceUtil.getStringSharedData(getActivity(), PreferenceUtil.PREF_ISSUBSCRIBED, App.isSubscribed).equals("true")){
                    TedBackPressDialog.startAdmobDialog(activity, getString(R.string.app_name), getString(R.string.admob_banner_key), new OnBackPressListener() {
                        @Override
                        public void onReviewClick() {
                            String packageName = "";
                            try {
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo( getActivity().getPackageName(), 0);
                                packageName =  getActivity().getPackageName();
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
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo( getActivity().getPackageName(), 0);
                                packageName =  getActivity().getPackageName();
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
                                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo( getActivity().getPackageName(), 0);
                                packageName =  getActivity().getPackageName();
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

    private PlayQueue getPlayQueue() {
        return getPlayQueue(0);
    }

    private PlayQueue getPlayQueue(final int index) {
        final List<StreamInfoItem> infoItems = new ArrayList<>();
        for(InfoItem i : infoListAdapter.getItemsList()) {
            if(i instanceof StreamInfoItem) {
                infoItems.add((StreamInfoItem) i);
            }
        }
        return new PlaylistPlayQueue(
                currentInfo.getServiceId(),
                currentInfo.getUrl(),
                currentInfo.getNextPageUrl(),
                infoItems,
                index
        );
    }

    @Override
    public void handleNextItems(ListExtractor.InfoItemsPage result) {
        super.handleNextItems(result);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(), UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId)
                    , "Get next page of: " + url, 0);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // OnError
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;

        int errorId = exception instanceof ExtractionException ? R.string.parsing_error : R.string.general_error;
        onUnrecoverableError(exception, UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), url, errorId);
        return true;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private Subscriber<List<PlaylistRemoteEntity>> getPlaylistBookmarkSubscriber() {
        return new Subscriber<List<PlaylistRemoteEntity>>() {
            @Override
            public void onSubscribe(Subscription s) {
                if (bookmarkReactor != null) bookmarkReactor.cancel();
                bookmarkReactor = s;
                bookmarkReactor.request(1);
            }

            @Override
            public void onNext(List<PlaylistRemoteEntity> playlist) {
                playlistEntity = playlist.isEmpty() ? null : playlist.get(0);

                updateBookmarkButtons();
                isBookmarkButtonReady.set(true);

                if (bookmarkReactor != null) bookmarkReactor.request(1);
            }

            @Override
            public void onError(Throwable t) {
                PlaylistFragment.this.onError(t);
            }

            @Override
            public void onComplete() {

            }
        };
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        headerTitleView.setText(title);
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

    private void onBookmarkClicked() {
        if (isBookmarkButtonReady == null || !isBookmarkButtonReady.get() ||
                remotePlaylistManager == null)
            return;

        final Disposable action;

        if (currentInfo != null && playlistEntity == null) {
            action = remotePlaylistManager.onBookmark(currentInfo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ignored -> {/* Do nothing */}, this::onError);
        } else if (playlistEntity != null) {
            action = remotePlaylistManager.deletePlaylist(playlistEntity.getUid())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> playlistEntity = null)
                    .subscribe(ignored -> {/* Do nothing */}, this::onError);
        } else {
            action = Disposables.empty();
        }

        disposables.add(action);
    }

    private void updateBookmarkButtons() {
        if (playlistBookmarkButton == null || activity == null) return;

        final int iconAttr = playlistEntity == null ?
                R.attr.ic_playlist_add : R.attr.ic_playlist_check;

        final int titleRes = playlistEntity == null ?
                R.string.bookmark_playlist : R.string.unbookmark_playlist;

        playlistBookmarkButton.setIcon(ThemeHelper.resolveResourceIdFromAttr(activity, iconAttr));
        playlistBookmarkButton.setTitle(titleRes);
    }
}
