package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.rules.Rule;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flags payments involving blacklisted senders or receivers.
 *
 * Blacklist source is configurable for now (YAML list). In production this
 * would be backed by a database table + Redis cache + periodic refresh.
 *
 * Scoring:
 *   - sender blacklisted    -> 100 (immediate block)
 *   - receiver blacklisted  ->  90 (likely money mule)
 *   - both blacklisted      -> 100 (capped)
 */
@Component
public class BlacklistRule implements Rule {

    private static final Logger log = LoggerFactory.getLogger(BlacklistRule.class);

    private static final String RULE_NAME = "BLACKLIST";

    private final Set<String> configuredBlacklist;
    private Set<String> blacklist;

    public BlacklistRule(
        @Value("${rules.blacklist.entries:}") String entries) {
        this.configuredBlacklist = parseCsv(entries);
    }

    @PostConstruct
    void init() {
        this.blacklist = this.configuredBlacklist;
        log.info("BlacklistRule initialized with {} entries", blacklist.size());
    }

    @Override
    public String name() {
        return RULE_NAME;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        String senderId = context.payment().getSenderId();
        String receiverId = context.payment().getReceiverId();

        boolean senderHit = senderId != null && blacklist.contains(senderId);
        boolean receiverHit = receiverId != null && blacklist.contains(receiverId);

        if(senderHit && receiverHit) {
            return fire(100, String.format(
                "Both sender (%s) and receiver (%s) are blacklisted",
                senderId, receiverId
            ));
        }

        if (senderHit) {
            return fire(100, String.format(
                "Sender %s is blacklisted", senderId));
        }

        if (receiverHit) {
            return fire(90, String.format(
                "Receiver %s is blacklisted", receiverId));
        }

        return RuleResult.pass(RULE_NAME);
    }

    private RuleResult fire(int score, String reason) {
        log.warn("Blacklist rule fired: {}", reason);
        return RuleResult.hit(RULE_NAME, score, reason);
    }

    private static Set<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }
}
