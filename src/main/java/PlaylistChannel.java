import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PlaylistChannel 
{
	private Icon img;
	private String channelTitle;
	private String channelId;
	
	private static ChannelRenderer channel;
	
	public PlaylistChannel(Icon img, String title, String id)
	{
		this.img = img;
		this.channelTitle = title;
		this.channelId = id;
	}
	
	public String getChannelTitle()
	{
		return channelTitle;
	}
	
	public String getChannelId()
	{
		return channelId;
	}
	
	public ChannelRenderer getChannelRenderer(ArrayList<PlaylistChannel> channels)
	{
		channel = new ChannelRenderer(channels);
		return channel;
	}
	
	private class ChannelRenderer extends DefaultTableCellRenderer
    {
		JLabel label;
    	ArrayList<PlaylistChannel> channels;
    	
    	private ChannelRenderer(ArrayList<PlaylistChannel> channels)
    	{
    		label = new JLabel();
    		this.channels = channels;
    	}
    	
    	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    	{
    		label.setFont(new Font("Roboto", Font.PLAIN, 15));
    		label.setIconTextGap(50);
    		label.setText(channels.get(row).channelTitle);
    		label.setIcon(channels.get(row).img);
    		return label;
    	}
    }
	
}
