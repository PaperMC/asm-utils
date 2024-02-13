package io.papermc.asm.rules.generate;

import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Executable;
import java.util.Map;

public interface GeneratedMethodHolder {

    ClassDesc generatedMethodOwner();

    default ClassDesc staticRedirectOwner() {
        return this.generatedMethodOwner();
    }

    void generateMethod(final Map.Entry<Executable, ? extends MethodTypeDesc> pair, final RewriteRule.MethodGeneratorFactory factory);
}
