package com.qendel.authenticationservice.registration;



import com.qendel.authenticationservice.email.EmailSender;
import com.qendel.authenticationservice.model.AppUser;
import com.qendel.authenticationservice.model.UserRole;
import com.qendel.authenticationservice.registration.token.ConfirmationToken;
import com.qendel.authenticationservice.registration.token.ConfirmationTokenService;
import com.qendel.authenticationservice.repository.AppUserRepository;
import com.qendel.authenticationservice.service.impl.AppUserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    @Autowired
    private final AppUserServiceImpl appUserService;
    @Autowired
    private final EmailValidator emailValidator;
    @Autowired
    private final ConfirmationTokenService confirmationTokenService;
    @Autowired
    private final EmailSender emailSender;
    @Autowired
    private final AppUserRepository appUserRepository;
//    @Autowired
//    private EmailSender emailSenderService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.
                test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }
        String role = request.getRole().toString();
        String token = null;
        if (role != null) {
            switch (role) {
                case "STUDENT":
                    token = appUserService.signUpUser(new AppUser(
                            request.getFirstName(), request.getLastName(),
                            request.getEmail(), request.getPassword(), UserRole.STUDENT));
                    break;
                case "TUTOR":
                    token = appUserService.signUpUser(new AppUser(
                            request.getFirstName(), request.getLastName(),
                            request.getEmail(), request.getPassword(), UserRole.TUTOR));
                    break;
                case "ADMIN":
                    token = appUserService.signUpUser(new AppUser(request.getFirstName(),
                            request.getLastName(), request.getEmail(),
                            request.getPassword(), UserRole.ADMIN));
                    break;
                default:
                    System.out.println("The role didn't match");
            }

        }else {
            System.out.println("empty role is not allowed");
        }

        String link = "http://localhost:8080/api/v1/registration/confirm?token=" + token;
//        emailSender.send(
//                request.getEmail(),
//                buildEmail(request.getFirstName(), link));

        emailSender.sendEMail(
                request.getEmail(),
                buildEmail(request.getFirstName(), link),"Confirm");

        return token;
    }
//    @EventListener(ApplicationReadyEvent.class)
//    public void sendEmail(){
//        emailSenderService.sendEMail("lumerry15@gmail.com",
//                "This is subject email",
//                "This is body email" );
//    }
//    public LoginResponse login(Login login) {
//        AppUser getEmail= userRepository.findByEmail(login.getEmail()).get();
//
//  if(getEmail!=null){
//      String password =login.getPassword();
//      String encodePassword= getEmail.getPassword();
//  Boolean isPwdRight= passwordEncoder.matches(password,encodePassword);
//  if (isPwdRight){
//      Optional<AppUser> user=userRepository.findByEmailAndPassword(login.getEmail(),login.getPassword());
//      if(user.isPresent()){
//          return  new LoginResponse("Login Sucess",true);
//      }else {
//          return new LoginResponse("Login failed",false);
//      }
//  }else {
//      return new LoginResponse("Password not Match",false);
//  }
//  }else {
//      return new LoginResponse("Email not exist",false);
//  }
//    }

   @Override
    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());
        return "Confirmed";
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }



}
