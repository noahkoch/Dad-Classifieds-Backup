/**
 * Date.java 02/22/00 Tom Kochanowicz
 * 02/05/03 TJK
 * This Applet is used to give the user of blueads.com the date & format to enter. 
 */
 
import java.util.*;
import java.text.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;
import java.awt.FontMetrics;
import java.applet.AudioClip;

public class Date extends java.applet.Applet {
	public void paint(Graphics g){

		String Date;
		Calendar calendar1;
		Calendar calendar2;
		SimpleDateFormat formatter1;
		SimpleDateFormat formatter2;
		java.util.Date firstDate;
		java.util.Date lastDate;

		// Format firstDate
		calendar1 = Calendar.getInstance();
		formatter1 = new SimpleDateFormat("MM/dd/yy");
		formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		firstDate = calendar1.getTime();

		// Format lastDate
		calendar2 = Calendar.getInstance();
		formatter2 = new SimpleDateFormat("MM/dd/yy");
		formatter2.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		lastDate = calendar2.getTime();

		// Add Three days to calender1.
		calendar1.add(Calendar.DAY_OF_YEAR, 3);
		firstDate = calendar1.getTime();

		// Add Seven days to calendar2.
		calendar2.add(Calendar.DAY_OF_YEAR, 7);
		lastDate = calendar2.getTime();

		// Print out firstDate and lastDate.
		Font f = new Font("Serif", Font.BOLD, 16);
		g.setFont(f);
		g.setColor(Color.blue);
		setBackground(Color.white);	
		g.drawString(formatter1.format(firstDate) + " thru " + formatter2.format(lastDate), 10, 25);

	}
}

		
