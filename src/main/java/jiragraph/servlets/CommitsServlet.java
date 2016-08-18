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
import com.atlassian.applinks.api.application.bitbucket.BitbucketApplicationType;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;

@ExportAsService( {CommitsServlet.class} )
@Named( "bb.jira.CommitsServlet" )

public class CommitsServlet extends HttpServlet
{

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

		ApplicationLink link = applicationLinkService.getPrimaryApplicationLink( BitbucketApplicationType.class );

		ApplicationLinkRequestFactory fac = link.createAuthenticatedRequestFactory();
		response.setContentType( "text/plain;charset=UTF-8" );
		PrintWriter pw = response.getWriter();

		String pathInfo = req.getPathInfo();
		String[] components = pathInfo.split( "/" );
		String ticket = null;

		if ( components.length > 2 )
		{
			String prev2 = components[ 0 ];
			String prev1 = components[ 1 ];
			for ( int i = 2; i < components.length; i++ )
			{
				String s = components[ i ];
				if ( "jira".equals( prev2 ) && "browse".equals( prev1 ) )
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

			if ( "TKT-1".equals( ticket ) )
			{
				pw.write( "\n{\n\"jira\":\"http://localhost:2990/jira\",\n" +
						"\"err\":\"742 commits found that match [TKT-1].  Only analyzing first 10.\",\n" +
						"\"repos\":[{\"repo\":\"rep_1\",\"proj\":\"PROJECT_1\",\"hits\":1}\n" +
						",{\"repo\":\"mdi\",\"proj\":\"PROJECT_1\",\"hits\":742}\n" +
						"]," +
						"\"currentRepo\":{\"repo\":\"mdi\",\"proj\":\"PROJECT_1\",\"hits\":742},\n" +
						"\"tips\":[{\"id\":\"04f104890582e063b4150196e292416cdeb33f07\", \"refs\":\"(tag: 515.2.628)\"},{\"id\":\"3a0dbffce75b50937d07840de76a123b4b528b34\", \"refs\":\"(HEAD -> master)\"},{\"id\":\"4e3fc7214afcf1e72b72d63b9f976d118e607422\", \"refs\":\"(tag: 514.9.624, release/624, release.624/MRR-12789)\"},{\"id\":\"58b50c48019c27cb48a16a09ce514fd0012cf5c5\", \"refs\":\"(release/616, release.616/CST-72)\"},{\"id\":\"a0af6781fe7d36466cefddf4d95154d7873f6fea\", \"refs\":\"(tag: 511.2.620, release/620, release.620/CST-78)\"}],\"hits\":[\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc\",\"f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"968713f096cc7d55c41619465da0b0bae67c7e79\"],\"lines\":[[\"1471035368\",\"6 days ago\",\"3a0dbffce75b50937d07840de76a123b4b528b34\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"HEAD -> master\",\"\",\"\"],\n"
						+
						"[\"1470786156\",\"9 days ago\",\"04f104890582e063b4150196e292416cdeb33f07\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"tag: 515.2.628\",\"\",\"\"],\n"
						+
						"[\"1469834497\",\"3 weeks ago\",\"4e3fc7214afcf1e72b72d63b9f976d118e607422\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"tag: 514.9.624, release/624, release.624/MRR-12789\",\"\",\"\"],\n"
						+
						"[\"1465850176\",\"9 weeks ago\",\"58b50c48019c27cb48a16a09ce514fd0012cf5c5\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"release/616, release.616/CST-72\",\"\",\"\"],\n"
						+
						"[\"1465487872\",\"2 months ago\",\"a0af6781fe7d36466cefddf4d95154d7873f6fea\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"tag: 511.2.620, release/620, release.620/CST-78\",\"\",\"\"],\n"
						+
						"[\"1460128709\",\"4 months ago\",\"e5a79b0b398882cf85cd247dbac3d96c0d352a53\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460126987\",\"4 months ago\",\"75b03cd6e8f666e9328c7ca7d36adf54b0e73a4d\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460126338\",\"4 months ago\",\"0089e39b366cebe9a411955a8e7f50f9edd09dbc\",\"968713f096cc7d55c41619465da0b0bae67c7e79\",\"\",\"\",\"\",true],\n"
						+
						"[\"1460122146\",\"4 months ago\",\"968713f096cc7d55c41619465da0b0bae67c7e79\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"\",\"\",\"\",true],\n"
						+
						"[\"1459538972\",\"5 months ago\",\"f2bde6f4d3b32b4f38e1280f7822dd192c61957d\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"\",\"\",\"\",true],\n"
						+
						"[\"1459532691\",\"5 months ago\",\"4dcafbaa6c832e299ad415eb2f24f4b45098fa03\",\"b16d2073c4465881a276015a1df5b2ded1ff3764\",\"tag: 507.20160401.612\",\"\",\"\"]\n"
						+
						"]\n" +
						"}\n" );
			}
			else
			{
				ApplicationLinkRequest linkReq = fac.createRequest( Request.MethodType.GET,
						"plugins/servlet/bb_dag/?all=y&grep=true&jira=" + ticket );
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