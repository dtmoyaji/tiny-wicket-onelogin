package com.tmworks;

import com.tmworks.sso.SamlLoginPage;
import com.tmworks.sso.SamlLogoutPage;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.html.WebPage;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 *
 * @see com.tmworks.Start#main(String[])
 */
public class WicketApplication extends AuthenticatedWebApplication {

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();

        //エンコーディングの指定
        this.getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
        this.getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        // needed for the styling used by the quickstart
        getCspSettings().blocking()
                .add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.STYLE_SRC, "https://fonts.googleapis.com/css")
                .add(CSPDirective.FONT_SRC, "https://fonts.gstatic.com");

        // add your configuration here
        this.mountPage("MainPage", MainPage.class);
        this.mountPage("SamlLogin", SamlLoginPage.class);
        this.mountPage("SamlLogout", SamlLogoutPage.class);
    }

    /**
     * @return @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return MainPage.class;

    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return AuthenticatedSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return SamlLoginPage.class;
    }
}
