/*
 * Copyright 2023 The open source project at https://github.com/saggcs/abebc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.saggcs.abebc.core.api;

import com.github.jochenw.afw.core.util.HttpConnector;
import com.github.jochenw.afw.core.util.RestAccess;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.Scopes;

import io.github.saggcs.abebc.core.impl.BuildProvider;
import io.github.saggcs.abebc.core.impl.SessionProvider;
import io.github.saggcs.abebc.core.impl.Uploader;

public class AbebcCore {
	public static final Module MODULE = (b) -> {
		b.bind(SessionProvider.class).in(Scopes.SINGLETON);
		b.bind(BuildProvider.class).in(Scopes.SINGLETON);
		b.bind(Uploader.class).in(Scopes.SINGLETON);
		b.bind(RestAccess.class).in(Scopes.SINGLETON);
		b.bind(HttpConnector.class).in(Scopes.SINGLETON);
	};
}
