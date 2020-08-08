package com.portal.service;

import java.util.List;

import com.portal.entity.MBMessage;

public interface MessageService {
	
	public List<MBMessage> byClassPK(Long classPK);
	
	public List<MBMessage> getReplyListByCommentId(Long messageId);
	
	public int likeCount(Long messageid);
	
	public int likebyuserid(Long messageid,Long webuserid);
}
