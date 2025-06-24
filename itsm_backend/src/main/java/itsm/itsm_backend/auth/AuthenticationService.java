package itsm.itsm_backend.auth;

import itsm.itsm_backend.email.EmailService;
import itsm.itsm_backend.email.EmailTemplateName;
import itsm.itsm_backend.security.JwtService;
import itsm.itsm_backend.user.Token;
import itsm.itsm_backend.user.TokenRepository;
import itsm.itsm_backend.user.User;
import itsm.itsm_backend.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    @Value("${application.mailing.frontend.activation_url}")
    private  String activationUrl;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegistrationRequest request) throws MessagingException {
        var user =
                User.builder()
                        .firstname(request.getFirstname())
                        .lastname(request.getLastname())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .accountLocked(false)
                        .enable(false)
                        .role(request.getRole())
                        .group(request.getGroup())
                        .build();
        userRepository.save(user);
        sendValidationEmail(user);

    }

    private void sendValidationEmail(User user) throws MessagingException {

        var newToken=generateAndSaveActivationToken(user);
        emailService.sendEmail(user.getEmail(), user.fullName(), EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken
                ,"Account activation");
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken=generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters="0123456789";
        StringBuilder codeBuilder=new StringBuilder();
        SecureRandom secureRandom=new SecureRandom();
        for (int i=0;i<length;i++){
            int randomIndex= secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims=new HashMap<String,Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName",user.fullName());
        var jwtToken= jwtService.generateToken(claims,user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken=tokenRepository.findByToken(token)
                .orElseThrow(()->new RuntimeException("Invalid Token"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired . A new token has been sent to the same email address");
        }
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(()->new UsernameNotFoundException("User not found"));
        user.setEnable(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    public void modifyPassword(Map<String, String> parameters) throws MessagingException {
        User user=
                this.userRepository.findByEmail(
                        (parameters.get("email"))).orElseThrow(()->new UsernameNotFoundException("User not found"));
        this.register(user);

    }

    public void register(User user) throws MessagingException {
        Token token=new Token();
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(60));
        Random random=new Random();
        int randomInteger= random.nextInt(999999);
        String code=String.format("%06d",randomInteger);
        token.setToken(code);
        this.tokenRepository.save(token);
        this.emailService.sendEmail(user.getEmail(), user.fullName(), EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                token.getToken()
                ,"Modifying password");
    }

    public Token lireEnFonctionDucode(String code){
        return    this.tokenRepository.findByToken(code).orElseThrow(()->new RuntimeException("Your token not valid"));
    }
    @Transactional
    public void nouveauMotDePasse(Map<String, String> parameters) {
        User user=this.userRepository.findByEmail(parameters.get("email")).orElseThrow(()->new UsernameNotFoundException("User not found"));
        final  Token token=lireEnFonctionDucode(parameters.get("code"));

        if (token.getUser().getEmail().equals(user.getEmail())) {
            String mdpCrypte = this.passwordEncoder.encode(parameters.get("password"));
            user.setPassword(mdpCrypte);
            this.userRepository.save(user);
        }
    }

    public User getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            String email = null;
            if (principal instanceof UserDetails) {
                email = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                email = principal.toString();
            }

            if (email != null) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        }

        return null;
    }
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void netterTable(){
        log.info("Netting Table already");
        this.tokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }

   //@Scheduled(cron = "0 */1 * * * *")
    /* @Transactional
    public void netterTableOfUsersDisabled(){
        log.info("Netting Table already");
        this.userRepository.deleteAllByCreatedDateBeforeAndEnable(LocalDateTime.now().plusHours(24),false);
    }*/
}
