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
				pw.write( "\n" +
						"{\n" +
						"\"jira\":\"http://localhost:2990/jira\",\n" +
						"\"repos\":[\"proj1/repo1\", \"proj1/repo2\", \"proj2/repo3\"],\n" +
						"\"currentRepo\":\"proj1/repo2\",\n" +
						"\"lines\":[[\"1470692999\",\"43 minutes ago\",\"132285b9a74fecfcda628822ef1e921eca32002a\",\"fbccfa1ca1f1da3b336c566bd05e884b7341ffc9\",\"CPEI-71\",\"\",\"\",true],\n"
						+
						"[\"1470692149\",\"57 minutes ago\",\"8f83c01423ccb199d70b5fdbe0e8ddd70adb2229\",\"2fa02264d6fa6480785e72f01da6be22fa3770af\",\"HEAD -> master, OAO-761\",\"\",\"\"],\n"
						+
						"[\"1470683388\",\"3 hours ago\",\"fbccfa1ca1f1da3b336c566bd05e884b7341ffc9\",\"294bbf40693e6e5b16dce40f775812a7e4fe19c3\",\"\",\"\",\"\",true],\n"
						+
						"[\"1470681880\",\"4 hours ago\",\"294bbf40693e6e5b16dce40f775812a7e4fe19c3\",\"c642de2670212f424d1f20efa92e46a560da63e6\",\"\",\"\",\"\",true],\n"
						+
						"[\"1470681880\",\"4 hours ago\",\"c642de2670212f424d1f20efa92e46a560da63e6\",\"96d13a61e6667eb4a1a2c1927c387a2479c0fff9\",\"\",\"\",\"\",true],\n"
						+
						"[\"1470681880\",\"4 hours ago\",\"96d13a61e6667eb4a1a2c1927c387a2479c0fff9\",\"2fa02264d6fa6480785e72f01da6be22fa3770af\",\"\",\"\",\"\",true],\n"
						+
						"[\"1470418889\",\"3 days ago\",\"2fa02264d6fa6480785e72f01da6be22fa3770af\",\"88fc434302df5d15a886d30f69282a0471fa1ef8\",\"tag: 517.20160805.630, MRR-12888, DAILY.prev\",\"\",\"\"]\n"
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