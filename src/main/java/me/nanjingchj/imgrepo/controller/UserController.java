package me.nanjingchj.imgrepo.controller;

import me.nanjingchj.imgrepo.dto.SessionStatusDto;
import me.nanjingchj.imgrepo.model.User;
import me.nanjingchj.imgrepo.repository.UserRepo;
import me.nanjingchj.imgrepo.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/login",  method = RequestMethod.PUT)
    public void createUserOrLogIn(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletResponse response) {
        String passwordHash = DigestUtils.sha256Hex(username + password);
        // check if user already exists
        // if user doesn't exist, sign up before logging in
        if (!userService.userExists(username)) {
            userService.createUser(username, passwordHash);
        }
        String token = userService.loginUser(username, passwordHash);
        if (token == null) {
            // wrong password
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.addCookie(userService.createSessionCookie(token));
    }

    @RequestMapping(value = "/status/{session-id}", method = RequestMethod.GET)
    public SessionStatusDto getSessionStatus(@PathVariable("session-id") String token, HttpServletResponse response) {
        SessionStatusDto status;
        if (userService.isUserLoggedInByToken(token)) {
            status = new SessionStatusDto(true, userService.getSessionExpiry(token));
        } else {
            status = new SessionStatusDto(false, null);
        }
        return status;
    }

}
