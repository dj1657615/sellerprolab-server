package common;

import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import java.awt.GraphicsEnvironment;
import java.awt.FontFormatException;
import java.io.IOException;

public class Layout {
	
	public static String UPDATER_FRAME = "resource/updaterFrame.png";
	
	public static Font PRETENDARD_THIN_FONT(int size) {return customFont("resource/fonts/Pretendard-Thin.ttf",size);}
	public static Font PRETENDARD_LIGHT_FONT(int size) {return customFont("resource/fonts/Pretendard-Light.ttf",size);}
	public static Font PRETENDARD_REGULAR_FONT(int size) {return customFont("resource/fonts/Pretendard-Regular.ttf",size);}
	public static Font PRETENDARD_MEDIUM_FONT(int size) {return customFont("resource/fonts/Pretendard-Medium.ttf",size);}
	public static Font PRETENDARD_SEMIBOLD_FONT(int size) {return customFont("resource/fonts/Pretendard-SemiBold.ttf",size);}
	public static Font PRETENDARD_BOLD_FONT(int size) {return customFont("resource/fonts/Pretendard-Bold.ttf",size);}
	public static Font PRETENDARD_EXTRABOLD_FONT(int size) {return customFont("resource/fonts/Pretendard-ExtraBold.ttf",size);}
	public static Font PRETENDARD_BLACK_FONT(int size) {return customFont("resource/fonts/Pretendard-BLACK.ttf",size);}
	
	public static Font customFont(String path,int size) {
        Font customFont = loadFont(path, size);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);
        return customFont;

    }
	
	public static Font loadFont(String path, float size){
	    try {
	        Font myFont = Font.createFont(Font.TRUETYPE_FONT
	        		, new File(path) );
	        return myFont.deriveFont(size);
	    } catch (FontFormatException | IOException e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	    return null;
	}
}
