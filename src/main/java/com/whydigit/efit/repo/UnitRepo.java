package com.whydigit.efit.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whydigit.efit.entity.UnitVO;


@Repository
public interface UnitRepo extends JpaRepository<UnitVO, Integer> {

}


