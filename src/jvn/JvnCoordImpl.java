/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import jvn.JvnRemoteCoord;
import jvn.DataStore;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.io.Serializable;



public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{

	//table de correspondance nom symbolique-identifiant objet distant
	private HashMap<String, Integer> tableNomId;
	//table de correspondance identifiant objet distant- DataStore correspondant
	private HashMap<Integer, DataStore> tableIdData;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		//création d'un RmiRegistery interne
		LocateRegistry.createRegistry(2015);
		
		//initialisation des structures de données
		tableNomId = new HashMap<>();
		tableIdData = new HashMap<>();
		
		//passage d'une référence du coordinateur dans le rmiRegistry sous le nom "coord"
		try{
			java.rmi.Naming.bind("coord", this);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
	  return Integer.parseInt(UUID.randomUUID().toString()); 
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	int joi= jvnGetObjectId();
	//on vérifie que l'objet est bien identifié dans la table 
	if (tableNomId.containsValue(joi)){
		//ajout du nouveau nom symbolique associé à la resource
		tableNomId.put(jon, joi);
		//comme le server JS n'a pas de verrou sur la resource, on ne l'enregistre pas dans tableIdData
		//un dataStore de la resource existe déjà, pas besoin de le modifier ou d'en créer un autre
	}else{ //créer une nouvelle entrée dans les deux tables: nouvelle resource
		tableNomId.put(jon, joi);
		//ajout d'un nouveau dataStore
		DataStore dt= new DataStore(jo);
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
			res=  (JvnObject)tableIdData.get(tableNomId.get(jon)).getObjDistant();
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
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	 //la resource existe-t-elle?
	 		if (tableIdData.containsKey(joi)){
	 			//quel verrou est apposé sur la resource?
	 			Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
	 			switch (v) {
	 			case NL:
	 				//verrou directement accordé
	 				VerrouListeClients l= tableIdData.get(joi).getListeVerrouClients();
	 				l.setVerrou(Verrou.R); //verrou en lecture
	 				ArrayList<JvnRemoteServer> list= new ArrayList<>();
	 				list.add(js);
	 				l.setListeClients(list);
	 				tableIdData.get(joi).setListeVerrouClients(l); //l'utilisateur est ajouté en tant que possésseur du verrou
	 				
	 				break;
	 			case R:
	 				//verrou compatible avec les autres verrous, mise à jour de la liste des clients ayant le verrou
	 				ArrayList<JvnRemoteServer> list= tableIdData.get(joi).getListeVerrouClients()
	 				//TODO
	 				break;
	 			case W:
	 				//verrou incompatible, on invalide le verrou actuel
	 				//TODO
	 				break;
	 			default:
	 				//TODO
	 				break;
	 			}

	 			//on retourne l'objet sérialisable
 				return (Serializable)tableIdData.get(joi).getObjDistant();
	 			
	 		}else{
	 			
	 		}
	 		
	 		return null;
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    return null;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 // to be completed
    }
}

 
