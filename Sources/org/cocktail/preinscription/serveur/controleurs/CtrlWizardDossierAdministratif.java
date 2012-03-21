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
package org.cocktail.preinscription.serveur.controleurs;

import java.util.Enumeration;

import org.cocktail.preinscription.serveur.components.WizardDossierAdministratif;
import org.cocktail.scolarix.serveur.components.modules.ModuleDossierAdministratif;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.ui.EOGarnucheCadreApplication;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSRange;

public class CtrlWizardDossierAdministratif {
	private WizardDossierAdministratif wocomponent = null;

	private NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation;
	private EOGarnucheCadreApplication unModuleForNavigation;
	private NSArray<EOGarnucheCadreApplication> modules;

	private boolean modificationEnCours = false;
	private int indexOfFirstNavigationModuleToDisplay = -1;

	public CtrlWizardDossierAdministratif(WizardDossierAdministratif component) {
		wocomponent = component;
		modulesForNavigation = new NSMutableArray<EOGarnucheCadreApplication>();
	}

	public NSArray<EOGarnucheCadreApplication> modulesAAfficher() {
		NSArray<EOGarnucheCadreApplication> modulesAAfficher = null;
		if (modules() != null) {
			int location = 0;
			int actif = wocomponent.session.indexModuleActif;
			if (actif > -1) {
				if (indexOfFirstNavigationModuleToDisplay == -1) {
					if (actif >= 6) {
						location = actif - 6;
					}
					else {
						location = 0;
					}
				}
				else {
					location = indexOfFirstNavigationModuleToDisplay;
				}
			}
			NSRange range = new NSRange(location, 7);
			modulesAAfficher = modules().subarrayWithRange(range);
		}
		return modulesAAfficher;
	}

	public void afficherPremierModuleDansBarreDeNavigation() {
		indexOfFirstNavigationModuleToDisplay = 0;
	}

	public void afficherDernierModuleDansBarreDeNavigation() {
		indexOfFirstNavigationModuleToDisplay = modules().count() - 7;
	}

	public void afficherLeModule() {
		int index = modulesForNavigation.indexOf(unModuleForNavigation());
		NSRange range = new NSRange(index, modulesForNavigation.count() - index);
		modulesForNavigation.removeObjectsInRange(range);
		wocomponent.session.indexModuleActif = index;
	}

	public void precedent() {
		// System.out.println("XXXXXXXXXXXXXXXXXX precedent()");
		if (!modificationEnCours) {
			// System.out.println("XXXXXXXXXXXXXXXXXX precedent1");
			if (wocomponent.session.indexModuleActif > 0) {
				wocomponent.session.indexModuleActif--;
				modulesForNavigation.removeLastObject();
				indexOfFirstNavigationModuleToDisplay = -1;
			}
		}
		// System.out.println("XXXXXXXXXXXXXXXXXX precedent2");
	}

	public WOActionResults suivant() {
		WOResponse response = new WOResponse();
		if (!modificationEnCours) {
			EOGarnucheCadreApplication module = modules().objectAtIndex(wocomponent.session.indexModuleActif);
			ModuleDossierAdministratif womodule = (ModuleDossierAdministratif) wocomponent.componentCache().objectForKey(
					module.garnucheCadre().componentName());
			if (womodule != null) {
				try {
					womodule.validate();

					if (wocomponent.session.indexModuleActif < modules().count() - 1) {
						wocomponent.session.indexModuleActif++;
						modulesForNavigation.addObject(module);
						indexOfFirstNavigationModuleToDisplay = -1;
					}
				}
				catch (EtudiantException e) {
					String messageErreur = e.getMessageFormatte();
					wocomponent.session.setObjectForKey(messageErreur, "MessageErreur");
					response.setStatus(500);
				}
			}
		}
		return response;
	}

	public String onCompleteAfficherLeModule() {
		String onCompletePrecedent = "function (oC){var modules=[";
		NSArray<EOGarnucheCadreApplication> modules = modules();
		Enumeration<EOGarnucheCadreApplication> enumModules = modules.objectEnumerator();
		while (enumModules.hasMoreElements()) {
			EOGarnucheCadreApplication module = enumModules.nextElement();
			String moduleName = module.garnucheCadre().componentName();
			onCompletePrecedent += "'" + moduleName + "', ";
		}
		if (modules != null && modules.count() > 1) {
			onCompletePrecedent = onCompletePrecedent.substring(0, onCompletePrecedent.length() - 2);
		}
		int index = modulesForNavigation.indexOf(unModuleForNavigation());
		int times = modulesForNavigation.count() - index;
		onCompletePrecedent += "];precedent(modules,$('btn_precedent')," + times + ");}";
		wocomponent.session.removeObjectForKey("MessageErreur");

		return onCompletePrecedent;
	}

	public String onSuccessPrecedent() {
		String onSuccessPrecedent = "function (oS){var modules=[";
		NSArray<EOGarnucheCadreApplication> modules = modules();
		Enumeration<EOGarnucheCadreApplication> enumModules = modules.objectEnumerator();
		while (enumModules.hasMoreElements()) {
			EOGarnucheCadreApplication module = enumModules.nextElement();
			String moduleName = module.garnucheCadre().componentName();
			onSuccessPrecedent += "'" + moduleName + "', ";
		}
		if (modules != null && modules.count() > 1) {
			onSuccessPrecedent = onSuccessPrecedent.substring(0, onSuccessPrecedent.length() - 2);
		}
		onSuccessPrecedent += "];precedent(modules,$('btn_precedent'),1);}";
		wocomponent.session.removeObjectForKey("MessageErreur");

		return onSuccessPrecedent;
	}

	public String onSuccessSuivant() {
		String onSuccessSuivant = "function (oS){var modules=[";
		NSArray<EOGarnucheCadreApplication> modules = modules();
		Enumeration<EOGarnucheCadreApplication> enumModules = modules.objectEnumerator();
		while (enumModules.hasMoreElements()) {
			EOGarnucheCadreApplication module = enumModules.nextElement();
			String moduleName = module.garnucheCadre().componentName();
			onSuccessSuivant += "'" + moduleName + "', ";
		}
		if (modules != null && modules.count() > 1) {
			onSuccessSuivant = onSuccessSuivant.substring(0, onSuccessSuivant.length() - 2);
		}
		onSuccessSuivant += "];suivant(modules,$('btn_suivant'));}";
		wocomponent.session.removeObjectForKey("MessageErreur");

		return onSuccessSuivant;
	}

	/**
	 * @return the modules
	 */
	public NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation() {
		return modulesForNavigation;
	}

	/**
	 * @param modules
	 *            the modules to set
	 */
	public void setModulesForNavigation(NSMutableArray<EOGarnucheCadreApplication> modulesForNavigation) {
		this.modulesForNavigation = modulesForNavigation;
	}

	/**
	 * @return the unModule
	 */
	public EOGarnucheCadreApplication unModuleForNavigation() {
		return unModuleForNavigation;
	}

	/**
	 * @param unModule
	 *            the unModule to set
	 */
	public void setUnModuleForNavigation(EOGarnucheCadreApplication unModule) {
		this.unModuleForNavigation = unModule;
	}

	/**
	 * @return the modules
	 */
	public NSArray<EOGarnucheCadreApplication> modules() {
		return modules;
	}

	/**
	 * @param modules
	 *            the modules to set
	 */
	public void setModules(NSArray<EOGarnucheCadreApplication> modules) {
		this.modules = modules;
	}

}
