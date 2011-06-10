package com.siderakis.xsrfsafe.client;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;

public class XsrfTokenServiceUtil {

	private static XsrfToken token;

	public static void addToken(final HasRpcToken remoteService) {
		if (token == null) {
			final Dictionary info = Dictionary.getDictionary("info");
			token = new XsrfToken(info.get("xsrf"));
		}
		remoteService.setRpcToken(token);

	}

}