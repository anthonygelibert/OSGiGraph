/******************************************************************************
 * Copyright (c) 2011, Anthony GELIBERT                                       *
 * All rights reserved.                                                       *
 *                                                                            *
 * Redistribution and use in source and binary forms, with or without         *
 * modification, are permitted provided that the following conditions are met:*
 *                                                                            *
 *     * Redistributions of source code must retain the above copyright       *
 *     notice, this list of conditions and the following disclaimer.          *
 *                                                                            *
 *     * Redistributions in binary form must reproduce the above copyright    *
 *     notice, this list of conditions and the following disclaimer in the    *
 *     documentation and/or other materials provided with the distribution.   *
 *                                                                            *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"*
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,     *
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,      *
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY        *
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT               *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE      *
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.       *
 ******************************************************************************/

package fr.ag.osgi.tools.graph.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Simple bundle declaring a new WebConsole plugin displaying the dependencies
 * graph as an HTML array.
 *
 * @author Anthony Gelibert
 * @version 1.0.0
 */
@Component(name = "PrintGraphHTMLArrayComponent")
@Provides(specifications = SimpleWebConsolePlugin.class)
@Instantiate(name = "PrintGraphHTMLArrayInstance")
@SuppressWarnings("unused")
public final class PrintGraphHTMLArray extends SimpleWebConsolePlugin
{
    private static final long serialVersionUID = 1L;

    /** Webpage label. */
    private static final String PAGE_LABEL = "graph";
    /** Webpage title. */
    private static final String PAGE_TITLE = "OSGi Bundle HTML Graph";
    /** Webpage css. */
    private static final String[] PAGE_CSS = new String[0];

    /** Framework Bundle Context. */
    private final BundleContext m_bundleContext;

    /** PackageAdmin permits to obtain the current mapping. */
    @Requires
    private PackageAdmin m_packageAdmin;

    /**
     * Constructor with the BundleContext.
     *
     * @param bundleContext Framework Bundle Context.
     */
    public PrintGraphHTMLArray(final BundleContext bundleContext)
    {
        super(PAGE_LABEL, PAGE_TITLE, PAGE_CSS);
        m_bundleContext = bundleContext;
    }

    /** "Start" */
    @Validate
    public void start()
    {
        register(m_bundleContext);
    }

    /** "Stop" */
    @Invalidate
    public void stop()
    {
        unregister();
    }

    @Override
    protected void renderContent(final HttpServletRequest req, final HttpServletResponse res) throws IOException
    {
        final Set<Bundle> bundleSet = new TreeSet<Bundle>();
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("<center><table border>");
        for (final Bundle bundle : m_bundleContext.getBundles())
        {
            if (m_packageAdmin.getExportedPackages(bundle) != null)
            {
                bundleSet.clear();
                for (final ExportedPackage ep : m_packageAdmin.getExportedPackages(bundle))
                {
                    bundleSet.addAll(Arrays.asList(ep.getImportingBundles()));
                }
                final Bundle[] importers = bundleSet.toArray(new Bundle[bundleSet.size()]);

                if (importers.length > 0)
                {
                    sb.append("<tr><td align=\"center\" valign=\"middle\" rowspan=");
                    sb.append(importers.length).append("><a href=\"bundles/");
                    sb.append(bundle.getBundleId()).append("\">");
                    sb.append(bundle.getSymbolicName());
                    sb.append("</a></td><td valign=\"middle\" align=\"center\"><a href=\"bundles/");
                    sb.append(importers[0].getBundleId()).append("\">");
                    sb.append(importers[0].getSymbolicName());
                    sb.append("</a></td></tr>");
                    for (int i = 1; i < importers.length; i++)
                    {
                        sb.append("<tr><td valign=\"middle\" align=\"center\"><a href=\"bundles/");
                        sb.append(importers[i].getBundleId());
                        sb.append("\">");
                        sb.append(importers[i].getSymbolicName());
                        sb.append("</a></td></tr>");

                    }
                }
            }
        }
        sb.append("</table border></center>");
        res.getWriter().print(sb.toString());
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("PrintGraphHTMLArray");
        sb.append("{m_bundleContext=").append(m_bundleContext);
        sb.append(", m_packageAdmin=").append(m_packageAdmin);
        sb.append('}');
        return sb.toString();
    }
}

