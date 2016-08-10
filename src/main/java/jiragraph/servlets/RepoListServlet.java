package jiragraph.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;

@ExportAsService( {RepoListServlet.class} )
@Named( "bb.jira.RepoListServlet" )

public class RepoListServlet extends HttpServlet
{

    @ComponentImport
    private final ApplicationLinkService applicationLinkService;

    @ComponentImport
    private final ProjectManager projectManager;

    @Inject
    public RepoListServlet( ApplicationLinkService applicationLinkService, ProjectManager projectManager )
    {
        this.applicationLinkService = applicationLinkService;
        this.projectManager = projectManager;
    }

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse response ) throws IOException, ServletException {
        String currentRepo = req.getParameter("currentRepo");
        currentRepo = currentRepo != null ? currentRepo.trim() : "";

        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(BitbucketApplicationType.class);

        ApplicationLinkRequestFactory fac = link.createAuthenticatedRequestFactory();
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        String pathInfo = req.getPathInfo();
        String[] components = pathInfo.split("/");
        String ticket = null;
        Project myProject = null;

        if (components.length > 2) {
            String prev2 = components[0];
            String prev1 = components[1];
            for (int i = 2; i < components.length; i++) {
                String s = components[i];
                if ("jira".equals(prev2) && "browse".equals(prev1)) {
                    ticket = s;
                    break;
                } else {
                    prev2 = prev1;
                    prev1 = s;
                }
            }
        }
        if (ticket != null) {
            String[] tempTicket = ticket.split("-");

            List<Project> projects = projectManager.getProjects();

            for (Project p : projects) {
                if (tempTicket[0].equals(p.getKey())) {
                    myProject = p;
                }
            }
        }

        if (myProject != null) {
        try {
            ApplicationLinkRequest linkReq = fac.createRequest(Request.MethodType.GET,
                    "plugins/servlet/bb_dag/?all=y&grep=true&jira=" + ticket);
            String result = linkReq.execute();

            JSONObject myObject = new JSONObject(result);
            System.out.println(result);
            JSONArray myArray = myObject.getJSONArray("repos");
            ArrayList<String> reposToPass = new ArrayList<String>();
            for (int q = 0; q < myArray.length(); q++) {
                String[] projAndRepo = myArray.get(q).toString().split("/");
                if(projAndRepo[0].equals(myProject.getName())) {
                    reposToPass.add(projAndRepo[1]);
                }
            }
            String repoSet = "[\"";
            for (int i = 0; i < reposToPass.size(); i++) {
                if (i == reposToPass.size() - 1) {
                    repoSet = repoSet + reposToPass.get(i) + "]\n";
                } else {
                    repoSet = repoSet + reposToPass.get(i) + ", ";
                }

            }
            pw.write("\n" +
                            "{\n" +
                            "\"repos\":" + repoSet +
                            "}\n");
        } catch(Exception e) {
            e.printStackTrace();
        }
        }
    }
}
