package carrie.toy.friends.automoney.database.history.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.List;

import carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity;
import carrie.toy.friends.automoney.database.history.model.StreamHistoryEntry;
import carrie.toy.friends.automoney.database.stream.StreamStatisticsEntry;
import carrie.toy.friends.automoney.database.stream.model.StreamEntity;
import io.reactivex.Flowable;

import static carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity.JOIN_STREAM_ID;
import static carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity.STREAM_ACCESS_DATE;
import static carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity.STREAM_HISTORY_TABLE;
import static carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity.STREAM_REPEAT_COUNT;

@Dao
public abstract class StreamHistoryDAO implements HistoryDAO<StreamHistoryEntity> {
    @Query("SELECT * FROM " + STREAM_HISTORY_TABLE +
            " WHERE " + STREAM_ACCESS_DATE + " = " +
            "(SELECT MAX(" + STREAM_ACCESS_DATE + ") FROM " + STREAM_HISTORY_TABLE + ")")
    @Override
    @Nullable
    public abstract StreamHistoryEntity getLatestEntry();

    @Override
    @Query("SELECT * FROM " + STREAM_HISTORY_TABLE)
    public abstract Flowable<List<StreamHistoryEntity>> getAll();

    @Override
    @Query("DELETE FROM " + STREAM_HISTORY_TABLE)
    public abstract int deleteAll();

    @Override
    public Flowable<List<StreamHistoryEntity>> listByService(int serviceId) {
        throw new UnsupportedOperationException();
    }

    @Query("SELECT * FROM " + StreamEntity.STREAM_TABLE +
            " INNER JOIN " + STREAM_HISTORY_TABLE +
            " ON " + StreamEntity.STREAM_ID + " = " + JOIN_STREAM_ID +
            " ORDER BY " + STREAM_ACCESS_DATE + " DESC")
    public abstract Flowable<List<StreamHistoryEntry>> getHistory();

    @Query("DELETE FROM " + STREAM_HISTORY_TABLE + " WHERE " + JOIN_STREAM_ID + " = :streamId")
    public abstract int deleteStreamHistory(final long streamId);

    @Query("SELECT * FROM " + StreamEntity.STREAM_TABLE +

            // Select the latest entry and watch count for each stream id on history table
            " INNER JOIN " +
            "(SELECT " + JOIN_STREAM_ID + ", " +
            "  MAX(" + STREAM_ACCESS_DATE + ") AS " + StreamStatisticsEntry.STREAM_LATEST_DATE + ", " +
            "  SUM(" + STREAM_REPEAT_COUNT + ") AS " + StreamStatisticsEntry.STREAM_WATCH_COUNT +
            " FROM " + STREAM_HISTORY_TABLE + " GROUP BY " + JOIN_STREAM_ID + ")" +

            " ON " + StreamEntity.STREAM_ID + " = " + JOIN_STREAM_ID)
    public abstract Flowable<List<StreamStatisticsEntry>> getStatistics();
}
