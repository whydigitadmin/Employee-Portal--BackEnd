package com.whydigit.efit.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.whydigit.efit.entity.LeaveRequestVO;

public interface LeaveRequestRepo extends JpaRepository<LeaveRequestVO, Integer> {

	List<LeaveRequestVO> findByEmpcode(String empcode);

	@Query(value="SELECT COALESCE(SUM(l.noOfDays), 0) FROM LeaveRequestVO l WHERE l.empcode=?2 AND l.fromdate=?1 AND l.status in('APPROVED','APPLIED')")
	float findNoOfLeave(LocalDate fromdate,String empCode);

	@Query("select a from LeaveRequestVO a, EmployeeDetailsVO b where a.empcode=b.empcode and b.reporting_person=(select c.id from EmployeeDetailsVO c where c.orgId=?1 and c.empcode=?2)")
	List<LeaveRequestVO> findAllRequestBasedOnApproval(Long orgId, String empcode);

}
