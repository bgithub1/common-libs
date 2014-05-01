package com.billybyte.ui.messagerboxes;

import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextArea;

public class WarningFrame extends javax.swing.JFrame  implements ActionListener{
	JTextArea jta ;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1740192429694298375L;

	public WarningFrame(String message, String title) throws HeadlessException  {
		super();
		FlowLayout fl = new FlowLayout();
		this.setLayout(fl);
		JButton jb = new JButton(message + "\n" + " - Press to Close" );
		this.getContentPane().add(jb);
		this.setTitle(title);
		this.setLocation(100, 100);
		this.setSize(message.length()*10, 100);
		this.setTitle(title);
		jb.addActionListener(this);
		this.setVisible(true);
	}
	
	public WarningFrame(String message, String title, String defaultText) throws HeadlessException  {
		super();
		FlowLayout fl = new FlowLayout();
		this.setLayout(fl);
		JButton jb = new JButton(message);
		this.getContentPane().add(jb);
		jta = new JTextArea(defaultText);
		jta.setSize(200, 20);
		this.getContentPane().add(jta);
		this.setTitle(title);
		this.setLocation(100, 100);
		this.setSize(300, 100);
		this.setTitle(title);
		jb.addActionListener(this);
		this.setVisible(true);
		
	}

	public JTextArea getJTextArea(){
		return jta;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		this.setVisible(false);
		
		this.dispose();
	}
	
}
