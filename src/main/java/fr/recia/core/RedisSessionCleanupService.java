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
package fr.recia.core;

import fr.recia.model.RedisCleanupConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RedisSessionCleanupService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private final RedisCleanupConfig config;
    private final TaskScheduler taskScheduler;

    public RedisSessionCleanupService(RedisTemplate<String, Object> redisTemplate,
                                      FindByIndexNameSessionRepository<? extends Session> sessionRepository,
                                      RedisCleanupConfig config,
                                      TaskScheduler taskScheduler) {
        this.redisTemplate = redisTemplate;
        this.sessionRepository = sessionRepository;
        this.config = config;
        this.taskScheduler = taskScheduler;
        log.info("RedisSessionCleanupService setup");

        final String  couldNotGetHost = "Could not get host address";
        if (config.isCronEnabled()) {
            scheduleCleanup(config.getCronExpression());
            try {
                log.info("RedisSessionCleanupService is enabled for host {}", InetAddress.getLocalHost().getAddress());
            } catch (UnknownHostException e) {
                log.info("RedisSessionCleanupService is enabled");
                log.error(couldNotGetHost);
            }
        } else {
            try {
                log.info("RedisSessionCleanupService is disabled for host {}", InetAddress.getLocalHost().getAddress());
            } catch (UnknownHostException e) {
                log.info("RedisSessionCleanupService is disabled");
                log.error(couldNotGetHost);
            }
        }
    }

    public void scheduleCleanup(String cronExpression) {
        taskScheduler.schedule(this::runCleanup, new CronTrigger(cronExpression));
        log.info("CRedisSessionCleanupService scheduled cleanup task with cron expression : {}", cronExpression);
    }

    public void runCleanup() {
        log.info("RedisSessionCleanupService begin run cleanup");
        Set<String> principals = getAllPrincipals();
        log.debug("RedisSessionCleanupService principals : {} ", principals);
        for (String principal : principals) {
            cleanupPrincipal(principal);
        }
    }

    private void cleanupPrincipal(String principalName) {
        log.debug("RedisSessionCleanupService cleaning principal : {} ", principalName);
        String redisIndexKey = buildIndexKey(principalName);
        log.debug("RedisSessionCleanupService redisIndexKey : {}", redisIndexKey);

        Set<Object> rawValues = redisTemplate.opsForSet().members(redisIndexKey);
        log.debug("RedisSessionCleanupService raw values : {}", rawValues);

        assert rawValues != null;
        Set<String> sessionIds = rawValues.stream()
                .map(obj -> (String) obj)
                .collect(Collectors.toSet());
        log.debug("RedisSessionCleanupService sessionIds for principal {} : {}", principalName, sessionIds);

        Set<String> validSessionIds = new HashSet<>();
        for (String sessionId: sessionIds){
            Session session = sessionRepository.findById(sessionId);
            if( Objects.nonNull(session)) {
                log.debug("RedisSessionCleanupService session EXIST {}", sessionId);
                validSessionIds.add(sessionId);
            }else{
                log.debug("RedisSessionCleanupService session is ORPHAN {}", sessionId);
            }
        }

        // si le nombre de session valide est différent du nombre de session id trouvé pour cet utilisateur
        if(validSessionIds.size() != sessionIds.size()){
            // on supprime l'index
            redisTemplate.delete(redisIndexKey);
            // s'il reste encore des session valides, on réécrit l'index
            if (!validSessionIds.isEmpty()) {
                redisTemplate.opsForSet().add(
                        redisIndexKey,
                        validSessionIds.toArray()
                );
            }
        }
    }

    private String buildIndexKey(String principal) {
        return config.getIndexPrefix() + ":" +
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME +
                ":" + principal;
    }

    private String keyPattern(){
        return config.getIndexPrefix() + ":" + FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME + "*";
    }

    private Set<String> getAllPrincipals() {
        String pattern = keyPattern();
        log.debug("RedisSessionCleanupService getting principals with pattern : {}", pattern);
        Set<String> keys = redisTemplate.keys(pattern);
        log.info("RedisSessionCleanupService principal keys : {}", keys);
        return keys.stream()
                .map(k -> k.substring(k.lastIndexOf(":") + 1))
                .collect(Collectors.toSet());
    }
}