/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.client.file.options;

import tachyon.annotation.PublicApi;
import tachyon.client.ClientContext;
import tachyon.conf.TachyonConf;

@PublicApi
public final class LoadMetadataOptions {
  public static class Builder {
    private boolean mRecursive;

    /**
     * Creates a new builder for {@link FreeOptions}.
     *
     * @param conf a Tachyon configuration
     */
    public Builder(TachyonConf conf) {
      mRecursive = false;
    }

    /**
     * @param recursive the recursive flag value to use; if the object whose metadata is to be
     *        loaded is a directory, the flag specifies whether metadata for the directory content
     *        should be loaded as well
     * @return the builder
     */
    public Builder setRecursive(boolean recursive) {
      mRecursive = recursive;
      return this;
    }

    /**
     * Builds a new instance of {@code LoadMetadataOptions}.
     *
     * @return a {@code LoadMetadataOptions} instance
     */
    public LoadMetadataOptions build() {
      return new LoadMetadataOptions(this);
    }
  }

  /**
   * @return the default {@code LoadMetadataOptions}
   */
  public static LoadMetadataOptions defaults() {
    return new Builder(ClientContext.getConf()).build();
  }

  private final boolean mRecursive;

  private LoadMetadataOptions(LoadMetadataOptions.Builder builder) {
    mRecursive = builder.mRecursive;
  }

  /**
   * @return the recursive flag value; if the object whose metadata is to be loaded is a directory,
   *         the flag specifies whether metadata for the directory content should be loaded as well
   */
  public boolean isRecursive() {
    return mRecursive;
  }
}
