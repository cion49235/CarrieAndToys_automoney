package carrie.toy.friends.automoney.local.playlist;

import org.schabi.newpipe.extractor.playlist.PlaylistInfo;

import java.util.List;

import carrie.toy.friends.automoney.database.AppDatabase;
import carrie.toy.friends.automoney.database.playlist.dao.PlaylistRemoteDAO;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistRemoteEntity;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class RemotePlaylistManager {

    private final PlaylistRemoteDAO playlistRemoteTable;

    public RemotePlaylistManager(final AppDatabase db) {
        playlistRemoteTable = db.playlistRemoteDAO();
    }

    public Flowable<List<PlaylistRemoteEntity>> getPlaylists() {
        return playlistRemoteTable.getAll().subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlaylistRemoteEntity>> getPlaylist(final PlaylistInfo info) {
        return playlistRemoteTable.getPlaylist(info.getServiceId(), info.getUrl())
                .subscribeOn(Schedulers.io());
    }

    public Single<Integer> deletePlaylist(final long playlistId) {
        return Single.fromCallable(() -> playlistRemoteTable.deletePlaylist(playlistId))
                .subscribeOn(Schedulers.io());
    }

    public Single<Long> onBookmark(final PlaylistInfo playlistInfo) {
        return Single.fromCallable(() -> {
            final PlaylistRemoteEntity playlist = new PlaylistRemoteEntity(playlistInfo);
            return playlistRemoteTable.upsert(playlist);
        }).subscribeOn(Schedulers.io());
    }

    public Single<Integer> onUpdate(final PlaylistInfo playlistInfo) {
        return Single.fromCallable(() -> playlistRemoteTable.update(new PlaylistRemoteEntity(playlistInfo)))
                .subscribeOn(Schedulers.io());
    }
}
