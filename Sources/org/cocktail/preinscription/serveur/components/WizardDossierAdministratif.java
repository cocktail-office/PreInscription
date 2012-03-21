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

package org.cocktail.preinscription.serveur.components;

import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.preinscription.serveur.controleurs.CtrlWizardDossierAdministratif;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.exception.ScolarixFwkException;
import org.cocktail.scolarix.serveur.interfaces.IEtudiant;
import org.cocktail.scolarix.serveur.ui.EOGarnucheCadreApplication;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponseRewriter;

public class WizardDossierAdministratif extends MyComponent {
	private static final long serialVersionUID = 1L;

	public CtrlWizardDossierAdministratif ctrl = null;
	public IEtudiant etudiant = null;
	public Integer anneeScolaire = null;
	private NSMutableDictionary<String, WOComponent> componentCache;
	// private NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation;
	private EOGarnucheCadreApplication unModule;

	public WizardDossierAdministratif(WOContext context) {
		super(context);
		ctrl = new CtrlWizardDossierAdministratif(this);
		componentCache = new NSMutableDictionary<String, WOComponent>();
		// modulesForNavigation = new NSMutableArray<EOGarnucheCadreApplication>();
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		ERXResponseRewriter.addScriptResourceInHead(response, context, null, "scripts/myprototype.js");
		ERXResponseRewriter.addScriptResourceInHead(response, context, null, "scripts/assistant.js");

		ERXResponseRewriter.addStylesheetResourceInHead(response, context, null, "styles/assistant.css");
	}

	public void setCheque(NSMutableDictionary<String, String> cheque) {

	}

	// Bindings a passer aux modules
	public void setEtudiant(IEtudiant etudiant) {
		this.etudiant = etudiant;

	}

	public IEtudiant getEtudiant() {
		return etudiant;
	}

	public void setAnneeScolaire(Integer anneeScolaire) {
		this.anneeScolaire = anneeScolaire;
	}

	public Integer anneeScolaire() {
		return anneeScolaire;
	}

	/**
	 * @return the componentCache
	 */
	public NSMutableDictionary<String, WOComponent> componentCache() {
		return componentCache;
	}

	/**
	 * @param componentCache
	 *            the componentCache to set
	 */
	public void setComponentCache(NSMutableDictionary<String, WOComponent> componentCache) {
		this.componentCache = componentCache;
	}

	/*	*//**
	 * @return the modules
	 */
	/*
	 * public NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation() { return modulesForNavigation; }
	 *//**
	 * @param modules
	 *            the modules to set
	 */
	/*
	 * public void setModulesForNavigation(NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation) { this.modulesForNavigation =
	 * modulesForNavigation; }
	 */

	public boolean isModuleForNavigationDisabled() {
		boolean isModuleForNavigationDisabled = true;
		if (ctrl.modules().indexOfObject(ctrl.unModuleForNavigation()) < session.indexModuleActif) {
			isModuleForNavigationDisabled = false;
		}
		return isModuleForNavigationDisabled;
	}

	public String styleModuleForNavigation() {
		String styleModuleForNavigation = "";
		if (ctrl.modules().indexOfObject(ctrl.unModuleForNavigation()) == session.indexModuleActif) {
			styleModuleForNavigation = "color:red;";
		}
		return styleModuleForNavigation;
	}

	public boolean isEnregistrerPossible() {
		if (etudiant != null && etudiant.isPreInscrit()) {
			return true;
		}
		if (session.indexModuleActif == (ctrl.modules().count() - 1)) {
			return true;
		}
		return false;
	}

