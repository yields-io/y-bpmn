package com.yields.yieldsbpmn.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
class TestMessage {
    private final String message;
    private final int identifier;

    public TestMessage(@JsonProperty("message") final String message,
                       @JsonProperty("identifier") final int identifier) {
        this.message = message;
        this.identifier = identifier;
    }
}
