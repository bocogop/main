package org.bocogop.shared.model;

import java.io.Serializable;
import java.security.MessageDigest;

import org.apache.commons.lang3.StringUtils;

public class HashSummary implements Serializable {
	private static final long serialVersionUID = -1112389637275043740L;

	private static final String STRING_TOKEN = "::";

	private String hash;
	private String algorithm = null;
	private String providerName = null;
	private String providerInfo = null;
	private double providerVersion;

	public HashSummary(String hash, MessageDigest digest) {
		this.hash = hash;
		this.algorithm = digest.getAlgorithm();
		this.providerName = digest.getProvider().getName();
		this.providerInfo = digest.getProvider().getInfo();
		this.providerVersion = digest.getProvider().getVersion();
	}

	private HashSummary(String hash) {
		this.hash = hash;
	}

	public String getHash() {
		return hash;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getProviderName() {
		return providerName;
	}

	public String getProviderInfo() {
		return providerInfo;
	}

	public double getProviderVersion() {
		return providerVersion;
	}

	public String getHashSummary() {
		return algorithm + STRING_TOKEN + providerName + STRING_TOKEN + providerVersion + STRING_TOKEN + hash;
	}

	public String toString() {
		return getHashSummary();
	}

	public static HashSummary valueOf(String hashSummary) {
		if (StringUtils.isEmpty(hashSummary))
			return null;

		String[] tokens = hashSummary.split(STRING_TOKEN);
		HashSummary summary = new HashSummary(tokens[3]);
		summary.algorithm = tokens[0];
		summary.providerName = tokens[1];
		summary.providerVersion = Double.valueOf(tokens[2]);
		return summary;
	}
}
