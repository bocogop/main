package org.bocogop.wr.web.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingService.BenefitingServiceView;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonationType;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.organization.AbstractBasicOrganization.OrganizationView;
import org.bocogop.wr.model.organization.BasicOrganization;
import org.bocogop.wr.model.organization.OrganizationType;
import org.bocogop.wr.model.organization.StdVAVSTitle;
import org.bocogop.wr.model.views.CombinedFacility;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.benefitingService.BenefitingServiceRoleFieldType;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.interceptor.BreadcrumbsInterceptor;

@Controller
public class ReportParametersController extends AbstractAppController {
	private static final Logger log = LoggerFactory.getLogger(ReportParametersController.class);

	private static final String PROP_REPORT_PREFIX = "reports.";

	@Value("${disableHostnameVerification}")
	private boolean disableHostnameVerification;
	@Value("${ssrs.baseURL}")
	private String baseURL;
	@Value("${ssrs.reportSubdirectory}")
	private String reportSubdirectory;
	@Value("${ssrs.viewReport.baseURL}")
	private String baseViewURL;
	@Value("#{PropertySplitter.map('${donationSummary.setAcknowledgementDate.reportAndParamMap}')}")
	private Map<String, String> donationSummarySetAckDateMap;
	@Value("${ssrsEmbedFrameMargin}")
	private int ssrsEmbedFrameMargin;

	public CloseableHttpClient createHttpClient() {
		HttpClientBuilder b = HttpClientBuilder.create();

		try {
			if (disableHostnameVerification) {
				HttpsURLConnection.setDefaultHostnameVerifier(new NoopHostnameVerifier());

				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy())
						.build();
				SSLContext.setDefault(sslContext);

				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
						SSLContexts.createSystemDefault(), new NoopHostnameVerifier());
				b.setSSLSocketFactory(sslConnectionFactory);
				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
						.register("https", sslConnectionFactory)
						// .register("http", new PlainConnectionSocketFactory())
						.build();
				HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
				b.setConnectionManager(ccm);
			}

			return b.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@RequestMapping("/displayReportParameters")
	public String displayReportParameters(@RequestParam(required = true) String reportCode, ModelMap model,
			HttpServletRequest request) {
		model.put("reportBaseURL", baseURL + reportSubdirectory);
		model.put("reportViewURL", baseViewURL + reportSubdirectory);

		model.put("allStates", stateDAO.findAllSorted());
		model.put("allDonationTypes", donationTypeDAO.findAllSorted());
		model.put("currentDate", getTodayAtFacility());

		model.put("ssrsEmbedFrameMargin", ssrsEmbedFrameMargin);
		model.put("allFiscalYears", dateUtil.getAllFiscalYears(getFacilityTimeZone()));

		appendCommonReportParams(model);

		String displayName = messageSource.getMessage(PROP_REPORT_PREFIX + reportCode + ".displayName", null, null);
		model.put("pageTitle", "Specify Parameters for the " + displayName + " Report");
		model.put("pageDescription",
				"This screen allows the user to specify parameters for the " + displayName + " Report");
		model.put("ssrsReportBasename",
				messageSource.getMessage(PROP_REPORT_PREFIX + reportCode + ".ssrsReportBasename", null, null));
		model.put("disclaimerText",
				messageSource.getMessage(PROP_REPORT_PREFIX + reportCode + ".disclaimerText", null, "", null));
		BreadcrumbsInterceptor.setRequestBreadcrumb(request, "Run Report \"" + displayName + "\"");

		return messageSource.getMessage(PROP_REPORT_PREFIX + reportCode + ".tilesView", null, null);
	}

	@RequestMapping(value = "/assignedVAFacilities", method = RequestMethod.GET)
	public @ResponseBody Collection<VAFacility> getAssignedVAFacilities() {
		SortedSet<VAFacility> allLinkedFacilities = facilityDAO.findVAFacilitiesWithLinkToFacility();

		List<VAFacility> assignedFacilities = new ArrayList<VAFacility>();
		for (AppUserFacility auFac : getCurrentUser().getFacilities()) {
			VAFacility facility = auFac.getFacility();
			if (allLinkedFacilities.contains(facility))
				assignedFacilities.add(facility);
		}

		return assignedFacilities;
	}

	@RequestMapping(value = "/donorType", method = RequestMethod.GET)
	public @ResponseBody Collection<DonorType> getDonorTypes() {
		List<DonorType> allDonorTypes = donorTypeDAO.findAll();
		return allDonorTypes;
	}

