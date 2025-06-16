package itsm.itsm_backend.auth;

import itsm.itsm_backend.security.JwtService;
import itsm.itsm_backend.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserDetailsServiceImpl userDetailsService;

    private final JwtService jwtService;

    @GetMapping("/success")
    public void success(HttpServletResponse response, OAuth2AuthenticationToken auth) throws IOException {
        String username = auth.getPrincipal().getAttribute("username");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String jwt = jwtService.generateToken(userDetails);

        response.sendRedirect("http://localhost:4200/oauth2/callback?token=" + jwt);
    }
}