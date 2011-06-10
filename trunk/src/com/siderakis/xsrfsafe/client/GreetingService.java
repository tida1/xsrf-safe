package com.siderakis.xsrfsafe.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.XsrfProtect;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
@XsrfProtect
public interface GreetingService extends RemoteService {

	public static class Util {
		private static GreetingServiceAsync instance;

		public static GreetingServiceAsync getInstance() {
			if (instance == null) {
				instance = GWT.create(GreetingService.class);
				XsrfTokenServiceUtil.addToken((HasRpcToken) instance);
			}
			return instance;
		}
	}

	String greetServer(String name) throws IllegalArgumentException;
}
