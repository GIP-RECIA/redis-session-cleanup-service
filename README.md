# Redis Session Cleanup Service

Compatible pour un projet springboot version **2.7.18** (version supérieures non testées).

Cette librairie permet de nettoyer les sessions id stockés dans un index d'user dans Redis, pour les projets utilisant Redis comme session mapping storage.

## Paramètres 


### RedisCleanupConfig

| Nom            | Type    | Description                                                                                                    |
|----------------|---------|----------------------------------------------------------------------------------------------------------------|
| indexPrefix    | String  | Préfixe à concaténer *:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME* |
| cronEnabled    | Boolean | Détermine si le service est sensée tourner ou non                                                              |
| cronExpression | String  | Délai d'execution en syntaxe Cron                                                                              |

### RedisSessionCleanupService

| Nom               | Type                                                | Description                                                                                  |
|-------------------|-----------------------------------------------------|----------------------------------------------------------------------------------------------|
| redisTemplate     | RedisTemplate<String,Object>                        | Template Redis qui sera utilisé pour requêter le Redis                                       |
| sessionRepository | FindByIndexNameSessionRepository<? extends Session> | Repo des sessions, afin de vérifier si un id corresponds à une session encore valide nou non |
| config            | RedisCleanupConfig                                  | La configuration additionelle du service                                                     |
| taskScheduler     | TaskScheduler                                       | TaskScheduler qui gèrera l'execution du service                                              |

## Autres

### Déployer localement la librairie

`mvn clean install -DskipTests`

### Commandes pour notice et license
- `mvn notice:check`
- `mvn notice:generate`
- `mvn license:check`
- `mvn license:format`
- `mvn license:remove`
