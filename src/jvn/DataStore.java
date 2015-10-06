package jvn;

import java.io.Serializable;
import java.util.ArrayList;

public class DataStore {
	private Serializable objDistant;
	private VerrouListeClients listeVerrouClients;
	
	public DataStore(){
		objDistant= null;
		listeVerrouClients = new VerrouListeClients();
	}
	
	//on fait l'hypothès qu'à la création, l'objet créé n'est pas utilisé immédiatement par un client
	public DataStore(Serializable o ){
		objDistant= o;
		listeVerrouClients= new VerrouListeClients();
	}
	
	public Serializable getObjDistant(){
		return objDistant;
	}
	
	public void setObjDistant(Serializable o){
		objDistant= o;
	}
	
	public VerrouListeClients getListeVerrouClients(){
		return listeVerrouClients;
	}
	
	public void setListeVerrouClients (VerrouListeClients l){
		listeVerrouClients = l;
	}
	
	//TODO
}
