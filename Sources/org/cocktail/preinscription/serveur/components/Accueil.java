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

import java.lang.reflect.InvocationTargetException;

import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.preinscription.serveur.Application;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.finder.FinderGarnucheCadreApplication;
import org.cocktail.scolarix.serveur.finder.FinderPreCandidat;
import org.cocktail.scolarix.serveur.interfaces.IEtudiant;
import org.cocktail.scolarix.serveur.metier.eos.EOHistorique;
import org.cocktail.scolarix.serveur.metier.eos.EOPreCandidat;
import org.cocktail.scolarix.serveur.metier.eos.EORdvCandidat;
import org.cocktail.scolarix.serveur.ui.EOGarnucheCadreApplication;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

// Generated by the WOLips Templateengine Plug-in at 1 avr. 2008 15:44:13
public class Accueil extends MyComponent {

	private String onloadJS;

	public Accueil(WOContext context) {
		super(context);
	}

	public String commentaires() {
		String commentaires = "<div class=\"bleu\">Bienvenu(e) dans la " + session.garnucheApplication().applNom() + " en ligne<br/>"
				+ session.garnucheApplication().toVComposanteScolarite().llStructure() + "</div><br/><br/>";
		EORdvCandidat rdv = session.etudiant().rendezVous();
		if (rdv != null && rdv.convocDate() != null) {
			NSTimestamp dateRdv = rdv.convocDate();
			String dateStr = DateCtrl.dateToString(dateRdv, "%d %B %Y");
			String heureRdv = rdv.convocHeureEnClair();
			commentaires += "Vous avez rendez-vous le : <blink><b>";
			commentaires += dateStr + " &agrave; " + heureRdv + "</font></b></blink><br/>";
			commentaires += "&agrave;<br/>";
			commentaires += rdv.toRdvPlanningInfo().lieu1Convoc() + "<br/>" + rdv.toRdvPlanningInfo().lieu2Convoc();
		}
		else {
			commentaires += "<div class=\"rougegras\">Apr&egrave;s avoir pris connaissance des diff&eacute;rents documents d'aide et d'information en ligne,<br/>veuillez suivre les diff&eacute;rentes &eacute;tapes ci-dessous :</div><br/><br/>";
		}
		return commentaires;
	}

