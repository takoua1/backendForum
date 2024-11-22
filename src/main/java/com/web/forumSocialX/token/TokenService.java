package com.web.forumSocialX.token;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenService {
private final TokenRepository tokenRepository;
    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }
}
