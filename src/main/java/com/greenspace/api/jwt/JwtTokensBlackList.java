package com.greenspace.api.jwt;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class JwtTokensBlackList {
    private final Set<String> blacklist = new HashSet<>();

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
