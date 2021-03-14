import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;

public class Item 
{
	private Icon img;
	private String itemTitle;
	private String itemId;
	
	private String resourceKind;
	private String videoId;
	
	private String currentTag;

	public static String playlistId;
	private static PlaylistItemRenderer itemRenderer;
	
	
	public Item(Icon img, String title, String id, String resourceKind, String videoId)
	{
		this.img = img;
		this.itemTitle = title;
		this.itemId = id;
		this.resourceKind = resourceKind;
		this.videoId = videoId;
		this.currentTag = "default";
	}
	
	public void updateSnippet(YouTube youtubeService, long position) throws IOException
	{
		PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setId(itemId);

        // Add the snippet object property to the PlaylistItem object.
        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setPlaylistId(playlistId);
        snippet.setPosition(position);
        ResourceId resourceId = new ResourceId();
        resourceId.setKind(resourceKind);
        resourceId.setVideoId(videoId);
        snippet.setResourceId(resourceId);
        playlistItem.setSnippet(snippet);

        // Define and execute the API request
        YouTube.PlaylistItems.Update request = youtubeService.playlistItems()
            .update("contentDetails, id, snippet, status", playlistItem);
        request.execute();
	}
	
	public String getTitle()
	{
		return itemTitle;
	}
	
	public PlaylistItemRenderer getItemRenderer(ArrayList<Item> playlistItems)
	{
		itemRenderer = new PlaylistItemRenderer(playlistItems);
		return itemRenderer;
	}
	
	public String getCurrentTag()
	{
		return currentTag;
	}
	
	public void setCurrentTag(String currentTag)
	{
		this.currentTag = currentTag;
	}
	
	
	private class PlaylistItemRenderer extends DefaultListCellRenderer
	{
		private JLabel label;
		private ArrayList<Item> items;
		
		private PlaylistItemRenderer(ArrayList<Item> items)
		{
			label = new JLabel();
			label.setOpaque(true);
			label.setBackground(Color.WHITE);
			this.items = items;
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean expanded)
		{
			label.setFont(new Font("Roboto", Font.PLAIN, 15));
    		label.setIconTextGap(50);
    		label.setText(items.get(index).itemTitle);
    		label.setIcon(items.get(index).img);
    		label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
    		return label;
		}
	}
}
