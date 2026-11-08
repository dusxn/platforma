package org.raflab.platforma_server.controllers.request;

import java.time.LocalDateTime;

public record CreateExamTermRequest (LocalDateTime startTime, LocalDateTime endTime, Long examPeriodId){
}
