package carrie.toy.friends.automoney.local.holder;

import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;

import carrie.toy.friends.automoney.database.LocalItem;
import carrie.toy.friends.automoney.database.playlist.PlaylistMetadataEntry;
import carrie.toy.friends.automoney.local.LocalItemBuilder;
import carrie.toy.friends.automoney.util.ImageDisplayConstants;

public class LocalPlaylistItemHolder extends PlaylistItemHolder {

    public LocalPlaylistItemHolder(LocalItemBuilder infoItemBuilder, ViewGroup parent) {
        super(infoItemBuilder, parent);
    }

    @Override
    public void updateFromItem(final LocalItem localItem, final DateFormat dateFormat) {
        if (!(localItem instanceof PlaylistMetadataEntry)) return;
        final PlaylistMetadataEntry item = (PlaylistMetadataEntry) localItem;

        itemTitleView.setText(item.name);
        itemStreamCountView.setText(String.valueOf(item.streamCount));
        itemUploaderView.setVisibility(View.INVISIBLE);

        itemBuilder.displayImage(item.thumbnailUrl, itemThumbnailView,
                ImageDisplayConstants.DISPLAY_PLAYLIST_OPTIONS);

        super.updateFromItem(localItem, dateFormat);
    }
}
