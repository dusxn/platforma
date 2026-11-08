package org.raflab.platforma_server.controllers.request;

import java.util.List;

public record BulkExamRegistrationRequest (Long examTermId,
                                          Long groupId,
                                          List<Long> studentIds){
}
