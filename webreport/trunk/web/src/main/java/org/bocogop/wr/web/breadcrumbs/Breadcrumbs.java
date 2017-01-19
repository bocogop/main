package org.bocogop.wr.web.breadcrumbs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class Breadcrumbs implements Serializable {
	private static final long serialVersionUID = -6556371037011322754L;

	private List<Breadcrumb> breadcrumbs = new ArrayList<>();

	public void navigate(String relativeLink, Object handler, String linkName) {
		/*
		 * find the index of the breadcrumb whose handler is the same as the
		 * request handler
		 */
		int index = -1;
		for (int i = 0; i < breadcrumbs.size(); i++) {
			Link testLink = breadcrumbs.get(i).getLink();
			if (testLink != null && testLink.getText() != null && testLink.getText().equals(linkName)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			/*
			 * never been here; add new breadcrumb with the latest request
			 * string
			 */
			breadcrumbs.add(new Breadcrumb(new Link(linkName, relativeLink)));
			return;
		}

		if (breadcrumbs.size() > index + 1) {
			// chop off everything after the matching breadcrumb
			breadcrumbs = breadcrumbs.subList(0, index + 1);
		}

		// replace matching breadcrumb with the latest request string
		Breadcrumb b = breadcrumbs.get(index);
		Link link = b.getLink();
		link.setText(linkName);
		link.setHref(relativeLink);
	}

	public void navigate(HttpServletRequest request, Object handler, String linkName) {
		String relativeLink = request.getRequestURL().toString();

		String queryString = request.getQueryString();
		if (!StringUtils.isBlank(queryString))
			relativeLink += "?" + queryString;
		relativeLink = relativeLink.replaceAll("[^\\p{Print}]", "");

		navigate(relativeLink, handler, linkName);
	}

	public List<Breadcrumb> getBreadcrumbs() {
		return breadcrumbs;
	}

	public static class Breadcrumb implements Serializable {
		private static final long serialVersionUID = -4882008060432730331L;

		private Link link;

		public Breadcrumb() {
		}

		public Breadcrumb(Link link) {
			this.link = link;
		}

		public Link getLink() {
			return link;
		}

		public void setLink(Link link) {
			this.link = link;
		}

	}

}