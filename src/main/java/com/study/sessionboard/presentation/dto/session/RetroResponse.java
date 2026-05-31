package com.study.sessionboard.presentation.dto.session;

import com.study.sessionboard.application.event.dto.AuthorDto;
import com.study.sessionboard.domain.session.Retro;
import java.time.OffsetDateTime;

public record RetroResponse(
        Long id,
        AuthorDto author,
        Integer rating,
        String body,
        OffsetDateTime createdAt
) {
    public static RetroResponse from(Retro retro) {
        return new RetroResponse(
                retro.getId(),
                AuthorDto.from(retro.getAuthor()),
                retro.getRating(),
                retro.getBody(),
                retro.getCreatedAt()
        );
    }
}