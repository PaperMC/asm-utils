package io.papermc.classfile.method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestMethodNamePredicate {

    @Test
    void exactMatchSingleNameMatchesExact() {
        final MethodNamePredicate predicate = MethodNamePredicate.exact("doThing");
        assertThat(predicate.test("doThing")).isTrue();
    }

    @Test
    void exactMatchSingleNameNoMatchOther() {
        final MethodNamePredicate predicate = MethodNamePredicate.exact("doThing");
        assertThat(predicate.test("doOtherThing")).isFalse();
    }

    @Test
    void exactMatchMultipleNamesMatchesAny() {
        final MethodNamePredicate predicate = MethodNamePredicate.exact("doThing", "doOtherThing");
        assertThat(predicate.test("doThing")).isTrue();
        assertThat(predicate.test("doOtherThing")).isTrue();
    }

    @Test
    void exactMatchMultipleNamesNoMatchUnknown() {
        final MethodNamePredicate predicate = MethodNamePredicate.exact("doThing", "doOtherThing");
        assertThat(predicate.test("doThirdThing")).isFalse();
    }

    @Test
    void exactMatchRejectsInitMethodName() {
        assertThatThrownBy(() -> MethodNamePredicate.exact("<init>"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prefixMatchMatchesPrefix() {
        final MethodNamePredicate predicate = MethodNamePredicate.prefix("get");
        assertThat(predicate.test("getEntity")).isTrue();
        assertThat(predicate.test("getName")).isTrue();
    }

    @Test
    void prefixMatchNoMatchDifferentPrefix() {
        final MethodNamePredicate predicate = MethodNamePredicate.prefix("get");
        assertThat(predicate.test("setEntity")).isFalse();
        assertThat(predicate.test("get")).isTrue(); // exact prefix match also counts
    }

    @Test
    void prefixMatchNoMatchShorterThanPrefix() {
        final MethodNamePredicate predicate = MethodNamePredicate.prefix("getEntity");
        assertThat(predicate.test("get")).isFalse();
    }

    @Test
    void prefixMatchRejectsInitPrefix() {
        assertThatThrownBy(() -> MethodNamePredicate.prefix("<init>"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void prefixMatchRejectsPartialInitPrefix() {
        assertThatThrownBy(() -> MethodNamePredicate.prefix("<ini"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorMatchesInit() {
        final MethodNamePredicate predicate = MethodNamePredicate.constructor();
        assertThat(predicate.test("<init>")).isTrue();
    }

    @Test
    void constructorNoMatchNonInit() {
        final MethodNamePredicate predicate = MethodNamePredicate.constructor();
        assertThat(predicate.test("doThing")).isFalse();
        assertThat(predicate.test("init")).isFalse();
    }

    @Test
    void constructorReturnsSingleton() {
        assertThat(MethodNamePredicate.constructor()).isSameAs(MethodNamePredicate.constructor());
    }
}