	@RequestMapping(value = "/donationType", method = RequestMethod.GET)
	public @ResponseBody Collection<DonationType> getDonationTypes() {
		SortedSet<DonationType> allDonorTypes = donationTypeDAO.findAllSorted();
		return allDonorTypes;
	}

	@RequestMapping(value = "/donationReferenceList", method = RequestMethod.GET)
	public @ResponseBody Collection<DonationReference> getDonationReferenceList(
			@RequestParam(name = "stationId[]") Long[] stationIds) {
		List<DonationReference> linkedFacilities = stationIds.length == 1
				? donationReferenceDAO.findDonReferenceByFacilityId(stationIds[0]) : null;
		return linkedFacilities;
	}

	@RequestMapping(value = "/generalPostFundList", method = RequestMethod.GET)
	public @ResponseBody Collection<DonGenPostFund> getGeneralPostFundList(
			@RequestParam(name = "stationId[]") Long[] stationIds) {
		List<DonGenPostFund> funds = stationIds.length == 1 ? donGenPostFundDAO.findByFacility(stationIds[0]) : null;
		return funds;
	}

	@RequestMapping(value = "/assignedFacilities", method = RequestMethod.GET)
	public @ResponseBody Collection<CombinedFacility> getAssignedFacilities(@RequestParam boolean showAllFacilities) {
		SortedSet<CombinedFacility> linkedFacilities = combinedInstitutionDAO.findActiveWithLinkToVAFacility();
		if (showAllFacilities)
			return linkedFacilities;

		Set<Long> vaFacilityIds = PersistenceUtil.translateObjectsToIds(getCurrentUser().getAssignedVAFacilities());

		for (Iterator<CombinedFacility> it = linkedFacilities.iterator(); it.hasNext();) {
			CombinedFacility vi = it.next();
			if (!vaFacilityIds.contains(vi.getVaFacility().getId()))
				it.remove();
		}

		return linkedFacilities;
	}

	@RequestMapping("/organization")
	@JsonView(OrganizationView.Basic.class)
	public @ResponseBody SortedSet<BasicOrganization> findOrganizations(@RequestParam boolean includeLocal,
			@RequestParam boolean includeNational, @RequestParam(required = false) boolean includeBranches,
			@RequestParam(name = "stationId[]", required = false) Long[] stationIds,
			@RequestParam(name = "stationId", required = false) Long stationId,
			@RequestParam(required = false) Boolean nacOrgsOnly,
			@RequestParam(required = false) Boolean includeInactiveOrgs) {
		return new TreeSet<>(organizationDAO.findByCriteria(null, includeNational, includeLocal, includeBranches,
				stationIds != null ? Arrays.asList(stationIds) : stationId != null ? Arrays.asList(stationId) : null,
				true, null, nacOrgsOnly, includeInactiveOrgs));
	}

	@RequestMapping("/benefitingServiceList")
	@JsonView(BenefitingServiceView.Basic.class)
	public @ResponseBody SortedSet<BenefitingService> findBenefitingServices(@RequestParam boolean excludeGames,
			@RequestParam long stationId) {
		return new TreeSet<>(benefitingServiceDAO.findByCriteria(null, null, null, Arrays.asList(stationId), null,
				excludeGames ? false : null, null, null));
	}

	@RequestMapping(value = "/VAVSTitle", method = RequestMethod.GET)
	public @ResponseBody Collection<StdVAVSTitle> getVAVSTitles() {
		SortedSet<StdVAVSTitle> allVAVSTitles = stdVAVSTitleDAO.findAllSorted();
		return allVAVSTitles;
	}

	@RequestMapping(value = "/assignedVISNs", method = RequestMethod.GET)
	public @ResponseBody Collection<AdministrativeUnit> getAssignedVISNs() {
		// if (getCurrentUser().isNationalAdmin()) {
		return administrativeUnitDAO.findAllSorted();
		// } else {
		// List<VAFacility> assignedFacilities = new ArrayList<VAFacility>();
		// for (AppUserFacility auFac : getCurrentUser().getFacilities()) {
		// assignedFacilities.add(auFac.getFacility());
		// }
		// return assignedFacilities;
		// }
	}

	@RequestMapping(value = "/volunteersAtFacility", method = RequestMethod.GET)
	@JsonView(VolunteerView.Basic.class)
	public @ResponseBody SortedSet<Volunteer> getVolunteersAtFacility(@RequestParam long stationId) {
		List<Volunteer> volunteerOrgs = volunteerDAO.findByCriteria(null, null, null, false, false, null, null, null,
				null, null, null, null, null, VolunteerStatusType.ACTIVE, Arrays.asList(stationId));
		return new TreeSet<>(volunteerOrgs);
	}

