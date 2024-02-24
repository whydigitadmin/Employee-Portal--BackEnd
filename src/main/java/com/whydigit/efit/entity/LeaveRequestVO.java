package com.whydigit.efit.entity;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.whydigit.efit.dto.CreatedUpdatedDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leaverequest")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LeaveRequestVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@JsonFormat(pattern="yyyy-MM-dd")
	private LocalDate fromdate;
	@JsonFormat(pattern="yyyy-MM-dd")
	private LocalDate todate;
	private String companycode;
	private String branchId;
	private String totaldays;
	private String leavetype;
	private String notes;
	private String empcode;
	private String empname;
	private String createdby;
	private String updatedby;
	private String approvedby;
	private String status;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date approvedat;
	private boolean cancel;
	private boolean active;
	private String remarks;
	private String notifyto;
	private float noOfDays;
	private String empmail;
	@Embedded
	private CreatedUpdatedDate commonDate = new CreatedUpdatedDate();

}
