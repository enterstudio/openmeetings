/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.web.common;

import org.apache.openmeetings.persistence.beans.user.User;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ComunityUserForm extends Form<User> {
	private static final long serialVersionUID = -4487619335283747717L;

	public ComunityUserForm(String id, IModel<User> model) {
		super(id, model);

		RadioGroup<Long> rg = new RadioGroup<Long>("community_settings", new IModel<Long>() {
			private static final long serialVersionUID = 1L;

			public Long getObject() {
				User u = ComunityUserForm.this.getModelObject();
				if (Boolean.TRUE.equals(u.getShowContactData())) {
					return 1L;
				} else if (Boolean.TRUE.equals(u.getShowContactDataToContacts())) {
					return 2L;
				}
				return 3L;
			}

			public void setObject(Long choice) {
				User u = ComunityUserForm.this.getModelObject();
				if (choice.equals(1L)) {
					u.setShowContactData(true);
					u.setShowContactDataToContacts(false);
				} else if (choice.equals(2L)) {
					u.setShowContactData(false);
					u.setShowContactDataToContacts(true);
				} else {
					u.setShowContactData(false);
					u.setShowContactDataToContacts(false);
				}
			}

			public void detach() {
			}
		});
		add(rg.add(new Radio<Long>("everybody", Model.of(1L)), new Radio<Long>("contact", Model.of(2L))
			, new Radio<Long>("nobody", Model.of(3L))).setOutputMarkupId(true).setRenderBodyOnly(false)
			);

		add(new TextArea<String>("userOffers"));
		add(new TextArea<String>("userSearchs"));
	}
	
	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
		return new PanelMarkupSourcingStrategy(false);
	}
}
