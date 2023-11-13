package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDailyMemberCertification is a Querydsl query type for DailyMemberCertification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyMemberCertification extends EntityPathBase<DailyMemberCertification> {

    private static final long serialVersionUID = 1949579726L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDailyMemberCertification dailyMemberCertification = new QDailyMemberCertification("dailyMemberCertification");

    public final com.moabam.global.common.entity.QBaseTimeEntity _super = new com.moabam.global.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final QParticipant participant;

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDailyMemberCertification(String variable) {
        this(DailyMemberCertification.class, forVariable(variable), INITS);
    }

    public QDailyMemberCertification(Path<? extends DailyMemberCertification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDailyMemberCertification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDailyMemberCertification(PathMetadata metadata, PathInits inits) {
        this(DailyMemberCertification.class, metadata, inits);
    }

    public QDailyMemberCertification(Class<? extends DailyMemberCertification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.participant = inits.isInitialized("participant") ? new QParticipant(forProperty("participant"), inits.get("participant")) : null;
    }

}

