package com.cwa.crud_springboot.service;

import com.cwa.crud_springboot.dto.JwtAuthenticationResponse;
import com.cwa.crud_springboot.exceptions.TokenRefreshException;
import com.cwa.crud_springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            // Extraction du username à partir du refresh token
            String username = tokenProvider.getUsernameFromJWT(refreshToken);

            // Charger les détails complets de l'utilisateur via UserRepository
            UserDetails userDetails = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            // Créer un objet Authentication à partir de UserDetails
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // Générer les nouveaux tokens (Access et Refresh) en utilisant Authentication
            String newAccessToken = tokenProvider.generateAccessToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            // Retourner la réponse contenant les nouveaux tokens
            return new JwtAuthenticationResponse(newAccessToken, newRefreshToken);
        }

        // Si le refresh token n'est pas valide, lancer une exception
        throw new TokenRefreshException(refreshToken, "Invalid refresh token");
    }
}