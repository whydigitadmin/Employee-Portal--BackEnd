package com.whydigit.efit.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whydigit.efit.entity.GroupVO;

@Repository
public interface GroupRepo extends JpaRepository<GroupVO, Integer> {

}


