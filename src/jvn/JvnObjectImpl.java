package jvn;

import java.io.Serializable;
import java.lang.Character.UnicodeScript;
import java.util.concurrent.Semaphore;

public class JvnObjectImpl implements JvnObject {            
    Semaphore queue;                                                    
	Serializable obj;                                        
    Verrou state;
    int id;
    //JvnLocalServer JvnServerImpl.jvnGetServer();
	public JvnObjectImpl(Serializable o,int i) {                   
		obj = o;   
		state = Verrou.NL;
		id = i;
		queue = new Semaphore(1);
	}                                                        
                                                             
	public void jvnLockRead() throws JvnException {          
		switch(state){
		case NL: obj = JvnServerImpl.jvnGetServer().jvnLockRead(id); state = Verrou.R; break;
		case R:throw new JvnException("Verrou déjà pris : lecture");
		case RC: state = Verrou.R; break;
		case W:throw new JvnException("Verrou déjà pris : écriture");
		case WC: state = Verrou.RWC; break;
		case RWC:break;
		}
	}                                                        
                                                             
	public void jvnLockWrite() throws JvnException {         
		switch(state){
		case NL:
		case R: 
		case RC: obj = JvnServerImpl.jvnGetServer().jvnLockWrite(id); state = Verrou.W; break;
		case W: throw new JvnException("Verrou déjà pris : écriture");
		case WC:state = Verrou.W;break;
		case RWC:state = Verrou.W;break;
		}
	}                                                        
                                                             
	public void jvnUnLock() throws JvnException {            
		switch(state){
		case NL: throw new JvnException("Aucun verrou");
		case R: state = Verrou.RC; break;
		case RC:throw new JvnException("Verrou déjà libéré : lecture");
		case W: state = Verrou.WC; break;
		case WC: throw new JvnException("Verrou déjà libéré : écriture");
		case RWC: state = Verrou.WC; break;
		}
		if(queue.availablePermits() == 0)
			queue.release();
	}

	public int jvnGetObjectId() throws JvnException {
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return obj;
	}

	public void jvnInvalidateReader() throws JvnException {
		switch(state){
		case R: 
			try{
				queue.acquire();
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

	public Serializable jvnInvalidateWriter() throws JvnException {
		switch(state){
		case NL:
		case R:
		case RC:throw new JvnException("Le verrou ne correspond pas à la demande : "+state);
		case W:
			try {
				queue.acquire();
				state = Verrou.NL;break;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
		case WC: state = Verrou.NL; break;
		case RWC: 
			try {
				queue.acquire();
				state = Verrou.NL;break;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			} 
		}
		return obj;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch(state){
		case W: 
			try {
				queue.acquire();
				state=Verrou.RC;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
			break;
		case WC:state=Verrou.RC; break;
		case RWC: 
			try {
				queue.acquire();
				state=Verrou.RC;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new JvnException("Une erreur s'est produite lors de la libération du verrou.");
			}
		case NL:
		case R:
		case RC:throw new JvnException("Le verrou ne correspond pas à la demande : "+state);
		}
		return obj;
	}
}
