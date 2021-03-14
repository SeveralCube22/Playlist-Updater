/**
 * Playlist Updater
 * @author Viswadeep Manam
 */

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.json.*;

public class ApplicationRunner extends JComponent
{
	private static String id;
	private static final int resultsPerPage = 5;
	private static int height;
	private static JFrame frame;
	
	public static void main(String[] args) throws GeneralSecurityException, IOException, GoogleJsonResponseException 
	{
	        final YouTube youtubeService = Service.getService();
	        
	        YouTube.Playlists.List playListRequest = youtubeService.playlists().list("snippet");
	       
	        final ArrayList<PlaylistChannel> channels = new ArrayList<>();
	        
	        //setup data
	        createPlaylistChannels(playListRequest, channels);
	        
	        //create necessary GUI elements
	        frame = new JFrame();
	        frame.setSize(1280, 720);
	        frame.setResizable(true);
	        frame.setMinimumSize(new Dimension(1280, 720));
	        frame.setTitle("Playlist Updater");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	        
	        //setup GUI for channels
	        String[] c = {"Please Select a Playlist to Modify"};
	        DefaultTableModel model = new DefaultTableModel((Object[]) c, channels.size());
	        model.setColumnCount(1);
	              
	        JTable table = new JTable(model);
	        table.setRowHeight(height);
	        table.getColumnModel().getColumn(0).setCellRenderer(channels.get(0).getChannelRenderer(channels));
	        table.setBounds(0, 0, frame.getWidth(), height);
	        table.getTableHeader().setFont(new Font("Roboto", Font.PLAIN, 25));
	        final JScrollPane scrollPane = new JScrollPane(table);
	        scrollPane.setWheelScrollingEnabled(true);
	        frame.add(scrollPane);
	        scrollPane.setOpaque(false);
	        frame.setVisible(true);
	        table.addMouseListener(new MouseAdapter()
	        {
	        	public void mouseClicked(MouseEvent e)
	        	{
	        		JTable target = (JTable) e.getSource();
	        		int row = target.getSelectedRow();
	        		id = channels.get(row).getChannelId();
	        		frame.remove(scrollPane);
					try 
					{
						channelSelected(youtubeService);
					} 
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
	        	}
	        });
	 }
	
	private static void channelSelected(final YouTube youtubeService) throws IOException
	{	
		YouTube.PlaylistItems.List playListItemsRequest = youtubeService.playlistItems().list("snippet");
		playListItemsRequest.setPlaylistId(id).execute();
        
        final ArrayList<Item> playlistItems = new ArrayList<>();
        createPlaylistItems(playListItemsRequest, playlistItems);
        
		Item.playlistId = id;
        
        DefaultListModel<Item> model = new DefaultListModel<>();
        model.setSize(playlistItems.size());

        final JList<Item> list = new JList<>(model);
        
        list.setSelectionBackground(Color.GREEN);
        list.setBackground(Color.WHITE);
        list.setCellRenderer(playlistItems.get(0).getItemRenderer(playlistItems));
         
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.setWheelScrollingEnabled(true);
       
        
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
        panel.setLayout(layout);
        panel.add(scrollPane);
        
        JPanel guiParent = new JPanel();
        BoxLayout guiLayout = new BoxLayout(guiParent, BoxLayout.X_AXIS);
        
        JPanel userInterface = new JPanel();
        BoxLayout userInterfaceLayout = new BoxLayout(userInterface, BoxLayout.Y_AXIS);
        
        final DefaultListModel<String> tagModel = new DefaultListModel<>();
        final JTextField input = new JTextField(20);
     
        final JList<String> tags = new JList<>(tagModel);
        TagMouseAdapter<String> listener = new TagMouseAdapter<>(tagModel, tags);
        tags.addMouseListener(listener);
        tags.addMouseMotionListener(listener);
        JScrollPane tagsPane = new JScrollPane(tags);
        tagsPane.setMinimumSize((new Dimension(frame.getWidth() / 3, frame.getHeight())));
        tagsPane.setPreferredSize(new Dimension(frame.getWidth() / 3, frame.getHeight()));
        tagsPane.setMaximumSize((new Dimension(frame.getWidth() / 3, frame.getHeight())));
        tagsPane.setOpaque(false);
        tagsPane.setWheelScrollingEnabled(true);
        
        input.addKeyListener(new KeyAdapter()
        {
			@Override
			public void keyReleased(KeyEvent e) 
			{
				if(e.getKeyCode() == e.VK_ENTER)
				{
					if(!input.getText().equals(""))
						tagModel.addElement(input.getText());
					input.setText("");
				}
			}
        });
        JPanel buttons = new JPanel();
        BoxLayout buttonLayout = new BoxLayout(buttons, BoxLayout.X_AXIS);
        
        JButton deselect = new JButton();
        deselect.setText("Deselect");
        deselect.addMouseListener(new MouseAdapter()
        {
        	public void mouseClicked(MouseEvent e)
        	{
        		list.clearSelection();
        	}
        });
        
        final HashMap<String, ArrayList<Item>> map = new HashMap<>();
        
        JButton update = new JButton();
        update.setText("Update");
        update.addMouseListener(new MouseAdapter()
        {
        	public void mouseClicked(MouseEvent e)
        	{
        		int position = 0;
        		for(int i = 0; i < tagModel.getSize(); i++)
        		{
        			String tag = tagModel.getElementAt(i);
        			ArrayList<Item> items = map.get(tag);
        			for(int j = 0; j < items.size(); j++)
        			{
        				Item item = items.get(j);
        				if(item.getCurrentTag().equals(tag))
        				{
							try 
							{
								item.updateSnippet(youtubeService, position++);
							} 
							catch (IOException e1) 
							{
								e1.printStackTrace();
							}
        				}
        			}
        		}
        	}
        });
        
        JButton add = new JButton();
        add.setText("Add");
        add.addMouseListener(new MouseAdapter()
        {
        	public void mouseClicked(MouseEvent e)
        	{
        		String tag = tagModel.get(tags.getSelectedIndex());
        		int[] selectedItems = list.getSelectedIndices();
        		list.clearSelection();
        		tags.clearSelection();
        		ArrayList<Item> items = new ArrayList<>();
        		for(int i : selectedItems)
        		{
        			Item item = playlistItems.get(i);
        			item.setCurrentTag(tag);
        			items.add(item);
        		}
        		map.put(tag, items);
        	}
        });
        
        buttons.add(deselect);
        buttons.add(update);
        buttons.add(add);
        
        userInterface.add(input);
        userInterface.add(buttons);
        
        
        guiParent.add(userInterface);
        guiParent.add(tagsPane);
        
        panel.add(guiParent);
        
        scrollPane.setMinimumSize(new Dimension(768, 720));
        guiParent.setMinimumSize(new Dimension(512, 720));
        
        scrollPane.setPreferredSize(new Dimension(1152, 1080));
        guiParent.setPreferredSize(new Dimension(768, 1080));
        
        panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        frame.add(panel);
        frame.repaint();
        frame.setVisible(true);
	}
	
