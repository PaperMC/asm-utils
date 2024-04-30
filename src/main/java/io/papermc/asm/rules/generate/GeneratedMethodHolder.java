package io.papermc.asm.rules.generate;

import io.papermc.asm.rules.RewriteRule;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

public interface GeneratedMethodHolder {

    void generateMethod(RewriteRule.GeneratorAdapterFactory factory, MethodCallData modified, MethodCallData original);

    interface CallData {
        int opcode();
    }

    record MethodCallData(int opcode, ClassDesc owner, String name, MethodTypeDesc descriptor, boolean isInvokeDynamic) implements CallData {
    }

    void generateConstructor(RewriteRule.GeneratorAdapterFactory factory, MethodCallData modified, ConstructorCallData original);

    record ConstructorCallData(int opcode, ClassDesc owner, MethodTypeDesc descriptor) implements CallData {
    }
}
