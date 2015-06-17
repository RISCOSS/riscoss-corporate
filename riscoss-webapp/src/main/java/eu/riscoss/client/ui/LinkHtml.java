package eu.riscoss.client.ui;

import com.google.gwt.safehtml.shared.SafeHtml;

@SuppressWarnings("serial")
public class LinkHtml implements SafeHtml {
	private String label;
	private String link;
	public LinkHtml( String label, String link ) {
		this.label = label;
		this.link = link;
	}
	@Override
	public String asString() {
		return "<a href='javascript:' onclick='" + link + "'>" + label + "</a>";
	}
}