    public static void createPlaylistChannels(YouTube.Playlists.List playListRequest, ArrayList<PlaylistChannel> channels) throws IOException
    {
    	PlaylistListResponse channelResponse = playListRequest.setMine(true).execute();
    	
    	JSONObject channelJSON = new JSONObject(channelResponse);
    	int results = channelJSON.getJSONObject("pageInfo").getInt("totalResults");
        int processedResults = 0;
        
        while(processedResults < results)
        {
        	if(processedResults > 0)
        	{
        		String nextPageToken = channelJSON.getString("nextPageToken");
        		channelResponse = playListRequest.setMine(true).setPageToken(nextPageToken).execute();
        		channelJSON = new JSONObject(channelResponse);
        	}
        	JSONArray entries = channelJSON.getJSONArray("items");
            for(int i = 0; i < entries.length(); i++)
            {
            	JSONObject items = entries.getJSONObject(i);
            	JSONObject snippet = items.getJSONObject("snippet");
            	JSONObject thumbnails = snippet.getJSONObject("thumbnails");
            	JSONObject image = thumbnails.getJSONObject("default");
            	URL url = new URL(image.getString("url"));
            	Image img = ImageIO.read(url);
            	channels.add(new PlaylistChannel(new ImageIcon(img), snippet.getString("title"), items.getString("id")));
            	height = image.getInt("height");
            }
            processedResults += resultsPerPage;
        }
    }
    
    public static void createPlaylistItems(YouTube.PlaylistItems.List playListItemsRequest, ArrayList<Item> channels) throws IOException
    {
    	JDialog modalDialog = new JDialog(frame, "LOADING", ModalityType.MODELESS);
    	modalDialog.setSize(300, 0);
    	modalDialog.setResizable(false);
    	modalDialog.setLocationRelativeTo(frame);
    	
    	modalDialog.revalidate();
    	modalDialog.repaint();
    	modalDialog.setVisible(true);
    	
    	PlaylistItemListResponse itemsResponse = playListItemsRequest.execute();
    	
    	JSONObject itemsJSON = new JSONObject(itemsResponse);
    	int results = itemsJSON.getJSONObject("pageInfo").getInt("totalResults");
        int processedResults = 0;
        
        while(processedResults < results)
        {
        	if(processedResults > 0)
        	{
        		String nextPageToken = itemsJSON.getString("nextPageToken");
        		itemsResponse = playListItemsRequest.setPageToken(nextPageToken).execute();
        		itemsJSON = new JSONObject(itemsResponse);
        	}
        	JSONArray entries = itemsJSON.getJSONArray("items");
            for(int i = 0; i < entries.length(); i++)
            {
            	JSONObject items = entries.getJSONObject(i);
            	JSONObject snippet = items.getJSONObject("snippet");
            	JSONObject thumbnails = snippet.getJSONObject("thumbnails");
            	JSONObject image = thumbnails.getJSONObject("default");
            	JSONObject resourceId = snippet.getJSONObject("resourceId");
            	URL url = new URL(image.getString("url"));
            	Image img = ImageIO.read(url);
            	channels.add(new Item(new ImageIcon(img), snippet.getString("title"), items.getString("id"), resourceId.getString("kind"), resourceId.getString("videoId")));
            }
            processedResults += resultsPerPage;
            modalDialog.setTitle(String.format("FETCHING DATA: %.2f%%", processedResults < results ? ((double) processedResults / results) * 100 : 100));
        }
        
        modalDialog.dispose();
    }
    
    private static class TagMouseAdapter<T> extends MouseInputAdapter 
    {
    	private DefaultListModel<T> model;
    	private JList<T> list;
        private int dragIndex;
        private boolean isMouseDragging;
        
        private TagMouseAdapter(DefaultListModel<T> model, JList<T> list)
        {
        	this.model = model;
        	this.list = list;
        	isMouseDragging = false;
        }
        
        @Override
        public void mousePressed(MouseEvent e) 
        {
            dragIndex = list.getSelectedIndex();
        }

        @Override
        public void mouseReleased(MouseEvent e) 
        {
            if (isMouseDragging) 
            {        
                int target = list.getSelectedIndex();
                T element =  model.get(dragIndex);
                model.remove(dragIndex);
                model.add(target, element);
                list.clearSelection();
            }
            isMouseDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) 
        {            
            isMouseDragging = true;
        }
    }
}
