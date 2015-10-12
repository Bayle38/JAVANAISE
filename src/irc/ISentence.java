package irc;

import java.io.Serializable;

import jvn.Operation;

public interface ISentence extends Serializable{
	
	@Operation(type="write")
	public void write(String text) ;
	
	@Operation(type="read")
	public String read() ;
}
