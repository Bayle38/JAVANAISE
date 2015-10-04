package jvn;

import java.io.Serializable;
import java.lang.Character.UnicodeScript;

public class JvnObjectImpl implements JvnObject {            
                                                             
	Serializable obj;                                        
    Verrou state;
    int id;
    JvnLocalServer js;
	public JvnObjectImpl(Serializable o,int i) {                   
		obj = o;   
		state = Verrou.NL;
		id = i;
		this.js=js;
		js = JvnServerImpl.jvnGetServer();
	}                                                        
                                                             
	synchronized public void jvnLockRead() throws JvnException {          
		switch(state){
		case NL:js.jvnLockRead(id); state = Verrou.R; break;
		case R:throw new JvnException("Verrou déjà pris : lecture");
		case RC: state = Verrou.R; break;
		case W:break;
		case WC: state = Verrou.RWC; break;
		case RWC:break;
		}
	}                                                        
                                                             
	synchronized public void jvnLockWrite() throws JvnException {         
		switch(state){
		case NL:js.jvnLockWrite(id); state = Verrou.W; break;
		case R: js.jvnLockWrite(id); state = Verrou.W; break;
		case RC: js.jvnLockWrite(id); state = Verrou.W; break;
		case W: throw new JvnException("Verrou déjà pris : écriture");
		case WC:state = Verrou.W;break;
		case RWC:state = Verrou.W;break;
		}
	}                                                        
                                                             
	synchronized public void jvnUnLock() throws JvnException {            
		switch(state){
		case NL: throw new JvnException("Aucun verrou");
		case R: state = Verrou.RC; break;
		case RC: state = Verrou.RC; break;
		case W: state = Verrou.WC; break;
		case WC: state = Verrou.WC; break;
		case RWC: state = Verrou.RWC; break;
		}
		notify();
	}

	public int jvnGetObjectId() throws JvnException {
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return obj;
	}

	synchronized public void jvnInvalidateReader() throws JvnException {
		switch(state){
		case R: 
			try{
				wait();
				state = Verrou.NL; 
			}catch(InterruptedException ie){
				ie.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
			break;
		case RC:state = Verrou.NL; break;
		case NL:
		case W: 
		case WC:  
		case RWC: throw new JvnException("Le verrou ne correspond pas à la demande : "+state);
		}
	}

	synchronized public Serializable jvnInvalidateWriter() throws JvnException {
		switch(state){
		case NL:
		case R:
		case RC:throw new JvnException("Le verrou ne correspond pas à la demande : "+state);
		case W:
			try {
				wait();
				state = Verrou.NL;break;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
		case WC: state = Verrou.NL; break;
		case RWC: state = Verrou.NL; break;
		}
		return obj;
	}

	synchronized public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch(state){
		case W: 
			try {
				wait();
				state=Verrou.RWC;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
			break;
		case WC:state=Verrou.RWC; break;
		case RWC: state = Verrou.RWC; break;
		case NL:
		case R:
		case RC:throw new JvnException("Le verrou ne correspond pas à la demande : "+state);
		}
		return obj;
	}

}
