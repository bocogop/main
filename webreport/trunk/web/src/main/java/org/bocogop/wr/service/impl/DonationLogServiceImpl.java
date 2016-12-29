package org.bocogop.wr.service.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationLogFile;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationLinkType;
import org.bocogop.wr.model.notification.NotificationSeverityType;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.service.DonationLogService;
import org.bocogop.wr.service.NotificationService;
import org.bocogop.wr.util.DateUtil;

@Service
public class DonationLogServiceImpl extends AbstractServiceImpl implements DonationLogService {
	private static final Logger log = LoggerFactory.getLogger(DonationLogServiceImpl.class);

	private SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

	@Value("${payGovActivity.downloadURL}")
	private String activityDownloadUrl;
	@Value("${payGovActivity.reportName}")
	private String reportName;
	@Value("${payGovActivity.userName}")
	private String username;
	@Value("${payGovActivity.password}")
	private String password;
	@Value("${payGovActivity.deidentifyDonors}")
	private boolean deidentifyDonors;
	@Value("${payGovActivity.maxCatchUpDays}")
	private int maxCatchUpDays;

	@Autowired
	private NotificationService notificationService;

	@Override
	public Map<LocalDate, List<DonationLog>> updateExternalDonations(LocalDate forceRefreshDate)
			throws IOException, ParserConfigurationException, SAXException {
		LocalDate today = LocalDate.now();
		LocalDate startDate = today.minusDays(maxCatchUpDays);
		SortedSet<LocalDate> existingDates = donationLogFileDAO.getExistingDatesOnOrAfter(startDate);

		Set<LocalDate> missingDates = new TreeSet<>();
		if (forceRefreshDate != null) {
			missingDates.add(forceRefreshDate);
		} else {
			for (LocalDate d = startDate; d.isBefore(today); d = d.plusDays(1))
				if (!existingDates.contains(d))
					missingDates.add(d);
		}

		Map<LocalDate, List<DonationLog>> results = new TreeMap<>();

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(activityDownloadUrl);
			
			Set<String> facilities = new HashSet<>();
			
			for (LocalDate date : missingDates) {
				try {
					List<NameValuePair> nvps = new ArrayList<>();
					nvps.add(new BasicNameValuePair("reportName", reportName));
					nvps.add(new BasicNameValuePair("reportDate", date.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT)));
					nvps.add(new BasicNameValuePair("userName", username));
					nvps.add(new BasicNameValuePair("password", password));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps));

					String fileContents;
					try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
						HttpEntity entity = response.getEntity();
						if (entity == null)
							throw new IOException("No file found at the URL " + activityDownloadUrl);

