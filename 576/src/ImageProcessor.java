import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
//						int pix = ((a << 24) + (r << 16) + (g << 8) + b);
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
		YUV trans[][] = new YUV[height][width];
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
			 
					byte a = 0;
					byte rr = bytes[ind];
					byte gg = bytes[ind+height*width];
					byte bb = bytes[ind+height*width*2]; 
					
					int r = rr & 0xff;
					int g = gg & 0xff;
					int b = bb & 0xff;

			        //get YUV
			        double y = (r*0.299 + g*0.587 +b*0.114);
			        double u = (r*(-0.147) + g*(-0.289) + b*0.436);
			        double v = (r*(0.615) + g*(-0.515) + b*(-0.100));
			        
			        YUV yuv = new YUV();
			        yuv.setY(y);
			        yuv.setU(u);
			        yuv.setV(v);
			        
			        trans[j][i] = yuv;
					ind++;
				}
			}
			
			  //process yuv subsampling
			  for(int k = 0; k < height; k++){
					for(int m = 0; m < width; m++){
						
						//get y
						double newy = trans[k][m].getY();
						double newu;
						double newv;
						
						if((m%usamp)!=0){
							int h = m/usamp;
							if((h+1)*usamp>=width){
								newu = trans[k][h*usamp].getU();
							}else{
								newu = (trans[k][h*usamp].getU()+trans[k][(h+1)*usamp].getU())/2;
							}
						}else{
							newu = trans[k][m].getU();
						}
						
						if((m%vsamp)!=0){
							int h = m/vsamp;
							if((h+1)*vsamp>=width){
								newv = trans[k][h*vsamp].getV();
							}else{
								newv = (trans[k][h*vsamp].getV()+trans[k][(h+1)*vsamp].getV())/2;
							}
						}else{
							newv = trans[k][m].getV();
						}

						
						//rgb quantization
						List<Double> rgbList = rgbQuantization(newy, newu, newv, quant);
						
						int newr = rgbList.get(0).intValue();
						int newg = rgbList.get(1).intValue();
						int newb = rgbList.get(2).intValue();
						
				        int pix = ((0 << 24) + (newr << 16) + (newg << 8) + newb);
				        
				        
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
  
    private static List<Double> rgbQuantization(double newy, double newu, double newv, int quant) {
    	
    	double newr = (newy*0.999 + newu*0.000 + newv*1.140);
    	double newg = (newy*1.000 + newu*(-0.395) + newv*(-0.581));
    	double newb = (newy*1.000 + newu*2.032 + newv*(-0.000)); 

    	if(newr<0){
    		newr = 0;
    	}
    	if(newg<0){
    		newg = 0;
    	}
    	if(newb<0){
    		newb = 0;
    	}
    	
    	List<Double> newrgb = new ArrayList<Double>();
		newrgb.add(newr);
		newrgb.add(newg);
		newrgb.add(newb);
        
        if(quant!=256){
        	double a = (double)256/(double)quant;
        	
        	for(int i=0;i<newrgb.size();i++){
        		double item = newrgb.get(i);
        		if((item%a)>=(a/2)){
                	if(((int)(item/a)+1)==quant){
                		newrgb.set(i, ((int)(item/a))*a);
                	}else{
                		newrgb.set(i, ((int)(item/a)+1)*a);
                	}    	
                }else{
                	newrgb.set(i, ((int)(item/a))*a);
                }
        	}
        }
		
		return newrgb;
		
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
	private double y;
	private double u;
	private double v;
	
	
	
	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getU() {
		return u;
	}

	public void setU(double u) {
		this.u = u;
	}

	public double getV() {
		return v;
	}

	public void setV(double v) {
		this.v = v;
	}

	public YUV(){
		
	}
}