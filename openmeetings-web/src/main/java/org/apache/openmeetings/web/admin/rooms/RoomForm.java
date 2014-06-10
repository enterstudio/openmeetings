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
package org.apache.openmeetings.web.admin.rooms;

import static org.apache.openmeetings.web.app.Application.getBean;
import static org.apache.openmeetings.web.app.WebSession.getSid;
import static org.apache.openmeetings.web.app.WebSession.getUserId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openmeetings.db.dao.room.RoomDao;
import org.apache.openmeetings.db.dao.room.RoomTypeDao;
import org.apache.openmeetings.db.dao.server.ISessionManager;
import org.apache.openmeetings.db.dao.user.IUserService;
import org.apache.openmeetings.db.dao.user.OrganisationDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.room.Client;
import org.apache.openmeetings.db.entity.room.Room;
import org.apache.openmeetings.db.entity.room.RoomModerator;
import org.apache.openmeetings.db.entity.room.RoomOrganisation;
import org.apache.openmeetings.db.entity.room.RoomType;
import org.apache.openmeetings.db.entity.user.Address;
import org.apache.openmeetings.db.entity.user.Organisation;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.web.admin.AdminBaseForm;
import org.apache.openmeetings.web.admin.AdminUserChoiceProvider;
import org.apache.openmeetings.web.app.Application;
import org.apache.openmeetings.web.app.WebSession;
import org.apache.openmeetings.web.common.ConfirmCallListener;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.Select2Choice;

public class RoomForm extends AdminBaseForm<Room> {
	private static final long serialVersionUID = 1L;
	private final static List<Long> DROPDOWN_NUMBER_OF_PARTICIPANTS = Arrays.asList(2L, 4L, 6L, 8L, 10L, 12L, 14L, 16L, 20L, 25L, 32L, 50L,
			100L, 150L, 200L, 500L, 1000L);
	private final WebMarkupContainer roomList;
	private final TextField<String> pin;
	private final WebMarkupContainer moderatorContainer = new WebMarkupContainer("moderatorContainer");
	private final WebMarkupContainer clientsContainer = new WebMarkupContainer("clientsContainer");
	private final ListView<Client> clients;
	private List<Client> clientsInRoom = null;
	private IModel<User> moderator2add = Model.of((User)null);
	
