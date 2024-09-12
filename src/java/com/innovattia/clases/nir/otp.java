/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases.nir;

import com.innovattia.clases.accesos;
import com.innovattia.clases.altan;
import com.innovattia.clases.bd;
import com.innovattia.clases.conexion;
import java.net.ProtocolException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 52556
 */
public class otp {

    Connection conexion = null;
    Statement st;
    boolean agregado = false;

    public JSONObject generarPIN(String numero) throws JSONException, Exception {
        JSONObject respuesta = new JSONObject();
        accesos app = new accesos();
        boolean envioPermitido = false;
        bd bd = new bd();
        if (numero.length() == 10) {
            conexionnir conn = new conexionnir();
            Random random = new Random();
            String pin = String.format("%06d", random.nextInt(1000000));
            String gsm = "";
            int idotp = 0;
            int envios = 0;
            Date ultimoEnvio = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
                Statement st = conexion.createStatement();
                ResultSet rs = st.executeQuery("select id, numero, solicitudes, enviosms from otpNir where numero='" + numero + "';");
                while (rs.next()) {
                    gsm = rs.getString("numero");
                    idotp = rs.getInt("id");
                    envios = rs.getInt("solicitudes");
                    ultimoEnvio = rs.getTimestamp("enviosms");
                }
                conexion.close();
                st.close();
                if (idotp == 0) {
                   // System.out.println("El usuario no existe");
                    if (pin.length() == 6) {
                        JSONObject obj = agregarOtp(numero, pin);
                        if (obj.getBoolean("success")) {
                            idotp = obj.getInt("id");
                            envioPermitido = true;
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, hubo un error al generar su código de validación.");
                    }
                } else {
                    Calendar ahora = Calendar.getInstance();
                    //System.out.println("Ultimo Envio -> " + ultimoEnvio);
                    Calendar expiracion = Calendar.getInstance();
                    expiracion.setTime(ultimoEnvio);
                    long milliseconds1 = expiracion.getTimeInMillis();
                    long milliseconds2 = ahora.getTimeInMillis();
                    long diff = milliseconds2 - milliseconds1;
                    long diffMinutes = diff / (60 * 1000);
                   // System.out.println("Time in minutes: " + diffMinutes + " minutes.");

                    if (diffMinutes < 60 && envios < 5) {
                        envioPermitido = true;
                    } else {
                        if (diffMinutes < 60 && envios >= 5) {
                            expiracion.add(Calendar.HOUR, 1);
                            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            respuesta.put("fechaDesbloqueo", df.format(expiracion.getTime()));
                        } else {
                            resetOTP(idotp, false);
                            envioPermitido = true;
                        }
                    }
                }

                if (envioPermitido && pin.length() == 6) {
                    UUID uuid = UUID.randomUUID();
                    if (sumarOTP(idotp, pin, uuid + "", false)) {
                        app.enviarSMSPIN(numero, pin, "Bait", "Codigo de verificacion Bait: " + pin + ". Es valido por 3 minuto. Por seguridad, no lo compartas.");
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

    public JSONObject agregarOtp(String numero, String pin) throws JSONException {

        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "insert into otpNir(numero,codigo,enviosms) values(?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, numero);
            stmt.setString(2, pin);
            stmt.setTimestamp(3, date);

            int ok = stmt.executeUpdate();

            if (ok == 1) {
                ejecutado = true;
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                    //System.out.println("ID usuario registrado = " + rs.getInt(1));
                    idtransaccion = rs.getInt(1);
                }
                obj.put("success", ejecutado);
                obj.put("id", idtransaccion);
            } else {
                obj.put("success", false);
            }
            stmt.close();
            conexion.close();
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public boolean sumarOTP(int idotp, String pin, String uuid, boolean valido) throws JSONException {
        //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo

        try {
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "update otpNir set uuid=?,codigo=?,enviosms=?, intento=?, valido=?, solicitudes=solicitudes+1 where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setString(1, uuid);
            stmt.setString(2, pin);
            stmt.setTimestamp(3, date);
            stmt.setInt(4, 0);
            stmt.setBoolean(5, valido);
            stmt.setInt(6, idotp);

            int ok = stmt.executeUpdate();
            stmt.close();
            conexion.close();

            if (ok == 1) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean resetOTP(int idotp, boolean valido) throws JSONException {
        //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo

        try {
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "update otpNir set solicitudes=?, valido=?, intento=? where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setInt(1, 0);
            stmt.setBoolean(2, valido);
            stmt.setInt(3, 0);
            stmt.setInt(4, idotp);

            int ok = stmt.executeUpdate();
            stmt.close();
            conexion.close();

            if (ok == 1) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public JSONObject getIdNir(String numero, String nir) throws JSONException, ProtocolException{
        altan altan = new altan();

        Calendar ahora = Calendar.getInstance();
        JSONObject respuesta = new JSONObject();
        int idusuario = 0;
        boolean cambioPermitido = false;
        int cambios = 0;
        Date ultimoCambio = null;
        JSONObject informacionnumero = altan.getinfo(numero);
        String iccid = informacionnumero.getJSONObject("informacion").getString("ICCID");
        String imsi = informacionnumero.getJSONObject("informacion").getString("IMSI");
        try {
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select id, iccid,numero, cambios, ultimoCambio from cambiosNir where iccid='" + iccid + "';");
            while (rs.next()) {
                idusuario = rs.getInt("id");
                cambios = rs.getInt("cambios");
                ultimoCambio = rs.getTimestamp("ultimoCambio");
            }
            conexion.close();
            st.close();

            if (idusuario == 0) {
                JSONObject nuevoNir = agregarNir(numero, iccid, imsi);
                idusuario = nuevoNir.getInt("id");
                cambioPermitido = true;
            } else {
               // System.out.println("Fecha Expiracion -> " + ultimoCambio);
                if (ultimoCambio != null) {
                    Calendar expiracion = Calendar.getInstance();
                    expiracion.setTime(ultimoCambio);
                    long milliseconds1 = expiracion.getTimeInMillis();
                    long milliseconds2 = ahora.getTimeInMillis();
                    long diff = milliseconds2 - milliseconds1;
                    long diffMinutes = diff / (60 * 1000);
                    //System.out.println("Time in minutes: " + diffMinutes + " minutos.");
                    // mayor a 3 meses el ultimo cambio
                    if (diffMinutes < (43800 * 3)) {
                        cambioPermitido = false;
                        respuesta.put("mensaje", "Lo sentimos, ha superado los cambios de número permitidos.");
                    } else {
                        cambioPermitido = true;
                    }
                } else {
                    cambioPermitido = true;
                }
            }

            if (cambioPermitido) {
                respuesta = altan.cambioNir(numero, nir, idusuario);
            } else {
                respuesta.put("success", false);
            }
            respuesta.put("idusuario", idusuario);

        } catch (Exception e) {
            System.out.println("Error -> " + e);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Error interno");
        }

        return respuesta;
    }

    public JSONObject agregarNir(String numero, String iccid, String imsi) throws JSONException {

        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "insert into cambiosNir(numero, iccid, imsi) values(?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, numero);
            stmt.setString(2, iccid);
            stmt.setString(3, imsi);

            int ok = stmt.executeUpdate();

            if (ok == 1) {
                ejecutado = true;
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                    System.out.println(numero + " cambio nir exitoso = " + rs.getInt(1));
                    idtransaccion = rs.getInt(1);
                }
                obj.put("success", ejecutado);
                obj.put("id", idtransaccion);
            } else {
                obj.put("success", false);
            }
            stmt.close();
            conexion.close();
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public JSONObject agregarNirNumero(int id, String nir, String nuevonumero) throws JSONException {

        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "insert into numero_nir(idcambionir,nir,nuevonumero ) values(?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, id);
            stmt.setString(2, nir);
            stmt.setString(3, nuevonumero);
            int ok = stmt.executeUpdate();
            if (ok == 1) {
                ejecutado = true;
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                    //System.out.println("ID usuario registrado = " + rs.getInt(1));
                    idtransaccion = rs.getInt(1);
                }
                obj.put("success", ejecutado);
                obj.put("id", idtransaccion);
            } else {
                obj.put("success", false);
            }
            stmt.close();
            conexion.close();
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public JSONObject agregarErrorNir(int codigo, String descripcion, String numero) throws JSONException {

        JSONObject obj = new JSONObject();
        boolean ejecutado = false;
        try {
            int idtransaccion = 0;
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "insert into errorNir(codigo,descripcion,numero ) values(?,?,?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, codigo);
            stmt.setString(2, descripcion);
            stmt.setString(3, numero);
            int ok = stmt.executeUpdate();
            if (ok == 1) {
                ejecutado = true;
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // aquí está el id generado para esta venta
                   // System.out.println("ID error nir registrado = " + rs.getInt(1) + " " + numero);
                    idtransaccion = rs.getInt(1);
                }
                obj.put("success", ejecutado);
                obj.put("id", idtransaccion);
            } else {
                obj.put("success", false);
            }
            stmt.close();
            conexion.close();
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            obj.put("success", false);
            return obj;
        }
    }

    public JSONObject getValidacionPin(String numero, String pin) throws Exception {
        conexionnir conn = new conexionnir();
        Calendar ahora = Calendar.getInstance();
        Date exp = null;
        boolean existe = false;
        boolean valido = false;
        String pinRegistrado = null;
        int intentos = 0;
        int idotp = 0;
        String uuid = null;
        int maximo = 3;
        JSONObject respuesta = new JSONObject();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "select id, uuid, intento, codigo, enviosms from otpNir where numero=?";
            stmt = conexion.prepareStatement(query);
            stmt.setString(1, numero);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existe = true;
                exp = rs.getTimestamp("enviosms");
                pinRegistrado = rs.getString("codigo");
                intentos = rs.getInt("intento");
                idotp = rs.getInt("id");
                uuid = rs.getString("uuid");
            }

            stmt.close();
            conexion.close();
            if (existe) {
                //System.out.println(numero + " Fecha Expiracion pin -> " + exp);
                Calendar expiracion = Calendar.getInstance();
                expiracion.setTime(exp);
                long milliseconds1 = expiracion.getTimeInMillis();
                long milliseconds2 = ahora.getTimeInMillis();
                long diff = milliseconds2 - milliseconds1;
                long diffMinutes = diff / (60 * 1000);
                long diffsegundos = diff / (1000);

                //System.out.println("Time in minutes: " + diffsegundos + " segundos.");

                if (diffsegundos <= 180) {
                    if ((intentos < maximo)) {
                        if (pinRegistrado.equals(pin)) {
                            valido = true;
                            respuesta.put("success", true);
                            respuesta.put("uuid", uuid);
                        } else {
                            respuesta.put("success", false);
                            respuesta.put("mensaje", "Lo sentimos, el PIN ingresado no es válido.");
                            System.out.println(numero + " Lo sentimos, el PIN ingresado no es válido.");
                        }
                    } else {
                        respuesta.put("success", false);
                        respuesta.put("mensaje", "Lo sentimos, has superado los intentos máximos de validaciones de OTP");
                        respuesta.put("redireccion", true);
                        System.out.println(numero + " Lo sentimos, has superado los intentos máximos de validaciones de OTP");
                    }
                    addIntentoOtp(idotp, valido);
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Lo sentimos, tu PIN esta expirado, por favor solicita uno nuevo");
                    respuesta.put("redireccion", true);
                    System.out.println(numero + " Lo sentimos, tu PIN esta expirado, por favor solicita uno nuevo");
                }
            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "Lo sentimos, debes de solicitar un PIN para poder realizar la validación");
                respuesta.put("redireccion", true);
                System.out.println(numero + " Lo sentimos, debes de solicitar un PIN para poder realizar la validación");
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Error interno");
            System.out.println(numero + " " + ex);
        }

        return respuesta;
    }

    public void addIntentoOtp(int id, boolean valido) {
        try {
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "update otpNir set valido=?, intento=intento+1 where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setBoolean(1, valido);
            stmt.setInt(2, id);

            int total = stmt.executeUpdate();
           // System.out.println("Ejecutado -> " + total);
        } catch (Exception e) {
            System.out.println("Error -> " + e);
        }

    }

    public void agregarSolicitudNir(int id) {
        try {
            conexionnir conn = new conexionnir();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            java.sql.Statement st = conexion.createStatement();
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "update cambiosNir set cambios=cambios+1, ultimoCambio=? where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setTimestamp(1, date);
            stmt.setInt(2, id);

            int total = stmt.executeUpdate();
            //System.out.println("Ejecutado -> " + total);
        } catch (Exception e) {
            System.out.println("Error -> " + e);
        }
    }

    public JSONObject getValidacionUUID(String numero, String uuid) throws Exception {
        conexionnir conn = new conexionnir();
        String uuidregistrado = null;
        JSONObject respuesta = new JSONObject();
        boolean existe = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo
            String query = "select numero, uuid from otpNir where numero=? and valido=?";
            stmt = conexion.prepareStatement(query);
            stmt.setString(1, numero);
            stmt.setBoolean(2, true);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existe = true;
                uuidregistrado = rs.getString("uuid");
            }

            stmt.close();
            conexion.close();
            if (existe) {
                if (uuidregistrado.equals(uuid)) {
                    respuesta.put("success", true);
                } else {
                    respuesta.put("success", false);
                    respuesta.put("mensaje", "Los datos ingresados son incorrectos.");
                }

            } else {
                respuesta.put("success", false);
                respuesta.put("mensaje", "No existe");
            }

        } catch (Exception ex) {
            Logger.getLogger(accesos.class.getName()).log(Level.SEVERE, null, ex);
            respuesta.put("success", false);
            respuesta.put("mensaje", "Error interno");
        }

        return respuesta;
    }

}
