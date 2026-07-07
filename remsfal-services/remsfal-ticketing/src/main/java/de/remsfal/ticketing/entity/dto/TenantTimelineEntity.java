package de.remsfal.ticketing.entity.dto;

import de.remsfal.core.model.ticketing.TenantTimelineModel;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("tenant_timelines")
public class TenantTimelineEntity extends AbstractEntity implements TenantTimelineModel {

	@Id
	private TenantTimelineKey key;

	@Column("url")
	private String url;

	@Column("title")
	private String title;

	@Column("message")
	private String message;

	@Column("role")
	private String role;

	public TenantTimelineKey getKey() {
		return key;
	}

	public void setKey(TenantTimelineKey key) {
		this.key = key;
	}

	@Override
	public UUID getIssueId() {
		return Optional.ofNullable(key)
			.map(TenantTimelineKey::getIssueId)
			.orElse(null);
	}

	@Override
	public UUID getTenantId() {
		return Optional.ofNullable(key)
			.map(TenantTimelineKey::getTenantId)
			.orElse(null);
	}

	@Override
	public UUID getTimelineId() {
		return Optional.ofNullable(key)
			.map(TenantTimelineKey::getTimelineId)
			.orElse(null);
	}

	@Override
	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public String getRole() {
		return role;
	}

	public void setRole(final String role) {
		this.role = role;
	}

	private TenantTimelineKey getOrCreateKey() {
		if (this.key == null) {
			this.key = new TenantTimelineKey();
		}
		return this.key;
	}
}