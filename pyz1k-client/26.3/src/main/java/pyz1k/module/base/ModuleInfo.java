/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.module.base;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import pyz1k.module.base.Category;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    public String name();

    public String desc();

    public Category category();

    public boolean enabledByDefault() default false;
}

