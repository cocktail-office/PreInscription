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

import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.preinscription.serveur.components.Accueil;
import org.cocktail.scolarix.serveur.components.LoginInterface;
import org.cocktail.scolarix.serveur.exception.EtudiantException;
import org.cocktail.scolarix.serveur.finder.FinderGarnucheApplication;
import org.cocktail.scolarix.serveur.finder.FinderPreCandidat;
import org.cocktail.scolarix.serveur.interfaces.IEtudiant;
import org.cocktail.scolarix.serveur.metier.eos.EOEtudiant;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheApplication;
import org.cocktail.scolarix.serveur.metier.eos.EOHistorique;
import org.cocktail.scolarix.serveur.metier.eos.EOPreCandidat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

public class DirectActionIdentification extends DirectAction {

	public DirectActionIdentification(WORequest request) {
		super(request);
	}

	public WOActionResults validerLoginNeoBachelierViaNumeroIneAction() {
		WORequest request = context().request();
		String codeRne = (String) request.formValueForKey("code_rne");
		String numeroINE = (String) request.formValueForKey("numero_ine");
		String dateDeNaissanceStr = (String) request.formValueForKey("date_de_naissance");
		String messageErreur = null;
		Session session = null;

		if (!StringCtrl.isEmpty(codeRne) && !StringCtrl.isEmpty(numeroINE) && !StringCtrl.isEmpty(dateDeNaissanceStr)) {
			NSTimestamp dateDeNaissance = DateCtrl.stringToDate(dateDeNaissanceStr, "%d/%m/%Y");
			if (dateDeNaissance != null) {
				session = (Session) context().session();
				EOEditingContext edc = session.defaultEditingContext();
				IEtudiant etudiant = null;
				IEtudiant oldEtudiant = session.etudiant();
				if (oldEtudiant != null) {
					// Reinitialisation de l'etudiant
					edc.revert();
					// edc.invalidateAllObjects();
				}
				try {
					session.setCodeRne(codeRne);

					EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatIne(edc, numeroINE.toLowerCase(), dateDeNaissance);
					etudiant = session.getEtudiant(preCandidat, codeRne);
					if (etudiant != null) {
						if (etudiant.isPreInscription()) {
							EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "PREINSCAPP", codeRne);
							if (garnucheApp != null) {
								// Recuperation de l'annee scolaire de preinscription
								Integer anneeScolaire = garnucheApp.anneeEnCours();
								session.setAnneeScolaire(anneeScolaire);
								session.setEtudiant(etudiant);
								session.setGarnucheApplication(garnucheApp);
								WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
								// pdm juste pour voir...
								System.out.println("[PreInscViaIne " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - INE = "
										+ etudiant.numeroINE());
								return page;
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
						else {
							messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
						}
					}
					else {
						messageErreur = "Etudiant inconnu dans notre base de donnees.";
					}
				}
				catch (EtudiantException e) {
					messageErreur = e.getMessageHTML();
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
					Throwable cause = e5.getCause();
					EtudiantException e = (EtudiantException) cause;
					if (e.isBloquant()) {
						messageErreur = ((EtudiantException) cause).getMessageHTML();
					}
					else {
						if (etudiant != null) {
							if (etudiant.isPreReInscription()) {
								EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "PREINSCAPP", codeRne);
								if (garnucheApp != null) {
									// Recuperation de l'annee scolaire de preinscription
									Integer anneeScolaire = garnucheApp.anneeEnCours();
									session.setAnneeScolaire(anneeScolaire);
									session.setEtudiant(etudiant);
									session.setGarnucheApplication(garnucheApp);
									WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
									session.setObjectForKey(e.getMessageHTML(), "MessageErreur");
									// pdm juste pour voir...
									System.out.println("[PreInscViaIne. " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - INE = "
											+ etudiant.numeroINE());
									return page;
								}
								else {
									messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
								}
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
					}
				}
			}
			else {
				messageErreur = "Veuillez saisir la date de naissance au format jj/mm/aaaa.";
			}
		}
		else {
			messageErreur = "Veuillez saisir le num&eacute;ro INE ET la date de naissance.";
		}

		if (session != null) {
			session.terminate();
		}

		LoginInterface page = app().getLoginPage(context(), "PREINSC", codeRne);
		if (page != null) {
			page.setMessageErreur(messageErreur);
			page.setIsIdentificationNumeroIneDateNaissance(true);
		}

		return page;
	}

	public WOActionResults validerLoginNeoBachelierViaNumeroPostbacAction() {
		WORequest request = context().request();
		String codeRne = (String) request.formValueForKey("code_rne");
		String numeroPostbac = (String) request.formValueForKey("numero_postbac");
		String dateDeNaissanceStr = (String) request.formValueForKey("date_de_naissance");
		String messageErreur = null;
		Session session = null;

		if (!StringCtrl.isEmpty(codeRne) && !StringCtrl.isEmpty(numeroPostbac) && !StringCtrl.isEmpty(dateDeNaissanceStr)) {
			NSTimestamp dateDeNaissance = DateCtrl.stringToDate(dateDeNaissanceStr, "%d/%m/%Y");
			if (dateDeNaissance != null) {
				session = (Session) context().session();
				EOEditingContext edc = session.defaultEditingContext();
				IEtudiant etudiant = null;
				IEtudiant oldEtudiant = session.etudiant();
				if (oldEtudiant != null) {
					// Reinitialisation de l'etudiant
					edc.revert();
					// edc.invalidateAllObjects();
				}
				try {
					session.setCodeRne(codeRne);

					EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatPostBac(edc, numeroPostbac.toLowerCase(), dateDeNaissance);
					etudiant = session.getEtudiant(preCandidat, codeRne);
					if (etudiant != null) {
						if (etudiant.isPreInscription()) {
							EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "PREINSCAPP", codeRne);
							if (garnucheApp != null) {
								// Recuperation de l'annee scolaire de preinscription
								Integer anneeScolaire = garnucheApp.anneeEnCours();
								session.setAnneeScolaire(anneeScolaire);
								session.setEtudiant(etudiant);
								session.setGarnucheApplication(garnucheApp);
								WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
								// pdm juste pour voir...
								System.out.println("[PreInscViaPostBac " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - INE = "
										+ etudiant.numeroINE());
								return page;
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
						else {
							messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
						}
					}
					else {
						messageErreur = "Etudiant inconnu dans notre base de donnees.";
					}
				}
				catch (EtudiantException e) {
					messageErreur = e.getMessageHTML();
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
					Throwable cause = e5.getCause();
					EtudiantException e = (EtudiantException) cause;
					if (e.isBloquant()) {
						messageErreur = ((EtudiantException) cause).getMessageHTML();
					}
					else {
						if (etudiant != null) {
							if (etudiant.isPreInscription()) {
								EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "PREINSCAPP", codeRne);
								if (garnucheApp != null) {
									// Recuperation de l'annee scolaire de preinscription
									Integer anneeScolaire = garnucheApp.anneeEnCours();
									session.setAnneeScolaire(anneeScolaire);
									session.setEtudiant(etudiant);
									session.setGarnucheApplication(garnucheApp);
									WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
									session.setObjectForKey(e.getMessageHTML(), "MessageErreur");
									// pdm juste pour voir...
									System.out.println("[PreInscViaPostBac. " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - INE = "
											+ etudiant.numeroINE());
									return page;
								}
								else {
									messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
								}
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
					}
				}
			}
			else {
				messageErreur = "Veuillez saisir la date de naissance au format jj/mm/aaaa.";
			}
		}
		else {
			messageErreur = "Veuillez saisir le num&eacute;ro PostBac ET la date de naissance.";
		}

		if (session != null) {
			session.terminate();
		}

		LoginInterface page = app().getLoginPage(context(), "PREINSC", codeRne);
		if (page != null) {
			page.setMessageErreur(messageErreur);
			page.setIsIdentificationNumeroPostbacDateNaissance(true);
		}

		return page;
	}

	private void testBlocageBu(IEtudiant etudiant, Integer anneeScolaire) throws EtudiantException {
		// cas particulier bu bloquant ou non selon paramètre...
		if (app().config().booleanForKey("ETUDIANT_BU_BLOQUANT")) {
			EOHistorique historique = etudiant.historiquePlusRecent(anneeScolaire);
			if (historique != null && historique.histResteBu() != null && historique.histResteBu().intValue() == 1) {
				throw new EtudiantException("Vous êtes en possession de livres à la BU, vous devez les rendre avant de pouvoir poursuivre !");
			}
		}
	}

	public WOActionResults validerLoginEtudiantViaLoginAction() {
		WORequest request = context().request();
		String codeRne = (String) request.formValueForKey("code_rne");
		String login = (String) request.formValueForKey("login");
		String pwd = (String) request.formValueForKey("pwd");
		String messageErreur = null;
		Session session = null;

		if (!StringCtrl.isEmpty(codeRne) && !StringCtrl.isEmpty(login) && !StringCtrl.isEmpty(pwd)) {
			session = (Session) context().session();
			EOEditingContext edc = session.defaultEditingContext();
			IEtudiant etudiant = null;
			IEtudiant oldEtudiant = session.etudiant();
			if (oldEtudiant != null) {
				// Reinitialisation de l'etudiant
				edc.revert();
				// edc.invalidateAllObjects();
			}
			try {
				session.setCodeRne(codeRne);

				if (edc != null && login != null && pwd != null) {
					EOEtudiant myOldEtudiant = EOEtudiant.getEtudiantLogin(edc, login, pwd);
					if (myOldEtudiant == null) {
						throw new EtudiantException("Login / mot de passe inconnu");
					}
					else {
						Integer numeroEtudiant = myOldEtudiant.numero();
						NSTimestamp dateDeNaissance = myOldEtudiant.toFwkpers_Individu().dNaissance();

						EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatNumero(edc, numeroEtudiant, dateDeNaissance);
						etudiant = session.getEtudiant(preCandidat, codeRne);
					}
				}
				else {
					throw new EtudiantException("Login / mot de passe inconnu...");
				}

				if (etudiant != null) {
					if (etudiant.isPreReInscription()) {
						EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
						if (garnucheApp != null) {
							// Recuperation de l'annee scolaire de reinscription
							Integer anneeScolaire = garnucheApp.anneeEnCours();
							testBlocageBu(etudiant, anneeScolaire);
							session.setAnneeScolaire(anneeScolaire);
							session.setEtudiant(etudiant);
							session.setGarnucheApplication(garnucheApp);
							WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
							// pdm juste pour voir...
							System.out.println("[ReInscViaLogin " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - etudNumero = "
									+ etudiant.numero());
							return page;
						}
						else {
							messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
						}
					}
					else {
						messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
					}
				}
				else {
					messageErreur = "Etudiant inconnu dans notre base de donnees.";
				}
			}
			catch (EtudiantException e) {
				messageErreur = e.getMessageHTML();
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
				Throwable cause = e5.getCause();
				if (cause.getClass().equals(EtudiantException.class)) {
					EtudiantException e = (EtudiantException) cause;
					if (e.isBloquant()) {
						messageErreur = ((EtudiantException) cause).getMessageHTML();
					}
					else {
						if (etudiant != null) {
							if (etudiant.isPreReInscription()) {
								EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
								if (garnucheApp != null) {
									// Recuperation de l'annee scolaire de reinscription
									Integer anneeScolaire = garnucheApp.anneeEnCours();
									testBlocageBu(etudiant, anneeScolaire);
									session.setAnneeScolaire(anneeScolaire);
									session.setEtudiant(etudiant);
									session.setGarnucheApplication(garnucheApp);
									WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
									session.setObjectForKey(e.getMessageHTML(), "MessageErreur");
									// pdm juste pour voir...
									System.out.println("[ReInscViaLogin. " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - etudNumero = "
											+ etudiant.numero());
									return page;
								}
								else {
									messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
								}
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
					}
				}
				else {
					e5.printStackTrace();
				}
			}
		}
		else {
			messageErreur = "Veuillez saisir l'identifiant ET le mot de passe.";
		}

		if (session != null) {
			session.terminate();
		}

		LoginInterface page = app().getLoginPage(context(), "REINSC", codeRne);
		if (page != null) {
			page.setMessageErreur(messageErreur);
			page.setIsIdentificationLoginPwd(true);
		}

		return page;
	}

	public WOActionResults validerLoginEtudiantViaNumeroEtudiantAction() {
		WORequest request = context().request();
		String codeRne = (String) request.formValueForKey("code_rne");
		String numeroEtudiant = (String) request.formValueForKey("num_etudiant");
		String dateDeNaissanceStr = (String) request.formValueForKey("date_de_naissance");
		String messageErreur = null;
		Session session = null;

		if (!StringCtrl.isEmpty(codeRne) && !StringCtrl.isEmpty(numeroEtudiant) && !StringCtrl.isEmpty(dateDeNaissanceStr)) {
			NSTimestamp dateDeNaissance = DateCtrl.stringToDate(dateDeNaissanceStr, "%d/%m/%Y");
			if (dateDeNaissance != null) {
				session = (Session) context().session();
				EOEditingContext edc = session.defaultEditingContext();
				IEtudiant etudiant = null;
				IEtudiant oldEtudiant = session.etudiant();
				if (oldEtudiant != null) {
					// Reinitialisation de l'etudiant
					edc.revert();
					// edc.invalidateAllObjects();
				}
				try {
					session.setCodeRne(codeRne);
					EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatNumero(edc, Integer.valueOf(numeroEtudiant), dateDeNaissance);
					etudiant = session.getEtudiant(preCandidat, codeRne);
					if (etudiant != null) {
						if (etudiant.isPreReInscription()) {
							EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
							if (garnucheApp != null) {
								// Recuperation de l'annee scolaire de reinscription
								Integer anneeScolaire = garnucheApp.anneeEnCours();
								testBlocageBu(etudiant, anneeScolaire);
								session.setAnneeScolaire(anneeScolaire);
								session.setEtudiant(etudiant);
								session.setGarnucheApplication(garnucheApp);
								WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
								// pdm juste pour voir...
								System.out.println("[ReInscViaNumero " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - etudNumero = "
										+ etudiant.numero());
								return page;
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
						else {
							messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
						}
					}
					else {
						messageErreur = "Etudiant inconnu dans notre base de donnees.";
					}
				}
				catch (EtudiantException e) {
					messageErreur = e.getMessageHTML();
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
					Throwable cause = e5.getCause();
					if (cause.getClass().equals(EtudiantException.class)) {
						EtudiantException e = (EtudiantException) cause;
						if (e.isBloquant()) {
							messageErreur = ((EtudiantException) cause).getMessageHTML();
						}
						else {
							if (etudiant != null) {
								if (etudiant.isPreReInscription()) {
									EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
									if (garnucheApp != null) {
										// Recuperation de l'annee scolaire de reinscription
										Integer anneeScolaire = garnucheApp.anneeEnCours();
										testBlocageBu(etudiant, anneeScolaire);
										session.setAnneeScolaire(anneeScolaire);
										session.setEtudiant(etudiant);
										session.setGarnucheApplication(garnucheApp);
										WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
										session.setObjectForKey(e.getMessageHTML(), "MessageErreur");
										// pdm juste pour voir...
										System.out.println("[ReInscViaNumero. " + codeRne + "] " + DateCtrl.currentDateTimeString()
												+ " - etudNumero = " + etudiant.numero());
										return page;
									}
									else {
										messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
									}
								}
								else {
									messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
								}
							}
						}
					}
					else {
						e5.printStackTrace();
					}
				}
			}
			else {
				messageErreur = "Veuillez saisir la date de naissance au format jj/mm/aaaa.";
			}
		}
		else {
			messageErreur = "Veuillez saisir le num&eacute;ro d'&eacute;tudiant ET la date de naissance.";
		}

		if (session != null) {
			session.terminate();
		}

		LoginInterface page = app().getLoginPage(context(), "REINSC", codeRne);
		if (page != null) {
			page.setMessageErreur(messageErreur);
			page.setIsIdentificationNumeroEtudiantDateNaissance(true);
		}

		return page;
	}

	public WOActionResults validerLoginEtudiantViaNumeroIneAction() {
		WORequest request = context().request();
		String codeRne = (String) request.formValueForKey("code_rne");
		String numeroINE = (String) request.formValueForKey("ine");
		String dateDeNaissanceStr = (String) request.formValueForKey("date_de_naissance");
		String messageErreur = null;
		Session session = null;

		if (!StringCtrl.isEmpty(codeRne) && !StringCtrl.isEmpty(numeroINE) && !StringCtrl.isEmpty(dateDeNaissanceStr)) {
			NSTimestamp dateDeNaissance = DateCtrl.stringToDate(dateDeNaissanceStr, "%d/%m/%Y");
			if (dateDeNaissance != null) {
				session = (Session) context().session();
				EOEditingContext edc = session.defaultEditingContext();
				IEtudiant etudiant = null;
				IEtudiant oldEtudiant = session.etudiant();
				if (oldEtudiant != null) {
					// Reinitialisation de l'etudiant
					edc.revert();
					// edc.invalidateAllObjects();
				}
				try {
					session.setCodeRne(codeRne);

					EOPreCandidat preCandidat = FinderPreCandidat.getPreCandidatIne(edc, numeroINE.toLowerCase(), dateDeNaissance);
					etudiant = session.getEtudiant(preCandidat, codeRne);
					if (etudiant != null) {
						if (etudiant.isPreReInscription()) {
							EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
							if (garnucheApp != null) {
								// Recuperation de l'annee scolaire de reinscription
								Integer anneeScolaire = garnucheApp.anneeEnCours();
								testBlocageBu(etudiant, anneeScolaire);
								session.setAnneeScolaire(anneeScolaire);
								session.setEtudiant(etudiant);
								session.setGarnucheApplication(garnucheApp);
								WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
								// pdm juste pour voir...
								System.out.println("[ReInscViaIne " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - etudNumero = "
										+ etudiant.numero());
								return page;
							}
							else {
								messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
							}
						}
						else {
							messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
						}
					}
					else {
						messageErreur = "Etudiant inconnu dans notre base de donnees.";
					}
				}
				catch (EtudiantException e) {
					messageErreur = e.getMessageHTML();
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
					Throwable cause = e5.getCause();
					if (cause.getClass().equals(EtudiantException.class)) {
						EtudiantException e = (EtudiantException) cause;
						if (e.isBloquant()) {
							messageErreur = ((EtudiantException) cause).getMessageHTML();
						}
						else {
							if (etudiant != null) {
								if (etudiant.isPreReInscription()) {
									EOGarnucheApplication garnucheApp = FinderGarnucheApplication.getGarnucheApplication(edc, "REINSCAPP", codeRne);
									if (garnucheApp != null) {
										// Recuperation de l'annee scolaire de reinscription
										Integer anneeScolaire = garnucheApp.anneeEnCours();
										testBlocageBu(etudiant, anneeScolaire);
										session.setAnneeScolaire(anneeScolaire);
										session.setEtudiant(etudiant);
										session.setGarnucheApplication(garnucheApp);
										WOActionResults page = session.getSavedPageWithName(Accueil.class.getName());
										session.setObjectForKey(e.getMessageHTML(), "MessageErreur");
										// pdm juste pour voir...
										System.out.println("[ReInscViaIne. " + codeRne + "] " + DateCtrl.currentDateTimeString() + " - etudNumero = "
												+ etudiant.numero());
										return page;
									}
									else {
										messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
									}
								}
								else {
									messageErreur = "Vous n'&egrave;tes pas autoris&eacute;(e) &agrave; utiliser cette application.";
								}
							}
						}
					}
					else {
						e5.printStackTrace();
					}
				}
			}
			else {
				messageErreur = "Veuillez saisir la date de naissance au format jj/mm/aaaa.";
			}
		}
		else {
			messageErreur = "Veuillez saisir le num&eacute;ro INE ET la date de naissance.";
		}

		if (session != null) {
			session.terminate();
		}

		LoginInterface page = app().getLoginPage(context(), "REINSC", codeRne);
		if (page != null) {
			page.setMessageErreur(messageErreur);
			page.setIsIdentificationNumeroIneDateNaissance(true);
		}

		return page;
	}

}
