package com.cf.common.permission;

import android.content.Context;

public interface IPermissionService {

    void checkPermission(Context context, PermissionCallBack callBack, String... permissions);

}
