package irc;

import java.io.Serializable;

public interface ISentence extends Serializable{
	
	@Writing
	public void write(String text) ;
	
	@Reading
	public String read() ;
}
