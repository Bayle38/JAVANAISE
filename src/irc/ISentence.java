package irc;

import java.io.Serializable;

import jvn.JvnException;
import jvn.Operation;

public interface ISentence extends Serializable{
	
	@Operation(type="write")
	public void write(String text) throws JvnException;
	
	@Operation(type="read")
	public String read() throws JvnException;
}
