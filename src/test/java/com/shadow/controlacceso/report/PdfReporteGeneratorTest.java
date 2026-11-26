package com.shadow.controlacceso.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.model.enums.TipoAlerta;

class PdfReporteGeneratorTest {

    @Test
    void generaUnPdfValido() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        DatosReporte datos = new DatosReporte("Prueba", ahora,
                List.of(new FilaRegistro(ahora, "Bodega", "Ana Torres", Resultado.PERMITIDO),
                        new FilaRegistro(ahora, "Bodega", "Desconocido", Resultado.DENEGADO_TARJETA_DESCONOCIDA)),
                List.of(new Alerta(1L, TipoAlerta.TARJETA_DESCONOCIDA, "detalle", ahora, 2L)));

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ReporteGenerator generador = new PdfReporteGenerator();
        generador.generar(datos, salida);

        String cabecera = new String(salida.toByteArray(), 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", cabecera);
        assertTrue(salida.size() > 1000);
        assertEquals(1, datos.totalPermitidos());
        assertEquals(1, datos.totalDenegados());
    }
}
