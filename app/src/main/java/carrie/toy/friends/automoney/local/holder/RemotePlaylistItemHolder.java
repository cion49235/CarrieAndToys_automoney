package carrie.toy.friends.automoney.local.holder;

import android.view.ViewGroup;

import java.text.DateFormat;

import carrie.toy.friends.automoney.database.LocalItem;
import carrie.toy.friends.automoney.database.playlist.model.PlaylistRemoteEntity;
import carrie.toy.friends.automoney.local.LocalItemBuilder;
import carrie.toy.friends.automoney.util.ImageDisplayConstants;
import carrie.toy.friends.automoney.util.Localization;

public class RemotePlaylistItemHolder extends PlaylistItemHolder {
    public RemotePlaylistItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
        super(infoItemBuilder, parent);
    }

    @Override
    public void updateFromItem(final LocalItem localItem, final DateFormat dateFormat) {
        if (!(localItem instanceof PlaylistRemoteEntity)) return;
        final PlaylistRemoteEntity item = (PlaylistRemoteEntity) localItem;

        itemTitleView.setText(item.getName());
        itemStreamCountView.setText(String.valueOf(item.getStreamCount()));
        itemUploaderView.setText(Localization.concatenateStrings(item.getUploader(),""));

        itemBuilder.displayImage(item.getThumbnailUrl(), itemThumbnailView,
                ImageDisplayConstants.DISPLAY_PLAYLIST_OPTIONS);

        super.updateFromItem(localItem, dateFormat);
    }
}
