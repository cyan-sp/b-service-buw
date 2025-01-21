/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet;

import com.innovattia.clases.accesos;
import com.innovattia.clases.bd;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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
public class accesoMifi extends HttpServlet {

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
            throws ServletException, IOException, ClassNotFoundException, SQLException, Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        accesos entrar = new accesos();
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
            bd bd = new bd();
            if (json != null) {
                String operacion = json.getString("operacion");
                String numero = null;
                if (json.has("numero") && !json.isNull("numero")) {
                    numero = json.getString("numero");
                }
                    if (operacion.equals("pin")) {
                        if (json.has("numero") && json.has("email") && json.has("servicio")) {
                            if (entrar.existeServicio(json.getString("servicio"), 3)) {
                                respuesta.put("success", false);
                                respuesta.put("code", 1);
                                out.println(respuesta);
                            } else {
                                JSONObject resultado = entrar.generarPINHBB(json.getString("servicio"), numero, json.getString("email"), 3, "registro");
                                if (resultado.getBoolean("success")) {
                                    out.println(respuesta.put("success", true));
                                } else {
                                    respuesta.put("success", false);
                                    respuesta.put("code", 1);
                                    out.println(respuesta);
                                }
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "La petición debe contener numero, email y servicio.");
                            out.println(respuesta);
                        }
                    } else if (operacion.equals("pinv2")) {
                        if (json.has("numero") && json.has("email") && json.has("servicio")) {
                            String servicio = json.getString("servicio");
                            if (numero.length() == 10 && servicio.length() == 10) {
                                if (entrar.existeServicio(servicio, 3)) {
                                    respuesta.put("success", false);
                                    respuesta.put("code", 1);
                                    out.println(respuesta);
                                } else {
                                    JSONObject resultado = entrar.generarPINHBBRestringido(servicio, numero, json.getString("email"), 3, "registro");
                                    if (resultado.getBoolean("success")) {
                                        out.println(respuesta.put("success", true));
                                    } else {
                                        respuesta = resultado;
                                        respuesta.put("code", 1);
                                        out.println(respuesta);
                                    }
                                }
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "El número celular o el número de servicio no son validos.");
                                out.println(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "La petición debe contener numero, email y servicio.");
                            out.println(respuesta);
                        }
                    } else if (operacion.equals("validar_pin")) {
                        if (json.has("numero") && json.has("pin")) {
                            if (numero.length() == 10 && json.getString("pin").length() == 4) {
                                int idUsuario = entrar.getIdUsuarioServicio(numero, 3);
                               // System.out.println("idusuario -> " + idUsuario);
                                JSONObject obj = entrar.getValidacionPin(idUsuario, json.getString("pin"));
                                respuesta = obj;
                                out.println(respuesta);
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Longitudes no validas");
                                out.println(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, su petición no cumple con los requerimientos.");
                            out.println(respuesta);
                        }
                    } else if (operacion.equals("login")) {
                        if (json.has("servicio") && json.has("pass")) {
                            out.print(entrar.loginHBB(json.getString("servicio"), json.getString("pass"), 3));
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "La petición debe contener servicio y pass.");
                            out.println(respuesta);
                        }
                    } else if (operacion.equals("registro_datos")) {
                        if (json.has("numero") && json.has("servicio") && json.has("pass") && json.has("permiso") && json.has("foto") && json.has("nombre")) {
                            out.println(entrar.insertarInformacionHBB(json.getString("numero"), json.getString("nombre"), json.getString("servicio"), json.getInt("permiso"), json.getString("pass"), json.getString("foto"), 3));
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Debe de enviar los siguientes datos: numero, servicio, nombre, pass, foto (base64) y permiso.");
                            out.print(respuesta);
                        }
                    } else if (operacion.equals("registro_datosv2")) {
                        if (json.has("numero") && json.has("servicio") && json.has("pass") && json.has("permiso") && json.has("foto") && json.has("nombre")) {
                            String servicio = json.getString("servicio");
                            String foto = json.getString("foto");
                            if (json.has("uuid")) {
                                String uuid = json.getString("uuid");
                                JSONObject idusuarioobj=bd.obteneridhbb(numero, servicio, 3);
                                int idUsuario = idusuarioobj.getInt("idUsuario");
                                JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                                if (obj.getBoolean("success")) {
                                    respuesta=entrar.insertarInformacionHBB(json.getString("numero"), json.getString("nombre"), servicio, json.getInt("permiso"), json.getString("pass"), foto, 3);
                                    respuesta.put("email", idusuarioobj.getString("email"));
                                    out.println(respuesta);
                                } else {
                                    out.println(obj);
                                }
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                                out.print(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            out.print(respuesta);
                        }
                    } else if (operacion.equals("recuperacion")) {
                        if (entrar.existeServicio(json.getString("servicio"), 3)) {
                            JSONObject resultado = entrar.generarPINHBB(json.getString("servicio"), json.getString("numero"), "", 3, "recuperacion");
                            out.println(resultado);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("code", 0);
                            respuesta.put("mensaje", "El número de servicio no se ha registrado.");
                            out.print(respuesta);
                        }
                    } else if (operacion.equals("recuperacionv2")) {
                        if (json.has("servicio")) {
                            String servicio = json.getString("servicio");
                            if (servicio.length() == 10) {
                                if (entrar.existeServicio(servicio, 3)) {
                                    JSONObject resultado = entrar.generarPINHBBRestringido(servicio, numero, "", 3, "recuperacion");
                                    out.println(resultado);
                                } else {
                                    respuesta.put("success", false);
                                    respuesta.put("code", 0);
                                    respuesta.put("mensaje", "El número de servicio no se ha registrado.");
                                    out.print(respuesta);
                                }
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                                out.print(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            out.print(respuesta);
                        }

                    } else if (operacion.equals("restablecer_pass")) {
                        if (json.has("servicio") && json.has("pass")) {
                            out.println(entrar.actualizarPassHBBv2(json.getString("servicio"), json.getString("pass"), 3));
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Debe de enviar los siguientes datos: servicio y pass.");
                            out.print(respuesta);
                        }
                    } else if (operacion.equals("restablecer_passv2")) {
                       // System.out.println("BugBounty " + line2);
                        if (json.has("servicio") && json.has("pass") && json.has("uuid")) {
                            String uuid = json.getString("uuid");
                            String servicio = json.getString("servicio");
                            if (servicio.length() == 10) {
                                JSONObject idusuarioobj=bd.obteneridhbb(numero, servicio, 3);
                                int idUsuario = idusuarioobj.getInt("idUsuario");
                                JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                                if (obj.getBoolean("success")) {
                                    respuesta = entrar.actualizarPassHBBv2(json.getString("servicio"), json.getString("pass"), 3);
                                } else {
                                    respuesta = obj;
                                }
                                out.println(respuesta);
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                                out.print(respuesta);
                            }

                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            out.print(respuesta);
                        }
                    } else if (operacion.equals("actualizacion")) {
                        if (json.has("access")) {
                            int idUsuario = entrar.validarAcceso(json.getString("access"));
                            if (idUsuario > 0) {
                                if (json.has("nombre")) {
                                    out.println(entrar.actualizarTopHBB(json.getString("numero"), json.getString("nombre"), json.getString("email"), json.getInt("permiso"), json.getString("foto"), null, null, idUsuario, 1, 3));
                                } else {

                                    if (json.has("actual_pass") && json.has("nueva_pass") && (json.getString("actual_pass") != json.getString("nueva_pass"))) {
                                        JSONObject respuestajson = entrar.getNumero(idUsuario);
                                        String msisdn;
                                        if (respuestajson.getInt("tipo") == 2 || respuestajson.getInt("tipo") == 3) {
                                            msisdn = respuestajson.getString("servicio");
                                        } else {
                                            msisdn = respuestajson.getString("numero");
                                        }
                                        JSONObject validarpass = entrar.adminpass(msisdn, json.getString("nueva_pass"), 3);
                                        if (validarpass.getBoolean("success")) {
                                            out.println(entrar.actualizarTopHBB(json.getString("numenumro"), null, null, 0, null, json.getString("actual_pass"), json.getString("nueva_pass"), idUsuario, 2, 3));

                                        } else {
                                            out.println(validarpass);
                                        }

                                    } else {
                                        respuesta.put("success", false);
                                        respuesta.put("mensaje", "Las propiedades actual_pass y nueva_pass no son correctos.");
                                        out.println(respuesta);
                                    }
                                }
                            } else {
                                respuesta.put("success", false);
                                out.println(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Es necesario enviar la propiedad access en ésta operación.");
                            out.println(respuesta);
                        }
                    } else if (operacion.equals("logout")) {
                        if (json.has("access")) {
                            int idUsuario = entrar.validarAcceso(json.getString("access"));
                            if (idUsuario > 0) {
                                respuesta.put("success", entrar.eliminarAcceso(idUsuario, json.getString("access")));
                                out.println(respuesta);
                            } else {
                                respuesta.put("success", false);
                                out.println(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Es necesario enviar la propiedad access en ésta operación.");
                            out.println(respuesta);
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "La operación ingresada no es reconocida.");
                        out.println(respuesta);
                    }
                
            } else {
                respuesta.put("success", false);
                out.println(respuesta);
            }

        } catch (JSONException ex) {
            Logger.getLogger(canjear.class.getName()).log(Level.SEVERE, null, ex);
            try {
                out.println(respuesta.put("success", false));
            } catch (JSONException ex1) {
                Logger.getLogger(operacioneslog.class.getName()).log(Level.SEVERE, null, ex1);
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
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accesoMifi.class.getName()).log(Level.SEVERE, null, ex);
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
