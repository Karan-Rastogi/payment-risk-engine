package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.rules.Rule;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * Flags payments originating from high-risk geographies, or from
 * a country different from the sender's registered home country.
 *
 * Uses a configurable list of high-risk ISO country codes (FATF-style).
 * Real systems integrate with an IP-to-country lookup service.
 */
@Component
public class GeoAnomalyRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(GeoAnomalyRule.class);

    private static final String RULE_NAME = "GEO_ANOMALY";

    private final Set<String> highRiskCountries;
    private final int highRiskScore;
    private final int mismatchScore;

    public GeoAnomalyRule(
        @Value("${rules.geo.high-risk-countries:KP,IR,SY,CU}") Set<String> highRiskCountries,
        @Value("${rules.geo.high-risk-score:80}") int highRiskScore,
        @Value("${rules.geo.mismatch-score:40}") int mismatchScore) {
        this.highRiskCountries = highRiskCountries;
        this.highRiskScore = highRiskScore;
        this.mismatchScore = mismatchScore;
    }

    @Override
    public String name() {
        return RULE_NAME;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        String ipCountry = context.payment().getIpCountry();
        String homeCountry = context.homeCountry();

        if (ipCountry == null || ipCountry.isBlank()) {
            return RuleResult.pass(RULE_NAME);
        }

        String normalizedIpCountry = ipCountry.trim().toUpperCase();

        // Priority 1: High-risk country
        if (highRiskCountries.contains(normalizedIpCountry)) {
            String reason = String.format(
                "Transaction originated from high-risk country: %s",
                normalizedIpCountry);
            log.warn("Geo rule fired (high-risk): {}", reason);
            return RuleResult.hit(RULE_NAME, highRiskScore, reason);
        }

        // Priority 2: Country mismatch with home
        if (homeCountry != null && !homeCountry.isBlank()) {
            String normalizedHome = homeCountry.trim().toUpperCase();
            if (!normalizedIpCountry.equals(normalizedHome)) {
                String reason = String.format(
                    "Transaction origin (%s) differs from home country (%s)",
                    normalizedIpCountry, normalizedHome);
                log.warn("Geo rule fired (mismatch): {}", reason);
                return RuleResult.hit(RULE_NAME, mismatchScore, reason);
            }
        }

        return RuleResult.pass(RULE_NAME);
    }
}
