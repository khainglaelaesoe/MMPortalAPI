package com.portal.serive.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.portal.dao.JournalArticleDao;
import com.portal.entity.JournalArticle;
import com.portal.service.JournalArticleService;

@Service("journalArticleService")
public class JournalArticleServiceImpl implements JournalArticleService {

	@Autowired
	private JournalArticleDao journalDao;

	private static Logger logger = Logger.getLogger(JournalArticleServiceImpl.class);

	public JournalArticle getJournalArticleByFolderId(long folderId) {
		String queryStr = "select articleid, max(version) from JournalArticle journalArticle where folderid=" + folderId;
		List<Object> objectList = journalDao.byQueryString(queryStr);
		Object[] obj = (Object[]) objectList.get(0);
		if (obj[0] == null || obj[1] == null)
			return null;

		Long articleId = Long.parseLong(obj[0].toString());
		String version = obj[1].toString();
		return getJournalArticleByArticleIdAndVersion(articleId, version);
	}

	public List<JournalArticle> getJournalArticlesByFolderId(long folderId) {
		List<Object> objectList;
		List<JournalArticle> articleList = new ArrayList<JournalArticle>();
		String queryStr = "Select distinct articleid from JournalArticle where folderid=" + folderId;
		objectList = journalDao.byQueryString(queryStr);
		int i = 0;
		while (i < objectList.size()) {
			Long articleid = (Long) objectList.get(i);
			i++;
			articleList.add(getJournalArticleByArticleId(articleid));
		}
		return articleList;
	}

	@Override
	public JournalArticle getJournalArticleByArticleId(long articleId) {
		List<Long> objectList;
		String queryStr = "select id_ from JournalArticle where articleid=" + articleId + "" + "order by version desc";
		objectList = journalDao.findLongByQueryString(queryStr);
		Long obj = objectList.get(0);
		if (obj == null)
			return null;
		return getJournalArticle(obj);
	}

	@Override
	public JournalArticle getJournalArticle(long Id) {
		List<JournalArticle> journalArticleList;
		String queryStr = "select journalArticle from JournalArticle journalArticle where journalArticle.id_=" + Id;
		journalArticleList = journalDao.byQuery(queryStr);
		return journalArticleList.get(0);
	}

	public JournalArticle getJournalArticleBySearchTerms(long Id, String searchTerm) {
		String queryStr = "select journalArticle from JournalArticle journalArticle where journalArticle.id_=" + Id;
		List<JournalArticle> journalArticleList = journalDao.byQuery(queryStr);
		JournalArticle journalArticle = journalArticleList.get(0);
		StringBuilder searchTerms = new StringBuilder();
		searchTerms.append(journalArticle.getContent());
		searchTerms.append(journalArticle.getTitle());
		if (searchTerms.toString().contains(searchTerm))
			return journalArticle;
		return null;
	}

	public List<JournalArticle> getEmergenyContact() {
		List<JournalArticle> journallist = new ArrayList<JournalArticle>();
		long[] articleidlist = { 53269, 53276, 53283, 53262 };
		for (long articleid : articleidlist) {
			String queryStr = "select journalArticle from JournalArticle journalArticle where folderId=53239 and articleid=:dataInput order by version desc";
			journallist.add(journalDao.findDatabyQueryString(queryStr, articleid).get(0));
		}
		return journallist;
	}

	@Override
	public JournalArticle getJournalArticleforGov() {
		List<Long> objectList;
		String queryStr = "select id_ from JournalArticle where folderId=55681 order by version desc";
		objectList = journalDao.findLongByQueryString(queryStr);
		Long obj = objectList.get(0);
		if (obj == null)
			return null;
		return getJournalArticle(obj);
	}

	public List<Long> getAllArticleId() {
		String queryStr = "select id_ from JournalArticle journalArticle";
		return journalDao.findLongByQueryString(queryStr);
	}

	public Long getJournalArticleByClassUuIdandSearchTerm(String classuuid) {
		String queryStr = "select articleid, max(version) from JournalArticle journalArticle where journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_='" + classuuid + "')";
		List<Object> objectList = journalDao.byQueryString(queryStr);
		Object[] obj = (Object[]) objectList.get(0);
		if (obj[0] == null || obj[1] == null)
			return null;

		Long articleId = Long.parseLong(obj[0].toString());
		String version = obj[1].toString();
		return getJournalArticleByArticleIdAndVersionAndSearchTerm(articleId, version);
	}

