package irc;

import jvn.JvnCoordImpl;

public class CoordIRC {
	public static void main (String args[]){
		try {
			JvnCoordImpl co = new JvnCoordImpl();
			System.out.println("Serveur lancé !!!");
		} catch (Exception e) {
			e.printStackTrace();
			//System.exit(0);
		}
	}
}