	public RoomForm(String id, WebMarkupContainer roomList, final Room room) {
		super(id, new CompoundPropertyModel<Room>(room));
		this.roomList = roomList;
		setOutputMarkupId(true);
		RequiredTextField<String> name = new RequiredTextField<String>("name");
		name.setLabel(new Model<String>(WebSession.getString(193)));
		add(name);

		add(new DropDownChoice<Long>("numberOfPartizipants", //
				DROPDOWN_NUMBER_OF_PARTICIPANTS, //
				new IChoiceRenderer<Long>() {
					private static final long serialVersionUID = 1L;
					public Object getDisplayValue(Long id) {
						return id;
					}
					public String getIdValue(Long id, int index) {
						return "" + id;
					}
				}));

		add(new DropDownChoice<RoomType>("roomtype", Application.getBean(RoomTypeDao.class).getAll(WebSession.getLanguage()),
				new ChoiceRenderer<RoomType>("label.value", "roomtypes_id")));

		add(new TextArea<String>("comment"));

		add(new CheckBox("appointment"));
		add(new CheckBox("ispublic"));

		List<Organisation> orgList = Application.getBean(OrganisationDao.class).get(0, Integer.MAX_VALUE);
		List<RoomOrganisation> orgRooms = new ArrayList<RoomOrganisation>(orgList.size());
		for (Organisation org : orgList) {
			orgRooms.add(new RoomOrganisation(org));
		}
		ListMultipleChoice<RoomOrganisation> orgChoiceList = new ListMultipleChoice<RoomOrganisation>(
				"roomOrganisations", orgRooms,
				new ChoiceRenderer<RoomOrganisation>("organisation.name",
						"organisation.organisation_id"));
		orgChoiceList.setMaxRows(6);
		add(orgChoiceList);

		add(new CheckBox("isDemoRoom"));
		TextField<Integer> demoTime = new TextField<Integer>("demoTime");
		demoTime.setLabel(new Model<String>(WebSession.getString(637)));
		add(demoTime);
		add(new CheckBox("allowUserQuestions"));
		add(new CheckBox("isAudioOnly"));
		add(new CheckBox("allowFontStyles"));
		add(new CheckBox("isClosed"));
		add(new TextField<String>("redirectURL"));
		add(new CheckBox("waitForRecording"));
		add(new CheckBox("allowRecording"));

		add(new CheckBox("hideTopBar"));
		add(new CheckBox("hideChat"));
		add(new CheckBox("hideActivitiesAndActions"));
		add(new CheckBox("hideFilesExplorer"));
		add(new CheckBox("hideActionsMenu"));
		add(new CheckBox("hideScreenSharing"));
		add(new CheckBox("hideWhiteboard"));
		add(new CheckBox("showMicrophoneStatus"));
		add(new CheckBox("chatModerated"));
		add(new CheckBox("chatOpened"));
		add(new CheckBox("filesOpened"));
		add(new CheckBox("autoVideoSelect"));	
		
		// Users in this Room 
		clients = new ListView<Client>("clients", clientsInRoom) {
			private static final long serialVersionUID = 8542589945574690054L;

			@Override
			protected void populateItem(final ListItem<Client> item) {
				Client client = item.getModelObject();
				item.add(new Label("clientId", "" + client.getId()))
					.add(new Label("clientLogin", "" + client.getUsername()))
					.add(new WebMarkupContainer("clientDelete").add(new AjaxEventBehavior("onclick"){
						private static final long serialVersionUID = 1L;
	
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmCallListener(833L));
						}
						
						@Override
						protected void onEvent(AjaxRequestTarget target) {
							Client c = item.getModelObject();
							getBean(IUserService.class).kickUserByStreamId(getSid(), c.getStreamid()
									, c.getServer() == null ? 0 : c.getServer().getId());
							
							updateClients(target);
						}
					}));
			}
		};
		add(clientsContainer.add(clients.setOutputMarkupId(true)).setOutputMarkupId(true));
		
		// Moderators
		final Select2Choice<User> moderatorChoice = new Select2Choice<User>("moderator2add", moderator2add, new AdminUserChoiceProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public void query(String term, int page, Response<User> response) {
				response.addAll(getBean(UserDao.class).get(term, false, page * PAGE_SIZE, PAGE_SIZE));
				response.setHasMore(PAGE_SIZE == response.getResults().size());
			}

			@Override
			protected String getDisplayText(User choice) {
				Address a = choice.getAdresses();
				return String.format("\"%s %s\" <%s>", choice.getFirstname(), choice.getLastname(), a == null ? "" : a.getEmail());
			}
		});
		add(moderatorChoice.add(new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				Room r = RoomForm.this.getModelObject();
				User u = moderator2add.getObject();
				boolean found = false;
				if (u != null) {
					for (RoomModerator rm : r.getModerators()) {
						if (rm.getUser().getUser_id().equals(u.getUser_id())) {
							found = true;
							break;
						}
					}
					if (!found) {
						RoomModerator rm = new RoomModerator();
						rm.setRoomId(r.getRooms_id());
						rm.setUser(u);
						r.getModerators().add(0, rm);
						moderator2add.setObject(null);
						target.add(moderatorContainer, moderatorChoice);
					}
				}
			}
		}).setOutputMarkupId(true));
		add(moderatorContainer.add(new ListView<RoomModerator>("moderators") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<RoomModerator> item) {
				RoomModerator moderator = item.getModelObject();
				Label name = new Label("uName", moderator.getUser().getFirstname() + " " + moderator.getUser().getLastname());
				if (moderator.getRoomModeratorsId() == 0) {
					name.add(AttributeAppender.append("class", "newItem"));
				}
				item.add(new CheckBox("isSuperModerator", new PropertyModel<Boolean>(moderator, "isSuperModerator")))
					.add(new Label("userId", "" + moderator.getUser().getUser_id()))
					.add(name)
					.add(new Label("email", moderator.getUser().getAdresses().getEmail()))
					.add(new WebMarkupContainer("delete").add(new AjaxEventBehavior("onclick"){
						private static final long serialVersionUID = 1L;
	
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmCallListener(833L));
						}
						
						@Override
						protected void onEvent(AjaxRequestTarget target) {
							RoomForm.this.getModelObject().getModerators().remove(item.getIndex());
							target.add(moderatorContainer);
						}
					}));
			}
		}).setOutputMarkupId(true));

        add(new CheckBox("isModeratedRoom"));

		add(new TextField<String>("confno").setEnabled(false));
		add(pin = new TextField<String>("pin"));
		pin.setEnabled(room.isSipEnabled());
		add(new TextField<String>("ownerId").setEnabled(false));
		add(new AjaxCheckBox("sipEnabled") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				updateView(target);
			}
		}.setOutputMarkupId(true));
		
		// attach an ajax validation behavior to all form component's keydown
		// event and throttle it down to once per second
		AjaxFormValidatingBehavior.addToAllFormComponents(this, "keydown", Duration.ONE_SECOND);
	}

	void updateClients(AjaxRequestTarget target) {
		long roomId = (getModelObject().getRooms_id() != null ? getModelObject().getRooms_id() : 0);  
		final List<Client> clientsInRoom = Application.getBean(ISessionManager.class).getClientListByRoom(roomId);
		clients.setDefaultModelObject(clientsInRoom);
		target.add(clientsContainer);
	}
	
	@Override
	protected void onSaveSubmit(AjaxRequestTarget target, Form<?> form) {
		Room r = getModelObject();
		boolean newRoom = r.getRooms_id() == null;
		r = getBean(RoomDao.class).update(r, getUserId());
		if (newRoom) {
			for (RoomModerator rm : r.getModerators()) {
				rm.setRoomId(r.getRooms_id());
			}
			// FIXME double update
			getBean(RoomDao.class).update(getModelObject(), getUserId());
		}
		hideNewRecord();
		updateView(target);
	}

	@Override
	protected void onSaveError(AjaxRequestTarget target, Form<?> form) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onNewSubmit(AjaxRequestTarget target, Form<?> form) {
		setModelObject(new Room());
		updateView(target);
	}

	@Override
	protected void onNewError(AjaxRequestTarget target, Form<?> form) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onRefreshSubmit(AjaxRequestTarget target, Form<?> form) {
		Room r = getModelObject();
		if (r.getRooms_id() != null) {
			r = getBean(RoomDao.class).get(r.getRooms_id());
		} else {
			r = new Room();
		}
		setModelObject(r);
		updateView(target);
	}

	@Override
	protected void onRefreshError(AjaxRequestTarget target, Form<?> form) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onDeleteSubmit(AjaxRequestTarget target, Form<?> form) {
		getBean(RoomDao.class).delete(getModelObject(), getUserId());
		target.add(roomList);
		setModelObject(new Room());
		updateView(target);
	}

	@Override
	protected void onDeleteError(AjaxRequestTarget target, Form<?> form) {
		// TODO Auto-generated method stub
	}

	public void updateView(AjaxRequestTarget target) {
		target.add(this);
		target.add(roomList);
		target.add(pin.setEnabled(getModelObject().isSipEnabled()));
		updateClients(target);
		target.appendJavaScript("omRoomPanelInit();");
	}
}