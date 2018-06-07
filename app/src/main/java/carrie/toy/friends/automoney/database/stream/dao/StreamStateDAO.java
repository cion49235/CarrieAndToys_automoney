package carrie.toy.friends.automoney.database.stream.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import carrie.toy.friends.automoney.database.BasicDAO;
import carrie.toy.friends.automoney.database.stream.model.StreamStateEntity;
import io.reactivex.Flowable;

import static carrie.toy.friends.automoney.database.stream.model.StreamStateEntity.JOIN_STREAM_ID;
import static carrie.toy.friends.automoney.database.stream.model.StreamStateEntity.STREAM_STATE_TABLE;

@Dao
public abstract class StreamStateDAO implements BasicDAO<StreamStateEntity> {
    @Override
    @Query("SELECT * FROM " + STREAM_STATE_TABLE)
    public abstract Flowable<List<StreamStateEntity>> getAll();

    @Override
    @Query("DELETE FROM " + STREAM_STATE_TABLE)
    public abstract int deleteAll();

    @Override
    public Flowable<List<StreamStateEntity>> listByService(int serviceId) {
        throw new UnsupportedOperationException();
    }

    @Query("SELECT * FROM " + STREAM_STATE_TABLE + " WHERE " + JOIN_STREAM_ID + " = :streamId")
    public abstract Flowable<List<StreamStateEntity>> getState(final long streamId);

    @Query("DELETE FROM " + STREAM_STATE_TABLE + " WHERE " + JOIN_STREAM_ID + " = :streamId")
    public abstract int deleteState(final long streamId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract void silentInsertInternal(final StreamStateEntity streamState);

    @Transaction
    public long upsert(StreamStateEntity stream) {
        silentInsertInternal(stream);
        return update(stream);
    }
}
