/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet.nir;

import com.innovattia.clases.nir.otp;
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
public class cambioNir extends HttpServlet {

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
            throws ServletException, IOException, JSONException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");
        otp otp = new otp();
        PrintWriter out = response.getWriter();
        JSONObject respuesta = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
            String line;
            String line2 = "";
            while ((line = br.readLine()) != null) {
                line = line.replace("\n", "").replace("\r", "");
                line2 += line;
            }
            JSONObject json = new JSONObject(line2);
            if (json != null) {
                if (json.has("operacion")) {
                    String operacion = json.getString("operacion");
                    if (operacion.equals("generar_pin")) {
                        if (json.has("numero")) {
                            respuesta = otp.generarPIN(json.getString("numero"));
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Su petición no puede ser procesada.");
                        }
                    } else if (operacion.equals("validar_pin")) {
                        if (json.has("numero") && json.has("pin")) {
                            String numero = json.getString("numero");
                            String pin = json.getString("pin");
                            if (numero.length() == 10 && pin.length() == 6) {
                                respuesta = otp.getValidacionPin(numero, pin);
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Longitudes no validas");
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Su petición no puede ser procesada.");
                        }
                    } else if (operacion.equals("confirmacion_nir")) {
                        if (json.has("numero") && json.has("uuid") && json.has("nir")) {
                            String numero = json.getString("numero");
                            String nir = json.getString("nir");
                            if (numero.length() == 10 && nir.length() < 4) {
                                String uuid = json.getString("uuid");
                                JSONObject rsp = otp.getValidacionUUID(numero, uuid);
                                if (rsp.getBoolean("success")) {
                                    respuesta = otp.getIdNir(numero, nir);
                                } else {
                                    respuesta = rsp;
                                }

                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Longitudes no validas");
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Su petición no puede ser procesada.");
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Su petición no puede ser procesada.");
                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Su petición no puede ser procesada.");
                }
            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "Su petición no puede ser procesada.");
            }
        } catch (Exception e) {
            System.out.println("Error -> " + e);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Ha ocurrido un error interno.");

        }

        out.println(respuesta);
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
        } catch (JSONException ex) {
            Logger.getLogger(cambioNir.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (JSONException ex) {
            Logger.getLogger(cambioNir.class.getName()).log(Level.SEVERE, null, ex);
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
