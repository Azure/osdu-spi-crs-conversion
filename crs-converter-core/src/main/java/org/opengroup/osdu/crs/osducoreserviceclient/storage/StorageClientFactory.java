// Copyright © 2022 Amazon Web Services
// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.crs.osducoreserviceclient.storage;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.storage.IStorageFactory;
import org.opengroup.osdu.core.common.storage.StorageAPIConfig;
import org.opengroup.osdu.core.common.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageClientFactory extends AbstractFactoryBean<IStorageFactory> {

	private final HttpResponseBodyMapper bodyMapper;

    @Value("${osdu.storage.url:null}")
	private String STORAGE_API;

	@Override
	public Class<?> getObjectType() {
		return IStorageFactory.class;
	}

	@Override
	protected IStorageFactory createInstance() throws Exception {
		return new StorageFactory(StorageAPIConfig
				.builder()
				.rootUrl(STORAGE_API)
				.build(),
				bodyMapper);
	}
}