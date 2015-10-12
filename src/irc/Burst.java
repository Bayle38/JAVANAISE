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
import java.util.Random;


public class Burst {
	ISentence       sentence;
  /**
  * main method
  * create a JVN object nammed IRC for representing the Chat application
  **/
	public static void main(String argv[]) {
	   try {
		   
		// initialize JVN
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		ISentence jo = (ISentence) JvnInterceptor.newInstance("IRC",Sentence.class);
		   
		// create the graphical part of the Chat application
		new Burst(jo);
//	   jo.write("q");
//	   System.out.println(jo.read());
		int i = 0;
		while(true){
			Thread.sleep(100);
			Random r = new Random();
			if(r.nextInt(1)==0){
				System.out.println(jo.read());
			}else{
				jo.write("J'écris " + i++);
			}
		}
	   } catch (Exception e) {
		   e.printStackTrace();
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the JVN object representing the Chat
   **/
	public Burst(ISentence jo) {
		sentence = jo;
	}
}




