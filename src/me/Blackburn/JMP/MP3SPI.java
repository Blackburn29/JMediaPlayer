package me.Blackburn.JMP;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;


public class MP3SPI {
	
	static SourceDataLine line;
	static AudioInputStream din;
	
	private static JMediaPlayer jmp;
	
	private static boolean stop = false;
	private static boolean pause = false;
	
	private static float equalizer[] = new float[31];
	
	static byte[] data;
	
	static Thread mp3, fft;
	
	public MP3SPI(JMediaPlayer instance)
	{
		MP3SPI.jmp = instance;
	}

	public static void testPlay(String filename)
	{
	  try { 
	    File file = new File(filename);
	    final AudioInputStream in= AudioSystem.getAudioInputStream(file);
	    din = null;
	    AudioFormat baseFormat = in.getFormat();
	    AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
	    final AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
	                                                                                  baseFormat.getSampleRate(),
	                                                                                  16,
	                                                                                  baseFormat.getChannels(),
	                                                                                  baseFormat.getChannels() * 2,
	                                                                                  baseFormat.getSampleRate(),
	                                                                                  false);
	    
	    if (baseFileFormat instanceof TAudioFileFormat)
	    {
	        @SuppressWarnings("rawtypes")
			Map properties = ((TAudioFileFormat)baseFileFormat).properties();
	      //  String key = "author";
	     //   String val = (String) properties.get(key);
	       // key = "duration";
	        String title = (String) properties.get("title");
	        String author = (String) properties.get("author");
	        String album = (String) properties.get("album");
	        
	        
	        
	        javax.swing.JPanel infos = new javax.swing.JPanel(new java.awt.GridLayout(3,1));
	        infos.add(new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER)).add(new javax.swing.JLabel(title)));
	        infos.add(new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER)).add(new javax.swing.JLabel(author)));
	        infos.add(new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER)).add(new javax.swing.JLabel(album)));
	        UI.showSlideout(infos,1, 200, false);
	        
	        
	       long length = (Long)properties.get("duration");
	        UI.seek.setMaximum((int)length);
	    }
	    din = AudioSystem.getAudioInputStream(decodedFormat, in);
	    UI.seek.setValue(0);
	    System.out.println("Playing: "+file.getAbsolutePath());
	    UI.play.setText("Pause");
	    // Play now. 
	    
	    mp3 =  new Thread(new Runnable(){
	        public void run() {
	            try {
	            	stop = false;
					rawplay(decodedFormat);
					in.close();
					if(!stop)
					{
						Thread.sleep(300);
						nextTrackWithNoStop();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    }});
	   
	   		mp3.start();
	   		
	  } catch (Exception e)
	    {
	        //Handle exception.
	    }
	} 
	
	public static void nextTrack()
	{
		if(UI.files.getSelectedIndex() != -1 && (UI.files.getSelectedIndex() + 1) < UI.lm.getSize())
		{
			jmp.spi.stopPlayer();
			while(mp3 !=null && mp3.isAlive()){try{Thread.sleep(10);} catch (InterruptedException e){}};
			MP3SPI.testPlay(UI.music.get(UI.files.getSelectedIndex()+1).getFilepath());
			//MP3SPI.testPlay(UI.dir + File.separator + UI.lm.elementAt(UI.files.getSelectedIndex()+1));
			if(!UI.random.isSelected())
			{
				UI.files.setSelectedIndex(UI.files.getSelectedIndex() + 1);
			}
			else
			{
				Random random = new Random();
				int ran = random.nextInt(UI.lm.getSize());
				UI.files.setSelectedIndex(ran-1);
			}
		}
	}
	
	public static void nextTrackWithNoStop()
	{
		if(UI.files.getSelectedIndex() != -1 && (UI.files.getSelectedIndex() + 1) < UI.lm.getSize())
		{
			MP3SPI.testPlay(UI.music.get(UI.files.getSelectedIndex()+1).getFilepath());
			//MP3SPI.testPlay(UI.dir + File.separator + UI.lm.elementAt(UI.files.getSelectedIndex()+1));
			if(!UI.random.isSelected())
			{
				UI.files.setSelectedIndex(UI.files.getSelectedIndex() + 1);
			}
			else
			{
				Random random = new Random();
				int ran = random.nextInt(UI.lm.getSize());
				UI.files.setSelectedIndex(ran);
			}
		}
	}
	
	public boolean isStopped()
	{
		return stop;
	}
	
	public boolean isPaused()
	{
		return pause;
	}
	
	public void pauseTrack(boolean val)
	{
		MP3SPI.pause = val;
	}

	private static void rawplay(AudioFormat targetFormat) throws IOException, LineUnavailableException
	{
	  data = new byte[8192];
	  
	  line = getLine(targetFormat);
	  if (line != null)
	  {
		  
			if( din instanceof javazoom.spi.PropertiesContainer )
			{
				Map properties = ((javazoom.spi.PropertiesContainer)din).properties();
					float[] eq = (float[])properties.get("mp3.equalizer");
					eq = equalizer;
			}
	    // Start 
	    line.start();
	    int nBytesRead = 0, nBytesWritten = 0;
	    
	    int cnt = 0;
	    
	    while (nBytesRead != -1 && !stop)
	    {
	    	while(pause){try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}};
			
			try{
	        nBytesRead = din.read(data, 0, data.length);
	        UI.seek.setBorder(BorderFactory.createTitledBorder(Long.toString(line.getMicrosecondPosition()/1000000)));
	        
	        cnt++;
	        
	        fft = new Thread(new Runnable(){
	        	public void run()
	        	{
	        		int winSize = 4096;
	        		//FloatFFT_1D test = new FloatFFT_1D(8);
	        		DoubleFFT_1D blah = new DoubleFFT_1D(winSize);
	        		double[] fl = toDoubles(data);

	        		for (int i = 0; i < winSize; i++) {
	        			double multiplier = 0.5 * (1 - Math.cos(2*Math.PI*i/(winSize-1)));
	        			fl[i] = multiplier * fl[i];
	        		}

	        		blah.realForward(fl);

	        		for(int i = 0; i<UI.eq.length; i++)
	        		{
	        			double real = fl[(2*i)];
	        			double img = fl[(2*i+1)];
	        			//double img = fl[(2*i+1)*2];
	        			//int value = (int) (20*Math.log10(Math.sqrt((((real*real)+(img*img))))));
	        			int value = (int) Math.sqrt((real*real)+(img*img));
	        			//if(value > UI.eq[i/2].getValue())
	        			{
	        				UI.eq[i].setValue((int)(20*Math.log10(value)));//10*Math.log10
	        			}
	        			//else if(UI.eq[i/2].getValue() > 1)
	        			{
	        				//UI.eq[i/2].setValue(UI.eq[i/2].getValue() - 1);
	        			//UI.eq[i-1].setValue(UI.eq[i-1].getMinimum());
	        			}
	        		}
	        	}
	        });
	        
	        //System.out.println(targetFormat.getSampleSizeInBits());
	        //System.out.println(targetFormat.getFrameRate());
	        
	        if(cnt == targetFormat.getSampleSizeInBits()/3)
	        {
	        if(!fft.isAlive())
	        	fft.start();
	        	cnt = 0;
	        }
	        
        	/*float[] fArr = floatMe(shortMe(data));
        	FloatFFT_1D test = new FloatFFT_1D(7);
        	test.complexForward(fArr);
        	//System.out.println(fArr.length);
        	
        	for(int i = 0; i<32;i++)
        	{
        		int val = Math.abs((int)fArr[i]);
        		
        		if(val <= 31)
	        	{
	        		UI.eq[0].setValue(val);

	        		//System.out.println("Bass low "+val);
	        	}
	        	else if(val > 31 && val <= 62)
	        	{
	        		UI.eq[1].setValue(val);
	        	}
	        	else if(val > 62 && val <= 125)
	        	{
	        		UI.eq[2].setValue(val);
	        	}
	        	else if(val > 125 && val <= 250)
	        	{
	        		UI.eq[3].setValue(val);
	        	}
	        	else if(val > 250 && val <= 500)
	        	{
	        		UI.eq[4].setValue(val);
	        	}
	        	else if(val > 500 && val <= 1000)
	        	{
	        		UI.eq[5].setValue(val);
	        	}
	        	else if(val >= 1000 && val <=2000)
	        	{
	        		UI.eq[6].setValue(val);

	        	}
        		
        	}*/
			} catch (NullPointerException n){return;}
	       
	       // if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
	        if(nBytesRead != -1)
	        {
	        	nBytesWritten = line.write(data, 0, nBytesRead);
	        	UI.seek.setValue((int)line.getMicrosecondPosition());
	        	
	        	/*int rms = calculateRMSLevel(data);
	        	
	        	if(rms < 53 && rms > 50)
	        	{
	        		UI.eq[0].setValue(rms);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 53 && rms < 57)
	        	{
	        		UI.eq[1].setValue(rms);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 57 && rms < 61)
	        	{
	        		UI.eq[2].setValue(rms);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 61 && rms < 65)
	        	{
	        		UI.eq[3].setValue(rms);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 65 && rms < 69)
	        	{
	        		UI.eq[4].setValue(rms);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 69 && rms < 73)
	        	{
	        		UI.eq[5].setValue(rms);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[6].setValue(UI.eq[6].getValue()-1);
	        	}
	        	else if(rms >= 73)
	        	{
	        		UI.eq[6].setValue(rms);
	        		UI.eq[4].setValue(UI.eq[4].getValue()-1);
	        		UI.eq[3].setValue(UI.eq[3].getValue()-1);
	        		UI.eq[2].setValue(UI.eq[2].getValue()-1);
	        		UI.eq[1].setValue(UI.eq[1].getValue()-1);
	        		UI.eq[0].setValue(UI.eq[0].getValue()-1);
	        		UI.eq[5].setValue(UI.eq[5].getValue()-1);
	        	}*/
	        }
	    }
	    // Stop
	    UI.play.setText("Play");
	    line.drain();
	    line.stop();
	    line.close();
	    din.close();
	  } 
	}
	
	public static int calculateRMSLevel(byte[] audioData) {
	    // audioData might be buffered data read from a data line
	    long lSum = 0;
	    for (int i = 0; i < audioData.length; i++) {
	        lSum = lSum + audioData[i];
	    }

	    double dAvg = lSum / audioData.length;

	    double sumMeanSquare = 0d;
	    for (int j = 0; j < audioData.length; j++) {
	        sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
	    }

	    double averageMeanSquare = sumMeanSquare / audioData.length;
	    return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
	}
	
	public void stopPlayer()
	{
	    	stop = true;
	    	UI.play.setText("Play");
	}

	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
	  SourceDataLine res = null;
	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	  res = (SourceDataLine) AudioSystem.getLine(info);
	  res.open(audioFormat);
	  return res;
	}

	public static float[] getEqualizer() {
		return equalizer;
	}

	public static void setEqualizer(float equalizer[]) {
		MP3SPI.equalizer = equalizer;
	} 
	
	public static short[] shortMe(byte[] bytes) {
	    short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
	    ByteBuffer bb = ByteBuffer.wrap(bytes);
	    for (int i = 0; i < out.length; i++) {
	        out[i] = bb.getShort();
	    }
	    return out;
	}
	
	public static float[] floatMe(short[] pcms) {
	    float[] floaters = new float[pcms.length];
	    for (int i = 0; i < pcms.length; i++) {
	        floaters[i] = pcms[i];
	    }
	    return floaters;
	}
	
	public static double[] toDoubles(byte[] bytes)
	{
		double[] fl = new double[bytes.length];
		for(int i = 0; i<fl.length;i++)
		{
			fl[i] = (double)bytes[i];
		}
		return fl;
	}
	
}
