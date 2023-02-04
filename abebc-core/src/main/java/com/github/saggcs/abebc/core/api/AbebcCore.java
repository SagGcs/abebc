package com.github.saggcs.abebc.core.api;

import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.saggcs.abebc.core.impl.BuildProvider;
import com.github.saggcs.abebc.core.impl.SessionProvider;
import com.github.saggcs.abebc.core.impl.Uploader;

public class AbebcCore {
	public static final Module MODULE = (b) -> {
		b.bind(SessionProvider.class).in(Scopes.SINGLETON);
		b.bind(BuildProvider.class).in(Scopes.SINGLETON);
		b.bind(Uploader.class).in(Scopes.SINGLETON);
	};
}
