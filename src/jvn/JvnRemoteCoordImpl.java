package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JvnRemoteCoordImpl implements JvnRemoteCoord {
	//table de correspondance nom symbolique-identifiant objet distant
	private HashMap<String, Integer> tableNomId;
	//table de correspondance identifiant objet distant- DataStore correspondant
	private HashMap<Integer, DataStore> tableIdData;
	
	
	public JvnRemoteCoordImpl() throws RemoteException{
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

	//pour déterminer un numéro unique on le créé grâce à la classe UUID
	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		return Integer.parseInt(UUID.randomUUID().toString());
	}

	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, int joi, JvnRemoteServer js)
			throws RemoteException, JvnException {
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

	@Override
	//a t'on vraiment besoin de la ref' vers le remoteServer? 
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
			throws RemoteException, JvnException {
		JvnObject res= new JvnObject();
		if(tableNomId.containsKey(jon)){
			res=  (JvnObject)tableIdData.get(tableNomId.get(jon)).getObjDistant();
		}
		return res;
	}

	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js)
			throws RemoteException, JvnException {
		//la resource existe-t-elle?
		if (tableIdData.containsKey(joi)){
			//quel verrou est apposé sur la resource?
			Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
			switch (v) {
			case NL:
				//verrou directement accordé
				VerrouListeClients l= tableIdData.get(joi).getListeVerrouClients();
				l.setVerrou(Verrou.R);
				l.setListeClients(new ArrayList<>(js));
				//TODO
				break;
			case R:
				//verrou compatible avec les uatres verrous
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
			//quel(s) client(s) possède(nt) un verrou sur la resource?
			
		}else{
			
		}
		
		return null;
	}

	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
			throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException,
			JvnException {
		// TODO Auto-generated method stub

	}

}
