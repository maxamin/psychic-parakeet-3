package be.separate.csrf;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CsrfController {

    @GetMapping(path = "/get/attack")
    public @ResponseBody void createUser(HttpServletRequest request) {
        //request.getHeader("AUTH_DATA");
        request.getCookies();
    }

}
