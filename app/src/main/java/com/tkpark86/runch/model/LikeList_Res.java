package com.tkpark86.runch.model;

import java.util.ArrayList;

public class LikeList_Res {
	public String returnCode;		// 응답코드
	public String returnMessage;	// 응답메시지
	public int totalCount;			// 총 리스트 카운트
	public int resultCount;			// 검색결과 카운트
	public ArrayList<LikeList_Out> output;
}
