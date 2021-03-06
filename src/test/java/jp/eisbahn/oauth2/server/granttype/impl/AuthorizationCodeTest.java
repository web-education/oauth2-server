/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package jp.eisbahn.oauth2.server.granttype.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jp.eisbahn.oauth2.server.async.Handler;
import jp.eisbahn.oauth2.server.data.DataHandlerSync;
import jp.eisbahn.oauth2.server.exceptions.Try;
import jp.eisbahn.oauth2.server.mock.MockDataHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jp.eisbahn.oauth2.server.exceptions.OAuthError;
import jp.eisbahn.oauth2.server.fetcher.clientcredential.ClientCredentialFetcherImpl;
import jp.eisbahn.oauth2.server.granttype.GrantHandler.GrantHandlerResult;
import jp.eisbahn.oauth2.server.models.Request;

public class AuthorizationCodeTest {

	private AuthorizationCode target;

	@Before
	public void setUp() {
		target = new AuthorizationCode();
		target.setClientCredentialFetcher(new ClientCredentialFetcherImpl());
	}

	@After
	public void tearDown() {
		target = null;
	}

	@Test
	public void testHandleRequestCodeNotFound() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn(null);
		DataHandlerSync dataHandler = createDataHandlerMock(request);
		replay(request, dataHandler);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.InvalidRequest not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestRedirectUriNotFound() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code1");
		expect(request.getParameter("redirect_uri")).andReturn(null);
		DataHandlerSync dataHandler = createDataHandlerMock(request);
		replay(request, dataHandler);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.InvalidRequest not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestAuthInfoNotFound() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("nullCode1");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.InvalidGrant not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestClientIdMismatch() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code2");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri2");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.InvalidClient not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestRedirectUriNotSet() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code2missingRedirect");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.RedirectUriMismatch not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestRedirectUriMismatch() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code2");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					event.get();
					fail("Error.RedirectUriMismatch not occurred.");
				} catch (OAuthError e) {
				}
			}
		});
	}

	@Test
	public void testHandleRequestSimple() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code1");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					GrantHandlerResult result = event.get();
					assertEquals("Bearer", result.getTokenType());
					assertEquals("accessToken1", result.getAccessToken());
					assertEquals(900L, (long) result.getExpiresIn());
					assertEquals("refreshToken1", result.getRefreshToken());
					assertEquals("scope1", result.getScope());
				} catch (OAuthError oAuthError) {
					fail(oAuthError.getMessage());
				}
			}
		});
	}

	@Test
	public void testHandleRequestFull() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("code")).andReturn("code1");
		expect(request.getParameter("redirect_uri")).andReturn("redirectUri1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					GrantHandlerResult result = event.get();
					assertEquals("Bearer", result.getTokenType());
					assertEquals("accessToken1", result.getAccessToken());
					assertEquals(900L, (long) result.getExpiresIn());
					assertEquals("refreshToken1", result.getRefreshToken());
					assertEquals("scope1", result.getScope());
				} catch (OAuthError oAuthError) {
					fail(oAuthError.getMessage());
				}
			}
		});
	}

	private Request createRequestMock() {
		Request request = createMock(Request.class);
		expect(request.getHeader("Authorization")).andReturn(null);
		expect(request.getParameter("client_id")).andReturn("clientId1");
		expect(request.getParameter("client_secret")).andReturn("clientSecret1");
		return request;
	}

	private DataHandlerSync createDataHandlerMock(Request request) {
		DataHandlerSync dataHandler = createMock(DataHandlerSync.class);
		expect(dataHandler.getRequest()).andReturn(request);
		return dataHandler;
	}

}
