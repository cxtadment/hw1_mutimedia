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
		
		//new a two-dimention array
		YUV a[][] = new YUV[height][width];
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
			        
			        YUV yuv = new YUV();
			        yuv.setY(y);
			        yuv.setU(u);
			        yuv.setV(v);
			        
			        a[j][i] = yuv;
					ind++;
				}
			}
			
			  //process yuv subsampling
			  for(int k = 0; k < height; k++){
					for(int m = 0; m < width; m++){
						
						//get y
						int newy = a[k][m].getY();
						int newu;
						int newv;
						//get u, if subsampling or not
						if(usamp==2&&((m%2)==1)){
							if(m==351){
								newu = a[k][m-1].getU();
							}else{
								newu = (a[k][m-1].getU()+a[k][m+1].getU())/2;
							}
						}else{
							newu = a[k][m].getU();
						}
						//get u, if subsampling or not
						if(vsamp==2&&(m%2==1)){
							if(m==351){
								newv = a[k][m-1].getV();
							}else{
								newv = (a[k][m-1].getV()+a[k][m+1].getV())/2;
							}
						}else{
							newv = a[k][m].getV();
						}
						
						//get new new rgb
						int newr = (int) (newy*0.999 + newu*0.000 + newv*1.140);
				        int newg = (int) (newy*1.000 + newu*(-0.395) + newv*(-0.581));
				        int newb = (int) (newy*1.000 + newu*2.032 + newv*(-0.000)); 
				        
				        int pix = 0;
				        if(quant==256){
				        	pix = ((0 << 24) + (newr << 16) + (newg << 8) + newb);
				        }else if(quant==64){
				        	pix = ((0 << 18) + (newr << 12) + (newg << 6) + newb);
				        }else{
				        	pix = ((0 << 12) + (newr << 6) + (newg << 3) + newb);
				        }
				        
				        
						img.setRGB(m,k,pix);
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

class YUV{
	private int y;
	private int u;
	private int v;
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getU() {
		return u;
	}
	public void setU(int u) {
		this.u = u;
	}
	public int getV() {
		return v;
	}
	public void setV(int v) {
		this.v = v;
	}
	
	public YUV(){
		
	}
}