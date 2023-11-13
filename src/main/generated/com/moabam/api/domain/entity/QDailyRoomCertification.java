package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDailyRoomCertification is a Querydsl query type for DailyRoomCertification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyRoomCertification extends EntityPathBase<DailyRoomCertification> {

    private static final long serialVersionUID = -272809811L;

    public static final QDailyRoomCertification dailyRoomCertification = new QDailyRoomCertification("dailyRoomCertification");

    public final DatePath<java.time.LocalDate> certifiedAt = createDate("certifiedAt", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    public QDailyRoomCertification(String variable) {
        super(DailyRoomCertification.class, forVariable(variable));
    }

    public QDailyRoomCertification(Path<? extends DailyRoomCertification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyRoomCertification(PathMetadata metadata) {
        super(DailyRoomCertification.class, metadata);
    }

}

