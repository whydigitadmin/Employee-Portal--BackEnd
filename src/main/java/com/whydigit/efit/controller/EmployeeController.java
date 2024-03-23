package com.whydigit.efit.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.whydigit.efit.common.CommonConstant;
import com.whydigit.efit.common.EmployeePortalConstants;
import com.whydigit.efit.dto.EmployeeAttendanceActivityDTO;
import com.whydigit.efit.dto.EmployeeDailyStatusDTO;
import com.whydigit.efit.dto.EmployeeInOutActionDTO;
import com.whydigit.efit.dto.ResponseDTO;
import com.whydigit.efit.entity.EmployeeDailyStatusVO;
import com.whydigit.efit.service.EmployeeService;

import net.bytebuddy.asm.Advice.Local;

@CrossOrigin
@RestController
@RequestMapping("/api/employee")
public class EmployeeController extends BaseController {

	public static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

	@Autowired
	EmployeeService employeeService;

	@GetMapping("/getEmployeeAttendanceActivity")
	public ResponseEntity<ResponseDTO> getEmployeeAttendanceActivity(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) long orgId, @RequestParam(required = false) String empId) {
		String methodName = "getEmployeeAttendanceActivity()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		String errorMsg = null;
		Map<String, Object> responseObjectsMap = new HashMap<>();
		ResponseDTO responseDTO = null;
		List<EmployeeAttendanceActivityDTO> employeeAttendanceActivityDTO = new ArrayList<>();
		try {
			employeeAttendanceActivityDTO = employeeService.getEmployeeAttendanceActivity(startDate, endDate, orgId,
					empId);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(EmployeePortalConstants.ERROR_MSG_METHOD_NAME, methodName, errorMsg);
		}
		if (StringUtils.isBlank(errorMsg)) {
			responseObjectsMap.put(CommonConstant.STRING_MESSAGE, "Employee attendance activity get successfully");
			responseObjectsMap.put("employeeAttendanceActivity", employeeAttendanceActivityDTO);
			responseDTO = createServiceResponse(responseObjectsMap);
		} else {
			responseDTO = createServiceResponseError(responseObjectsMap, "Employee attendance activity receive failed",
					errorMsg);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return ResponseEntity.ok().body(responseDTO);
	}

	@PostMapping("/employeeInOutAction")
	public ResponseEntity<ResponseDTO> employeeInOutAction(@RequestBody EmployeeInOutActionDTO employeeInOutActionDTO) {
		String methodName = "employeeInOutAction()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		String errorMsg = null;
		Map<String, Object> responseObjectsMap = new HashMap<>();
		ResponseDTO responseDTO = null;
		String action = employeeInOutActionDTO.isCheckIn() ? EmployeePortalConstants.CHECK_IN
				: EmployeePortalConstants.CHECK_OUT;
		try {
			EmployeeDailyStatusVO employeeDailyStatusVO = employeeService.employeeInOutAction(employeeInOutActionDTO);
			responseObjectsMap.put(CommonConstant.STRING_MESSAGE,
					new StringBuilder("Employee ").append(action).append("successful.").toString());
			responseObjectsMap.put(EmployeePortalConstants.EMPLOYEE_DAILY_STATUS_VO, employeeDailyStatusVO);
			responseDTO = createServiceResponse(responseObjectsMap);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(EmployeePortalConstants.ERROR_MSG_METHOD_NAME, methodName, errorMsg);
			responseDTO = createServiceResponseError(responseObjectsMap,
					new StringBuilder("Employee ").append(action).append(" failed.").toString(), errorMsg);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return ResponseEntity.ok().body(responseDTO);
	}

	@GetMapping("/getEmployeeStatus")
	public ResponseEntity<ResponseDTO> getEmployeeStatus(@RequestParam(required = false) Long orgId,
			@RequestParam(required = false) Long empId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		String methodName = "getEmployeeStatus()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		String errorMsg = null;
		Map<String, Object> responseObjectsMap = new HashMap<>();
		ResponseDTO responseDTO = null;
		List<EmployeeDailyStatusDTO> employeeDailyStatusDTO = new ArrayList<>();
		try {
			employeeDailyStatusDTO = employeeService.getEmployeeStatus(orgId, empId,date);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(EmployeePortalConstants.ERROR_MSG_METHOD_NAME, methodName, errorMsg);
		}
		if (StringUtils.isBlank(errorMsg)) {
			responseObjectsMap.put(CommonConstant.STRING_MESSAGE, "Employee status get successfully");
			responseObjectsMap.put(EmployeePortalConstants.EMPLOYEE_DAILY_STATUS_VO, employeeDailyStatusDTO);
			responseDTO = createServiceResponse(responseObjectsMap);
		} else {
			responseDTO = createServiceResponseError(responseObjectsMap, "Employee status receive failed", errorMsg);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return ResponseEntity.ok().body(responseDTO);
	}
	
	@GetMapping("/monthlyReport")
	public ResponseEntity<ResponseDTO> getMonthlyReport(@RequestParam String fromdt,@RequestParam String todt,@RequestParam String branch) {
		String methodName = "getMonthlyReport()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		String errorMsg = null;
		Map<String, Object> responseObjectsMap = new HashMap<>();
		ResponseDTO responseDTO = null;
		Set<Object[]> monthlyReport = new HashSet<>();
		try {
			monthlyReport = employeeService.getAllEmployeeMonthlyAttendanceDetails(fromdt, todt,branch);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(EmployeePortalConstants.ERROR_MSG_METHOD_NAME, methodName, errorMsg);
		}
		if (StringUtils.isBlank(errorMsg)) {
			List<Map<String, String>>MonthlyReports=getReport(monthlyReport);
			responseObjectsMap.put(CommonConstant.STRING_MESSAGE, "Monthly Report get successfully");
			responseObjectsMap.put("MonthlyReports", MonthlyReports);
			responseDTO = createServiceResponse(responseObjectsMap);
		} else {
			responseDTO = createServiceResponseError(responseObjectsMap, "Monthly Report receive failed", errorMsg);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return ResponseEntity.ok().body(responseDTO);
	}

	private List<Map<String, String>> getReport(Set<Object[]> monthlyReport) {
		List<Map<String, String>>MonthlyReports=new ArrayList<>();
		for(Object[]report:monthlyReport) {
			Map<String, String>formateReport=new HashMap<>();
			formateReport.put("EmployeeCode", report[0].toString());
			formateReport.put("EmployeeName", report[1].toString());
			formateReport.put("Sundays", report[2].toString());
			formateReport.put("Holidays", report[3].toString());
			formateReport.put("TotalDays", report[4].toString());
			formateReport.put("OfficeWorkingDays", report[5].toString());
			formateReport.put("TotalLeaveCount", report[6].toString());
			formateReport.put("CLLeave", report[7].toString());
			formateReport.put("PLLeave", report[8].toString());
			formateReport.put("PTLLeave", report[9].toString());
			formateReport.put("MLLeave", report[10].toString());
			formateReport.put("PresentDays", report[11].toString());
			formateReport.put("CLCreadit", report[12].toString());
			formateReport.put("EmpWorkingDays", report[13].toString());
			formateReport.put("SalaryDays", report[14].toString());
			MonthlyReports.add(formateReport);
		}
		return MonthlyReports;
	}
	
}
