/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet;

import com.innovattia.clases.accesos;
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
 * @author Innovattia
 */
public class canjear extends HttpServlet {

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
                line2 += line;
            }
            JSONObject json = new JSONObject(line2);
            if (json != null) {
                if (json.has("access")) {
                    //comercio, total, codigo, base64
                    String token = json.getString("access");
                    int idUsuario = entrar.validarAcceso(token);
                    if (idUsuario > 0) {
                        if (json.has("numero_ticket") && json.has("comercio") && json.has("total") && json.has("base64")) {
                            String ticket = json.getString("numero_ticket");
                            String comercio = json.getString("comercio");
                            String total = json.getString("total");
                            String base64 = json.getString("base64");
                            JSONObject respuestajson = entrar.getNumero(idUsuario);
                            String numero;
                            if (respuestajson.getInt("tipo") == 2) {
                                numero = respuestajson.getString("servicio");
                            } else {
                                numero = respuestajson.getString("numero");
                            }
                            if (!numero.isEmpty()) {
                                out.println(entrar.canjearTicket(idUsuario, ticket, numero, total, comercio, base64));
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Ocurrió un error. Favor de cerrar e iniciar una nueva sesión.");
                                out.println(respuesta);
                            }

                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Faltan propiedades a enviar.");
                            out.println(respuesta);
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "No se encontró el usuario asignado al acceso ingresado.");
                        out.println(respuesta);
                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Debe enviar su clave de acceso.");
                    out.println(respuesta);
                }

            } else {
                out.println(respuesta.put("success", false));
            }
        } catch (JSONException ex) {
            Logger.getLogger(canjear.class.getName()).log(Level.SEVERE, null, ex);
            try {
                out.println(respuesta.put("success", false));
            } catch (JSONException ex1) {
                Logger.getLogger(canjear.class.getName()).log(Level.SEVERE, null, ex1);
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
            Logger.getLogger(canjear.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(canjear.class.getName()).log(Level.SEVERE, null, ex);
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
