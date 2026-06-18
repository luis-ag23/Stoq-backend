package com.Proyecto.stoq.application.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.Proyecto.stoq.application.services.AlertaService;

@Component
public class AlertaInventarioScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AlertaInventarioScheduler.class);

    private final AlertaService alertaService;

    public AlertaInventarioScheduler(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void evaluarRiesgosPeriodicamente() {
        logger.info("[STOQ-SCHED] Iniciando evaluación programada de riesgos de inventario");
        alertaService.evaluarRiesgosInventarioProgramado();
    }
}
