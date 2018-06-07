package carrie.toy.friends.automoney.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import carrie.toy.friends.automoney.database.history.dao.SearchHistoryDAO;
import carrie.toy.friends.automoney.database.history.dao.StreamHistoryDAO;
import carrie.toy.friends.automoney.database.history.model.SearchHistoryEntry;
import carrie.toy.friends.automoney.database.history.model.StreamHistoryEntity;
import carrie.toy.friends.automoney.database.playlist.dao.PlaylistDAO;
import carrie.toy.friends.automoney.database.playlist.dao.PlaylistRemoteDAO;
import carrie.toy.friends.automoney.database.playlist.dao.PlaylistStreamDAO;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistEntity;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistRemoteEntity;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistStreamEntity;
import carrie.toy.friends.automoney.database.stream.dao.StreamDAO;
import carrie.toy.friends.automoney.database.stream.dao.StreamStateDAO;
import carrie.toy.friends.automoney.database.stream.model.StreamStateEntity;
import carrie.toy.friends.automoney.database.subscription.SubscriptionDAO;
import carrie.toy.friends.automoney.database.subscription.SubscriptionEntity;

import static carrie.toy.friends.automoney.database.Migrations.DB_VER_12_0;

@TypeConverters({Converters.class})
@Database(
        entities = {
                SubscriptionEntity.class, SearchHistoryEntry.class,
                carrie.toy.friends.automoney.database.stream.model.StreamEntity.class, StreamHistoryEntity.class, StreamStateEntity.class,
                PlaylistEntity.class, PlaylistStreamEntity.class, PlaylistRemoteEntity.class
        },
        version = DB_VER_12_0,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "newpipe.db";

    public abstract SubscriptionDAO subscriptionDAO();

    public abstract SearchHistoryDAO searchHistoryDAO();

    public abstract StreamDAO streamDAO();

    public abstract StreamHistoryDAO streamHistoryDAO();

    public abstract StreamStateDAO streamStateDAO();

    public abstract PlaylistDAO playlistDAO();

    public abstract PlaylistStreamDAO playlistStreamDAO();

    public abstract PlaylistRemoteDAO playlistRemoteDAO();
}
