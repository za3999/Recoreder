package com.permission;

import android.content.Context;

import com.cf.common.permission.IPermissionService;
import com.cf.common.permission.PermissionCallBack;
import com.google.auto.service.AutoService;
import com.permission.util.AndroidMPermissionHelper;

@AutoService(IPermissionService.class)
public class PermissionServiceImp implements IPermissionService {

    @Override
    public void checkPermission(Context context, PermissionCallBack callBack, String... permissions) {
        AndroidMPermissionHelper.checkPermission(context, callBack, permissions);
    }

}
