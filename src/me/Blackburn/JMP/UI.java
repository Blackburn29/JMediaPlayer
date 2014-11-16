package me.Blackburn.JMP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

public class UI extends JFrame{
	private static final long serialVersionUID = -4519469576556786931L;

	private static JMediaPlayer jmp;
	
	static JPanel vu, vu2, fileList, controls;
	static JTextField title;
	static JButton play,stop,prev,next;
	static JList<String> files;
	static DefaultListModel<String> lm;
	static Vector<MusicItem> music;
	static String dir = "";
	static JFileChooser chooser;
	static JSlider vol, seek;
	static JProgressBar[] eq = new JProgressBar[32];
	static JMenuBar menu;
	static JMenu fileMenu;
	static JMenuItem chooseDir;
	static JMenuItem setEQ;
	static JCheckBox random;
	
	static CountDownLatch latch = new CountDownLatch(1);
	
	static JFrame titleSlider;
	
	static boolean greet = true;
	
	public UI(JMediaPlayer instance)
	{
		setUI();
		UI.jmp = instance;
		
		ColorUIResource cyan = new ColorUIResource(Color.CYAN.darker());
		ColorUIResource gray = new ColorUIResource(Color.gray);
		
		UIManager.put("nimbusOrange",cyan);
		UIManager.put("control",gray);	
			
		
		files = new JList<String>();
    	lm = new DefaultListModel<String>();
    	music = new Vector<MusicItem>();
		chooser = new JFileChooser();
	    chooser.setDialogTitle("Pick a Directory for media");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    
		JScrollPane fpane = new JScrollPane(files, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		fpane.setBorder(BorderFactory.createTitledBorder("Library"));
	    
		
		menu = new JMenuBar();
		fileMenu = new JMenu("File");
			menu.add(fileMenu);
			
		chooseDir = new JMenuItem("Add Music Directory");	
			fileMenu.add(chooseDir);
		setEQ = new JMenuItem("Adjust Equalizer");	
			fileMenu.add(setEQ);
		
		this.setVisible(true);
		this.setTitle("JMediaPlayer");
		this.setLayout(new BorderLayout());
		vu = new JPanel(new GridLayout(1,1));
		vu2 = new JPanel(new GridLayout(1,eq.length));
		vu2.setPreferredSize(new Dimension(175,75));
		vu2.setMinimumSize(new Dimension(175,50));
		vu2.setMaximumSize(new Dimension(175,50));
		vu2.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		controls = new JPanel(new GridLayout(4,1,5,5));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fileList = new JPanel(new BorderLayout());
		title = new JTextField(20);
		play = new JButton("Play");
		stop = new JButton("Stop");
		prev = new JButton("<");
		next = new JButton(">");
		//chooseDir = new JButton("Choose Directory");
		seek = new JSlider();
		seek.setBorder(BorderFactory.createTitledBorder("00.00 / 00.00"));
		vol = new JSlider();
		vol.setBorder(BorderFactory.createTitledBorder("Volume"));
		vol.setMinimum(0);
		vol.setMaximum(1000);
		vol.setValue(875);
		jmp.adjVolume((float)vol.getValue()/1000);
		

		random = new JCheckBox();
		JPanel randomPanel = new JPanel(new FlowLayout());
		randomPanel.add(random);
		randomPanel.setBorder(BorderFactory.createTitledBorder("Shuffle"));
		
		JPanel buttons = new JPanel(new GridLayout(2,1));
		JPanel ps = new JPanel(new FlowLayout());
			buttons.add(seek);
			buttons.add(ps);
			ps.add(prev);
			ps.add(play);
			ps.add(stop);
			ps.add(next);
			
		
		JPanel top = new JPanel(new FlowLayout());
			top.add(title);
		//	top.add(chooseDir);
			
		//vu.add(vu2);	
		vu.add(new JPanel(new FlowLayout()).add(fpane));
		vu.setSize(400,200);
		
		//controls.add(vol);
		
		this.add(vu, BorderLayout.CENTER);
		this.add(new JPanel(new FlowLayout()).add(controls), BorderLayout.EAST);
		JPanel nGrid = new JPanel(new GridLayout(2,2));
		JPanel spacer = new JPanel(new GridLayout(3,1));
		spacer.add(menu);
		spacer.add(new JPanel());
		nGrid.add(new JPanel(new GridLayout(1,2)).add(vu2));
		nGrid.add(vol);
		nGrid.add(randomPanel);
		JPanel nGrid2 = new JPanel(new GridLayout(2,1));
		nGrid2.add(spacer);
		nGrid2.add(nGrid);
		this.add(nGrid2, BorderLayout.NORTH);
		//this.add(top, BorderLayout.NORTH);
		this.add(buttons, BorderLayout.SOUTH);
		
		playActionListener();
		stopActionListener();
		volActionListener();
		chooseDirActionListener();
		prevActionListener();
		nextActionListener();
		setEQActionListener();
		equalizer();
		
		for(int i = 0; i<800; i = i+25)
		{
			this.setSize(i/2, i);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		
		/*new Thread()
		{
			public void run()
			{
				JPanel test = new JPanel();
				test.add(new JLabel("HELLO WORLD"));
				showSlideout(test, 100);
				String jp = "Welcome to JMediaPlayer!";
				while(greet)
				{
					for(int i = 0; i<jp.length();i++)
					{
						title.setText(jp.substring(0,i));
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					for(int i = jp.length(); i>0;i--)
					{
						title.setText(jp.substring(0,i));
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}	
		}.start();*/
		
		shutdownEQ();
	}
	
	public static void setUI()
	{
		UIManager.LookAndFeelInfo plafinfo[] = UIManager.getInstalledLookAndFeels();
		boolean nimbusfound=false;
		int nimbusindex=0;

		for (int look = 0; look < plafinfo.length; look++) {
		if(plafinfo[look].getClassName().toLowerCase().contains("nimbus"))
		{
		nimbusfound=true;
		nimbusindex=look;
		}
		}

		try {

		if(nimbusfound)
		{ 
		UIManager.setLookAndFeel(plafinfo[nimbusindex].getClassName());
		}
		else

		UIManager.setLookAndFeel(
		UIManager.getCrossPlatformLookAndFeelClassName());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public JPanel generateEQPanel(AudioInputStream in)
	{
		JPanel fin = new JPanel(new BorderLayout());
		JPanel buttons = new JPanel(new FlowLayout());
		JPanel panel = new JPanel(new GridLayout(1,8));
		fin.add(panel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createTitledBorder("Equalizer"));
		
		final float[] equalizer;
		final JButton ok = new JButton("Save");
		if( in instanceof javazoom.spi.PropertiesContainer )
		{
			Map properties = ((javazoom.spi.PropertiesContainer)in).properties();
				equalizer = (float[]) properties.get("mp3.equalizer");
				for(int z = 0; z<MP3SPI.getEqualizer().length;z++)
				{
					equalizer[z] = MP3SPI.getEqualizer()[z];
				}
				final JSlider[] eqAdj = new JSlider[equalizer.length/4];
				for(int i = 0; i<eqAdj.length;i++)
				{
					eqAdj[i] = new JSlider(SwingConstants.VERTICAL);
					eqAdj[i].setBorder(BorderFactory.createRaisedBevelBorder());
					eqAdj[i].setMinimum(-10);
					eqAdj[i].setMaximum(10);
					System.out.printf("EQ[%d] = %f\n", i, equalizer[i*4]);
					eqAdj[i].setValue((int) ((equalizer[i*4+1])*10));
					panel.add(eqAdj[i]);
					
					eqAdj[i].addChangeListener(new ChangeListener(){
						@Override
						public void stateChanged(ChangeEvent arg0) {
							int q = 0;
							while((JSlider)arg0.getSource() != eqAdj[q])
							{
								q++;
							}
							for(int i = q*4; i<(q*4)+4;i++)
							{
								equalizer[i] = (float)eqAdj[q].getValue()/10;
								System.out.printf("Setting EQ%d to %f\n", i, equalizer[i]);
							}
						}
						
					});
				}
				
				for(int a = 0; a<equalizer.length;a++)
					System.out.println("["+a+"] "+equalizer[a]);
				
				final JButton reset = new JButton("Reset");
				reset.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						for(int i = 0; i<equalizer.length;i++)
						{
							equalizer[i] = (float)0.0;
							eqAdj[i/4].setValue(0);
						}
						
					}
					
				});
				buttons.add(reset);
			
				ok.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						latch.countDown();
						ok.setText("Saving...");
						ok.setEnabled(false);
						if(equalizer != null)
							MP3SPI.setEqualizer(equalizer);
						
						
					}
					
				});
		}
		else
		{
			ok.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					latch.countDown();
					ok.setEnabled(false);
				}
				
			});
		}
		buttons.add(ok);
		fin.add(new JPanel(new FlowLayout()).add(buttons), BorderLayout.SOUTH);

		return fin;
	}
	
	public static void showSlideout(JPanel panel, final int xmul, final int ysize, final boolean useLock)
	{
		if(titleSlider != null)
		{
			titleSlider.dispose();
			return;
		}
		
		if(panel == null)
			return;
		
		titleSlider = new JFrame();
		titleSlider.setUndecorated(true);
		titleSlider.setBackground(new Color(100,100,100,155));
		
		JPanel jp = new JPanel(new FlowLayout(FlowLayout.CENTER));
			jp.setBorder(BorderFactory.createLoweredBevelBorder());
			panel.setBackground(new Color(100,100,100,0));
			jp.add(panel);
			jp.setBackground(new Color(100,100,100,155));
		titleSlider.add(jp);
		
		titleSlider.setVisible(true);
		//this.toFront();
		//this.requestFocus();
		if(titleSlider.isFocused())
		{
			UI.getFrames()[0].toFront();
			UI.getFrames()[0].requestFocus();
		}
		
		new Thread(){
			public void run()
			{
					int x = UI.getFrames()[0].getLocation().x + 380;
					int y = UI.getFrames()[0].getLocation().y + 54;

					for(int i = 0; i<400; i = i+25)
					{
						titleSlider.setSize(i*xmul, ysize);
						titleSlider.setLocation(x, y);
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {}
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}
					
					try {
					if(useLock)
					{
						latch.await();
						latch = new CountDownLatch(1);
					}
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
					for(int i = 400; i>0; i = i-25)
					{
						titleSlider.setSize(i*xmul, ysize);
						titleSlider.setLocation(x, y);
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {}
					}
					titleSlider.setVisible(false);
					titleSlider.dispose();
					titleSlider = null;
			}
		}.start();
	}
	
	public void equalizer()
	{
		for(int i = 0; i<eq.length;i++)
		{
			eq[i] = new JProgressBar(SwingConstants.VERTICAL);
				eq[i].setMaximum(100);
//				eq[i].setMinimum(500 - (i*15));
				//eq[i].setMaximum(20000);
				eq[i].setMinimum(40);
				eq[i].setPreferredSize(new Dimension(15,125));
				eq[i].setValue(50);
			vu2.add(eq[i]);
		}	
	}
	
	public static void shutdownEQ()
	{
		new Thread(){
			
			public void run()
			{
				for(int q = 73; q >= 0 ; q--)
				{	
					for(int i = 0; i<eq.length;i++)
						eq[i].setValue(eq[i].getValue()-1);
					/*eq[1].setValue(eq[1].getValue()-1);
					eq[2].setValue(eq[2].getValue()-1);
					eq[3].setValue(eq[3].getValue()-1);
					eq[4].setValue(eq[4].getValue()-1);
					eq[5].setValue(eq[5].getValue()-1);
					eq[6].setValue(eq[6].getValue()-1);*/
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public static void playActionListener()
	{
		play.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(jmp.spi==null)
					jmp.spi = new MP3SPI(jmp);
				
				int i = files.getSelectedIndex();
				if(play.getText().equals("Play"))
				{
					if(files.getSelectedIndex() != -1)
					{
						if(MP3SPI.mp3 !=null && MP3SPI.mp3.isAlive()&& jmp.spi.isPaused())
						{
							jmp.spi.pauseTrack(false);
							System.out.println("Unpausing track");
						}
						else
						{
							jmp.spi.stopPlayer();
							MP3SPI.testPlay(dir + File.separator + lm.elementAt(i));
						}
					}
				}
				else if(play.getText().equals("Pause"))
				{
					if(MP3SPI.mp3 !=null && MP3SPI.mp3.isAlive()&& jmp.spi.isPaused())
					{
						jmp.spi.pauseTrack(false);
						System.out.println("Unpausing track");
					}
					else
					{
						jmp.spi.pauseTrack(true);
						System.out.println("Unpausing track");
					}
					
				}
			}
			
		});
		
		files.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent evt)
			{
				if(jmp.spi==null)
					jmp.spi = new MP3SPI(jmp);
				
				if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1)
				{
					jmp.spi.stopPlayer();
					while(MP3SPI.mp3 !=null && MP3SPI.mp3.isAlive()){try{Thread.sleep(10);} catch (InterruptedException e){}};
					MP3SPI.testPlay(music.get(files.getSelectedIndex()).getFilepath());
				}
			}
		});
	}
	
	public static void prevActionListener()
	{
		prev.addMouseListener(new MouseAdapter(){
			
			public void mouseClicked(MouseEvent evt)
			{
				if(jmp.spi==null)
					jmp.spi = new MP3SPI(jmp);
				
				if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1)
				{
					if((files.getSelectedIndex() -1) >= 0)
					{
						jmp.spi.stopPlayer();
						files.setSelectedIndex(files.getSelectedIndex() -1 );
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						MP3SPI.testPlay(music.get(files.getSelectedIndex()).getFilepath());
					}
				}
				else if(evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON1)
				{
					jmp.spi.stopPlayer();
					MP3SPI.testPlay(music.get(files.getSelectedIndex()).getFilepath());
				}
			}
		});
	}
	
	public static void nextActionListener()
	{
		next.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(jmp.spi==null)
					jmp.spi = new MP3SPI(jmp);
				
				MP3SPI.nextTrack();
			}
			
		});
	}
	
	public static void stopActionListener()
	{
		stop.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(jmp.spi != null && MP3SPI.mp3.isAlive()&& jmp.spi.isPaused())
				{
					jmp.spi.pauseTrack(false);
					play.setText("Play");
				}
				jmp.spi.stopPlayer();
				shutdownEQ();
				jmp.spi = null;
			}
			
		});
	}
	
	public static void volActionListener()
	{
		vol.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {
				
				jmp.adjVolume((float)vol.getValue()/1000);
			}
			
		});
	}
	
	public void chooseDirActionListener()
	{
		chooseDir.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				if(lm!=null)
				{
					lm.clear();
					files.setModel(lm);
				}
			    if (chooser.showOpenDialog(jmp.ui) == JFileChooser.APPROVE_OPTION) {
			    	File file;
			    	
						file = chooser.getSelectedFile();
						dir = file.getAbsolutePath();
						JMediaPlayer.appendToFile(jmp.config, dir);
						System.out.println(dir);
						
						addToMusicList(file);
			    }
			}
			
		});
	}
	
	public void setEQActionListener()
	{
		setEQ.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				showSlideout(generateEQPanel(MP3SPI.din),1,300, true);
			}
		});
	}
	
	public void addToMusicList(File file)
	{
    	File[] list = file.listFiles();
    	
    	//lm.clear();
    	//music.clear();
    	
    	if(list != null)
	    	for(int i = 0; i < list.length; i++)
	    	{
	    		if(list[i].isDirectory())
	    		{
	    			addToMusicList(list[i]);
	    		}
	    		else if(list[i].getName().contains(".mp3"))
	    		{
	    			try {
						music.add(new MusicItem(list[i].getName(), list[i].getCanonicalPath()));
						lm.addElement(list[i].getName());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    	}
    	//files.
    	files.setModel(lm);
    	revalidate();
	}
}
