package com.jslink.wc.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {
    public static final int PRINT_RECCTABLE_EXPERIENCE_ROW = 3;
    public static final int PRINT_RECCTABLE_PRIZE_ROW = 3;
    public static final int PRINT_RECCTABLE_SUBSIDIZE_ROW = 3;

    public static final byte WORKS_STATUS_DRAFT = 0;
    public static final byte WORKS_STATUS_SUBMIT = 1;

    public static final byte ACCOUNT_TYPE_ADMIN = 1; //管理员
    public static final byte ACCOUNT_TYPE_UNIT1 = 2; //推荐单位
    public static final byte ACCOUNT_TYPE_UNIT2 = 3; //申报单位

    public static final String WORK_TYPE = "work_type";
    public static final int WORKS_TYPE_TEXT_SCI = 11;//科普文章
    public static final int WORKS_TYPE_TEXT_CARTOON = 12;//漫画
    public static final int WORKS_TYPE_TEXT_POSTER = 13;//
    public static final int WORKS_TYPE_TEXT_OTHERS = 14;//
    public static final int WORKS_TYPE_AUDIO_SPECIAL = 21;//
    public static final int WORKS_TYPE_AUDIO_BROADCAST = 22;//
    public static final int WORKS_TYPE_AUDIO_BOOK = 23;//
    public static final int WORKS_TYPE_AUDIO_OTHERS = 24;//
    public static final int WORKS_TYPE_VIDEO_SINGLE_SHORT = 31;//
    public static final int WORKS_TYPE_VIDEO_SINGLE_LONG = 32;//
    public static final int WORKS_TYPE_VIDEO_MULTI_SHORT = 33;//
    public static final int WORKS_TYPE_VIDEO_MULTI_LONG = 34;//
    public static final int WORKS_TYPE_TEXTBOOK = 41;//

    public static final String DICT_TYPE_RATE = "rate";

    public static final String WORKS_COMMENT_TYPE_COMMENT = "comment";

    public static final DateFormat DFYMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final DateFormat DFYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat DFHMS = new SimpleDateFormat("HH:mm:ss");
    public static final DateFormat DFYMD = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat DFWEEK = new SimpleDateFormat("EEE");
    public static final DateFormat DFYMDHMS_2 = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String PUBLISH_DOMAIN = "http://workscollect.shbxjk.cn";
    public static final String DIRECTORY_WORKS_FILES = "WorksFiles";
    public static final String DIRECTORY_RECC_FORM = "WorksReccForm";

    public static final String ACCOUNT_PERMISSION_BRAND = "brand";
    public static final String ACCOUNT_PERMISSION_PEOPLE = "people";
    public static final String ACCOUNT_PERMISSION_WORKS = "works";
    public static final String ACCOUNT_PERMISSION_MGMTORG = "mgmtorg";
    public static final String ACCOUNT_PERMISSION_MGMTINDIVIDUAL = "mgmtindividual";

    public static final int PEOPLE_APPLYTYPE_GONGXIAN = 1;
    public static final int PEOPLE_APPLYTYPE_JIECU = 2;
    public static final int PEOPLE_APPLYTYPE_XINRUI = 3;

    public static final String POPSCI_APPLYTYPE ="popsci_applytype";
    public static final int POPSCI_APPLYTYPE_ORG = 1; //组织机构
    public static final int POPSCI_APPLYTYPE_INDIVIDUAL = 2; //个人


    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    public static final String DEGREE = "degree";
    public static final String DEGREE_PRIMARY = "小学";
    public static final String DEGREE_SECONDARY = "初中";
    public static final String DEGREE_HIGHSCHOOL = "高中";
    public static final String DEGREE_COLLEGE = "大专";
    public static final String DEGREE_BACHELOR = "大学本科";
    public static final String DEGREE_MASTER = "研究生";
    public static final String DEGREE_DOCTOR = "博士";
}
