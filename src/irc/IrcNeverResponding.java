/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.*;
import java.awt.event.*;

import jvn.*;

import java.io.*;


public class IrcNeverResponding {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	ISentence       sentence;


  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {
		   
		// initialize JVN
		JvnServerImplNeverResponding js = JvnServerImplNeverResponding.jvnGetServer();
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		ISentence jo = (ISentence) JvnInterceptor.newInstance("IRC",Sentence.class);
		   
		// create the graphical part of the Chat application
		new IrcNeverResponding(jo);
//	   jo.write("q");
//	   System.out.println(jo.read());
	   } catch (Exception e) {
		   e.printStackTrace();
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public IrcNeverResponding(ISentence jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener3(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener3(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter(){
			  public void windowClosing(WindowEvent we){
				  try {
					JvnServerImplNeverResponding.jvnGetServer().jvnTerminate();
				} catch (JvnException e) {
					e.printStackTrace();
				}
				  frame.dispose();
			  }
		});
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener3 implements ActionListener {
	IrcNeverResponding irc; 
  
	public readListener3 (IrcNeverResponding i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {
	 try {
		// lock the object in read mode
		//irc.sentence.jvnLockRead();
		
		// invoke the method
		String s = ((ISentence)irc.sentence).read();
		
		// unlock the object
		//irc.sentence.jvnUnLock();
		
		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	   } catch (JvnException je) {
		   je.printStackTrace();
		   System.out.println("IRC problem : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener3 implements ActionListener {
	IrcNeverResponding irc;
  
	public writeListener3 (IrcNeverResponding i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
	   try {	
		// get the value to be written from the buffer
    String s = irc.data.getText();
        	
    // lock the object in write mode
		//irc.sentence.jvnLockWrite();
		
		// invoke the method
		((ISentence)irc.sentence).write(s);
		
		// unlock the object
		//irc.sentence.jvnUnLock();
	 } catch (JvnException je) {
		je.printStackTrace();
		System.out.println("IRC problem  : " + je.getMessage());
	 }
	}
}



