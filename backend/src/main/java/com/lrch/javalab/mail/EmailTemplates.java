package com.lrch.javalab.mail;

/** Plantillas de correo. Estilos en linea y tablas: los clientes de correo ignoran {@code <style>}. */
public final class EmailTemplates {

    private static final String INDIGO = "#4F46E5";
    private static final String INDIGO_CLARO = "#EEF2FF";
    private static final String FONDO = "#F4F4F5";
    private static final String TEXTO = "#18181B";
    private static final String GRIS = "#71717A";

    private EmailTemplates() {
    }

    public static EmailMessage bienvenida(String to, String nombre, String correo) {
        String cuerpo = parrafo("Hola <strong>" + escapar(nombre) + "</strong>, tu cuenta ya está lista. "
                + "Aquí tienes los datos para que no se te pierdan:")
                + caja("Tu correo de acceso", escapar(correo), "font-size:18px;font-weight:600;")
                + parrafo("Por seguridad <strong>nunca</strong> te enviamos tu contraseña, y nosotros tampoco la conocemos. "
                + "Si algún día la olvidas, entra a «¿Olvidaste tu contraseña?» y te mandamos un código de 6 dígitos "
                + "a este correo.");
        String texto = "Hola " + nombre + ", tu cuenta de Java Lab está lista.\n\n"
                + "Tu correo de acceso: " + correo + "\n\n"
                + "Por seguridad nunca enviamos tu contraseña. Si la olvidas, usa «¿Olvidaste tu contraseña?» "
                + "y te mandamos un código de 6 dígitos a este correo.";
        return new EmailMessage(to, "¡Bienvenido a Java Lab!", pagina("¡Bienvenido a Java Lab!", cuerpo), texto);
    }

    public static EmailMessage invitacion(String to, String nombre, String correo, String passwordTemporal) {
        String cuerpo = parrafo("Hola <strong>" + escapar(nombre) + "</strong>, te invitaron a practicar Java en "
                + "<strong>Java Lab</strong>. Entra con estos datos:")
                + caja("Tu correo", escapar(correo), "font-size:18px;font-weight:600;")
                + caja("Contraseña temporal", escapar(passwordTemporal), "font-family:'SFMono-Regular',Menlo,Consolas,"
                + "monospace;font-size:26px;font-weight:700;letter-spacing:2px;")
                + parrafo("Es una contraseña temporal: <strong>puedes cambiarla</strong> cuando quieras desde "
                + "<em>Perfil → Cambiar contraseña</em>. Si algún día la olvidas, usa «¿Olvidaste tu contraseña?» "
                + "y te enviamos un código de 6 dígitos a este correo.");
        String texto = "Hola " + nombre + ", te invitaron a Java Lab.\n\nTu correo: " + correo
                + "\nContraseña temporal: " + passwordTemporal
                + "\n\nPuedes cambiarla cuando quieras desde Perfil → Cambiar contraseña. "
                + "Si la olvidas, usa «¿Olvidaste tu contraseña?» y te enviamos un código a este correo.";
        return new EmailMessage(to, "Te invitaron a Java Lab", pagina("Te invitaron a Java Lab", cuerpo), texto);
    }

    public static EmailMessage codigoRecuperacion(String to, String nombre, String codigo) {
        return codigo(to, nombre, codigo, "Tu código para recuperar la contraseña",
                "Recibimos una solicitud para restablecer tu contraseña. Escribe este código en Java Lab:",
                "Si no fuiste tú, ignora este correo: tu contraseña no cambia.");
    }

    public static EmailMessage codigoCambioCorreo(String to, String nombre, String codigo) {
        return codigo(to, nombre, codigo, "Confirma tu nuevo correo",
                "Alguien (esperamos que tú) pidió usar este correo en su cuenta de Java Lab. "
                        + "Escribe este código para confirmarlo:",
                "Si no fuiste tú, ignora este correo: no se cambia nada.");
    }

    public static EmailMessage avisoCambioCorreo(String to, String nombre, String correoNuevo) {
        String cuerpo = parrafo("Hola <strong>" + escapar(nombre) + "</strong>, el correo de tu cuenta de Java Lab "
                + "cambió a:")
                + caja("Correo nuevo", escapar(correoNuevo), "font-size:18px;font-weight:600;")
                + parrafo("Si no fuiste tú, contacta al administrador cuanto antes.");
        String texto = "Hola " + nombre + ", el correo de tu cuenta de Java Lab cambió a " + correoNuevo
                + ".\nSi no fuiste tú, contacta al administrador cuanto antes.";
        return new EmailMessage(to, "El correo de tu cuenta cambió", pagina("Cambio de correo", cuerpo), texto);
    }

    private static EmailMessage codigo(String to, String nombre, String codigo, String asunto,
                                       String introduccion, String pie) {
        String separado = codigo.replaceAll("(\\d{3})(\\d{3})", "$1 $2");
        String cuerpo = parrafo("Hola <strong>" + escapar(nombre) + "</strong>,")
                + parrafo(introduccion)
                + caja("Tu código", separado, "font-family:'SFMono-Regular',Menlo,Consolas,monospace;"
                + "font-size:36px;font-weight:700;letter-spacing:6px;")
                + parrafo("Vence en <strong>10 minutos</strong> y solo funciona una vez.")
                + "<p style=\"margin:16px 0 0;font-size:13px;color:" + GRIS + ";\">" + pie + "</p>";
        String texto = "Hola " + nombre + ",\n\n" + introduccion + "\n\nTu código: " + separado
                + "\n\nVence en 10 minutos y solo funciona una vez.\n" + pie;
        return new EmailMessage(to, asunto + " · " + separado, pagina(asunto, cuerpo), texto);
    }

    private static String parrafo(String contenido) {
        return "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;color:" + TEXTO + ";\">" + contenido + "</p>";
    }

    private static String caja(String etiqueta, String valor, String estiloValor) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:8px 0 20px;\">"
                + "<tr><td align=\"center\" style=\"background:" + INDIGO_CLARO + ";border:1px solid #C7D2FE;"
                + "border-radius:12px;padding:20px;\">"
                + "<div style=\"font-size:12px;text-transform:uppercase;letter-spacing:1px;color:" + GRIS
                + ";margin-bottom:8px;\">" + etiqueta + "</div>"
                + "<div style=\"color:" + INDIGO + ";" + estiloValor + "\">" + valor + "</div>"
                + "</td></tr></table>";
    }

    private static String pagina(String titulo, String cuerpo) {
        return "<!doctype html><html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>"
                + "<body style=\"margin:0;padding:0;background:" + FONDO + ";"
                + "font-family:-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:"
                + FONDO + ";padding:32px 16px;\"><tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:520px;background:#FFFFFF;border-radius:16px;overflow:hidden;\">"
                + "<tr><td style=\"background:" + INDIGO + ";padding:24px 32px;\">"
                + "<div style=\"font-size:13px;letter-spacing:2px;text-transform:uppercase;color:#C7D2FE;\">Java Lab</div>"
                + "<div style=\"font-size:22px;font-weight:700;color:#FFFFFF;margin-top:4px;\">" + escapar(titulo) + "</div>"
                + "</td></tr>"
                + "<tr><td style=\"padding:28px 32px;\">" + cuerpo + "</td></tr>"
                + "<tr><td style=\"padding:16px 32px;background:#FAFAFA;font-size:12px;color:" + GRIS + ";\">"
                + "Práctica de Java para entrevistas técnicas · Este correo es automático, no respondas a él."
                + "</td></tr></table></td></tr></table></body></html>";
    }

    private static String escapar(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
