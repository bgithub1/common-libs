package com.billybyte.ui.messagerboxes;

import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.billybyte.commonstaticmethods.Utils;

/**
 * Loop to feed a }@code QueryInterface<String,String>} stuff to process
 * @author bperlman1
 *
 */
public class MessageBoxLooper{
	public interface MessageBoxLooperCallBack{
		public String processResponse(String msgBoxResponse);
	}
	private final JFrame jframe = new JFrame();
	private final MessageBoxLooperCallBack callBack;
	private final String title;
	private final String initialMessageToDisplay;
	private final boolean useConsole;
	
	
	/**
	 * 
	 * @param callBack MessageBoxLooperCallBack which process message box response
	 * @param timeoutValue - timeout for the query
	 * @param timeoutType - timeoutType 
	 * @param title - title to be displayed in MessageBox
	 * @param initialMessageToDisplay - initial contents of MessageBox entry area
	 */
	public MessageBoxLooper(
			MessageBoxLooperCallBack callBack,
			int timeoutValue,TimeUnit timeoutType,
			String title,
			String initialMessageToDisplay){
		this.callBack = callBack;
		this.title = title;
		this.initialMessageToDisplay = initialMessageToDisplay;
		this.useConsole=false;
	}

	/**
	 * 
	 * @param query - any query that you want to repeating execute
	 * @param timeoutValue - timeout for the query
	 * @param timeoutType - timeoutType 
	 * @param title - title to be displayed in MessageBox
	 * @param initialMessageToDisplay - initial contents of MessageBox entry area
	 */
	public MessageBoxLooper(
			MessageBoxLooperCallBack callBack,
			String title,
			String initialMessageToDisplay,
			boolean useConsole){
		this.callBack = callBack;
		this.title = title;
		this.initialMessageToDisplay = initialMessageToDisplay;
		this.useConsole = useConsole;
	}
	

	
	public void loop(){
		String messageToDisplay = initialMessageToDisplay;
		String response= "";
		while(true){
			if(this.useConsole){
				response = MessageBox.ConsoleMessage(title+" : " + messageToDisplay + " ->");
			}else{
				response = MessageBox.MessageBoxNoChoices(jframe, 
						messageToDisplay, title, response);
			}
			if(response.compareTo("  ")<=0){
				break;
			}
			
			messageToDisplay = callBack.processResponse(response);
		}
		Utils.prtObMess(this.getClass()," EXECUTED ALL REQUESTS. EXITING NOW");
		
	}
}
