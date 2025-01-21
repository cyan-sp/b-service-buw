/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Innovattia
 */
public class accesos {

    Connection conexion = null;
    Statement st;
    boolean agregado = false;

    public JSONObject login(String numero, String pass) throws JSONException, Exception {
        conexion conn = new conexion();
        boolean respuesta = false;
        JSONObject resultado = new JSONObject();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            
            try (CallableStatement statement = conexion.prepareCall("{call userLogin(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                statement.setString(1, Config.tk);
                statement.setString(2, numero);
                statement.setString(3, pass);
                statement.registerOutParameter(4, java.sql.Types.INTEGER);
                statement.registerOutParameter(5, java.sql.Types.VARCHAR);
                statement.registerOutParameter(6, java.sql.Types.VARCHAR);
                statement.registerOutParameter(7, java.sql.Types.VARCHAR);
                statement.registerOutParameter(8, java.sql.Types.VARCHAR);
                statement.registerOutParameter(9, java.sql.Types.INTEGER);
                statement.execute();
                
                idUsuario = statement.getInt(4);
                gsm = statement.getString(5);
                nombre = statement.getString(6);
                email = statement.getString(7);
                foto = statement.getString(8);
                permiso = statement.getInt(9);
                if (idUsuario > 0) {
                    respuesta = true;
                }
                statement.close();
                conexion.close();                
            }
            
            if (respuesta) {
                token token = new token();
                String acceso = token.generateNewToken();
                if (acceso.length() > 0) {
                    insertarToken(idUsuario, acceso);
                    resultado.put("success", true);
                    resultado.put("access", acceso);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", gsm);
                    resultado.put("foto", foto);
                    resultado.put("permiso", permiso);
                    return resultado;
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                    return resultado;
                }
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "El número y/o contraseña ingresados son incorrectos.");
                return resultado;
            }
            
            /*
            ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso from usuarios where numero='" + numero + "' and pass='" + pass + "' and tipo is null and activo=true;");
            while (rs.next()) {
                if (rs.getString("numero").length() > 0) {
                    idUsuario = rs.getInt("id");
                    gsm = rs.getString("numero");
                    nombre = rs.getString("nombre");
                    email = rs.getString("email");
                    foto = rs.getString("foto");
                    permiso = rs.getInt("permiso");
                    respuesta = true;
                } else {
                    respuesta = false;
                }
            }
            conexion.close();
            st.close();
            
            if (respuesta && idUsuario > 0) {
                token token = new token();
                String acceso = token.generateNewToken();
                if (acceso.length() > 0) {
                    insertarToken(idUsuario, acceso);
                    resultado.put("success", true);
                    resultado.put("access", acceso);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", gsm);
                    resultado.put("foto", foto);
                    resultado.put("permiso", permiso);
                    return resultado;
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                    return resultado;
                }
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "El número y/o contraseña ingresados son incorrectos.");
                return resultado;
            }
            */
            //return resultado;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject loginHBB(String servicio, String pass, int tipo) throws JSONException, Exception {
        conexion conn = new conexion();
        boolean respuesta = false;
        JSONObject resultado = new JSONObject();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            String storeProcedure = "userLoginHBB";
            if(tipo == 3){
                storeProcedure = "userLoginMifi";
            }
            System.out.println("storeProcedure -> "+storeProcedure);
            try (CallableStatement statement = conexion.prepareCall("{call "+ storeProcedure +"(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
                statement.setString(1, Config.tk);
                statement.setString(2, servicio);
                statement.setString(3, pass);
                statement.registerOutParameter(4, java.sql.Types.INTEGER);
                statement.registerOutParameter(5, java.sql.Types.VARCHAR);
                statement.registerOutParameter(6, java.sql.Types.VARCHAR);
                statement.registerOutParameter(7, java.sql.Types.VARCHAR);
                statement.registerOutParameter(8, java.sql.Types.VARCHAR);
                statement.registerOutParameter(9, java.sql.Types.INTEGER);
                statement.execute();
                idUsuario = statement.getInt(4);
                gsm = statement.getString(5);
                nombre = statement.getString(6);
                email = statement.getString(7);
                foto = statement.getString(8);
                permiso = statement.getInt(9);
                if (idUsuario > 0) {
                    respuesta = true;
                }
                statement.close();
                conexion.close();                
            }
            /*
            ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso from usuarios where servicio='" + servicio + "' and pass='" + pass + "' and tipo=" + tipo + " and activo=true;");
            while (rs.next()) {
                if (rs.getString("numero").length() > 0) {
                    idUsuario = rs.getInt("id");
                    gsm = rs.getString("numero");
                    nombre = rs.getString("nombre");
                    email = rs.getString("email");
                    foto = rs.getString("foto");
                    permiso = rs.getInt("permiso");
                    respuesta = true;
                } else {
                    respuesta = false;
                }
            }
            */
            conexion.close();
            st.close();
            if (respuesta && idUsuario > 0) {
                token token = new token();
                String acceso = token.generateNewToken();
                if (acceso.length() > 0) {
                    insertarToken(idUsuario, acceso);
                    resultado.put("success", true);
                    resultado.put("access", acceso);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", gsm);
                    resultado.put("foto", foto);
                    resultado.put("permiso", permiso);
                    resultado.put("servicio", servicio);
                    return resultado;
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                    return resultado;
                }
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "El usuario y/o contraseña son incorrectos.");
                return resultado;
            }
            //return resultado;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public boolean generarPIN(String numero) throws Exception {
        bd bd = new bd();
        if (numero.length() == 10) {
            conexion conn = new conexion();
            Random random = new Random();
            String pin = String.format("%04d", random.nextInt(10000));
            String gsm = "";
            int id = 0;

            while (pin.startsWith("0")) {
                pin = String.format("%04d", random.nextInt(10000));
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select id, numero from usuarios where numero='" + numero + "' and tipo is null and activo=true;");
                while (rs.next()) {
                    gsm = rs.getString("numero");
                    id = rs.getInt("id");
                }
                int idotp = 0;
                conexion.close();
                st.close();
                if (gsm.length() > 0) {
                    //el usuario existe
                    JSONObject rsp = bd.getidOtp(id);
                    idotp = rsp.getInt("otpusuario");

                    if (idotp == 0) {
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                        }
                    }
                    //System.out.println("el usuario existe");
                } else {
                   // System.out.println("El usuario no existe");
                    //el usuario no existe, hay que registrarlo
                    JSONObject obj = agregarUsuario(numero, pin);
                    if (obj.getBoolean("success")) {
                        id = obj.getInt("id");
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                        }
                    }
                }

                if (idotp > 0 && id > 0 && pin.length() == 4) {
                    UUID uuid = UUID.randomUUID();
                    if (bd.agregarOTP(idotp, pin, uuid + "", false)) {
                        enviarSMSPIN(numero, pin, "Bait", "Tu codigo de verificacion BAit es: " + pin);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            return false;
        }
    }

    public JSONObject generarPINRestringido(String numero) throws Exception {
        bd bd = new bd();
        JSONObject respuesta = new JSONObject();
        if (numero.length() == 10) {
            conexion conn = new conexion();
            Random random = new Random();
            String pin = String.format("%04d", random.nextInt(10000));
            String gsm = "";
            int id = 0;
            boolean envioPermitido = false;

            while (pin.startsWith("0")) {
                pin = String.format("%04d", random.nextInt(10000));
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select id, numero from usuarios where numero='" + numero + "' and tipo is null and activo=true;");
                while (rs.next()) {
                    gsm = rs.getString("numero");
                    id = rs.getInt("id");
                }
                int idotp = 0;
                conexion.close();
                st.close();
                if (gsm.length() > 0) {
                    //el usuario existe
                    JSONObject rsp = bd.getidOtp(id);
                    idotp = rsp.getInt("otpusuario");
                    envioPermitido = rsp.getBoolean("success");
                    if (idotp == 0) {
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                            envioPermitido = true;
                        }
                    } else {
                        if (!envioPermitido) {
                            respuesta.put("fechaDesbloqueo", rsp.getString("fechaDesbloqueo"));
                        }
                    }
                    //System.out.println("el usuario existe");
                } else {
                    //System.out.println("El usuario no existe");
                    //el usuario no existe, hay que registrarlo
                    JSONObject obj = agregarUsuario(numero, pin);
                    if (obj.getBoolean("success")) {
                        id = obj.getInt("id");
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                            envioPermitido = true;
                        }
                    }
                }

                if (idotp > 0 && id > 0 && pin.length() == 4 && envioPermitido) {
                    UUID uuid = UUID.randomUUID();
                    if (bd.agregarOTP(idotp, pin, uuid + "", false)) {
                        enviarSMSPIN(numero, pin, "Bait", "Tu codigo de verificacion BAit es: " + pin);
                        respuesta.put("success", true);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, ha ocurrido un error.");
                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Se han excedido los envíos de códigos");
                }
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                respuesta.put("success", false);
                respuesta.put("mensaje", "Lo sentimos, ha ocurrido un error.");
            }
        } else {
            respuesta.put("success", false);
            respuesta.put("mensaje", "El número ingresado no es válido");
        }
        return respuesta;
    }

