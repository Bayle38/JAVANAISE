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
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.io.Serializable;
import java.lang.invoke.MethodHandles.Lookup;



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
	public JvnCoordImpl() throws Exception {
		//création d'un RmiRegistery interne
		Registry reg = LocateRegistry.createRegistry(2015);
		
		//initialisation des structures de données
		tableNomId = new HashMap<>();
		tableIdData = new HashMap<>();
		
		//passage d'une référence du coordinateur dans le rmiRegistry sous le nom "coord"
		try{
//			java.rmi.Naming.bind("coord", this);
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
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
	  int id = Integer.parseInt(UUID.randomUUID().toString());
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
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	int joi= jo.jvnGetObjectId();
	//on vérifie qu'un autre objet n'est pas déjà identifié dans la table avec le même nom
	if (tableNomId.containsKey(jon)){
		throw new JvnException();
	}else{ //créer une nouvelle entrée dans les deux tables
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
   public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
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
	 		ArrayList<JvnRemoteServer> liste= (tableIdData.get(joi).getListeVerrouClients().getListeClients());
	 		liste.add(js);
	 		tableIdData.get(joi).getListeVerrouClients().setListeClients(liste);
	 		break;
	 	case W:
	 		//verrou incompatible, on invalide le verrou actuel (qui passe en lecture chez le posseseur actuel du verrou)
	 		Serializable o= tableIdData.get(joi).getListeVerrouClients().getListeClients().get(0).jvnInvalidateWriterForReader(joi);
	 		// mettre à jour la dernière version de l'objet
	 		tableIdData.get(joi).setObjDistant(o);
	 		//on change le verrou en lecture
	 		VerrouListeClients vl= tableIdData.get(joi).getListeVerrouClients();
	 		vl.setVerrou(Verrou.R);
	 		//on ajoute le nouveau client à la liste des possésseurs du verrou
	 		ArrayList<JvnRemoteServer> ll= vl.getListeClients();
	 		ll.add(js);
	 		vl.setListeClients(ll);
	 		tableIdData.get(joi).setListeVerrouClients(vl);
	 		break;
	 	default:
	 		//throw new JvnException("unhandled case");
	 		break;
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
		 	//quel verrou est apposé sur la resource?
		 	Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
		 	switch (v) {
		 	case NL:
		 		//verrou directement accordé
		 		VerrouListeClients l= tableIdData.get(joi).getListeVerrouClients();
		 		l.setVerrou(Verrou.W); //verrou en ecriture
		 		ArrayList<JvnRemoteServer> list= new ArrayList<>();
		 		list.add(js);
		 		l.setListeClients(list);
		 		tableIdData.get(joi).setListeVerrouClients(l); //l'utilisateur est ajouté en tant que possésseur du verrou
		 		
		 		break;
		 	case R:
		 		//verrou incompatible avec les autres verrous, on invalide le(s) verrou(s) actuel(s) et js devient seul possésseur du verrou
		 		
		 		//invalidation des verrous des clients de la liste
		 		for (int i = 0; i < tableIdData.get(joi).getListeVerrouClients().getListeClients().size(); i++) {
		 			tableIdData.get(joi).getListeVerrouClients().getListeClients().get(0).jvnInvalidateReader(i);
				}
		 		//changement du verrou
		 		VerrouListeClients l2= tableIdData.get(joi).getListeVerrouClients();
		 		l2.setVerrou(Verrou.W);
		 		//js devient le seul possésseur du verrou
		 		ArrayList<JvnRemoteServer> list2= new ArrayList<>();
		 		list2.add(js);
		 		l2.setListeClients(list2);
		 		tableIdData.get(joi).setListeVerrouClients(l2);
		 		break;
		 	case W:
//verrou incompatible avec les autres verrous, on invalide le(s) verrou(s) actuel(s) et js devient seul possésseur du verrou
		 		
		 		//invalidation du verrou du client
		 		Serializable o= tableIdData.get(joi).getListeVerrouClients().getListeClients().get(0).jvnInvalidateWriter(0);
		 		// mettre à jour la dernière version de l'objet
		 		tableIdData.get(joi).setObjDistant(o);
		 		//js devient le seul possésseur du verrou
		 		VerrouListeClients l3= tableIdData.get(joi).getListeVerrouClients();
		 		ArrayList<JvnRemoteServer> list3= new ArrayList<>();
		 		list3.add(js);
		 		l3.setListeClients(list3);
		 		tableIdData.get(joi).setListeVerrouClients(l3);
		 		break;
		 	default:
		 		//throw new JvnException("unhandled case");
		 		break;
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

 
