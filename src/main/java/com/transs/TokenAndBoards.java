package com.transs;

import java.util.HashMap;
import java.util.Map;

public class TokenAndBoards {
    Map<String, String> namesAndIds = new HashMap<>();
    String token;

    public TokenAndBoards(Map<String, String> namesAndIds, String token) {
        this.namesAndIds = namesAndIds;
        this.token = token;
    }
}