	public String getClassUuidByArticleId(long articleId) {
		String queryStr = "select uuid_ from JournalArticleResource r where articleId=" + articleId;
		return journalDao.findByQuery(queryStr).get(0);
	}

	public Long getJournalArticleByArticleIdAndVersionAndSearchTerm(long articleId, String version) {
		String queryStr = "select id_ from JournalArticle journalArticle where journalArticle.articleid=" + articleId + " and version=" + version;
		List<Long> idList = journalDao.findLongByQueryString(queryStr);
		if (CollectionUtils.isEmpty(idList))
			return null;
		return journalDao.findLongByQueryString(queryStr).get(0);
	}

	@Override
	public JournalArticle getJournalArticleByArticleIdAndVersion(long articleId, String version) {

		String queryStr = "from JournalArticle journalArticle where journalArticle.articleid=" + articleId + " and version=" + version;
		return journalDao.getAll(queryStr).get(0);

		
	}

	public JournalArticle getJournalArticleIdByArticleIdAndVersion(long articleId, String version) {
		String queryStr = "from JournalArticle journalArticle where journalArticle.articleid=" + articleId + " and version=" + version;
		List<JournalArticle> journalArticles = journalDao.getAll(queryStr);
		if (CollectionUtils.isEmpty(journalArticles))
			return null;
		return journalArticles.get(0);
	}

