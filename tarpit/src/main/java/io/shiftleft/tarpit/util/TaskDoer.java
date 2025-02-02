package io.shiftleft.tarpit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TaskDoer {

    public static void doTask(String x, HttpServletResponse response) throws Exception {
        BufferedReader r = new BufferedReader(new FileReader(x));
        while ((x = r.readLine()) != null) {
            response.getWriter().println(x);
        }
    }
}