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

import org.cocktail.preinscription.serveur.components.Telechargements;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheAppliDocumentation;
import org.cocktail.scolarix.serveur.metier.eos.EOGarnucheApplication;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

public class CtrlTelechargements {
	private Telechargements wocomponent = null;
	private EOEditingContext edc;

	NSArray<EOGarnucheAppliDocumentation> documents = null;

	public CtrlTelechargements(Telechargements component) {
		wocomponent = component;
		if (wocomponent != null) {
			edc = wocomponent.session().defaultEditingContext();
		}
	}

	public NSArray<EOGarnucheAppliDocumentation> documents() {
		if (documents == null) {
			EOGarnucheApplication appl = wocomponent.session.garnucheApplication();
			EOKeyValueQualifier qualAppl = new EOKeyValueQualifier(EOGarnucheAppliDocumentation.TO_GARNUCHE_APPLICATION_KEY,
					EOQualifier.QualifierOperatorEqual, appl);
			EOKeyValueQualifier qualTypeDocument = new EOKeyValueQualifier(EOGarnucheAppliDocumentation.ADOC_TYPE_KEY,
					EOQualifier.QualifierOperatorCaseInsensitiveLike, "A");
			EOAndQualifier qualifier = new EOAndQualifier(new NSArray<EOQualifier>(new EOKeyValueQualifier[] { qualAppl, qualTypeDocument }));
			EOSortOrdering sortOrdering = EOSortOrdering.sortOrderingWithKey(EOGarnucheAppliDocumentation.ADOC_KEY_KEY,
					EOSortOrdering.CompareCaseInsensitiveAscending);
			documents = EOGarnucheAppliDocumentation.fetchAll(edc, qualifier, new NSArray<EOSortOrdering>(sortOrdering));
		}

		return documents;
	}
}
