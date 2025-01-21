/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.servlet;

import com.innovattia.clases.accesos;
import com.innovattia.clases.bd;
import com.innovattia.clases.log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
public class acceso extends HttpServlet {

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
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json;charset=UTF-8");

        accesos entrar = new accesos();
        bd bd = new bd();
        PrintWriter out = response.getWriter();
        JSONObject respuesta = new JSONObject();
        String host = null;
        Map<String, String> result = new HashMap<>();
        boolean esApp = false;
        log log = new log();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            if (key.equals("origin") || key.equals("Origin")) {
                host = value;
            }
            result.put(key, value);
        }
        System.out.println("BugBounty Acceso por -> " + host);
        JSONObject acceso = entrar.getHost("origen", host);
        if (acceso.getBoolean("success")) {
            esApp = true;
        }
        System.out.println("BugBounty EsApp-> " + esApp);

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
                String operacion = json.getString("operacion");
                String numero = null;
                if (json.has("numero")) {
                    numero = json.getString("numero");
                }
                if (operacion.equals("pin_web")) {
                    //System.out.println("BugBounty " + line2);
                    System.out.println("numero -> "+numero);
                    if (entrar.registroCompleto(numero)) {
                        respuesta.put("success", false);
                        respuesta.put("code", 1);
                        out.println(respuesta);
                    } else {
                        boolean resultado = entrar.generarPIN(numero);
                        System.out.println("resultado -> "+ resultado);
                        if (resultado) {
                            entrar.insertarIntento(numero, 1);
                            out.println(respuesta.put("success", true));
                        } else {
                            respuesta.put("success", resultado);
                            respuesta.put("code", 1);
                            out.println(respuesta);
                        }
                    }
                    //System.out.println("BugBounty " + respuesta.toString());
                } else if (operacion.equals("pin_webv2")) {
                   // System.out.println("BugBounty " + line2);
                    if (entrar.registroCompleto(numero)) {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "El número ingresado ya cuenta con una cuentra creada.");
                        respuesta.put("code", 1);
                        out.println(respuesta);
                    } else {
                        JSONObject resultado = entrar.generarPINRestringido(numero);
                        out.println(resultado);
                    }
                    //System.out.println("BugBounty " + respuesta.toString());
                } else if (operacion.equals("pin_hbb")) {
                    //System.out.println("BugBounty " + line2);
                    if (json.has("numero") && json.has("email") && json.has("servicio")) {
                        if (json.getString("numero").length() == 10 && json.getString("servicio").length() == 10) {
                            if (entrar.existeServicio(json.getString("servicio"), 2)) {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Ya existe una cuenta creada para el servicio " + json.getString("servicio"));
                                respuesta.put("code", 1);
                                out.println(respuesta);
                            } else {
                                JSONObject resultado = entrar.generarPINHBB(json.getString("servicio"), numero, json.getString("email"), 2, "registro");
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
                            respuesta.put("mensaje", "El número celular o el número de servicio no son validos.");
                            out.println(respuesta);
                        }

                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "La petición debe contener numero, email y servicio.");
                        out.println(respuesta);
                    }
                    //System.out.println("BugBounty " + respuesta.toString());
                } else if (operacion.equals("pin_hbbv2")) {
                    //System.out.println("BugBounty " + line2);
                    if (json.has("numero") && json.has("email") && json.has("servicio")) {
                        if (json.getString("numero").length() == 10 && json.getString("servicio").length() == 10) {
                            if (entrar.existeServicio(json.getString("servicio"), 2)) {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Ya existe una cuenta creada para el servicio " + json.getString("servicio"));
                                respuesta.put("code", 1);
                                out.println(respuesta);
                            } else {
                                JSONObject resultado = entrar.generarPINHBBRestringido(json.getString("servicio"), numero, json.getString("email"), 2, "registro");
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
                    //System.out.println("BugBounty " + respuesta.toString());
                } else if (operacion.equals("validar_pin")) {
                    //System.out.println("BugBounty " + line2);
                    String pin = json.getString("pin");
                    if (pin.length() == 4 && numero.length() == 10) {
                        JSONObject objusuario=entrar.getIdUsuario(numero);
                        int idUsuario = objusuario.getInt("idusuario");
                        //System.out.println("idusuario -> " + idUsuario);
                        JSONObject obj = entrar.getValidacionPin(idUsuario, pin);
                        respuesta = obj;
                        out.println(respuesta);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Longitudes no validas");
                        out.println(respuesta);
                    }
                    //System.out.println("BugBounty " + respuesta.toString());
                } else if (operacion.equals("validar_pin_hbb")) {
                    if (json.has("numero") && json.has("pin")) {
                        if (numero.length() == 10 && json.getString("pin").length() == 4) {
                            int idUsuario = entrar.getIdUsuarioServicio(numero, 2);
                            //System.out.println("idusuario -> " + idUsuario);
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
                    String pass = json.getString("pass");
                    out.print(entrar.login(numero, pass));
                } else if (operacion.equals("login_hbb")) {
                    if (json.has("servicio") && json.has("pass")) {
                        out.print(entrar.loginHBB(json.getString("servicio"), json.getString("pass"), 2));
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "La petición debe contener servicio y pass.");
                        out.println(respuesta);
                    }
                } else if (operacion.equals("registro_datos")) {

                    String nombre = json.getString("nombre");
                    String email = json.getString("email");
                    int permiso = 5;
                    permiso = json.getInt("permiso");
                    String pass = json.getString("pass");
                    String foto = json.getString("foto");
                    if (esApp) {
                        JSONObject objusuario=entrar.getIdUsuario(numero);
                        int idUsuario = objusuario.getInt("idusuario");
                        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || permiso == 5) {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Debe de enviar los siguientes datos: Nombre, email, contraseña, foto (base64) y permiso.");
                            out.print(respuesta);
                        } else {
                            out.println(entrar.insertarInformacion(numero, nombre, email, permiso, pass, foto, idUsuario));
                        }
                    } else {
                        //System.out.println("BugBounty " + line2);
                        if (json.has("uuid")) {
                            String uuid = json.getString("uuid");
                            JSONObject objusuario = entrar.getIdUsuario(numero);
                            int idUsuario = objusuario.getInt("idusuario");
                            JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                            if (obj.getBoolean("success")) {
                                if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || permiso == 5) {
                                    respuesta.put("success", false);
                                    respuesta.put("mensaje", "Debe de enviar los siguientes datos: Nombre, email, contraseña, foto (base64) y permiso.");
                                } else {
                                    respuesta = entrar.insertarInformacion(numero, nombre, email, permiso, pass, foto, idUsuario);
                                }
                            } else {
                                respuesta = obj;
                            }
                            out.println(respuesta);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            respuesta.put("redireccion", true);
                            out.println(respuesta);
                        }
                        //System.out.println("BugBounty " + respuesta.toString());
                    }

                } else if (operacion.equals("registro_datosv2")) {

                    String nombre = json.getString("nombre");
                    String email = json.getString("email");
                    int permiso = 5;
                    permiso = json.getInt("permiso");
                    String pass = json.getString("pass");
                    String foto = json.getString("foto");

                    //System.out.println("BugBounty " + line2);
                    if (json.has("uuid")) {
                        String uuid = json.getString("uuid");
                        JSONObject objusuario = entrar.getIdUsuario(numero);
                        int idUsuario = objusuario.getInt("idusuario");
                        JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                        if (obj.getBoolean("success")) {
                            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || permiso == 5) {
                                respuesta.put("success", false);
                                respuesta.put("mensaje", "Debe de enviar los siguientes datos: Nombre, email, contraseña, foto (base64) y permiso.");
                            } else {
                                respuesta = entrar.insertarInformacion(numero, nombre, email, permiso, pass, foto, idUsuario);
                            }
                        } else {
                            respuesta = obj;
                        }
                        out.println(respuesta);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                        respuesta.put("redireccion", true);
                        out.println(respuesta);
                    }
                    //System.out.println("BugBounty " + respuesta.toString());

                } else if (operacion.equals("registro_datos_hbb")) {
                    if (json.has("numero") && json.has("servicio") && json.has("pass") && json.has("permiso") && json.has("foto") && json.has("nombre")) {
                        out.println(entrar.insertarInformacionHBB(numero, json.getString("nombre"), json.getString("servicio"), json.getInt("permiso"), json.getString("pass"), json.getString("foto"), 2));
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Debe de enviar los siguientes datos: numero, servicio, nombre, pass, foto (base64) y permiso.");
                        out.print(respuesta);
                    }
                } else if (operacion.equals("registro_datos_hbbv2")) {
                    if (json.has("numero") && json.has("servicio") && json.has("pass") && json.has("permiso") && json.has("foto") && json.has("nombre")) {
                        String servicio = json.getString("servicio");
                        String foto = json.getString("foto");
                        if (json.has("uuid")) {
                            String uuid = json.getString("uuid");
                            JSONObject idusuarioobj=bd.obteneridhbb(numero, servicio, 2);
                            int idUsuario = idusuarioobj.getInt("idUsuario");
                            JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                            if (obj.getBoolean("success")) {
                                respuesta = entrar.insertarInformacionHBB(numero, json.getString("nombre"), servicio, json.getInt("permiso"), json.getString("pass"), foto, 2);
                            } else {
                                respuesta = obj;
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            respuesta.put("redireccion", true);
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                    }
                    out.print(respuesta);
                } else if (operacion.equals("recuperacion")) {
                    if (entrar.existeNumero(numero)) {
                        boolean generacionPin = entrar.generarPIN(numero);
                        out.println(respuesta.put("success", generacionPin));
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("code", 0);
                        respuesta.put("mensaje", "El número telefónico no se ha registrado en la aplicación.");
                        out.print(respuesta);
                    }
                } else if (operacion.equals("recuperacionv2")) {
                    if (entrar.existeNumero(numero)) {
                        JSONObject generacionPin = entrar.generarPINRestringido(numero);
                        out.println(generacionPin);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("code", 0);
                        respuesta.put("mensaje", "El número telefónico no se ha registrado en la aplicación.");
                        out.print(respuesta);
                    }
                } else if (operacion.equals("recuperacion_hbb")) {
                    if (entrar.existeServicio(json.getString("servicio"), 2)) {
                        JSONObject resultado = entrar.generarPINHBB(json.getString("servicio"), numero, "", 2, "recuperacion");
                        if (resultado.getBoolean("success")) {
                            out.println(respuesta.put("success", true));
                        } else {
                            out.println(respuesta.put("success", false));
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("code", 0);
                        respuesta.put("mensaje", "El número de servicio no se ha registrado.");
                        out.print(respuesta);
                    }
                } else if (operacion.equals("recuperacion_hbbv2")) {
                    if (json.has("servicio")) {
                        String servicio = json.getString("servicio");
                        if (servicio.length() == 10 && numero.length() == 10) {
                            if (entrar.existeServicio(servicio, 2)) {
                                JSONObject resultado = entrar.generarPINHBBRestringido(servicio, numero, "", 2, "recuperacion");
                                if (resultado.getBoolean("success")) {
                                    out.println(respuesta.put("success", true));
                                } else {
                                    out.println(resultado);
                                }
                            } else {
                                respuesta.put("success", false);
                                respuesta.put("code", 0);
                                respuesta.put("mensaje", "El número de servicio no se ha registrado.");
                                out.print(respuesta);
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "El número celular o el número de servicio no son validos.");
                            out.println(respuesta);
                        }

                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, la petición es incorrecta.");
                        out.println(respuesta);
                    }

                } /*else if (operacion.equals("restablecer_pass")) {
                 out.println(entrar.actualizarPass(numero, json.getString("pass")));
                 } */ else if (operacion.equals("restablecer_passv2")) {
                    if (esApp) {
                        out.println(entrar.actualizarPassv2(numero, json.getString("pass")));
                    } else {
                       // System.out.println("BugBounty " + line2);
                        if (json.has("uuid")) {
                            String uuid = json.getString("uuid");
                            JSONObject objusuario = entrar.getIdUsuario(numero);
                            int idUsuario = objusuario.getInt("idusuario");
                            JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                            if (obj.getBoolean("success")) {
                                respuesta = entrar.actualizarPassv2(numero, json.getString("pass"));
                            } else {
                                respuesta = obj;
                            }
                            out.println(respuesta);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                            respuesta.put(("redireccion"), true);
                            out.println(respuesta);
                        }
                        //System.out.println("BugBounty " + respuesta.toString());
                    }

                } else if (operacion.equals("restablecer_pass")) {
                    //System.out.println("BugBounty " + line2);
                    if (json.has("uuid")) {
                        String uuid = json.getString("uuid");
                        JSONObject objusuario = entrar.getIdUsuario(numero);
                        int idUsuario = objusuario.getInt("idusuario");

                        JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                        if (obj.getBoolean("success")) {
                            respuesta = entrar.actualizarPassv2(numero, json.getString("pass"));
                        } else {
                            respuesta = obj;
                        }
                        out.println(respuesta);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                        respuesta.put(("redireccion"), true);
                        out.println(respuesta);
                    }
                    //System.out.println("BugBounty " + respuesta.toString());

                } else if (operacion.equals("restablecer_pass_hbb")) {
                    //System.out.println("BugBounty " + line2);
                    if (json.has("uuid")) {
                        String uuid = json.getString("uuid");
                        if (json.has("servicio") && json.has("pass")) {
                            String servicio = json.getString("servicio");
                            
                            JSONObject idusuarioobj=bd.obteneridhbb(numero, servicio, 2);
                            int idUsuario = idusuarioobj.getInt("idUsuario");
                            JSONObject obj = entrar.getAccesoPermitidoPIN(uuid, idUsuario);
                            if (obj.getBoolean("success")) {
                                respuesta = entrar.actualizarPassHBB(servicio, json.getString("pass"));
                            } else {
                                respuesta = obj;
                            }
                            out.println(respuesta);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Debe de enviar los siguientes datos: servicio y pass.");
                            out.print(respuesta);
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, la solicitud no esta permitida.");
                        respuesta.put(("redireccion"), true);
                        out.println(respuesta);
                    }
                } else if (operacion.equals("restablecer_pass_hbbv2")) {
                    if (json.has("servicio") && json.has("pass")) {
                        out.println(entrar.actualizarPassHBBv2(json.getString("servicio"), json.getString("pass"), 2));
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Debe de enviar los siguientes datos: servicio y pass.");
                        out.print(respuesta);
                    }
                } else if (operacion.equals("actualizacion")){
                    if (json.has("access")) {
                        String token = json.getString("access");
                        int idUsuario = entrar.validarAcceso(token);
                        if (idUsuario > 0) {
                            JSONObject respuestajson = entrar.getNumero(idUsuario);
                            String msisdn;
                            if (respuestajson.getInt("tipo") == 2) {
                                msisdn = respuestajson.getString("servicio");
                            } else {
                                msisdn = respuestajson.getString("numero");
                            }
                            if (json.has("nombre")) {
                                out.println(entrar.actualizarTop(msisdn, json.getString("nombre"), json.getString("email"), json.getInt("permiso"), json.getString("foto"), null, null, 1));
                            } else {
                                if (json.has("actual_pass") && json.has("nueva_pass")) {
                                    out.println(entrar.actualizarTop(msisdn, null, null, 0, null, json.getString("actual_pass"), json.getString("nueva_pass"), 2));

                                } else {
                                    respuesta.put("success", false);
                                    respuesta.put("mensaje", "Las propiedades actual_pass y nueva_pass no son correctos.");
                                    out.println(respuesta);
                                }
                            }
                        } else {
                            out.println(respuesta.put("success", false));
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Es necesario enviar la propiedad access en ésta operación.");
                        out.println(respuesta);
                    }

                } else if (operacion.equals("actualizacionv2")){
                    if (json.has("access")) {
                        String token = json.getString("access");
                        int idUsuario = entrar.validarAcceso(token);
                        if (idUsuario > 0) {
                            JSONObject respuestajson = entrar.getNumero(idUsuario);
                            String msisdn;
                            if (respuestajson.getInt("tipo") == 2) {
                                msisdn = respuestajson.getString("servicio");
                            } else {
                                msisdn = respuestajson.getString("numero");
                            }
                            if (json.has("nombre")) {
                                out.println(entrar.actualizarTop(msisdn, json.getString("nombre"), json.getString("email"), json.getInt("permiso"), json.getString("foto"), null, null, 1));
                            } else {
                                if (json.has("actual_pass") && json.has("nueva_pass") && (json.getString("actual_pass") != json.getString("nueva_pass"))) {
                                    JSONObject validarpass = entrar.adminpass(msisdn, json.getString("nueva_pass"), 1);
                                    if (validarpass.getBoolean("success")) {
                                        out.println(entrar.actualizarTop(msisdn, null, null, 0, null, json.getString("actual_pass"), json.getString("nueva_pass"), 2));
                                    } else {
                                        out.println(validarpass);
                                    }

                                } else {
                                    respuesta.put("success", false);
                                    respuesta.put("mensaje", "Las propiedades actual_pass y nueva_pass no son correctos. La nueva contraseña debe ser diferente a la actual.");
                                    out.println(respuesta);
                                }
                            }
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "No se ha encontrado un usuario registrado con el número ingresado.");
                            out.println(respuesta);
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Es necesario enviar la propiedad access en ésta operación.");
                        out.println(respuesta);
                    }

                } else if (operacion.equals("actualizacion_hbb")) {
                    if (json.has("access")) {
                        int idUsuario = entrar.validarAcceso(json.getString("access"));
                        if (idUsuario > 0) {
                            if (json.has("nombre")) {
                                out.println(entrar.actualizarTopHBB(json.getString("numero"), json.getString("nombre"), json.getString("email"), json.getInt("permiso"), json.getString("foto"), null, null, idUsuario, 1, 2));
                            } else {
                                if (json.has("actual_pass") && json.has("nueva_pass")) {
                                    out.println(entrar.actualizarTopHBB(json.getString("numero"), null, null, 0, null, json.getString("actual_pass"), json.getString("nueva_pass"), idUsuario, 2, 2));

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
                } else if (operacion.equals("actualizacion_hbbv2")) {
                    if (json.has("access")) {
                        int idUsuario = entrar.validarAcceso(json.getString("access"));
                        if (idUsuario > 0) {
                            if (json.has("nombre")) {
                                out.println(entrar.actualizarTopHBB(json.getString("numero"), json.getString("nombre"), json.getString("email"), json.getInt("permiso"), json.getString("foto"), null, null, idUsuario, 1, 2));
                            } else {

                                if (json.has("actual_pass") && json.has("nueva_pass") && (json.getString("actual_pass") != json.getString("nueva_pass"))) {
                                    JSONObject respuestajson = entrar.getNumero(idUsuario);
                                    String msisdn;
                                    if (respuestajson.getInt("tipo") == 2) {
                                        msisdn = respuestajson.getString("servicio");
                                    } else {
                                        msisdn = respuestajson.getString("numero");
                                    }
                                    JSONObject validarpass = entrar.adminpass(msisdn, json.getString("nueva_pass"), 2);
                                    if (validarpass.getBoolean("success")) {
                                        out.println(entrar.actualizarTopHBB(json.getString("numero"), null, null, 0, null, json.getString("actual_pass"), json.getString("nueva_pass"), idUsuario, 2, 2));

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
                Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex1);
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
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(acceso.class.getName()).log(Level.SEVERE, null, ex);
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