    public JSONObject agregarUsuario(String numero, String pin) throws JSONException {
        //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            //id, numero, pin, nombre, email, pass, foto, permiso, fechaAlta, servicio, tipo, intentos, activo, sso_linked, sso_id, uuid, stepotp
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            
            int ok = 0;
            try (CallableStatement statement = conexion.prepareCall("{call insertarUsuario(?, ?, ?, ?, ?)}")) {
                statement.setString(1, Config.tk);
                statement.setString(2, numero);
                statement.setString(3, pin);
                statement.setTimestamp(4, date);
                statement.registerOutParameter(5, java.sql.Types.INTEGER);
                statement.executeUpdate();
                ok = statement.getInt(5);
                statement.close();
                conexion.close();                
            }
            /*
            String query = "insert into usuarios(numero,pin,fechaAlta) values(?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, numero);
            stmt.setString(2, pin);
            stmt.setTimestamp(3, date);
            int ok = stmt.executeUpdate();
            */
            
            if (ok > 0) {
                ejecutado = true;
                /*
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                   // System.out.println("ID usuario registrado = " + rs.getInt(1));
                    idtransaccion = rs.getInt(1);
                }
                */
                obj.put("success", ejecutado);
                obj.put("id", ok);
            } else {
                obj.put("success", false);
            }
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public JSONObject generarPINHBB(String servicio, String numero, String correo, int tipo, String metodo) throws JSONException, Exception {
        JSONObject respuesta = new JSONObject();
        boolean envioPermitido = false;
        bd bd = new bd();
        if (numero.length() == 10 && servicio.length() == 10) {
            conexion conn = new conexion();
            Random random = new Random();
            String pin = String.format("%04d", random.nextInt(10000));
            String gsm = "";
            int id = 0;
            int idotp = 0;
            while (pin.startsWith("0")) {
                pin = String.format("%04d", random.nextInt(10000));
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select id, numero from usuarios where servicio='" + servicio + "' and numero='" + numero + "' and tipo=" + tipo + " and activo=true;");
                while (rs.next()) {
                    gsm = rs.getString("numero");
                    id = rs.getInt("id");
                }
                conexion.close();
                st.close();
                if (gsm.length() > 0 && id > 0) {
                    //el usuario existe
                    //System.out.println("el usuario existe");
                    JSONObject rsp = bd.getidOtp(id);
                    idotp = rsp.getInt("otpusuario");
                    envioPermitido = rsp.getBoolean("success");
                    if (idotp == 0) {
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                        }
                    }

                } else {
                    //System.out.println("El usuario no existe");
                    if (pin.length() == 4 && metodo.equals("registro")) {
                        JSONObject obj = agregarUsuarioNoMBB(numero, servicio, pin, correo, tipo);
                        if (obj.getBoolean("success")) {
                            id = obj.getInt("id");
                            JSONObject addotp = bd.agregarIntentoOTP(id);
                            if (addotp.getBoolean("success")) {
                                idotp = addotp.getInt("id");
                            }
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "El número móvil ingresado no corresponde al registrado con su número de servicio de internet.");
                    }
                }

                if (idotp > 0 && id > 0 && pin.length() == 4) {
                    UUID uuid = UUID.randomUUID();
                    if (bd.agregarOTP(idotp, pin, uuid + "", false)) {
                        enviarSMSPIN(numero, pin, "93000", "Tu codigo de verificacion BAit es: " + pin);
                        respuesta.put("success", true);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, ocurrió un error en su proceso de registro.");
                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Lo sentimos, ocurrió un error en su proceso de registro.");
                }

                return respuesta;

            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                respuesta.put("success", false);
                respuesta.put("mensaje", "Ha ocurrido un error interno. Favor de intentarlo en unos minutos.");
                return respuesta;
            }
        } else {
            respuesta.put("success", false);
            respuesta.put("mensaje", "El formato de número celular y/o número de servicio no son correctos");
            return respuesta;
        }

    }

    public JSONObject generarPINHBBRestringido(String servicio, String numero, String correo, int tipo, String metodo) throws JSONException, Exception {
        JSONObject respuesta = new JSONObject();
        boolean envioPermitido = false;
        bd bd = new bd();
        if (numero.length() == 10 && servicio.length() == 10) {
            conexion conn = new conexion();
            Random random = new Random();
            String pin = String.format("%04d", random.nextInt(10000));
            String gsm = "";
            int id = 0;
            int idotp = 0;
            while (pin.startsWith("0")) {
                pin = String.format("%04d", random.nextInt(10000));
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select id, numero from usuarios where servicio='" + servicio + "' and numero='" + numero + "' and tipo=" + tipo + " and activo=true;");
                while (rs.next()) {
                    gsm = rs.getString("numero");
                    id = rs.getInt("id");
                }
                conexion.close();
                st.close();
                if (gsm.length() > 0 && id > 0) {
                    //el usuario existe
                   // System.out.println("el usuario existe");
                    JSONObject rsp = bd.getidOtp(id);
                    idotp = rsp.getInt("otpusuario");
                    envioPermitido = rsp.getBoolean("success");
                    if (idotp == 0) {
                        JSONObject addotp = bd.agregarIntentoOTP(id);
                        if (addotp.getBoolean("success")) {
                            idotp = addotp.getInt("id");
                            envioPermitido = true;
                        }
                    } else {
                        if (!envioPermitido) {
                            respuesta.put("fechaDesbloqueo", rsp.getString("fechaDesbloqueo"));
                        }
                    }
                } else {
                    //System.out.println("El usuario no existe");
                    if (pin.length() == 4 && metodo.equals("registro")) {
                        JSONObject obj = agregarUsuarioNoMBB(numero, servicio, pin, correo, tipo);
                        if (obj.getBoolean("success")) {
                            id = obj.getInt("id");
                            JSONObject addotp = bd.agregarIntentoOTP(id);
                            if (addotp.getBoolean("success")) {
                                idotp = addotp.getInt("id");
                                envioPermitido = true;
                            }
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "El número móvil ingresado no corresponde al registrado con su número de servicio de internet.");
                    }
                }

                if (idotp > 0 && id > 0 && pin.length() == 4 && envioPermitido) {
                    UUID uuid = UUID.randomUUID();
                    if (bd.agregarOTP(idotp, pin, uuid + "", false)) {
                        enviarSMSPIN(numero, pin, "93000", "Tu codigo de verificacion BAit es: " + pin);
                        respuesta.put("success", true);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, ocurrió un error en su proceso de registro.");
                    }
                } else if (idotp == 0) {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "El número móvil ingresado no corresponde al registrado con su número de servicio de internet.");

                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Se han excedido los envíos de códigos");
                }

                return respuesta;

            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                respuesta.put("success", false);
                respuesta.put("mensaje", "Ha ocurrido un error interno. Favor de intentarlo en unos minutos.");
                return respuesta;
            }
        } else {
            respuesta.put("success", false);
            respuesta.put("mensaje", "El formato de número celular y/o número de servicio no son correctos");
            return respuesta;
        }

    }

