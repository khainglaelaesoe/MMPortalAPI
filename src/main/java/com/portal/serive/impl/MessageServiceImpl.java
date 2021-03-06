package com.portal.serive.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.portal.dao.MessageDao;
import com.portal.dao.RatingsEntryDao;
import com.portal.entity.MBMessage;
import com.portal.service.MessageService;

@Service("messageService")
public class MessageServiceImpl implements MessageService {

	@Autowired
	private MessageDao messageDao;

	@Autowired
	private RatingsEntryDao ratingDao;

	private static Logger logger = Logger.getLogger(MessageServiceImpl.class);

//	public List<Message> byClassPK(Long classPK) {
//		String query = "from Message message where classpk=" + classPK;
//		return messageDao.byQuery(query);
//	}

	public List<MBMessage> byClassPK(Long classPK) {
		List<MBMessage> msgList = new ArrayList<MBMessage>();
		String query = "from MBMessage message where classpk=" + classPK + " And parentMessageId<>0 And parentMessageId=rootMessageId";
		msgList = messageDao.byQuery(query);
		for (MBMessage msg : msgList) {
			String querycount = "Select count(*) from RatingsEntry where classPk=" + msg.getMessageid() + " order by createdate";
			int count = messageDao.findCountByQueryString(querycount);
			msg.setLikecount(count);
		}
		return msgList;
	}

	public List<MBMessage> getReplyListByCommentId(Long messageId) {
		String query = "from MBMessage message where rootMessageId=" + messageId + "And parentMessageId<>rootMessageId And parentMessageId<>0";
		return messageDao.byQuery(query);
	}

	public int likeCount(Long messageid) {
		String querycount = "Select count(*) from RatingsEntry where classPk=" + messageid;
		return messageDao.findCountByQueryString(querycount);
	}

	public boolean likebyuserid(Long messageid, Long webuserid, long score) {		
		String querycount = "Select count(*) from RatingsEntry where classPk=" + messageid + " And userid=" + webuserid + " And score= " + score;		
		return messageDao.findCountByQueryString(querycount) > 0;
	}

	public List<MBMessage> byClassPKbymessageid(List<Long> messageidList) {
		List<MBMessage> msg = new ArrayList<MBMessage>();
		for (Long messageid : messageidList) {
			MBMessage mbmessage = new MBMessage();
			String query = "from MBMessage message where messageId=" + messageid;
			List<MBMessage> msgList = messageDao.byQuery(query);
			if (!CollectionUtils.isEmpty(msgList)) {
				mbmessage = messageDao.byQuery(query).get(0);
				String querycount = "Select count(*) from RatingsEntry where classPk=" + messageid;
				int count = messageDao.findCountByQueryString(querycount);
				mbmessage.setLikecount(count);
				msg.add(mbmessage);
			}
		}
		return msg;
	}

}
