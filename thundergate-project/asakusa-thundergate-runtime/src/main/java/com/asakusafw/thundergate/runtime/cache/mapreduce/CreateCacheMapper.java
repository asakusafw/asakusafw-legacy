/**
 * Copyright 2011-2021 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.thundergate.runtime.cache.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Logical deleted filter.
 * @since 0.2.3
 */
public class CreateCacheMapper extends Mapper<
        NullWritable, ThunderGateCacheSupport,
        NullWritable, ThunderGateCacheSupport> {

    private long invalidate;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.invalidate = Invalidation.getInvalidationTimestamp(context.getConfiguration());
    }

    @Override
    protected void map(
            NullWritable key,
            ThunderGateCacheSupport value,
            Context context) throws IOException, InterruptedException {
        if (value.__tgc__Deleted() == false && Invalidation.isStillValid(value, invalidate)) {
            context.write(key, value);
        }
    }
}
