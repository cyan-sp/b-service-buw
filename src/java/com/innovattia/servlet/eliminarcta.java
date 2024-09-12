/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet;

import com.innovattia.clases.accesos;
import com.innovattia.clases.altan;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 52556
 */
public class eliminarcta extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        PrintWriter out = response.getWriter();
        JSONObject respuesta = new JSONObject();
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.addHeader("Access-Control-Allow-Origin", "*");
            accesos entrar = new accesos();
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
            String line;
            String line2 = "";
            while ((line = br.readLine()) != null) {
                line = line.replace("\n", "").replace("\r", "");
               // System.out.println(line);
                line2 += line;
            }
            JSONObject json = new JSONObject(line2);

            if (json != null) {
                if (json.has("access") && json.has("pass")) {
                    String token = json.getString("access");
                    String pass = json.getString("pass");
                    if (token.isEmpty()) {
                        out.println(respuesta.put("success", false));
                    } else {
                        int idUsuario = entrar.validarAcceso(token);
                        if (idUsuario > 0) {
                            if (pass.equals(entrar.getPass(idUsuario))) {
                                boolean eliminar = entrar.eliminarCuenta(idUsuario);
                                out.println(respuesta.put("success", eliminar));
                            } else {
                                out.println(respuesta.put("success", false));
                            }
                        } else {
                            out.println(respuesta.put("success", false));
                        }
                    }
                } else {
                    out.println(respuesta.put("success", false));
                }
            } else {
                out.println(respuesta.put("success", false));
            }
        } catch (JSONException ex) {
            try {
                Logger.getLogger(eliminarcta.class.getName()).log(Level.SEVERE, null, ex);
                out.println(respuesta.put("success", false));
            } catch (JSONException ex1) {
                Logger.getLogger(eliminarcta.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(eliminarcta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(eliminarcta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
