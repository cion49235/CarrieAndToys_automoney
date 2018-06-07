package carrie.toy.friends.automoney.database.playlist.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import carrie.toy.friends.automoney.database.BasicDAO;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity;
import io.reactivex.Flowable;

import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_ID;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_TABLE;


@Dao
public abstract class PlaylistDAO implements BasicDAO<PlaylistEntity> {
    @Override
    @Query("SELECT * FROM " + PLAYLIST_TABLE)
    public abstract Flowable<List<PlaylistEntity>> getAll();

    @Override
    @Query("DELETE FROM " + PLAYLIST_TABLE)
    public abstract int deleteAll();

    @Override
    public Flowable<List<PlaylistEntity>> listByService(int serviceId) {
        throw new UnsupportedOperationException();
    }

    @Query("SELECT * FROM " + PLAYLIST_TABLE + " WHERE " + PLAYLIST_ID + " = :playlistId")
    public abstract Flowable<List<PlaylistEntity>> getPlaylist(final long playlistId);

    @Query("DELETE FROM " + PLAYLIST_TABLE + " WHERE " + PLAYLIST_ID + " = :playlistId")
    public abstract int deletePlaylist(final long playlistId);
}
