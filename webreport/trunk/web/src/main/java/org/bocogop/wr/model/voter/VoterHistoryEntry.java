package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author Connor
 *
 */
@Entity
@Table(name = "Voter_H")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "Id_H")) })
public class VoterHistoryEntry extends AbstractVoter<VoterHistoryEntry> {
	private static final long serialVersionUID = -8388522085833837401L;

	private long primaryId;

	@Column(name = "id", nullable = false)
	public long getPrimaryId() {
		return primaryId;
	}

	public void setPrimaryId(long primaryId) {
		this.primaryId = primaryId;
	}

}