	public WOActionResults enregistrer() {
		WOResponse response = new WOResponse();
		session.setErreur(null);
		try {
			IEtudiant etudiantTemp = null;
			etudiantTemp = etudiant.enregistrer(session.dataBus(), session.defaultEditingContext());
			if (etudiantTemp != null) {
				session.setEtudiant(etudiantTemp);
				// pdm juste pour voir...
				try {
					System.out.println("[SAVE " + (etudiantTemp.isPreInscription() ? "PreInsc" : "ReInsc") + session.codeRne + "] "
							+ DateCtrl.currentDateTimeString() + " - etudNumero = " + etudiantTemp.numero());
				}
				catch (Exception e) {
				}
				session.setIsApresEnregistrer(true);
			}
			else {
				CktlLog.rawLog("[WizardDossierAdministratif.java] " + DateCtrl.currentDateTimeString()
						+ " - Erreur innatendue lors de l'enregistrement du dossier:");
				throw new ScolarixFwkException("Une erreur indéterminée a été générée lors de l'enregistrement du dossier.");
			}
		}
		catch (EtudiantException e1) {
			response.setStatus(500);
			// e1.setMessage("Une erreur indéterminée a été générée lors de l'enregistrement du dossier.");
			String messageErreur = e1.getMessageFormatte();
			session.setObjectForKey(messageErreur, "MessageErreur");
			session.etudiant().numeroINE();
			// e1.printStackTrace();
		}
		catch (ScolarixFwkException e) {
			response.setStatus(500);
			String messageErreur = e.getMessageFormatte();
			if (messageErreur == null) {
				messageErreur = "Erreur non déterminée !";
			}
			session.setObjectForKey(messageErreur, "MessageErreur");
			// e.printStackTrace();
		}
		return response;
	}

	public WOComponent prendreRdv() {
		return null;
	}

	public WOComponent imprimer() {
		return null;
	}

	public WOComponent accueil() {
		Accueil page = (Accueil) session.getSavedPageWithName(Accueil.class.getName());
		session.removeObjectForKey("MessageErreur");
		page.setOnloadJS(null);

		return page;
	}

	public WOComponent quitter() {
		return session.logout();
	}

	public String styleRoll() {
		String styleRoll = "";

		int left = -820 * session.indexModuleActif;

		styleRoll = "margin:0px;padding:0px;top: 0px; position: relative; left:" + left + "px;";

		return styleRoll;
	}

	public String classBtnPrecedent() {
		String classBtnPrecedent = "btn_nav btn_nav_precedent ";
		if (session.indexModuleActif < 1) {
			classBtnPrecedent += "btn_nav_disabled";
		}
		return classBtnPrecedent;
	}

	public String classBtnSuivant() {
		String classBtnSuivant = "btn_nav btn_nav_suivant ";
		if (session.indexModuleActif == (ctrl.modules().count() - 1)) {
			classBtnSuivant += "btn_nav_disabled";
		}
		return classBtnSuivant;
	}

	public String containerModuleId() {
		String containerModuleId = "Container";
		containerModuleId += unModule().garnucheCadre().componentName();
		return containerModuleId;
	}

	public String divModuleName() {
		String divModuleName = unModule().garnucheCadre().componentName();
		return divModuleName;
	}

	public String divModuleNameStyle() {
		String divModuleNameStyle = "display:none;";
		int index = ctrl.modules().indexOfObject(unModule());

		if (index == session.indexModuleActif) {
			divModuleNameStyle = "height:490px;margin:0px;padding:0px;display:block;overflow:auto;";
		}
		return divModuleNameStyle;
	}

	// public boolean disabled() {
	// boolean disabled = true;
	// System.out.println("unModule() = " + unModule());
	// if (unModule() != null && unModule().cappModifiable().equalsIgnoreCase("O")) {
	// disabled = false;
	// }
	// return disabled;
	// }

	public String spanForNavigationId() {
		String spanForNavigationId = "spanForNavigation_" + ctrl.unModuleForNavigation().garnucheCadre().componentName();
		return spanForNavigationId;
	}

	public boolean isAfficherFlechesDeNavigation() {
		boolean isAfficherFlechesDeNavigation = false;
		if (ctrl.modules() != null && ctrl.modules().count() > 8) {
			isAfficherFlechesDeNavigation = true;
		}
		return isAfficherFlechesDeNavigation;
	}

	/**
	 * @return the unModule
	 */
	public EOGarnucheCadreApplication unModule() {
		return unModule;
	}

	/**
	 * @param unModule
	 *            the unModule to set
	 */
	public void setUnModule(EOGarnucheCadreApplication unModule) {
		this.unModule = unModule;
	}

}