	public WizardDossierAdministratif completerLeDossier() {
		WizardDossierAdministratif page = (WizardDossierAdministratif) pageWithName(WizardDossierAdministratif.class.getName());
		IEtudiant oldEtudiant = session.etudiant();
		String numIne = oldEtudiant.numeroINE();
		NSTimestamp dateDeNaissance = oldEtudiant.individu().dNaissance();
		// Reinitialisation de l'etudiant
		// session.defaultEditingContext().revert();

		IEtudiant etudiant = null;
		EOEditingContext edc = session.defaultEditingContext();
		try {
			edc.invalidateAllObjects();
			if (numIne != null) {
				EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatIne(edc, numIne, dateDeNaissance);
				etudiant = session.getEtudiant(preCandidat, session.codeRne);
			}
			else {
				EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatNumero(edc, oldEtudiant.numero(), dateDeNaissance);
				etudiant = session.getEtudiant(preCandidat, session.codeRne);
			}
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
		catch (InvocationTargetException e5) {
			e5.printStackTrace();
		}
		catch (EtudiantException e6) {
			e6.printStackTrace();
		}
		session.setEtudiant(etudiant);
		page.setEtudiant(etudiant);
		page.setAnneeScolaire(session.anneeScolaire);
		EOSortOrdering cAppPositionOrdering = EOSortOrdering.sortOrderingWithKey(EOGarnucheCadreApplication.CAPP_POSITION_KEY,
				EOSortOrdering.CompareAscending);
		NSArray<EOSortOrdering> sortOrderings = new NSArray<EOSortOrdering>(cAppPositionOrdering);
		NSArray<EOGarnucheCadreApplication> cadres = FinderGarnucheCadreApplication.getGarnucheCadreApplications(edc, session.garnucheApplication(),
				sortOrderings);
		page.ctrl.setModules(cadres);
		session.indexModuleActif = 0;
		return page;
	}

	public WOResponse imprimerLeDossier() {
		if (session == null || session.etudiant() == null || session.ctrlImpression() == null) {
			return null;
		}
		if (session.ctrlImpression().isPossibleImprimerLeDossierAdministratif()) {
			return session.ctrlImpression().imprimerLeDossierDePreInscription();
		}
		return null;
	}

	public WOComponent telecharger() {
		return null;
	}

	public WOComponent modifierRdv() {
		// FIXME pdm en attendant le retour du messie...
		if (session == null || session.etudiant() == null || session.etudiant().numero() == null || session.etudiant().individu() == null
				|| session.etudiant().individu().dNaissance() == null) {
			return null;
		}
		Integer numero = session.etudiant().numero();
		NSTimestamp dateDeNaissance = session.etudiant().individu().dNaissance();
		String webRdvUrl = Application.webRdvURL;
		String queryString = "ControlerIdentification?";

		queryString = queryString + "NumeroEtudiant=" + StringCtrl.extendWithChars(numero.toString(), "0", 8, true);
		queryString = queryString + "&JourNaissance=" + DateCtrl.dateToString(dateDeNaissance, "%d");
		queryString = queryString + "&MoisNaissance=" + DateCtrl.dateToString(dateDeNaissance, "%m");
		queryString = queryString + "&AnneeNaissance=" + DateCtrl.dateToString(dateDeNaissance, "%y");
		/*
		 * String appWebUrl = context().directActionURLForActionNamed("retourRdv",null); appWebUrl =
		 * appWebUrl.substring(appWebUrl.indexOf(application.name())-1); appWebUrl =
		 * application.cgiAdaptorURL()+appWebUrl+"&ne="+numero.toString()+"&dn="+DateCtrl.dateToString(dateDeNaissance,"%d/%m/%Y"); try {
		 * queryString = queryString + "&Retour="+URLEncoder.encode(appWebUrl, "UTF-8"); } catch (UnsupportedEncodingException e) {
		 * e.printStackTrace(); }
		 */
		WORedirect nextPage = (WORedirect) pageWithName(WORedirect.class.getName());
		nextPage.setUrl(webRdvUrl + queryString);
		Application.dicoSessionIDNumeroEtudiant().setObjectForKey(session.sessionID(), StringCtrl.extendWithChars(numero.toString(), "0", 8, true));

		return (nextPage);
	}

	public WOComponent quitter() {
		if (session != null && session.etudiant() != null && session.etudiant().numero() != null) {
			Application.dicoSessionIDNumeroEtudiant().removeObjectForKey(String.valueOf(session.etudiant().numero().intValue()));
		}
		WORedirect nextPage = (WORedirect) pageWithName(WORedirect.class.getName());
		nextPage.setUrl(Application.appUrlRetour);
		session.terminate();
		return nextPage;
	}

	public boolean isExisteUnRdv() {
		boolean isExisteUnRdv = false;
		EORdvCandidat rdv = session.etudiant().rendezVous();
		if (rdv != null) {
			isExisteUnRdv = true;
		}

		return isExisteUnRdv;
	}

	public boolean isRdvPossible() {
		return (session.etudiant().isRendezVousPossible() && Application.webRdvURL != null && Application.webRdvURL.length() > 0);
	}

	/**
	 * @return the onloadJS
	 */
	public String onloadJS() {
		if (onloadJS == null) {
			onloadJS = "";
			EOHistorique historique = session.etudiant().historiquePlusRecent(session.anneeScolaire());
			if (historique != null && historique.histResteBu() != null && historique.histResteBu().intValue() == 1) {
				// onloadJS +=
				// "alert('Vous avez encore des livres de la Bibliothèque Universitaire !.\\nPensez à les rendre avant de passer sur la chaine d\\'inscriptions, sinon .......');";
				onloadJS += "openAlertWin('<font color=#FF0000>Vous &ecirc;tes encore en possession de <b>livres</b> appartenant &agrave; la <b>Biblioth&egrave;que Universitaire</b>.<br/><br/>";
				onloadJS += "Pensez &agrave; les rendre avant de passer sur la chaine d\\'inscriptions.</font>');";
			}
			try {
				session.etudiant().alerteFormationsEnvisagees();
			}
			catch (EtudiantException e) {
				onloadJS += "openAlertWin('<font color=#FF0000>";
				onloadJS += e.getMessageJS();
				onloadJS += "</font>');";
			}
		}
		return onloadJS;
	}

	/**
	 * @param onloadJS
	 *            the onloadJS to set
	 */
	public void setOnloadJS(String onloadJS) {
		this.onloadJS = onloadJS;
	}
}