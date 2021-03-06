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

public class ClientCredentialsTest {

	private ClientCredentials target;

	@Before
	public void setUp() {
		target = new ClientCredentials();
		target.setClientCredentialFetcher(new ClientCredentialFetcherImpl());
	}

	@After
	public void tearDown() {
		target = null;
	}

	@Test
	public void testHandleRequestClientUserIdNotFound() throws Exception {
		Request request = createMock(Request.class);
		expect(request.getHeader("Authorization")).andReturn("Basic client1:null");
		expect(request.getParameter("client_id")).andReturn("clientId1");
		expect(request.getParameter("client_secret")).andReturn(null);
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
	public void testHandleRequestAuthInfoNotFound() throws Exception {
		Request request = createMock(Request.class);
		expect(request.getHeader("Authorization")).andReturn(null);
		expect(request.getParameter("client_id")).andReturn("authInfoNotFound");
		expect(request.getParameter("client_secret")).andReturn("clientSecret1");
		expect(request.getParameter("scope")).andReturn("scope1");
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
	public void testHandleRequestSimple() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("scope")).andReturn("scope1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					GrantHandlerResult result = event.get();
					assertEquals("Bearer", result.getTokenType());
					assertEquals("accessToken1", result.getAccessToken());
				} catch (OAuthError oAuthError) {
					fail(oAuthError.getMessage());
				}
			}
		});

	}

	@Test
	public void testHandleRequestFull() throws Exception {
		Request request = createRequestMock();
		expect(request.getParameter("scope")).andReturn("scope1");
		DataHandlerSync dataHandler = new MockDataHandler(request);
		replay(request);
		 target.handleRequest(dataHandler, new Handler<Try<OAuthError, GrantHandlerResult>>() {
			@Override
			public void handle(Try<OAuthError, GrantHandlerResult> event) {
				try {
					GrantHandlerResult result = event.get();
					assertEquals("Bearer", result.getTokenType());
					assertEquals("accessToken1", result.getAccessToken());
					assertEquals(900L, (long)result.getExpiresIn());
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

}
