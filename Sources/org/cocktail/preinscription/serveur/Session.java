/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2012 This software
 * is governed by the CeCILL license under French law and abiding by the
 * rules of distribution of free software. You can use, modify and/or
 * redistribute the software under the terms of the CeCILL license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability. In this
 * respect, the user's attention is drawn to the risks associated with loading,
 * using, modifying and/or developing or reproducing the software by the user
 * in light of its specific status of free software, that may mean that it
 * is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security. The
 * fact that you are presently reading this means that you have had knowledge
 * of the CeCILL license and that you accept its terms.
 */

package org.cocktail.preinscription.serveur;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import org.cocktail.fwkcktlajaxwebext.serveur.CocktailAjaxSession;
import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.impression.CtrlImpression;
import org.cocktail.scolarix.serveur.interfaces.IEtudiant;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheApplication;
import org.cocktail.scolarix.serveur.metier.eos.EOPreCandidat;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

public class Session extends CocktailAjaxSession {
	private static final long serialVersionUID = 1L;

	// Message d'erreur a afficher via une alerte JS
	public String erreur;

	public IEtudiant etudiant;

	public EOGarnucheApplication garnucheApplication;

	private CtrlImpression ctrlImpression;
	/**
	 * Annee scolaire
	 */
	public Integer anneeScolaire;
	public String anneeScolaireAAfficher;

	// Index du module affiche afin de pouvoir bien se repositionner lors d'un
	// "refresh" du navigateur
	public int indexModuleActif;

	private Class finder;
	private Class interfaceEtudiant;
	public String codeRne;
	private boolean isApresEnregistrer;
	private boolean _editingContextWasCreated;

	public Session() {
		super();
		CktlLog.rawLog("[Session.java] " + DateCtrl.currentDateTimeString() + " - New " + sessionID() + "(" + timeOut() + ") ");
		// CktlLog.rawLog("[Session.java] "+DateCtrl.currentDateTimeString()+" - Edc "+defaultEditingContext());
	}

	public Session(String sessionID) {
		super();
		CktlLog.rawLog("[Session.java] " + DateCtrl.currentDateTimeString() + " - New " + sessionID + "(" + timeOut() + ") ");
		// CktlLog.rawLog("[Session.java] "+DateCtrl.currentDateTimeString()+" - Edc "+defaultEditingContext());
	}

	public void initSession() {
		reset();
		// ERXEC.setDefaultFetchTimestampLag(2000);
		// setDefaultEditingContext(ERXEC.newEditingContext());
		super.initSession();
	}

	public void reset() {
		setErreur(null);
		setEtudiant(null);
		setGarnucheApplication(null);
		setCtrlImpression(null);
		setAnneeScolaire(null);
		setAnneeScolaireAAfficher(null);
		setIndexModuleActif(0);
		setFinder(null);
		setInterfaceEtudiant(null);
		setCodeRne(null);
		setIsApresEnregistrer(false);
		_editingContextWasCreated = false;
	}

	/**
	 * Ensures that the returned editingContext was created with the {@link ERXEC} factory.
	 * 
	 * @return the session's default editing context with the default delegate set.
	 */
	public EOEditingContext defaultEditingContext() {
		/*
		 * if (!_editingContextWasCreated) { setDefaultEditingContext(ERXEC.newEditingContext()); _editingContextWasCreated = true; }
		 */
		return super.defaultEditingContext();
	}

	public void setDefaultEditingContext(EOEditingContext ec) {
		// _editingContextWasCreated = true;
		super.setDefaultEditingContext(ec);
	}

	public void terminate() {
		NSMutableDictionary<String, String> dicoSessionIDNumeroEtudiant = Application.dicoSessionIDNumeroEtudiant();
		if (dicoSessionIDNumeroEtudiant != null && dicoSessionIDNumeroEtudiant.containsValue(sessionID())) {
			String numeroEtudiant = null;
			Enumeration<String> enumDicoSessionIDNumeroEtudiant = dicoSessionIDNumeroEtudiant.keyEnumerator();
			while (enumDicoSessionIDNumeroEtudiant.hasMoreElements()) {
				numeroEtudiant = enumDicoSessionIDNumeroEtudiant.nextElement();
				if (sessionID().equals(dicoSessionIDNumeroEtudiant.valueForKey(numeroEtudiant))) {
					break;
				}
			}
			if (numeroEtudiant != null) {
				CktlLog.rawLog("[Session.java] " + DateCtrl.currentDateTimeString() + " - terminate() Suppression d l'etudiant numero "
						+ numeroEtudiant + " dans le dico de l'application");
				dicoSessionIDNumeroEtudiant.removeObjectForKey(numeroEtudiant);
			}
		}
		if (etudiant() != null) {
			CktlLog.rawLog("[Session.java] " + DateCtrl.currentDateTimeString() + " - terminate(" + sessionID() + ") Etudiant numero "
					+ etudiant().numero() + "(" + etudiant().numeroINE() + ")");
		}
		else {
			CktlLog.rawLog("[Session.java] " + DateCtrl.currentDateTimeString() + " - terminate(" + sessionID() + ")");
		}

		super.terminate();
	}

