package com.study.sessionboard.presentation.dto.session;

import com.study.sessionboard.application.event.dto.AuthorDto;
import com.study.sessionboard.domain.session.Resource;
import java.time.OffsetDateTime;

public record ResourceResponse(
        Long id,
        String type,
        String name,
        AuthorDto uploader,
        String sizeLabel,
        String visibility,
        String url,
        OffsetDateTime uploadedAt
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getType(),
                resource.getName(),
                AuthorDto.from(resource.getUploader()),
                resource.getSizeLabel(),
                resource.getVisibility().name(),
                resource.getUrl(),
                resource.getUploadedAt()
        );
    }
}