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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.CktlUserInfo;
import org.cocktail.fwkcktlwebapp.common.database.CktlUserInfoDB;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.fwkcktlwebapp.server.CktlWebAction;
import org.cocktail.fwkcktlwebapp.server.components.CktlAlertPage;
import org.cocktail.fwkcktlwebapp.server.components.CktlLogin;
import org.cocktail.fwkcktlwebapp.server.components.CktlLoginResponder;
import org.cocktail.lecteurcheque.serveur.components.PaiementParCheque;
import org.cocktail.preinscription.serveur.components.Accueil;
import org.cocktail.preinscription.serveur.components.DetailPreCandidat;
import org.cocktail.preinscription.serveur.components.LoginAdministratif;
import org.cocktail.preinscription.serveur.components.LoginCAS;
import org.cocktail.scolarix.serveur.components.LoginInterface;
import org.cocktail.scolarix.serveur.finder.FinderGarnucheApplication;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheAppliUtilisateur;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheApplication;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

public class DirectAction extends CktlWebAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	public Session session() {
		return session();
	}

	public Application app() {
		return (Application) WOApplication.application();
	}

	/**
	 * Execute l'action par defaut de l'application Jefyco. Elle affiche la page de connexion e l'application.
	 */
	public WOActionResults defaultAction() {
		if (useCasService()) {
			return loginCASPage();
		}
		else {
			return loginNoCasPage(null);
		}
	}

	public WOActionResults recupererInfosLecteurChequeAction() {
		WORequest request = request();
		System.out.println("request.sessionID() = " + request.sessionID());
		System.out.println("session() = " + session());
		System.out.println("existingSession() = " + existingSession());
		PaiementParCheque page = null;
		if (session() == null) {
			page = (PaiementParCheque) pageWithName(PaiementParCheque.class.getName());
		}
		else {
			page = (PaiementParCheque) (session()).getSavedPageWithName(PaiementParCheque.class.getName());
		}
		System.out.println("page = " + page);
		String erreur = (String) request.formValueForKey("erreur");
		try {
			if (erreur != null) {
				String erreurDecodee = URLDecoder.decode(erreur, "UTF-8");
				page.setErreur(erreurDecodee);
			}
			else {
				String chequeStr = (String) request.formValueForKey("cheque");
				String chequeStrDecodee = URLDecoder.decode(chequeStr, "UTF-8");
				chequeStrDecodee = chequeStrDecodee.replace(',', ';');
				chequeStrDecodee = chequeStrDecodee.replace('}', ';');
				chequeStrDecodee += "}";
				NSMutableDictionary cheque = new NSMutableDictionary(NSPropertyListSerialization.dictionaryForString(chequeStrDecodee));
				page.setCheque(cheque);
			}
		}
		catch (UnsupportedEncodingException e) {
			System.out.println(" DirectAction.java.recupererInfosLecteurChequeAction : Erreur de decodage \n");
			System.out.println(" Exception: " + e.getMessage());
			e.printStackTrace();
		}
		return page;
	}

	public WOActionResults preinscriptionAction() {
		WORequest request = context().request();
		Session session = (Session) context().session();
		session.reset();
		EOEditingContext ec = session.defaultEditingContext();
		String rne = (String) request.formValueForKey("rne");
		if (!StringCtrl.isEmpty(rne)) {
			rne = rne.toUpperCase();
			EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(ec, EOGarnucheApplication.APPL_CODE_PREINSCRIPTION,
					rne);
			if (garnucheApp != null && garnucheApp.isOuverte()) {
				LoginInterface nextPage = app().getLoginPage(context(), "PREINSC", rne);
				if (nextPage != null) {
					return nextPage;
				}
				else {
					WOActionResults returnPage = pageWithName(WORedirect.class.getName());
					((WORedirect) returnPage).setUrl(Application.appUrlRetour);
					return returnPage;
				}
			}
			else {
				// TODO Prevenir l'utilisateur via un message que l'appli est fermee
				WOActionResults returnPage = pageWithName(WORedirect.class.getName());
				((WORedirect) returnPage).setUrl(Application.appUrlRetour);
				return returnPage;
			}
		}
		else {
			WOActionResults returnPage = pageWithName(WORedirect.class.getName());
			((WORedirect) returnPage).setUrl(Application.appUrlRetour);
			return returnPage;
		}
	}

	public WOActionResults reinscriptionAction() {
		WORequest request = context().request();
		Session session = (Session) context().session();
		session.reset();
		EOEditingContext ec = session.defaultEditingContext();
		String rne = (String) request.formValueForKey("rne");
		if (!StringCtrl.isEmpty(rne)) {
			rne = rne.toUpperCase();
			EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(ec,
					EOGarnucheApplication.APPL_CODE_PREREINSCRIPTION, rne);
			if (garnucheApp != null && garnucheApp.isOuverte()) {
				LoginInterface nextPage = app().getLoginPage(context(), "REINSC", rne);
				if (nextPage != null) {
					return nextPage;
				}
				else {
					WOActionResults returnPage = pageWithName(WORedirect.class.getName());
					((WORedirect) returnPage).setUrl(Application.appUrlRetour);
					return returnPage;
				}
			}
			else {
				// TODO Prevenir l'utilisateur via un message que l'appli est fermee
				WOActionResults returnPage = pageWithName(WORedirect.class.getName());
				((WORedirect) returnPage).setUrl(Application.appUrlRetour);
				return returnPage;
			}
		}
		else {
			WOActionResults returnPage = pageWithName(WORedirect.class.getName());
			((WORedirect) returnPage).setUrl(Application.appUrlRetour);
			return returnPage;
		}
	}

	/**
	 * CAS : traitement authentification OK
	 */
	public WOActionResults loginCasSuccessPage(String s) {
		return loginCasSuccessPage(s, null);
	}

	/**
	 * CAS : traitement authentification en echec
	 */
	public WOActionResults loginCasFailurePage(String errorMessage, String arg1) {
		CktlLog.log("loginCasFailurePage : " + errorMessage + " (" + arg1 + ")");
		StringBuffer msg = new StringBuffer();
		msg.append("Une erreur s'est produite lors de l'authentification de l'uilisateur");
		if (errorMessage != null) {
			msg.append(":<br><br>").append(errorMessage);
		}
		return getErrorPage(msg.toString());
	}

	@SuppressWarnings("unchecked")
	public WOActionResults loginCasSuccessPage(String netid, NSDictionary actionParams) {
		WOActionResults nextPage = null;
		String messageErreur = null;
		Session session = (Session) context().session();
		messageErreur = session.setConnectedUser(netid);

		String cRne = null;
		if (actionParams != null) {
			cRne = (String) actionParams.objectForKey("rne");
			if (cRne != null) {
				cRne = cRne.trim().toUpperCase();
			}
		}
		if (messageErreur == null) {
			messageErreur = checkLogin(session, cRne);
		}

		if (messageErreur != null) {
			nextPage = loginCasFailurePage(messageErreur, null);
		}
		else {
			CktlLoginResponder loginResponder = getNewLoginResponder(null);
			nextPage = loginResponder.loginAccepted(null);
		}
		return nextPage;
	}

	public WOActionResults loginNoCasPage(NSDictionary actionParams) {
		WORequest request = context().request();
		LoginAdministratif page = (LoginAdministratif) pageWithName(LoginAdministratif.class.getName());
		page.registerLoginResponder(getNewLoginResponder(actionParams));
		page.setCRne((String) request.formValueForKey("rne"));
		return page;
	}

	public WOActionResults loginCASPage() {
		LoginCAS pageLoginCAS = (LoginCAS) pageWithName("LoginCAS");
		WORequest request = context().request();
		pageLoginCAS.setCRne((String) request.formValueForKey("rne"));
		return pageLoginCAS;
	}

	/**
	 * CAS : page par defaut si CAS n'est pas parametre
	 */
	public WOActionResults loginNoCasPage() {
		return pageWithName(LoginAdministratif.class.getName());
	}

	/**
	 * affiche une page avec un message d'erreur
	 */
	private WOComponent getErrorPage(String errorMessage) {
		System.out.println("ERREUR = " + errorMessage);
		CktlAlertPage page = (CktlAlertPage) cktlApp.pageWithName(CktlAlertPage.class.getName(), context());
		page.showMessage(null, "ERREUR", errorMessage, null, null, null, CktlAlertPage.ERROR, null);
		return page;
	}

	/**
	 * Retourne la directAction attendue d'apres son nom <code>daName</code>. Si rien n'a ete trouve, alors une page d'avertissement est
	 * affichee.
	 */
	public WOActionResults performActionNamed(String aName) {
		WOActionResults result = null;
		try {
			result = super.performActionNamed(aName);
		}
		catch (Exception e) {
			result = getErrorPage("DirectAction introuvable : \"" + aName + "\"");
		}
		return result;
	}

	public WOActionResults retourRdvAction() {
		System.out.println("retourRdvAction...");
		WOActionResults nextPage = null;
		WORequest aRequest = this.request();
		String numeroEtudiant = (String) aRequest.formValueForKey("NumeroEtudiant");
		System.out.println("numeroEtudiant = " + numeroEtudiant);
		Session session = null;

		if (numeroEtudiant != null) {
			Application appli = (Application) WOApplication.application();
			String sessionID = (String) Application.dicoSessionIDNumeroEtudiant().valueForKey(numeroEtudiant);
			System.out.println("sessionID = " + sessionID);
			session = (Session) appli.restoreSessionWithID(sessionID, this.context());
			System.out.println("session = " + session);
			if (session == null || session.isTerminating()) {
				System.out.println("aaaiiiiiiiieeee !!!");
				nextPage = pageWithName(WORedirect.class.getName());
				((WORedirect) nextPage).setUrl(Application.appUrlRetour);
			}
			else {
				System.out.println("ok !");
				// On nettoie l'edc de la session afin de s'assurer que les donnees de l'etudiant sont fraiches
				session.defaultEditingContext().refaultAllObjects();
				nextPage = session.getSavedPageWithName(Accueil.class.getName());
				session.setIsApresEnregistrer(false);
			}
			Application.dicoSessionIDNumeroEtudiant().removeObjectForKey(numeroEtudiant);
		}
		else {
			nextPage = pageWithName(WORedirect.class.getName());
			((WORedirect) nextPage).setUrl(Application.appUrlRetour);
			session().terminate();
		}
		return nextPage;
	}

	public WOActionResults validerLoginAdministratifAction() {
		WOActionResults page = null;
		WORequest request = context().request();
		String login = StringCtrl.normalize((String) request.formValueForKey("identifiant"));
		String password = StringCtrl.normalize((String) request.formValueForKey("mot_de_passe"));
		String messageErreur = null;
		Session session = (Session) context().session();

		String cRne = (String) request.formValueForKey("rne");
		if (cRne == null) {
			cRne = ((Application) WOApplication.application()).config().stringForKey("DEFAULT_C_RNE");
		}
		CktlLoginResponder loginResponder = getNewLoginResponder(null);
		CktlUserInfo loggedUserInfo = new CktlUserInfoDB(cktlApp.dataBus());
		if (login.length() == 0) {
			messageErreur = "Vous devez renseigner l'identifiant.";
		}
		else
			if (!loginResponder.acceptLoginName(login)) {
				messageErreur = "Vous n'ètes pas autorisé(e) à utiliser cette application";
			}
			else {
				if (password == null) {
					password = "";
				}
				loggedUserInfo.setRootPass(loginResponder.getRootPassword());
				loggedUserInfo.setAcceptEmptyPass(loginResponder.acceptEmptyPassword());
				loggedUserInfo.compteForLogin(login, password, true);
				if (loggedUserInfo.errorCode() != CktlUserInfo.ERROR_NONE) {
					if (loggedUserInfo.errorMessage() != null) {
						messageErreur = loggedUserInfo.errorMessage();
					}
					CktlLog.rawLog(">> Erreur | " + loggedUserInfo.errorMessage());
				}
			}

		if (messageErreur == null) {
			session.setConnectedUserInfo(loggedUserInfo);
			String erreur = session.setConnectedUser(loggedUserInfo.login());
			if (erreur != null) {
				messageErreur = erreur;
			}
			else {
				if (StringCtrl.isEmpty(cRne)) {
					messageErreur = "Vous n'êtes pas autorisé(e) à utiliser cette application";
				}
				else {
					messageErreur = checkLogin(session, cRne);
				}
			}
		}

		if (messageErreur != null) {
			if (session != null) {
				session.terminate();
			}
			page = pageWithName(LoginAdministratif.class.getName());
			((LoginAdministratif) page).setMessageErreur(messageErreur);
			return page;
		}

		return loginResponder.loginAccepted(null);

	}

	private String checkLogin(Session session, String codeRne) {
		if (StringCtrl.isEmpty(codeRne)) {
			codeRne = ((Application) WOApplication.application()).config().stringForKey("DEFAULT_C_RNE");
		}
		if (StringCtrl.isEmpty(codeRne)) {
			return "Il faut spécifier l'établissement (paramètre rne).";
		}
		EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(session.defaultEditingContext(),
				EOGarnucheApplication.APPL_CODE_ADMIN_PRE_PRERE_INSCRIPTION, codeRne.toUpperCase());
		if (garnucheApp == null || garnucheApp.isOuverte() == false) {
			return "L'application d'administration n'est pas ouverte pour l'établissement " + codeRne;
		}
		else {
			session.setGarnucheApplication(garnucheApp);
			session.setAnneeScolaire(garnucheApp.anneeEnCours());

			NSMutableArray<EOQualifier> quals = new NSMutableArray<EOQualifier>(2);
			quals.addObject(new EOKeyValueQualifier(EOGarnucheAppliUtilisateur.NO_INDIVIDU_KEY, EOKeyValueQualifier.QualifierOperatorEqual, session
					.connectedUserInfo().noIndividu()));
			quals.addObject(new EOKeyValueQualifier(EOGarnucheAppliUtilisateur.TO_GARNUCHE_APPLICATION_KEY,
					EOKeyValueQualifier.QualifierOperatorEqual, garnucheApp));
			NSArray<EOGarnucheAppliUtilisateur> arrayUtilisateurs = EOGarnucheAppliUtilisateur.fetchAll(session.defaultEditingContext(),
					new EOAndQualifier(quals), null);
			if (arrayUtilisateurs == null || arrayUtilisateurs.isEmpty()) {
				return "Vous n'êtes pas autorisé(e) à utiliser cette application.";
			}
		}
		return null;
	}

	public CktlLoginResponder getNewLoginResponder(NSDictionary actionParams) {
		return new DefaultLoginResponder(actionParams);
	}

	public class DefaultLoginResponder implements CktlLoginResponder {
		private NSDictionary actionParams;

		public DefaultLoginResponder(NSDictionary actionParams) {
			this.actionParams = actionParams;
		}

		public NSDictionary actionParams() {
			return actionParams;
		}

		public WOComponent loginAccepted(CktlLogin loginComponent) {
			return cktlApp.pageWithName(DetailPreCandidat.class.getName(), context());
		}

		public boolean acceptLoginName(String loginName) {
			return cktlApp.acceptLoginName(loginName);
		}

		public boolean acceptEmptyPassword() {
			return cktlApp.config().booleanForKey("ACCEPT_EMPTY_PASSWORD");
		}

		public String getRootPassword() {
			return cktlApp.getRootPassword();
		}
	}

}