	public IEtudiant getEtudiant(EOPreCandidat preCandidat, String codeRne) throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {
		Method m = finder().getDeclaredMethod("getEtudiantPreCandidat",
				new Class[] { EOEditingContext.class, EOPreCandidat.class, String.class, interfaceEtudiant() });
		IEtudiant etudiant = (IEtudiant) m.invoke(null, new Object[] { defaultEditingContext(), preCandidat, codeRne, null });
		if (etudiant != null && etudiant.userInfos() != null && etudiant.userInfos().count() > 0) {
			throw (EtudiantException) etudiant.userInfos().objectAtIndex(0);
		}
		return etudiant;
	}

	public Integer anneeScolaire() {
		return anneeScolaire;
	}

	public void setAnneeScolaire(Integer anneeScolaire) {
		this.anneeScolaire = anneeScolaire;
		if (anneeScolaire != null) {
			// Calcul de anneeScolaire a afficher
			this.anneeScolaireAAfficher = String.valueOf(anneeScolaire);
			if (Application.isAnneeCivile == false) {
				this.anneeScolaireAAfficher += " / " + String.valueOf(anneeScolaire.intValue() + 1);
			}
			// Mise a jour du ctrl d'impression
			ctrlImpression().setAnnee(anneeScolaire);
		}
		else {
			this.anneeScolaireAAfficher = "";
		}
	}

	public void setErreur(String erreur) {
		this.erreur = erreur;
	}

	public String erreur() {
		return erreur;
	}

	public IEtudiant etudiant() {
		return etudiant;
	}

	public void setEtudiant(IEtudiant etudiant) {
		this.etudiant = etudiant;
		if (etudiant != null && etudiant.numero() != null) {
			// Mise a jour du ctrl d'impression
			ctrlImpression().setEtudiant(etudiant);
		}
	}

	public EOGarnucheApplication garnucheApplication() {
		return garnucheApplication;
	}

	public void setGarnucheApplication(EOGarnucheApplication garnucheApp) {
		this.garnucheApplication = garnucheApp;
		if (garnucheApplication != null) {
			// Mise a jour du ctrl d'impression
			ctrlImpression().setGarnucheApp(garnucheApp);
		}
	}

	public CtrlImpression ctrlImpression() {
		if (ctrlImpression == null) {
			ctrlImpression = new CtrlImpression(etudiant(), anneeScolaire(), garnucheApplication());
		}
		return ctrlImpression;
	}

