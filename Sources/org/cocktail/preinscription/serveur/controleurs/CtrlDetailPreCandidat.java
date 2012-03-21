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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import org.cocktail.preinscription.serveur.components.DetailPreCandidat;
import org.cocktail.preinscription.serveur.components.exceptions.CtrlDetailPreCandidatException;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.finder.FinderGarnucheApplication;
import org.cocktail.scolarix.serveur.finder.FinderGarnucheCadreApplication;
import org.cocktail.scolarix.serveur.finder.FinderPreCandidat;
import org.cocktail.scolarix.serveur.interfaces.IEtudiant;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheApplication;
import org.cocktail.scolarix.serveur.metier.eos.EOPreCandidat;
import org.cocktail.scolarix.serveur.ui.EOGarnucheCadreApplication;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

public class CtrlDetailPreCandidat {
	private DetailPreCandidat component = null;
	private EOEditingContext edc = null;

	private NSArray<EOPreCandidat> preCandidats = null;
	public EOPreCandidat unPreCandidat;

	public EOPreCandidat lePreCandidat;

	public NSArray<EOGarnucheCadreApplication> cadres = null;
	public EOGarnucheCadreApplication unCadre;
	public EOGarnucheCadreApplication unCadreSelectionne;

	public CtrlDetailPreCandidat(DetailPreCandidat component) {
		super();
		this.component = component;
		if (component != null) {
			edc = component.context().session().defaultEditingContext();
		}
	}

	public WOActionResults rechercherLesPreCandidats() {
		WOResponse response = null;
		try {
			if (component.qbe().allKeys().count() > 0) {
				EOSortOrdering nomOrdering = EOSortOrdering.sortOrderingWithKey(EOPreCandidat.CAND_NOM_KEY, EOSortOrdering.CompareAscending);
				NSArray<EOSortOrdering> sortOrderings = new NSArray<EOSortOrdering>(nomOrdering);
				preCandidats = FinderPreCandidat.getPreCandidats(edc, component.qbe(), sortOrderings);
				if (preCandidats == null || preCandidats.count() == 0) {
					throw new CtrlDetailPreCandidatException("Aucun pré-candidat ne correspond à la recherche");
				}
			}
			else {
				throw new CtrlDetailPreCandidatException("Vous devez renseigner au moins une valeur.");
			}

			setLePreCandidat(null);
		}
		catch (CtrlDetailPreCandidatException e) {
			response = new WOResponse();
			response.setStatus(500);
			component.session().setObjectForKey(e.getMessageJS(), "MessageErreur");
		}

		return response;
	}

	public WOActionResults annulerLaRecherche() {
		reset();
		component.qbe().removeAllObjects();
		return null;
	}

	public void reset() {
		component.session.etudiant = null;
		cadres = null;
		unCadre = null;
		unCadreSelectionne = null;
		lePreCandidat = null;
		unPreCandidat = null;
		preCandidats = null;
	}

