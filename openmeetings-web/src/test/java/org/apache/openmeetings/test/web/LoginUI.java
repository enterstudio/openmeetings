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
package org.apache.openmeetings.test.web;

import static org.junit.Assert.assertTrue;

import org.apache.openmeetings.test.AbstractWicketTester;
import org.apache.openmeetings.web.app.Application;
import org.apache.openmeetings.web.app.WebSession;
import org.apache.openmeetings.web.pages.MainPage;
import org.apache.openmeetings.web.pages.auth.SignInPage;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

public class LoginUI extends AbstractWicketTester {
	@Test
	public void testValidLogin() {
		tester.startPage(MainPage.class);
		tester.assertRenderedPage(SignInPage.class);

		FormTester formTester = tester.newFormTester("signin:signin");
		formTester.setValue("login", username);
		formTester.setValue("pass", userpass);
		formTester.submit("submit");

		tester.assertNoErrorMessage();
		tester.assertRenderedPage(MainPage.class);
		WebSession ws = (WebSession)tester.getSession();
		assertTrue("Login should be successful", ws.isSignedIn());
	}

	@Test
	public void testEmptyLogin() {
		tester.startPage(SignInPage.class);
		tester.assertRenderedPage(SignInPage.class);

		FormTester formTester = tester.newFormTester("signin:signin");
		formTester.submit("submit");

		tester.assertErrorMessages(String.format("'%s' is required.", Application.getString(114))
				, String.format("'%s' is required.", Application.getString(115)));
	}
}
