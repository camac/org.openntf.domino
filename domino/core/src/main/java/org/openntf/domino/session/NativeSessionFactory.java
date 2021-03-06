package org.openntf.domino.session;

import org.openntf.domino.Session;

public class NativeSessionFactory extends AbstractSessionFactory {
	private static final long serialVersionUID = 1L;

	public NativeSessionFactory(final String apiPath) {
		super(apiPath);
	}

	@Override
	public Session createSession() {
		lotus.domino.Session raw = LotusSessionFactory.createSession();
		return wrapSession(raw, true);
	}
}
