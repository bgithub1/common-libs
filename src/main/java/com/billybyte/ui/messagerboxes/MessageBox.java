package com.billybyte.ui.messagerboxes;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;


import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;


public class MessageBox {
	public final static String MessageBoxWithChoices(
			JFrame frame,
			String messageToDisplay,
			String title,
			Object[] choices,
			Object defaultValue){
			return 
				(String)JOptionPane.showInputDialog(
						frame,
						messageToDisplay,
						title,
						JOptionPane.OK_OPTION,
						null,
						choices,
						defaultValue.toString());				
		
	}
	
	public final static String MessageBoxNoChoices(
			JFrame frame,
			String messageToDisplay,
			String title,
			Object defaultValue){
			return 
				(String)JOptionPane.showInputDialog(
						frame,
						messageToDisplay,
						title,
						JOptionPane.OK_OPTION,
						null,
						null,
						defaultValue.toString());				
		
	}

	public final static String MessageBoxNoChoices(
			String messageToDisplay,
			Object defaultValue){
			return 
				(String)JOptionPane.showInputDialog(
						new JFrame(),
						messageToDisplay,
						messageToDisplay,
						JOptionPane.OK_OPTION,
						null,
						null,
						defaultValue.toString());				
		
	}
	
	public final static int MessageBoxYesNo(
			JFrame frame,
			String messageToDisplay,
			String title){
		return 
		JOptionPane.showConfirmDialog(
				frame,
				messageToDisplay,
				title,
				JOptionPane.YES_NO_OPTION);				
	
	}
	
	public final static void MessageBoxNonModal(String messageToDisplay, String title){
		new WarningFrame(messageToDisplay,title);
	}
	public final static void MessageBoxNonModal(String messageToDisplay, String title,int xloc, int yloc){
		WarningFrame wf = new WarningFrame(messageToDisplay,title);
		wf.setLocation(xloc, yloc);
	}
	
	public final static String ConsoleMessage(String message){
        try {
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			System.out.print(message);
			return console.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * Post a messageBox with a YYYYMMDD date, and accept a date return
	 * @param businessDayOffset offset from today
	 * @param dateSplitChar like "/" or"-"
	 * @return String hopefully, a date in YYYYMMDD form.
	 */
	public static String messageBoxYyyyMmDd(int businessDayOffset,String dateSplitChar){
		Calendar c = Calendar.getInstance();
		c = Dates.addBusinessDays("US", c, businessDayOffset);
		int year = new Integer(c.get(Calendar.YEAR));
		int month = new Integer(c.get(Calendar.MONTH))+1;
		int day = new Integer(c.get(Calendar.DAY_OF_MONTH));
		DecimalFormat dfYear = new DecimalFormat("0000");
		DecimalFormat dfMonthDay = new DecimalFormat("00");
		
		String defaultValue = ""+dfYear.format(year)+dateSplitChar+dfMonthDay.format(month)+dateSplitChar+dfMonthDay.format(day);
		defaultValue = MessageBox.MessageBoxNoChoices(new JFrame(), "adjust date as neede", "Get Date", defaultValue);
		return defaultValue;
	}
	
	
	/**
	 * Non Modal message box to be used for any input.  Since it respawns itself,
	 * 	  you don't need a MessageBoxLoop
	 * @author bperlman1
	 *
	 */
	@SuppressWarnings("serial")
	public static abstract class MessageBoxNonModalWithTextBox extends WarningFrame{
		protected abstract void processCommaSepValuesAndDisplay(String[] messageBoxResponseCommaSepValues);
		protected abstract void processCsvDataAndDisplay(List<String[]> csvData);
		protected abstract MessageBoxNonModalWithTextBox newInstance();
		protected final String message;
		private String defaultText;
		private final boolean reDisplay;
		
		public MessageBoxNonModalWithTextBox(
				String message, 
				String title, 
				String defaultText)
				throws HeadlessException {

			super(message, title, defaultText);
			this.message = message;
			this.defaultText = defaultText;
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.reDisplay=false;
		}

		public MessageBoxNonModalWithTextBox(
				String message, 
				String title, 
				String defaultText,
				boolean reDisplay)
				throws HeadlessException {

			super(message, title, defaultText);
			this.message = message;
			this.defaultText = defaultText;
			this.reDisplay=reDisplay;
			if(!reDisplay){
				this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			}
		}

		protected String getLastMessageBoxResponse(){
			return this.defaultText;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(getJTextArea()==null || getJTextArea().getText().compareTo("   ")<=0){
				super.actionPerformed(arg0);
				System.exit(0);
				return;
			}
			String s = getJTextArea().getText();
			this.defaultText = s;
			if(s.contains(".csv")){
				List<String[]> csv = Utils.getCSVData(s);
				if(csv==null || csv.size()<1)return ;
				processCsvDataAndDisplay(csv);
			}else{
				String[] names = s.split(",");
				if(names!=null && names.length>0){
					processCommaSepValuesAndDisplay(names);
				}
			}
			MessageBoxNonModalWithTextBox newMb = newInstance();
			newMb.setLocation(400, 400);
			if(!reDisplay){
				super.actionPerformed(arg0);
			}
		}
	}

}
