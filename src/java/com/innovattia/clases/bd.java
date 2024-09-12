/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author 52556
 */
public class bd {

    Connection conexion = null;
    Statement st;
    boolean agregado = false;

    public JSONObject getidOtp(int id) throws JSONException {
        JSONObject rsp = new JSONObject();
        int otpusuario = 0;
        int sms = 0;
        int enviosmax = 0;
        Date ultimoEnvio = null;
        try {
            conexion conn = new conexion();
            PreparedStatement stmt = null;
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            String query = "select id, sms, enviosmax, fechaCreacion from validacionOTP where id_usuarios=?";
            stmt = conexion.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                otpusuario = rs.getInt("id");
                sms = rs.getInt("sms");
                enviosmax = rs.getInt("enviosmax");
                ultimoEnvio = rs.getTimestamp("fechaCreacion");
            }
            stmt.close();
            conexion.close();

            rsp.put("otpusuario", otpusuario);
            if (otpusuario > 0) {
                Calendar ahora = Calendar.getInstance();
                //System.out.println("Ultimo Envio -> " + ultimoEnvio);
                Calendar expiracion = Calendar.getInstance();
                expiracion.setTime(ultimoEnvio);
                long milliseconds1 = expiracion.getTimeInMillis();
                long milliseconds2 = ahora.getTimeInMillis();
                long diff = milliseconds2 - milliseconds1;
                long diffMinutes = diff / (60 * 1000);
                //System.out.println("Time in minutes: " + diffMinutes + " minutes.");

                if (diffMinutes < 60 && sms < enviosmax) {
                    rsp.put("success", true);
                } else {
                    if (diffMinutes < 60 && sms >= enviosmax) {
                        rsp.put("success", false);
                        expiracion.add(Calendar.HOUR, 1);
                        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        rsp.put("fechaDesbloqueo", df.format(expiracion.getTime()));
                    } else {
                        resetOTP(otpusuario, false);
                        rsp.put("success", true);
                    }
                }
            }else{
                rsp.put("success", false);
            }
        } catch (Exception e) {
            System.out.println("Error -> " + e);
            rsp.put("success", false);
        }
        return rsp;
    }

    public JSONObject agregarIntentoOTP(int idusuario) throws JSONException {
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
            String query = "insert into validacionOTP(id_usuarios) values (?)";
            stmt = conexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, idusuario);

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

    public boolean agregarOTP(int idotp, String pin, String uuid, boolean valido) throws JSONException {
        //id, id_usuarios, uuid, valido, pin, fechaCreacion, intentos, maximo

        try {
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "update validacionOTP set uuid=?,pin=?,fechaCreacion=?, intentos=?, valido=?, sms=sms+1 where id=?";
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
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement stmt = null;
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            String query = "update validacionOTP set sms=?, valido=? where id=?";
            stmt = conexion.prepareStatement(query);
            stmt.setInt(1, 0);
            stmt.setBoolean(2, valido);
            stmt.setInt(3, idotp);

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

    public JSONObject obteneridhbb(String numero, String servicio, int tipo) throws JSONException {
        JSONObject obj= new JSONObject();
        int idUsuario = 0;
        String email="";
        try {
            conexion conn = new conexion();
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(conn.host + conn.base, conn.usuario, conn.decrypt(conn.encrypt()));
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("select id, email from usuarios where numero='" + numero + "' and servicio='" + servicio + "' and tipo=" + tipo + " and activo=true;");
            while (rs.next()) {
                idUsuario = rs.getInt("id");
                email= rs.getString("email");
            }
            obj.put("idUsuario", idUsuario);
            obj.put("email", email);
            return obj;
        } catch (Exception e) {
            obj.put("idUsuario", 0);
            return obj;
        }
    }
}
