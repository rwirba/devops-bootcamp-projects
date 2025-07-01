package com.ezlearn.metrics;

import io.micrometer.prometheus.PrometheusMeterRegistry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrometheusMeterRegistry prometheusRegistry = (PrometheusMeterRegistry) getServletContext().getAttribute("prometheusRegistry");
        if (prometheusRegistry != null) {
            resp.setContentType("text/plain");
            resp.getWriter().write(prometheusRegistry.scrape());
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