	public WOComponent afficherLeDossier() {

		if (unPreCandidat() != null) {
			EOEditingContext edc = component.session.defaultEditingContext();
			EOPreCandidat preCandidat = unPreCandidat();
			component.session.removeObjectForKey("MessageErreur");
			setLePreCandidat(preCandidat);
			component.session.setCodeRne(lePreCandidat().toRne().cRne());
			IEtudiant etudiant = null;
			try {
				edc.invalidateAllObjects();

				Method m = component.session.finder().getDeclaredMethod("getEtudiantPreCandidat",
						new Class[] { EOEditingContext.class, EOPreCandidat.class, String.class, component.session.interfaceEtudiant() });
				etudiant = (IEtudiant) m.invoke(null, new Object[] { edc, preCandidat, null, null });
			}
			catch (SecurityException e1) {
				e1.printStackTrace();
			}
			catch (NoSuchMethodException e2) {
				e2.printStackTrace();
			}
			catch (IllegalArgumentException e3) {
				e3.printStackTrace();
			}
			catch (IllegalAccessException e4) {
				e4.printStackTrace();
			}
			catch (EtudiantException e6) {
				e6.printStackTrace();
			}
			catch (InvocationTargetException e5) {
				if (e5.getCause().getClass().equals(EtudiantException.class)) {
					EtudiantException exception = (EtudiantException) e5.getCause();
					component.session.setObjectForKey(exception.getMessageJS(), "MessageErreur");
					setLePreCandidat(null);
				}
			}
			if (etudiant != null) {
				component.session.setEtudiant(etudiant);
				NSArray<EtudiantException> userInfos = etudiant.userInfos();
				if (userInfos != null && userInfos.count() > 0) {
					String messages = "";
					Enumeration<EtudiantException> enumUserInfos = userInfos.objectEnumerator();
					while (enumUserInfos.hasMoreElements()) {
						EtudiantException exception = enumUserInfos.nextElement();
						messages += exception.getMessageJS() + "\\n";
					}
					component.session.setObjectForKey(messages, "MessageErreur");
				}
				// Recherche des cadres (ou modules) du dossier adm de l'etudiant
				String appCode = null;
				if (preCandidat.isPreInscription()) {
					appCode = EOGarnucheApplication.APPL_CODE_PREINSCRIPTION;
				}
				else
					if (preCandidat.isReInscription()) {
						appCode = EOGarnucheApplication.APPL_CODE_PREREINSCRIPTION;
					}
				EOGarnucheApplication garnucheApplication = FinderGarnucheApplication
						.getGarnucheApplication(edc, appCode, preCandidat.toRne().cRne());
				EOSortOrdering cAppPositionOrdering = EOSortOrdering.sortOrderingWithKey(EOGarnucheCadreApplication.CAPP_POSITION_KEY,
						EOSortOrdering.CompareAscending);
				NSArray<EOSortOrdering> sortOrderings = new NSArray<EOSortOrdering>(cAppPositionOrdering);
				cadres = FinderGarnucheCadreApplication.getGarnucheCadreApplications(edc, garnucheApplication, sortOrderings);

				if (cadres != null && cadres.count() > 0) {
					EOGarnucheCadreApplication cadre = cadres.objectAtIndex(0);
					setUnCadreSelectionne(cadre);
				}
				else {
					setUnCadreSelectionne(null);
				}
			}
		}

		return null;
	}

	public NSArray<EOPreCandidat> preCandidats() {
		return preCandidats;
	}

	public void setPreCandidats(NSArray<EOPreCandidat> preCandidats) {
		this.preCandidats = preCandidats;
	}

	public NSArray<EOGarnucheCadreApplication> cadres() {
		if (lePreCandidat() != null && cadres == null) {
			EOEditingContext ec = component.session().defaultEditingContext();
			String appCode = null;
			if (lePreCandidat().isPreInscription()) {
				appCode = EOGarnucheApplication.APPL_CODE_PREINSCRIPTION;
			}
			else
				if (lePreCandidat().isReInscription()) {
					appCode = EOGarnucheApplication.APPL_CODE_PREREINSCRIPTION;
				}
			EOGarnucheApplication garnucheApplication = FinderGarnucheApplication.getGarnucheApplication(ec, appCode, lePreCandidat.toRne().cRne());
			EOSortOrdering cAppPositionOrdering = EOSortOrdering.sortOrderingWithKey(EOGarnucheCadreApplication.CAPP_POSITION_KEY,
					EOSortOrdering.CompareAscending);
			NSArray<EOSortOrdering> sortOrderings = new NSArray<EOSortOrdering>(cAppPositionOrdering);
			cadres = FinderGarnucheCadreApplication.getGarnucheCadreApplications(ec, garnucheApplication, sortOrderings);
		}
		return cadres;
	}

	public void setCadres(NSArray<EOGarnucheCadreApplication> cadres) {
		this.cadres = cadres;
	}

	public EOPreCandidat unPreCandidat() {
		return unPreCandidat;
	}

	public void setUnPreCandidat(EOPreCandidat unPreCandidat) {
		this.unPreCandidat = unPreCandidat;
	}

	public EOPreCandidat lePreCandidat() {
		return lePreCandidat;
	}

	public void setLePreCandidat(EOPreCandidat lePreCandidat) {
		this.lePreCandidat = lePreCandidat;
	}

	public EOGarnucheCadreApplication getUnCadre() {
		return unCadre;
	}

	public void setUnCadre(EOGarnucheCadreApplication unCadre) {
		this.unCadre = unCadre;
	}

	public EOGarnucheCadreApplication getUnCadreSelectionne() {
		return unCadreSelectionne;
	}

	public void setUnCadreSelectionne(EOGarnucheCadreApplication unCadreSelectionne) {
		this.unCadreSelectionne = unCadreSelectionne;
	}

}
