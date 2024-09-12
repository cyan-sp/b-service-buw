/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet;

import com.innovattia.clases.encrypt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author 52556
 */
public class testrsa extends HttpServlet {

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
        String clave = "IdSsaOk$ABHzxweNm68TjtN59xKmKflXN^*yiXdBrpjZRJBzPzIdSsaOk$ABHzxweNm68TjtN59xKmKflXN^*yiXdBrpjZRJBzPz";
        String ruta_publica = "/opt/llaves/public/publico_pub.pem";
        String ruta_privada = "/opt/llaves/private/publico_prv.pem";

        File archivopub = new File(ruta_publica);
        File archivopriv = new File(ruta_privada);

        FileReader frpub = new FileReader(archivopub);
        BufferedReader br = new BufferedReader(frpub);
        String line;
        String llavepublica = "";
        while ((line = br.readLine()) != null) {
            llavepublica += line;
        }
        System.out.println("public " + llavepublica);

        FileReader frpri = new FileReader(archivopriv);
        br = new BufferedReader(frpri);
        String llaveprivada = "";
        while ((line = br.readLine()) != null) {
            llaveprivada += line;
        }
        System.out.println("priv "+llaveprivada);
        encrypt enc = new encrypt();
        String encriptado = enc.encrypt(clave, llavepublica);
        System.out.println("encryp" + encriptado);
        String desencriptado = enc.decrypt(new String(encriptado.getBytes()), llaveprivada);

        System.out.println("decrypt " + desencriptado);

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
            Logger.getLogger(testrsa.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(testrsa.class.getName()).log(Level.SEVERE, null, ex);
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
