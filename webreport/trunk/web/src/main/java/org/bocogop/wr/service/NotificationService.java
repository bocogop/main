package org.bocogop.wr.service;

import java.util.List;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.notification.Notification;

public interface NotificationService {

	public static class NotificationSearchResult {
		private boolean hitMaxResults;
		private List<Notification> notifications;

		public NotificationSearchResult(boolean hitMaxResults, List<Notification> notifications) {
			this.hitMaxResults = hitMaxResults;
			this.notifications = notifications;
		}

		public boolean isHitMaxResults() {
			return hitMaxResults;
		}

		public List<Notification> getNotifications() {
			return notifications;
		}

	}

	Notification saveOrUpdate(Notification o);

	void delete(long notificationId) throws ServiceValidationException;

	NotificationSearchResult getNotificationsForFacility(long facilityId);

	int purgeExpiredNotifications();

}
