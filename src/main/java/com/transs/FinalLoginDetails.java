package com.transs;

import java.util.List;

public class FinalLoginDetails {
    public List<BoardDetails> boardDetails;
    public String token;

    public FinalLoginDetails(String token, List<BoardDetails> boardDetails) {
        this.boardDetails = boardDetails;
        this.token = token;
    }
}
