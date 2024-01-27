package com.whydigit.efit.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.whydigit.efit.dto.EmployeeDailyStatusDTO;
import com.whydigit.efit.entity.EmployeeDailyStatusVO;

public interface EmployeeDailyStatusRepo extends JpaRepository<EmployeeDailyStatusVO, Long> {

	@Query(value = "SELECT count(e) FROM EmployeeDailyStatusVO e WHERE e.empId = ?3 AND CAST(e.loginDate AS LocalDate) BETWEEN ?1 AND ?2")
	long getPresentDays(LocalDate startDate, LocalDate endDate,long empId);

	@Query(value = "SELECT e FROM EmployeeDailyStatusVO e WHERE e.empId = ?3 ORDER BY e.loginDate")
	EmployeeDailyStatusVO getLatestSataus(Long empId);


	void getTodaySatausByOrgId(Long orgId);

	@Query(value = "SELECT new com.whydigit.efit.dto.EmployeeDailyStatusDTO(e.empId,ed.empname,e.orgId,e.loginDate,e.logoutDate,e.isCheckIn) FROM EmployeeDetailsVO ed left join EmployeeDailyStatusVO e on ed.id=e.empId WHERE ed.orgId=?1 AND CAST(e.loginDate AS LocalDate) = ?2")
	List<EmployeeDailyStatusDTO> getStatusByOrgIdAndDate(Long orgId, LocalDate date);

	@Query(value = "SELECT new com.whydigit.efit.dto.EmployeeDailyStatusDTO(e.empId,ed.empname,e.orgId,e.loginDate,e.logoutDate,e.isCheckIn) FROM EmployeeDetailsVO ed left join EmployeeDailyStatusVO e on ed.id=e.empId WHERE ed.id=?1 AND CAST(e.loginDate AS LocalDate) = ?2")
	List<EmployeeDailyStatusDTO> getStatusByEmpIdAndDate(Long empId, LocalDate date);
	

}
