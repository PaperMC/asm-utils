package io.papermc.asm.rules.builder.matcher.method;

public interface MethodTypeMatcherBuilder<B> {

    default B virtual() {
        return this.type(MethodType.VIRTUAL);
    }

    default B statik() {
        return this.type(MethodType.STATIC);
    }

    default B itf() {
        return this.type(MethodType.INTERFACE);
    }

    B type(final MethodType... types);
}
