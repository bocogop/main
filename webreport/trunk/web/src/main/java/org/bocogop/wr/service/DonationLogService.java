package org.bocogop.wr.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.bocogop.wr.model.donation.DonationLog;

public interface DonationLogService {

	Map<LocalDate, List<DonationLog>> updateExternalDonations(LocalDate forceRefreshDate)
			throws IOException, ParserConfigurationException, SAXException;

	List<DonationLog> importExternalDonations(InputStream is)
			throws ParserConfigurationException, SAXException, IOException;
}
