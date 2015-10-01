/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{
  // A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	// Référence vers le coordinateur
	JvnRemoteCoord coordinateur;
	Registry registryLocal;
	/** Arguments pour le cache d'objet
	 * 		cache liste des JvnObect
	 * */
	HashMap<String,JvnObject> cache;
	HashMap<Integer,String> map;

	//
  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		// to be completed
		cache = new HashMap<String, JvnObject>();
        JvnRemoteServer server = (JvnRemoteServer) UnicastRemoteObject.exportObject(this, 10000); // Génère un stub vers notre service.
        registryLocal = LocateRegistry.createRegistry(10000);
        registryLocal.rebind("client", server); // publie notre instance sous le nom "client"
        Registry registry2 = LocateRegistry.getRegistry("127.0.0.1", 10000); //l'adresse peut etre changée par celle du coordinateur. 
        coordinateur = (JvnRemoteCoord) registry2.lookup("coord");
	}
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
    // to be completed
		cache = null;
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		// to be completed 
		JvnObject jo = new JvnObjectImpl(o);
		return jo; 
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		// to be completed
		try {
			coordinateur.jvnRegisterObject(jon, jo, js);
			cache.put(jon, jo);
			map.put(jo.jvnGetObjectId(), jon);
		} catch (RemoteException e) {
			// TODO : Gérer les pannes coordinateur. 
			e.printStackTrace();
		}
	}
	
	/**
	* Provide the reference of a JVN object being given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws JvnException {
    // to be completed 
		JvnObject obj;
		try {
			obj = coordinateur.jvnLookupObject(jon, js);
		} catch (RemoteException e) {
			// TODO Gérer les pannes coordinateur.
			e.printStackTrace();
			obj = null;
		}
		return obj;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
	   try {
		coordinateur.jvnLockRead(joi, js);
	} catch (RemoteException e) {
		// TODO : Gérer les pannes coordinateur.
		e.printStackTrace();
	}
	   
	   return null;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		try {
			coordinateur.jvnLockWrite(joi, js);
		} catch (RemoteException e) {
			// TODO : Gérer les pannes coordinateur.
			e.printStackTrace();
		} 
		return null;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,JvnException {
		// to be completed 
	  	cache.get(joi).jvnInvalidateReader();
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
	  	cache.get(joi).jvnInvalidateWriter();
		return null;
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		cache.get(joi).jvnInvalidateWriterForReader(); 
		return null;
	 };

}

 
