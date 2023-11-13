package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBugHistory is a Querydsl query type for BugHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBugHistory extends EntityPathBase<BugHistory> {

    private static final long serialVersionUID = -696983513L;

    public static final QBugHistory bugHistory = new QBugHistory("bugHistory");

    public final com.moabam.global.common.entity.QBaseTimeEntity _super = new com.moabam.global.common.entity.QBaseTimeEntity(this);

    public final EnumPath<com.moabam.api.domain.entity.enums.BugActionType> actionType = createEnum("actionType", com.moabam.api.domain.entity.enums.BugActionType.class);

    public final EnumPath<com.moabam.api.domain.entity.enums.BugType> bugType = createEnum("bugType", com.moabam.api.domain.entity.enums.BugType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBugHistory(String variable) {
        super(BugHistory.class, forVariable(variable));
    }

    public QBugHistory(Path<? extends BugHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBugHistory(PathMetadata metadata) {
        super(BugHistory.class, metadata);
    }

}

