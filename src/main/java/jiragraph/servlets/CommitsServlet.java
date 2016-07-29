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
	ApplicationLinkService applicationLinkService;

	@Inject
	public CommitsServlet( ApplicationLinkService applicationLinkService )
	{
		this.applicationLinkService = applicationLinkService;
	}

	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException
	{

		ApplicationLink link = applicationLinkService.getPrimaryApplicationLink( BitbucketApplicationType.class );

		ApplicationLinkRequestFactory fac = link.createAuthenticatedRequestFactory();
		response.setContentType( "text/plain;charset=UTF-8" );
		PrintWriter pw = response.getWriter();

		try
		{
			ApplicationLinkRequest req = fac.createRequest( Request.MethodType.GET,
					"plugins/servlet/bb_dag/bitbucket/projects/PROJECT_1/repos/rep_1/commits/HEAD?n=3" );
			String result = req.execute();
			pw.write( result );

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