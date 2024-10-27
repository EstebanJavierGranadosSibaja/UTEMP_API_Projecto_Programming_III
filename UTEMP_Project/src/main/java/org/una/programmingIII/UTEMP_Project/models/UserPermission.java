package org.una.programmingIII.UTEMP_Project.models;

public enum UserPermission {

    //permisos estudiante
    INSCRIBE_TO_CURSE,
    VIEW_ASSIGNMENTS,
    DELETE_ASSIGNMENT,
    SUBMIT_ASSIGNMENT,

    //permisos de profesor
    MANAGER_CURSE,
    GRADE_ASSIGNMENTS,
    CREATE_ASSIGNMENT,
    EDIT_GRADE,

    EDIT_ASSIGNMENT,

    //estudiante y profesor
    VIEW_GRADES,
    NOTIFICATION,
    USER_INFO,
    LOAD_FILE,

    //administrador// empleado
    //university
    MANAGE_UNIVERSITY,
    MANAGE_USERS,

    //TODO?
    VIEW_REPORTS
    //for all

}
