### Event Materializer Sample


#### Description

Event materializer listens in a datasource (eg: MySQL, Kafka, etc.) where events are emitted
and materializes them to a in memory data structure (eg: map, list, etc.)


Eg:

```
Datasource: [UserCreatedEvent, UserActivatedEvent, UserJoinedPointsMembershipEvent, ...]  
Offset:             0               1                       2                         n

                        |
                        |
                        |
                    [EVENT MATERIALIZER]
                        |
                        |
                        
                 usersBuffer:[ userId:{user}, ... ]
                    
    

```

Offset should only moves forward as time goes by (events added, etc)


More info about event sourcing: [Nice presentation of event sourcing](https://ookami86.github.io/event-sourcing-in-practice/#title.md)

#### Abstractions:

```text
[DATA-BUFFER] <--(2)-- [DATA-BUILDER] <--(1)-- [DATA-SOURCE]
```

* (1) observes state-changes/events for specified datasource
* (2) updates contents of buffer with the ones received from datasource



#### How to run
* Execute: `docker-compose up`

* Execute: `mvn spring-boot:run`

* Visit: `http://localhost:1711/posts.xhtml`

* Execute (optional): `docker-compose down`