    public JSONObject agregarUsuarioNoMBB(String numero, String servicio, String pin, String email, int tipo) throws JSONException {
        //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
        // st.executeUpdate("insert into usuarios(numero,pin,fechaAlta,email,servicio,tipo,intentos) values('" + numero + "'," + pin + ",now(),'" + correo + "','" + servicio + "'," + tipo + ",0);");

        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            /*
            String query = "insert into usuarios(numero,pin,fechaAlta,email,servicio,tipo) values(?,?,?,?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, numero);
            stmt.setString(2, pin);
            stmt.setTimestamp(3, date);
            stmt.setString(4, email);
            stmt.setString(5, servicio);
            stmt.setInt(6, tipo);

            int ok = stmt.executeUpdate();
            */
            
            int ok = 0;
            try (CallableStatement statement = conexion.prepareCall("{call insertarUsuarioNoMBB(?, ?, ?, ?, ?, ?, ?, ?)}")) {
                statement.setString(1, Config.tk);
                statement.setString(2, numero);
                statement.setString(3, pin);
                statement.setTimestamp(4, date);
                statement.setString(5, email);
                statement.setString(6, servicio);
                statement.setInt(7, tipo);
                statement.registerOutParameter(8, java.sql.Types.INTEGER);
                statement.executeUpdate();
                ok = statement.getInt(8);
                statement.close();
                conexion.close();                
            }

            if (ok > 0) {
                ejecutado = true;
                /*
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                   // System.out.println("ID usuario registrado = " + rs.getInt(1));
                    idtransaccion = rs.getInt(1);
                }
                */
                obj.put("success", ejecutado);
                obj.put("id", ok);
            } else {
                obj.put("success", false);
            }
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public boolean insertarToken(int idusuario, String acceso) throws Exception {
        conexion conn = new conexion();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            if (acceso.length() > 0) {
                st.executeUpdate("insert into accesos(id_usuario,acceso,fecha) values(" + idusuario + ",'" + acceso + "',now());");
            }
            conexion.close();
            st.close();
            return true;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public JSONObject validarNumero(String numero, String pin) throws JSONException, Exception {
        conexion conn = new conexion();
        JSONObject obj = new JSONObject();
        ResultSet rs;
        int intento = 0;
        boolean respuesta = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            rs = st.executeQuery("select numero, intentos from usuarios where numero='" + numero + "' and pin='" + pin + "' and tipo is null and activo=true;");
            while (rs.next()) {
                if (rs.getString("numero").length() > 0) {
                    respuesta = true;
                    intento = rs.getInt("intentos");
                }
            }
            if (!respuesta) {
                rs = st.executeQuery("select numero, intentos from usuarios where numero='" + numero + "' and tipo is null and activo=true;");
                while (rs.next()) {
                    intento = rs.getInt("intentos");
                }
                respuesta = false;
            }
            conexion.close();
            st.close();
            insertarIntento(numero, 1);
            obj.put("respuesta", respuesta);
            obj.put("intento", intento);
            return obj;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("respuesta", respuesta);
            obj.put("intento", intento);
            return obj;
        }
    }

    public void insertarIntento(String numero, int tipo) throws ClassNotFoundException, SQLException, Exception {
        conexion conn = new conexion();
        String consulta;
        if (tipo == 1) {
            consulta = "update usuarios set intentos=intentos+1 where numero='" + numero + "' and tipo is null and activo=true;";
        } else if (tipo == 2) {
            consulta = "update usuarios set intentos=intentos+1 where numero='" + numero + "' and tipo=2 and activo=true;";
        } else if (tipo == 3) {
            consulta = "update usuarios set intentos=intentos+1 where numero='" + numero + "' and tipo=3 and activo=true;";
        } else if (tipo == 11) {
            consulta = "update usuarios set intentos=0,pin=null where numero='" + numero + "' and tipo is null and activo=true;";
        } else if (tipo == 12) {
            consulta = "update usuarios set intentos=0,pin=null where numero='" + numero + "' and tipo=2 and activo=true;";
        } else {
            consulta = "update usuarios set intentos=0,pin=null where numero='" + numero + "' and tipo=3 and activo=true;";
        }
        Class.forName("com.mysql.jdbc.Driver");
        conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
        Statement st = conexion.createStatement();
        st.executeUpdate(consulta);
        conexion.close();
        st.close();
    }

    public JSONObject validarNumeroHBB(String numero, String pin, int tipo) throws JSONException, Exception {
        conexion conn = new conexion();
        JSONObject obj = new JSONObject();
        boolean respuesta = false;
        int intento = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            if (pin.length() == 4) {
                ResultSet rs = st.executeQuery("select numero, intentos, pin from usuarios where numero='" + numero + "' and tipo=" + tipo + " and activo=true;");
                while (rs.next()) {
                    if (rs.getString("numero").length() > 0 && pin.equals(Integer.toString(rs.getInt("pin"))) && rs.getInt("intentos") < 49) {
                        respuesta = true;
                        intento = rs.getInt("intentos");
                    } else if (rs.getString("numero").length() > 0) {
                        if (rs.getInt("pin") <= 0) {
                            respuesta = false;
                            intento = 100;
                        } else {
                            respuesta = false;
                            intento = rs.getInt("intentos");
                        }
                    } else {
                        respuesta = false;
                    }
                }
            } else {
                respuesta = false;
            }
            conexion.close();
            st.close();
            insertarIntento(numero, tipo);
            obj.put("respuesta", respuesta);
            obj.put("intento", intento);
            return obj;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("respuesta", respuesta);
            obj.put("intento", intento);
            return obj;
        }
    }

    public int validarAcceso(String acceso) throws Exception {
        conexion conn = new conexion();
        int respuesta = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            ResultSet rs = st.executeQuery("select id_usuario from accesos where acceso='" + acceso + "';");
            while (rs.next()) {
                respuesta = rs.getInt("id_usuario");
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public JSONObject getIdUsuario(String numero) throws JSONException {
        JSONObject obj = new JSONObject();
        int idusuario = 0;
        String email = "";
        try {
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select id, numero, email from usuarios where numero='" + numero + "' and tipo is null and activo=true;");
            while (rs.next()) {
                idusuario = rs.getInt("id");
                email = rs.getString("email");
            }
            conexion.close();
            st.close();

        } catch (Exception e) {
            System.out.println("Error -> " + e);
        }
        obj.put("idusuario", idusuario);
        obj.put("email", email);

        return obj;
    }

    public int getIdUsuarioServicio(String numero, int tipo) {
        int idusuario = 0;
        try {
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select id, numero from usuarios where numero='" + numero + "' and tipo=" + tipo + " and activo=true;");
            while (rs.next()) {
                idusuario = rs.getInt("id");
            }
            conexion.close();
            st.close();

        } catch (Exception e) {
            System.out.println("Error -> " + e);
        }

        return idusuario;
    }

    public JSONObject getValidacionPin(int idusuario, String pin) throws Exception {
        conexion conn = new conexion();
        Calendar ahora = Calendar.getInstance();
        Date exp = null;
        boolean existe = false;
        boolean valido = false;
        String pinRegistrado = null;
        int intentos = 0;
        int idotp = 0;
        String uuid = null;
        int maximo = 0;
        JSONObject respuesta = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "select id, uuid, intentos, maximo, pin, fechaCreacion from validacionOTP where id_usuarios=?";
            stmt = conexion.prepareStatement(query);
            stmt.setInt(1, idusuario);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existe = true;
                exp = rs.getTimestamp("fechaCreacion");
                pinRegistrado = rs.getString("pin");
                intentos = rs.getInt("intentos");
                idotp = rs.getInt("id");
                uuid = rs.getString("uuid");
                maximo = rs.getInt("maximo");
            }

            stmt.close();
            conexion.close();
            if (existe) {
               // System.out.println("Fecha Expiracion -> " + exp);
                Calendar expiracion = Calendar.getInstance();
                expiracion.setTime(exp);
                long milliseconds1 = expiracion.getTimeInMillis();
                long milliseconds2 = ahora.getTimeInMillis();
                long diff = milliseconds2 - milliseconds1;
                long diffMinutes = diff / (60 * 1000);
               // System.out.println("Time in minutes: " + diffMinutes + " minutes.");

                if (diffMinutes <= 15) {
                    if ((intentos < maximo)) {
                        if (pinRegistrado.equals(pin)) {
                            valido = true;
                            respuesta.put("success", true);
                            respuesta.put("uuid", uuid);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, el PIN ingresado no es válido.");
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, has superado los intentos máximos de validaciones de OTP");
                        respuesta.put("redireccion", true);
                    }
                    addIntentoOtp(idotp, valido);
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Lo sentimos, tu PIN esta expirado, por favor solicita uno nuevo");
                    respuesta.put("redireccion", true);
                }
            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "Lo sentimos, debes de solicitar un PIN para poder realizar la validación");
                respuesta.put("redireccion", true);
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Error interno");
        }

        return respuesta;
    }

    public JSONObject getAccesoPermitidoPIN(String uuid, int usuario) throws Exception {
        conexion conn = new conexion();
        Calendar ahora = Calendar.getInstance();
        Date exp = null;
        boolean existe = false;
        boolean valido = false;
        int idusuario = 0;
        int idotp = 0;
        int maximo = 0;
        JSONObject respuesta = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "select id,id_usuarios, uuid, valido from validacionOTP where uuid=?";
            stmt = conexion.prepareStatement(query);
            stmt.setString(1, uuid);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existe = true;
                idusuario = rs.getInt("id_usuarios");
                valido = rs.getBoolean("valido");
                idotp = rs.getInt("id");
            }

            stmt.close();
            conexion.close();
            if (existe) {
                if (idusuario == usuario) {
                    if (valido) {
                        respuesta.put("success", true);
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, tu proceso de verificación de usuario no esta completa.");
                        respuesta.put("redireccion", true);

                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Lo sentimos, la información enviada no corresponde a tu bait.");
                    respuesta.put("redireccion", true);
                }

            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "Lo sentimos, tu solicitud no esta permitida.");
                respuesta.put("redireccion", true);
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Error interno");
            respuesta.put("redireccion", true);
        }
        return respuesta;
    }

