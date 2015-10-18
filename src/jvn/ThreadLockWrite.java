package jvn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Timer;

public class ThreadLockWrite extends Thread {
	private ConcurrentHashMap<Integer, DataStore> tableIdData;
	private int joi;
	private JvnRemoteServer js;
	
	public ThreadLockWrite( ConcurrentHashMap<Integer, DataStore> table, int i, JvnRemoteServer j){
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
		 		l.setVerrou(Verrou.W); //verrou en ecriture
		 		ArrayList<JvnRemoteServer> list= new ArrayList<>();
		 		list.add(js);
		 		l.setListeClients(list);
		 		tableIdData.get(joi).setListeVerrouClients(l); //l'utilisateur est ajout√© en tant que poss√©sseur du verrou
		 		
		 		break;
		 	case R:
		 		//verrou incompatible avec les autres verrous, on invalide le(s) verrou(s) actuel(s) et js devient seul poss√©sseur du verrou
		 		
		 		//invalidation des verrous des clients de la liste
		 		for (int i = 0; i < tableIdData.get(joi).getListeVerrouClients().getListeClients().size(); i++) {
		 			class taskperf implements ActionListener{
			 			Thread t;
			 			
			 			public taskperf(Thread t){
			 				this.t= t;
			 			}
			 			
						@SuppressWarnings("deprecation")
						@Override
						public void actionPerformed(ActionEvent arg0) {
							//le thread continue, on ignore le client
							//TODO probleme: potentiellement on boucle sur le invalidateReader du mÍme client
						}
			 			
			 		}			 		

			 		Timer t= new Timer(500, new taskperf(this));
			 		t.start();
		 			tableIdData.get(joi).getListeVerrouClients().getListeClients().get(i).jvnInvalidateReader(joi);
		 			t.stop();
				}
		 		//changement du verrou
		 		VerrouListeClients l2= tableIdData.get(joi).getListeVerrouClients();
		 		l2.setVerrou(Verrou.W);
		 		//js devient le seul poss√©sseur du verrou
		 		ArrayList<JvnRemoteServer> list2= new ArrayList<>();
		 		list2.add(js);
		 		l2.setListeClients(list2);
		 		tableIdData.get(joi).setListeVerrouClients(l2);
		 		break;
		 	case W:
		 		//verrou incompatible avec leverrou ctuel, js devient seul poss√©sseur du verrou
		 		
		 		//invalidation du verrou du client
		 		class taskperf implements ActionListener{
		 			Thread t;
		 			
		 			public taskperf(Thread t){
		 				this.t= t;
		 			}
		 			
					@SuppressWarnings("deprecation")
					@Override
					public void actionPerformed(ActionEvent arg0) {
						//effectuer le changement de propriÈtaire sans tenir compte de l'ancien
						//js devient le seul poss√©sseur du verrou
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
		 		Serializable o= tableIdData.get(joi).getListeVerrouClients().getListeClients().get(0).jvnInvalidateWriter(joi);
		 		t.stop();
		 		// mettre √† jour la derni√®re version de l'objet
		 		tableIdData.get(joi).setObjDistant(o);
		 		//js devient le seul poss√©sseur du verrou
		 		VerrouListeClients l3= tableIdData.get(joi).getListeVerrouClients();
		 		ArrayList<JvnRemoteServer> list3= new ArrayList<>();
		 		list3.add(js);
		 		l3.setListeClients(list3);
		 		tableIdData.get(joi).setListeVerrouClients(l3);
		 		break;
		 	default:
		 		break;
		 	}
		 	//fin du thread
		 	this.interrupt();
		} catch (RemoteException e) { // problËme de connexion dÈtectÈe lors de l'appel ‡ invalidate
			Verrou v= tableIdData.get(joi).getListeVerrouClients().getVerrou();
			if (v == Verrou.R){
				//tuer le thread
				System.out.println("fin de thread");
				this.interrupt();
			}else if (v == Verrou.W){
				//effectuer le changement de propriÈtaire sans tenir compte de l'ancien
				//js devient le seul poss√©sseur du verrou
		 		VerrouListeClients l3= tableIdData.get(joi).getListeVerrouClients();
		 		ArrayList<JvnRemoteServer> list3= new ArrayList<>();
		 		list3.add(js);
		 		l3.setListeClients(list3);
		 		tableIdData.get(joi).setListeVerrouClients(l3);
		 		
				//tuer le thread
				System.out.println("fin de thread");
				this.interrupt();
			}else{
				System.out.println("cas non prÈvu");
			}
			
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
