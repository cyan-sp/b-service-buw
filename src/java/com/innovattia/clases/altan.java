/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovattia.clases;

import com.innovattia.clases.nir.otp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author Innovattia
 */
public class altan {

    static String accessToken;
    static Calendar fechaCreacion;
    static boolean consumir;

    public boolean credencial() throws ProtocolException, JSONException {
        if (accessToken == null) {
            consumir = true;
        }
        Calendar calendar = Calendar.getInstance();
        try {
            String server = "https://altanredes-prod.apigee.net/v1/oauth/accesstoken?grant-type=client_credentials";
            HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            //connection.setRequestProperty("Authorization", "Basic Z3Y3cWlmSVZYQXRHN0sxRUFRZ0xpSEpvQUVWMVRTVUM6Nng5b2lPYVB1YTdNWWJNYg==");
            connection.setRequestProperty("Authorization", "Basic eVc1NXpBSnpHdkR1R25NNmdiYVE3TkFId0xXN3ptaTY6bFhBWU5Mb05uaEdHUUhRMA==");
            String parametros = "";
            OutputStream outs = connection.getOutputStream();
            OutputStreamWriter wout = new OutputStreamWriter(outs, "UTF-8");
            wout.write(parametros);
            wout.flush();
            String line = "";
            String line2 = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                line = line.replace(" ", "");
                line = line.replace("\n", "").replace("\r", "");
                line2 += line;
            }

            JSONObject json = new JSONObject(line2);
            outs.close();
            accessToken = json.getString("accessToken");
            fechaCreacion = calendar;

            return true;
        } catch (IOException ex) {
            Logger.getLogger(altan.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public JSONObject miApp(JSONObject respuestajson) throws ProtocolException, JSONException {
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }
        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / (60 * 60 * 1000);
        long diffMinutes = diff / (60 * 1000);
        if (diffHours >= 23 || accessToken.isEmpty()) {
            credencial();
        }
        String numero;
        if (respuestajson.getInt("tipo") == 2 || respuestajson.getInt("tipo") == 3) {
            numero = respuestajson.getString("servicio");
        } else {
            numero = respuestajson.getString("numero");
        }

        if (consumir == true) {
            try {
                String server = "https://altanredes-prod.apigee.net/ma/v1/self-consumption/" + numero + "/buckets?beId=142";
                HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                String line = "";
                String line2 = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\n", "").replace("\r", "");
                    line2 += line;
                }
                JSONObject json = new JSONObject(line2);
                JSONObject respuesta = new JSONObject();
                String proximaRecarga = "";
                int smsconsumidos = 0;
                int minutosconsumidos = 0;
                String fechaproximarecarga = "Hoy mismo";
                if (json.getJSONObject("sms").has("consumedAmt")) {
                    smsconsumidos = json.getJSONObject("sms").getInt("consumedAmt");
                }
                if (json.getJSONObject("minutes").has("consumedAmt")) {
                    minutosconsumidos = json.getJSONObject("minutes").getInt("consumedAmt");
                }
                if (respuestajson.getInt("tipo") == 2 || respuestajson.getInt("tipo") == 3) {
                    if (json.getJSONObject("freeUnits").has("expireDate")) {
                        proximaRecarga = json.getJSONObject("freeUnits").getString("expireDate");
                    }
                } else {
                    if (json.getJSONObject("minutes").has("expireDate")) {
                        proximaRecarga = json.getJSONObject("minutes").getString("expireDate");
                    }
                }
                int numeroOfertas = json.getJSONObject("freeUnits").getInt("count");

                if (proximaRecarga.length() > 1) {
                    fechaproximarecarga = proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4);
                }
                int datosTotal = json.getJSONObject("freeUnits").getInt("totalAmt");
                double datosDisponibles = json.getJSONObject("freeUnits").getDouble("unusedAmt");
                double datosConsumidos = json.getJSONObject("freeUnits").getDouble("consumedAmt");

                JSONObject datos = new JSONObject();
                datos.put("MB_totales", datosTotal);
                datos.put("MB_disponibles", datosDisponibles);
                datos.put("MB_usados", datosConsumidos);
                JSONObject consumo = new JSONObject();

                //System.out.println("json-> Se obtiene respuesta");
                respuesta.put("success", true);
                consumo.put("numero_recargas_vigentes", numeroOfertas);
                consumo.put("proxima_recarga", fechaproximarecarga);
                consumo.put("datos", datos);
                consumo.put("SMS_usados", smsconsumidos);
                consumo.put("MIN_usados", minutosconsumidos);
                respuesta.put("consumo", consumo);
                return respuesta;
            } catch (IOException ex) {
                JSONObject error = new JSONObject();
                error.put("success", false);
                return error;
            }
        } else {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("code", 500);
            return error;
        }
    }

    public JSONObject miPerfil(JSONObject respuestajson) throws ProtocolException, JSONException {
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }
        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / (60 * 60 * 1000);
        long diffMinutes = diff / (60 * 1000);
        //System.out.println("Time in hours: " + diffHours + " hours.");
        // System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        if (diffHours >= 23 || accessToken.isEmpty()) {
            System.out.println("Se genera nueva llave");
            credencial();
        }
        String numero;
        int tipo = respuestajson.getInt("tipo");
        if (tipo == 2 || tipo == 3) {
            numero = respuestajson.getString("servicio");
        } else {
            numero = respuestajson.getString("numero");
        }

        if (consumir == true) {
            try {
                String server = "https://altanredes-prod.apigee.net/cm/v1/subscribers/" + numero + "/profile";
                HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                String line = "";
                String line2 = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\n", "").replace("\r", "");
                    line2 += line;
                }
                JSONObject json = new JSONObject(line2);
                JSONObject respuesta = new JSONObject();
                String proximaRecarga = "";

                int smsconsumidos = 0;
                int minutosconsumidos = 0;
                double datosReducida = 0;
                double datosReducidaDisponible = 0;
                double datosNormal = 0;
                double datosNormalDisponible = 0;
                double datosInternacionales = 0;
                double datosInternacionalesDisponible = 0;
                double redesSociales = 0;
                double redesSocialesDisponible = 0;
                Date fechaVencimiento = null;
                boolean esIlimitado = false;
                boolean comparteDatos = false;
                JSONObject resultado = json.getJSONObject("responseSubscriber");
                JSONArray bolsas = resultado.getJSONArray("freeUnits");
                JSONArray offeringid = new JSONArray();
                if (tipo == 2 || tipo == 3) {
                    for (int i = 0; i < bolsas.length(); i++) {
                        JSONObject detalle = bolsas.getJSONObject(i);
                        if (detalle.getString("name").equals("Free Units") || detalle.getString("name").equals("FU_Altan-RN")) {
                            datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));

                            JSONArray ofertas = detalle.getJSONArray("detailOfferings");
                            for (int c = 0; c < ofertas.length(); c++) {
                                JSONObject ofertasdetalle = ofertas.getJSONObject(c);
                                if (ofertasdetalle.has("expireDate")) {
                                    String offering = ofertasdetalle.getString("offeringId");
                                    proximaRecarga = ofertasdetalle.getString("expireDate");
                                    JSONObject offeringdetalle = new JSONObject();
                                    offeringdetalle.put("offeringId", offering);
                                    offeringdetalle.put("expireDate", proximaRecarga);
                                    offeringid.put(offeringdetalle);

                                    //System.out.println("proxima recarga " + proximaRecarga);
                                    Date fechaExp = ParseFecha(proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4));

                                    if (fechaVencimiento == null) {
                                        fechaVencimiento = fechaExp;
                                    } else {
                                        Date dateMayor = fechaMayor(fechaVencimiento, fechaExp);
                                        fechaVencimiento = dateMayor;
                                    }
                                }
                            }
                        } else if (detalle.getString("name").startsWith("FU_RS_")) {
                            redesSociales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            redesSocialesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        }
                    }

                    JSONArray consumo = new JSONArray();
                    JSONObject datosnormal = new JSONObject();
                    datosnormal.put("nombre", "datosVelocidadNormal");
                    datosnormal.put("mb_totales", datosNormal);
                    datosnormal.put("mb_disponibles", datosNormalDisponible);
                    datosnormal.put("mb_usados", datosNormal - datosNormalDisponible);

                    JSONObject dredesSociales = new JSONObject();
                    dredesSociales.put("nombre", "datosRedesSociales");
                    dredesSociales.put("mb_totales", redesSociales);
                    dredesSociales.put("mb_disponibles", redesSocialesDisponible);
                    dredesSociales.put("mb_usados", redesSociales - redesSocialesDisponible);

                    if (fechaVencimiento == null) {
                        respuesta.put("proxima_recarga", "Hoy mismo");
                    } else {
                        respuesta.put("proxima_recarga", formatoFecha(fechaVencimiento));
                    }

                    consumo.put(datosnormal);
                    consumo.put(dredesSociales);

                    respuesta.put("offeringsIds", offeringid);
                    respuesta.put("consumo", consumo);
                    respuesta.put("success", true);
                    return respuesta;

                } else {

                    for (int i = 0; i < bolsas.length(); i++) {
                        try {
                            JSONObject detalle = bolsas.getJSONObject(i);
                            if (detalle.getString("name").endsWith("kbps") || detalle.getString("name").endsWith("kbps_CT_P2") || detalle.getString("name").endsWith("kbps_CT")) {
                                datosReducida += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosReducidaDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_Data_Altan") && (!detalle.getString("name").endsWith("RG21") && !detalle.getString("name").endsWith("RG18"))) {
                                datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                if (detalle.getString("name").startsWith("FU_Data_Altan-NR-IR")) {
                                    datosInternacionales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                    datosInternacionalesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                }
                                JSONArray ofertas = detalle.getJSONArray("detailOfferings");
                                for (int c = 0; c < ofertas.length(); c++) {
                                    JSONObject ofertasdetalle = ofertas.getJSONObject(c);
                                    if (ofertasdetalle.has("expireDate")) {
                                        String offering = ofertasdetalle.getString("offeringId");
                                        proximaRecarga = ofertasdetalle.getString("expireDate");
                                        JSONObject offeringdetalle = new JSONObject();
                                        offeringdetalle.put("offeringId", offering);
                                        offeringdetalle.put("expireDate", proximaRecarga);
                                        offeringid.put(offeringdetalle);

                                        if (offering.equals("1809933023") || offering.equals("1709933007") || offering.equals("1879933001") || offering.equals("1779933001")
                                                || offering.equals("1809933027") || offering.equals("1809933026") || offering.equals("1809933025") || offering.equals("1879933006")
                                                || offering.equals("1879933005") || offering.equals("1879933004") || offering.equals("1779933002") || offering.equals("1879933002") || offering.equals("1779933000")
                                                || offering.equals("1879933000") || offering.equals("1679933000") || offering.equals("1679933001") || offering.equals("1679933002") || offering.equals("1609933000")
                                                || offering.equals("1609933001") || offering.equals("1609933002") || offering.equals("1809933027") || offering.equals("1679933004")
                                                || offering.equals("1879933010") || offering.equals("1609933005") || offering.equals("1809933034") || offering.equals("1709933009") || offering.equals("1809933029")
                                                || offering.equals("1609933004") || offering.equals("1809933031") || offering.equals("1609933017") || offering.equals("1609933009")
                                                || offering.equals("1809933056") || offering.equals("1609933010") || offering.equals("1809933057") || offering.equals("1609933013")
                                                || offering.equals("1809933060") || offering.equals("1609933014") || offering.equals("1809933061") || offering.equals("1609933015")
                                                || offering.equals("1809933062") || offering.equals("1609933016") || offering.equals("1809933063") || offering.equals("1679933006")
                                                || offering.equals("1879933018") || offering.equals("1679933007") || offering.equals("1879933019") || offering.equals("1679933008")
                                                || offering.equals("1879933020") || offering.equals("1679933009") || offering.equals("1879933021") || offering.equals("1709933008")
                                                || offering.equals("1809933024") || offering.equals("1779933008") || offering.equals("1879933014")) {
                                            esIlimitado = true;
                                        }

                                        if (offering.equals("1809933061") || offering.equals("1809933060") || offering.equals("1809933059") || offering.equals("1809933058") || offering.equals("1809933057") || offering.equals("1809933056") || offering.equals("1809933055") || offering.equals("1809933054") || offering.equals("1809933031") || offering.equals("1809933005") || offering.equals("1809933049") || offering.equals("1809933001") || offering.equals("1809933000") || offering.equals("1809933027") || offering.equals("1809933026") || offering.equals("1809933025") || offering.equals("1809933023") || offering.equals("1809933029")) {
                                            comparteDatos = true;
                                        }

                                        Date fechaExp = ParseFecha(proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4));

                                        if (fechaVencimiento == null) {
                                            fechaVencimiento = fechaExp;
                                        } else {
                                            Date dateMayor = fechaMayor(fechaVencimiento, fechaExp);
                                            fechaVencimiento = dateMayor;
                                        }
                                    }
                                }
                            } else if (detalle.getString("name").startsWith("FreeData_Altan-RN")) {
                                datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_RS_")) {
                                redesSociales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                redesSocialesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_SMS_Altan")) {
                                smsconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_Min_Altan") || detalle.getString("name").startsWith("FreeMinutes")) {
                                minutosconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONArray consumo = new JSONArray();
                    JSONObject datosnormal = new JSONObject();
                    datosnormal.put("nombre", "datosVelocidadNormal");
                    datosnormal.put("mb_totales", datosNormal);
                    datosnormal.put("mb_disponibles", datosNormalDisponible);
                    datosnormal.put("mb_usados", datosNormal - datosNormalDisponible);

                    JSONObject datosreducido = new JSONObject();
                    datosreducido.put("nombre", "datosVelocidadReducida");
                    datosreducido.put("mb_totales", datosReducida);
                    datosreducido.put("mb_disponibles", datosReducidaDisponible);
                    datosreducido.put("mb_usados", datosReducida - datosReducidaDisponible);

                    JSONObject datosInternacional = new JSONObject();
                    datosInternacional.put("nombre", "datosUsoInternacional");
                    datosInternacional.put("mb_totales", datosInternacionales);
                    datosInternacional.put("mb_disponibles", datosInternacionalesDisponible);
                    datosInternacional.put("mb_usados", datosInternacionales - datosInternacionalesDisponible);

                    JSONObject dredesSociales = new JSONObject();
                    dredesSociales.put("nombre", "datosRedesSociales");
                    dredesSociales.put("mb_totales", redesSociales);
                    dredesSociales.put("mb_disponibles", redesSocialesDisponible);
                    dredesSociales.put("mb_usados", redesSociales - redesSocialesDisponible);

                    respuesta.put("success", true);
                    respuesta.put("esIlimitado", esIlimitado);
                    respuesta.put("comparteDatos", comparteDatos);
                    if (fechaVencimiento == null) {
                        respuesta.put("proxima_recarga", "Hoy mismo");
                    } else {
                        respuesta.put("proxima_recarga", formatoFecha(fechaVencimiento));
                    }

                    consumo.put(datosnormal);
                    consumo.put(datosreducido);
                    consumo.put(datosInternacional);
                    consumo.put(dredesSociales);
                    respuesta.put("min_usados", minutosconsumidos);
                    respuesta.put("sms_usados", smsconsumidos);

                    respuesta.put("offeringsIds", offeringid);
                    respuesta.put("consumo", consumo);
                    return respuesta;
                }

            } catch (IOException ex) {
                JSONObject error = new JSONObject();
                error.put("success", false);
                return error;
            }

        } else {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("code", 500);
            return error;
        }
    }

    public JSONObject miPerfilv3(JSONObject respuestajson) throws ProtocolException, JSONException {
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }
        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / (60 * 60 * 1000);
        long diffMinutes = diff / (60 * 1000);
        //System.out.println("Time in hours: " + diffHours + " hours.");
        // System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        if (diffHours >= 23 || accessToken.isEmpty()) {
            System.out.println("Se genera nueva llave");
            credencial();
        }
        String numero;
        int tipo = respuestajson.getInt("tipo");
        if (tipo == 2 || tipo == 3) {
            numero = respuestajson.getString("servicio");
        } else {
            numero = respuestajson.getString("numero");
        }

        if (consumir == true) {
            try {
                String server = "https://altanredes-prod.apigee.net/cm/v1/subscribers/" + numero + "/profile";
                HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                String line = "";
                String line2 = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\n", "").replace("\r", "");
                    line2 += line;
                }
                JSONObject json = new JSONObject(line2);
                JSONObject respuesta = new JSONObject();
                String proximaRecarga = "";

                int smsconsumidos = 0;
                int smsRestantes = 0;
                int minutosconsumidos = 0;
                int minutosRestantes = 0;
                double datosReducida = 0;
                double datosReducidaDisponible = 0;
                double datosNormal = 0;
                double datosNormalDisponible = 0;
                double datosInternacionales = 0;
                double datosInternacionalesDisponible = 0;
                double redesSociales = 0;
                double redesSocialesDisponible = 0;
                Date fechaVencimiento = null;
                boolean esIlimitado = false;
                boolean comparteDatos = false;
                JSONObject resultado = json.getJSONObject("responseSubscriber");
                JSONArray bolsas = resultado.getJSONArray("freeUnits");
                JSONArray offeringid = new JSONArray();
                if (tipo == 2 || tipo == 3) {
                    for (int i = 0; i < bolsas.length(); i++) {
                        JSONObject detalle = bolsas.getJSONObject(i);
                        if (detalle.getString("name").equals("Free Units") || detalle.getString("name").equals("FU_Altan-RN")) {
                            datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));

                            JSONArray ofertas = detalle.getJSONArray("detailOfferings");
                            for (int c = 0; c < ofertas.length(); c++) {
                                JSONObject ofertasdetalle = ofertas.getJSONObject(c);
                                if (ofertasdetalle.has("expireDate")) {
                                    String offering = ofertasdetalle.getString("offeringId");
                                    proximaRecarga = ofertasdetalle.getString("expireDate");
                                    JSONObject offeringdetalle = new JSONObject();
                                    offeringdetalle.put("offeringId", offering);
                                    offeringdetalle.put("expireDate", proximaRecarga);
                                    offeringid.put(offeringdetalle);

                                    //System.out.println("proxima recarga " + proximaRecarga);
                                    Date fechaExp = ParseFecha(proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4));

                                    if (fechaVencimiento == null) {
                                        fechaVencimiento = fechaExp;
                                    } else {
                                        Date dateMayor = fechaMayor(fechaVencimiento, fechaExp);
                                        fechaVencimiento = dateMayor;
                                    }
                                }
                            }
                        } else if (detalle.getString("name").startsWith("FU_RS_")) {
                            redesSociales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            redesSocialesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        }
                    }

                    JSONArray consumo = new JSONArray();
                    JSONObject datosnormal = new JSONObject();
                    datosnormal.put("nombre", "datosVelocidadNormal");
                    datosnormal.put("mb_totales", datosNormal);
                    datosnormal.put("mb_disponibles", datosNormalDisponible);
                    datosnormal.put("mb_usados", datosNormal - datosNormalDisponible);

                    JSONObject dredesSociales = new JSONObject();
                    dredesSociales.put("nombre", "datosRedesSociales");
                    dredesSociales.put("mb_totales", redesSociales);
                    dredesSociales.put("mb_disponibles", redesSocialesDisponible);
                    dredesSociales.put("mb_usados", redesSociales - redesSocialesDisponible);

                    if (fechaVencimiento == null) {
                        respuesta.put("proxima_recarga", "Hoy mismo");
                    } else {
                        respuesta.put("proxima_recarga", formatoFecha(fechaVencimiento));
                    }

                    consumo.put(datosnormal);
                    consumo.put(dredesSociales);

                    respuesta.put("offeringsIds", offeringid);
                    respuesta.put("consumo", consumo);
                    respuesta.put("success", true);
                    return respuesta;

                } else {

                    for (int i = 0; i < bolsas.length(); i++) {
                        try {
                            JSONObject detalle = bolsas.getJSONObject(i);
                            if (detalle.getString("name").endsWith("kbps") || detalle.getString("name").endsWith("kbps_CT_P2") || detalle.getString("name").endsWith("kbps_CT")) {
                                datosReducida += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosReducidaDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_Data_Altan") && (!detalle.getString("name").endsWith("RG21") && !detalle.getString("name").endsWith("RG18"))) {
                                datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                if (detalle.getString("name").startsWith("FU_Data_Altan-NR-IR")) {
                                    datosInternacionales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                    datosInternacionalesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                }
                                JSONArray ofertas = detalle.getJSONArray("detailOfferings");
                                for (int c = 0; c < ofertas.length(); c++) {
                                    JSONObject ofertasdetalle = ofertas.getJSONObject(c);
                                    if (ofertasdetalle.has("expireDate")) {
                                        String offering = ofertasdetalle.getString("offeringId");
                                        proximaRecarga = ofertasdetalle.getString("expireDate");
                                        JSONObject offeringdetalle = new JSONObject();
                                        offeringdetalle.put("offeringId", offering);
                                        offeringdetalle.put("expireDate", proximaRecarga);
                                        offeringid.put(offeringdetalle);

                                        if (offering.equals("1879933001") || offering.equals("1809933023") || offering.equals("1809933029") || offering.equals("1809933025")
                                                || offering.equals("1809933026") || offering.equals("1809933027") || offering.equals("1879933006") || offering.equals("1779933001")
                                                || offering.equals("1709933007") || offering.equals("1709933009") || offering.equals("1609933000") || offering.equals("1609933001") || offering.equals("1609933002")
                                                || offering.equals("1679933002")) {
                                            esIlimitado = true;
                                        }

                                        if (offering.equals("1809933182") || offering.equals("1809933183") || offering.equals("1809933186") || offering.equals("1809933023") || offering.equals("1809933215") || offering.equals("1809933216") || offering.equals("1809933029") || offering.equals("1809933025") || offering.equals("1809933026") 
                                                || offering.equals("1809933027") || offering.equals("1809933211") || offering.equals("1809933212") || offering.equals("1809933213") || offering.equals("1809933214") || offering.equals("1809933024")
                                                || offering.equals("1709933023") || offering.equals("1709933024") || offering.equals("1709933027") || offering.equals("1709933007") || offering.equals("1709933028") || offering.equals("1709933029") || offering.equals("1709933009") || offering.equals("1609933000") || offering.equals("1609933001") 
                                                || offering.equals("1609933002") || offering.equals("1609933053") || offering.equals("1609933054") || offering.equals("1609933055") || offering.equals("1609933056") || offering.equals("1709933008")) {
                                            comparteDatos = true;
                                        }

                                        Date fechaExp = ParseFecha(proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4));
                                        if (offering.equals("1809933007") || offering.equals("1809933012") || offering.equals("1809933064") || offering.equals("1809933065") || offering.equals("1809933066")
                                                || offering.equals("1809933067") || offering.equals("1809933068") || offering.equals("1809933069") || offering.equals("1809933070")
                                                || offering.equals("1809933071") || offering.equals("1809933072") || offering.equals("1809933073") || offering.equals("1809933074") || offering.equals("1809933075")
                                                || offering.equals("1809933076") || offering.equals("1809933077") || offering.equals("1809933078") || offering.equals("1809933079")
                                                || offering.equals("1809933080") || offering.equals("1809933081") || offering.equals("1809933082") || offering.equals("1809933138") || offering.equals("1809933139") || offering.equals("1809933140")
                                                || offering.equals("1879933022")) {
                                            System.out.println("Offering de Suma en el usuario "+numero);
                                        } else {
                                            if (fechaVencimiento == null) {
                                                fechaVencimiento = fechaExp;
                                            } else {
                                                Date dateMayor = fechaMayor(fechaVencimiento, fechaExp);
                                                fechaVencimiento = dateMayor;
                                            }
                                        }

                                    }
                                }
                            } else if (detalle.getString("name").startsWith("FreeData_Altan-RN")) {
                                datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_RS_")) {
                                redesSociales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                redesSocialesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_SMS_Altan")) {
                                smsconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                smsRestantes += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            } else if (detalle.getString("name").startsWith("FU_Min_Altan") || detalle.getString("name").startsWith("FreeMinutes")) {
                                minutosconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                                minutosRestantes += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONArray consumo = new JSONArray();
                    JSONObject datosnormal = new JSONObject();
                    datosnormal.put("nombre", "datosVelocidadNormal");
                    datosnormal.put("mb_totales", datosNormal);
                    datosnormal.put("mb_disponibles", datosNormalDisponible);
                    datosnormal.put("mb_usados", datosNormal - datosNormalDisponible);

                    JSONObject datosreducido = new JSONObject();
                    datosreducido.put("nombre", "datosVelocidadReducida");
                    datosreducido.put("mb_totales", datosReducida);
                    datosreducido.put("mb_disponibles", datosReducidaDisponible);
                    datosreducido.put("mb_usados", datosReducida - datosReducidaDisponible);

                    JSONObject datosInternacional = new JSONObject();
                    datosInternacional.put("nombre", "datosUsoInternacional");
                    datosInternacional.put("mb_totales", datosInternacionales);
                    datosInternacional.put("mb_disponibles", datosInternacionalesDisponible);
                    datosInternacional.put("mb_usados", datosInternacionales - datosInternacionalesDisponible);

                    JSONObject dredesSociales = new JSONObject();
                    dredesSociales.put("nombre", "datosRedesSociales");
                    dredesSociales.put("mb_totales", redesSociales);
                    dredesSociales.put("mb_disponibles", redesSocialesDisponible);
                    dredesSociales.put("mb_usados", redesSociales - redesSocialesDisponible);

                    respuesta.put("success", true);
                    respuesta.put("esIlimitado", esIlimitado);
                    respuesta.put("comparteDatos", comparteDatos);
                    if (fechaVencimiento == null) {
                        respuesta.put("proxima_recarga", "Hoy mismo");
                    } else {
                        respuesta.put("proxima_recarga", formatoFecha(fechaVencimiento));
                    }

                    consumo.put(datosnormal);
                    consumo.put(datosreducido);
                    consumo.put(datosInternacional);
                    consumo.put(dredesSociales);
                    JSONObject minutos = new JSONObject();
                    minutos.put("nombre", "minutos");
                    minutos.put("min_usados", minutosconsumidos);
                    minutos.put("min_disponibles", minutosRestantes);

                    JSONObject sms = new JSONObject();
                    sms.put("nombre", "sms");
                    sms.put("sms_usados", smsconsumidos);
                    sms.put("sms_disponibles", smsRestantes);

                    consumo.put(minutos);
                    consumo.put(sms);
                    respuesta.put("offeringsIds", offeringid);
                    respuesta.put("consumo", consumo);
                    return respuesta;
                }

            } catch (IOException ex) {
                JSONObject error = new JSONObject();
                error.put("success", false);
                return error;
            }

        } else {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("code", 500);
            return error;
        }
    }

    public JSONObject miSaldo(String numero) throws ProtocolException, JSONException {
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }
        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / (60 * 60 * 1000);
        long diffMinutes = diff / (60 * 1000);
        //System.out.println("Time in hours: " + diffHours + " hours.");
        //System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        if (diffHours >= 23 || accessToken.isEmpty()) {
            System.out.println("Se genera nueva llave");
            credencial();
        }

        if (consumir == true) {
            try {
                String server = "https://altanredes-prod.apigee.net/cm/v1/subscribers/" + numero + "/profile";
                HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                String line = "";
                String line2 = "";
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    line = line.replace("\n", "").replace("\r", "");
                    line2 += line;
                }
                JSONObject json = new JSONObject(line2);
                JSONObject respuesta = new JSONObject();
                String proximaRecarga = "";

                int smsconsumidos = 0;
                int smsRestantes = 0;
                int minutosconsumidos = 0;
                int minutosRestantes = 0;
                double datosReducida = 0;
                double datosReducidaDisponible = 0;
                double datosNormal = 0;
                double datosNormalDisponible = 0;
                double datosInternacionales = 0;
                double datosInternacionalesDisponible = 0;
                double redesSociales = 0;
                double redesSocialesDisponible = 0;
                Date fechaVencimiento = null;
                boolean esIlimitado = false;
                boolean comparteDatos = false;
                JSONObject resultado = json.getJSONObject("responseSubscriber");
                JSONArray bolsas = resultado.getJSONArray("freeUnits");
                JSONArray offeringid = new JSONArray();

                for (int i = 0; i < bolsas.length(); i++) {
                    try {
                        JSONObject detalle = bolsas.getJSONObject(i);
                        if (detalle.getString("name").endsWith("kbps") || detalle.getString("name").endsWith("kbps_CT_P2") || detalle.getString("name").endsWith("kbps_CT")) {
                            datosReducida += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            datosReducidaDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        } else if (detalle.getString("name").startsWith("FU_Data_Altan") && (!detalle.getString("name").endsWith("RG21") && !detalle.getString("name").endsWith("RG18"))) {
                            datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            if (detalle.getString("name").startsWith("FU_Data_Altan-NR-IR")) {
                                datosInternacionales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                                datosInternacionalesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            }
                            JSONArray ofertas = detalle.getJSONArray("detailOfferings");
                            for (int c = 0; c < ofertas.length(); c++) {
                                JSONObject ofertasdetalle = ofertas.getJSONObject(c);
                                if (ofertasdetalle.has("expireDate")) {
                                    String offering = ofertasdetalle.getString("offeringId");
                                    proximaRecarga = ofertasdetalle.getString("expireDate");
                                    JSONObject offeringdetalle = new JSONObject();
                                    offeringdetalle.put("offeringId", offering);
                                    offeringdetalle.put("expireDate", proximaRecarga);
                                    offeringid.put(offeringdetalle);

                                    if (offering.equals("1809933023") || offering.equals("1709933007") || offering.equals("1879933001") || offering.equals("1779933001")
                                            || offering.equals("1809933027") || offering.equals("1809933026") || offering.equals("1809933025") || offering.equals("1879933006")
                                            || offering.equals("1879933005") || offering.equals("1879933004") || offering.equals("1779933002") || offering.equals("1879933002") || offering.equals("1779933000")
                                            || offering.equals("1879933000") || offering.equals("1679933000") || offering.equals("1679933001") || offering.equals("1679933002") || offering.equals("1609933000")
                                            || offering.equals("1609933001") || offering.equals("1609933002") || offering.equals("1809933027") || offering.equals("1679933004")
                                            || offering.equals("1879933010") || offering.equals("1609933005") || offering.equals("1809933034") || offering.equals("1709933009") || offering.equals("1809933029")
                                            || offering.equals("1609933004") || offering.equals("1809933031") || offering.equals("1609933017") || offering.equals("1609933009")
                                            || offering.equals("1809933056") || offering.equals("1609933010") || offering.equals("1809933057") || offering.equals("1609933013")
                                            || offering.equals("1809933060") || offering.equals("1609933014") || offering.equals("1809933061") || offering.equals("1609933015")
                                            || offering.equals("1809933062") || offering.equals("1609933016") || offering.equals("1809933063") || offering.equals("1679933006")
                                            || offering.equals("1879933018") || offering.equals("1679933007") || offering.equals("1879933019") || offering.equals("1679933008")
                                            || offering.equals("1879933020") || offering.equals("1679933009") || offering.equals("1879933021") || offering.equals("1709933008")
                                            || offering.equals("1809933024") || offering.equals("1779933008") || offering.equals("1879933014")) {
                                        esIlimitado = true;
                                    }

                                    if (offering.equals("1809933061") || offering.equals("1809933060") || offering.equals("1809933059") || offering.equals("1809933058") || offering.equals("1809933057") || offering.equals("1809933056") || offering.equals("1809933055") || offering.equals("1809933054") || offering.equals("1809933031") || offering.equals("1809933005") || offering.equals("1809933049") || offering.equals("1809933001") || offering.equals("1809933000") || offering.equals("1809933027") || offering.equals("1809933026") || offering.equals("1809933025") || offering.equals("1809933023") || offering.equals("1809933029")) {
                                        comparteDatos = true;
                                    }

                                    Date fechaExp = ParseFecha(proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4));

                                    if (fechaVencimiento == null) {
                                        fechaVencimiento = fechaExp;
                                    } else {
                                        Date dateMayor = fechaMayor(fechaVencimiento, fechaExp);
                                        fechaVencimiento = dateMayor;
                                    }
                                }
                            }
                        } else if (detalle.getString("name").startsWith("FreeData_Altan-RN")) {
                            datosNormal += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            datosNormalDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        } else if (detalle.getString("name").startsWith("FU_RS_")) {
                            redesSociales += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("totalAmt"));
                            redesSocialesDisponible += Double.parseDouble(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        } else if (detalle.getString("name").startsWith("FU_SMS_Altan")) {
                            smsconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            smsRestantes += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        } else if (detalle.getString("name").startsWith("FU_Min_Altan") || detalle.getString("name").startsWith("FreeMinutes")) {
                            minutosconsumidos += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("totalAmt")) - Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                            minutosRestantes += Integer.parseInt(detalle.getJSONObject("freeUnit").getString("unusedAmt"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                respuesta.put("success", true);
                respuesta.put("esIlimitado", esIlimitado);
                respuesta.put("comparteDatos", comparteDatos);
                if (fechaVencimiento == null) {
                    respuesta.put("proxima_recarga", "Hoy mismo");
                } else {
                    respuesta.put("proxima_recarga", formatoFecha(fechaVencimiento));
                }

                respuesta.put("offeringsIds", offeringid);
                respuesta.put("minutos", minutosRestantes);
                respuesta.put("sms", smsRestantes);
                respuesta.put("datos", datosInternacionalesDisponible + datosNormalDisponible + datosReducidaDisponible + redesSocialesDisponible);
                return respuesta;

            } catch (IOException ex) {
                JSONObject error = new JSONObject();
                error.put("success", false);
                return error;
            }

        } else {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("code", 500);
            return error;
        }
    }

    public static Date ParseFecha(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        Date fechaDate = null;
        try {
            fechaDate = formato.parse(fecha);
        } catch (ParseException ex) {
            System.out.println(ex);
        }

        return fechaDate;
    }

    public static String formatoFecha(Date fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String dateToString = formato.format(fecha);
        return dateToString;
    }

    public Date fechaMayor(Date fecha1, Date fecha2) {

        if (fecha1.after(fecha2)) {
            return fecha1;
        } else {
            return fecha2;
        }

    }

    public String cambiar(String valor) {
        if (valor.equals("0")) {
            consumir = false;
            return "Se ha desactivado la consulta Altan";
        } else {
            consumir = true;
            return "Se ha actuvado la consulta Altan";
        }
    }

    public JSONObject ultimaRecarga(String numero) throws ProtocolException, JSONException {
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }
        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / (60 * 60 * 1000);
        long diffMinutes = diff / (60 * 1000);
        if (diffHours >= 23 || accessToken.isEmpty()) {
            System.out.println("Se genera nueva llave");
            credencial();
        }
        try {
            String server = "https://altanredes-prod.apigee.net/cm-360/v1/subscribers/" + numero + "/recordOfferings?startDate=2020092300&endDate=2020092419&reportMode=true&page=1&limit=50";
            HttpURLConnection connection = (HttpURLConnection) (new URL(server).openConnection());
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            String line = "";
            String line2 = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                line = line.replace("\n", "").replace("\r", "");
                line2 += line;
            }
            JSONObject json = new JSONObject(line2);
            JSONObject respuesta = new JSONObject();
            int numeroOfertas = json.getJSONObject("freeUnits").getInt("count");
            String proximaRecarga = json.getJSONObject("freeUnits").getString("expireDate");
            String fechaproximarecarga = proximaRecarga.substring(6, 8) + "/" + proximaRecarga.substring(4, 6) + "/" + proximaRecarga.substring(0, 4);
            int datosTotal = json.getJSONObject("freeUnits").getInt("totalAmt");
            double datosDisponibles = json.getJSONObject("freeUnits").getDouble("unusedAmt");
            double datosConsumidos = json.getJSONObject("freeUnits").getDouble("consumedAmt");
            int smsconsumidos = json.getJSONObject("sms").getInt("consumedAmt");
            int minutosconsumidos = json.getJSONObject("minutes").getInt("consumedAmt");
            JSONObject datos = new JSONObject();
            datos.put("MB_totales", datosTotal);
            datos.put("MB_disponibles", datosDisponibles);
            datos.put("MB_usados", datosConsumidos);
            JSONObject consumo = new JSONObject();

            //System.out.println("json-> Se obtiene respuesta");
            respuesta.put("success", true);
            consumo.put("numero_recargas_vigentes", numeroOfertas);
            consumo.put("proxima_recarga", fechaproximarecarga);
            consumo.put("datos", datos);
            consumo.put("SMS_usados", smsconsumidos);
            consumo.put("MIN_usados", minutosconsumidos);
            respuesta.put("consumo", consumo);
            return respuesta;
        } catch (IOException ex) {
            JSONObject error = new JSONObject();
            error.put("success", false);
            return error;
        }
    }

    public JSONObject cdn() throws JSONException, IOException, Exception {
        accesos ac = new accesos();
        String url = ac.getCdn();
        JSONObject data = new JSONObject();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet requestget = new HttpGet(url);
        HttpResponse responseget = httpClient.execute(requestget);
        int responseCode = responseget.getStatusLine().getStatusCode();
        if (responseCode == 200) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(responseget.getEntity().getContent()));
            String line = "";
            String line2 = "";
            while ((line = rd.readLine()) != null) {
                line = line.replace("\n", "").replace("\r", "");
                line2 += line;
            }
            data = new JSONObject(line2);
        } else {
            data.put("error", 500);
        }
        return data;
    }

    public JSONObject getinfo(String numero) throws ProtocolException, JSONException {
        JSONObject respuesta = new JSONObject();
        Calendar ahora = Calendar.getInstance();
        if (fechaCreacion == null) {
            credencial();
        }

        long milliseconds1 = fechaCreacion.getTimeInMillis();
        long milliseconds2 = ahora.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;
        long diffHours = diff / 3600000L;
        long diffMinutes = diff / 60000L;
        //System.out.println("Time in hours: " + diffHours + " hours.");
        //System.out.println("Time in minutes: " + diffMinutes + " minutes.");
        if (diffHours >= 23L || accessToken.isEmpty()) {
            System.out.println("Se genera nueva llave");
            credencial();
        }

        if (consumir) {
            try {
                String server = "https://altanredes-prod.apigee.net/cm/v1/subscribers/" + numero + "/profile";
                HttpURLConnection connection = (HttpURLConnection) ((HttpURLConnection) (new URL(server)).openConnection());
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                String line = "";
                String line2 = "";

                for (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); (line = reader.readLine()) != null; line2 = line2 + line) {
                    line = line.replace("\n", "").replace("\r", "");
                }

                JSONObject json = new JSONObject(line2);
                respuesta.put("informacion", json.getJSONObject("responseSubscriber").getJSONObject("information"));
                //System.out.println("INFO " + respuesta);
                return respuesta;
            } catch (IOException var20) {
                JSONObject error = new JSONObject();
                error.put("success", false);
                return error;
            }
        } else {
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("code", 500);
            return error;
        }
    }

    public JSONObject cambioNir(String numero, String nir, int idusuario) throws JSONException {
        JSONObject respuesta = new JSONObject();
        try {

            Calendar ahora = Calendar.getInstance();
            if (fechaCreacion == null) {
                credencial();
            }
            long milliseconds1 = fechaCreacion.getTimeInMillis();
            long milliseconds2 = ahora.getTimeInMillis();
            long diff = milliseconds2 - milliseconds1;
            long diffHours = diff / (60 * 60 * 1000);
            if (diffHours >= 23 || accessToken.isEmpty()) {
                System.out.println("Se genera nueva llave");
                credencial();
            }

            StringEntity params = new StringEntity("{\"changeSubscriberMSISDN\": {\"nir\": \"" + nir + "\",\"msisdnType\": \"1\"}}");
            HttpClient httpClient = HttpClientBuilder.create().build();
            String url = "https://altanredes-prod.apigee.net/cm/v1/subscribers/" + numero;

            HttpPatch request = new HttpPatch(url);

            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "Bearer " + accessToken);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("Response Code cambio nir : " + responseCode);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String line2 = "";
            while ((line = rd.readLine()) != null) {
                line = line.replace("\n", "").replace("\r", "");
                line2 += line;
            }
            JSONObject json = new JSONObject(line2);
            otp otp = new otp();
            if (responseCode == 200) {
                String orderid = json.getJSONObject("order").getString("id");
                String newMsisdn = json.getString("newMsisdn");
                respuesta.put("success", true);
                respuesta.put("orderid", orderid);
                respuesta.put("newMsisdn", newMsisdn);
                respuesta.put("effectiveDate", json.getString("effectiveDate"));

                otp.agregarNirNumero(idusuario, nir, newMsisdn);
                otp.agregarSolicitudNir(idusuario);
            } else {
                String description;
                if (json.has("description")) {
                    description = json.getString("description");
                } else {
                    description = "Error " + responseCode + " devuelto por Altan.";
                }

                respuesta.put("success", false);
                respuesta.put("detalle", description);
                otp.agregarErrorNir(responseCode, description, numero);

            }

            return respuesta;

        } catch (Exception ex) {
            respuesta.put("success", false);
            return respuesta;
        }
    }

    public void bajaRecurrencia(String numero) {
        try {
            String url = "https://mibaitqa.ordenaris.com/core/servicio/recurrencias/" + numero + "/baja";
            JSONObject data = new JSONObject();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet requestget = new HttpGet(url);
            HttpResponse responseget = httpClient.execute(requestget);
            int responseCode = responseget.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(responseget.getEntity().getContent()));
                String line = "";
                String line2 = "";
                while ((line = rd.readLine()) != null) {
                    line = line.replace("\n", "").replace("\r", "");
                    line2 += line;
                }
                data = new JSONObject(line2);
            } else {
                data.put("error", 500);
            }
        } catch (Exception ex) {
            Logger.getLogger(altan.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
