package com.dcampus.weblib.util;

import com.dcampus.common.util.HTML;

/**
 * 进行HTML标签过滤的工具类
 *
 * @author zim
 *
 */
public class Filter {
	
	//private static HTML.Filter filter;
	/*
	static {
		filter = new HTML.Filter();
		filter.acceptElement("a", new String[] { "style", "href", "target",
				"name", "rel", "shape", "coords", "title" });
		filter.acceptElement("area", new String[] { "style", "alt", "coords",
				"href", "nohref", "shape", "target" });
		filter.acceptElement("b", null);
		filter.acceptElement("big", null);
		filter.acceptElement("blockquote", new String[] { "style", "cite" });
		filter.acceptElement("br", null);
		filter.acceptElement("caption", null);
		filter.acceptElement("center", null);
		filter.acceptElement("code", null);
		filter.acceptElement("div", new String[] { "style", "align", "class" });
		filter.acceptElement("em", null);
		filter.acceptElement("form", new String[] { "action", "enctype",
				"method", "type", "target", "onsubmit" });
		filter.acceptElement("font", new String[] { "size", "color", "face",
				"style", "class" });
		filter.acceptElement("h1", null);
		filter.acceptElement("h2", null);
		filter.acceptElement("h3", null);
		filter.acceptElement("h4", null);
		filter.acceptElement("h5", null);
		filter.acceptElement("h6", null);
		filter.acceptElement("hr", null);
		filter.acceptElement("i", null);
		filter.acceptElement("img", new String[] { "style", "src", "width",
				"height", "border", "vspace", "hspace", "alt", "title",
				"align", "ismap", "usemap" });
		filter.acceptElement("input", new String[] { "style", "alt", "align",
				"checked", "disabled", "maxlength", "name", "readonly", "src",
				"size", "type", "value" });
		filter.acceptElement("li", new String[] { "style", "type", "value" });
		filter.acceptElement("map", new String[] { "id", "name" });
		filter.acceptElement("ol", new String[] { "compact", "start", "type" });
		filter.acceptElement("option", new String[] { "disabled", "label",
				"selected", "value" });
		filter.acceptElement("p", new String[] { "align" });
		filter.acceptElement("pre", new String[] { "width" });
		filter.acceptElement("s", null);
		filter.acceptElement("select", new String[] { "disabled", "multiple",
				"name", "size" });
		filter.acceptElement("small", null);
		filter.acceptElement("span", new String[] { "lang", "style" });
		filter.acceptElement("strike", null);
		filter.acceptElement("strong", null);
		filter.acceptElement("sub", null);
		filter.acceptElement("sup", null);
		filter.acceptElement("tbody", null);
		filter.acceptElement("table", new String[] { "style", "align",
				"border", "cellpadding", "cellspacing", "summary", "width",
				"class" });
		filter.acceptElement("td", new String[] { "style", "abbr", "align",
				"colspan", "height", "nowrap", "rowspan", "valign", "width",
				"class" });
		filter.acceptElement("textarea", new String[] { "style", "cols",
				"rows", "disabled", "readonly", "name" });
		filter.acceptElement("th", new String[] { "style", "abbr", "align",
				"colspan", "height", "nowrap", "rowspan", "valign", "width" });
		filter.acceptElement("tr", new String[] { "style", "align", "valign" });
		filter.acceptElement("tt", null);
		filter.acceptElement("u", null);
		filter.acceptElement("fieldset", new String[] { "class" });
		filter.acceptElement("legend", new String[] { "class" });
		filter.acceptElement("ul", new String[] { "compact", "type" });
		filter.acceptElement("var", null);

		filter.removeElement("head");
		filter.removeElement("style");
		filter.removeElement("meta");
		filter.removeElement("link");
		filter.removeElement("title");
		filter.removeElement("script");
		filter.removeElement("noscript");

		filter.acceptEmptyElement("area");
		filter.acceptEmptyElement("br");
		filter.acceptEmptyElement("img");
		filter.acceptEmptyElement("input");
		filter.acceptEmptyElement("hr");
		filter.acceptEmptyElement("li");
		filter.acceptEmptyElement("option");
		filter.acceptEmptyElement("p");
		filter.acceptEmptyElement("select");
		filter.acceptEmptyElement("td");
		filter.acceptEmptyElement("textarea");
		filter.acceptEmptyElement("th");
		filter.acceptEmptyElement("tr");
		filter.acceptEmptyElement("td");
		filter.acceptEmptyElement("textarea");
		
        
		
		//filter.replaceAttribValue("a", "target", null, "_blank");
		//filter.replaceAttribValue("form", "target", null, "_blank");
	}
*/
	public static String convertHtmlBody(String body) {
		try {
			return HTML.trip(body);
		} catch (Exception e) {
			return body;
		}
	}
}