						try (BufferedInputStream isr = new BufferedInputStream(entity.getContent())) {
							fileContents = IOUtils.toString(isr);
						} finally {
							EntityUtils.consume(entity);
						}
					}

					if (fileContents.contains("Internal Server Error")) {
						log.error("There was an error downloading a pay.gov donation file for the date "
								+ date.format(DateUtil.DATE_ONLY_FORMAT) + ": the pay.gov server had an error:\n"
								+ fileContents);
						continue;
					}

					List<DonationLog> logs = null;
					try {
						logs = importExternalDonations(IOUtils.toInputStream(fileContents));
					} catch (Exception e) {
						fileContents = ExceptionUtils.getFullStackTrace(e) + "\n\n---------------" + fileContents;
					}

					final List<DonationLog> finalLogs = logs;
					final String finalFileContents = fileContents;

					TransactionTemplate transactionTemplate = new TransactionTemplate(tm);
					transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
					transactionTemplate.execute(new TransactionCallback<DonationLogFile>() {
						@Override
						public DonationLogFile doInTransaction(TransactionStatus status) {
							DonationLogFile dlf = donationLogFileDAO.getByDate(date);

							if (dlf != null) {
								dlf.getDonations().clear();
							} else {
								dlf = new DonationLogFile();
								dlf.setFileDate(date);
							}
							dlf.setFileContents(deidentifyDonors && finalLogs != null
									? "[Redacted donation log details]" : finalFileContents);
							dlf = donationLogFileDAO.saveOrUpdate(dlf);

							results.put(date, finalLogs);

							if (finalLogs != null)
								for (DonationLog l : finalLogs) {
									l.setDonationLogFile(dlf);
									if (log.isDebugEnabled())
										log.debug("Donation log found for date {}: {}",
												date.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT), l);
									dlf.getDonations().add(l);
									facilities.add(l.getFacility());
								}
							dlf = donationLogFileDAO.saveOrUpdate(dlf);

							return dlf;
						}
					});

					// let's not bang on their system - CPB
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				} catch (Exception dayException) {
					log.error("There was an error processing day {}, skipping...", dayException);
				}
			}
			
			Permission eDonationPermission = permissionDAO.findByLookup(PermissionType.EDONATION_MANAGE);

			for (String facilityNum : facilities) {
				Facility f = facilityDAO.findByStationNumber(facilityNum);
				if (f == null) {
					log.warn(
							"A donation arrived with facility station number '{}' but no facility exists with that station number.",
							facilityNum);
					continue;
				}

				notificationService.saveOrUpdate(new Notification("New E-Donation posted",
						"A new e-donation was received from pay.gov",
						NotificationSeverityType.MEDIUM, NotificationType.DONATION, LocalDate.now(), null, null,
						null, true, NotificationLinkType.DONATION_LOG).withTargetFacility(f)
								.withTargetPermission(eDonationPermission));
			}
		}

		if (log.isInfoEnabled()) {
			log.info("Donation log import report:");
			for (Entry<LocalDate, List<DonationLog>> entry : results.entrySet()) {
				log.info("\t{}: {} donations imported", entry.getKey().format(DateUtil.DATE_ONLY_FORMAT),
						entry.getValue().size());
			}
		}

		return results;
	}

	@Override
	public List<DonationLog> importExternalDonations(InputStream is)
			throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = saxParserFactory.newSAXParser();
		MyHandler handler = new MyHandler(deidentifyDonors);
		saxParser.parse(is, handler);
		List<DonationLog> empList = handler.getResults();
		return empList;
	}

	static class MyHandler extends DefaultHandler {

		static enum Tag {
			TRACKING_ID, TRANSACTION_STATUS, TRANSACTION_DATE, TRANSACTION_AMOUNT, FACILITY, NAME, ADDRESS, CITY, STATE, ZIP, PHONE, EMAIL, PROGRAM_FIELD, ADDITIONAL_INFO, DEPOSIT_NUMBER;
		}

		private String type; // credit_card, ach_debit, etc
		private List<DonationLog> donationList = new ArrayList<>();
		private DonationLog current = null;

		private boolean deidentifyDonors;

		public MyHandler(boolean deidentifyDonors) {
			this.deidentifyDonors = deidentifyDonors;
		}

		// getter method for employee list
		public List<DonationLog> getResults() {
			return donationList;
		}

		private Tag currentTag;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			if (qName.equalsIgnoreCase("credit_card") || qName.equalsIgnoreCase("ach_debit")) {
				type = qName;
			}

			if (qName.equalsIgnoreCase("item_detail_record")) {
				// String id = attributes.getValue("id");
				current = new DonationLog();
				current.setType(type);
			} else if (qName.equalsIgnoreCase("paygov_tracking_id")) {
				currentTag = Tag.TRACKING_ID;
			} else if (qName.equalsIgnoreCase("transaction_status")) {
				currentTag = Tag.TRANSACTION_STATUS;
			} else if (qName.equalsIgnoreCase("transaction_date")) {
				currentTag = Tag.TRANSACTION_DATE;
			} else if (qName.equalsIgnoreCase("transaction_amount")) {
				currentTag = Tag.TRANSACTION_AMOUNT;
			} else if (qName.equalsIgnoreCase("Facility")) {
				currentTag = Tag.FACILITY;
			} else if (qName.equalsIgnoreCase("Name_Field")) {
				currentTag = Tag.NAME;
			} else if (qName.equalsIgnoreCase("Street_Address_Field")) {
				currentTag = Tag.ADDRESS;
			} else if (qName.equalsIgnoreCase("City_Field")) {
				currentTag = Tag.CITY;
			} else if (qName.equalsIgnoreCase("State1_Field")) {
				currentTag = Tag.STATE;
			} else if (qName.equalsIgnoreCase("ZipCode_Field")) {
				currentTag = Tag.ZIP;
			} else if (qName.equalsIgnoreCase("PhoneNumber_Field")) {
				currentTag = Tag.PHONE;
			} else if (qName.equalsIgnoreCase("account_holder_email_address")) {
				currentTag = Tag.EMAIL;
			} else if (qName.toLowerCase().startsWith("program_field_")) {
				currentTag = Tag.PROGRAM_FIELD;
			} else if (qName.equalsIgnoreCase("AdditionalInformation")) {
				currentTag = Tag.ADDITIONAL_INFO;
			} else if (qName.equalsIgnoreCase("deposit_ticket_number")) {
				currentTag = Tag.DEPOSIT_NUMBER;
			}

			sb.setLength(0);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			try {
				if (qName.equalsIgnoreCase("item_detail_record")) {
					donationList.add(current);
					current = null;
					return;
				}

				if (currentTag == null || current == null) {
					return;
				}

				String strVal = sb.toString().replaceAll("\\s+", " ").trim();

				switch (currentTag) {
				case TRACKING_ID:
					current.setTrackingId(strVal);
					break;
				case ADDITIONAL_INFO:
					current.setAdditionalInfo(deidentifyDonors ? RandomStringUtils.randomAlphabetic(10, 30) : strVal);
					break;
				case ADDRESS:
					current.setAddress(
							deidentifyDonors ? "123 " + RandomStringUtils.randomAlphabetic(10, 20) + " St." : strVal);
					break;
				case CITY:
					current.setCity(strVal);
					break;
				case DEPOSIT_NUMBER:
					current.setDepositNumber(strVal);
					break;
				case EMAIL:
					current.setEmail(deidentifyDonors ? RandomStringUtils.randomAlphabetic(8) + "@"
							+ RandomStringUtils.randomAlphabetic(5) + ".com" : strVal);
					break;
				case FACILITY:
					current.setFacility(strVal);
					break;
				case NAME:
					current.setName(deidentifyDonors
							? RandomStringUtils.randomAlphabetic(4, 8) + " " + RandomStringUtils.randomAlphabetic(4, 8)
							: strVal);
					break;
				case PHONE:
					current.setPhone(deidentifyDonors ? RandomStringUtils.randomNumeric(3) + "-"
							+ RandomStringUtils.randomNumeric(3) + "-" + RandomStringUtils.randomNumeric(4) : strVal);
					break;
				case PROGRAM_FIELD:
					if (!"0".equals(strVal))
						current.setProgramField(strVal);
					break;
				case STATE:
					current.setState(strVal);
					break;
				case TRANSACTION_AMOUNT:
					current.setDonationAmount(
							strVal.length() == 0 ? null : new BigDecimal(strVal.replaceAll("[^\\d\\.]", "")));
					break;
				case TRANSACTION_DATE:
					current.setTransactionDate(strVal.length() == 0 ? null : LocalDateTime.parse(strVal));
					break;
				case TRANSACTION_STATUS:
					current.setStatus(strVal);
					break;
				case ZIP:
					current.setZip(strVal);
					break;
				default:
					throw new AssertionError("Unexpected value: " + currentTag);
				}
			} finally {
				sb.setLength(0);
				currentTag = null;
			}
		}

		private StringBuilder sb = new StringBuilder();

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			sb.append(ch, start, length);
		}
	}

}
