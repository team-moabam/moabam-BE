package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QItem is a Querydsl query type for Item
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItem extends EntityPathBase<Item> {

    private static final long serialVersionUID = -80408326L;

    public static final QItem item = new QItem("item");

    public final com.moabam.global.common.entity.QBaseTimeEntity _super = new com.moabam.global.common.entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> bugPrice = createNumber("bugPrice", Integer.class);

    public final EnumPath<com.moabam.api.domain.entity.enums.ItemCategory> category = createEnum("category", com.moabam.api.domain.entity.enums.ItemCategory.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> goldenBugPrice = createNumber("goldenBugPrice", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final StringPath name = createString("name");

    public final EnumPath<com.moabam.api.domain.entity.enums.ItemType> type = createEnum("type", com.moabam.api.domain.entity.enums.ItemType.class);

    public final NumberPath<Integer> unlockLevel = createNumber("unlockLevel", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QItem(String variable) {
        super(Item.class, forVariable(variable));
    }

    public QItem(Path<? extends Item> path) {
        super(path.getType(), path.getMetadata());
    }

    public QItem(PathMetadata metadata) {
        super(Item.class, metadata);
    }

}

