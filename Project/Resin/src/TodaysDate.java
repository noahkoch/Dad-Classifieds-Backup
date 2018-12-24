/**
 * StartDate.java 05/31/99 Tom Kochanowicz
 * 02/04/03 TJK
 * This Applet shows todays date. In the ManageClassified.jsp page
 * and may show in other pages.
 */

import java.util.*;
import java.text.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;
import java.awt.FontMetrics;

public class TodaysDate extends java.applet.Applet {
	public void paint(Graphics g){

		Calendar calendar1;
		SimpleDateFormat formatter1;
		java.util.Date todaysDate;

		// Format todaysDate
		calendar1 = Calendar.getInstance();
		formatter1 = new SimpleDateFormat("MM/dd/yy");
		formatter1.setTimeZone(java.util.TimeZone.getDefault());  // bug fix so not PST & is CST
		todaysDate = calendar1.getTime();

		// Print out todaysDate.
		Font f = new Font("Serif", Font.BOLD, 16);
		g.setFont(f);
		g.setColor(Color.blue);
		setBackground(Color.white);	
		g.drawString(formatter1.format(todaysDate), 10, 25);

	}
}

		