	/**
	 * Recherche d'une classe spécifique si elle existe, sinon renvoie la classe générale. On recherche du niveau le plus fin jusqu'au plus
	 * général, jusqu'à trouver une classe... Si aucune classe spécifique n'est trouvée, on renvoie la classe générale.<br>
	 * La logique de recherche dans l'ordre (stoppe dès qu'une classe est trouvée) :<br>
	 * - Si le paramètre SUFFIXE_SPECIFICITE est défini :<br>
	 * . Recherche d'une classe avec suffixeSpécificité + codePays + codeRne<br>
	 * . Si non trouvé, recherche d'une classe avec suffixeSpécificité + codePays<br>
	 * . Si non trouvé, recherche d'une classe avec suffixeSpécificité<br>
	 * - Si le paramètre SUFFIXE_SPECIFICITE n'est pas défini ou aucune classe trouvée avec :<br>
	 * . Recherche d'une classe avec codePays + codeRne<br>
	 * . Si non trouvé, recherche d'une classe avec codePays<br>
	 * . Si non trouvé, recherche d'une classe avec codeRne<br>
	 * . Si non trouvé, recherche de la classe générale (doit toujours exister, sinon l'application s'arrête)<br>
	 * 
	 * @param className
	 *            Le nom complet de la classe à chercher (package inclus)
	 * @param cRne
	 *            L'établissement pour lequel on recherche une spécificité éventuelle
	 * @return La classe qui va bien... Doit forcément retourner une classe, ou bien l'application s'arrête...
	 */
	private Class getGoodClass(String className, String cRne) {
		Class goodClass = null;
		String suffixeSpecificite = cktlApp.config().stringForKey("SUFFIXE_SPECIFICITE");
		String codePays = cktlApp.config().stringForKey("GRHUM_C_PAYS_DEFAUT");
		if (StringCtrl.isEmpty(suffixeSpecificite) == false) {
			if (cRne != null) {
				try {
					System.out.print("Looking for " + className + suffixeSpecificite + codePays + cRne + "... ");
					goodClass = Class.forName(className + suffixeSpecificite + codePays + cRne);
					System.out.println("YES!");
					return goodClass;
				}
				catch (ClassNotFoundException e1) {
					System.out.println("NO...");
				}
			}
			try {
				System.out.print("Looking for " + className + suffixeSpecificite + codePays + "... ");
				goodClass = Class.forName(className + suffixeSpecificite + codePays);
				System.out.println("YES!");
				return goodClass;
			}
			catch (ClassNotFoundException e2) {
				System.out.println("NO...");
			}
			try {
				System.out.print("Looking for " + className + suffixeSpecificite + "... ");
				goodClass = Class.forName(className + suffixeSpecificite);
				System.out.println("YES!");
				return goodClass;
			}
			catch (ClassNotFoundException e3) {
				System.out.println("NO...");
			}
		}
		if (cRne != null) {
			try {
				System.out.print("Looking for " + className + codePays + cRne + "... ");
				goodClass = Class.forName(className + codePays + cRne);
				System.out.println("YES!");
				return goodClass;
			}
			catch (ClassNotFoundException e4) {
				System.out.println("NO...");
			}
		}
		try {
			System.out.print("Looking for " + className + codePays + "... ");
			goodClass = Class.forName(className + codePays);
			System.out.println("YES!");
			return goodClass;
		}
		catch (ClassNotFoundException e5) {
			System.out.println("NO...");
		}
		if (cRne != null) {
			try {
				System.out.print("Looking for " + className + cRne + "... ");
				goodClass = Class.forName(className + cRne);
				System.out.println("YES!");
				return goodClass;
			}
			catch (ClassNotFoundException e6) {
				System.out.println("NO...");
			}
		}
		try {
			System.out.print("Looking for " + className + "... ");
			goodClass = Class.forName(className);
			System.out.println("YES!");
			return goodClass;
		}
		catch (ClassNotFoundException e7) {
			System.out.println("NO...");
			e7.printStackTrace();
			System.out.println("Required class " + className + " not found, exiting !");
			System.exit(-1);
		}
		return null;
	}

	public void setCodeRne(String codeRne) {
		this.codeRne = codeRne;
		setFinder(getGoodClass("org.cocktail.scolarix.serveur.finder.FinderEtudiant", codeRne));
		setInterfaceEtudiant(getGoodClass("org.cocktail.scolarix.serveur.interfaces.IEtudiant", codeRne));
	}

	public String codeRne() {
		return codeRne;
	}

	public Class finder() {
		return finder;
	}

	public void setFinder(Class finder) {
		this.finder = finder;
	}

	public Class interfaceEtudiant() {
		return interfaceEtudiant;
	}

	public void setInterfaceEtudiant(Class interfaceEtudiant) {
		this.interfaceEtudiant = interfaceEtudiant;
	}

	public boolean isApresEnregistrer() {
		return isApresEnregistrer;
	}

	public void setIsApresEnregistrer(boolean isApresEnregistrer) {
		this.isApresEnregistrer = isApresEnregistrer;
	}

	public CtrlImpression getCtrlImpression() {
		return ctrlImpression;
	}

	public void setCtrlImpression(CtrlImpression ctrlImpression) {
		this.ctrlImpression = ctrlImpression;
	}

	public String getAnneeScolaireAAfficher() {
		return anneeScolaireAAfficher;
	}

	public void setAnneeScolaireAAfficher(String anneeScolaireAAfficher) {
		this.anneeScolaireAAfficher = anneeScolaireAAfficher;
	}

	public int getIndexModuleActif() {
		return indexModuleActif;
	}

	public void setIndexModuleActif(int indexModuleActif) {
		this.indexModuleActif = indexModuleActif;
	}

}
