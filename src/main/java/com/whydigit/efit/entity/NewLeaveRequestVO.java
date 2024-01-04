package com.whydigit.efit.entity;

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
@Table(name = "new_leave_request")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class NewLeaveRequestVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date from;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date to;
	private String totaldays;
	private String leavetype;
	private String notes;
	private String search;
	private String createdby;
	private String updatedby;
	private boolean cancel;
	private boolean active;
	private String remarks;
	@Embedded
	private CreatedUpdatedDate commonDate = new CreatedUpdatedDate();
}
