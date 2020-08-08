package com.portal.service;

import java.util.List;

import com.portal.entity.JournalArticle;

public interface JournalArticleService {

	public List<JournalArticle> getJournalArticles();

	public List<Long> getIdByFolderIdByName(String input, String classuuid);

	public JournalArticle getJournalArticleByFolderId(long folderId);

	public List<JournalArticle> getJournalArticlesByFolderId(long folderId);

	public JournalArticle getJournalArticleByArticleId(long articleId);

	public JournalArticle getJournalArticle(long id);

	public void hibernateInitialize(JournalArticle journalArticle);

	public void hibernateInitializeJournalArticleList(List<JournalArticle> journalArticleList);

	public JournalArticle getJournalArticleByAssteEntryClassUuId(String classuuid);

	public JournalArticle getJournalArticleByArticleIdAndVersion(long articleId, String version);

	public JournalArticle getJournalArticleforGov();

	public List<JournalArticle> getEmergenyContact();

	public JournalArticle getContactUsbyArticleAndVersion();

	public JournalArticle getTermsbyVersion();

	public List<Long> getAllArticleId();

	public JournalArticle getJournalArticleBySearchTerms(long Id, String searchTerm);

	public long getAllBySearchterm(String searchTerm, long classTypeId);

	public List<Object> getServiceByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getDocumentByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getFormByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getJobAndVacancyByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getTenderByTopicAndSearchTerm(long categoryId, String searchTerm);

	public String getClassUuidByArticleId(long articleId);

	public long getCountBySearchterm(String searchTerm, long classTypeId);

	JournalArticle getImage(long Id);

	public List<Long> getIdByFolderIdAndSearchTerms(int folderId, String searchTerm);

	public long getFormBySearchterm(String searchTerm, long classTypeId);

	public long getTenderBySearchterm(String searchTerm, long classTypeId);

	public long getJobBySearchterm(String searchTerm, long classTypeId);

	public JournalArticle getJournalArticleByAssteEntryClassUuIdAndSearchTerm(String classuuid, String searchTerm);

	public JournalArticle getJournalArticleByClassPK(long classpk);

	public JournalArticle getServiceByAssteEntryClassUuIdAndSearchTerm(String classuuid, String searchTerm);

	public int getCount(String searchTerm, long classTypeId);

	public List<Long> getIdByFolderId(String classuuid);
	
	public JournalArticle getJournalArticleByDate(String classuuid, String dateStr);
	
	public List<JournalArticle> getJournalsByDate(String dateStr, Long classTypeId);


}
