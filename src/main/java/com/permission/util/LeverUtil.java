package com.permission.util;

import org.apache.commons.lang3.StringUtils;

public class LeverUtil {
    public final static String SEPARATOR = ".";

    public final static String ROOT = "0";

    /**
     * 0
     * 0.1
     * 0.1.2
     * 0.1.2
     * 0.4
     * @param parentLeverl
     * @param parentId
     * @return
     */
    public static String calculateLever(String parentLeverl,int parentId){
        if(StringUtils.isBlank(parentLeverl)){
            return ROOT;
        }else {
            return StringUtils.join(parentLeverl,SEPARATOR,parentId);
        }
    }

}
