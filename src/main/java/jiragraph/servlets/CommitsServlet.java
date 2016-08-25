package jiragraph.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.stash.StashApplicationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;

@ExportAsService( {CommitsServlet.class} )
@Named( "bb.jira.CommitsServlet" )

public class CommitsServlet extends HttpServlet
{

	private final boolean DEBUG_MODE = false;

	@ComponentImport
	private final ApplicationLinkService applicationLinkService;

	@Inject
	public CommitsServlet( ApplicationLinkService applicationLinkService )
	{
		this.applicationLinkService = applicationLinkService;
	}

	@Override
	public void doGet( HttpServletRequest req, HttpServletResponse response ) throws IOException, ServletException
	{
		final String bbProj = req.getParameter( "bbProj" );
		final String bbRepo = req.getParameter( "bbRepo" );
		ApplicationLink link = applicationLinkService.getPrimaryApplicationLink( StashApplicationType.class );

		ApplicationLinkRequestFactory fac = link.createAuthenticatedRequestFactory();
		response.setContentType( "text/plain;charset=UTF-8" );
		PrintWriter pw = response.getWriter();

		String pathInfo = req.getPathInfo();
		String[] components = pathInfo.split( "/" );
		String ticket = req.getParameter( "jira" );
		if ( ticket == null && components.length > 2 )
		{
			String prev2 = components[ 0 ];
			String prev1 = components[ 1 ];
			for ( int i = 2; i < components.length; i++ )
			{
				String s = components[ i ];
				if ( "browse".equals( prev1 ) )
				{
					ticket = s;
					break;
				}
				else if ( "issues".equals( prev1 ) )
				{
					ticket = s;
					break;
				}
				else
				{
					prev2 = prev1;
					prev1 = s;
				}
			}
		}

		try
		{

			if ( DEBUG_MODE && "TKT-1".equals( ticket ) )
			{
				String currentRepo = "\"currentRepo\":{\"repo\":\"mdi\",\"proj\":\"MDI\",\"hits\":742},\n";
				if ( bbProj != null && bbRepo != null )
				{
					currentRepo = "\"currentRepo\":{\"repo\":\"" + bbRepo + "\",\"proj\":\"" + bbProj + "\",\"hits\":1},\n";
				}

				pw.write( "\n{\n\"jira\":\"http://localhost:2990/jira\",\n" +
						"\"bitbucket\": \"http://localhost:7990/bitbucket\",\n" +
						"\"err\":\"742 commits found that match [TKT-1]. Graph limited to first 10.\",\n" +
						"\"repos\":[{\"repo\":\"mdi\",\"proj\":\"MDI\",\"hits\":742}\n" +
						",{\"repo\":\"rat\",\"proj\":\"MDI\",\"hits\":34}\n" +
						",{\"repo\":\"aft-warehouse\",\"proj\":\"AFT\",\"hits\":1}\n" +
						",{\"repo\":\"archived_do_not_use.psa-real-time\",\"proj\":\"PSAREALTIME\",\"hits\":5}\n" +
						",{\"repo\":\"boi_client\",\"proj\":\"MTS\",\"hits\":2}\n" +
						",{\"repo\":\"boi_server\",\"proj\":\"MTS\",\"hits\":9}\n" +
						",{\"repo\":\"c1-common\",\"proj\":\"COM\",\"hits\":2}\n" +
						",{\"repo\":\"c1-devkit\",\"proj\":\"INNOVATION\",\"hits\":7}\n" +
						",{\"repo\":\"c1-element-llim\",\"proj\":\"OICU\",\"hits\":3}\n" +
						",{\"repo\":\"c1-element-micheal\",\"proj\":\"OICU\",\"hits\":3}\n" +
						",{\"repo\":\"c1-shared\",\"proj\":\"MDI\",\"hits\":2}\n" +
						",{\"repo\":\"c1-test-form\",\"proj\":\"OICU\",\"hits\":1}\n" +
						",{\"repo\":\"camelutil\",\"proj\":\"PAY\",\"hits\":3}\n" +
						",{\"repo\":\"change-contact-information-form\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"contact-us-form\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"contact-us-form-llim\",\"proj\":\"OICU\",\"hits\":1}\n" +
						",{\"repo\":\"contact-us-form-llim2\",\"proj\":\"OICU\",\"hits\":1}\n" +
						",{\"repo\":\"contact-us-lani\",\"proj\":\"OICU\",\"hits\":2}\n" +
						",{\"repo\":\"db_schema\",\"proj\":\"MTS\",\"hits\":1}\n" +
						",{\"repo\":\"emt\",\"proj\":\"PSAREALTIME\",\"hits\":6}\n" +
						",{\"repo\":\"eswitch\",\"proj\":\"PAY\",\"hits\":1}\n" +
						",{\"repo\":\"form-datepicker-sample\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"hk-text-area-input\",\"proj\":\"OICU\",\"hits\":3}\n" +
						",{\"repo\":\"init-rem\",\"proj\":\"OICU\",\"hits\":3}\n" +
						",{\"repo\":\"iop\",\"proj\":\"PSAREALTIME\",\"hits\":6}\n" +
						",{\"repo\":\"iso-shared\",\"proj\":\"MDI\",\"hits\":4}\n" +
						",{\"repo\":\"legacyandroidmdimobileapp\",\"proj\":\"MAP\",\"hits\":2}\n" +
						",{\"repo\":\"legacyiphonemdimobileapp\",\"proj\":\"MAP\",\"hits\":1}\n" +
						",{\"repo\":\"logdirect\",\"proj\":\"MDI\",\"hits\":16}\n" +
						",{\"repo\":\"mdi-core-components\",\"proj\":\"OIWA\",\"hits\":6}\n" +
						",{\"repo\":\"mdimobileapp\",\"proj\":\"MAP\",\"hits\":2}\n" +
						",{\"repo\":\"mdimobileplugins\",\"proj\":\"MAP\",\"hits\":2}\n" +
						",{\"repo\":\"member-info\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"member-name\",\"proj\":\"OICU\",\"hits\":1}\n" +
						",{\"repo\":\"oir\",\"proj\":\"OIR\",\"hits\":3}\n" +
						",{\"repo\":\"psa\",\"proj\":\"PSAREALTIME\",\"hits\":1}\n" +
						",{\"repo\":\"psa-real-time-egs\",\"proj\":\"PSAREALTIME\",\"hits\":1}\n" +
						",{\"repo\":\"psa-real-time-xgs\",\"proj\":\"PSAREALTIME\",\"hits\":1}\n" +
						",{\"repo\":\"psa-reporting\",\"proj\":\"PSAREALTIME\",\"hits\":1}\n" +
						",{\"repo\":\"rcs\",\"proj\":\"MDI\",\"hits\":14}\n" +
						",{\"repo\":\"shared\",\"proj\":\"PSAREALTIME\",\"hits\":6}\n" +
						",{\"repo\":\"srd\",\"proj\":\"PAY\",\"hits\":1}\n" +
						",{\"repo\":\"twoss-common-lib\",\"proj\":\"TWOSS\",\"hits\":1}\n" +
						",{\"repo\":\"twoss-idp\",\"proj\":\"TWOSS\",\"hits\":9}\n" +
						",{\"repo\":\"twoss-lib\",\"proj\":\"TWOSS\",\"hits\":3}\n" +
						",{\"repo\":\"twoss-ws\",\"proj\":\"TWOSS\",\"hits\":7}\n" +
						",{\"repo\":\"umpire\",\"proj\":\"MDI\",\"hits\":4}\n" +
						",{\"repo\":\"webmf\",\"proj\":\"AFT\",\"hits\":2}\n" +
						",{\"repo\":\"webparser\",\"proj\":\"MDI\",\"hits\":10}\n" +
						",{\"repo\":\"webparser-lib\",\"proj\":\"MDI\",\"hits\":19}\n" +
						",{\"repo\":\"x9fp\",\"proj\":\"PAY\",\"hits\":6}\n" +
						",{\"repo\":\"xml-data\",\"proj\":\"MDI\",\"hits\":1}\n" +
						",{\"repo\":\"z-boolean\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-date\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-element\",\"proj\":\"OIEX\",\"hits\":8}\n" +
						",{\"repo\":\"z-email\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-form\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-form-address\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-checkbox\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-choose-atleast\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-component-behavior\",\"proj\":\"OIEX\",\"hits\":4}\n" +
						",{\"repo\":\"z-form-dropdown\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-email-input\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-error-textarea\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-numeric-input\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-form-page\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-phone-input\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-postal-code-input\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-form-recaptcha\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-section\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-segmented-control\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-slider-input\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-submit-button\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-form-text-area-input\",\"proj\":\"OIEX\",\"hits\":4}\n" +
						",{\"repo\":\"z-form-text-input\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-form-zip-code-input\",\"proj\":\"OIEX\",\"hits\":4}\n" +
						",{\"repo\":\"z-member-address\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-address-city\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-member-address-country\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-member-address-postal-code\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-member-address-province-state\",\"proj\":\"OIEX\",\"hits\":3}\n" +
						",{\"repo\":\"z-member-address-street\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-member-birth-date\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-email\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-member-first-name\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-job-title\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-last-name\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-name\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-service\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-sex\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-member-telephone\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-postal-code\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-property\",\"proj\":\"OIEX\",\"hits\":2}\n" +
						",{\"repo\":\"z-telephone\",\"proj\":\"OIEX\",\"hits\":1}\n" +
						",{\"repo\":\"z-text-property\",\"proj\":\"OIEX\",\"hits\":1}]," +
						currentRepo +
						"\"tips\":[{\"id\":\"04f104890582e063b4150196e292416cdeb33f07\", \"refs\":\"(tag: 515.2.628)\"},{\"id\":\"3a0dbffce75b50937d07840de76a123b4b528b34\", \"refs\":\"(HEAD -> master)\"},{\"id\":\"4e3fc7214afcf1e72b72d63b9f976d118e607422\", \"refs\":\"(tag: 514.9.624, release/624)\"},{\"id\":\"58b50c48019c27cb48a16a09ce514fd0012cf5c5\", \"refs\":\"(release/616)\"},{\"id\":\"a0af6781fe7d36466cefddf4d95154d7873f6fea\", \"refs\":\"(tag: 511.2.620, release/620)\"}],\"hits\":[\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc\",\"f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"968713f096cc7d55c41619465da0b0bae67c7e79\"],"
						+

						"\"lines\":[[\"1466738242\",\"9 weeks ago\",\"5b1bb2d2aeb4831be1758791756610514c94595a\",\"f48a80b318b512832228536d599817870c2327ab\",\"HEAD -> master\",\"build-agent\",\"Merge branch '5.10.x' of bitbucket.org:atlassian/aui\"],\n"
						+
						"[\"1466400388\",\"9 weeks ago\",\"c1ba15ba1daed078fcba8ae9600b384c3fb94669\",\"e3e380b0b0f83fccdeee9072a5ead597d6d6f67f\",\"tag: 0.0.0-5-10-0-SNAPSHOT-003-epic-AUI-4224-skate-responsive-header\",\"build-agent\",\"0.0.0-5-10-0-SNAPSHOT-003-epic-AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1466393061\",\"9 weeks ago\",\"e3e380b0b0f83fccdeee9072a5ead597d6d6f67f\",\"8535751e4890bde895e327cd52b0c336fb4946d5 f48a80b318b512832228536d599817870c2327ab\",\"\",\"benwong\",\"Merge remote-tracking branch 'origin/5.10.x' into epic/AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1465972664\",\"2 months ago\",\"427be59da7b7323591364a1efe3bb681fb3cd4b9\",\"8535751e4890bde895e327cd52b0c336fb4946d5\",\"tag: 0.0.0-5-10-0-SNAPSHOT-002-epic-AUI-4224-skate-responsive-header\",\"build-agent\",\"0.0.0-5-10-0-SNAPSHOT-002-epic-AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1465964211\",\"2 months ago\",\"8535751e4890bde895e327cd52b0c336fb4946d5\",\"3e60001ef0bcd551f79a863ac82023a980f2b6e1 f48a80b318b512832228536d599817870c2327ab\",\"\",\"benwong\",\"Merge remote-tracking branch 'origin/5.10.x' into epic/AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1465953204\",\"2 months ago\",\"3e60001ef0bcd551f79a863ac82023a980f2b6e1\",\"58576c88ece8672dce5e7bc03bd920ea24f3467f\",\"\",\"benwong\",\"Merge branch 'epic/AUI-4224-skate-responsive-header' of bitbucket.org:atlassian/aui into epic/AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1465952992\",\"2 months ago\",\"58576c88ece8672dce5e7bc03bd920ea24f3467f\",\"f48a80b318b512832228536d599817870c2327ab\",\"\",\"benwong\",\"Merge remote-tracking branch 'origin/DP-782-perform-510-release' into epic/AUI-4224-skate-responsive-header\",true],\n"
						+
						"[\"1465364303\",\"3 months ago\",\"f48a80b318b512832228536d599817870c2327ab\",\"3da77debbb0ebae6ee49d458391eb4ccf9cf879a f43477387a2b5d518bc862dffc721a9db55ac2f0\",\"\",\"benwong\",\"Merged in DP-782-perform-510-release (pull request #1876)\"]\n"
						+
						"]\n" +
						"}" );

				/*
						"\"lines\":[[\"1471035368\",\"9 days ago\",\"3a0dbffce75b50937d07840de76a123b4b528b34\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"HEAD -> master\",\"\",\"\"],\n"
						+
						"[\"1469834497\",\"3 weeks ago\",\"4e3fc7214afcf1e72b72d63b9f976d118e607422\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"tag: 3.1.3.rc1, release/3.1.x\",\"\",\"\"],\n"
						+
						"[\"1465850176\",\"10 weeks ago\",\"58b50c48019c27cb48a16a09ce514fd0012cf5c5\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"release/2.x\",\"\",\"\"],\n"
						+
						"[\"1465487872\",\"2 months ago\",\"a0af6781fe7d36466cefddf4d95154d7873f6fea\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"tag: 1.8.8-SNAPSHOT\",\"\",\"\"],\n"
						+
						"[\"1460128709\",\"5 months ago\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460126987\",\"5 months ago\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460126338\",\"5 months ago\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc\",\"968713f096cc7d55c41619465da0b0bae67c7e79\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460122146\",\"5 months ago\",\"968713f096cc7d55c41619465da0b0bae67c7e79\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"\",\"\",\"\",true],\n"
						+
						"[\"1459538972\",\"5 months ago\",\"f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"\",\"\",\"\",true],\n"
						+
						"[\"1459532691\",\"5 months ago\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"b16d2073c4465881a276015a1df5b2ded1ff3764\",\"\",\"\",\"\"]\n"
						+
						"]\n" +
						"}" );
						*/
			}
			else
			{
				String url = "plugins/servlet/bb_dag/?bb=Hpdhrt&all=y&fromJira=y&grep=true&jira=" + ticket;
				if ( bbProj != null )
				{
					url += "&bbProj=" + bbProj;
				}
				if ( bbRepo != null )
				{
					url += "&bbRepo=" + bbRepo;
				}
				ApplicationLinkRequest linkReq = fac.createRequest( Request.MethodType.GET, url );
				System.out.println( "SENDING: " + url );
				String result = linkReq.execute();
				pw.write( result );
			}
		}
		catch ( CredentialsRequiredException re )
		{

			pw.write( "Need to Auth!\n\n\n" + link + "\n\n\n" + re + "\n\n\n" );
			re.printStackTrace( pw );

		}
		catch ( ResponseException re )
		{

			pw.write( "Problem!\n\n\n" + link + "\n\n\n" + re + "\n\n\n" );
			re.printStackTrace( pw );
		}
		pw.close();

	}
}