package itsm.itsm_backend.email;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.user.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import org.thymeleaf.context.Context;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(String to,String username,EmailTemplateName emailTemplate
            ,String confirmationUrl,
                          String activationCode,
                          String subject) throws MessagingException, jakarta.mail.MessagingException {


        String templateName;
        if (emailTemplate==null){
            templateName ="confirm-email";
        }else {
            templateName=emailTemplate.name();
        }
        MimeMessage mimeMessage =mailSender.createMimeMessage();
        MimeMessageHelper helper =new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );
        Map<String,Object> properties=new HashMap<>();
        properties.put("username",username);
        properties.put("confirmationUrl",confirmationUrl);
        properties.put("activation_code",activationCode);
        Context context=new Context();
        context.setVariables(properties);
        helper.setFrom("contact@ihebConsulting.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName,context);
        helper.setText(template,true);
        mailSender.send(mimeMessage);



    }

    @Async
    public void sendEmailForTicketNotAssigned(User user, String username, EmailTemplateName emailTemplate
            , Ticket ticket,
                                              String subject) throws MessagingException, jakarta.mail.MessagingException {


        String templateName;
        if (emailTemplate==null){
            templateName ="confirm-email";
        }else {
            templateName=emailTemplate.name();
        }
        MimeMessage mimeMessage =mailSender.createMimeMessage();
        MimeMessageHelper helper =new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );
        Map<String,Object> properties=new HashMap<>();
        properties.put("username",username);
        properties.put("ticketId",ticket.getId());
        properties.put("createdAt",ticket.getCreatedDate());


       // properties.put("confirmationUrl",confirmationUrl);
       // properties.put("activation_code",activationCode);
        Context context=new Context();
        context.setVariables(properties);
        helper.setFrom("contact@ihebConsulting.com");
        helper.setTo(user.getEmail());
        helper.setSubject(subject);

        String template = templateEngine.process(templateName,context);
        helper.setText(template,true);
        mailSender.send(mimeMessage);



    }




}