    public void addIntentoOtp(int id, boolean valido) {

        try {
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "update validacionOTP set valido=?, intentos=intentos+1 where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setBoolean(1, valido);
            stmt.setInt(2, id);

            int total = stmt.executeUpdate();
            //System.out.println("Ejecutado -> " + total);
        } catch (Exception e) {
            System.out.println("Error -> " + e);
        }

    }

    public String getPass(int idUsuario) throws Exception {
        conexion conn = new conexion();
        String respuesta = "";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            ResultSet rs = st.executeQuery("select pass from usuarios where id=" + idUsuario + ";");
            while (rs.next()) {
                respuesta = rs.getString("pass");
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return "error";
        }
    }

    public int getIdUsuariombb(String numero) throws Exception {
        conexion conn = new conexion();
        int respuesta = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            ResultSet rs = st.executeQuery("select id from usuarios where numero='" + numero + "' and permiso is not null and tipo is null and activo=true;");
            while (rs.next()) {
                respuesta = rs.getInt("id");
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public int getIdUsuariohbb(String numero, int tipo) throws Exception {
        conexion conn = new conexion();
        int respuesta = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            ResultSet rs = st.executeQuery("select id from usuarios where servicio='" + numero + "' and permiso is not null and tipo=" + tipo + " and activo=true;");
            while (rs.next()) {
                respuesta = rs.getInt("id");
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public boolean eliminarAcceso(int idusuario, String acceso) throws Exception {
        conexion conn = new conexion();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            int total = st.executeUpdate("delete from accesos where id_usuario=" + idusuario + " and acceso='" + acceso + "';");

            conexion.close();
            st.close();
            if (total > 0) {
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean enviarSMSPIN(String gsm, String pin, String sender, String mensaje) {
        try {
            String server = "https://upfrontmessage.com:8080/walmart/SendSMSWS?wsdl";
            HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "text/xml; charset=utf-8");
            connection.setRequestProperty("Accept", "text/xml");

            String parametros = "";
            parametros += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:inn=\"http://innovattia.com/\">"
                    + "<soapenv:Header/>"
                    + "<soapenv:Body>"
                    + "<inn:SendSMS>"
                    + "<usrClient>recargaBait</usrClient>"
                    + "<pasClient>eRE233s3</pasClient>"
                    //+ "<sender>93000</sender>"
                    + "<sender>" + sender + "</sender>"
                    + "<text>" + mensaje + "</text>"
                    + "<gsm>52" + gsm + "</gsm>"
                    + "</inn:SendSMS>"
                    + "</soapenv:Body>"
                    + "</soapenv:Envelope>";
            OutputStream outs = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(outs, "UTF-8");
            wout.write(parametros);
            wout.flush();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                System.out.println("Envio PIN Walmart" +line);
            }
            outs.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public JSONObject insertarInformacion(String numero, String nombre, String email, int permiso, String pass, String foto, int idUsuario) throws JSONException, IOException, Exception {
        conexion conn = new conexion();
        JSONObject resultado = new JSONObject();
        try {
            System.out.println("idUsuario -> "+idUsuario);
            if (idUsuario > 0){
                String urlImagen = "";
                String urlfinal = null;
                if (!foto.equals("") || foto.length() > 10) {
                    urlImagen = guardarImagen("10.11.10.62", foto, idUsuario);
                    urlImagen = guardarImagen("10.11.10.63", foto, idUsuario);
                }
                if (!urlImagen.equals("")) {
                    // urlfinal = "http://198.72.120.24/" + urlImagen;
                    urlfinal = "https://mibait.com/repositorio/" + urlImagen;
                }
                
                
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                
                try (CallableStatement statement = conexion.prepareCall("{call actualizarUsuario(?, ?, ?, ?, ?, ?, ?)}")) {
                    statement.setString(1, Config.tk);
                    statement.setString(2, nombre);
                    statement.setString(3, email);
                    statement.setString(4, pass);
                    statement.setString(5, urlfinal);
                    statement.setInt(6, permiso);
                    statement.setInt(7, idUsuario);
                    statement.executeUpdate();
                    statement.close();
                    conexion.close();                
                }
                
                /*
                Statement st = conexion.createStatement();
                st.executeUpdate("update usuarios set nombre='" + nombre + "', email='" + email + "', pass='" + pass + "', foto='" + urlfinal + "', permiso=" + permiso + " where id=" + idUsuario + " and activo=true;");
                conexion.close();
                st.close();
                */
                
                token token = new token();
                String acceso = token.generateNewToken();
                insertarToken(idUsuario, acceso);
                resultado.put("success", true);
                resultado.put("access", acceso);
                resultado.put("nombre", nombre);
                resultado.put("email", email);
                resultado.put("numero", numero);
                resultado.put("foto", urlfinal);
                resultado.put("permiso", permiso);
                return resultado;
            } else{
                resultado.put("success", false);
                resultado.put("mensaje", "Lo sentimos, no se encontró el usuario a registrar.");
                return resultado;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject insertarInformacionHBB(String numero, String nombre, String servicio, int permiso, String pass, String foto, int tipo) throws JSONException, IOException, Exception {
        bd bd = new bd();
        conexion conn = new conexion();
        JSONObject resultado = new JSONObject();
        try {
            JSONObject idusuarioobj = bd.obteneridhbb(numero, servicio, tipo);
            int idUsuario = idusuarioobj.getInt("idUsuario");
            if (idUsuario > 0) {
                String urlImagen = "";
                String urlfinal = null;
                if (!foto.equals("") || foto.length() > 10) {
                    urlImagen = guardarImagen("10.11.10.62", foto, idUsuario);
                    urlImagen = guardarImagen("10.11.10.63", foto, idUsuario);
                }
                if (!urlImagen.equals("")) {
                    // urlfinal = "http://198.72.120.24/" + urlImagen;
                    urlfinal = "https://mibait.com/repositorio/" + urlImagen;
                }
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                
                try (CallableStatement statement = conexion.prepareCall("{call actualizarUsuarioHBB(?, ?, ?, ?, ?, ?)}")) {
                    statement.setString(1, Config.tk);
                    statement.setString(2, nombre);
                    statement.setString(3, pass);
                    statement.setString(4, urlfinal);
                    statement.setInt(5, permiso);
                    statement.setInt(6, idUsuario);
                    statement.executeUpdate();
                    statement.close();
                    conexion.close();                
                }
                
                /*
                Statement st = conexion.createStatement();
                st.executeUpdate("update usuarios set nombre='" + nombre + "', pass='" + pass + "', foto='" + urlfinal + "', permiso=" + permiso + " where id=" + idUsuario + " and activo=true;");
                conexion.close();
                st.close();
                */
                token token = new token();
                String acceso = token.generateNewToken();
                insertarToken(idUsuario, acceso);
                resultado.put("success", true);
                resultado.put("access", acceso);
                resultado.put("nombre", nombre);
                resultado.put("servicio", servicio);
                resultado.put("numero", numero);
                resultado.put("foto", urlfinal);
                resultado.put("permiso", permiso);
                return resultado;
            } else {
                resultado.put("success", false);
                return resultado;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject actualizarTop(String numero, String nombre, String email, int permiso, String foto, String pass, String nuevapass, int tipo) throws JSONException, IOException, Exception {
        conexion conn = new conexion();
        int idUsuario = 0;
        String photo = null;
        String actualpass = null;
        JSONObject resultado = new JSONObject();
        try {
            System.out.println("numero -> "+numero);
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            System.out.println("numero -> "+numero);
            ResultSet rs = st.executeQuery("select id, foto, pass from usuarios where numero='" + numero + "' and tipo is null and activo=true;");
            while (rs.next()) {
                idUsuario = rs.getInt("id");
                photo = rs.getString("foto");
                actualpass = rs.getString("pass");
            }
            System.out.println("idUsuario -> "+idUsuario);
            String query;
            if (idUsuario > 0) {

                if (tipo == 1) {
                    //query = "update usuarios set nombre='" + nombre + "', email='" + email + "', permiso=" + permiso + " where id=" + idUsuario + " and activo=true;";
                    String urlImagen = "";
                    String urlfinal = null;
                    if (!foto.equals("") || foto.length() > 10) {
                        urlImagen = guardarImagen("10.11.10.62", foto, idUsuario);
                        urlImagen = guardarImagen("10.11.10.63", foto, idUsuario);
                        if (!urlImagen.equals("")) {
                            // urlfinal = "http://198.72.120.24/" + urlImagen;
                            urlfinal = "https://mibait.com/repositorio/" + urlImagen;
                            photo = urlfinal;
                        }
                        //query = "update usuarios set nombre='" + nombre + "', email='" + email + "', foto='" + urlfinal + "', permiso=" + permiso + " where id=" + idUsuario + " and activo=true;";
                    }

                    try (CallableStatement statement = conexion.prepareCall("{call actualizarTop(?, ?, ?, ?, ?, ?)}")) {
                        statement.setString(1, Config.tk);
                        statement.setString(2, nombre);
                        statement.setString(3, email);
                        statement.setString(4, urlfinal);
                        statement.setInt(5, permiso);
                        statement.setInt(6, idUsuario);
                        statement.executeUpdate();
                        statement.close();            
                    }
                    
                    /*
                    st.executeUpdate(query);
                    conexion.close();
                    st.close();
                    */
                    
                    
                    resultado.put("success", true);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", numero);
                    resultado.put("foto", photo);
                    resultado.put("permiso", permiso);
                } else {
                    if (actualpass.equals(pass)) {
                        query = "update usuarios set pass='" + nuevapass + "' where id=" + idUsuario + " and activo=true;";
                        st.executeUpdate(query);
                        conexion.close();
                        st.close();
                        resultado.put("success", true);
                    } else {
                        resultado.put("success", false);
                        resultado.put("mensaje", "La contraseña actual no coincide");

                    }
                }

                return resultado;
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "No se encontró usuario");
                return resultado;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject actualizarTopHBB(String numero, String nombre, String email, int permiso, String foto, String pass, String nuevapass, int idUsuario, int tipo, int dn) throws JSONException, IOException, Exception {
        conexion conn = new conexion();
        String photo = null;
        String servicio = null;
        String actualpass = null;
        JSONObject resultado = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select id, foto, pass, servicio from usuarios where id='" + idUsuario + "' and tipo=" + dn + " and activo=true;");
            while (rs.next()) {
                servicio = rs.getString("servicio");
                photo = rs.getString("foto");
                actualpass = rs.getString("pass");
            }
            String query;
            if (tipo == 1) {
                //query = "update usuarios set nombre='" + nombre + "', email='" + email + "', permiso=" + permiso + ",numero='" + numero + "' where id=" + idUsuario + " and activo=true;";
                String urlImagen = "";
                String urlfinal = null;
                if (!foto.equals("") || foto.length() > 10) {
                    urlImagen = guardarImagen("10.11.10.62", foto, idUsuario);
                    urlImagen = guardarImagen("10.11.10.63", foto, idUsuario);
                    if (!urlImagen.equals("")) {
                        //urlfinal = "http://198.72.120.24/" + urlImagen;
                        urlfinal = "https://mibait.com/repositorio/" + urlImagen;
                        photo = urlfinal;
                    }
                    //query = "update usuarios set nombre='" + nombre + "', email='" + email + "', foto='" + urlfinal + "', permiso=" + permiso + ", numero='" + numero + "' where id=" + idUsuario + " and activo=true;";
                }
                
                try (CallableStatement statement = conexion.prepareCall("{call actualizarTopHbb(?, ?, ?, ?, ?, ?, ?)}")) {
                    statement.setString(1, Config.tk);
                    statement.setString(2, nombre);
                    statement.setString(3, email);
                    statement.setString(4, numero);
                    statement.setString(5, urlfinal);
                    statement.setInt(6, permiso);
                    statement.setInt(7, idUsuario);
                    statement.executeUpdate();
                    statement.close();            
                }

                /*
                st.executeUpdate(query);
                conexion.close();
                st.close();
                */
                
                
                
                resultado.put("success", true);
                resultado.put("nombre", nombre);
                resultado.put("email", email);
                resultado.put("numero", numero);
                resultado.put("foto", photo);
                resultado.put("permiso", permiso);
                resultado.put("servicio", servicio);
            } else {
                if (actualpass.equals(pass)) {
                    query = "update usuarios set pass='" + nuevapass + "' where id=" + idUsuario + " and activo=true;";
                    st.executeUpdate(query);
                    conexion.close();
                    st.close();
                    resultado.put("success", true);
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "La contraseña actual no coincide");

                }
            }
            return resultado;

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public static String guardarImagen(String IP, String imageString, int iduser) throws UnsupportedEncodingException, IOException {
        StringEntity params = new StringEntity("{\"foto\":\"" + imageString + "\",\"idUsuario\":" + iduser + "}");
        org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create().build();
        String url = "http://" + IP + ":8080/imagenApp/imagen";
        HttpPost request = new HttpPost(url);
        request.addHeader("Content-Type", "application/json");
        request.setEntity(params);
        HttpResponse response = httpClient.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("Response Code guardar imagen: " + responseCode);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        String line2 = "";
        while ((line = rd.readLine()) != null) {
            line = line.replace("\n", "").replace("\r", "");
            //System.out.println(line);
            line2 += line;
        }
        if (responseCode == 200) {
            return line2;
        } else {
            return "";
        }
    }

    /*public static String guardarImagen(String imageString, int iduser) {
     String ruta = "/var/www/html/app/photos/" + iduser + "/perfil.jpg";
     String rutacarpeta = "/var/www/html/app/photos/" + iduser;
     String urlImagen = "app/photos/" + iduser + "/perfil.jpg";
     File file = new File(ruta);
     File fcarpeta = new File(rutacarpeta);

     try {
     if (!fcarpeta.exists()) {
     fcarpeta.mkdirs();
     }
     if (!file.exists()) {
     file.createNewFile();
     }
     byte[] data = Base64.decodeBase64(imageString);
     try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file))) {
     writer.write(data);
     writer.flush();
     writer.close();
     }

     } catch (Exception e) {
     e.printStackTrace();
     }

     return urlImagen;
     }*/
    public static String guardarTicket(String imageString, String numero) {
        String ruta = "/var/www/html/app/photos/tickets/" + numero + ".jpg";
        String rutacarpeta = "/var/www/html/app/photos/tickets";
        String urlImagen = "app/photos/tickets/" + numero + ".jpg";
        File file = new File(ruta);
        File fcarpeta = new File(rutacarpeta);

        try {
            if (!fcarpeta.exists()) {
                fcarpeta.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] data = Base64.decodeBase64(imageString);
            try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file))) {
                writer.write(data);
                writer.flush();
                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return urlImagen;
    }

    public JSONObject getNumero(int idUsuario) throws JSONException, Exception {
        conexion conn = new conexion();
        String numero = "";
        int tipo = 0;
        String servicio = "";
        JSONObject respuesta = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select numero,tipo, servicio from usuarios where id=" + idUsuario + " and permiso is not null and activo=true;");
            while (rs.next()) {
                numero = rs.getString("numero");
                tipo = rs.getInt("tipo");
                servicio = rs.getString("servicio");
            }
            conexion.close();
            st.close();
            respuesta.put("tipo", tipo);
            respuesta.put("numero", numero);
            respuesta.put("servicio", servicio);

            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return respuesta;
        }
    }

    public JSONObject getOfertas() throws JSONException, Exception {
        conexion conn = new conexion();
        JSONObject ofertas = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select nombre, imagen, tipo, offeringid, pesos, duracion, disfruta, incluye, vigencia, estado from ofertas where activo=1 and tipo<3 order by pesos desc");
            while (rs.next()) {
                JSONObject lista = new JSONObject();
                String nombre = rs.getString("nombre");
                String imagen = rs.getString("imagen");
                int tipo = rs.getInt("tipo");
                String offeringid = rs.getString("offeringid");
                int pesos = rs.getInt("pesos");
                String duracion = rs.getString("duracion");
                String disfruta = rs.getString("disfruta");
                String incluye = rs.getString("incluye");
                String vigencia = rs.getString("vigencia");

                lista.put("nombre", nombre);
                lista.put("imagen", imagen);
                lista.put("tipo", tipo);
                lista.put("offeringid", offeringid);
                lista.put("pesos", pesos);
                lista.put("duracion", duracion);
                lista.put("disfruta", disfruta);
                lista.put("incluye", incluye);
                lista.put("vigencia", vigencia);
                lista.put("estado", rs.getInt("estado"));
                array.put(lista);
            }
            conexion.close();
            st.close();
            ofertas.put("success", true);
            ofertas.put("lista", array);
            return ofertas;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            JSONObject error = new JSONObject();
            error.put("success", false);
            array.put("error");
            return error;
        }
    }

    public JSONObject getOfertasHBB() throws JSONException, Exception {
        conexion conn = new conexion();
        JSONObject ofertas = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select nombre, imagen, tipo, offeringid, pesos, duracion, disfruta, incluye, vigencia, estado from ofertas where activo=1 and tipo=3 order by pesos desc");
            while (rs.next()) {
                JSONObject lista = new JSONObject();
                String nombre = rs.getString("nombre");
                String imagen = rs.getString("imagen");
                int tipo = rs.getInt("tipo");
                String offeringid = rs.getString("offeringid");
                int pesos = rs.getInt("pesos");
                String duracion = rs.getString("duracion");
                String disfruta = rs.getString("disfruta");
                String incluye = rs.getString("incluye");
                String vigencia = rs.getString("vigencia");

                lista.put("nombre", nombre);
                lista.put("imagen", imagen);
                lista.put("tipo", tipo);
                lista.put("offeringid", offeringid);
                lista.put("pesos", pesos);
                lista.put("duracion", duracion);
                lista.put("disfruta", disfruta);
                lista.put("incluye", incluye);
                lista.put("vigencia", vigencia);
                lista.put("estado", rs.getInt("estado"));
                array.put(lista);
            }
            conexion.close();
            st.close();
            ofertas.put("success", true);
            ofertas.put("lista", array);
            return ofertas;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            JSONObject error = new JSONObject();
            error.put("success", false);
            array.put("error");
            return error;
        }
    }

    public JSONObject getOfertasSams() throws JSONException, Exception {
        conexion conn = new conexion();
        JSONObject ofertas = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select nombre, imagen, tipo, offeringid, pesos, duracion, disfruta, incluye, vigencia, estado from ofertas where activo=1 and tipo=4 order by id asc");
            while (rs.next()) {
                JSONObject lista = new JSONObject();
                String nombre = rs.getString("nombre");
                String imagen = rs.getString("imagen");
                int tipo = rs.getInt("tipo");
                String offeringid = rs.getString("offeringid");
                int pesos = rs.getInt("pesos");
                String duracion = rs.getString("duracion");
                String disfruta = rs.getString("disfruta");
                String incluye = rs.getString("incluye");
                String vigencia = rs.getString("vigencia");

                lista.put("nombre", nombre);
                lista.put("imagen", imagen);
                lista.put("tipo", tipo);
                lista.put("offeringid", offeringid);
                lista.put("pesos", pesos);
                lista.put("duracion", duracion);
                lista.put("disfruta", disfruta);
                lista.put("incluye", incluye);
                lista.put("vigencia", vigencia);
                lista.put("estado", rs.getInt("estado"));
                array.put(lista);
            }
            conexion.close();
            st.close();
            ofertas.put("success", true);
            ofertas.put("lista", array);
            return ofertas;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            JSONObject error = new JSONObject();
            error.put("success", false);
            array.put("error");
            return error;
        }
    }

    public JSONObject getRecargas(String numero) throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject lista = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection("jdbc:mysql://119.8.8.250:3306/bait", "bait", "q$Lyd&4Nj@Pl");
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select date(fechaRecarga) as fecha, pesos as nombre, date(fechaVencimiento) as vencimiento from recargas where numero='" + numero + "' and pesos is not null and tipo=2 order by fechaRecarga desc");
            while (rs.next()) {
                JSONObject recarga = new JSONObject();
                recarga.put("fecha", rs.getDate("fecha"));
                recarga.put("nombre", rs.getInt("nombre"));
                recarga.put("vencimiento", rs.getDate("vencimiento"));
                array.put(recarga);
            }
            conexion.close();
            st.close();
            lista.put("success", true);
            lista.put("recargas", array);
            return lista;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            lista.put("success", false);
            return lista;
        }
    }

    public JSONObject getRecargasOnline(String numero) throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject lista = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection("jdbc:mysql://119.8.8.250:3306/bait", "bait", "q$Lyd&4Nj@Pl");
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select date(fechaRecarga) as fecha, pesos as nombre, date(fechaVencimiento) as vencimiento from recargas where numero='" + numero + "' and pesos is not null and tipo=2 order by fechaRecarga desc");
            while (rs.next()) {
                JSONObject recarga = new JSONObject();
                recarga.put("fecha", rs.getDate("fecha"));
                recarga.put("nombre", rs.getInt("nombre"));
                recarga.put("vencimiento", rs.getDate("vencimiento"));
                array.put(recarga);
            }
            conexion.close();
            st.close();
            lista.put("success", true);
            lista.put("recargas", array);
            return lista;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            lista.put("success", false);
            return lista;
        }
    }

    public boolean getTickets(String ticket) throws Exception {
        conexion conn = new conexion();
        String existe = "";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select ticket from tickets where ticket='" + ticket + "'; ");
            while (rs.next()) {
                existe = rs.getString("ticket");
            }
            conexion.close();
            st.close();
            if (existe.length() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public JSONObject canjearTicket(int idUsuario, String ticket, String numero, String total, String comercio, String ticket_foto) throws IOException, Exception {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        int megas = 0;
        JSONObject respuesta = new JSONObject();
        try {
            if (getTickets(ticket)) {
                respuesta.put("success", false);
                respuesta.put("mensaje", "El ticket ya ha sido redimido. Favor de intentar con otro ticket de los comercios participantes.");
                return respuesta;
            } else {
                conexion conn = new conexion();
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select sum(megas) as megas from tickets where fecha>'" + formatter.format(date) + "' and id_user=" + idUsuario + " group by id_user;");
                while (rs.next()) {
                    megas = rs.getInt("megas");
                }
                if (megas < 50) {
                    //revisar bonos del mes
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int month = cal.get(Calendar.MONTH);
                    int megasMensual = 0;
                    rs = st.executeQuery("SELECT MONTH(fecha)AS mes, sum(megas) AS total FROM tickets where id_user=" + idUsuario + " and month(fecha)=" + month + " GROUP BY mes;");
                    while (rs.next()) {
                        megasMensual = rs.getInt("total");
                    }
                    conexion.close();
                    st.close();
                    if (megasMensual < 1000) {
                        insertarCanje(idUsuario, ticket, total, comercio);
                        //realizar recarga de bonos
                        //offerring 1809933007
                        //guardarTicket(ticket_foto, ticket);
                        //uriel leal -> hacerRecarga(numero);
                        respuesta.put("success", true);
                        return respuesta;
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "El limite de megabytes redimidos por mes ha sido superado.");
                        return respuesta;
                    }
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "El limite de tickets redimidos por día ha sido superado.");
                    return respuesta;
                }
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            try {
                respuesta.put("success", false);
            } catch (JSONException ex1) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return respuesta;
        } catch (JSONException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return respuesta;
        }
    }

    public boolean insertarCanje(int idusuario, String ticket, String total, String comercio) throws Exception {
        conexion conn = new conexion();
        try {
            //insertarCanje
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            //Statement st = conexion.createStatement();
            //st.executeUpdate("insert into tickets(id_user, ticket, fecha, megas,total,comercio) values(" + idusuario + ",'" + ticket + "',now(),50,'" + total + "','" + comercio + "');");
            try (CallableStatement statement = conexion.prepareCall("{call insertarCanje(?, ?, ?, ?, ?)}")) {
                statement.setString(1, Config.tk);
                statement.setInt(2, idusuario);
                statement.setString(3, ticket);
                statement.setString(4, total);
                statement.setString(5, comercio);
                statement.execute();
                statement.close();
                conexion.close();                
            }
            return true;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public String hacerRecarga(String gsm) throws MalformedURLException, IOException {

        String respuesta = "";
        try {
            String server = "http://localhost:8080/purchaseBait/recarga?numero=" + gsm + "&offeringid=1809933012&origen=4";
            HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "aXA0bVhZd21CSEZTeTZ2Tm5UV2NRa0xhM1NzRTYzTlBUa0p3S3hRNE01bGE3dDFoYnU4SUZwRXlMTG8yRXpWM01Xc0x2UHNxaG1tMXlqK1VkYUdLQzE2TmV2VkwzMGVhQnhhNElOSFl5M2xqWUF6b0YrelpzcXo2d0dWYTMrTy9KdjBmZXN6b1k4R0RRZ1FPVzk5d3RsLzhOR2RLZWQ1R0ExRUp1NnA1aXE0VmlVOFl0QnVQdEplTjN6cmFhZTlsY2xvYUVvRzJZZ1dqaG9YSzN1RC9WU2VENXVMQ2V1OThsc2tmQlBHRUNEOFlESmF5aTNvaFp4eUxzeHE4MzI0ZlI5cTFPZEVhK3UvTDV1VUd2RnlWdlBXRzJCMW0xRkVJc3RycWJTMDl1N2J4VFRoK08xLzhGN3RZNGF0QjI1UmpUR0RzdCtpd1M5ZnZxVHJOcXMvMC9RPT0=");
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            respuesta = line;
            reader.close();
            return respuesta;
        } catch (IOException e) {
            System.out.println(e + "error recarga bono");
            return respuesta;
        }
    }

    public boolean existeNumero(String numero) throws JSONException, Exception {
        conexion conn = new conexion();
        boolean respuesta = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select numero from usuarios where numero='" + numero + "' and tipo is null and pass is not null and activo=true;");
            while (rs.next()) {
                if (rs.getString("numero").length() > 0) {
                    respuesta = true;

                } else {
                    respuesta = false;
                }
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean registroCompleto(String numero) throws JSONException, Exception {
        conexion conn = new conexion();
        boolean respuesta = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select numero from usuarios where numero='" + numero + "' and nombre is not null and permiso is not null and activo=true and tipo is null;");
            while (rs.next()) {
                if (rs.getString("numero").length() > 0) {
                    respuesta = true;

                } else {
                    respuesta = false;
                }
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean existeServicio(String servicio, int tipo) throws JSONException, Exception {
        conexion conn = new conexion();
        boolean respuesta = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select servicio from usuarios where servicio='" + servicio + "' and tipo=" + tipo + " and nombre is not null and permiso is not null and activo=true;");
            while (rs.next()) {
                if (rs.getString("servicio").length() > 0) {
                    respuesta = true;
                } else {
                    respuesta = false;
                }
            }
            conexion.close();
            st.close();
            return respuesta;
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public JSONObject actualizarPass(String numero, String pass) throws JSONException, Exception {
        conexion conn = new conexion();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        JSONObject resultado = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            st.executeUpdate("update usuarios set pass='" + pass + "' where numero='" + numero + "' and tipo is null and activo=true; ");

            ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso  from usuarios where numero='" + numero + "' and pass='" + pass + "' and tipo is null and activo=true;");
            while (rs.next()) {
                idUsuario = rs.getInt("id");
                gsm = rs.getString("numero");
                nombre = rs.getString("nombre");
                email = rs.getString("email");
                foto = rs.getString("foto");
                permiso = rs.getInt("permiso");
            }
            conexion.close();
            st.close();
            token token = new token();
            String acceso = token.generateNewToken();
            if (acceso.length() > 0 && idUsuario > 0) {
                insertarToken(idUsuario, acceso);
                resultado.put("success", true);
                resultado.put("access", acceso);
                resultado.put("nombre", nombre);
                resultado.put("email", email);
                resultado.put("numero", gsm);
                resultado.put("foto", foto);
                resultado.put("permiso", permiso);
                return resultado;
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                return resultado;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject actualizarPassv2(String numero, String pass) throws JSONException, ClassNotFoundException, SQLException, Exception {
        conexion conn = new conexion();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        JSONObject passvalido = adminpass(numero, pass, 1);
        if (passvalido.getBoolean("success")) {
            JSONObject resultado = new JSONObject();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                st.executeUpdate("update usuarios set pass='" + pass + "' where numero='" + numero + "' and tipo is null and activo=true;");

                ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso  from usuarios where numero='" + numero + "' and pass='" + pass + "' and tipo is null and activo=true;");
                while (rs.next()) {
                    idUsuario = rs.getInt("id");
                    gsm = rs.getString("numero");
                    nombre = rs.getString("nombre");
                    email = rs.getString("email");
                    foto = rs.getString("foto");
                    permiso = rs.getInt("permiso");
                }
                conexion.close();
                st.close();
                token token = new token();
                String acceso = token.generateNewToken();
                if (acceso.length() > 0 && idUsuario > 0) {
                    insertarToken(idUsuario, acceso);
                    resultado.put("success", true);
                    resultado.put("access", acceso);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", gsm);
                    resultado.put("foto", foto);
                    resultado.put("permiso", permiso);
                    return resultado;
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                    return resultado;
                }

            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                resultado.put("success", false);
                resultado.put("mensaje", "Ha ocurrido un error interno al generar el acceso. Intentelo más tarde.");
                return resultado;
            }
        } else {
            return passvalido;
        }

    }

    public JSONObject actualizarPassHBB(String servicio, String pass) throws JSONException, Exception {
        conexion conn = new conexion();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        JSONObject resultado = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            st.executeUpdate("update usuarios set pass='" + pass + "' where servicio='" + servicio + "' and tipo=2 and activo=true;");

            ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso  from usuarios where servicio='" + servicio + "' and pass='" + pass + "' and tipo=2 and activo=true;");
            while (rs.next()) {
                idUsuario = rs.getInt("id");
                gsm = rs.getString("numero");
                nombre = rs.getString("nombre");
                email = rs.getString("email");
                foto = rs.getString("foto");
                permiso = rs.getInt("permiso");
            }
            conexion.close();
            st.close();
            token token = new token();
            String acceso = token.generateNewToken();
            if (acceso.length() > 0 && idUsuario > 0) {
                insertarToken(idUsuario, acceso);
                resultado.put("success", true);
                resultado.put("access", acceso);
                resultado.put("nombre", nombre);
                resultado.put("email", email);
                resultado.put("numero", gsm);
                resultado.put("foto", foto);
                resultado.put("permiso", permiso);
                resultado.put("servicio", servicio);
                return resultado;
            } else {
                resultado.put("success", false);
                resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                return resultado;
            }

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            resultado.put("success", false);
            return resultado;
        }
    }

    public JSONObject actualizarPassHBBv2(String servicio, String pass, int tipo) throws JSONException, ClassNotFoundException, SQLException, Exception {
        conexion conn = new conexion();
        int idUsuario = 0;
        String nombre = "";
        String email = "";
        String foto = "";
        String gsm = "";
        int permiso = 0;
        JSONObject passvalido = adminpass(servicio, pass, tipo);
        if (passvalido.getBoolean("success")) {
            JSONObject resultado = new JSONObject();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                st.executeUpdate("update usuarios set pass='" + pass + "' where servicio='" + servicio + "' and tipo=" + tipo + " and activo=true;");

                ResultSet rs = st.executeQuery("select id, numero, email, foto, nombre,permiso  from usuarios where servicio='" + servicio + "' and pass='" + pass + "' and tipo=" + tipo + " and activo=true;");
                while (rs.next()) {
                    idUsuario = rs.getInt("id");
                    gsm = rs.getString("numero");
                    nombre = rs.getString("nombre");
                    email = rs.getString("email");
                    foto = rs.getString("foto");
                    permiso = rs.getInt("permiso");
                }
                conexion.close();
                st.close();
                token token = new token();
                String acceso = token.generateNewToken();
                if (acceso.length() > 0 && idUsuario > 0) {
                    insertarToken(idUsuario, acceso);
                    resultado.put("success", true);
                    resultado.put("access", acceso);
                    resultado.put("nombre", nombre);
                    resultado.put("email", email);
                    resultado.put("numero", gsm);
                    resultado.put("foto", foto);
                    resultado.put("permiso", permiso);
                    resultado.put("servicio", servicio);
                    return resultado;
                } else {
                    resultado.put("success", false);
                    resultado.put("mensaje", "Ha ocurrido un error al generar el acceso");
                    return resultado;
                }
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
                resultado.put("success", false);
                return resultado;
            }
        } else {
            return passvalido;
        }

    }

    public JSONObject adminpass(String numero, String pass, int tipo) throws ClassNotFoundException, SQLException, JSONException, Exception {
        conexion conn = new conexion();
        JSONObject respuesta = new JSONObject();
        int idusuario = 0;
        if (tipo == 1) {
            idusuario = getIdUsuariombb(numero);
        } else {
            idusuario = getIdUsuariohbb(numero, tipo);
        }
        if (idusuario > 0) {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            String passActual = null;
            ResultSet rs = st.executeQuery("select pass from usuarios where id=" + idusuario + " and activo=true;");
            String actualpass = null;
            boolean existe = false;
            while (rs.next()) {
                actualpass = rs.getString("pass");
            }
            if (!actualpass.equals(pass)) {
                rs = st.executeQuery("select pass from hist_pass where id_usuario=" + idusuario + " and pass='" + pass + "';");

                while (rs.next()) {
                    passActual = rs.getString("pass");
                }
                if (pass.equals(passActual)) {
                    existe = true;
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "La contraseña ingresada ya se ha utilizado anteriormente. Favor de utilizar una diferente.");
                } else {
                    respuesta.put("success", true);
                }
                rs = st.executeQuery("select id from hist_pass where id_usuario=" + idusuario + " order by id desc;");
                int total = 0;
                int id = 0;
                while (rs.next()) {
                    id = rs.getInt("id");
                    total++;
                }

                if (total < 5 && existe == false) {
                    st.executeUpdate("insert into hist_pass (id_usuario, pass, fecha) values('" + idusuario + "','" + pass + "',now());");
                } else {
                    st.executeUpdate("delete from hist_pass where id=" + id + ";");
                    st.executeUpdate("insert into hist_pass (id_usuario, pass, fecha) values('" + idusuario + "','" + pass + "',now());");
                }
            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "La contraseña ingresada ya se ha utilizado anteriormente. Favor de utilizar una diferente.");
            }

        } else {
            respuesta.put("success", false);
            respuesta.put("mensaje", "No se ha encontrado un usuario registrado con el número ingresado.");
        }

        return respuesta;
    }

    public boolean eliminarCuenta(int idusuario) throws Exception {
        conexion conn = new conexion();
        DateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String fecha = formato.format(date);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();

            int total = st.executeUpdate("update usuarios set activo=false where id=" + idusuario + ";");

            if (total > 0) {
                ResultSet rs = st.executeQuery("select numero from usuarios where id=" + idusuario + ";");
                while (rs.next()) {
                    String numero = rs.getString("numero");
                    enviarSMS(numero, "Tu cuenta ha sido eliminada el " + fecha + ", no podras acceder a ella a partir de este momento. Gracias por tu preferencia.", "Bait");
                }
                enviarSMS(null, null, null);
                conexion.close();
                st.close();
                return true;

            } else {
                return false;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean enviarSMS(String gsm, String mensaje, String sender) {
        try {
            String server = "https://upfrontmessage.com:8080/walmart/SendSMSWS?wsdl";
            HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "text/xml; charset=utf-8");
            connection.setRequestProperty("Accept", "text/xml");

            String parametros = "";
            parametros += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:inn=\"http://innovattia.com/\">"
                    + "<soapenv:Header/>"
                    + "<soapenv:Body>"
                    + "<inn:SendSMS>"
                    + "<usrClient>recargaBait</usrClient>"
                    + "<pasClient>eRE233s3</pasClient>"
                    + "<sender>93000</sender>"
                    //+ "<sender>" + sender + "</sender>"
                    + "<text>" + mensaje + "</text>"
                    + "<gsm>52" + gsm + "</gsm>"
                    + "</inn:SendSMS>"
                    + "</soapenv:Body>"
                    + "</soapenv:Envelope>";
            OutputStream outs = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(outs, "UTF-8");
            wout.write(parametros);
            wout.flush();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            outs.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String getCdn() throws Exception {
        conexion conn = new conexion();
        String cdn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select valor from variablesSo where nombre='cdn'");
            while (rs.next()) {
                cdn = rs.getString("valor");
            }
            System.out.println("cdn -> "+cdn);
            conexion.close();
            st.close();
            return cdn;
        } catch (ClassNotFoundException | SQLException ex) {
            return null;
        }
    }

    public JSONObject getHost(String nombre, String valorhost) throws JSONException, Exception {
        conexion conn = new conexion();
        Connection conexion = null;
        Statement st;
        JSONObject resultados = new JSONObject();
        boolean existe = false;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select valor from variablesSo where nombre='" + nombre + "' and activo=true;");
            while (rs.next()) {
                String valor = rs.getString("valor");
                if (valorhost.equals(valor)) {
                    existe = true;
                    resultados.put("valor", valor);
                }

            }
            st.close();
            conexion.close();
            resultados.put("success", existe);
            return resultados;

        } catch (ClassNotFoundException e1) {
            System.out.println("ERROR:No encuentro el driver de la BD: "
                    + e1.getMessage());
            return resultados;
        } catch (SQLException ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return resultados;
        }
    }
}
