package org.tiny.wicket;

import org.tiny.wicket.onelogin.SamlSLOPage;
import org.tiny.wicket.onelogin.SamlSSOPage;
import org.tiny.wicket.onelogin.SamlSession;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.markup.html.WebPage;

/**
 * このアプリケーションを継承し、メソッドを作成すると、SAMLを使ったユーザー認証が出来る。
 *
 * @see com.tmworks.Start#main(String[])
 */
public abstract class SamlWicketApplication extends AuthenticatedWebApplication implements ISamlWicketApplication {

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

        this.mountSamlPages(this.getSSOPageMountPoint(), this.getSLOPageMountPoint());
    }

    /**
     * 必要に応じて継承し、SamlSinginPageとSamlLogoutPageのマウント先を変更する。
     */
    private void mountSamlPages(String loginPagePoint, String logOutPagePoint) {
        this.mountPage(loginPagePoint, SamlSSOPage.class);
        this.mountPage(logOutPagePoint, SamlSLOPage.class);
    }

    /**
     * @return @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return SamlMainPage.class;

    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return SamlSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return SamlSSOPage.class;
    }
}
