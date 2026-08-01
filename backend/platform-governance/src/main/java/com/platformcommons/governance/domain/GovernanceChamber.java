package com.platformcommons.governance.domain;

import lombok.Getter;

/**
 * 治理议院枚举。
 *
 * <p>对应宪章四院治理结构：劳动者议院、消费者议院、商户议院、公共议院。
 * 重大提案需多院分别表决。</p>
 */
@Getter
public enum GovernanceChamber {

    WORKER_CHAMBER("劳动者议院"),
    CONSUMER_CHAMBER("消费者议院"),
    MERCHANT_CHAMBER("商户议院"),
    PUBLIC_CHAMBER("公共议院");

    private final String displayName;

    GovernanceChamber(String displayName) {
        this.displayName = displayName;
    }
}
