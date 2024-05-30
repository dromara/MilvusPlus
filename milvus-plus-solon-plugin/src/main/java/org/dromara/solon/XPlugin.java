package org.dromara.solon;

import org.dromara.solon.entity.MilvusProperties;
import org.dromara.solon.service.MilvusInit;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

public class XPlugin implements Plugin {

    public void start(AppContext context) throws Throwable {
        context.beanMake(MilvusProperties.class);
        context.beanMake(MilvusInit.class);
    }
}
