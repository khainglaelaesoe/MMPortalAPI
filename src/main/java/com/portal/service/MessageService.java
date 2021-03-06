package com.portal.service;

import java.util.List;

import com.portal.entity.MBMessage;

public interface MessageService {
	
	public List<MBMessage> byClassPK(Long classPK);
	
	public List<MBMessage> getReplyListByCommentId(Long messageId);
	
	public int likeCount(Long messageid);
	
	public boolean likebyuserid(Long messageid,Long webuserid,long score);
	
	public List<MBMessage> byClassPKbymessageid(List<Long> messageidList);
}
