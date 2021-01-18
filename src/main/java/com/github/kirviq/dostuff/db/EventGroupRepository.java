package com.github.kirviq.dostuff.db;

import org.springframework.data.repository.CrudRepository;

import com.github.kirviq.dostuff.db.TypeGroup;

public interface EventGroupRepository extends CrudRepository<TypeGroup, String> {
}
