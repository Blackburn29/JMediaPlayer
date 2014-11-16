package me.Blackburn.JMP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.Port.Info;


public class JMediaPlayer
{
	File config;
	String PATH = ("D:"+File.separator+"My Music"+File.separator+"Flux Pavilion - The Scientist [edmeXQlusiv.com].mp3");
	
	UI ui = new UI(this);
	MP3SPI spi;
	
	
	public static void main(String[] args)
	{
		JMediaPlayer main = new JMediaPlayer();
		main.init();
	}
	
	public void init()
	{
		config = new File(System.getProperty("user.dir")+File.separator+"JMPConfig.dat");
		if(!config.exists())
		{
			try {
				config.createNewFile();
			} catch (IOException e) {
			}
		}
		else
		{
			loadMusicDirs();
		}
	}
	
	public void loadMusicDirs()
	{
		Scanner scanner;
		try {
			scanner = new Scanner(config);
			while(scanner.hasNextLine())
			{
				String in = scanner.nextLine().trim();
					File file = new File(in);
					ui.addToMusicList(file);
					UI.dir = file.getAbsolutePath();
					
			}
		} catch (FileNotFoundException e) {
		}
	}
	
	public void adjVolume(float vol)
	{
		Info source = Port.Info.SPEAKER;
		
		if(AudioSystem.isLineSupported(source))
		{
			try {
				Port outline = (Port) AudioSystem.getLine(source);
				outline.open();
				FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
					volumeControl.setValue(vol);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void appendToFile(File fName, String txt)
	{
		try {
			FileWriter fileWriter = new FileWriter(fName.getAbsolutePath(),true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
	        bufferWritter.write(txt+"\r\n");
	        bufferWritter.close();
		} catch (IOException e) {
		}
	}
	
	
	
/*	public void initPlayer()
	{
		String PATH = ("D:"+File.separator+"My Music"+File.separator+"Flux Pavilion - The Scientist [edmeXQlusiv.com].mp3");
		File file = new File(PATH);
		try {
			AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
			
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),16,baseFormat.getChannels(),baseFormat.getChannels()*2,baseFormat.getSampleRate(),false);
			
			din = AudioSystem.getAudioInputStream(decodedFormat,in);
			rawplay(decodedFormat, din);
			in.close();
			
		} catch (UnsupportedAudioFileException
				| IOException | LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static synchronized void rawplay(final AudioFormat targetFormat, final AudioInputStream din) throws IOException, LineUnavailableException
	{
		final byte[] data = new byte[4096];
		final SourceDataLine line = getLine(targetFormat);
		if(line != null)
		{
			line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1) {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) {
                    nBytesWritten = line.write(data, 0, nBytesRead);
                    System.out.println("... -->" + data[0] + " bytesWritten:" + nBytesWritten);
                }                                           
            }
			
		}
	}
	
	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
	  SourceDataLine res = null;
	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	  res = (SourceDataLine) AudioSystem.getLine(info);
	  res.open(audioFormat);
	  return res;
	}
*/
	
	
}
