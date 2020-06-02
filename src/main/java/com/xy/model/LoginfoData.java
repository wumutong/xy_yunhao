package com.xy.model;

public class LoginfoData {

	//企业名称
	private String pubName;
	//企业id
	private String pubId;
	//类型：登录、set、get
	private String type;
	//成功或者失败
	private String status;
	//时间
	private String dates;
	//token值
	private String taken;
	//数据中任务id
	private String taskID;
	//请求数据返回转态码
	private String state;

	public LoginfoData() {
	}


	public LoginfoData(String pubName, String pubId, String type, String status, String dates, String taken, String taskID, String state) {
		this.pubName = pubName;
		this.pubId = pubId;
		this.type = type;
		this.status = status;
		this.dates = dates;
		this.taken = taken;
		this.taskID = taskID;
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPubName() {
		return pubName;
	}

	public void setPubName(String pubName) {
		this.pubName = pubName;
	}

	public String getPubId() {
		return pubId;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDates() {
		return dates;
	}

	public void setDates(String dates) {
		this.dates = dates;
	}

	public String getTaken() {
		return taken;
	}

	public void setTaken(String taken) {
		this.taken = taken;
	}

	public String getTaskID() {
		return taskID;
	}

	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	@Override
	public String toString() {
		return "LoginfoData{" +
				"pubName='" + pubName + '\'' +
				", pubId='" + pubId + '\'' +
				", type='" + type + '\'' +
				", status='" + status + '\'' +
				", dates='" + dates + '\'' +
				", taken='" + taken + '\'' +
				", taskID='" + taskID + '\'' +
				", state='" + state + '\'' +
				'}';
	}
}
