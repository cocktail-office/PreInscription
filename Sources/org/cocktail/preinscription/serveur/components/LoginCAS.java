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

import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;
import org.cocktail.fwkcktlwebapp.server.CktlWebAction;
import org.cocktail.fwkcktlwebapp.server.components.CktlWebComponent;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

/**
 * Le gestionnaire de la page de login via le serveur CAS. Cette page affiche le lien pour acceder au serveur CAS.
 * 
 * @author Arunas STOCKUS <arunas.stockus at univ-lr.fr>
 */
public class LoginCAS extends CktlWebComponent {
	/** Le commentaire a afficher dans l'entete de al page. */
	private String titleComment = StringCtrl.emptyString();

	/** Le lien d'acces au serveur CAS. */
	private String casLoginLink;

	private String cRne = null;

	/**
	 * Le URL de l'image d'une ligne horisontale dans la page.
	 */
	public String imageLigneSrc() {
		return cktlApp.getImageDefaultURL("ligneApplisDegradee.gif");
	}

	/**
	 * Retourne le URL de l'image "cles" affiches a cote de lien d'acces au serveur CAS.
	 */
	public String imageClefsSrc() {
		return cktlApp.getImageDefaultURL("clefs.gif");
	}

	/**
	 * Cree un nouvel composant.
	 */
	public LoginCAS(WOContext context) {
		super(context);
	}

	/**
	 * Retourne le lien d'acces au service CAS. Ce lien peut etre explicitement defini a l'aide de la methode <code>setCASLoginLink</code>.
	 * 
	 * @see #setCASLoginLink(String)
	 */
	public String lienLoginCAS() {
		if (casLoginLink == null) {
			NSDictionary<String, String> d = null;
			if (cRne() != null) {
				d = new NSDictionary<String, String>(cRne(), "rne");
			}
			return CktlWebAction.getLoginActionURL(context(), false, "DirectAction", true, d);
		}
		else {
			return casLoginLink;
		}
	}

	/**
	 * Retourne le commentaire a afficher dans le titre de la page de login.
	 */
	public String titleComment() {
		if (StringCtrl.normalize(titleComment).length() > 0) {
			return new StringBuffer("<br>(").append(titleComment).append(")").toString();
		}
		else {
			return StringCtrl.emptyString();
		}
	}

	/**
	 * Definit le commentaire a afficher dans le titre de la page de login. Le commentaire peut etre vide ou <em>null</em>.
	 */
	public void setTitleComment(String comment) {
		titleComment = comment;
	}

	/**
	 * Definit le lien d'acces au serveur de login.
	 */
	public void setCASLoginLink(String link) {
		casLoginLink = link;
	}

	public String cRne() {
		return cRne;
	}

	public void setCRne(String cRne) {
		this.cRne = cRne;
	}
}