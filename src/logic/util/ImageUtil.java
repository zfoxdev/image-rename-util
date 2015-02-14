package logic.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageUtil {

	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, 
		image.getBounds().width, image.getBounds().height, 
		0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
	}
	
	public static Image scaledResize(Image image, int width, int height) {
		int startWidth = image.getBounds().x;
		int startHeight = image.getBounds().y;
		int scaledWidth = width;
		int scaledHeight = height;
		if(startWidth > startHeight){
			scaledHeight =  scaledWidth/startWidth*startHeight;
		}else{
			scaledWidth = scaledHeight/startHeight*startWidth;
		}
		return resize(image, scaledWidth, scaledHeight);
	}
	
}
