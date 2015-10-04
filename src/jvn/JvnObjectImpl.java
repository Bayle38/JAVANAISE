package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {            
                                                             
	Serializable obj;                                        
    Verrou state;
    int id;
	public JvnObjectImpl(Serializable o,int i) {                   
		obj = o;   
		state = Verrou.NL;
		id = i;
	}                                                        
                                                             
	public void jvnLockRead() throws JvnException {          
		// TODO Auto-generated method stub                   
	}                                                        
                                                             
	public void jvnLockWrite() throws JvnException {         
		// TODO Auto-generated method stub                   
	}                                                        
                                                             
	public void jvnUnLock() throws JvnException {            
		// TODO Auto-generated method stub                   
	}

	public int jvnGetObjectId() throws JvnException {
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return obj;
	}

	public void jvnInvalidateReader() throws JvnException {
		switch(state){
		case NL:break;
		case R:break;
		case RC:break;
		case W:break;
		case WC:break;
		case RWC:break;
		}
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		switch(state){
		case NL:break;
		case R:break;
		case RC:break;
		case W:break;
		case WC:break;
		case RWC:break;
		}
		return obj;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch(state){
		case NL:break;
		case R:break;
		case RC:break;
		case W:break;
		case WC:break;
		case RWC:break;
		}
		return obj;
	}

}