	@RequestMapping(value = "/volunteerZipCodesAtFacility", method = RequestMethod.GET)
	public @ResponseBody SortedSet<String> getVolunteerZipCodesAtFacility(@RequestParam long stationId) {
		List<String> zipCodes = volunteerDAO.findZipCodesAtFacilities(Arrays.asList(stationId), true, true);
		return new TreeSet<>(zipCodes);
	}

	@RequestMapping(value = "/monthsOfYear", method = RequestMethod.GET)
	public @ResponseBody SortedSet<MultiSelectGenericCommand<Integer, String>> getMonthsOfYear() {
		SortedSet<MultiSelectGenericCommand<Integer, String>> months = new TreeSet<MultiSelectGenericCommand<Integer, String>>();
		Calendar cal = Calendar.getInstance();
		Map<String, Integer> monthMap = cal.getDisplayNames(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
		for (Map.Entry<String, Integer> entry : monthMap.entrySet()) {
			String monthName = entry.getKey();
			Integer monthInt = entry.getValue() + 1;
			months.add(new MultiSelectGenericCommand<Integer, String>(monthInt, monthName));
		}

		return months;
	}

	@RequestMapping("/printReport.htm")
	public void printReport(@RequestParam String reportName, HttpServletRequest req,
			HttpServletResponse servletResponse) throws IOException {
		processSetAckDate(reportName, req);

		servletResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
		try (CloseableHttpClient httpclient = createHttpClient();) {

			HttpPost httpPost = new HttpPost(baseURL + "?" + reportSubdirectory + reportName);
			List<NameValuePair> nvps = new ArrayList<>();

			Set<String> paramsToIgnore = new HashSet<>(
					Arrays.asList("reportName", "_csrf", "downloadCompleteCookieId"));

			Map<String, String[]> params = req.getParameterMap();
			for (String name : params.keySet()) {
				if (paramsToIgnore.contains(name))
					continue;

				for (String val : params.get(name)) {
					name = name.replaceAll("[^A-Za-z0-9\\.\\-\\:_,/]", "");
					if (val != null)
						val = val.replaceAll("[^\\p{Graph}}]", "");
					nvps.add(new BasicNameValuePair(name, val));
				}
			}

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			String downloadCompleteCookieId = req.getParameter("downloadCompleteCookieId");
			if (StringUtils.isNotBlank(downloadCompleteCookieId)) {
				downloadCompleteCookieId = downloadCompleteCookieId.replaceAll("[^A-Za-z0-9\\.,\\-_]", "");
				Cookie cookie = new Cookie(downloadCompleteCookieId, "true");
				cookie.setMaxAge(-1); // delete cookie when they close the
										// browser
				servletResponse.addCookie(cookie);
			}

			try (CloseableHttpResponse ssrsResponse = httpclient.execute(httpPost);) {
				copyEntity(ssrsResponse, servletResponse, reportName, "PDF".equals(req.getParameter("rs:Format")));
			}
		}
	}

	public void processSetAckDate(String reportName, HttpServletRequest req) {
		if (donationSummarySetAckDateMap == null)
			return;

		for (Entry<String, String> entry : donationSummarySetAckDateMap.entrySet()) {
			String rName = entry.getKey();
			String paramName = entry.getValue();

			if (reportName.equals(rName)) {
				String[] donationSummaryIds = req.getParameterValues(paramName);
				if (donationSummaryIds == null) {
					log.error("No request parameter sent with name " + paramName + " for report " + rName
							+ ", not setting " + DonationSummary.class.getSimpleName() + " acknowledgement date...");
					continue;
				}

				for (String donationSummaryId : donationSummaryIds) {
					try {
						long donSumId = Long.parseLong(donationSummaryId);
						DonationSummary ds = donationSummaryDAO.findRequiredByPrimaryKey(donSumId);

						if (ds.getAcknowledgementDate() == null) {
							ds.setAcknowledgementDate(getTodayAtFacility());
							ds = donationService.saveOrUpdateDonationSummary(ds, false, null, null, null, null);
						}
					} catch (Exception e) {
						log.error("Couldn't update the donation summary acknowledgement date during a report print", e);
					}
				}
				break;
			}
		}
	}

	private static void copyEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse, String reportName,
			boolean decoratePDFEntity) throws IOException {
		OutputStream servletOutputStream = servletResponse.getOutputStream();

		HttpEntity entity = proxyResponse.getEntity();
		if (entity == null)
			return;

		byte[] inputPdf = EntityUtils.toByteArray(entity);

		if (decoratePDFEntity) {
			try {
				PdfReader reader = new PdfReader(inputPdf);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(inputPdf.length + 2000);
				PdfStamper stamper = new PdfStamper(reader, baos);
				stamper.addJavaScript("print();");
				stamper.close();

				servletResponse.setContentType("application/pdf");
				servletResponse.setContentLength(baos.size());

				baos.writeTo(servletOutputStream);
			} catch (Exception e) {
				log.error("Error stamping PDF with additional content", e);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(inputPdf.length);
				baos.write(inputPdf);
				baos.writeTo(servletOutputStream);
			}
		} else {
			servletResponse.setContentType("application/msword");
			reportName = reportName.replaceAll("[^A-Za-z0-9\\.\\-_,]", "");
			servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".doc\"");
			servletResponse.setContentLength(inputPdf.length);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(inputPdf.length);
			baos.write(inputPdf);
			baos.writeTo(servletOutputStream);
		}
	}

	public static class IdAndDisplayName {
		public long id;
		public String name;

		public IdAndDisplayName(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

	}

	@RequestMapping("/assignmentRoleList")
	public @ResponseBody List<IdAndDisplayName> findRolesForFacility(@RequestParam long stationId) {

		List<BenefitingServiceRole> list = benefitingServiceRoleDAO.findByCriteria(null, Arrays.asList(stationId), true,
				true, new QueryCustomization(BenefitingServiceRoleFieldType.BENEFITING_SERVICE));

		Collections.sort(list, new Comparator<BenefitingServiceRole>() {
			public int compare(BenefitingServiceRole o1, BenefitingServiceRole o2) {
				return (o1.getDisplayName(true)).compareTo(o2.getDisplayName(true));
			}
		});

		List<IdAndDisplayName> results = new ArrayList<>();
		for (BenefitingServiceRole r : list)
			results.add(new IdAndDisplayName(r.getId(), r.getDisplayName(true)));
		return results;
	}

	@RequestMapping("/serviceList")
	public @ResponseBody List<IdAndDisplayName> findServicesForFacility(@RequestParam long stationId,
			@RequestParam(required = false) boolean gamesRelated,
			@RequestParam(required = false) boolean includeInactive) {

		List<BenefitingService> list = benefitingServiceDAO.findByCriteria(null, null, null,
				stationId == -1 ? Collections.emptyList() : Arrays.asList(stationId), null, gamesRelated, true,
				includeInactive);

		Collections.sort(list, new Comparator<BenefitingService>() {
			public int compare(BenefitingService o1, BenefitingService o2) {
				return ((o1.getName() + o1.getAbbreviation()).compareTo((o2.getName() + o2.getAbbreviation())));
			}
		});

		List<IdAndDisplayName> results = new ArrayList<>();
		String abbev = null;
		for (BenefitingService r : list) {
			if (r.getName() != null) {
				abbev = (r.getAbbreviation() != null && r.getAbbreviation().length() > 0)
						? " (" + r.getAbbreviation() + ")" : "";
				results.add(new IdAndDisplayName(r.getId(), r.getName() + abbev));
			}
		}
		return results;
	}

	@RequestMapping("/serviceTemplateList")
	public @ResponseBody List<IdAndDisplayName> findServiceTemplate(@RequestParam(required = false) Boolean gamesOnly,
			@RequestParam(required = false) Boolean includeInactive) {

		List<BenefitingServiceTemplate> list = benefitingServiceTemplateDAO.findByCriteria(null, true, gamesOnly,
				includeInactive);

		Collections.sort(list, new Comparator<BenefitingServiceTemplate>() {
			public int compare(BenefitingServiceTemplate o1, BenefitingServiceTemplate o2) {
				return ((o1.getName() + o1.getAbbreviation()).compareTo((o2.getName() + o2.getAbbreviation())));
			}
		});

		List<IdAndDisplayName> results = new ArrayList<>();
		String abbrev = null;
		for (BenefitingServiceTemplate r : list) {
			if (r.getName() != null) {
				abbrev = r.getAbbreviation() != null ? " (" + r.getAbbreviation() + ")" : "";
				results.add(new IdAndDisplayName(r.getId(), r.getName() + abbrev));
			}
		}
		return results;
	}

	@RequestMapping(value = "/organizationTypeList", method = RequestMethod.GET)
	public @ResponseBody Collection<OrganizationType> getOrganizationTypes() {
		SortedSet<OrganizationType> allOrgTypes = organizationTypeDAO.findAllSorted();
		return allOrgTypes;
	}

}