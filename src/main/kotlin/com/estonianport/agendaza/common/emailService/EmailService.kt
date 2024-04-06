package com.estonianport.agendaza.common.emailService

import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.TipoExtra
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    lateinit var sender: JavaMailSender

    fun isEmailValid(target: String): Boolean {
        return target.isNotEmpty() && EmailValidator.getInstance().isValid(target)
    }

    fun sendEmail(emailBody: Email) {
        if(isEmailValid(emailBody.email)){
            sendEmailTool(emailBody.content, emailBody.email, emailBody.subject)
        }else{
            throw BusinessException("Email Invalido")
        }
    }

    private fun sendEmailTool(textMessage: String, email: String, subject: String) {
        val message: MimeMessage = sender.createMimeMessage()
        val helper = MimeMessageHelper(message)
        try {
            helper.setTo(email)
            helper.setText(textMessage, true)
            helper.setSubject(subject)
            sender.send(message)
        } catch (e: MessagingException) {
            throw BusinessException("No se pudo enviar el mail")
        }
    }

    fun enviarMailComprabanteReserva(evento: Evento, action: String, empresa : Empresa) {

        // -------------------------- Extra --------------------------
        val listaExtra: List<Extra> = evento.listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }
        val extraMail = StringBuilder()
        if (listaExtra.isNotEmpty()) {
            var i = 0
            extraMail.append("Con los siguientes extras: ")
            for (extra in listaExtra) {
                extraMail.append(extra.nombre)
                i++
                if (i < listaExtra.size) {
                    extraMail.append(", ")
                } else {
                    extraMail.append(".")
                }
            }
        } else {
            extraMail.append("Sin ningun extra.")
        }

        // -------------------------- Extra variable --------------------------
        val listaEventoEventoExtraVariable: List<EventoExtraVariable> = evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
        val extraVariableMail = StringBuilder()
        if (listaEventoEventoExtraVariable.isNotEmpty()) {
            val listaExtraVariable: MutableSet<Extra> = mutableSetOf()
            for (eventoExtraVariableSubTipoEvento in listaEventoEventoExtraVariable) {
                listaExtraVariable.add(eventoExtraVariableSubTipoEvento.extra)
            }
            if (listaExtraVariable.isNotEmpty()) {
                var i = 0
                extraVariableMail.append("Con los siguientes extras variables: ")
                for (extraVariableSubTipoEvento in listaExtraVariable) {
                    extraVariableMail.append(extraVariableSubTipoEvento.nombre)
                    i++
                    if (i < listaExtraVariable.size) {
                        extraVariableMail.append(", ")
                    } else {
                        extraVariableMail.append(".")
                    }
                }
            }
        } else {
            extraVariableMail.append("Sin ningun extra variable.")
        }

        // -------------------------- Servicio --------------------------
        val listaServicios: Set<Servicio> = evento.tipoEvento.listaServicio
        val servicioMail = StringBuilder()
        if (listaServicios.isNotEmpty()) {
            var i = 0
            servicioMail.append("El evento incluye los siguientes servicios: ")
            for (servicio in listaServicios) {
                servicioMail.append(servicio.nombre)
                i++
                if (i < listaServicios.size) {
                    servicioMail.append(", ")
                } else {
                    servicioMail.append(".")
                }
            }
        } else {
            servicioMail.append("El evento no incluye ningun otro servicio.")
        }
        val catering = StringBuilder()

        // -------------------------- Catering --------------------------
        if (evento.getPresupuestoCatering() != 0.0) {

            // ------------------- Tipo Catering -----------------------
            val listaTipoCatering: List<Extra> = evento.listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }
            val tipoCateringMail = StringBuilder()
            if (listaTipoCatering.isNotEmpty()) {
                var i = 0
                tipoCateringMail.append("Tipo de catering: ")
                for (tipoCatering in listaTipoCatering) {
                    tipoCateringMail.append(tipoCatering.nombre)
                    i++
                    if (i < listaTipoCatering.size) {
                        tipoCateringMail.append(", ")
                    } else {
                        tipoCateringMail.append(".")
                    }
                }
            } else {
                tipoCateringMail.append("El evento no incluye ningun tipo catering.")
            }

            // ------------------- Extra Catering -----------------------
            val listaCateringExtraCatering: List<EventoExtraVariable> = evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }
            val extraVariableCateringMail = StringBuilder()
            if (listaCateringExtraCatering.isNotEmpty()) {
                val listaExtraCatering: MutableSet<Extra> = mutableSetOf()
                for (cateringExtraVariableCatering in listaCateringExtraCatering) {
                    listaExtraCatering.add(cateringExtraVariableCatering.extra)
                }
                if (listaExtraCatering.isNotEmpty()) {
                    var i = 0
                    extraVariableCateringMail.append("Extras catering: ")
                    for (extraVariableCatering in listaExtraCatering) {
                        extraVariableCateringMail.append(extraVariableCatering.nombre)
                        i++
                        if (i < listaExtraCatering.size) {
                            extraVariableCateringMail.append(", ")
                        } else {
                            extraVariableCateringMail.append(".")
                        }
                    }
                }
            } else {
                extraVariableCateringMail.append("El evento no incluye extra catering.")
            }
            catering.append("El catering contratado es el siguiente: ")
            catering.append("<br>")
            if (tipoCateringMail.isNotEmpty()) {
                catering.append(tipoCateringMail)
                catering.append("<br>")
            }
            if (extraVariableCateringMail.isNotEmpty()) {
                catering.append(extraVariableCateringMail)
                catering.append("<br>")
            }
        } else {
            catering.append("El evento no incluye catering")
        }

        // ------------------- Presupuesto -----------------------
        var presupuestoTotal = 0.0
        if (evento.getPresupuesto() != 0.0) {
            presupuestoTotal += evento.getPresupuesto()
        }
        if (evento.getPresupuestoCatering() != 0.0) {
            presupuestoTotal += evento.getPresupuestoCatering()
        }

        // ------------------- Dia y hora ---------------------------
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()

        // ----------------- Armado de mail -------------------------
        val emailBody = Email(
            evento.cliente.email,
            "Tu evento: " + evento.nombre + " para el " + dia + ", codigo: " + evento.codigo
        )

        // ----------------- Imagenes del mail -------------------------
        val imagenLogo = "https://i.ibb.co/djW4JkC/logo.png"
        val imagenComprobante = "https://i.ibb.co/9TLSqHy/comprobante.jpg"
        val imagenSalon = "https://i.ibb.co/wLKMCP4/salon.jpg"
        val imagenExtra = "https://i.ibb.co/92R9Yj3/extras.jpg"
        val imagenCatering = "https://i.ibb.co/pJdbbmB/catering.jpg"
        val imagenServicio = "https://i.ibb.co/dmtxW9z/servicios.jpg"
        val imagenIg = "https://i.ibb.co/8Xb3WwH/ig.png"
        val imagenWpp = "https://i.ibb.co/pnZg9Nr/wpp.png"
        val imagenFb = "https://i.ibb.co/dB90K2y/fb.png"
        val imagenMail = "https://i.ibb.co/bWgGQv6/mail.png"

        // ----------------- Style del mail -------------------------
        val contentEmail = StringBuilder()
        contentEmail.append(createHeadWithStyle())
        contentEmail.append(createBody())
        // TODO ajustar foto de empresa para que aparezca al header
        //contentEmail.append(createHeader(imagenLogo))
        contentEmail.append(createTitle(evento.nombre, action))
        contentEmail.append(
            createComprobante(
                evento.codigo,
                evento.tipoEvento.nombre,
                evento.capacidad.capacidadAdultos,
                evento.capacidad.capacidadNinos,
                presupuestoTotal,
                dia,
                horaInicio,
                horaFin,
                imagenComprobante
            )
        )

        // TODO rehacer mas entendible, que sea size 3 significa que es un Salon y tiene direccion
        if (empresa.getContacto().size == 3){
            contentEmail.append(
                createSalonConDireccion(
                    empresa.nombre,
                    empresa.getContacto(),
                    imagenSalon
                )
            )
        }else{
            contentEmail.append(
                createSalonSinDireccion(
                    empresa.nombre,
                    empresa.getContacto(),
                    imagenSalon
                )
            )
        }
        contentEmail.append(createExtras(extraMail, extraVariableMail, imagenExtra))
        contentEmail.append(createCatering(catering, imagenCatering))
        contentEmail.append(createServicios(servicioMail, imagenServicio))
        //TODO Ajustar contacto de salon para que aparezcan redes sociales y logo
        //contentEmail.append(createContact(imagenLogo, imagenIg, imagenWpp, imagenFb, imagenMail))
        contentEmail.append(createFooter())
        contentEmail.append("</body>	</html>")
        emailBody.content = contentEmail.toString()

        sendEmail(emailBody)
    }

    private fun createHeadWithStyle(): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<!DOCTYPE html>"
                    + "<html lang='es'> "
                    + "<head>"
                    + "	<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />"
                    + "	<meta name='viewport' content='width=device-width; initial-scale=1.0; maximum-scale=1.0;' />"
                    + " <!--[if !mso]--><!-- -->"
                    + " <link href='https://fonts.googleapis.com/css?family=Work+Sans:300,400,500,600,700' rel='stylesheet'>"
                    + " <link href='https://fonts.googleapis.com/css?family=Quicksand:300,400,700' rel='stylesheet'>"
                    + " <!-- <![endif]-->"
                    + " <title>Geservapp mail</title>"
        )
        contentMain.append(
            "<style type='text/css'>"
                    + "body {"
                    + "	width: 100%;"
                    + "	background-color: #ffffff;"
                    + "	margin: 0;"
                    + "	padding: 0;"
                    + "	-webkit-font-smoothing: antialiased;"
                    + "	mso-margin-top-alt: 0px;"
                    + "	mso-margin-bottom-alt: 0px;"
                    + "	mso-padding-alt: 0px 0px 0px 0px;"
                    + "}"
                    + "p, h1, h2, h3, h4 {"
                    + "	margin-top: 0;"
                    + "	margin-bottom: 0;"
                    + "	padding-top: 0;"
                    + "	padding-bottom: 0;"
                    + "}"
                    + "span.preheader {"
                    + "	display: none;"
                    + "	font-size: 1px;"
                    + "}"
                    + "html {"
                    + "	width: 100%;"
                    + "}"
                    + "table {"
                    + "	font-size: 14px;"
                    + "	border: 0;"
                    + "}"
                    + "@media only screen and (max-width: 640px) {" /* ----------- responsivity ----------- */
                    + " .main-header {" /*------ top header ------ */
                    + "	 font-size: 20px !important;"
                    + " }"
                    + " .main-section-header {"
                    + "	 font-size: 28px !important;"
                    + " }"
                    + " .show {"
                    + "	 display: block !important;"
                    + " }"
                    + " .hide {"
                    + "	 display: none !important;"
                    + " }"
                    + " .align-center {"
                    + "	 text-align: center !important;"
                    + " }"
                    + " .no-bg {"
                    + "	 background: none !important;"
                    + " }"
                    + " .main-image img {" /*----- main image -------*/
                    + "	 width: 440px !important;"
                    + "	 height: auto !important;"
                    + " }"
                    + " .divider img {" /* ====== divider ====== */
                    + "	 width: 440px !important;"
                    + " }"
                    + " .container590 {" /*-------- container --------*/
                    + "	 width: 440px !important;"
                    + " }"
                    + " .container580 {"
                    + "	 width: 400px !important;"
                    + " }"
                    + " .main-button {"
                    + "	 width: 220px !important;"
                    + " }"
                    + " .section-img img {" /*-------- secions ----------*/
                    + "	 width: 320px !important;"
                    + "	 height: auto !important;"
                    + " }"
                    + " .team-img img {"
                    + "	 width: 100% !important;"
                    + "	 height: auto !important;"
                    + " }"
                    + "}"
                    + "@media only screen and (max-width: 479px) {"
                    + " .main-header {" /*------ top header ------ */
                    + "  font-size: 18px !important;"
                    + " }"
                    + " .main-section-header {"
                    + "  font-size: 26px !important;"
                    + " }"
                    + " .divider img {" /* ====== divider ====== */
                    + "  width: 280px !important;"
                    + " }"
                    + " .container590 {" /*-------- container --------*/
                    + "  width: 280px !important;"
                    + " }"
                    + " .container580 {"
                    + "  width: 260px !important;"
                    + " }"
                    + " .section-img img {" /*-------- secions ----------*/
                    + "  width: 280px !important;"
                    + "  height: auto !important;"
                    + " }"
                    + "}"
                    + "</style>"
                    + "<!-- [if gte mso 9]><style type=”text/css”>"
                    + " body {"
                    + "  font-family: arial, sans-serif!important;"
                    + " }"
                    + "<![endif]-->"
                    + "</head>"
        )
        return contentMain
    }

    private fun createHeader(imagenLogo: String): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center' height='70' style='height:70px;'>"
                    + "<a href='' style='display: block; border-style: none !important; border: 0 !important;'><img width='100' border='0' style='display: block; width: 100px;' src='" + imagenLogo + "' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createBody(): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append("<body class='respond' leftmargin='0' topmargin='0' marginwidth='0' marginheight='0'>")
        return contentMain
    }

    private fun createTitle(eventoNombre: String, action: String): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff' class='bg_color'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center' style='color: #343434; font-size: 24px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "TU <span style='color: #5caad2;'>COMPROBANTE</span>"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='10' style='font-size: 10px; line-height: 10px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' width='40' align='center' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' width='400' align='center' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center' style='color: #888888; font-size: 16px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;'>"
                    + "<div style='line-height: 24px'>"
                    + "Tu evento: " + eventoNombre + ", ha " + action + " exitosamente."
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr class='hide'>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createComprobante(
        codigo: String,
        subTipoEventoNombre: String,
        capacidadAdultos: Int,
        capacidadNinos: Int,
        presupuestoTotal: Double,
        dia: String,
        horaInicio: String,
        horaFin: String,
        imagenComprobante: String
    ): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenComprobante + "' style='display: block; width: 280px;' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;'"
                    + "class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;'"
                    + "class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;'class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "RESERVA"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + "Codigo de reserva: " + codigo
                    + " </div>"
                    + "<div style='line-height: 24px'>"
                    + "Fecha de evento: " + dia
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "de " + horaInicio + " a " + horaFin
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Contrataste un " + subTipoEventoNombre
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Para " + capacidadAdultos + " adultos y " + capacidadNinos + " niños"
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Precio final: " + presupuestoTotal
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createSalonConDireccion(
        salonNombre: String,
        contacto : ArrayList<String>,
        imagenSalon: String
    ): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenSalon + "' style='display: block; width: 280px;' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "SALON"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + "Nombre de salon: " + salonNombre
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Direccion: " + contacto[0] + " " + contacto[1]
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Localidad: " + contacto[2]
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createSalonSinDireccion(
        salonNombre: String,
        contacto : ArrayList<String>,
        imagenSalon: String
    ): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenSalon + "' style='display: block; width: 280px;' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "SALON"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + "Nombre de salon: " + salonNombre
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Telefono: " + contacto[0]
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + "Mail: " + contacto[1]
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createExtras(
        extraMail: StringBuilder,
        extraVariableMail: StringBuilder,
        imagenExtra: String
    ): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenExtra + "' style='display: block; width: 280px;' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "EXTRAS"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + extraMail
                    + "</div>"
                    + "<div style='line-height: 24px'>"
                    + extraVariableMail
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createCatering(cateringMail: StringBuilder, imagenCatering: String): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenCatering + "' style='display: block; width: 280px;' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "CATERING"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + cateringMail
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createServicios(servicioMail: StringBuilder, imagenServicio: String): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<a href='' style=' border-style: none !important; border: 0 !important;'><img src='" + imagenServicio + "' width='280' border='0' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='5' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='5' height='20' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='260' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #3d3d3d; font-size: 22px; font-family: Quicksand, Calibri, sans-serif; font-weight:700;letter-spacing: 3px; line-height: 35px;' class='align-center main-header'>"
                    + "<div style='line-height: 35px'>"
                    + "SERVICIOS"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left'>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' width='40' border='0' cellpadding='0' cellspacing='0' bgcolor='eeeeee'>"
                    + "<tr>"
                    + "<td height='2' style='font-size: 2px; line-height: 2px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 12px; line-height: 12px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;' class='align-center'>"
                    + "<div style='line-height: 24px'>"
                    + servicioMail
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createContact(
        imagenLogo: String,
        imagenIg: String,
        imagenWpp: String,
        imagenFb: String,
        imagenMail: String
    ): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='ffffff' class='bg_color'>"
                    + "<tr class='hide'>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='40' style='font-size: 40px; line-height: 40px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='60' style='border-top: 1px solid #e0e0e0;font-size: 60px; line-height: 60px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590 bg_color'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' width='300' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left'>" /* logo */
                    + "<a href='' style='display: block; border-style: none !important; border: 0 !important;'><img width='80' border='0' style='display: block; width: 80px;' src='" + imagenLogo + "' alt='' /></a>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>"
                    + "&nbsp;"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + " <td align='left' style='color: #888888; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 23px;' class='text_color'>"
                    + "<div style='color: #333333; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; font-weight: 300; mso-line-height-rule: exactly; line-height: 23px;'>"
                    + "Hablanos a nuestras redes sociales:"
                    + "</div>"
                    + " </td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='2' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td width='2' height='10' style='font-size: 10px; line-height: 10px;'></td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' width='200' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td class='hide' height='45' style='font-size: 45px; line-height: 45px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='15' style='font-size: 15px; line-height: 15px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='right' cellpadding='0' cellspacing='0'>"
                    + "<tr>"
                    + "<td>"
                    + "<a href='https://www.instagram.com/mix_eventos_/?hl=es' style='display: block; border-style: none !important; border: 0 !important;'><img width='24' border='0' style='display: block;' src='" + imagenIg + "' alt=''></a>"
                    + "</td>"
                    + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                    + "<td>"
                    + "<a href='https://www.facebook.com/mixeventoscaseros/' style='display: block; border-style: none !important; border: 0 !important;'><img width='24' border='0' style='display: block;' src='" + imagenFb + "' alt=''></a>"
                    + "</td>"
                    + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                    + "<td>"
                    + "<a href='http://bit.ly/MixEventosCaseros' style='display: block; border-style: none !important; border: 0 !important;'><img width='24' border='0' style='display: block;' src='" + imagenWpp + "'alt=''></a>"
                    + "</td>"
                    + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                    + "<td>"
                    + "<td>"
                    + "<a href='mailto:miixeventos1@gmail.com' style='display: block; border-style: none !important; border: 0 !important;'><img width='24' border='0' style='display: block;' src='" + imagenMail + "' alt=''></a>"
                    + "</td>"
                    + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='60' style='font-size: 60px; line-height: 60px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    private fun createFooter(): StringBuilder {
        val contentMain = StringBuilder()
        contentMain.append(
            "<table border='0' width='100%' cellpadding='0' cellspacing='0' bgcolor='f4f4f4'>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table border='0' align='center' width='590' cellpadding='0' cellspacing='0' class='container590'>"
                    + "<tr>"
                    + "<td>"
                    + "<table border='0' align='left' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='left' style='color: #aaaaaa; font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;'>"
                    + "<div style='line-height: 24px;'>"
                    + "<span style='color: #333333;'>"
                    + "Geservapp diseñado por"
                    + "</span>"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' align='left' width='5' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td height='20' width='5' style='font-size: 20px; line-height: 20px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
                    + "<table border='0' align='right' cellpadding='0' cellspacing='0' style='border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;' class='container590'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<table align='center' border='0' cellpadding='0' cellspacing='0'>"
                    + "<tr>"
                    + "<td align='center'>"
                    + "<span style='font-size: 14px; font-family: 'Work Sans', Calibri, sans-serif; line-height: 24px;color: #5caad2; text-decoration: none;font-weight:bold;'>"
                    + "Estonian Port"
                    + "</span>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td height='25' style='font-size: 25px; line-height: 25px;'>&nbsp;</td>"
                    + "</tr>"
                    + "</table>"
        )
        return contentMain
    }

    fun enviarMailComprabantePago(pago: Pago, listaPagos: List<Pago>) {
        val evento: Evento = pago.evento
        val diaPago: String = pago.fecha.toLocalDate().toString()
        val horaPago: String = pago.fecha.toLocalTime().toString()
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()
        var totalPago = 0
        for (pagos in listaPagos) {
            totalPago += pagos.monto
        }
        val emailBody =
            Email(evento.cliente.email, "Tu pago del evento  " + evento.nombre + ", codigo: " + evento.codigo)

        emailBody.content =
            "Tu pago para el evento: " + evento.nombre + " ha sido realizado exitosamente." + "<br>" +
                    "Fecha de pago: " + diaPago + " hora: " + horaPago + "<br>" +
                    "Monto abonado: $" + pago.monto + "<br>" +
                    "Monto total abonado hasta la fecha: $" + totalPago + "<br>" +
                    "Monto faltante: $" + Math.abs(evento.getPresupuesto() - totalPago) + "<br>" +
                    "El precio total del evento: $" + evento.getPresupuesto() + "<br>" +
                    "Acercate cuando quieras al salon: " + evento.empresa.nombre +
                    " en calle " + ""/*evento.getSalon().getCalle()*/ + " " + ""/*evento.getSalon()*/
        /*.getNumero() */ "" + ", " + "" /*evento.getSalon().getMunicipio()*/ + "." + "<br>" +
                "Te recordamos que tu evento se realizara el dia " + dia + " de " + horaInicio + " a " + horaFin + "." + "<br>"
        // TODO salon calle y eso
        sendEmail(emailBody)
    }

    fun enviarMailEventoEliminado(evento: Evento) {
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()
        val emailBody = Email(
            evento.cliente.email,
            "El evento  " + evento.nombre + ", codigo: " + evento.codigo + " fue cancelado con exito"
        )
        emailBody.content =
            "El evento: " + evento.nombre + " ha sido cancelado exitosamente." + "<br>" +
                    "tu evento se iba a realizar el dia " + dia + " de " + horaInicio + " a " + horaFin + "." + "<br>" +
                    "Ante cualquier consulta" + "<br>" +
                    "Acercate al salon: " + evento.empresa.nombre + " en calle " + ""/*evento.getSalon()
                .getCalle() */ + " " + ""/*evento.getSalon().getNumero()*/ + ", " + /*evento.getSalon()*/
                    /*.getMunicipio()*/ "" + "." + "<br>"

        sendEmail(emailBody)
    }
}