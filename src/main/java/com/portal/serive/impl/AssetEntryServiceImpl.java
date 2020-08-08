package com.portal.serive.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.AssetEntryDao;
import com.portal.entity.AssetEntry;
import com.portal.service.AssetEntryService;
import com.portal.service.MessageService;

@Service("assetEntryService")
public class AssetEntryServiceImpl implements AssetEntryService {

	@Autowired
	private AssetEntryDao assetEntryDao;

	@Autowired
	private MessageService messageService;

	private static Logger logger = Logger.getLogger(AssetEntryServiceImpl.class);

	// KLLS
	@Override
	public AssetEntry getAssetEntryByClassTypeCategoryIdTitle(long cid, long ctid, String title) {
		List<AssetEntry> entryList;
		AssetEntry entry = new AssetEntry();
		String queryStr = "select entry from AssetEntry entry where entry.classtypeid=" + ctid + " and visible=1 and entry.entryid in " + "(Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + cid + ")";
		System.out.println("categoryid..............." + cid + ".........title..........." + title);
		entryList = assetEntryDao.byQuery(queryStr);

		if (cid == 80321) {
			for (AssetEntry ae : entryList) {
				if (ae.getViewcount() > 0)
					entry = ae;
			}
		} else {
			for (AssetEntry ae : entryList)
				entry = ae;

		}

		return entry;
	}

	@Override
	public AssetEntry getAssetEntryByClassTypeCategoryTitle(long cid, String engtitle, String myantitle) {
		List<AssetEntry> entryList;
		engtitle = engtitle.replaceAll("\\s", "");
		myantitle = myantitle.replaceAll("\\s", "");
		System.out.println("eng..." + engtitle + "...myan.." + myantitle);
		AssetEntry entry = new AssetEntry();
		String queryStr = "select entry from AssetEntry entry where entry.classtypeid=91073 and visible=1";
		entryList = assetEntryDao.byQuery(queryStr);
		for (AssetEntry ae : entryList) {
			String title = ae.getTitle().replaceAll("\\s", "");

			if (title.contains(engtitle) == true || title.contains(myantitle) == true) {
				entry = ae;
			}

		}

		return entry;
	}

	@Override
	public AssetEntry getAssetEntryMNREC(String title) {
		List<AssetEntry> entryList;
		String queryStr = "select entry from AssetEntry entry where classtypeid=91073 and visible=1 and title like '%" + title + "%'";
		entryList = assetEntryDao.byQuery(queryStr);
		return entryList.get(0);
	}

	// KLLS
	@Override
	public AssetEntry getAssetEntryByClassType(long cid) {
		List<AssetEntry> entryList;
		AssetEntry entry = new AssetEntry();
		String queryStr = "select entry from AssetEntry entry where visible=1 and viewcount>0 and entry.classtypeid=" + cid;
		entryList = assetEntryDao.byQuery(queryStr);
		for (AssetEntry ae : entryList) {
			entry = ae;
		}

		return entry;
	}

	@Override
	public AssetEntry getAssetEntry(long id) {
		List<AssetEntry> entryList;
		String queryStr = "select entry from AssetEntry entry where entry.entryid=" + id;
		entryList = assetEntryDao.byQuery(queryStr);
		return entryList.get(0);
	}

	public List<String> getAssetEntryListByClassTypeId(long classTypeId) {
		String query = "SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1 order by createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListByClassTypeIdAndOrderByPriority(long classTypeId) {
		String query = "SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1 order by priority desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListByName(long classTypeId, String searchTerm) {
		String query = "SELECT classuuid from AssetEntry where title LIKE " + "'%" + searchTerm + "%'" + " and classtypeid=" + classTypeId + " and visible = 1 order by priority desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<Object> byClassTypeId(long classTypeId) {
		String query = "SELECT classuuid, classpk from AssetEntry where classtypeid=" + classTypeId + " and visible = 1";
		return assetEntryDao.findByQueryString(query);
	}

	public List<String> getAssetEntryListByClassTypeIdAndViewCount(long classTypeId) {
		String query = "SELECT classuuid from AssetEntry where classtypeid=" + classTypeId + " and visible = 1 order by viewCount desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForServicesByViewCount(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85099 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.viewCount desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForServicesByLatest(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85099 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForTendersByViewCount(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85086 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.viewCount desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForTendersByLatest(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85086 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForJobAndVacancyByViewCount(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85090 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.viewcount desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForJobAndVacancyByLatest(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85090 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForServices(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85099 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForLiveStockService(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.layoutuuid='79c2b1f9-ce78-abb4-873d-90bc9d52f02f' and ae.classtypeid= 85099 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForTender(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85086 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForForm(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85212 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForJobAndVacancy(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 85090 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForLiveStockJobAndVacancy(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.layoutuuid='79c2b1f9-ce78-abb4-873d-90bc9d52f02f' and ae.classtypeid= 85090 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.entryid desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForDocument(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 84948 and ae.entryid in((Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + " or aeac.categoryid in (Select ac.categoryid from AssetCategory ac where ac.parentcategoryid=" + categoryId + "))) order by ae.createdate";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListForDocumentByAllTopic() {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible = 1 and ae.classtypeid= 84948 order by ae.entryid desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getClassuuidListForPollAndSurvey(long categoryId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible=1 and ae.classtypeid= 107607 and ae.entryid in ( Select aeac.entryid from AssetEntries_AssetCategories aeac where aeac.categoryid=" + categoryId + ") order by ae.entryid desc";
		return assetEntryDao.findByQuery(query);
	}

	public List<Long> getClassNameByClassUuid(String classUuid) {
		// String query = "Select classnameid, classpk, entryid from AssetEntry where
		// classuuid='" + classUuid + "'";
		String query = "Select ae.entryid from AssetEntry ae where classuuid='" + classUuid + "'";
		return assetEntryDao.findLongByQueryString(query);
	}

	public List<Long> getClassName(String classUuid) {
		// String query = "Select classnameid, classpk, entryid from AssetEntry where
		// classuuid='" + classUuid + "'";
		String query = "Select ae.classnameid from AssetEntry ae where classuuid='" + classUuid + "'";
		return assetEntryDao.findLongByQueryString(query);
	}

	public List<Long> getClassPK(String classUuid) {
		// String query = "Select classnameid, classpk, entryid from AssetEntry where
		// classuuid='" + classUuid + "'";
		String query = "Select ae.classpk from AssetEntry ae where classuuid='" + classUuid + "'";
		return assetEntryDao.findLongByQueryString(query);
	}

	public List<String> getAssetEntryByTitle(String title) {
		String query = "Select classuuid from AssetEntry where classtypeId=85767 and visible=1 and title like '%" + title + "%'";
		return assetEntryDao.findByQuery(query);
	}

	public List<String> getAssetEntryListByClassTypeId1(long classTypeId) {
		String query = "Select ae.classuuid from AssetEntry ae where ae.visible=1 and ae.classtypeid= 46307";
		return assetEntryDao.findByQuery(query);
	}

	public long getClassPK() {
		String query = "SELECT classpk from AssetEntry where visible=1 and entryid in (Select entryid from AssetEntries_AssetCategories where categoryId=126202) order by entryid desc";
		return assetEntryDao.findLongByQueryString(query).get(0);
	}

	public List<String> getClassUuidByDate(Long classTypeId, String dateStr) {
		String query = "select classuuid from AssetEntry ae where classtypeid=" + classTypeId + "and createdate like '%" + dateStr + "%' ";
		return assetEntryDao.findByQuery(query);
	}
}
