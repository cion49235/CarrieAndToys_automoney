package carrie.toy.friends.automoney.database.playlist.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import carrie.toy.friends.automoney.database.BasicDAO;
import carrie.toy.friends.automoney.database.playlist.PlaylistMetadataEntry;
import carrie.toy.friends.automoney.database.playlist.PlaylistStreamEntry;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity;
import carrie.toy.friends.automoney.database.stream.model.StreamEntity;
import io.reactivex.Flowable;

import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_ID;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_NAME;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_TABLE;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity.PLAYLIST_THUMBNAIL_URL;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity.JOIN_INDEX;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity.JOIN_PLAYLIST_ID;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity.JOIN_STREAM_ID;
import static carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity.PLAYLIST_STREAM_JOIN_TABLE;


@Dao
public abstract class PlaylistStreamDAO implements BasicDAO<PlaylistStreamEntity> {
    @Override
    @Query("SELECT * FROM " + PLAYLIST_STREAM_JOIN_TABLE)
    public abstract Flowable<List<PlaylistStreamEntity>> getAll();

    @Override
    @Query("DELETE FROM " + PLAYLIST_STREAM_JOIN_TABLE)
    public abstract int deleteAll();

    @Override
    public Flowable<List<PlaylistStreamEntity>> listByService(int serviceId) {
        throw new UnsupportedOperationException();
    }

    @Query("DELETE FROM " + PLAYLIST_STREAM_JOIN_TABLE +
            " WHERE " + JOIN_PLAYLIST_ID + " = :playlistId")
    public abstract void deleteBatch(final long playlistId);

    @Query("SELECT COALESCE(MAX(" + JOIN_INDEX + "), -1)" +
            " FROM " + PLAYLIST_STREAM_JOIN_TABLE +
            " WHERE " + JOIN_PLAYLIST_ID + " = :playlistId")
    public abstract Flowable<Integer> getMaximumIndexOf(final long playlistId);

    @Transaction
    @Query("SELECT * FROM " + StreamEntity.STREAM_TABLE + " INNER JOIN " +
            // get ids of streams of the given playlist
            "(SELECT " + JOIN_STREAM_ID + "," + JOIN_INDEX +
            " FROM " + PLAYLIST_STREAM_JOIN_TABLE +
            " WHERE " + JOIN_PLAYLIST_ID + " = :playlistId)" +

            // then merge with the stream metadata
            " ON " + StreamEntity.STREAM_ID + " = " + JOIN_STREAM_ID +
            " ORDER BY " + JOIN_INDEX + " ASC")
    public abstract Flowable<List<PlaylistStreamEntry>> getOrderedStreamsOf(long playlistId);

    @Transaction
    @Query("SELECT " + PLAYLIST_ID + ", " + PLAYLIST_NAME + ", " +
            PLAYLIST_THUMBNAIL_URL + ", " +
            "COALESCE(COUNT(" + JOIN_PLAYLIST_ID + "), 0) AS " + PlaylistMetadataEntry.PLAYLIST_STREAM_COUNT +

            " FROM " + PLAYLIST_TABLE +
            " LEFT JOIN " + PLAYLIST_STREAM_JOIN_TABLE +
            " ON " + PLAYLIST_ID + " = " + JOIN_PLAYLIST_ID +
            " GROUP BY " + JOIN_PLAYLIST_ID +
            " ORDER BY " + PLAYLIST_NAME + " COLLATE NOCASE ASC")
    public abstract Flowable<List<PlaylistMetadataEntry>> getPlaylistMetadata();
}
