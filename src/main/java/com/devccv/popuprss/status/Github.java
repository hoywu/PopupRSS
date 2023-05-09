package com.devccv.popuprss.status;

import com.devccv.popuprss.util.ResourceBundleUtil;

public final class Github implements Status {
    public boolean isValid() {
        return true;
    }

    public String getVersion() {
        return ResourceBundleUtil.getStringValue("current_version");
    }
}