	public List<JournalArticle> getIdByFolderIdByName(String input, String classuuid) {
		List<JournalArticle> journalArticleIdList = new ArrayList<JournalArticle>();
		String query = "select articleid, max(version) from JournalArticle journalArticle  where  (title LIKE " + "'%" + input + "%'" + " or content LIKE " + "'%" + input + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_='" + classuuid + "')";
		List<Object> objectList = journalDao.findByQueryString(query);
		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null || obj[1] == null)
				return new ArrayList<JournalArticle>();

			Long articleId = Long.parseLong(obj[0].toString());
			String version = obj[1].toString();
			journalArticleIdList.add(getJournalArticleIdByArticleIdAndVersion(articleId, version));
		}
		return journalArticleIdList;
	}

	public List<JournalArticle> getIdByFolderIdAndSearchTerms(int folderId, String searchTerm) {
		List<JournalArticle> journalArticleIdList = new ArrayList<JournalArticle>();
		String query = "SELECT articleid, max(version) FROM JournalArticle journal where (title LIKE " + "'%\"" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and folderid=" + folderId + " group by articleid";
		List<Object> objectList = journalDao.findByQueryString(query);
		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null || obj[1] == null)
				return null;

			Long articleId = Long.parseLong(obj[0].toString());
			String version = obj[1].toString();
			journalArticleIdList.add(getJournalArticleIdByArticleIdAndVersion(articleId, version));
		}
		return journalArticleIdList;
	}

	public JournalArticle getIdByFolderId(Long classpk) {
		String query = "from JournalArticle j where j.resourceprimkey=" + classpk + "order by version desc";
		return journalDao.getAll(query).get(0);
	}

	public JournalArticle getContactUsbyArticleAndVersion() {
		List<Long> objectList;
		String queryStr = "select id_ from JournalArticle where articleid=44959 order by version desc";
		objectList = journalDao.findLongByQueryString(queryStr);
		Long obj = objectList.get(0);
		if (obj == null)
			return null;
		return getJournalArticle(obj);
	}

	public JournalArticle getTermsbyVersion() {
		List<Long> objectList;
		String queryStr = "select id_ from JournalArticle where articleid=44911 order by version desc";
		objectList = journalDao.findLongByQueryString(queryStr);
		Long obj = objectList.get(0);
		if (obj == null)
			return null;
		return getJournalArticle(obj);
	}

	public JournalArticle byClassPK(Long classpk) {
		String query = "from JournalArticle j where j.resourceprimkey=" + classpk + "order by version desc";
		List<JournalArticle> journals = journalDao.getAll(query);
		if (CollectionUtils.isEmpty(journals))
			return null;
		return journals.get(0);
	}

	public JournalArticle byClassPKAndSearchTerms(Long classpk, String searchTerm) {
		String query = "from JournalArticle j where title LIKE " + "'%" + searchTerm + "%' and j.resourceprimkey=" + classpk + "order by version desc";
		List<JournalArticle> journals = journalDao.getAll(query);
		if (CollectionUtils.isEmpty(journals))
			return null;
		return journals.get(0);
	}

	public JournalArticle getServiceByAssteEntryClassUuIdAndSearchTerm(String classuuid, String searchTerm) {
		String queryStr = "select articleid, max(version) from JournalArticle journalArticle  where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ")  and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_='" + classuuid + "')";
		List<Object> objectList = journalDao.byQueryString(queryStr);
		Object[] obj = (Object[]) objectList.get(0);
		if (obj[0] == null || obj[1] == null)
			return null;

		Long articleId = Long.parseLong(obj[0].toString());
		String version = obj[1].toString();
		return getJournalArticleByArticleIdAndVersion(articleId, version);
	}

	public JournalArticle getJournalArticleByAssteEntryClassUuId(String classuuid) {
		String queryStr = "select articleid, max(version) from JournalArticle journalArticle  where journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_='" + classuuid + "')";
		List<Object> objectList = journalDao.byQueryString(queryStr);
		Object[] obj = (Object[]) objectList.get(0);
		if (obj[0] == null || obj[1] == null)
			return null;

		Long articleId = Long.parseLong(obj[0].toString());
		String version = obj[1].toString();
		return getJournalArticleByArticleIdAndVersion(articleId, version);
	}

	public JournalArticle getJournalArticleByClassPK(long classpk) {
		String queryStr = "from JournalArticle journalArticle where journalArticle.resourceprimkey=" + classpk + " order by version desc";
		return  journalDao.getAll(queryStr).get(0);
		/*
		 * List<Object> objectList = journalDao.byQueryString(queryStr); Object[] obj =
		 * (Object[]) objectList.get(0); if (obj[0] == null || obj[1] == null) return
		 * null;
		 * 
		 * Long articleId = Long.parseLong(obj[0].toString()); String version =
		 * obj[1].toString(); return getJournalArticleByArticleIdAndVersion(articleId,
		 * version);
		 */
	}

	public int getCount(String searchTerm, long classTypeId) {
		String queryStr = "select distinct articleid from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1))";
		return journalDao.findLongByQueryString(queryStr).size();
	}

	public long getJobBySearchterm(String searchTerm, long classTypeId) {
		String queryStr = "select count(*) from JournalArticle journalArticle where title LIKE " + "'%" + searchTerm + "%'" + " and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1))";
		return journalDao.findLongByQueryString(queryStr).get(0);
	}

	public long getTenderBySearchterm(String searchTerm, long classTypeId) {
		String queryStr = "select count(*) from JournalArticle journalArticle where title LIKE " + "'%" + searchTerm + "%'" + " and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1))";
		return journalDao.findLongByQueryString(queryStr).get(0);
	}

	public long getFormBySearchterm(String searchTerm, long classTypeId) {
		String queryStr = "select count(*) from JournalArticle journalArticle where title LIKE " + "'%" + searchTerm + "%'" + " and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1))";
		return journalDao.findLongByQueryString(queryStr).get(0);
	}

	public long getCountBySearchterm(String searchTerm, long classTypeId) {
		String queryStr = "select distinct articleid from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1))";
		return journalDao.findLongByQueryString(queryStr).size();
	}

	public List<Object> getServiceByTopicAndSearchTerm(long categoryId, String searchTerm) {
		String query = "select articleid, max(version)  from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in(Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85099 and ae.entryid in(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId
		        + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + ")) order by ae.createdate desc)) group by articleid";
		return journalDao.findByQueryString(query);
	}

	public List<Object> getDocumentByTopicAndSearchTerm(long categoryId, String searchTerm) {
		String query = "select articleid, max(version) from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in(Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 84948 and ae.entryid in(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId
		        + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + ")) order by ae.createdate desc)) group by articleid";
		return journalDao.findByQueryString(query);
	}

	public List<Object> getFormByTopicAndSearchTerm(long categoryId, String searchTerm) {
		String query = "select articleid, max(version)   from JournalArticle journalArticle where  (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in(Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85212 and ae.entryid in(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId
		        + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + ")) order by ae.createdate desc)) group by articleid";
		return journalDao.findByQueryString(query);
	}

	public List<Object> getJobAndVacancyByTopicAndSearchTerm(long categoryId, String searchTerm) {
		String query = "select articleid, max(version) from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in(Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85090 and ae.entryid in(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId
		        + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + ")) order by ae.createdate desc)) group by articleid";
		return journalDao.findByQueryString(query);
	}

	public List<Object> getTenderByTopicAndSearchTerm(long categoryId, String searchTerm) {
		String query = "select articleid, max(version) from JournalArticle journalArticle where (title LIKE " + "'%" + searchTerm + "%'" + " or content LIKE " + "'%" + searchTerm + "%'" + ") and journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in(Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85086 and ae.entryid in(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId
		        + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + ")) order by ae.createdate desc)) group by articleid";
		return journalDao.findByQueryString(query);
	}

	@Override
	public List<JournalArticle> getJournalArticles() {
		String query = "SELECT title, content, displaydate, max(version) FROM JournalArticle journal where journal.folderid=86245 group by journal.articleid";
		return journalDao.findByQueryStringWithSize(query, 10);
	}

	@Override
	public void hibernateInitialize(JournalArticle journalArticle) {
		Hibernate.initialize(journalArticle.getTitle());
		Hibernate.initialize(journalArticle.getContent());
		Hibernate.initialize(journalArticle.getDisplaydate());
	}

	@Override
	public void hibernateInitializeJournalArticleList(List<JournalArticle> journalArticleList) {
		journalArticleList.forEach(journalArticle -> {
			hibernateInitialize(journalArticle);
		});
	}

	@Override
	public JournalArticle getImage(long Id) {
		List<JournalArticle> journalArticleList;
		String queryStr = "select journalArticle from JournalArticle journalArticle where journalArticle.articleid =" + Id;
		journalArticleList = journalDao.byQuery(queryStr);
		return journalArticleList.get(0);
	}

	public List<JournalArticle> getImagebyClassuuid() {
		List<JournalArticle> articleList = new ArrayList<JournalArticle>();
		String queryStr = "Select journalArticle from JournalArticle journalArticle  where journalArticle.articleid in (Select r.articleid from JournalArticleResource r where r.uuid_ in (Select classuuid from AssetEntry  where visible=1 and classtypeid= 46307))";
		articleList = journalDao.byQuery(queryStr);
		return articleList;
	}

	public List<Long> getAssetEntryListByClassTypeIdAndOrderByPriority(long classTypeId) {
		String query = "SELECT classpk from AssetEntry where classtypeid=" + classTypeId + " and visible = 1";
		return journalDao.findLongByQueryString(query);
	}

	public JournalArticle getJournalArticleByDate(String dateStr, Long classpk) {
		String query = "Select articleid, max(version) from JournalArticle journalArticle  where journalArticle.displaydate like '%" + dateStr + "%' and journalArticle.resourceprimkey=" + classpk;
		List<Object> objectList = journalDao.byQueryString(query);
		if (CollectionUtils.isEmpty(objectList))
			return null;

		Object[] obj = (Object[]) objectList.get(0);
		if (obj[0] == null || obj[1] == null)
			return null;

		Long articleId = Long.parseLong(obj[0].toString());
		String version = obj[1].toString();
		return getJournalArticleByArticleIdAndVersion(articleId, version);
	}
	
	public List<JournalArticle> getJournalArticlebyRprimekey(List<Long> classPKList) {
		List<JournalArticle> resjournalarticle = new ArrayList<JournalArticle>();
		for(Long classPK : classPKList) {
			String queryStr = "from JournalArticle journalArticle where  journalArticle.status= 0 And journalArticle.resourceprimkey=" + classPK + " order by version desc";
			List<JournalArticle> journalArticles = journalDao.getAll(queryStr);
			if (CollectionUtils.isEmpty(journalArticles))
				journalArticles = null;
			else {
				resjournalarticle.add(journalArticles.get(0));
			}
					
		}
		
		return resjournalarticle;
	}
}
