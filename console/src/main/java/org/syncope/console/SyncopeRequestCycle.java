/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.console;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Response;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.syncope.console.pages.ErrorPage;

/**
 * SyncopeRequestCycle.
 */
public class SyncopeRequestCycle extends WebRequestCycle {

    /**
     * SyncopeRequestCycle constructor.
     *
     * @param application the web application
     * @param request the web request
     * @param response the web response
     */
    public SyncopeRequestCycle(final WebApplication application,
            final WebRequest request, final Response response) {

        super(application, request, response);
    }

    @Override
    public final Page onRuntimeException(final Page cause,
            final RuntimeException e) {

        Page errorPage;
        PageParameters errorParameters = new PageParameters();
        errorParameters.add("errorTitle",
                new StringResourceModel("alert", null).getString());

        if (e instanceof UnauthorizedInstantiationException) {
            errorParameters.add("errorMessage", new StringResourceModel(
                    "unauthorizedInstantiationException", null).getString());

            errorPage = new ErrorPage(errorParameters);
        } else if (e instanceof HttpClientErrorException) {
            errorParameters.add("errorMessage",
                    new StringResourceModel("httpClientException", null).
                    getString());

            errorPage = new ErrorPage(errorParameters);
        } else if (e instanceof PageExpiredException
                || !((SyncopeSession) getSession()).isAuthenticated()) {

            errorParameters.add("errorMessage",
                    new StringResourceModel("pageExpiredException", null).
                    getString());

            errorPage = new ErrorPage(errorParameters);
        } else if (e.getCause() != null && e.getCause().getCause() != null
                && e.getCause().getCause() instanceof RestClientException) {

            errorParameters.add("errorMessage",
                    new StringResourceModel("restClientException", null).
                    getString());

            errorPage = new ErrorPage(errorParameters);
        } else {
            // redirect to default Wicket error page
            errorPage = new ExceptionErrorPage(e, cause);
        }

        return errorPage;
    }
}
