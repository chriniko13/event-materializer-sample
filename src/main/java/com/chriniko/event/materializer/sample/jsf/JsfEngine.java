package com.chriniko.event.materializer.sample.jsf;

import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Component
public class JsfEngine {

    public void displayMessage(String message) {

        FacesMessage msg = construct(message);

        FacesContext.getCurrentInstance()
                .addMessage(null, msg);
    }

    public void displayWarnMessage(String message) {

        FacesMessage msg = construct(message);
        msg.setSeverity(FacesMessage.SEVERITY_WARN);

        FacesContext.getCurrentInstance()
                .addMessage(null, msg);
    }

    public void displayFatalMessage(String message) {

        FacesMessage msg = construct(message);
        msg.setSeverity(FacesMessage.SEVERITY_FATAL);

        FacesContext.getCurrentInstance()
                .addMessage(null, msg);
    }


    private FacesMessage construct(String message) {
        FacesMessage msg = new FacesMessage();
        msg.setSummary(message);
        return msg;
    }
}
