import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;


public class ImageProcessor {

	public static BufferedImage displayOriginal(String fileName, int width, int height){
		
		  BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		    try {
			    File file = new File(fileName);
			    InputStream is = new FileInputStream(file);
		
			    long len = file.length();
			    byte[] bytes = new byte[(int)len];
			    
			    int offset = 0;
			    int numRead = 0;
			    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		          offset += numRead;
			    }
		    
		    		
		    	int ind = 0;
				  for(int y = 0; y < height; y++){
			
					for(int x = 0; x < width; x++){
				 
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
						
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
						img.setRGB(x,y,pix);
						ind++;
					}
				}
				
				
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    
		    return img;
	}
	
	public static BufferedImage displayModifiedImage(String fileName, int width, int height, 
			int ysamp, int usamp, int vsamp, int quant) throws IOException{
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		try {
		    File file = new File(fileName);
		    InputStream is = new FileInputStream(file);
	
		    long len = file.length();
		    byte[] bytes = new byte[(int)len];
		    
		    int offset = 0;
		    int numRead = 0;
		    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	          offset += numRead;
		    }
	    
	    		
	    	int ind = 0;
			  for(int j = 0; j < height; j++){
		
				for(int i = 0; i < width; i++){
			 
					byte ba = 0;
					byte br = bytes[ind];
					byte bg = bytes[ind+height*width];
					byte bb = bytes[ind+height*width*2]; 
					
					int pixel = 0xff000000 | ((br & 0xff) << 16) | ((bg & 0xff) << 8) | (bb & 0xff);
					
					int r = (pixel >> 16) & 0xff;
				    int g = (pixel >> 8) & 0xff;
				    int b = (pixel) & 0xff;
			        //get YUV
			        int y = (int) (r*0.299 + g*0.587 +b*0.114);
			        int u = (int) (r*(-0.147) + g*(-0.289) + b*0.436);
			        int v = (int) (r*(0.615) + g*(-0.515) + b*(-0.100));
			        
			        int newr = (int) (y*0.999 + u*0.000 + v*1.140);
			        int newg = (int) (y*1.000 + u*(-0.395) + v*(-0.581));
			        int newb = (int) (y*1.000 + u*2.032 + v*(-0.000)); 
					
					int pix = ((0 << 24) + (newr << 16) + (newg << 8) + newb);
					img.setRGB(i,j,pix);
					ind++;
				}
			}
			
			
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
		
		
		return img;
		
	}
  
    public static void main(String[] args) throws IOException {
    	
    	String fileName = args[0];
    	int ysamp = Integer.parseInt(args[1]);
    	int usamp = Integer.parseInt(args[2]);
    	int vsamp = Integer.parseInt(args[3]);
    	int quant = Integer.parseInt(args[4]);
    	
    	int width = 352;
    	int height = 288;
       
	    // Use a panel and label to display the image
	    JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (displayOriginal(fileName, width, height))));
	    panel.add (new JLabel (new ImageIcon (displayModifiedImage(fileName, width, height, ysamp, usamp, vsamp, quant))));
	    
	    JFrame frame = new JFrame("Display images");
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
	
	 }
    
    

  
}