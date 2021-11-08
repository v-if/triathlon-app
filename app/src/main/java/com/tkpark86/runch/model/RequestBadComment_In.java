package com.tkpark86.runch.model;

public class RequestBadComment_In {
	public String proc_tp; 			// 1.입력, 2.수정, 3.삭제
	public String bad_comment_id;	// BAD_COMMENT_ID
	public String member_id;		// 회원ID
	public String comment_tp;		// 1.코멘트, 2.댓글
	public String comment_id;		// COMMENT_ID or COMMENT_REPLY_ID
	public String content;			// 내용
}
