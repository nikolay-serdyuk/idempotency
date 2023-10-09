1. Just add the annotation @Idempotent to a class containing a listener or controller method. Then a couple annotations: @IdempotentResource on the method itself and @IdempotentParameter on its parameters, as shown below:
```
    @Component
    @Idempotent
    public class Consumer {
        @IdempotentResource(resourceId = "some_id")
        @KafkaListener(topics = "some_topic")
        public void listener(
            @IdempotentParameter ConsumerRecord<UUID, String> record, Acknowledgment ack 
        ) {
            ...
        }
    }
```
1.a In some cases, it can be sufficient to declare a special parameters that contains uuid:
```
        @KafkaListener(topics = "some_topic")
        public void listener(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) @IdempotentParameter Integer key
        ) {
            ...
        }
```

2. Declare a table that stores json-serialized responses for requests:
```
   create table if not exists idempotency
   (
       id varchar(32) not null,
       processed boolean,
       timestamp timestamp not null default now(),
       data jsonb null,
       constraint idempotency_pkey primary key (id)
   );
```

3. Configure idempotency with some properties in the application.yaml, setting the required hash algorithm:
```
   idempotency:
       enabled: true
       keyGenerator: MD5
```

All the magic has been done with BeanPostProcessor, which generates a special proxy. This proxy intercepts any invocation of the listener or controller method, examines its request, calculates a hash code, and matches the corresponding response (if it already has one).
The MD5 algorithm has good reliability against collisions; the probability is about 10^(-30). 
