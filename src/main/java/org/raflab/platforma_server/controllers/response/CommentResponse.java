package org.raflab.platforma_server.controllers.response;

import org.raflab.platforma_server.model.Comment;

public record CommentResponse(
        Long commentId,
        String filePath,
        Integer lineNumber,
        String text
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getFilePath(),
                comment.getLineNumber(),
                comment.getText()
        );
    }
}
