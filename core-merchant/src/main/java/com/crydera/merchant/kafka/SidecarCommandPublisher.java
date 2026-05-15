package com.crydera.merchant.kafka;

import com.crydera.merchant.domain.Network;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SidecarCommandPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(Network network, String commandJson) {
        String topic = "sidecar.commands." + network.name().toLowerCase();
        
        kafkaTemplate.send(topic, commandJson);
        log.debug("sent command to {}", topic);
    }
}
