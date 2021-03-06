package com.portal.service;

import java.util.List;

import com.portal.entity.JournalArticle;

public interface JournalArticleService {

	public JournalArticle getMaxVersionJournalByArticleId(String articleId);

	public JournalArticle getJournalArticleByResourcePrimKey(long resourcePrimKey);

	public JournalArticle byClassPKAndTitle(Long classpk, String searchTerm);

	public List<JournalArticle> getJournalArticles();

	public List<JournalArticle> getIdByFolderIdByName(String input, String classuuid);

	public JournalArticle getJournalArticleByFolderId(long folderId);

	public JournalArticle getJournalArticleByArticleId(String articleId);

	public JournalArticle getJournalArticle(long id);

	public void hibernateInitialize(JournalArticle journalArticle);

	public void hibernateInitializeJournalArticleList(List<JournalArticle> journalArticleList);

	public JournalArticle getJournalArticleByArticleIdAndVersion(long articleId, String version);

	public JournalArticle getJournalArticleforGov();

	public List<JournalArticle> getEmergenyContact();

	public JournalArticle getContactUsbyArticleAndVersion();

	public JournalArticle getTermsbyVersion();

	public List<Long> getAllArticleId();

	public JournalArticle getJournalArticleBySearchTerms(long Id, String searchTerm);

	public List<Object> getServiceByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getDocumentByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getFormByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getJobAndVacancyByTopicAndSearchTerm(long categoryId, String searchTerm);

	public List<Object> getTenderByTopicAndSearchTerm(long categoryId, String searchTerm);

	public String getClassUuidByArticleId(long articleId);

	public long getCountBySearchterm(String searchTerm, long classTypeId);

	JournalArticle getImage(long Id);

	public List<JournalArticle> getIdByFolderIdAndSearchTerms(int folderId, String searchTerm);

	public long getFormBySearchterm(String searchTerm, long classTypeId);

	public long getTenderBySearchterm(String searchTerm, long classTypeId);

	public long getJobBySearchterm(String searchTerm, long classTypeId);

	public JournalArticle getJournalArticleByClassPK(long classpk);

	public JournalArticle getServiceByAssteEntryClassUuIdAndSearchTerm(String classuuid, String searchTerm);

	public int getCount(String searchTerm, long classTypeId);

	public JournalArticle byClassPK(Long classpk);

	public JournalArticle getJournalArticleByDate(String classuuid, Long dateStr);

	public List<Long> getAssetEntryListByClassTypeIdAndOrderByPriority(long classTypeId);

	public JournalArticle getJournalArticleByAssteEntryClassUuId(String classuuid);

	public JournalArticle byClassPKAndSearchTerms(Long classpk, String searchTerm);

	public List<JournalArticle> getJournalArticlebyRprimekey(List<Long> classPKList);

	public List<String> byClassPKAndSearchTerm(Long classTypeId, String searchTerm);

	public List<Long> getServiceByTopicAndSearchTerm2(long categoryId, String searchTerm);

	public String getTitleByClassPK(Long classpk);

	public JournalArticle byClassPKAndDate(String dateStr, Long classpk);

	public JournalArticle byClassPK(Long classpk, String searchTerm);

	public List<Long> getJournalsByOverallSearch(String searchTerm);

}
