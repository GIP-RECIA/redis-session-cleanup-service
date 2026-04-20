/**
 * Copyright © 2026 GIP-RECIA (https://www.recia.fr/)
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
package fr.recia.redis.session.cleanup.model;

import lombok.Getter;

@Getter
public class RedisCleanupConfig {

    private final String indexPrefix;
    private final boolean cronEnabled;
    private final String cronExpression;

    public RedisCleanupConfig(String indexPrefix,
                             boolean cronEnabled, String cronExpression) {
        this.indexPrefix = indexPrefix;
        this.cronEnabled = cronEnabled;
        this.cronExpression = cronExpression;
    }

}