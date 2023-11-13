package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInventory is a Querydsl query type for Inventory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventory extends EntityPathBase<Inventory> {

    private static final long serialVersionUID = -1028580395L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInventory inventory = new QInventory("inventory");

    public final com.moabam.global.common.entity.QBaseTimeEntity _super = new com.moabam.global.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final QItem item;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInventory(String variable) {
        this(Inventory.class, forVariable(variable), INITS);
    }

    public QInventory(Path<? extends Inventory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInventory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInventory(PathMetadata metadata, PathInits inits) {
        this(Inventory.class, metadata, inits);
    }

    public QInventory(Class<? extends Inventory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.item = inits.isInitialized("item") ? new QItem(forProperty("item")) : null;
    }

}

