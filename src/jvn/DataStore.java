package jvn;

import java.util.ArrayList;

public class DataStore {
	private Object objDistant;
	private VerrouListeClients listeVerrouClients;
	
	public DataStore(){
		objDistant= new Object();
		listeVerrouClients = new VerrouListeClients();
	}
	
	//on fait l'hypothès qu'à la création, l'objet créé n'est pas utilisé immédiatement par un client
	public DataStore(Object o ){
		objDistant= o;
		listeVerrouClients= new VerrouListeClients();
	}
	
	public Object getObjDistant(){
		return objDistant;
	}
	
	public void setObjDistant(Object o){
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
