package com.whydigit.efit.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whydigit.efit.entity.LeaveTypeVO;

@Repository
public interface LeaveTypeRepo extends JpaRepository<LeaveTypeVO, Integer> {

}
