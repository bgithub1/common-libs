package com.billybyte.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

//import org.apache.log4j.Logger;

import com.billybyte.commonstaticmethods.LoggingUtils;



/**
 * This class redirect System.out and/or System.err to either a swing gui,
 *   or some other output.
 * @author bperlman1
 *
 */
public class RedirectedConsoleForJavaProcess {
	public enum ConsoleType{
		SYSTEM_OUT,SYSTEM_ERR,SYSTEM_BOTH;
	}
	
	private final JFrame jf;
	private final JPanel jpan;
	private final JScrollPane jsp;
	private final JTextArea jta;
	private final OutputStream out;
	private final LoggingUtils logger ;
	private boolean isError=false;
	
	/**
	 * 
	 * @param consoleWidth
	 * @param consoleLength
	 */
	public RedirectedConsoleForJavaProcess(int consoleWidth, int consoleLength){
		this(consoleWidth, consoleLength, null,null,null,null);

	}

	/**
	 * 
	 * @param consoleWidth
	 * @param consoleLength
	 * @param xLoc
	 * @param yLoc
	 * @param headerString
	 */
	public RedirectedConsoleForJavaProcess(
			int consoleWidth, 
			int consoleLength,
			Integer xLoc,
			Integer yLoc,
			String headerString){
		this(consoleWidth, consoleLength, xLoc, yLoc, headerString, null);
	}
	
	public RedirectedConsoleForJavaProcess(
			int consoleWidth, 
			int consoleLength,
			Integer xLoc,
			Integer yLoc,
			String headerString,ConsoleType consoleType){
		this(consoleWidth, consoleLength, xLoc, yLoc, headerString, consoleType, null);
	}	
	
	/**
	 * 
	 * @param consoleWidth
	 * @param consoleLength
	 * @param headerString
	 */
	public RedirectedConsoleForJavaProcess(
			int consoleWidth, 
			int consoleLength,
			Integer xLoc,
			Integer yLoc,
			String headerString,ConsoleType consoleType,
			LoggingUtils lu){
		
		this.logger = null;
		this.jf = new JFrame();
		
		this.jta = new JTextArea(100,100);
		this.jsp = new JScrollPane(jta);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.jpan =new JPanel();
		jpan.setPreferredSize(new Dimension(consoleWidth-1,consoleLength-1));
		jpan.add(jsp, BorderLayout.CENTER);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.getContentPane().add(jsp);
		jf.setSize(new Dimension(consoleWidth,consoleLength));
		jf.setVisible(true);
		out = redirectSystemStreamsToTextArea(jta);
		
		if(xLoc!=null && yLoc!=null){
			jf.setLocation(xLoc, yLoc);
		}
		ConsoleType type  = consoleType!=null ? consoleType :ConsoleType.SYSTEM_BOTH;
		String title = headerString!=null ? headerString : "";
		switch(type){
		case SYSTEM_BOTH:
			System.setOut(new PrintStream(out, true));
			System.setErr(new PrintStream(out, true));
			jf.setTitle(title+" Out and Err");
			break;
		case SYSTEM_ERR:
			System.setErr(new PrintStream(out, true));
			jf.setTitle(title+" System Err");
			isError = true;
			break;
		case SYSTEM_OUT:
			System.setOut(new PrintStream(out, true));
			jf.setTitle(title+" System Out");
			break;
		default:
			break;
		
		}

	}
		 
	
	private static void updateTextArea(
			final String text, 
			final JTextArea textArea, 
			final LoggingUtils logger) {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      textArea.append(text);
		    }
		  });
		}
	
	public  OutputStream redirectSystemStreamsToTextArea(final JTextArea textArea) {
		  OutputStream out = new OutputStream() {
		    @Override
		    public void write(int b) throws IOException {
		      updateTextArea(String.valueOf((char) b),textArea,isError?logger:null);
		    }
		 
		    @Override
		    public void write(byte[] b, int off, int len) throws IOException {
		      updateTextArea(new String(b, off, len),textArea,isError?logger:null);
		    }
		 
		    @Override
		    public void write(byte[] b) throws IOException {
		      write(b, 0, b.length);
		    }
		  };
		  return out;
	}		 


}
