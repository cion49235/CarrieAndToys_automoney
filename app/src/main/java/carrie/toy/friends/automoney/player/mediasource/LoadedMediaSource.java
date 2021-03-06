package carrie.toy.friends.automoney.player.mediasource;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.Allocator;

import java.io.IOException;

import carrie.toy.friends.automoney.player.playqueue.PlayQueueItem;

public class LoadedMediaSource implements ManagedMediaSource {

    private final MediaSource source;
    private final PlayQueueItem stream;
    private final long expireTimestamp;

    public LoadedMediaSource(@NonNull final MediaSource source,
                             @NonNull final PlayQueueItem stream,
                             final long expireTimestamp) {
        this.source = source;
        this.stream = stream;
        this.expireTimestamp = expireTimestamp;
    }

    public PlayQueueItem getStream() {
        return stream;
    }

    private boolean isExpired() {
        return System.currentTimeMillis() >= expireTimestamp;
    }

    @Override
    public void prepareSource(ExoPlayer player, boolean isTopLevelSource, Listener listener) {
        source.prepareSource(player, isTopLevelSource, listener);
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() throws IOException {
        source.maybeThrowSourceInfoRefreshError();
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator) {
        return source.createPeriod(id, allocator);
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        source.releasePeriod(mediaPeriod);
    }

    @Override
    public void releaseSource() {
        source.releaseSource();
    }

    @Override
    public boolean shouldBeReplacedWith(@NonNull PlayQueueItem newIdentity,
                                        final boolean isInterruptable) {
        return newIdentity != stream || (isInterruptable && isExpired());
    }

    @Override
    public boolean isStreamEqual(@NonNull PlayQueueItem stream) {
        return this.stream == stream;
    }
}
