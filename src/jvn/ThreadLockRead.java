package jvn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Timer;

public class ThreadLockRead extends Thread {
	private ConcurrentHashMap<Integer, DataStore> tableIdData;
	private int joi;
	private JvnRemoteServer js;
	
	public ThreadLockRead( ConcurrentHashMap<Integer, DataStore> table, int i, JvnRemoteServer j){
		tableIdData= table;
		joi= i;
		js= j;
	}
	
	public void run() {
		try {
			//quel verrou est appos√© sur la resource?
		 	Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
		 	switch (v) {
		 	case NL:
		 		//verrou directement accord√©
		 		VerrouListeClients l= tableIdData.get(joi).getListeVerrouClients();
		 		l.setVerrou(Verrou.R); //verrou en lecture
		 		ArrayList<JvnRemoteServer> list= new ArrayList<>();
		 		list.add(js);
		 		l.setListeClients(list);
		 		tableIdData.get(joi).setListeVerrouClients(l); //l'utilisateur est ajout√© en tant que poss√©sseur du verrou
		 		
		 		break;
		 	case R:
		 		//verrou compatible avec les autres verrous, mise √† jour de la liste des clients ayant le verrou
		 		ArrayList<JvnRemoteServer> liste= (tableIdData.get(joi).getListeVerrouClients().getListeClients());
		 		liste.add(js);
		 		tableIdData.get(joi).getListeVerrouClients().setListeClients(liste);
		 		break;
		 	case W:
		 		//verrou incompatible, on invalide le verrou actuel (qui passe en lecture chez le posseseur actuel du verrou)
		 		Serializable o;
		 		class taskperf implements ActionListener{
		 			Thread t;
		 			
		 			public taskperf(Thread t){
		 				this.t= t;
		 			}
		 			
					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//effectuer le changement de propriÈtaire sans tenir compte de l'ancien
						//js devient le seul poss√©sseur du verrou comme l'ancien ne rÈpond plus
				 		VerrouListeClients l3= tableIdData.get(joi).getListeVerrouClients();
				 		ArrayList<JvnRemoteServer> list3= new ArrayList<>();
				 		list3.add(js);
				 		l3.setListeClients(list3);
				 		tableIdData.get(joi).setListeVerrouClients(l3);
				 		
						//tuer le thread
						System.out.println("fin de thread");
						t.interrupt();
					}
		 			
		 		}
		 		

		 		Timer t= new Timer(500, new taskperf(this));
		 		t.start();
				o = tableIdData.get(joi).getListeVerrouClients().getListeClients().get(0).jvnInvalidateWriterForReader(joi);
				t.stop();
		 		// mettre √† jour la derni√®re version de l'objet
		 		tableIdData.get(joi).setObjDistant(o);
		 		//on change le verrou en lecture
		 		VerrouListeClients vl= tableIdData.get(joi).getListeVerrouClients();
		 		vl.setVerrou(Verrou.R);
		 		//on ajoute le nouveau client √† la liste des poss√©sseurs du verrou
		 		ArrayList<JvnRemoteServer> ll= vl.getListeClients();
		 		ll.add(js);
		 		vl.setListeClients(ll);
		 		tableIdData.get(joi).setListeVerrouClients(vl);	
		 		break;
		 	default:
		 		//throw new JvnException("unhandled case");
		 		break;
		 	} 	
		 	//fin du thread
		 	this.interrupt();
		} catch (RemoteException e) {// problËme de connexion dÈtectÈe lors de l'appel ‡ invalidateWriterForReader
			Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
			//effectuer le changement de propriÈtaire sans tenir compte de l'ancien
			//js devient le seul poss√©sseur du verrou comme l'ancien ne rÈpond plus
	 		VerrouListeClients l3= tableIdData.get(joi).getListeVerrouClients();
	 		ArrayList<JvnRemoteServer> list3= new ArrayList<>();
	 		list3.add(js);
	 		l3.setListeClients(list3);
	 		tableIdData.get(joi).setListeVerrouClients(l3);
	 		
			//tuer le thread
			System.out.println("fin de thread");
			this.interrupt();
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

}
