package com.tkpark86.runch.model;

public class Notice_Out {
	public String notice_id;	// 알림ID
	public String app_state;	// APP상태(0.정상, 1.공지사항, 2.업데이트있음, 3.필수업데이트, 4.점검중)
	public String content;		// 내용
	public String version_code;	// 버전코드(1)
	public String version_name;	// 버전이름(0.8.0)
	public String notice_dt;	// 등록일
}
