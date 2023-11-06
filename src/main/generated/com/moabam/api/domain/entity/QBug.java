package com.moabam.api.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBug is a Querydsl query type for Bug
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QBug extends BeanPath<Bug> {

    private static final long serialVersionUID = -556789843L;

    public static final QBug bug = new QBug("bug");

    public final NumberPath<Integer> goldenBug = createNumber("goldenBug", Integer.class);

    public final NumberPath<Integer> morningBug = createNumber("morningBug", Integer.class);

    public final NumberPath<Integer> nightBug = createNumber("nightBug", Integer.class);

    public QBug(String variable) {
        super(Bug.class, forVariable(variable));
    }

    public QBug(Path<? extends Bug> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBug(PathMetadata metadata) {
        super(Bug.class, metadata);
    }

}

