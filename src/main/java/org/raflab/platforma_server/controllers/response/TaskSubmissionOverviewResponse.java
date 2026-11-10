package org.raflab.platforma_server.controllers.response;

import java.time.LocalDateTime;

public record TaskSubmissionOverviewResponse (Long studentId,
                                              String studentFirstName,
                                              String studentLastName,
                                              String indexNumber,
                                              Long taskId,
                                              String taskTitle,
                                              LocalDateTime downloadTime,
                                              LocalDateTime submissionTime,
                                              String solutionPath){

}
