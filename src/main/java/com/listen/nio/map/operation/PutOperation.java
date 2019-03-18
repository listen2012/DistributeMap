/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.listen.nio.map.operation;

public class PutOperation extends BasicOperation {

  private static final long serialVersionUID = 1468270261208863692L;

  private String dataOldValue;

  public PutOperation(String key, String value) {
    super(key, value);

  }

  @Override
  public void run() {
    dataOldValue = MapManager.newInstance().get(dataKey) == null ? null
        : MapManager.newInstance().get(dataKey).toString();
    MapManager.newInstance().put(dataKey, dataValue);

  }

  public Object getResponse() {
    return dataOldValue;
  }

}
