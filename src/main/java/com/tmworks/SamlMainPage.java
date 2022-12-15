package com.tmworks;

import com.tmworks.sso.SamlSignControlPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author DtmOyaji
 */
//@AuthorizeInstantiation("user")
public class SamlMainPage extends WebPage {

    private final SamlSignControlPanel signControlPanel;

    public SamlMainPage(final PageParameters parameters) {
        super(parameters);
        
        this.signControlPanel = new SamlSignControlPanel(
                "signControlPanel",
                parameters
        );
        this.add(this.signControlPanel);

        this.signControlPanel.showStatus(this);
    }
}
