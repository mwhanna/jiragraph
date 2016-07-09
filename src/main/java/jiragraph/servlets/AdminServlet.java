package jiragraph.servlets;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mhanna on 2016-07-09.
 */
@ExportAsService( {AdminServlet.class} )
@Named( "JiragraphAdminServlet" )
public class AdminServlet extends HttpServlet {
    private static final String PLUGIN_STORAGE_KEY = "jiragraph.adminui";

    @ComponentImport
    private final TemplateRenderer renderer;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public AdminServlet( TemplateRenderer renderer, PluginSettingsFactory pluginSettingsFactory )
    {
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        Map<String, Object> context = new HashMap<String, Object>();

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch1") == null ||
                pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch1").equals("")) {
            String noBr1 = "Empty";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".branch1", noBr1);
        }

        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch2") == null ||
                pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch2").equals("")) {
            String noBr2 = "Empty";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".branch2", noBr2);
        }

        if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch3") == null ||
                pluginSettings.get(PLUGIN_STORAGE_KEY + ".branch3").equals("")) {
            String noBr3 = "Empty";
            pluginSettings.put(PLUGIN_STORAGE_KEY + ".branch3", noBr3);
        }

        context.put( "branch1", pluginSettings.get( PLUGIN_STORAGE_KEY + ".branch1" ) );
        context.put( "branch2", pluginSettings.get( PLUGIN_STORAGE_KEY + ".branch2" ) );
        context.put( "branch3", pluginSettings.get( PLUGIN_STORAGE_KEY + ".branch3" ) );

        response.setContentType( "text/html;charset=utf-8" );
        renderer.render( "admin.vm", context, response.getWriter() );
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse response )
            throws ServletException, IOException
    {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put( PLUGIN_STORAGE_KEY + ".branch1", req.getParameter( "branch1" ) );
        pluginSettings.put( PLUGIN_STORAGE_KEY + ".branch2", req.getParameter( "branch2" ) );
        pluginSettings.put( PLUGIN_STORAGE_KEY + ".branch3", req.getParameter( "branch3" ) );
        response.sendRedirect( "admin" );
    }

}
