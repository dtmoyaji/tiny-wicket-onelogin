package org.tiny;

import org.tiny.sso.SamlSignControlPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author DtmOyaji
 */
//@AuthorizeInstantiation("user")
public abstract class SamlMainPage extends WebPage implements ISamlWicketMainPage {

    protected final SamlSignControlPanel signControlPanel;

    public SamlMainPage(final PageParameters parameters) {
        super(parameters);

        this.signControlPanel = new SamlSignControlPanel(
                "signControlPanel",
                parameters
        );
        this.add(this.signControlPanel);
        this.signControlPanel.setUserAcclountKey(this.getUserAccountKey());

        this.signControlPanel.showStatus(this);
    }

}
