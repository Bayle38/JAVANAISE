/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jvn.JvnRemoteCoord;
import jvn.DataStore;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;



public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{

	//compteur pour les ID
	private int id;
	//table de correspondance nom symbolique-identifiant objet distant
	private ConcurrentHashMap<String, Integer> tableNomId;
	//table de correspondance identifiant objet distant- DataStore correspondant
	private ConcurrentHashMap<Integer, DataStore> tableIdData;
	
  /**
  * Default constructor
  * @throws JvnException
  **/
	public JvnCoordImpl() throws Exception {
		//création d'un RmiRegistery interne
		Registry reg = LocateRegistry.createRegistry(2015);
		
		//initialisation des structures de données
		id= 0;
		tableNomId = new ConcurrentHashMap<>();
		tableIdData = new ConcurrentHashMap<>();
		
		//passage d'une référence du coordinateur dans le rmiRegistry sous le nom "coord"
		try{
			reg.bind("coord", this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public synchronized int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
	  id++;
	  System.out.println("Generate new ID : " + id);
	  return id; 
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  //On suppose que lorsque l'intercepteur d'objet est créé, le client ne possède aucun verrou sur la resource
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	int joi= jo.jvnGetObjectId();
	//on vérifie qu'un autre objet n'est pas déjà identifié dans la table avec le même nom
	if (tableNomId.containsKey(jon)){
		throw new JvnException();
	}else{ //créer une nouvelle entrée dans les deux tables
		tableNomId.put(jon, joi);
		//ajout d'un nouveau dataStore
		DataStore dt= new DataStore(jo.jvnGetObjectState());
		VerrouListeClients l= dt.getListeVerrouClients();
		l.setVerrou(Verrou.NL); //à la création, une resource est libre et a le verrou NL
		dt.setListeVerrouClients(l);
		//comme le server JS n'a pas de verrou sur la resource, on ne l'enregistre pas dans tableIdData
		tableIdData.put(joi, dt);
	}
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  JvnObject res= null;
		if(tableNomId.containsKey(jon)){
			res=  new JvnObjectImpl(tableIdData.get(tableNomId.get(jon)).getObjDistant(),(int)tableNomId.get(jon));
		}
		return res; 
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	   
	 //la resource existe-t-elle?
	 if (tableIdData.containsKey(joi)){
	 	
		ThreadLockRead thread= new ThreadLockRead(tableIdData, joi, js);
		thread.start();
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			
		}
	 	//on retourne l'objet sérialisable 
	 	//on utilise pas jvnlookup car on devrais alors faire la correspondance entre une valeur (identifiant) et une clef (le nom symbolique)
	 	//dans une hasmap (et non pas l'inverse) afin d'utiliser la fonction
 		return (Serializable)tableIdData.get(joi).getObjDistant();	 			
	}else{
	 	throw new JvnException("resource inexistante");		
	 		}
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	 //la resource existe-t-elle?
		 if (tableIdData.containsKey(joi)){
			ThreadLockWrite thread= new ThreadLockWrite(tableIdData, joi, js);
			thread.start();
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				
			}
		 	//on retourne l'objet sérialisable
		 	//on utilise pas jvnlookup car on devrais alors faire la correspondance entre une valeur (identifiant) et une clef (le nom symbolique)
		 	//dans une hasmap (et non pas l'inverse) afin d'utiliser la fonction
	 		return (Serializable)tableIdData.get(joi).getObjDistant();		 			
		}else{
		 	throw new JvnException("resource inexistante");		
		 		}
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    
	//on parcours toutes les tables et si le jvnremoteserver apparait, alors on le retire et s'il est seul possésseur 
    //d'un verrou, on le passe en NL
    	
    Object [] s= tableIdData.keySet().toArray();
    //parcour de toutes les entrées
    for (int i = 0; i < tableIdData.size(); i++) {
		//le jvnRemoteServer possède-t-il un verrou sur cette resource?
    	ArrayList<JvnRemoteServer> L= tableIdData.get((int)s[i]).getListeVerrouClients().getListeClients();
    	if (L.contains(js)){ //retirer le server de la liste
    		if (tableIdData.get((int)s[i]).getListeVerrouClients().getVerrou() == Verrou.W){
    			//verrou en lecture, récupérer la dernière version de l'objet
    			//TODO g�rer le cas o� le client devient innaccessible ou r�pond trop lentement
    			Serializable o= js.jvnInvalidateWriter((int)s[i]);
    			tableIdData.get((int)s[i]).setObjDistant(o);
    		}
    		L.remove(js);
    		//y a-t-il encore des clients possédant un verrou sur la resource?
    		if (L.isEmpty()){
    			//passer le verrou en NL
    			tableIdData.get((int)s[i]).getListeVerrouClients().setVerrou(Verrou.NL);
    		}
    		tableIdData.get((int)s[i]).getListeVerrouClients().setListeClients(L);
    	}
	}	
    }
}

 
