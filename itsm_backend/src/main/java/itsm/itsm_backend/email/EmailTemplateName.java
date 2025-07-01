package itsm.itsm_backend.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account"),
    TICKET_NOT_ASSIGNED("ticket_not_assigned")
    ;


    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
