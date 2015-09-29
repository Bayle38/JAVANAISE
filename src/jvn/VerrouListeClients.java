package jvn;

import java.util.ArrayList;

public class VerrouListeClients {
	private Verrou verrou;
	private ArrayList<JvnRemoteServer> listeClients;
	
	public VerrouListeClients(){
		verrou= Verrou.NL;
		listeClients = new ArrayList<JvnRemoteServer>();
	}
	
	public VerrouListeClients(Verrou v, ArrayList<JvnRemoteServer> l){
		verrou= v;
		listeClients = l;
	} 
	
	public VerrouListeClients(ArrayList<JvnRemoteServer> l){
		verrou= Verrou.NL;
		listeClients = l;
	}
	
	public Verrou getVerrou(){
		return verrou;
	}
	
	public void setVerrou(Verrou v){
		verrou=  v;
	}
	
	public ArrayList<JvnRemoteServer> getListeClients(){
		return listeClients;
	}
	
	public void setListeClients(ArrayList<JvnRemoteServer> l){
		listeClients= l;
	}
}
